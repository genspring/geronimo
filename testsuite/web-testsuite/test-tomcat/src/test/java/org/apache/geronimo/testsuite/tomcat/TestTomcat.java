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

package org.apache.geronimo.testsuite.tomcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;

public class TestTomcat extends TestSupport
{
    @Test
    public void testTomcatHost() throws Exception {
        URL url = new URL("http://localhost:8080/TomcatWeb/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            String reply = doGET(conn, "testhost.com");

            assertEquals("responseCode", 200, conn.getResponseCode());

            assertTrue( reply.indexOf("Testing Tomcat.") != -1 );
        } finally {
            conn.disconnect();
        }
    }

    @Test
    public void testTomcatNoHost() throws Exception {
        URL url = new URL("http://localhost:8080/TomcatWeb/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            String reply = doGET(conn, null);

            assertEquals("responseCode", 404, conn.getResponseCode());
        } finally {
            conn.disconnect();
        }
    }

    private String doGET(HttpURLConnection conn, String host) throws IOException { 
        conn.setConnectTimeout(1000 * 30);
        conn.setReadTimeout(1000 * 30);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        if (host != null) {
            conn.setRequestProperty("Host", host);
        }

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

}
