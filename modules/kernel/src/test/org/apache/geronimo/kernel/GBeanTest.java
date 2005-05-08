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

package org.apache.geronimo.kernel;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanLifecycleController;
import org.apache.geronimo.kernel.management.State;

/**
 * @version $Rev$ $Date$
 */
public class GBeanTest extends TestCase {
    private ObjectName name;
    private ObjectName name2;
    private Kernel kernel;

    public void testLoad() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[0], cl);
        GBeanData gbean = new GBeanData(name, MockGBean.getGBeanInfo());
        gbean.setAttribute("name", "Test");
        gbean.setAttribute("finalInt", new Integer(123));
        kernel.loadGBean(gbean, myCl);
        kernel.startGBean(name);
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(name));
        assertEquals("Hello", kernel.invoke(name, "doSomething", new Object[]{"Hello"}, new String[]{String.class.getName()}));

        assertEquals(name.getCanonicalName(), kernel.getAttribute(name, "objectName"));
        assertEquals(name.getCanonicalName(), kernel.getAttribute(name, "actualObjectName"));

        assertSame(myCl, kernel.getAttribute(name, "actualClassLoader"));

        // the MockGBean implemmentation of getConfigurationClassLoader will throw an exception, but since the GBean architecture
        // handles this directly the implementation method will never be called
        kernel.getAttribute(name, "classLoader");

        GBeanLifecycleController gbeanLifecycleController = (GBeanLifecycleController) kernel.getAttribute(name, "gbeanLifecycleController");
        assertNotNull(gbeanLifecycleController);
        assertEquals(State.RUNNING_INDEX, gbeanLifecycleController.getState());

        assertSame(kernel, kernel.getAttribute(name, "kernel"));
        assertSame(kernel, kernel.getAttribute(name, "actualKernel"));

        kernel.stopGBean(name);
        kernel.unloadGBean(name);
    }

    public void testEndpoint() throws Exception {
        ClassLoader cl = MockGBean.class.getClassLoader();
        GBeanData gbean1 = new GBeanData(name, MockGBean.getGBeanInfo());
        gbean1.setAttribute("finalInt", new Integer(123));
        kernel.loadGBean(gbean1, cl);
        kernel.startGBean(name);

        GBeanData gbean2 = new GBeanData(name2, MockGBean.getGBeanInfo());
        gbean2.setAttribute("finalInt", new Integer(123));
        gbean2.setReferencePatterns("MockEndpoint", Collections.singleton(name));
        kernel.loadGBean(gbean2, cl);
        kernel.startGBean(name2);

        assertEquals("endpointCheck", kernel.invoke(name2, "checkEndpoint", null, null));
    }

    protected void setUp() throws Exception {
        name = new ObjectName("test:name=MyMockGBean");
        name2 = new ObjectName("test:name=MyMockGBean2");
        kernel = KernelFactory.newInstance().createKernel("test");
        kernel.boot();
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
    }
}
