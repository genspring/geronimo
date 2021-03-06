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
package org.apache.geronimo.j2ee.deployment;

import org.apache.geronimo.deployment.util.DeploymentUtil;

/**
 * EAR config builder tests for naked JAVAEE 5.
 *
 * @version $Rev:386276 $ $Date$
 */
public class EARConfigBuilder5NakedTest
    extends EARConfigBuilderTestSupport
{
    protected void setUp() throws Exception {
        super.setUp();

        earFile = DeploymentUtil.createJarFile(resolveFile("target/test-ear-javaee_5-naked.ear"));
        ejbConfigBuilder.ejbModule = new EJBModule(false, ejbModuleName, null, null, "test-ejb-jar.jar", null, null, null, null);
        webConfigBuilder.contextRoot = contextRoot;
        webConfigBuilder.webModule = new WebModule(false, webModuleName, null, null, "test-war.war", null, null, null, contextRoot, WEB_NAMESPACE, null);
        connectorConfigBuilder.connectorModule = new ConnectorModule(false, raModuleName, null, null, "test-rar.rar", null, null, null, null);
    }

    protected void tearDown() throws Exception {
        DeploymentUtil.close(earFile);
        close(ejbConfigBuilder.ejbModule);
        close(webConfigBuilder.webModule);
        close(connectorConfigBuilder.connectorModule);

        super.tearDown();
    }
}
