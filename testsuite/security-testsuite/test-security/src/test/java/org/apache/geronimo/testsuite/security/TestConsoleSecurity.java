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

package org.apache.geronimo.testsuite.security;


import org.apache.geronimo.testsupport.SeleniumTestSupport;
import org.testng.annotations.Test;

public class TestConsoleSecurity extends SeleniumTestSupport {
    
    @Test
    public void testLogin() throws Exception {
        selenium.open("/console");
        waitForPageLoad();
        assertFalse(selenium.isTextPresent("Deploy New"));
        selenium.type("j_username", "system");
        selenium.type("j_password", "manager");
        selenium.click("submit");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("Deploy New"));
        selenium.click("//a[contains(@href, '/console/logout.jsp')]");
        waitForPageLoad();
        assertEquals("Geronimo Console Login", selenium.getTitle());
    }

    @Test
    public void testBadPasswordLogin() throws Exception {   
        testFailure("system", "managerr");
    }
    
    @Test
    public void testBadUser() throws Exception {  
        testFailure("doesnotexist", "managerr");
    }
    
    @Test
    public void testNullPasswordLogin() throws Exception {        
        testFailure("system", null);
    }
    
    @Test
    public void testNullUserLogin() throws Exception {        
        testFailure(null, "manager");
    }
    
    @Test
    public void testNullCredentialsLogin() throws Exception {        
        testFailure(null, null);
    }
    
    @Test
    public void testEmptyCredentialsLogin() throws Exception {        
        testFailure("", "");
    }
    
    private void testFailure(String username, String password) throws Exception {
        selenium.open("/console");
        waitForPageLoad();
        if (username != null) {
            selenium.type("j_username", username);
        }
        if (password != null) {
            selenium.type("j_password", password);
        }
        selenium.click("submit");
        waitForPageLoad();
        
        assertTrue(selenium.isTextPresent("Invalid Username and"));
    }
    
}
