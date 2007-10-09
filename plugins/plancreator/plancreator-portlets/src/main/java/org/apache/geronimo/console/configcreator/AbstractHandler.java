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
package org.apache.geronimo.console.configcreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.MultiPageAbstractHandler;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.deployment.xbeans.PatternType;
import org.apache.geronimo.xbeans.geronimo.security.GerDistinguishedNameType;
import org.apache.geronimo.xbeans.geronimo.security.GerLoginDomainPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerRealmPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleType;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;
import org.apache.geronimo.xbeans.geronimo.security.GerSubjectInfoType;
import org.apache.xmlbeans.XmlException;

/**
 * Base class for portlet helpers
 * 
 * @version $Rev$ $Date$
 */
public abstract class AbstractHandler extends MultiPageAbstractHandler {
    private final static Log log = LogFactory.getLog(AbstractHandler.class);

    protected final static String GET_ARCHIVE_MODE = "index";
    // 'mode' of initial portlet must be one of "index" or "list"

    protected final static String ENVIRONMENT_MODE = "environment";

    protected final static String REFERENCES_MODE = "references";

    protected final static String SECURITY_MODE = "security";

    protected final static String DEPENDENCIES_MODE = "dependencies";

    protected final static String DISPLAY_PLAN_MODE = "displayPlan";

    protected final static String DEPLOY_STATUS_MODE = "deployStatus";

    protected final static String WAR_CONFIG_DATA_ID = "org.apache.geronimo.configcreator.warConfigData";

    protected final static String DATA_PARAMETER = "data";

    protected final static String MODULE_URI_PARAMETER = "moduleURI";

    protected final static String UPLOADED_WAR_URI_PARAMETER = "uploadedWarUri";

    protected final static String DEPLOYMENT_PLAN_PARAMETER = "deploymentPlan";

    protected final static String CONTEXT_ROOT_PARAMETER = "contextRoot";

    protected final static String ARTIFACT_ID_PARAMETER = "artifactId";

    protected final static String GROUP_ID_PARAMETER = "groupId";

    protected final static String VERSION_PARAMETER = "version";

    protected final static String TYPE_PARAMETER = "type";

    protected final static String HIDDEN_CLASSES_PARAMETER = "hiddenClasses";

    protected final static String NON_OVERRIDABLE_CLASSES_PARAMETER = "nonOverridableClasses";

    protected final static String INVERSE_CLASSLOADING_PARAMETER = "inverseClassLoading";

    protected final static String EJB_REF_PREFIX = "ejbRef";

    protected final static String EJB_LOCAL_REF_PREFIX = "ejbLocalRef";

    protected final static String JMS_CONNECTION_FACTORY_REF_PREFIX = "jmsConnectionFactoryRef";

    protected final static String JMS_DESTINATION_REF_PREFIX = "jmsDestinationRef";

    protected final static String MESSAGE_DESTINATION_PREFIX = "messageDestination";

    protected final static String JDBC_POOL_REF_PREFIX = "jdbcPoolRef";

    protected final static String REF_NAME = "refName";

    protected final static String REF_LINK = "refLink";

    protected final static String DEPENDENCY_PREFIX = "dependency";

    protected final static String ARCHIVE_NOT_SUPPORTED_PARAMETER = "archiveNotSupported";

    protected final static String REFERENCE_NOT_RESOLVED_PARAMETER = "referenceNotResolved";

    protected final static String DEPLOYED_EJBS_PARAMETER = "deployedEjbs";

    protected final static String DEPLOYED_JDBC_CONNECTION_POOLS_PARAMETER = "deployedJdbcConnectionPools";

    protected final static String DEPLOYED_JMS_CONNECTION_FACTORIES_PARAMETER = "deployedJmsConnectionFactories";

    protected final static String DEPLOYED_JMS_DESTINATIONS_PARAMETER = "deployedJmsDestinations";

    protected final static String DEPLOYED_SECURITY_REALMS_PARAMETER = "deployedSecurityRealms";

    protected final static String COMMON_LIBS_PARAMETER = "commonLibs";

    protected final static String SELECTED_LIBS_PARAMETER = "selectedLibs";

    protected final static String DEPLOY_ABBR_STATUS_PARAMETER = "abbrStatusMessage";

    protected final static String DEPLOY_FULL_STATUS_PARAMETER = "fullStatusMessage";

    public AbstractHandler(String mode, String viewName) {
        super(mode, viewName);
    }

    public static class WARConfigModel implements MultiPageModel {
        public WARConfigModel(PortletRequest request) {
        }

        public void save(ActionResponse response, PortletSession session) {
        }
    }

    public WARConfigData getSessionData(PortletRequest request) {
        return (WARConfigData) request.getPortletSession().getAttribute(WAR_CONFIG_DATA_ID);
    }

    public void setNewSessionData(PortletRequest request) {
        request.getPortletSession().setAttribute(WAR_CONFIG_DATA_ID, new WARConfigData());
    }

    public static class WARConfigData {
        private String uploadedWarUri;

        private String deploymentPlan;

        private String contextRoot;

        private String artifactId;

