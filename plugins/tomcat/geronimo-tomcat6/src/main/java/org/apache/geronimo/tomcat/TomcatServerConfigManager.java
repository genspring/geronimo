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
package org.apache.geronimo.tomcat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * This class is used to manipulate connectors in server.xml of tomcat.
 *
 * @version $Rev$ $Date$
 */
public class TomcatServerConfigManager {
    
    private static final Logger log = LoggerFactory.getLogger(TomcatServerConfigManager.class);

    private Document server_xml_dom_doc;
    private File server_XML_File;

    public TomcatServerConfigManager(File _server_XML_File) {

        this.server_XML_File = _server_XML_File;

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            server_xml_dom_doc = docBuilder.parse(this.server_XML_File);
        } catch (Exception e) {
            log.error("Error when parsing server.xml:"+server_XML_File+" into dom doc",e);
        }

    }


    /**
     * Remove the connector from server.xml
     * @param name
     *          the name of connector to be removed.
     */
    public void removeConnector(String name) {

        Element connector = this.findTargetConnector(name);
        if (null != connector) {
            connector.getParentNode().removeChild(connector);
        }
        persistServerConfig();
    }


    /**
     * @param attributesToUpdate
     *                      The connector attributes that need to be updated.
     * @param uniqueConnectorName
     *                      the name of connector to be updated. 
     * @param serviceName
     *                      the name attribute of <Service> that the connector resides in.
     */
    public void updateConnector(Map<String, String> attributesToUpdate, String uniqueConnectorName, String serviceName) {

        Element connector = this.findTargetConnector(uniqueConnectorName);

        if (null == connector) {
            // if can't find the unique Connector,create a new one.
            connector = server_xml_dom_doc.createElement("Connector");

            Element service = this.getService(serviceName);

            NodeList ChildNodes = service.getChildNodes();

            Node lastConnectorNode = ChildNodes.item(ChildNodes.getLength() - 1);

            service.insertBefore(connector, lastConnectorNode);
        }
        

        // set attributes for the connector

        for (String attributeName : attributesToUpdate.keySet()) {
            
            if(attributesToUpdate.get(attributeName)==null){
                continue;
            }
            
            // must use "SSLEnabled" instead of "sslEnabled" because attribute is case-sensitive in server.xml
            if (attributeName.equalsIgnoreCase("SSLEnabled")) {
                
                connector.setAttribute("SSLEnabled", attributesToUpdate.get(attributeName));
                
            } else {

                connector.setAttribute(attributeName, attributesToUpdate.get(attributeName));
            }
        }

        connector.setAttribute("name", uniqueConnectorName);

        persistServerConfig();

    }
    


    private Element findTargetConnector(String name) {

        NodeList connectors = server_xml_dom_doc.getElementsByTagName("Connector");

        for (int i = 0; i < connectors.getLength(); i++) {

            Element connector = (Element) (connectors.item(i));
            if (name.equals(connector.getAttribute("name"))) {
                return connector;
            }
        }

        return null;

    }

    private Element getService(String serviceName) {

        NodeList services = server_xml_dom_doc.getElementsByTagName("Service");

        Element service = null;

        if (services == null || services.getLength() == 0) {

            throw new IllegalStateException("No services in server");
        }

        // return the legal <Service> element when there's no serviceName
        // provided.
        if (null == serviceName) {

            if (services.getLength() > 1) {
                throw new IllegalStateException("More than one service in server.  Provide name of desired server");
            }

            return (Element) services.item(0);

        } else {

            // return the specific <Service> element by serviceName
            for (int i = 0; i < services.getLength(); i++) {

                service = (Element) services.item(i);

                if (service.getAttribute("name").equals(serviceName)) {
                    return service;
                }
            }

        }

        return service;

    }

    private void persistServerConfig() {
        
        TransformerFactory tf = TransformerFactory.newInstance();
       
        Transformer transformer;
        try {
            transformer = tf.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            DOMSource source = new DOMSource(server_xml_dom_doc);
            FileOutputStream fos=new FileOutputStream(server_XML_File);
            StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);
        } catch (Exception e1) {
            
            log.error("Error when persist modified dom back to file:"+server_XML_File,e1);
        }

    }

}

