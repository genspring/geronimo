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
package org.apache.geronimo.ejb.container;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.Principal;

import org.apache.geronimo.common.Invocation;

/**
 *
 *
 *
 * @version $Revision: 1.3 $ $Date: 2003/08/23 09:07:11 $
 */
public final class EJBContainerUtil implements Serializable {
    // Be careful here.  If you change the oridnals, this class must be changed on evey client.
    private static int MAX_ORIDNAL = 4;
    private static final EJBContainerUtil[] values = new EJBContainerUtil[MAX_ORIDNAL + 1];
    private static final EJBContainerUtil METHOD = new EJBContainerUtil("METHOD", 0);
    private static final EJBContainerUtil ID = new EJBContainerUtil("ID", 1);
    private static final EJBContainerUtil ARGUMENTS = new EJBContainerUtil("ARGUMENTS", 2);
    private static final EJBContainerUtil PRINCIPAL = new EJBContainerUtil("PRINCIPAL", 3);
    private static final EJBContainerUtil CREDENTIALS = new EJBContainerUtil("CREDENTIALS", 4);

    public static Method getMethod(Invocation invocation) {
        return (Method) invocation.getAsIs(METHOD);
    }

    public static void putMethod(Invocation invocation, Method method) {
        invocation.putAsIs(METHOD, method);
    }

    public static Object getId(Invocation invocation) {
        return invocation.getMarshal(ID);
    }

    public static void putId(Invocation invocation, Object id) {
        invocation.putMarshal(ID, id);
    }

    public static Object[] getArguments(Invocation invocation) {
        return (Object[]) invocation.getMarshal(ARGUMENTS);
    }

    public static void putArguments(Invocation invocation, Object[] arguments) {
        invocation.putMarshal(ARGUMENTS, arguments);
    }

    public static Principal getPrincipal(Invocation invocation) {
        return (Principal) invocation.getAsIs(PRINCIPAL);
    }

    public static void putPrincipal(Invocation invocation, Principal principal) {
        invocation.putAsIs(PRINCIPAL, principal);
    }

    public static Object getCredentials(Invocation invocation) {
        return invocation.getMarshal(CREDENTIALS);
    }

    public static void putCredentials(Invocation invocation, Object credentials) {
        invocation.putMarshal(CREDENTIALS, credentials);
    }

    private final transient String name;
    private final int ordinal;

    private EJBContainerUtil(String name, int ordinal) {
        assert ordinal < MAX_ORIDNAL;
        assert values[ordinal] == null;
        this.name = name;
        this.ordinal = ordinal;
        values[ordinal] = this;
    }

    public String toString() {
        return name;
    }

    Object readResolve() throws ObjectStreamException {
        return values[ordinal];
    }
}
