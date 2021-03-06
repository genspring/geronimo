/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.geronimo.concurrent.test;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.geronimo.testsupport.TestSupport;

public class EJBTestSupport extends TestSupport {

    protected InitialContext getInitialContext(String username, String password) throws Exception {
        Properties p = new Properties();
        
        p.put("java.naming.factory.initial", 
              "org.apache.openejb.client.RemoteInitialContextFactory");
        p.put("java.naming.provider.url", 
              "127.0.0.1:4201");
        
        if (username != null && password != null) {
            p.put(Context.SECURITY_PRINCIPAL, username);       
            p.put(Context.SECURITY_CREDENTIALS, password);
            p.put("openejb.authentication.realmName", "geronimo-admin");            
        }
        
        InitialContext ctx = new InitialContext(p);
        return ctx;
    }
      
}
