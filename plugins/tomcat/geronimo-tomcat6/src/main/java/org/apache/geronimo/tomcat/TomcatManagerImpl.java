/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.tomcat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.KeyManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.EditableConfigurationManager;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.management.geronimo.WebAccessLog;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.connector.AJP13ConnectorGBean;
import org.apache.geronimo.tomcat.connector.ConnectorGBean;
import org.apache.geronimo.tomcat.connector.Http11APRConnectorGBean;
import org.apache.geronimo.tomcat.connector.Http11ConnectorGBean;
import org.apache.geronimo.tomcat.connector.Http11NIOConnectorGBean;
import org.apache.geronimo.tomcat.connector.Https11APRConnectorGBean;
import org.apache.geronimo.tomcat.connector.Https11ConnectorGBean;
import org.apache.geronimo.tomcat.connector.Https11NIOConnectorGBean;
import org.apache.geronimo.tomcat.connector.TomcatWebConnector;

/**
 * Tomcat implementation of the WebManager management API.  Knows how to
 * manipulate other Tomcat objects for management purposes.
 *
 * @version $Rev$ $Date$
 */
public class TomcatManagerImpl implements WebManager {
    private final static Log log = LogFactory.getLog(TomcatManagerImpl.class);
    private final Kernel kernel;
    
    private static final ConnectorType HTTP_BIO = new ConnectorType(Messages.getString("TomcatManagerImpl.0")); //$NON-NLS-1$
    private static final ConnectorType HTTPS_BIO = new ConnectorType(Messages.getString("TomcatManagerImpl.1")); //$NON-NLS-1$
    private static final ConnectorType HTTP_NIO = new ConnectorType(Messages.getString("TomcatManagerImpl.2")); //$NON-NLS-1$
    private static final ConnectorType HTTPS_NIO = new ConnectorType(Messages.getString("TomcatManagerImpl.3")); //$NON-NLS-1$
    private static final ConnectorType HTTP_APR = new ConnectorType(Messages.getString("TomcatManagerImpl.4")); //$NON-NLS-1$
    private static final ConnectorType HTTPS_APR = new ConnectorType(Messages.getString("TomcatManagerImpl.5")); //$NON-NLS-1$
    private static final ConnectorType AJP = new ConnectorType(Messages.getString("TomcatManagerImpl.6")); //$NON-NLS-1$
    private static List<ConnectorType> CONNECTOR_TYPES = Arrays.asList(
            HTTP_BIO,
            HTTPS_BIO,
            HTTP_NIO,
            HTTPS_NIO,
            HTTP_APR,
            HTTPS_APR,
            AJP
    );
    
    private static Map<ConnectorType, List<ConnectorAttribute>> CONNECTOR_ATTRIBUTES = new HashMap<ConnectorType, List<ConnectorAttribute>>();

