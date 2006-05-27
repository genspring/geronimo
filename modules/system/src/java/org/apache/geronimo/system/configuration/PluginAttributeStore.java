/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.system.configuration;

import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * Extension to the ManageableAttributeStore that supports the plugin
 * installer.
 *
 * @version $Rev$ $Date$
 */
public interface PluginAttributeStore extends ManageableAttributeStore {

    /**
     * Adds a group of settings to the attribute store.  This is used by e.g.
     * the plugin installer to add the settings needed for a new plugin.
     */
    public void setModuleGBeans(Artifact moduleName, GBeanOverride[] gbeans);
}
