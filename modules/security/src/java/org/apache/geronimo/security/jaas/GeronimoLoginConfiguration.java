/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.security.jaas;

import java.util.Hashtable;
import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.security.SecurityService;


/**
 * A JAAS configuration mechanism (associating JAAS configuration names with
 * specific LoginModule configurations).  This is a drop-in replacement for the
 * normal file-reading JAAS configuration mechanism.  Instead of getting
 * its configuration from its file, it gets its configuration from other
 * GBeans running in Geronimo.
 *
 * @version $Rev$ $Date$
 */
public class GeronimoLoginConfiguration extends Configuration implements GBeanLifecycle {

    private static Map entries = new Hashtable();
    private Configuration oldConfiguration;

    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        ConfigurationEntry entry = (ConfigurationEntry) entries.get(name);

        if (entry == null) return null;

        return entry.getAppConfigurationEntry();
    }

    public void refresh() {
    }

    public static void register(ConfigurationEntry entry) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SecurityService.CONFIGURE);

        if (entries.containsKey(entry.getApplicationConfigName())) throw new java.lang.IllegalArgumentException("ConfigurationEntry already registered");

        entries.put(entry.getApplicationConfigName(), entry);
    }

    public static void unRegister(ConfigurationEntry entry) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SecurityService.CONFIGURE);

        entries.remove(entry.getApplicationConfigName());
    }

    public void doStart() throws WaitingException, Exception {
        try {
            oldConfiguration = Configuration.getConfiguration();
        } catch (SecurityException e) {
            oldConfiguration = null;
        }
        Configuration.setConfiguration(this);
    }

    public void doStop() throws WaitingException, Exception {
        Configuration.setConfiguration(oldConfiguration);
    }

    public void doFail() {
        Configuration.setConfiguration(oldConfiguration);
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    private static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(GeronimoLoginConfiguration.class.getName());
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
