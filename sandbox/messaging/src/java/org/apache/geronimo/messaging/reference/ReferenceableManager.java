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

package org.apache.geronimo.messaging.reference;

import org.apache.geronimo.messaging.EndPoint;
import org.apache.geronimo.messaging.Request;

/**
 * Referenceable manager.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:42 $
 */
public interface ReferenceableManager extends EndPoint
{
    
    /**
     * Builds a proxy for the provided Referenceable.
     * <BR>
     * If the Referenceable is hosted by this manager, the Referenceable itself
     * is returned.
     * 
     * @param aReferenceInfo Reference meta-data.
     * @return An instance implementing the Reference Class and delegating
     * all the invokations to the Reference.
     */
    public Object factoryProxy(ReferenceableInfo aReferenceInfo);
    
    /**
     * Registers a Referenceable.
     * 
     * @param aReference Referenceable to be registered.
     * @return Referenceable meta-data.
     */
    public ReferenceableInfo register(Referenceable aReference);
    
    /**
     * Unregisters a Referenceable.
     * 
     * @param aReferenceInfo Meta-data of the Referenceable to be unregistered.
     */
    public void unregister(ReferenceableInfo aReferenceInfo);
    
    /**
     * Invoke a request on the Referenceable having the specified identifier.
     * 
     * @param anId Referenceable identifier.
     * @param aRequest Request to be executed.
     * @return Result.
     * @exception Exception raised by the execution of aRequest against the
     * Referenceable identified by anId.
     */
    public Object invoke(int anId, Request aRequest) throws Exception;

}