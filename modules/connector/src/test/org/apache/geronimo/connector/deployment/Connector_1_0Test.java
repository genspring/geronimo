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

package org.apache.geronimo.connector.deployment;

import java.net.URL;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.jar.JarOutputStream;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import javax.management.ObjectName;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DConfigBeanRoot;

import junit.framework.TestCase;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerResourceadapterType;
import org.apache.geronimo.xbeans.geronimo.GerResourceadapterInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectType;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.geronimo.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionmanagerType;
import org.apache.geronimo.xbeans.j2ee.connector_1_0.ConnectorDocument;
import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.deployment.ConfigurationCallback;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.connector.deployment.dconfigbean.ResourceAdapterDConfigBean;
import org.apache.geronimo.connector.deployment.dconfigbean.ConfigPropertySettingDConfigBean;
import org.apache.geronimo.connector.deployment.dconfigbean.AdminObjectDConfigBean;
import org.apache.geronimo.connector.deployment.dconfigbean.AdminObjectInstance;
import org.apache.geronimo.connector.deployment.dconfigbean.ConfigPropertySettings;
import org.apache.geronimo.connector.deployment.dconfigbean.ConnectionDefinitionDConfigBean;
import org.apache.geronimo.connector.deployment.dconfigbean.ConnectionDefinitionInstance;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/20 18:10:29 $
 *
 * */
public class Connector_1_0Test extends TestCase implements ConfigurationCallback {
    private URL j2eeDD;
    private URL geronimoDD;
    private URI configID = URI.create("geronimo/connector10/test");
    private Map gbeans = new HashMap();
    XmlOptions xmlOptions;
    private List errors;

    public void testLoadJ2eeDeploymentDescriptor() throws Exception {
        InputStream j2eeInputStream = j2eeDD.openStream();
        ConnectorDocument connectorDocument = ConnectorDocument.Factory.parse(j2eeInputStream);
        assertNotNull(connectorDocument.getConnector().getResourceadapter());
        if (!connectorDocument.validate(xmlOptions)) {
            fail(errors.toString());
        }
    }

    public void testLoadGeronimoDeploymentDescriptor() throws Exception {
        InputStream geronimoInputStream = geronimoDD.openStream();
        GerConnectorDocument connectorDocument = GerConnectorDocument.Factory.parse(geronimoInputStream);
        assertNotNull(connectorDocument.getConnector().getResourceadapter());
        if (!connectorDocument.validate(xmlOptions)) {
            fail(errors.toString());
        }

    }

