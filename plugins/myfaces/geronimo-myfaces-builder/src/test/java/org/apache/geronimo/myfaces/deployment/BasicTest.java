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


package org.apache.geronimo.myfaces.deployment;

import java.util.Collections;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.j2ee.deployment.NamingBuilderCollection;

/**
 * @version $Rev:$ $Date:$
 */
public class BasicTest extends TestCase {
    
    public void testMyFacesModuleBuilderExtension() throws Exception {
        GBeanInfo gBeanInfo = MyFacesModuleBuilderExtension.getGBeanInfo();
        MyFacesModuleBuilderExtension instance = new MyFacesModuleBuilderExtension(null, null, new NamingBuilderCollection(Collections.EMPTY_SET, null));
    }

}