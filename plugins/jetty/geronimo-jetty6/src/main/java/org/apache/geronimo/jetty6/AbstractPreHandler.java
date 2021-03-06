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

package org.apache.geronimo.jetty6;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.component.LifeCycle;

/**
 * @version $Rev: 449059 $ $Date: 2006-09-23 05:23:09 +1000 (Sat, 23 Sep 2006) $
 */
public abstract class AbstractPreHandler implements PreHandler {
    protected Handler next;
    
    public final void setNextHandler(Handler next) {
        this.next = next;
    }
    
    public final Server getServer() {
        throw new UnsupportedOperationException();
    }

    public final boolean isFailed() {
        throw new UnsupportedOperationException();
    }

    public void addLifeCycleListener(Listener listener) {
    }

    public void removeLifeCycleListener(Listener listener) {
    }

    public final boolean isRunning() {
        throw new UnsupportedOperationException();
    }

    public final boolean isStarted() {
        throw new UnsupportedOperationException();
    }

    public final boolean isStarting() {
        throw new UnsupportedOperationException();
    }

    public final boolean isStopping() {
        throw new UnsupportedOperationException();
    }

    public boolean isStopped() {
        throw new UnsupportedOperationException();
    }

    public final void setServer(Server server) {
        throw new UnsupportedOperationException();
    }

    public final void start() throws Exception {
        throw new UnsupportedOperationException();
    }

    public final void stop() throws Exception {
        throw new UnsupportedOperationException();
    }

    public void destroy() {
    }

}
