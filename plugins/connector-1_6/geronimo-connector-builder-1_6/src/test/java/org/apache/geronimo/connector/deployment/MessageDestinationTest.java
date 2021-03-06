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
package org.apache.geronimo.connector.deployment;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.connector.AdminObjectWrapperGBean;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.ConnectorModule;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.mock.MockConfigurationManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev:390932 $ $Date$
 */
public class MessageDestinationTest extends TestCase {

    private static final Naming naming = new Jsr77Naming();
    Configuration configuration;
    AbstractName baseName;
    AdminObjectRefBuilder adminObjectRefBuilder = new AdminObjectRefBuilder(null, new String[] {SchemaConversionUtils.J2EE_NAMESPACE});
    Module module;

    Map componentContext = new HashMap();

    protected void setUp() throws Exception {
        super.setUp();
        Artifact id = new Artifact("test", "test", "", "car");
        module  = new ConnectorModule(false, new AbstractName(id, Collections.singletonMap("name", "test")), null, null, "foo", null, null, null, null);
        ConfigurationManager configurationManager = new MockConfigurationManager();
        EARContext earContext = new EARContext(new File("foo"),
            null,
            new Environment(new Artifact("foo", "bar", "1.0", "car")),
            ConfigurationModuleType.EAR,
            naming,
            configurationManager,
            (Collection) null,
            null,
            null,
            null,
            null,
            null);
        module.setEarContext(earContext);
        module.setRootEarContext(earContext);
        configuration = earContext.getConfiguration();
        baseName = naming.createRootName(configuration.getId(), "testRoot", NameFactory.RESOURCE_ADAPTER_MODULE);
    }

    private static final String SPECDD1 = "<tmp xmlns=\"http://java.sun.com/xml/ns/j2ee\">" +
            "<message-destination><message-destination-name>d1</message-destination-name></message-destination>" +
            "<message-destination><message-destination-name>d2</message-destination-name></message-destination>" +
            "<message-destination-ref>" +
            "  <message-destination-ref-name>n1</message-destination-ref-name>" +
            "  <message-destination-type>java.lang.Object</message-destination-type>" +
            "  <message-destination-usage>Consumes</message-destination-usage>" +
            "  <message-destination-link>d1</message-destination-link>" +
            "</message-destination-ref>" +
            "<message-destination-ref>" +
            "  <message-destination-ref-name>n2</message-destination-ref-name>" +
            "  <message-destination-type>java.lang.Object</message-destination-type>" +
            "  <message-destination-usage>Consumes</message-destination-usage>" +
            "  <message-destination-link>d2</message-destination-link>" +
            "</message-destination-ref>" +
            "</tmp>";

    private static final String PLAN1 = "<tmp xmlns=\"http://geronimo.apache.org/xml/ns/naming-1.2\">" +
            "<message-destination>" +
            "  <message-destination-name>d1</message-destination-name>" +
            "  <admin-object-link>l1</admin-object-link>" +
            "</message-destination>" +
            "<message-destination>" +
            "  <message-destination-name>d2</message-destination-name>" +
            "  <admin-object-link>l2</admin-object-link>" +
            "</message-destination>" +
            "</tmp>";

    public void testMessageDestinations() throws Exception {
        XmlObject specDD = parse(SPECDD1);
        XmlObject plan = parse(PLAN1);
        adminObjectRefBuilder.initContext(specDD, plan, module);
        AbstractName n1 = naming.createChildName(baseName, "l1", NameFactory.JCA_ADMIN_OBJECT);
        AbstractName n2 = naming.createChildName(baseName, "l2", NameFactory.JCA_ADMIN_OBJECT);
        configuration.addGBean(new GBeanData(n1, AdminObjectWrapperGBean.GBEAN_INFO));
        configuration.addGBean(new GBeanData(n2, AdminObjectWrapperGBean.GBEAN_INFO));
        adminObjectRefBuilder.buildNaming(specDD, plan, module, componentContext);
        assertEquals(2, NamingBuilder.JNDI_KEY.get(componentContext).size());
    }

    private static final String PLAN2 = "<tmp xmlns=\"http://geronimo.apache.org/xml/ns/naming-1.2\">" +
            "<message-destination>" +
            "  <message-destination-name>d1</message-destination-name>" +
            "  <admin-object-module>testRoot</admin-object-module>" +
            "  <admin-object-link>l1</admin-object-link>" +
            "</message-destination>" +
            "<message-destination>" +
            "  <message-destination-name>d2</message-destination-name>" +
            "  <admin-object-module>testRoot</admin-object-module>" +
            "  <admin-object-link>l2</admin-object-link>" +
            "</message-destination>" +
            "</tmp>";
    public void testMessageDestinationsWithModule() throws Exception {
        XmlObject specDD = parse(SPECDD1);
        XmlObject plan = parse(PLAN2);
        adminObjectRefBuilder.initContext(specDD, plan, module);
        AbstractName n1 = naming.createChildName(baseName, "l1", NameFactory.JCA_ADMIN_OBJECT);
        AbstractName n2 = naming.createChildName(baseName, "l2", NameFactory.JCA_ADMIN_OBJECT);
        configuration.addGBean(new GBeanData(n1, AdminObjectWrapperGBean.GBEAN_INFO));
        configuration.addGBean(new GBeanData(n2, AdminObjectWrapperGBean.GBEAN_INFO));
        adminObjectRefBuilder.buildNaming(specDD, plan, module, componentContext);
        assertEquals(2, NamingBuilder.JNDI_KEY.get(componentContext).size());
    }

    private static final String SPECDD2 = "<tmp xmlns=\"http://java.sun.com/xml/ns/j2ee\">" +
            "</tmp>";


    public void testMessageDestinationsMatch() throws Exception {
        XmlObject specDD = parse(SPECDD2);
        XmlObject plan = parse(PLAN1);
        try {
            adminObjectRefBuilder.initContext(specDD, plan, module);
            fail("tried to register a GerMessageDestination witout a MessageDestination and it succeeded");
        } catch (DeploymentException e) {

        }
    }

    private XmlObject parse(String xmlString) throws XmlException {
        XmlObject xmlObject = XmlObject.Factory.parse(xmlString);
        XmlCursor xmlCursor = xmlObject.newCursor();
        try {
            xmlCursor.toFirstChild();
            return xmlCursor.getObject();
        } finally {
            xmlCursor.dispose();
        }
    }

}