    static {
        //******************* HTTP - BIO CONNECTOR
        List<ConnectorAttribute> connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        addHttpConnectorAttributes(connectorAttributes);
        CONNECTOR_ATTRIBUTES.put(HTTP_BIO, connectorAttributes);
        
        //******************* HTTPS - BIO CONNECTOR
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        addHttpConnectorAttributes(connectorAttributes);
        addSslConnectorAttributes(connectorAttributes);
        setAttribute(connectorAttributes, "port", 8443); // SSL port
        CONNECTOR_ATTRIBUTES.put(HTTPS_BIO, connectorAttributes);
        
        //******************* HTTP - NIO CONNECTOR
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        addHttpConnectorAttributes(connectorAttributes);
        addNioConnectorAttributes(connectorAttributes);
        CONNECTOR_ATTRIBUTES.put(HTTP_NIO, connectorAttributes);
        
        //******************* HTTPS - NIO CONNECTOR
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        addHttpConnectorAttributes(connectorAttributes);
        addSslConnectorAttributes(connectorAttributes);
        addNioConnectorAttributes(connectorAttributes);
        setAttribute(connectorAttributes, "port", 8443); // SSL port
        CONNECTOR_ATTRIBUTES.put(HTTPS_NIO, connectorAttributes);
        
        //******************* HTTP - APR CONNECTOR
        connectorAttributes = new ArrayList<ConnectorAttribute>();        
        addCommonConnectorAttributes(connectorAttributes);
        addHttpConnectorAttributes(connectorAttributes);
        addAprConnectorAttributes(connectorAttributes);
        CONNECTOR_ATTRIBUTES.put(HTTP_APR, connectorAttributes);
        
        //******************* HTTPS - APR CONNECTOR
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        addHttpConnectorAttributes(connectorAttributes);
        addAprConnectorAttributes(connectorAttributes);
        //APR SSL specific values, different from BIO and NIO SSL because it uses openssl
        connectorAttributes.add(new ConnectorAttribute<String>("sslProtocol", "all", Messages.getString("TomcatManagerImpl.11"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<String>("sslCipherSuite", "ALL", Messages.getString("TomcatManagerImpl.14"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<String>("sslCertificateFile", "", Messages.getString("TomcatManagerImpl.17"), String.class, true)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<String>("sslCertificateKeyFile", null, Messages.getString("TomcatManagerImpl.19"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("sslPassword", null, Messages.getString("TomcatManagerImpl.21"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("sslVerifyClient", "none", Messages.getString("TomcatManagerImpl.24"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<Integer>("sslVerifyDepth", 10, Messages.getString("TomcatManagerImpl.26"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("sslCACertificateFile", null, Messages.getString("TomcatManagerImpl.28"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("sslCACertificatePath", null, Messages.getString("TomcatManagerImpl.30"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("sslCertificateChainFile", null, Messages.getString("TomcatManagerImpl.32"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("sslCARevocationFile", null, Messages.getString("TomcatManagerImpl.34"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("sslCARevocationPath", null, Messages.getString("TomcatManagerImpl.36"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$
        setAttribute(connectorAttributes, "port", 8443); // SSL port
        CONNECTOR_ATTRIBUTES.put(HTTPS_APR, connectorAttributes);
        
        //******************* AJP CONNECTOR
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        //AJP Attributes, see http://tomcat.apache.org/tomcat-6.0-doc/config/ajp.html
        connectorAttributes.add(new ConnectorAttribute<String>("host", "0.0.0.0", Messages.getString("TomcatManagerImpl.40"), String.class, true)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<Integer>("port", 8009, Messages.getString("TomcatManagerImpl.42"), Integer.class, true)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("backlog", 10, Messages.getString("TomcatManagerImpl.44"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("bufferSize", -1, Messages.getString("TomcatManagerImpl.46"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("connectionTimeout", org.apache.coyote.ajp.Constants.DEFAULT_CONNECTION_TIMEOUT, Messages.getString("TomcatManagerImpl.48"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("keepAliveTimeout", org.apache.coyote.ajp.Constants.DEFAULT_CONNECTION_TIMEOUT, Messages.getString("TomcatManagerImpl.50"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxThreads", 40, Messages.getString("TomcatManagerImpl.52"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("minSpareThreads", 10, Messages.getString("TomcatManagerImpl.54"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxSpareThreads", 100, Messages.getString("TomcatManagerImpl.56"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("tcpNoDelay", true, Messages.getString("TomcatManagerImpl.58"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("tomcatAuthentication", true, Messages.getString("TomcatManagerImpl.60"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        CONNECTOR_ATTRIBUTES.put(AJP, connectorAttributes);
    }
    
    private static Map<ConnectorType, GBeanInfo> CONNECTOR_GBEAN_INFOS = new HashMap<ConnectorType, GBeanInfo>();

    static {
        CONNECTOR_GBEAN_INFOS.put(HTTP_BIO, Http11ConnectorGBean.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(HTTPS_BIO, Https11ConnectorGBean.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(HTTP_NIO, Http11NIOConnectorGBean.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(HTTPS_NIO, Https11NIOConnectorGBean.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(HTTP_APR, Http11APRConnectorGBean.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(HTTPS_APR, Https11APRConnectorGBean.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(AJP, AJP13ConnectorGBean.GBEAN_INFO);
    }
    
    public TomcatManagerImpl(Kernel kernel) {
        this.kernel = kernel;
    }

    public String getProductName() {
        return "Tomcat";
    }

    /**
     * Gets the network containers.
     */
    public Object[] getContainers() {
        ProxyManager proxyManager = kernel.getProxyManager();
        AbstractNameQuery query = new AbstractNameQuery(TomcatWebContainer.class.getName());
        Set names = kernel.listGBeans(query);
        TomcatWebContainer[] results = new TomcatWebContainer[names.size()];
        int i=0;
        for (Iterator it = names.iterator(); it.hasNext(); i++) {
            AbstractName name = (AbstractName) it.next();
            results[i] = (TomcatWebContainer) proxyManager.createProxy(name, TomcatWebContainer.class.getClassLoader());
        }
        return results;
    }

    /**
     * Gets the protocols which this container can configure connectors for.
     */
    public String[] getSupportedProtocols() {
        return new String[]{PROTOCOL_HTTP, PROTOCOL_HTTPS, PROTOCOL_AJP};
    }

    /**
     * Removes a connector.  This shuts it down if necessary, and removes it from the server environment.  It must be a
     * connector that uses this network technology.
     * @param connectorName
     */
    public void removeConnector(AbstractName connectorName) {
        try {
            GBeanInfo info = kernel.getGBeanInfo(connectorName);
            boolean found = false;
            Set intfs = info.getInterfaces();
            for (Iterator it = intfs.iterator(); it.hasNext();) {
                String intf = (String) it.next();
                if (intf.equals(TomcatWebConnector.class.getName())) {
                    found = true;
                }
            }
            if (!found) {
                throw new GBeanNotFoundException(connectorName);
            }
            EditableConfigurationManager mgr = ConfigurationUtil.getEditableConfigurationManager(kernel);
            if(mgr != null) {
                try {
                    mgr.removeGBeanFromConfiguration(connectorName.getArtifact(), connectorName);
                } catch (InvalidConfigException e) {
                    log.error("Unable to add GBean", e);
                } finally {
                    ConfigurationUtil.releaseConfigurationManager(kernel, mgr);
                }
            } else {
                log.warn("The ConfigurationManager in the kernel does not allow editing");
            }
        } catch (GBeanNotFoundException e) {
            log.warn("No such GBean '" + connectorName + "'"); //todo: what if we want to remove a failed GBean?
        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * Gets the ObjectNames of any existing connectors for this network technology for the specified protocol.
     *
     * @param protocol A protocol as returned by getSupportedProtocols
     */
    public NetworkConnector[] getConnectors(String protocol) {
        if(protocol == null) {
            return getConnectors();
        }
        List result = new ArrayList();
        ProxyManager proxyManager = kernel.getProxyManager();
        AbstractNameQuery query = new AbstractNameQuery(TomcatWebConnector.class.getName());
        Set names = kernel.listGBeans(query);
        for (Iterator it = names.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            try {
                if (kernel.getAttribute(name, "protocol").equals(protocol)) {
                    result.add(proxyManager.createProxy(name, TomcatWebConnector.class.getClassLoader()));
                }
            } catch (Exception e) {
                log.error("Unable to check the protocol for a connector", e);
            }
        }
        return (TomcatWebConnector[]) result.toArray(new TomcatWebConnector[names.size()]);
    }

    public WebAccessLog getAccessLog(WebContainer container) {
        AbstractNameQuery query = new AbstractNameQuery(TomcatLogManager.class.getName());
        Set names = kernel.listGBeans(query);
        if(names.size() == 0) {
            return null;
        } else if(names.size() > 1) {
            throw new IllegalStateException("Should not be more than one Tomcat access log manager");
        }
        return (WebAccessLog) kernel.getProxyManager().createProxy((AbstractName)names.iterator().next(), TomcatLogManager.class.getClassLoader());
    }

    public List<ConnectorType> getConnectorTypes() {
        return CONNECTOR_TYPES;
    }

    public List<ConnectorAttribute> getConnectorAttributes(ConnectorType connectorType) {
        return ConnectorAttribute.copy(CONNECTOR_ATTRIBUTES.get(connectorType));
    }

    public AbstractName getConnectorConfiguration(ConnectorType connectorType, List<ConnectorAttribute> connectorAttributes, WebContainer container, String uniqueName) {
        GBeanInfo gbeanInfo = CONNECTOR_GBEAN_INFOS.get(connectorType);
        AbstractName containerName = kernel.getAbstractNameFor(container);
        AbstractName name = kernel.getNaming().createSiblingName(containerName, uniqueName, NameFactory.GERONIMO_SERVICE);
        GBeanData gbeanData = new GBeanData(name, gbeanInfo);
        gbeanData.setAttribute("name", uniqueName);
        gbeanData.setReferencePattern(ConnectorGBean.CONNECTOR_CONTAINER_REFERENCE, containerName);
        for (ConnectorAttribute connectorAttribute : connectorAttributes) {
            gbeanData.setAttribute(connectorAttribute.getAttributeName(), connectorAttribute.getValue());
        }
        AbstractNameQuery query = new AbstractNameQuery(ServerInfo.class.getName());
        Set set = kernel.listGBeans(query);
        AbstractName serverInfo = (AbstractName)set.iterator().next();
        gbeanData.setReferencePattern("ServerInfo", serverInfo);

        EditableConfigurationManager mgr = ConfigurationUtil.getEditableConfigurationManager(kernel);
        if (mgr != null) {
            try {
                mgr.addGBeanToConfiguration(containerName.getArtifact(), gbeanData, false);
            } catch (InvalidConfigException e) {
                log.error("Unable to add GBean", e);
                return null;
            } finally {
                ConfigurationUtil.releaseConfigurationManager(kernel, mgr);
            }
        } else {
            log.warn("The ConfigurationManager in the kernel does not allow editing");
            return null;
        }
        return name;
    }

    /**
     * Gets the ObjectNames of any existing connectors associated with this network technology.
     */
    public NetworkConnector[] getConnectors() {
        ProxyManager proxyManager = kernel.getProxyManager();
        AbstractNameQuery query = new AbstractNameQuery(TomcatWebConnector.class.getName());
        Set names = kernel.listGBeans(query);
        TomcatWebConnector[] results = new TomcatWebConnector[names.size()];
        int i=0;
        for (Iterator it = names.iterator(); it.hasNext(); i++) {
            AbstractName name = (AbstractName) it.next();
            results[i] = (TomcatWebConnector) proxyManager.createProxy(name, TomcatWebConnector.class.getClassLoader());
        }
        return results;
    }

    /**
     * Gets the ObjectNames of any existing connectors for the specified container for the specified protocol.
     *
     * @param protocol A protocol as returned by getSupportedProtocols
     */
    public NetworkConnector[] getConnectorsForContainer(Object container, String protocol) {
        if(protocol == null) {
            return getConnectorsForContainer(container);
        }
        AbstractName containerName = kernel.getAbstractNameFor(container);
        ProxyManager mgr = kernel.getProxyManager();
        try {
            List results = new ArrayList();
            AbstractNameQuery query = new AbstractNameQuery(TomcatWebConnector.class.getName());
            Set set = kernel.listGBeans(query); // all Tomcat connectors
            for (Iterator it = set.iterator(); it.hasNext();) {
                AbstractName name = (AbstractName) it.next(); // a single Tomcat connector
                GBeanData data = kernel.getGBeanData(name);
                ReferencePatterns refs = data.getReferencePatterns(ConnectorGBean.CONNECTOR_CONTAINER_REFERENCE);
                if(containerName.equals(refs.getAbstractName())) {
                    try {
                        String testProtocol = (String) kernel.getAttribute(name, "protocol");
                        if(testProtocol != null && testProtocol.equals(protocol)) {
                            results.add(mgr.createProxy(name, TomcatWebConnector.class.getClassLoader()));
                        }
                    } catch (Exception e) {
                        log.error("Unable to look up protocol for connector '"+name+"'",e);
                    }
                    break;
                }
            }
            return (TomcatWebConnector[]) results.toArray(new TomcatWebConnector[results.size()]);
        } catch (Exception e) {
            throw (IllegalArgumentException)new IllegalArgumentException("Unable to look up connectors for Tomcat container '"+containerName +"': ").initCause(e);
        }
    }

    /**
     * Gets the ObjectNames of any existing connectors for the specified container.
     */
    public NetworkConnector[] getConnectorsForContainer(Object container) {
        AbstractName containerName = kernel.getAbstractNameFor(container);
        ProxyManager mgr = kernel.getProxyManager();
        try {
            List results = new ArrayList();
            AbstractNameQuery query = new AbstractNameQuery(TomcatWebConnector.class.getName());
            Set set = kernel.listGBeans(query); // all Tomcat connectors
            for (Iterator it = set.iterator(); it.hasNext();) {
                AbstractName name = (AbstractName) it.next(); // a single Tomcat connector
                GBeanData data = kernel.getGBeanData(name);
                ReferencePatterns refs = data.getReferencePatterns(ConnectorGBean.CONNECTOR_CONTAINER_REFERENCE);
                if (containerName.equals(refs.getAbstractName())) {
                    results.add(mgr.createProxy(name, TomcatWebConnector.class.getClassLoader()));
                }
            }
            return (TomcatWebConnector[]) results.toArray(new TomcatWebConnector[results.size()]);
        } catch (Exception e) {
            throw (IllegalArgumentException) new IllegalArgumentException("Unable to look up connectors for Tomcat container '"+containerName).initCause(e);
        }
    }

    // see http://tomcat.apache.org/tomcat-6.0-doc/config/http.html    
    private static void addCommonConnectorAttributes(List<ConnectorAttribute> connectorAttributes) {
        connectorAttributes.add(new ConnectorAttribute<Boolean>("allowTrace", false, Messages.getString("TomcatManagerImpl.80"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("emptySessionPath", false, Messages.getString("TomcatManagerImpl.82"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("enableLookups", true, Messages.getString("TomcatManagerImpl.84"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxPostSize", 2097152, Messages.getString("TomcatManagerImpl.86"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxSavePostSize", 4096, Messages.getString("TomcatManagerImpl.88"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("proxyName", null, Messages.getString("TomcatManagerImpl.90"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("proxyPort", 0, Messages.getString("TomcatManagerImpl.92"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("redirectPort", 8443, Messages.getString("TomcatManagerImpl.94"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("uriEncoding", "ISO-8859-1", Messages.getString("TomcatManagerImpl.97"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useBodyEncodingForURI", false, Messages.getString("TomcatManagerImpl.99"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useIPVHosts", false, Messages.getString("TomcatManagerImpl.101"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("xpoweredBy", false, Messages.getString("TomcatManagerImpl.103"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    // see http://tomcat.apache.org/tomcat-6.0-doc/config/http.html
    private static void addHttpConnectorAttributes(List<ConnectorAttribute> connectorAttributes) {
        connectorAttributes.add(new ConnectorAttribute<Integer>("acceptCount", 10, Messages.getString("TomcatManagerImpl.105"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("address", "0.0.0.0", Messages.getString("TomcatManagerImpl.108"), String.class, true)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<Integer>("bufferSize", 2048, Messages.getString("TomcatManagerImpl.110"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("compressableMimeType", "text/html,text/xml,text/plain", Messages.getString("TomcatManagerImpl.113"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<String>("compression", "off", Messages.getString("TomcatManagerImpl.116"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<Integer>("connectionLinger", -1, Messages.getString("TomcatManagerImpl.118"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("connectionTimeout", 60000, Messages.getString("TomcatManagerImpl.120"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("executor", null, Messages.getString("TomcatManagerImpl.122"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("keepAliveTimeout", 60000, Messages.getString("TomcatManagerImpl.124"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("disableUploadTimeout", true, Messages.getString("TomcatManagerImpl.126"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxHttpHeaderSize", 4096, Messages.getString("TomcatManagerImpl.128"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxKeepAliveRequests", 100, Messages.getString("TomcatManagerImpl.130"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxThreads", 40, Messages.getString("TomcatManagerImpl.132"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("minSpareThreads", 10, Messages.getString("TomcatManagerImpl.134"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxSpareThreads", 100, Messages.getString("TomcatManagerImpl.136"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("noCompressionUserAgents", "", Messages.getString("TomcatManagerImpl.139"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<Integer>("port", 8080, Messages.getString("TomcatManagerImpl.141"), Integer.class, true)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("restrictedUserAgents", "", Messages.getString("TomcatManagerImpl.144"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<String>("server", "", Messages.getString("TomcatManagerImpl.147"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socketBuffer", 9000, Messages.getString("TomcatManagerImpl.149"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("tcpNoDelay", true, Messages.getString("TomcatManagerImpl.151"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("threadPriority", Thread.NORM_PRIORITY, Messages.getString("TomcatManagerImpl.153"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    // see http://tomcat.apache.org/tomcat-6.0-doc/config/http.html
    private static void addSslConnectorAttributes(List<ConnectorAttribute> connectorAttributes) {
        connectorAttributes.add(new ConnectorAttribute<String>("algorithm", KeyManagerFactory.getDefaultAlgorithm(), Messages.getString("TomcatManagerImpl.155"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("clientAuth", false, Messages.getString("TomcatManagerImpl.157"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("keystoreFile", "", Messages.getString("TomcatManagerImpl.160"), String.class, true)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<String>("keystorePass", null, Messages.getString("TomcatManagerImpl.162"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("keystoreType", "JKS", Messages.getString("TomcatManagerImpl.165"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<String>("sslProtocol", "TLS", Messages.getString("TomcatManagerImpl.168"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<String>("ciphers", "", Messages.getString("TomcatManagerImpl.171"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<String>("keyAlias", null, Messages.getString("TomcatManagerImpl.173"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("truststoreFile", null, Messages.getString("TomcatManagerImpl.175"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("truststorePass", null, Messages.getString("TomcatManagerImpl.177"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("truststoreType", null, Messages.getString("TomcatManagerImpl.179"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    // see http://tomcat.apache.org/tomcat-6.0-doc/config/http.html
    private static void addNioConnectorAttributes(List<ConnectorAttribute> connectorAttributes) {
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useSendfile", true, Messages.getString("TomcatManagerImpl.181"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useExecutor", true, Messages.getString("TomcatManagerImpl.183"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("acceptorThreadCount", 1, Messages.getString("TomcatManagerImpl.185"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("pollerThreadCount", 1, Messages.getString("TomcatManagerImpl.187"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("pollerThreadPriority", Thread.NORM_PRIORITY, Messages.getString("TomcatManagerImpl.189"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("acceptorThreadPriority", Thread.NORM_PRIORITY, Messages.getString("TomcatManagerImpl.191"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("selectorTimeout", 1000, Messages.getString("TomcatManagerImpl.193"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useComet", true, Messages.getString("TomcatManagerImpl.195"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("processCache", 200, Messages.getString("TomcatManagerImpl.197"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("socket_directBuffer", false, Messages.getString("TomcatManagerImpl.199"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_rxBufSize", 25188, Messages.getString("TomcatManagerImpl.201"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_txBufSize", 43800, Messages.getString("TomcatManagerImpl.203"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_appReadBufSize", 8192, Messages.getString("TomcatManagerImpl.205"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_appWriteBufSize", 8192, Messages.getString("TomcatManagerImpl.207"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_bufferPool", 500, Messages.getString("TomcatManagerImpl.209"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_bufferPoolSize", 104857600, Messages.getString("TomcatManagerImpl.211"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_processorCache", 500, Messages.getString("TomcatManagerImpl.213"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_keyCache", 500, Messages.getString("TomcatManagerImpl.215"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_eventCache", 500, Messages.getString("TomcatManagerImpl.217"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("socket_tcpNoDelay", false, Messages.getString("TomcatManagerImpl.219"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("socket_soKeepAlive", false, Messages.getString("TomcatManagerImpl.221"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("socket_ooBInline", true, Messages.getString("TomcatManagerImpl.223"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("socket_soReuseAddress", true, Messages.getString("TomcatManagerImpl.225"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("socket_soLingerOn", true, Messages.getString("TomcatManagerImpl.227"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_soLingerTime", 25, Messages.getString("TomcatManagerImpl.229"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_soTimeout", 5000, Messages.getString("TomcatManagerImpl.231"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_soTrafficClass", (0x04 | 0x08 | 0x010), Messages.getString("TomcatManagerImpl.233"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_performanceConnectionTime", 1, Messages.getString("TomcatManagerImpl.235"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_performanceLatency", 0, Messages.getString("TomcatManagerImpl.237"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_performanceBandwidth", 1, Messages.getString("TomcatManagerImpl.239"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("selectorPool_maxSelectors", 200, Messages.getString("TomcatManagerImpl.241"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("selectorPool_maxSpareSelectors", -1, Messages.getString("TomcatManagerImpl.243"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("command_line_options", true, Messages.getString("TomcatManagerImpl.245"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("oomParachute", 1048576, Messages.getString("TomcatManagerImpl.247"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    // http://tomcat.apache.org/tomcat-6.0-doc/apr.html
    private static void addAprConnectorAttributes(List<ConnectorAttribute> connectorAttributes) {
        connectorAttributes.add(new ConnectorAttribute<Integer>("pollTime", 2000, Messages.getString("TomcatManagerImpl.249"), Integer.class, true)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("pollerSize", 8192, Messages.getString("TomcatManagerImpl.251"), Integer.class, true)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useSendfile", true, Messages.getString("TomcatManagerImpl.253"), Boolean.class, true)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("sendfileSize", 1024, Messages.getString("TomcatManagerImpl.255"), Integer.class, true)); //$NON-NLS-1$ //$NON-NLS-2$
    }
       
    private static <T> void setAttribute (List<ConnectorAttribute> connectorAttributes, String attributeName, T value) {
        for (ConnectorAttribute connectorAttribute : connectorAttributes) {
            if (connectorAttribute.getAttributeName().equals(attributeName)) {
                connectorAttribute.setValue(value);
                return;
            }
        }
    }
    

    public ConnectorType getConnectorType(AbstractName connectorName) {
        ConnectorType connectorType = null; 
        try {
            GBeanInfo info = kernel.getGBeanInfo(connectorName);
            boolean found = false;
            Set intfs = info.getInterfaces();
            for (Iterator it = intfs.iterator(); it.hasNext() && !found;) {
                String intf = (String) it.next();
                if (intf.equals(TomcatWebConnector.class.getName())) {
                    found = true;
                }
            }
            if (!found) {
                throw new GBeanNotFoundException(connectorName);
            }
            String searchingFor = info.getName();
            for (Entry<ConnectorType, GBeanInfo> entry : CONNECTOR_GBEAN_INFOS.entrySet() ) {
                String candidate = entry.getValue().getName();
                if (candidate.equals(searchingFor)) {
                    return entry.getKey();
                }
            }
        } catch (GBeanNotFoundException e) {
            log.warn("No such GBean '" + connectorName + "'");
        } catch (Exception e) {
            log.error(e);
        }
            
        return connectorType;
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Tomcat Web Manager", TomcatManagerImpl.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addInterface(WebManager.class);
        infoFactory.setConstructor(new String[] {"kernel"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}