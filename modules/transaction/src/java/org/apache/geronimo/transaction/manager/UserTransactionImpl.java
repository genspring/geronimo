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
package org.apache.geronimo.transaction.manager;

import java.io.Serializable;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;

/**
 * TODO this needs to migrate to using TransactionContext.
 * TODO this needs to notify the TrackedConnectionAssociator when a tx starts.
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/23 19:06:56 $
 */
public class UserTransactionImpl implements UserTransaction, Serializable {
    private transient TransactionManager transactionManager;
    private transient TrackedConnectionAssociator trackedConnectionAssociator;

    public UserTransactionImpl() {
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public TrackedConnectionAssociator getTrackedConnectionAssociator() {
        return trackedConnectionAssociator;
    }

    public void setTrackedConnectionAssociator(TrackedConnectionAssociator trackedConnectionAssociator) {
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public void begin() throws NotSupportedException, SystemException {
        checkState();
        transactionManager.begin();
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        checkState();
        transactionManager.commit();
    }

    public int getStatus() throws SystemException {
        checkState();
        return transactionManager.getStatus();
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        checkState();
        transactionManager.rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        checkState();
        transactionManager.setRollbackOnly();
    }

    public void setTransactionTimeout(int timeout) throws SystemException {
        checkState();
        transactionManager.setTransactionTimeout(timeout);
    }

    private void checkState() {
        if (transactionManager == null || trackedConnectionAssociator == null) {
            throw new IllegalStateException("UserTransaction is disabled");
        }
    }
}
