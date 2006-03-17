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

package org.apache.geronimo.gbean.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;

/**
 * @version $Rev: 384141 $ $Date$
 */
public class GBeanSingleReference extends AbstractGBeanReference {
    private static final Log log = LogFactory.getLog(GBeanSingleReference.class);

    /**
     * The object to which the proxy is bound
     */
    private final AbstractName proxyTarget;

    public GBeanSingleReference(GBeanInstance gbeanInstance, GReferenceInfo referenceInfo, Kernel kernel, ReferencePatterns referencePatterns) throws InvalidConfigurationException {
        super(gbeanInstance, referenceInfo, kernel, referencePatterns != null && referencePatterns.getAbstractName() != null);
        proxyTarget = referencePatterns != null? referencePatterns.getAbstractName(): null;
    }

    public final synchronized void online() {
    }

    public final synchronized void offline() {
        stop();
    }


    public synchronized boolean start() {
        // We only need to start if there are patterns and we don't already have a proxy
        if (proxyTarget == null) {
            return true;
        }

        // assure the gbean is running
        AbstractName abstractName = getGBeanInstance().getAbstractName();
        if (!isRunning(getKernel(), proxyTarget)) {
            log.debug("Waiting to start " + abstractName + " because no targets are running for reference " + getName() +" matching the patterns " + proxyTarget);
            return false;
        }

        if (getProxy() != null) {
            return true;
        }

        if (NO_PROXY) {
            try {
                setProxy(getKernel().getGBean(proxyTarget));
            } catch (GBeanNotFoundException e) {
                // gbean disappeard on us
                log.debug("Waiting to start " + abstractName + " because no targets are running for reference " + getName() +" matching the patterns " + proxyTarget);
            }
        } else {
            setProxy(getKernel().getProxyManager().createProxy(proxyTarget, getReferenceType()));
        }

        return true;
    }

    public synchronized void stop() {
        Object proxy = getProxy();
        if (proxy != null) {
            getKernel().getProxyManager().destroyProxy(proxy);
            setProxy(null);
        }
    }


}
