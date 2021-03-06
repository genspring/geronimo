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

package org.apache.geronimo.deployment.plugin.factories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;

import org.apache.geronimo.deployment.ModuleConfigurer;
import org.apache.geronimo.deployment.plugin.jmx.RemoteDeploymentManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;

/**
 *
 * @version $Rev: 503905 $ $Date: 2007-02-06 09:20:49 +1100 (Tue, 06 Feb 2007) $
 */
public class DeploymentFactoryWithKernel extends BaseDeploymentFactory {
    private final Kernel kernel;
    private final RemoteDeploymentManager remoteDeploymentManager;
    
    public DeploymentFactoryWithKernel(Kernel kernel) {
        this(kernel, null);
    }

    public DeploymentFactoryWithKernel(Kernel kernel, RemoteDeploymentManager remoteDeploymentManager) {
        if (null == kernel) {
            throw new IllegalArgumentException("kernel is required");
        }
        this.kernel = kernel;
        this.remoteDeploymentManager = remoteDeploymentManager;
        DeploymentFactoryManager.getInstance().registerDeploymentFactory(this);
    }
    
    protected Collection<ModuleConfigurer> getModuleConfigurers() throws DeploymentManagerCreationException {
        Collection<ModuleConfigurer> moduleConfigurers = new ArrayList<ModuleConfigurer>();
        Set<AbstractName> configurerNames = kernel.listGBeans(new AbstractNameQuery(ModuleConfigurer.class.getName()));
        for (AbstractName configurerName : configurerNames) {
            try {
                ModuleConfigurer configurer = (ModuleConfigurer) kernel.getGBean(configurerName);
                moduleConfigurers.add(configurer);
            } catch (GBeanNotFoundException e) {
                throw (AssertionError)new AssertionError("No gbean found for name returned in query : " + configurerName).initCause(e);
            }
        }
        return moduleConfigurers;
    }
    
    protected RemoteDeploymentManager getRemoteDeploymentManager() throws DeploymentManagerCreationException {
        if (remoteDeploymentManager != null) {
            return remoteDeploymentManager;
        }
        try {
            return kernel.getGBean(RemoteDeploymentManager.class);
        } catch (Exception e) {
            throw (DeploymentManagerCreationException) new DeploymentManagerCreationException("See nested").initCause(e);
        }
    }

    public static final GBeanInfo GBEAN_INFO;
    
    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(DeploymentFactoryWithKernel.class, "DeploymentFactory");

        infoBuilder.addInterface(DeploymentFactory.class);
        
        infoBuilder.setConstructor(new String[] {"kernel"});
        
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
