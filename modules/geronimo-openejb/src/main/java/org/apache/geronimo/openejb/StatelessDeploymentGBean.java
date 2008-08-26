/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * @version $Rev$ $Date$
 */
public class StatelessDeploymentGBean {
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(EjbDeploymentGBean.class,
                EjbDeploymentGBean.class,
                EjbDeploymentGBean.GBEAN_INFO,
                NameFactory.STATELESS_SESSION_BEAN);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}