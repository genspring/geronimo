/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.security.bridge;

import java.io.IOException;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.security.providers.GeronimoPasswordCredential;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/20 06:12:45 $
 *
 * */
public class CallerIdentityUserPasswordRealmBridge extends AbstractRealmBridge {

    private static final GBeanInfo GBEAN_INFO;

    public CallerIdentityUserPasswordRealmBridge() {}

    public CallerIdentityUserPasswordRealmBridge(String targetRealm) {
        super(targetRealm);
    }


    protected CallbackHandler getCallbackHandler(final Subject sourceSubject) {
        return new CallbackHandler() {
            public void handle(Callback[] callbacks)
                    throws IOException, UnsupportedCallbackException {
                Set credentials = sourceSubject.getPrivateCredentials(GeronimoPasswordCredential.class);
                if (credentials == null || credentials.size() != 1) {
                    throw new UnsupportedCallbackException(null, "No GeronimoPasswordCredential to read");
                }
                GeronimoPasswordCredential geronimoPasswordCredential = (GeronimoPasswordCredential)credentials.iterator().next();
                for (int i = 0; i < callbacks.length; i++) {
                    Callback callback = callbacks[i];
                    if (callback instanceof NameCallback) {
                        ((NameCallback)callback).setName(geronimoPasswordCredential.getUserName());
                    } else if (callback instanceof PasswordCallback) {
                        ((PasswordCallback)callback).setPassword(geronimoPasswordCredential.getPassword());
                    } else {
                        throw new UnsupportedCallbackException(callback, "Only name and password callbacks supported");
                    }

                }
            }

        };
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(CallerIdentityUserPasswordRealmBridge.class.getName(), AbstractRealmBridge.getGBeanInfo());
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() {
        GeronimoMBeanInfo mbeanInfo = AbstractRealmBridge.getGeronimoMBeanInfo();
        mbeanInfo.setTargetClass(CallerIdentityUserPasswordRealmBridge.class);
        return mbeanInfo;
    }

}