        private String groupId;

        private String version;

        private String type;

        private String hiddenClasses;

        private String nonOverridableClasses;

        private boolean inverseClassLoading;

        private List ejbRefs = new ArrayList();

        private List ejbLocalRefs = new ArrayList();

        private List jdbcPoolRefs = new ArrayList();

        private List jmsConnectionFactoryRefs = new ArrayList();

        private List jmsDestinationRefs = new ArrayList();

        private List messageDestinations = new ArrayList();

        private List dependencies = new ArrayList();

        private boolean referenceNotResolved;

        private String securityRealmName;

        private GerSecurityType security = null;

        public WARConfigData() {
        }

        public void readEnvironmentData(PortletRequest request) {
            contextRoot = request.getParameter(CONTEXT_ROOT_PARAMETER);
            artifactId = request.getParameter(ARTIFACT_ID_PARAMETER);
            groupId = request.getParameter(GROUP_ID_PARAMETER);
            version = request.getParameter(VERSION_PARAMETER);
            type = request.getParameter(TYPE_PARAMETER);
            hiddenClasses = request.getParameter(HIDDEN_CLASSES_PARAMETER);
            nonOverridableClasses = request.getParameter(NON_OVERRIDABLE_CLASSES_PARAMETER);
            inverseClassLoading = "true".equalsIgnoreCase(request
                            .getParameter(INVERSE_CLASSLOADING_PARAMETER)) ? true : false;
        }

        public void readReferencesData(PortletRequest request) {
            readParameters(EJB_REF_PREFIX, ejbRefs, request);
            readParameters(EJB_LOCAL_REF_PREFIX, ejbLocalRefs, request);
            readParameters(JMS_CONNECTION_FACTORY_REF_PREFIX, jmsConnectionFactoryRefs, request);
            readParameters(JMS_DESTINATION_REF_PREFIX, jmsDestinationRefs, request);
            readParameters(MESSAGE_DESTINATION_PREFIX, messageDestinations, request);
            readParameters(JDBC_POOL_REF_PREFIX, jdbcPoolRefs, request);
        }

        private void readParameters(String prefix1, List list, PortletRequest request) {
            list.clear();
            Map map = request.getParameterMap();
            int index = 0;
            while (true) {
                String prefix2 = prefix1 + "." + (index++) + ".";
                if (!map.containsKey(prefix2 + REF_NAME)) {
                    break;
                }
                ReferenceData data = new ReferenceData();
                data.load(request, prefix2);
                list.add(data);
            }
        }

        public void readSecurityData(PortletRequest request) {
            securityRealmName = request.getParameter("securityRealmName");
            readSecurityParameters(request);
        }

        private void readSecurityParameters(PortletRequest request) {
            Map map = request.getParameterMap();
            GerRoleType[] roles = security.getRoleMappings().getRoleArray();
            for (int index = 0; index < roles.length; index++) {
                String prefix1 = "security.roleMappings" + "." + index + ".";
                GerRoleType role = roles[index];

                int lastIndex = Integer.parseInt(request.getParameter(prefix1 + "principal.lastIndex"));
                for (int i = 0; i < lastIndex; i++) {
                    String prefix2 = prefix1 + "principal" + "." + i + ".";
                    if (!map.containsKey(prefix2 + "name")) {
                        continue;
                    }
                    GerPrincipalType principal = role.addNewPrincipal();
                    principal.setName(request.getParameter(prefix2 + "name"));
                    principal.setClass1(request.getParameter(prefix2 + "class"));
                }

                lastIndex = Integer.parseInt(request.getParameter(prefix1 + "loginDomainPrincipal.lastIndex"));
                for (int i = 0; i < lastIndex; i++) {
                    String prefix2 = prefix1 + "loginDomainPrincipal" + "." + i + ".";
                    if (!map.containsKey(prefix2 + "name")) {
                        continue;
                    }
                    GerLoginDomainPrincipalType loginDomainPrincipal = role.addNewLoginDomainPrincipal();
                    loginDomainPrincipal.setName(request.getParameter(prefix2 + "name"));
                    loginDomainPrincipal.setClass1(request.getParameter(prefix2 + "class"));
                    loginDomainPrincipal.setDomainName(request.getParameter(prefix2 + "domainName"));
                }

                lastIndex = Integer.parseInt(request.getParameter(prefix1 + "realmPrincipal.lastIndex"));
                for (int i = 0; i < lastIndex; i++) {
                    String prefix2 = prefix1 + "realmPrincipal" + "." + i + ".";
                    if (!map.containsKey(prefix2 + "name")) {
                        continue;
                    }
                    GerRealmPrincipalType realmPrincipal = role.addNewRealmPrincipal();
                    realmPrincipal.setName(request.getParameter(prefix2 + "name"));
                    realmPrincipal.setClass1(request.getParameter(prefix2 + "class"));
                    realmPrincipal.setDomainName(request.getParameter(prefix2 + "domainName"));
                    realmPrincipal.setRealmName(request.getParameter(prefix2 + "realmName"));
                }

                lastIndex = Integer.parseInt(request.getParameter(prefix1 + "distinguishedName.lastIndex"));
                for (int i = 0; i < lastIndex; i++) {
                    String prefix2 = prefix1 + "distinguishedName" + "." + i + ".";
                    if (!map.containsKey(prefix2 + "name")) {
                        continue;
                    }
                    GerDistinguishedNameType distinguishedName = role.addNewDistinguishedName();
                    distinguishedName.setName(request.getParameter(prefix2 + "name"));
                }

                String prefix2 = prefix1 + "runAsSubject" + ".";
                if (map.containsKey(prefix2 + "realm")) {
                    GerSubjectInfoType runAsSubject = role.addNewRunAsSubject();
                    runAsSubject.setRealm(request.getParameter(prefix2 + "realm"));
                    runAsSubject.setId(request.getParameter(prefix2 + "id"));
                }
            }
            String prefix = "security" + "." + "defaultSubject" + ".";
            if (map.containsKey(prefix + "realm")) {
                GerSubjectInfoType runAsSubject = security.addNewDefaultSubject();
                runAsSubject.setRealm(request.getParameter(prefix + "realm"));
                runAsSubject.setId(request.getParameter(prefix + "id"));
            }
            String parameterName = "security" + "." + "credentialStoreRef";
            if (map.containsKey(parameterName)) {
                try {
                    PatternType pattern = PatternType.Factory.parse(request.getParameter(parameterName));
                    security.setCredentialStoreRef(pattern);
                } catch (XmlException e) {
                    e.printStackTrace();
                }
            }
            parameterName = "security" + "." + "doasCurrentCaller";
            if ("true".equalsIgnoreCase(request.getParameter(parameterName))) {
                security.setDoasCurrentCaller(true);
            }
            parameterName = "security" + "." + "useContextHandler";
            if ("true".equalsIgnoreCase(request.getParameter(parameterName))) {
                security.setUseContextHandler(true);
            }
            parameterName = "security" + "." + "defaultRole";
            security.setDefaultRole(request.getParameter(parameterName));
        }

