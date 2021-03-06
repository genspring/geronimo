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
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;

import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class StatelessContainerGBean extends EjbContainer {

    /**
     * Specifies the time to wait between invocations. This
     * value is measured in milliseconds. A value of 5 would
     * result in a time-out of 5 milliseconds between invocations.
     * A value of zero would mean no timeout.
     */
    private final int timeout;

    /**
     * Specifies the size of the bean pools for this
     * stateless SessionBean container.
     */

    private final int poolSize;

    /**
     * StrictPooling tells the container what to do when the pool
     * reaches it's maximum size and there are incoming requests
     * that need instances.
     * <p/>
     * With strict pooling, requests will have to wait for instances
     * to become available. The pool size will never grow beyond the
     * the set PoolSize value.
     * <p/>
     * Without strict pooling, the container will create temporary
     * instances to meet demand. The instances will last for just one
     * method invocation and then are removed.
     */
    private final boolean strictPooling;


    public StatelessContainerGBean(
            @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName abstractName,
            @ParamReference(name = "OpenEjbSystem") OpenEjbSystem openEjbSystem,
            @ParamAttribute(name = "provider") String provider,
            @ParamAttribute(name = "poolSize") int poolSize,
            @ParamAttribute(name = "strictPooling") boolean strictPooling,
            @ParamAttribute(name = "timeout") int timeout,
            @ParamAttribute(name = "properties") Properties properties) {
        super(abstractName, StatelessSessionContainerInfo.class, openEjbSystem, provider, "STATELESS", properties);
        set("PoolSize", Integer.toString(poolSize));
        set("StrictPooling", Boolean.toString(strictPooling));
        set("TimeOut", Integer.toString(timeout));
        this.poolSize = poolSize;
        this.strictPooling = strictPooling;
        this.timeout = timeout;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public boolean isStrictPooling() {
        return strictPooling;
    }

    public int getTimeout() {
        return timeout;
    }
}
