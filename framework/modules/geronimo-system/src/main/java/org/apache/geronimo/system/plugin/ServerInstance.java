/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.system.plugin;

import org.apache.geronimo.system.configuration.PluginAttributeStore;
import org.apache.geronimo.system.resolver.AliasedArtifactResolver;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * @version $Rev$ $Date$
 */
public class ServerInstance {

    private final String serverName;
    private final PluginAttributeStore attributeStore;
    private final AliasedArtifactResolver artifactResolver;


    public ServerInstance() {
        serverName = null;
        attributeStore = null;
        artifactResolver = null;
    }

    public ServerInstance(String serverName, PluginAttributeStore attributeStore, AliasedArtifactResolver artifactResolver) {
        this.serverName = serverName;
        this.attributeStore = attributeStore;
        this.artifactResolver = artifactResolver;
    }

    public String getServerName() {
        return serverName;
    }

    public PluginAttributeStore getAttributeStore() {
        return attributeStore;
    }

    public AliasedArtifactResolver getArtifactResolver() {
        return artifactResolver;
    }
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ServerInstance.class, "ServerInstance");
        infoFactory.addAttribute("serverName", String.class, true, true);
        infoFactory.addReference("PluginAttributeStore", PluginAttributeStore.class, "AttributeStore");
        infoFactory.addReference("ArtifactResolver", AliasedArtifactResolver.class, "ArtifactResolver");

        infoFactory.setConstructor(new String[]{"serverName", "PluginAttributeStore", "ArtifactResolver"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}