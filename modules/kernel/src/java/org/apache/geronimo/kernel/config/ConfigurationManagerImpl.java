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

package org.apache.geronimo.kernel.config;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationManagerImpl implements ConfigurationManager, GBeanLifecycle {
    private static final Log log = LogFactory.getLog(ConfigurationManagerImpl.class);
    private final Kernel kernel;
    private final Collection stores;
    private final ShutdownHook shutdownHook;

    public ConfigurationManagerImpl(Kernel kernel, Collection stores) {
        this.kernel = kernel;
        this.stores = stores;
        shutdownHook = new ShutdownHook(kernel);
    }

    public List listStores() {
        List storeSnapshot = getStores();
        List result = new ArrayList(storeSnapshot.size());
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            result.add(JMXUtil.getObjectName(store.getObjectName()));
        }
        return result;
    }

    public List listConfigurations(ObjectName storeName) throws NoSuchStoreException {
        List storeSnapshot = getStores();
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (storeName.equals(JMXUtil.getObjectName(store.getObjectName()))) {
                return store.listConfiguations();
            }
        }
        throw new NoSuchStoreException("No such store: " + storeName);
    }

    public boolean isLoaded(URI configID) {
        try {
            ObjectName name = Configuration.getConfigurationObjectName(configID);
            return kernel.isLoaded(name);
        } catch (MalformedObjectNameException e) {
            return false;
        }
    }

    public ObjectName load(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException {
        List storeSnapshot = getStores();

        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (store.containsConfiguration(configID)) {
                return store.loadConfiguration(configID);
            }
        }
        throw new NoSuchConfigException("No configuration with id: " + configID);
    }

    public List loadRecursive(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException {
        try {
            LinkedList ancestors = new LinkedList();
            while (configID != null && !isLoaded(configID)) {
                ObjectName name = load(configID);
                ancestors.addFirst(name);
                configID = (URI) kernel.getAttribute(name, "parentId");
            }
            return ancestors;
        } catch (NoSuchConfigException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (InvalidConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidConfigException(e);
        }
    }

    public void unload(URI configID) throws NoSuchConfigException {
        ObjectName configName;
        try {
            configName = Configuration.getConfigurationObjectName(configID);
        } catch (MalformedObjectNameException e) {
            throw new NoSuchConfigException("Cannot convert ID to ObjectName: ", e);
        }
        try {
            kernel.unloadGBean(configName);
        } catch (GBeanNotFoundException e) {
            throw new NoSuchConfigException("No config registered: " + configName, e);
        }
        log.info("Unloaded Configuration " + configName);
    }

    private List getStores() {
        return new ArrayList(stores);
    }

    public void doStart() {
        kernel.registerShutdownHook(shutdownHook);
    }

    private static final ObjectName CONFIG_QUERY = JMXUtil.getObjectName("geronimo.config:*");

    public void doStop() {
        kernel.unregisterShutdownHook(shutdownHook);
    }

    public void doFail() {
        log.error("Cofiguration manager failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ConfigurationManagerImpl.class, "ConfigurationManager");
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addReference("Stores", ConfigurationStore.class, "ConfigurationStore");
        infoFactory.addInterface(ConfigurationManager.class);
        infoFactory.setConstructor(new String[]{"kernel", "Stores"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    private static class ShutdownHook implements Runnable {
        private final Kernel kernel;

        public ShutdownHook(Kernel kernel) {
            this.kernel = kernel;
        }

        public void run() {
            while (true) {
                Set configs = kernel.listGBeans(CONFIG_QUERY);
                if (configs.isEmpty()) {
                    return;
                }
                for (Iterator i = configs.iterator(); i.hasNext();) {
                    ObjectName configName = (ObjectName) i.next();
                    if (kernel.isLoaded(configName)) {
                        try {
                            kernel.stopGBean(configName);
                        } catch (GBeanNotFoundException e) {
                            // ignore
                        } catch (InternalKernelException e) {
                            log.warn("Could not stop configuration: " + configName, e);
                        }
                        try {
                            kernel.unloadGBean(configName);
                        } catch (GBeanNotFoundException e) {
                            // ignore
                        }
                    }
                }
            }
        }
    }
}
