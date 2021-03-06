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

package org.apache.geronimo.persistence.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.annotation.PersistenceContextAnnotationHelper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.reference.PersistenceContextReference;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPersistenceContextRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerPersistenceContextRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPersistenceContextTypeType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPropertyType;
import org.apache.geronimo.xbeans.javaee.PersistenceContextRefType;
import org.apache.geronimo.xbeans.javaee.PersistenceContextTypeType;
import org.apache.geronimo.xbeans.javaee.PropertyType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceContextRefBuilder extends AbstractNamingBuilder {
    private static final QName PERSISTENCE_CONTEXT_REF_QNAME = new QName(JEE_NAMESPACE, "persistence-context-ref");
    private static final QNameSet PERSISTENCE_CONTEXT_REF_QNAME_SET = QNameSet.singleton(PERSISTENCE_CONTEXT_REF_QNAME);
    private static final QName GER_PERSISTENCE_CONTEXT_REF_QNAME = GerPersistenceContextRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_PERSISTENCE_CONTEXT_REF_QNAME_SET = QNameSet.singleton(GER_PERSISTENCE_CONTEXT_REF_QNAME);
    private static final Set<String> PERSISTENCE_UNIT_INTERFACE_TYPES = Collections.singleton("org.apache.geronimo.persistence.PersistenceUnitGBean");
    private final AbstractNameQuery defaultPersistenceUnitAbstractNameQuery;
    private final boolean strictMatching;

    public PersistenceContextRefBuilder(Environment defaultEnvironment, AbstractNameQuery defaultPersistenceUnitAbstractNameQuery, boolean strictMatching) {
        super(defaultEnvironment);
        this.defaultPersistenceUnitAbstractNameQuery = defaultPersistenceUnitAbstractNameQuery;
        this.strictMatching = strictMatching;
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) throws DeploymentException {
        return plan != null && plan.selectChildren(PersistenceContextRefBuilder.GER_PERSISTENCE_CONTEXT_REF_QNAME_SET).length > 0;
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map componentContext) throws DeploymentException {

        // Discover and process any @PersistenceContextRef annotations (if !metadata-complete)
        if ((module != null) && (module.getClassFinder() != null)) {
            processAnnotations(module);
        }

        List<PersistenceContextRefType> specPersistenceContextRefsUntyped = convert(specDD.selectChildren(PERSISTENCE_CONTEXT_REF_QNAME_SET), JEE_CONVERTER, PersistenceContextRefType.class, PersistenceContextRefType.type);
        Map<String, GerPersistenceContextRefType> gerPersistenceContextRefsUntyped = getGerPersistenceContextRefs(plan);
        List<DeploymentException> problems = new ArrayList<DeploymentException>();
        Configuration localConfiguration = module.getEarContext().getConfiguration();
        for (PersistenceContextRefType persistenceContextRef : specPersistenceContextRefsUntyped) {
            try {
                String persistenceContextRefName = persistenceContextRef.getPersistenceContextRefName().getStringValue().trim();

                addInjections(persistenceContextRefName, persistenceContextRef.getInjectionTargetArray(), componentContext);
                PersistenceContextTypeType persistenceContextType = persistenceContextRef.getPersistenceContextType();
                boolean transactionScoped = persistenceContextType == null || !persistenceContextType.getStringValue().equalsIgnoreCase("extended");

                PropertyType[] propertyTypes = persistenceContextRef.getPersistencePropertyArray();
                Map<String, String> properties = new HashMap<String, String>();
                for (PropertyType propertyType : propertyTypes) {
                    String key = propertyType.getName().getStringValue();
                    String value = propertyType.getValue().getStringValue();
                    properties.put(key, value);
                }

                AbstractNameQuery persistenceUnitNameQuery;
                GerPersistenceContextRefType gerPersistenceContextRef = gerPersistenceContextRefsUntyped.remove(persistenceContextRefName);
                if (gerPersistenceContextRef != null) {
                    persistenceUnitNameQuery = findPersistenceUnit(gerPersistenceContextRef);
                    addProperties(gerPersistenceContextRef, properties);
                    checkForGBean(localConfiguration, persistenceUnitNameQuery, true);
                } else if (persistenceContextRef.isSetPersistenceUnitName() && persistenceContextRef.getPersistenceUnitName().getStringValue().trim().length() > 0) {
                    String persistenceUnitName = persistenceContextRef.getPersistenceUnitName().getStringValue().trim();
                    persistenceUnitNameQuery = new AbstractNameQuery(null, Collections.singletonMap("name", persistenceUnitName), PERSISTENCE_UNIT_INTERFACE_TYPES);
                    if (!checkForGBean(localConfiguration, persistenceUnitNameQuery, strictMatching)) {
                        persistenceUnitName = "persistence/" + persistenceUnitName;
                        persistenceUnitNameQuery = new AbstractNameQuery(null, Collections.singletonMap("name", persistenceUnitName), PERSISTENCE_UNIT_INTERFACE_TYPES);
                        checkForGBean(localConfiguration, persistenceUnitNameQuery, true);
                    }
                } else {
                    persistenceUnitNameQuery = new AbstractNameQuery(null, Collections.EMPTY_MAP, PERSISTENCE_UNIT_INTERFACE_TYPES);
                    Set<AbstractNameQuery> patterns = Collections.singleton(persistenceUnitNameQuery);
                    LinkedHashSet<GBeanData> gbeans = localConfiguration.findGBeanDatas(localConfiguration, patterns);
                    persistenceUnitNameQuery = checkForDefaultPersistenceUnit(gbeans);
                    if (gbeans.isEmpty()) {
                        gbeans = localConfiguration.findGBeanDatas(patterns);
                        persistenceUnitNameQuery = checkForDefaultPersistenceUnit(gbeans);

                        if (gbeans.isEmpty()) {
                            if (defaultPersistenceUnitAbstractNameQuery == null) {
                                throw new DeploymentException("No default PersistenceUnit specified, and none located");
                            }
                            persistenceUnitNameQuery = defaultPersistenceUnitAbstractNameQuery;
                        }
                    }
                    checkForGBean(localConfiguration, persistenceUnitNameQuery, true);
                }

                PersistenceContextReference reference = new PersistenceContextReference(module.getConfigId(), persistenceUnitNameQuery, transactionScoped, properties);

                NamingBuilder.JNDI_KEY.get(componentContext).put(ENV + persistenceContextRefName, reference);
            } catch (DeploymentException e) {
                problems.add(e);
            }
        }

        // Support persistence context refs that are mentioned only in the geronimo plan
        for (GerPersistenceContextRefType gerPersistenceContextRef : gerPersistenceContextRefsUntyped.values()) {
            try {
                String persistenceContextRefName = gerPersistenceContextRef.getPersistenceContextRefName();
                GerPersistenceContextTypeType.Enum persistenceContextType = gerPersistenceContextRef.getPersistenceContextType();
                boolean transactionScoped = persistenceContextType == null || !persistenceContextType.equals(GerPersistenceContextTypeType.EXTENDED);
                Map properties = new HashMap();
                addProperties(gerPersistenceContextRef, properties);


                AbstractNameQuery persistenceUnitNameQuery = findPersistenceUnit(gerPersistenceContextRef);

                checkForGBean(localConfiguration, persistenceUnitNameQuery, true);

                PersistenceContextReference reference = new PersistenceContextReference(module.getConfigId(), persistenceUnitNameQuery, transactionScoped, properties);

                getJndiContextMap(componentContext).put(ENV + persistenceContextRefName, reference);
            } catch (DeploymentException e) {
                problems.add(e);
            }

        }
        if (!problems.isEmpty()) {
            // something is broken with cmp references that stops deployment... this is just a patch around the real problem
            // throw new DeploymentException("Could not resolve reference at deploy time for query " + persistenceUnitNameQuery, e);
            for (DeploymentException e : problems) {
                e.printStackTrace();
            }
            //TODO throw a "multi-exception"
        }
    }

    private AbstractNameQuery checkForDefaultPersistenceUnit(LinkedHashSet<GBeanData> gbeans) throws DeploymentException {
        AbstractNameQuery persistenceUnitNameQuery = null;
        for (java.util.Iterator it = gbeans.iterator(); it.hasNext();) {
            GBeanData gbean = (GBeanData) it.next();
            AbstractName name = gbean.getAbstractName();
            Map nameMap = name.getName();
            if ("cmp".equals(nameMap.get("name"))) {
                it.remove();
            } else {
                persistenceUnitNameQuery = new AbstractNameQuery(name);
            }
        }
        if (gbeans.size() > 1) {
            throw new DeploymentException("Too many matches for no-name persistence unit: " + gbeans);
        }
        return persistenceUnitNameQuery;
    }

    private boolean checkForGBean(Configuration localConfiguration, AbstractNameQuery persistenceUnitNameQuery, boolean complainIfMissing) throws DeploymentException {
        try {
            localConfiguration.findGBeanData(persistenceUnitNameQuery);
            return true;
        } catch (GBeanNotFoundException e) {
            if (complainIfMissing || e.hasMatches()) {
                String reason = e.hasMatches() ? "More than one GBean reference found." : "No GBeans found.";
                throw new DeploymentException("Could not resolve reference at deploy time for query " + persistenceUnitNameQuery + ". " + reason, e);
            }
            return false;
        }
    }

    private void addProperties(GerPersistenceContextRefType persistenceContextRef, Map<String, String> properties) {
        GerPropertyType[] propertyTypes = persistenceContextRef.getPropertyArray();
        for (GerPropertyType propertyType : propertyTypes) {
            String key = propertyType.getKey();
            String value = propertyType.getValue();
            properties.put(key, value);
        }
    }

    private Map<String, GerPersistenceContextRefType> getGerPersistenceContextRefs(XmlObject plan) throws DeploymentException {
        Map<String, GerPersistenceContextRefType> map = new HashMap<String, GerPersistenceContextRefType>();
        if (plan != null) {
            List<GerPersistenceContextRefType> refs = convert(plan.selectChildren(GER_PERSISTENCE_CONTEXT_REF_QNAME_SET), NAMING_CONVERTER, GerPersistenceContextRefType.class, GerPersistenceContextRefType.type);
            for (GerPersistenceContextRefType ref : refs) {
                map.put(ref.getPersistenceContextRefName().trim(), ref);
            }
        }
        return map;
    }

    private AbstractNameQuery findPersistenceUnit(GerPersistenceContextRefType persistenceContextRef) {
        AbstractNameQuery persistenceUnitNameQuery;
        if (persistenceContextRef.isSetPersistenceUnitName()) {
            String persistenceUnitName = persistenceContextRef.getPersistenceUnitName();
            persistenceUnitNameQuery = new AbstractNameQuery(null, Collections.singletonMap("name", persistenceUnitName), PERSISTENCE_UNIT_INTERFACE_TYPES);
        } else {
            GerPatternType gbeanLocator = persistenceContextRef.getPattern();

            persistenceUnitNameQuery = buildAbstractNameQuery(gbeanLocator, null, null, PERSISTENCE_UNIT_INTERFACE_TYPES);
        }
        return persistenceUnitNameQuery;
    }

    private void processAnnotations(Module module) throws DeploymentException {
        // Process all the annotations for this naming builder type
        PersistenceContextAnnotationHelper.processAnnotations(module.getAnnotatedApp(), module.getClassFinder());
    }

    public QNameSet getSpecQNameSet() {
        SchemaConversionUtils.registerNamespaceConversions(Collections.singletonMap(GER_PERSISTENCE_CONTEXT_REF_QNAME.getLocalPart(), new NamespaceElementConverter(GER_PERSISTENCE_CONTEXT_REF_QNAME.getNamespaceURI())));
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return GER_PERSISTENCE_CONTEXT_REF_QNAME_SET;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(PersistenceContextRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("defaultPersistenceUnitAbstractNameQuery", AbstractNameQuery.class, true, true);
        infoBuilder.addAttribute("strictMatching", boolean.class, true, true);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment", "defaultPersistenceUnitAbstractNameQuery", "strictMatching"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
