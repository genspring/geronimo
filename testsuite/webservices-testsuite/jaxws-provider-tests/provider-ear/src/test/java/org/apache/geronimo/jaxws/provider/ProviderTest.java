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

package org.apache.geronimo.jaxws.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;

public abstract class ProviderTest extends TestSupport {

    private String baseURL = "http://localhost:8080/";

    abstract String getTestServlet();

    private String doGET(HttpURLConnection conn) throws IOException {
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
        InputStream is = null;
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            is = conn.getErrorStream();
        }
        StringBuffer buf = new StringBuffer();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
            buf.append(inputLine);
        }
        in.close();
        return buf.toString();
    }

    @Test
    public void testHTTPDataSource() throws Exception {
        runTest("testHTTPDataSource");
    }

    @Test
    public void testHTTPSourceMessageMode() throws Exception {
        runTest("testHTTPSourceMessageMode");
    }

    @Test
    public void testHTTPSourcePayloadMode() throws Exception {
        runTest("testHTTPSourcePayloadMode");
    }

    @Test
    public void testSOAP11SOAPMessage() throws Exception {
        runTest("testSOAP11SOAPMessage");
    }

    @Test
    public void testSOAP11SourceMessageMode() throws Exception {
        runTest("testSOAP11SourceMessageMode");
    }

    @Test
    public void testSOAP11SourcePayloadMode() throws Exception {
        runTest("testSOAP11SourcePayloadMode");
    }

    @Test
    public void testSOAP12SOAPMessage() throws Exception {
        runTest("testSOAP12SOAPMessage");
    }

    @Test
    public void testSOAP12SourceMessageMode() throws Exception {
        runTest("testSOAP12SourceMessageMode");
    }

    @Test
    public void testSOAP12SourcePayloadMode() throws Exception {
        runTest("testSOAP12SourcePayloadMode");
    }
    
    protected void runTest(String testName) throws Exception {
        String warName = System.getProperty("webAppName");
        assertNotNull(warName);
        URL url = new URL(baseURL + warName + getTestServlet() + "?test=" + testName);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            doGET(connection);
            assertEquals("responseCode", 200, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }
}
