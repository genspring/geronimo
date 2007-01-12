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

package org.apache.geronimo.naming.deployment;

import java.util.Map;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.reference.KernelReference;
import org.apache.geronimo.xbeans.javaee.EnvEntryType;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class EnvironmentEntryBuilder extends AbstractNamingBuilder {

    private final QNameSet envEntryQNameSet;

    public EnvironmentEntryBuilder(String[] eeNamespaces) {
        envEntryQNameSet = buildQNameSet(eeNamespaces, "env-entry");
    }
    public void buildEnvironment(XmlObject specDD, XmlObject plan, Environment environment) {
    }

    public void initContext(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module) throws DeploymentException {
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        List<EnvEntryType> envEntriesUntyped = convert(specDD.selectChildren(envEntryQNameSet), JEE_CONVERTER, EnvEntryType.class, EnvEntryType.type);
        for (EnvEntryType envEntry: envEntriesUntyped) {
            String name = envEntry.getEnvEntryName().getStringValue().trim();
            String type = envEntry.getEnvEntryType().getStringValue().trim();
            String text = envEntry.getEnvEntryValue().getStringValue().trim();
            try {
                Object value;
                if (text == null) {
                    if ("org.apache.geronimo.kernel.Kernel".equals(type)) {
                        value = new KernelReference();
                    } else {
                        value = null;
                    }
                } else if ("java.lang.String".equals(type)) {
                    value = text;
                } else if ("java.lang.Character".equals(type)) {
                    value = new Character(text.charAt(0));
                } else if ("java.lang.Boolean".equals(type)) {
                    value = Boolean.valueOf(text);
                } else if ("java.lang.Byte".equals(type)) {
                    value = Byte.valueOf(text);
                } else if ("java.lang.Short".equals(type)) {
                    value = Short.valueOf(text);
                } else if ("java.lang.Integer".equals(type)) {
                    value = Integer.valueOf(text);
                } else if ("java.lang.Long".equals(type)) {
                    value = Long.valueOf(text);
                } else if ("java.lang.Float".equals(type)) {
                    value = Float.valueOf(text);
                } else if ("java.lang.Double".equals(type)) {
                    value = Double.valueOf(text);
                } else {
                    throw new DeploymentException("unrecognized type: " + type);
                }
                getJndiContextMap(componentContext).put(ENV + name, value);
            } catch (NumberFormatException e) {
                throw new DeploymentException("Invalid env-entry value for name: " + name, e);
            }
        }

    }

    public QNameSet getSpecQNameSet() {
        return envEntryQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return QNameSet.EMPTY;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(EnvironmentEntryBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.setConstructor(new String[] {"eeNamespaces"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