    public void testDConfigBeans() throws Exception {
        MockRARDeployable deployable = new MockRARDeployable(j2eeDD);
        DDBeanRoot ddroot = deployable.getDDBeanRoot();
        DeploymentConfiguration rarConfiguration = new RARConfigurer().createConfiguration(deployable);
        DConfigBeanRoot root = rarConfiguration.getDConfigBeanRoot(ddroot);
        assertNotNull(root);

        //outbound
        DDBean[] connectionDefinitiondds = ddroot.getChildBean(root.getXpaths()[0]);
        assertEquals(1, connectionDefinitiondds.length);
        ConnectionDefinitionDConfigBean connectionDefinitionDConfigBean = (ConnectionDefinitionDConfigBean)root.getDConfigBean(connectionDefinitiondds[0]);
        assertNotNull(connectionDefinitionDConfigBean);
        ConnectionDefinitionInstance connectionDefinitionInstance1 = new ConnectionDefinitionInstance();
        connectionDefinitionDConfigBean.setConnectionDefinitionInstance(new ConnectionDefinitionInstance[] {connectionDefinitionInstance1});
        DDBean[] connectionDefinitionConfigPropDDs = connectionDefinitiondds[0].getChildBean("config-property");
        assertEquals(4, connectionDefinitionConfigPropDDs.length);
        ConfigPropertySettings connectionDefinitionSetting1 = connectionDefinitionInstance1.getConfigProperty()[0];
        connectionDefinitionSetting1.setConfigPropertyValue("TestCDValue1");
        //connection manager properties
        connectionDefinitionInstance1.setBlockingTimeout(3000);

        //check the results
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rarConfiguration.save(baos);
        baos.flush();
        byte[] bytes = baos.toByteArray();
        baos.close();
        InputStream is = new ByteArrayInputStream(bytes);
        GerConnectorDocument gcDoc = GerConnectorDocument.Factory.parse(is);
        GerResourceadapterType ra = gcDoc.getConnector().getResourceadapter();

        //connection definition
        GerConnectionDefinitionType connectionDefinitionType = ra.getOutboundResourceadapter().getConnectionDefinitionArray(0);
        GerConnectiondefinitionInstanceType connectiondefinitionInstanceType = connectionDefinitionType.getConnectiondefinitionInstanceArray(0);
        assertEquals("TestCDValue1", connectiondefinitionInstanceType.getConfigPropertySettingArray(0).getStringValue());
        //connection manager
        GerConnectionmanagerType connectionmanagerType = connectiondefinitionInstanceType.getConnectionmanager();
        assertEquals(3000, connectionmanagerType.getBlockingTimeout().intValue());

        //and read back into dconfigbeans
        rarConfiguration.restore(new ByteArrayInputStream(bytes));

        //outbound
        connectionDefinitionDConfigBean = (ConnectionDefinitionDConfigBean)root.getDConfigBean(connectionDefinitiondds[0]);
        assertNotNull(connectionDefinitionDConfigBean);
        ConnectionDefinitionInstance[] connectionDefinitionInstances = connectionDefinitionDConfigBean.getConnectionDefinitionInstance();
        connectionDefinitionSetting1 = connectionDefinitionInstances[0].getConfigProperty()[0];
        assertEquals("TestCDValue1", connectionDefinitionSetting1.getConfigPropertyValue());
        //connection manager
        assertEquals(3000, connectionDefinitionInstances[0].getBlockingTimeout());

    }

    public void testCreateConnector_1_0Module() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JarOutputStream jarOutputStream = new JarOutputStream(baos);
        ZipEntry entry = new ZipEntry("META-INF/ra.xml");
        jarOutputStream.putNextEntry(entry);

        InputStream j2eeInputStream = j2eeDD.openStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = j2eeInputStream.read(buffer)) > 0; ) {
            jarOutputStream.write(buffer, 0, length);
        }
        jarOutputStream.flush();
        jarOutputStream.closeEntry();
        jarOutputStream.close();

        InputStream moduleArchive = new ByteArrayInputStream(baos.toByteArray());
        InputStream geronimoInputStream = geronimoDD.openStream();
        GerConnectorDocument connectorDocument = GerConnectorDocument.Factory.parse(geronimoInputStream);
        RARConfigurationFactory rarConfigurationFactory = new RARConfigurationFactory(ObjectName.getInstance("geronimo.test:role=ConnectionTracker"));
        DeploymentModule connector_1_0Module = rarConfigurationFactory.createModule(moduleArchive, connectorDocument, configID, true);
        connector_1_0Module.init();
        connector_1_0Module.generateClassPath(this);
        connector_1_0Module.defineGBeans(this, this.getClass().getClassLoader());
    }

    protected void setUp() throws Exception {
        File docDir = new File("src/test-data/connector_1_0");
        j2eeDD = new File(docDir, "ra.xml").toURL();
        geronimoDD = new File(docDir, "geronimo-ra.xml").toURL();
        xmlOptions = new XmlOptions();
        xmlOptions.setLoadLineNumbers();
        errors = new ArrayList();
        xmlOptions.setErrorListener(errors);
    }

    public void addFile(URI path, InputStream source) throws IOException {
    }

    public void addToClasspath(URI uri) {
    }

    public void addGBean(ObjectName name, GBeanMBean gbean) {
        gbeans.put(name, gbean);
    }
}
