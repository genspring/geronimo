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
package org.apache.geronimo.jetty6;

import java.io.File;
import java.net.URL;
import java.security.PermissionCollection;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.connector.outbound.connectiontracking.GeronimoTransactionListener;
import org.apache.geronimo.jetty6.connector.HTTPSocketConnector;
import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.apache.geronimo.security.jaas.GeronimoLoginConfiguration;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.ConfigurationEntryFactory;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.security.deploy.SubjectInfo;
import org.apache.geronimo.security.deploy.PrincipalInfo;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.jacc.PrincipalRoleMapper;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.apache.geronimo.security.jacc.mappingprovider.ApplicationPrincipalRoleConfigurationManager;
import org.apache.geronimo.security.jacc.mappingprovider.GeronimoPolicy;
import org.apache.geronimo.security.jacc.mappingprovider.GeronimoPolicyConfigurationFactory;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.testsupport.TestSupport;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.FormAuthenticator;


/**
 * @version $Rev$ $Date$
 */
public class AbstractWebModuleTest extends TestSupport {
    protected ClassLoader cl;
    protected ConfigurationFactory configurationFactory;
    protected HTTPSocketConnector connector;
    protected JettyContainerImpl container;
    private TransactionManager transactionManager;
    private ConnectionTrackingCoordinator connectionTrackingCoordinator;
    private URL configurationBaseURL;
    protected PreHandlerFactory preHandlerFactory = null;
    protected SessionHandlerFactory sessionHandlerFactory = null;

    protected void setUpStaticContentServlet(JettyServletRegistration webModule) throws Exception {
        Map<String, String> staticContentServletInitParams = new HashMap<String, String>();
        staticContentServletInitParams.put("acceptRanges", "true");
        staticContentServletInitParams.put("dirAllowed", "true");
        staticContentServletInitParams.put("putAllowed", "false");
        staticContentServletInitParams.put("delAllowed", "false");
        staticContentServletInitParams.put("redirectWelcome", "false");
        staticContentServletInitParams.put("minGzipLength", "8192");

        new JettyServletHolder("test:name=staticservlet",
                "default",
                "org.mortbay.jetty.servlet.DefaultServlet",
                null,
                staticContentServletInitParams,
                null,
                Collections.singleton("/"),
                null,
                webModule);

    }

    protected JettyWebAppContext setUpAppContext(String realmName, ConfigurationFactory configurationFactory, Authenticator authenticator, String policyContextId, PermissionCollection excludedPermissions, RunAsSource runAsSource, PermissionCollection checkedPermissions, String uriString) throws Exception {

        JettyWebAppContext app = new JettyWebAppContext(null,
                null,
                Collections.<String, Object>emptyMap(),
                cl,
                new URL(configurationBaseURL, uriString),
                null,
                null,
                "context",
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                authenticator,
                realmName,
                null,
                false,
                0,
                sessionHandlerFactory,
                preHandlerFactory,
                policyContextId,
                configurationFactory,
                runAsSource,
                null,
                null,
                transactionManager,
                connectionTrackingCoordinator,
                container,
                null,
                null,
                null,
                null);
        app.setContextPath("/test");
        app.doStart();
        return app;
    }

    protected JettyWebAppContext setUpSecureAppContext(ConfigurationFactory configurationFactory, Map<String, SubjectInfo> roleDesignates, Map<Principal, Set<String>> principalRoleMap, ComponentPermissions componentPermissions, SubjectInfo defaultSubjectInfo, PermissionCollection checked, Set securityRoles) throws Exception {
        String policyContextId = "TEST";
        ApplicationPolicyConfigurationManager jacc = setUpJACC(roleDesignates, principalRoleMap, componentPermissions, policyContextId);

        FormAuthenticator formAuthenticator = new FormAuthenticator();
        formAuthenticator.setLoginPage("/auth/logon.html?param=test");
        formAuthenticator.setErrorPage("/auth/logonError.html?param=test");
        return setUpAppContext("Test JAAS Realm",
                configurationFactory,
                formAuthenticator,
                policyContextId,
                componentPermissions.getExcludedPermissions(),
                jacc,
                checked,
                "war3/");

    }

    private ApplicationPolicyConfigurationManager setUpJACC(Map<String, SubjectInfo> roleDesignates, Map<Principal, Set<String>> principalRoleMap, ComponentPermissions componentPermissions, String policyContextId) throws Exception {
        setUpSecurityService();
        PrincipalRoleMapper roleMapper = new ApplicationPrincipalRoleConfigurationManager(principalRoleMap, null, roleDesignates, null);
        Map<String, ComponentPermissions> contextIDToPermissionsMap = new HashMap<String, ComponentPermissions>();
        contextIDToPermissionsMap.put(policyContextId, componentPermissions);
        ApplicationPolicyConfigurationManager jacc = new ApplicationPolicyConfigurationManager(contextIDToPermissionsMap, roleMapper, cl);
        jacc.doStart();
        return jacc;
    }

    protected void setUpSecurityService() throws Exception {
        String domainName = "demo-properties-realm";
        ServerInfo serverInfo = new BasicServerInfo(".");

        new SecurityServiceImpl(cl, serverInfo, GeronimoPolicyConfigurationFactory.class.getName(), GeronimoPolicy.class.getName(), null, null, null, null);

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("usersURI", new File(BASEDIR, "src/test/resources/data/users.properties").toURI().toString());
        options.put("groupsURI", new File(BASEDIR, "src/test/resources/data/groups.properties").toURI().toString());

        LoginModuleGBean loginModule = new LoginModuleGBean("org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule", null, true, options, domainName, cl);

        JaasLoginModuleUse loginModuleUse = new JaasLoginModuleUse(loginModule, null, LoginModuleControlFlag.REQUIRED);

        PrincipalInfo.PrincipalEditor principalEditor = new PrincipalInfo.PrincipalEditor();
        principalEditor.setAsText("metro,org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
        configurationFactory = new GenericSecurityRealm(domainName, loginModuleUse, true, false, serverInfo,  cl, null);

//        GeronimoLoginConfiguration loginConfiguration = new GeronimoLoginConfiguration(Collections.<ConfigurationEntryFactory>singleton(realm), true);
//        loginConfiguration.doStart();

    }

    protected void tearDownSecurity() throws Exception {
    }

    protected void setUp() throws Exception {
        cl = this.getClass().getClassLoader();

        configurationBaseURL = cl.getResource("deployables/");

        ServerInfo serverInfo = new BasicServerInfo(".");
        container = new JettyContainerImpl("test:name=JettyContainer", null, new File(BASEDIR, "target/var/jetty").toString(), serverInfo);
        container.doStart();
        connector = new HTTPSocketConnector(container, null);
        connector.setPort(5678);
        connector.setMaxThreads(50);
        connector.doStart();

        TransactionManagerImpl transactionManager = new TransactionManagerImpl();
        this.transactionManager = transactionManager;
        connectionTrackingCoordinator = new ConnectionTrackingCoordinator();
        transactionManager.addTransactionAssociationListener(new GeronimoTransactionListener(connectionTrackingCoordinator));
    }

    protected void tearDown() throws Exception {
        connector.doStop();
        Thread.sleep(1000);
    }
}
