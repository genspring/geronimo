/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.deployment.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.jmx.JMXUtil;

/**
 * A proxy for a repository of components that can accessed remotely and
 * downloaded to the local machine.
 *
 * @jmx:mbean
 *
 * @version $Revision: 1.3 $ $Date: 2003/09/01 20:38:49 $
 */
public class ComponentRepository
    implements ComponentRepositoryMBean,MBeanRegistration
{
    private final static Log log = LogFactory.getLog(ComponentRepository.class);
    
    private final File localRoot;
    private MBeanServer server;
    private Set remoteRoots = new HashSet();

    /**
     * Construct a repository using the specified local directory as root.
     * It will be created if it does not exists
     *
     * @jmx:managed-constructor
     *
     * @param localRoot the local root directory
     */
    public ComponentRepository(File localRoot) {
        if (!localRoot.exists()) {
            localRoot.mkdirs();
        } else if (!localRoot.isDirectory()) {
            throw new IllegalArgumentException("Local root is not a directory: " + localRoot);
        }
        this.localRoot = localRoot;
    }

    public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws Exception {
        this.server = mBeanServer;
        return objectName;
    }

    public void postRegister(Boolean aBoolean) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }
    
    /**
     * @jmx:managed-attribute
     */
    public File getLocalRoot() {
        return localRoot;
    }
    
    /**
     * @jmx:managed-attribute
     */
    public Set getRemoteRoots() {
        return Collections.unmodifiableSet(remoteRoots);
    }
    
    /**
     * @jmx:managed-operation
     */
    public void addRemoteRoot(URL root) {
        remoteRoots.add(root);
    }
    
    /**
     * @jmx:managed-operation
     */
    public void removeRemoteRoot(URL root) {
        remoteRoots.remove(root);
    }
    
    /**
     * @jmx:managed-operation
     */
    public void ensureLocal(String name, String version, String location) {
        // @todo get rid of this and use a property editor
        ComponentDescription desc = new ComponentDescription(name, version, location);
        ensureLocal(desc);
    }
    
    /**
     * @jmx:managed-operation
     */
    public boolean ensureLocal(ComponentDescription desc) {
        String location = desc.getLocation();
        File localFile = new File(localRoot, location);
        if (localFile.exists()) {
            // is exists - we do not check for SNAPSHOT updates
            log.debug("Local copy exists for "+location);
            return true;
        }

        for (Iterator i = remoteRoots.iterator(); i.hasNext();) {
            URL root = (URL) i.next();
            URL remoteURL = null;
            try {
                remoteURL = new URL(root, location);
            } catch (MalformedURLException e) {
                continue;
            }
            log.debug("Checking for archive at "+remoteURL);

            InputStream is = null;
            FileOutputStream os = null;
            try {
                URLConnection con = remoteURL.openConnection();
                con.connect();

                is = con.getInputStream();

                log.debug("Downloading archive "+remoteURL);
                localFile.getParentFile().mkdirs();
                localFile.createNewFile();
                os = new FileOutputStream(localFile);
                byte[] buffer = new byte[4096];
                int count;
                while ((count = is.read(buffer)) > 0) {
                    os.write(buffer, 0, count);
                }
                os.close();
                is.close();
                log.debug("Downloaded archive "+remoteURL);
                return true;
            } catch (IOException e) {
                // remove a potentially partially copied file
                localFile.delete();
                continue;
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * @jmx:managed-operation
     */
    public void removeLocal(String name, String version, String location) {
        // @todo get rid of this and use a property editor
        ComponentDescription desc = new ComponentDescription(name, version, location);
        removeLocal(desc);
    }
    
    /**
     * @jmx:managed-operation
     */
    public void removeLocal(ComponentDescription desc) {
        String location = desc.getLocation();
        File localFile = new File(localRoot, location);
        if (localFile.exists()) {
            localFile.delete();
        }
    }
    
    /**
     * @jmx:managed-operation
     */
    public void deploy(String name, String version, String location) throws DeploymentException {
        // @todo get rid of this and use a property editor
        ComponentDescription desc = new ComponentDescription(name, version, location);
        deploy(desc);
    }
    
    /**
     * @jmx:managed-operation
     */
    public void deploy(ComponentDescription desc) throws DeploymentException {
        if (!ensureLocal(desc)) {
            throw new DeploymentException("Could not obtain local copy of "+desc.getName()+" "+desc.getVersion());
        }
        URL url = null;
        try {
            url = new File(localRoot, desc.getLocation()).toURL();
        } catch (MalformedURLException e) {
            throw new DeploymentException(e);
        }

        // @todo replace with relation-based proxy
        ObjectName controller = JMXUtil.getObjectName("geronimo.deployment:role=DeploymentController");
        try {
            server.invoke(controller, "deploy", new Object[] { url }, new String[] { "java.net.URL"});
        } catch (InstanceNotFoundException e) {
            throw new DeploymentException(e);
        } catch (MBeanException e) {
            if (e.getCause() instanceof DeploymentException) {
                throw (DeploymentException) e.getCause();
            } else {
                throw new DeploymentException(e);
            }
        } catch (ReflectionException e) {
            throw new DeploymentException(e);
        }
    }
}
