/**
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

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.openejb.assembler.classic.SingletonSessionContainerInfo;

import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class SingletonContainerGBean extends EjbContainer {

    private final long accessTimeout;

    public SingletonContainerGBean(
            @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName abstractName,
            @ParamReference(name = "OpenEjbSystem") OpenEjbSystem openEjbSystem,
            @ParamAttribute(name = "provider") String provider,
            @ParamAttribute(name = "accessTimeout") long accessTimeout,
            @ParamAttribute(name = "properties") Properties properties) {
        super(abstractName, SingletonSessionContainerInfo.class, openEjbSystem, provider, "SINGLETON", properties);
        set("AccessTimeout", Long.toString(accessTimeout));
        this.accessTimeout = accessTimeout;
    }

    public long getAccessTimeout() {
        return accessTimeout;
    }
}
