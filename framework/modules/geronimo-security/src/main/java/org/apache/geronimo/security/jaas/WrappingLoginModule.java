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


package org.apache.geronimo.security.jaas;

import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.geronimo.security.DomainPrincipal;
import org.apache.geronimo.security.RealmPrincipal;

/**
 * @version $Revision$ $Date$
 */
public class WrappingLoginModule implements LoginModule {
    public static final String CLASS_OPTION = WrappingLoginModule.class.getName() + ".LoginModuleClass";
    public static final String DOMAIN_OPTION = WrappingLoginModule.class.getName() + ".DomainName";
    public static final String REALM_OPTION = WrappingLoginModule.class.getName() + ".RealmName";
    private String loginDomainName;
    private String realmName;
    private final Subject localSubject = new Subject();
    private Subject subject;
    private LoginModule delegate;

    public WrappingLoginModule() {
    }

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        Class lmClass = (Class) options.get(CLASS_OPTION);
        try {
            delegate = (LoginModule) lmClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create login module instance", e);
        }
        delegate.initialize(localSubject, callbackHandler, sharedState, options);
        loginDomainName = (String) options.get(DOMAIN_OPTION);
        realmName = (String) options.get(REALM_OPTION);
    }

    public boolean login() throws LoginException {
        return delegate.login();
    }

    public boolean abort() throws LoginException {
        return delegate.abort();
    }

    public boolean commit() throws LoginException {
        boolean result = delegate.commit();

        Set<Principal> wrapped = new HashSet<Principal>();
        for (Principal principal: localSubject.getPrincipals()) {
            wrapped.add(new DomainPrincipal(loginDomainName, principal));
            wrapped.add(new RealmPrincipal(realmName, loginDomainName, principal));
        }
        localSubject.getPrincipals().addAll(wrapped);
        subject.getPrincipals().addAll(localSubject.getPrincipals());
        subject.getPrivateCredentials().addAll(localSubject.getPrivateCredentials());
        subject.getPublicCredentials().addAll(localSubject.getPublicCredentials());
        return result;
    }

    public boolean logout() throws LoginException {
        boolean result = delegate.logout();

        subject.getPrincipals().removeAll(localSubject.getPrincipals());
        localSubject.getPrincipals().clear();

        return result;
    }
}