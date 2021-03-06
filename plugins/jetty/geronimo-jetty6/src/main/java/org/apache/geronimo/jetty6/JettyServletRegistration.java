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

import java.util.Set;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;

import javax.naming.Context;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.apache.geronimo.jetty6.handler.AbstractImmutableHandler;
import org.apache.geronimo.security.deploy.SubjectInfo;

/**
 * @version $Rev$ $Date$
 */
public interface JettyServletRegistration {

    void registerServletHolder(ServletHolder servletHolder, String servletName, Set<String> servletMappings, String objectName) throws Exception;

    void unregisterServletHolder(ServletHolder servletHolder, String servletName, Set<String> servletMappings, String objectName) throws Exception;

    ServletHandler getServletHandler();

    ClassLoader getWebClassLoader();

    AbstractImmutableHandler getLifecycleChain();

    Object newInstance(String className) throws InstantiationException, IllegalAccessException;

    void destroyInstance(Object o) throws Exception;

    Subject getSubjectForRole(String role) throws LoginException;
}
