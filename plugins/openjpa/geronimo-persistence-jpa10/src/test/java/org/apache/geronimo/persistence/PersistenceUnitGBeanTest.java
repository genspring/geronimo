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

package org.apache.geronimo.persistence;

import java.io.File;

import javax.persistence.spi.PersistenceUnitInfo;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceUnitGBeanTest extends TestCase {

    public void testNoArgConstructor() throws Exception {
        new PersistenceUnitGBean();
    }
    
    public void testNonNullJavaFileUrls() throws Exception {
        PersistenceUnitGBean gbean = new PersistenceUnitGBean("foo",
                null,
                "JTA",
                null,
                null,
                null,
                null,
                "/foo/bar/Root",
                null,
                true,
                null,
                null,
                null,
                new File("/foo/bar/Root").toURL(),
                getClass().getClassLoader());
        assertNotNull(gbean.getManagedClassNames());
        assertNotNull(gbean.getProperties());
        assertNotNull(gbean.getJarFileUrls());
        assertNotNull(gbean.getPersistenceUnitRootUrl());
        assertNotNull(gbean.getPersistenceProviderClassName());
    }
}
