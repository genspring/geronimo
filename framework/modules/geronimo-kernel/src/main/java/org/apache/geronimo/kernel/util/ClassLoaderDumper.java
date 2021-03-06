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
package org.apache.geronimo.kernel.util;

import java.net.URLClassLoader;
import java.net.URL;
import org.apache.geronimo.kernel.config.MultiParentClassLoader;

/**
 * Shows the ID and contents of a ClassLoader
 *
 * @version $Rev$ $Date$
 */
public class ClassLoaderDumper {
    public static void dump(Object o) {
        if(o != null) dump(o.getClass().getClassLoader());
    }
    public static void dump(Class cls) {
        System.out.println("ClassLoader dump for "+cls.getName());
        dump(cls.getClassLoader());
    }
    public static void dump(ClassLoader loader) {
        dumpIDs("", loader);
        dumpContents("", loader);
    }
    private static void dumpIDs(String prefix, ClassLoader loader) {
        if(loader == null) return;
        System.out.println(prefix+"ClassLoader is "+loader);

        if(loader instanceof MultiParentClassLoader) {
            MultiParentClassLoader mp = (MultiParentClassLoader) loader;
            ClassLoader[] parents = mp.getParents();
            for (int i = 0; i < parents.length; i++) {
                dumpIDs(prefix+"  ", parents[i]);
            }
        } else {
            dumpIDs(prefix+"  ", loader.getParent());
        }
    }
    private static void dumpContents(String prefix, ClassLoader loader) {
        if(loader == null) return;
        System.out.println(prefix+"ClassLoader is "+loader);

        if(loader instanceof URLClassLoader) {
            URLClassLoader url = (URLClassLoader) loader;
            URL[] entries = url.getURLs();
            for (int i = 0; i < entries.length; i++) {
                URL entry = entries[i];
                System.out.println(prefix+"  "+entry);
            }
        }
        if(loader instanceof MultiParentClassLoader) {
            MultiParentClassLoader mp = (MultiParentClassLoader) loader;
            ClassLoader[] parents = mp.getParents();
            for (int i = 0; i < parents.length; i++) {
                dumpContents(prefix+"    ", parents[i]);
            }
        } else {
            dumpContents(prefix+"    ", loader.getParent());
        }
    }
}
