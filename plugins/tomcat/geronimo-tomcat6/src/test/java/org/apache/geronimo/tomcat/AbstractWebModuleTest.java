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

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.connector.outbound.connectiontracking.GeronimoTransactionListener;
import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.security.deploy.PrincipalInfo;
import org.apache.geronimo.security.deploy.SubjectInfo;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
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
import org.apache.geronimo.tomcat.connector.ConnectorGBean;
import org.apache.geronimo.tomcat.connector.Http11ConnectorGBean;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;

import javax.transaction.TransactionManager;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractWebModuleTest extends TestSupport {
    
    protected ClassLoader cl = this.getClass().getClassLoader();
    protected final static String securityRealmName = "demo-properties-realm";
    protected ConnectorGBean connector;
    protected TomcatContainer container;
    protected static int port = 8181;
    private TransactionManager transactionManager;
    private ConnectionTrackingCoordinator connectionTrackingCoordinator;

    protected static final String POLICY_CONTEXT_ID = "securetest";
    protected GenericSecurityRealm realm;

    protected TomcatWebAppContext setUpInsecureAppContext(URI relativeWebAppRoot, URL configurationBaseURL, SecurityHolder securityHolder, RunAsSource runAsSource, ObjectRetriever tomcatRealm, ValveGBean valveChain) throws Exception {

        TomcatWebAppContext app = new TomcatWebAppContext(cl,
                null,
                null,
                new URL(configurationBaseURL, relativeWebAppRoot.getPath()),
                securityHolder,
                null,
                Collections.EMPTY_MAP,
                null,
                null,
                transactionManager,
                connectionTrackingCoordinator,
                container,
                runAsSource,
                securityHolder == null? null: securityHolder.getConfigurationFactory(),
                tomcatRealm,
                null,
                valveChain,
                null,
                null,
                null,
                false,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        app.setContextPath("/test");
        app.doStart();
        return app;
    }

    protected TomcatWebAppContext setUpSecureAppContext(Map roleDesignates, Map principalRoleMap, ComponentPermissions componentPermissions, RealmGBean realm, SecurityHolder securityHolder) throws Exception {
        ApplicationPolicyConfigurationManager jacc = setUpJACC(roleDesignates, principalRoleMap, componentPermissions, POLICY_CONTEXT_ID);
        securityHolder.setConfigurationFactory(this.realm);
        URL configurationBaseURL = new File(BASEDIR, "src/test/resources/deployables/war3/WEB-INF/web.xml").toURL();
        return setUpInsecureAppContext(new File(BASEDIR, "src/test/resources/deployables/war3/").toURI(),
                configurationBaseURL,
                securityHolder,
                jacc,
                realm,
                null);
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
        realm = new GenericSecurityRealm(domainName, loginModuleUse, true, true, serverInfo,  cl, null);
    }


    protected void tearDownSecurity() throws Exception {
    }

    protected void init(String realmClass) throws Exception {

        RealmGBean realm = null;
//        if (realmClass != null) {
//            Map initParams = new HashMap();
//            initParams.put("userClassNames", "org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
//            initParams.put("roleClassNames", "org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal");
//            realm = new RealmGBean(realmClass, initParams, null, null);
//            realm.doStart();
//        }

        //Default Host
        Map initParams = new HashMap();
        initParams.put("workDir", "work");
        initParams.put("name", "localhost");
        initParams.put("appBase", "");

        HostGBean host = new HostGBean("org.apache.catalina.core.StandardHost", initParams, null, realm, null, null, null, null, null);
        //Default Engine
        initParams = Collections.singletonMap("name", "Geronimo");

        EngineGBean engine = new EngineGBean(null, null,
                "org.apache.geronimo.tomcat.TomcatEngine",
                initParams,
                host,
                realm,
                null,
                null,
                null,
                null,
                null,
                null);
        engine.doStart();

        ServerInfo serverInfo = new BasicServerInfo(".");
        container = new TomcatContainer(cl, new File(BASEDIR, "target/var/catalina").toString(), null, null, null, engine, null, serverInfo, null, null);
        container.doStart();

        connector = new Http11ConnectorGBean("HTTP", null, "localhost", port++, container, serverInfo,null);
        connector.doStart();

        TransactionManagerImpl transactionManager = new TransactionManagerImpl();
        this.transactionManager = transactionManager;
        connectionTrackingCoordinator = new ConnectionTrackingCoordinator();
        transactionManager.addTransactionAssociationListener(new GeronimoTransactionListener(connectionTrackingCoordinator));
    }

    protected void tearDown() throws Exception {
        connector.doStop();
        super.tearDown();
    }
}
