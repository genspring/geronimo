/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.messaging;

import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.messaging.io.NullReplacerResolver;
import org.apache.geronimo.messaging.io.ReplacerResolver;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:41 $
 */
public class MockNode implements Node
{

    private NodeInfo nodeInfo;
    private ReplacerResolver replacerResolver = new NullReplacerResolver();

    public void setNodeInfo(NodeInfo aNodeInfo) {
        nodeInfo = aNodeInfo;
    }
    
    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void setTopology(NodeTopology aTopology) {
    }

    public void join(NodeInfo aNodeInfo) throws NodeException {
    }

    public void leave(NodeInfo aNodeInfo) throws NodeException {
    }

    public ReplacerResolver getReplacerResolver() {
        return replacerResolver;
    }

    public void addEndPoint(EndPoint aConnector) {
    }

    public void removeEndPoint(EndPoint aConnector) {
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
    }

    public void doStop() throws WaitingException, Exception {
    }

    public void doFail() {
    }

}