        public String getContextRoot() {
            return contextRoot;
        }

        public void setContextRoot(String contextRoot) {
            this.contextRoot = contextRoot;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getHiddenClasses() {
            return hiddenClasses;
        }

        public void setHiddenClasses(String hiddenClasses) {
            this.hiddenClasses = hiddenClasses;
        }

        public String getNonOverridableClasses() {
            return nonOverridableClasses;
        }

        public void setNonOverridableClasses(String nonOverridableClasses) {
            this.nonOverridableClasses = nonOverridableClasses;
        }

        public boolean isInverseClassLoading() {
            return inverseClassLoading;
        }

        public void setInverseClassLoading(boolean inverseClassLoading) {
            this.inverseClassLoading = inverseClassLoading;
        }

        public List getDependencies() {
            return dependencies;
        }

        public List getEjbLocalRefs() {
            return ejbLocalRefs;
        }

        public List getEjbRefs() {
            return ejbRefs;
        }

        public List getJdbcPoolRefs() {
            return jdbcPoolRefs;
        }

        public List getJmsConnectionFactoryRefs() {
            return jmsConnectionFactoryRefs;
        }

        public List getJmsDestinationRefs() {
            return jmsDestinationRefs;
        }

        public List getMessageDestinations() {
            return messageDestinations;
        }

        public boolean isReferenceNotResolved() {
            return referenceNotResolved;
        }

        public void setReferenceNotResolved(boolean referenceNotResolved) {
            this.referenceNotResolved = referenceNotResolved;
        }

        public String getUploadedWarUri() {
            return uploadedWarUri;
        }

        public void setUploadedWarUri(String uploadedWarUri) {
            this.uploadedWarUri = uploadedWarUri;
        }

        public String getDeploymentPlan() {
            return deploymentPlan;
        }

        public void setDeploymentPlan(String deploymentPlan) {
            this.deploymentPlan = deploymentPlan;
        }

        public String getSecurityRealmName() {
            return securityRealmName;
        }

        public void setSecurityRealmName(String securityRealmName) {
            this.securityRealmName = securityRealmName;
        }

        public GerSecurityType getSecurity() {
            return security;
        }

        public void setSecurity(GerSecurityType security) {
            this.security = security;
        }
    }

    public static class ReferenceData {
        private String refName;

        private String refLink;

        public ReferenceData() {
        }

        public ReferenceData(String refName) {
            this.refName = refName;
        }

        public void load(PortletRequest request, String prefix) {
            refName = request.getParameter(prefix + REF_NAME);
            refLink = request.getParameter(prefix + REF_LINK);
        }

        public void save(ActionResponse response, String prefix) {
            if (!isEmpty(refName))
                response.setRenderParameter(prefix + REF_NAME, refName);
            if (!isEmpty(refLink))
                response.setRenderParameter(prefix + REF_LINK, refLink);
        }

        public String getRefName() {
            return refName;
        }

        public void setRefName(String refName) {
            this.refName = refName;
        }

        public String getRefLink() {
            return refLink;
        }

        public void setRefLink(String refLink) {
            this.refLink = refLink;
        }
    }
}