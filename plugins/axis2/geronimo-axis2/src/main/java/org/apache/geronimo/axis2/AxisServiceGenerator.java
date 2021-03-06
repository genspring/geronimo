/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis2;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.jaxws.catalog.impl.OASISCatalogManager;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;
import org.apache.axis2.jaxws.description.builder.WebServiceProviderAnnot;
import org.apache.axis2.jaxws.description.builder.WsdlComposite;
import org.apache.axis2.jaxws.description.builder.WsdlGenerator;
import org.apache.axis2.jaxws.description.builder.converter.JavaClassToDBCConverter;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class AxisServiceGenerator
{
    private static final Logger log = LoggerFactory.getLogger(AxisServiceGenerator.class);

    private MessageReceiver messageReceiver;
    private ConfigurationContext configurationContext;
    private String catalogName;
    
    public AxisServiceGenerator(){
        this.messageReceiver = new JAXWSMessageReceiver();
    }
    
    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }
   
    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }
    
    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
    
    public AxisService getServiceFromClass(Class endpointClass) throws Exception {
        ServiceDescription serviceDescription = 
            DescriptionFactory.createServiceDescription(endpointClass);        
        EndpointDescription[] edArray = serviceDescription.getEndpointDescriptions();
        AxisService service = edArray[0].getAxisService();
                
        if (service.getNameSpacesMap() == null) {
            NamespaceMap map = new NamespaceMap();
            map.put(Java2WSDLConstants.AXIS2_NAMESPACE_PREFIX,
                    Java2WSDLConstants.AXIS2_XSD);
            map.put(Java2WSDLConstants.DEFAULT_SCHEMA_NAMESPACE_PREFIX,
                    Java2WSDLConstants.URI_2001_SCHEMA_XSD);
            service.setNameSpacesMap(map);
        }
        
        String endpointClassName = endpointClass.getName();
        ClassLoader classLoader = endpointClass.getClassLoader();
        
        service.addParameter(new Parameter(Constants.SERVICE_CLASS, endpointClassName));
        service.setClassLoader(classLoader);
        
        for(Iterator<AxisOperation> opIterator = service.getOperations() ; opIterator.hasNext() ;){
            AxisOperation operation = opIterator.next();
            operation.setMessageReceiver(this.messageReceiver);
        }
        
        Parameter serviceDescriptionParam = 
            new Parameter(EndpointDescription.AXIS_SERVICE_PARAMETER, edArray[0]);
        service.addParameter(serviceDescriptionParam);
        
        return service;
    }
    
    public AxisService getServiceFromWSDL(PortInfo portInfo, Class endpointClass, URL configurationBaseUrl) throws Exception {
        String wsdlFile = portInfo.getWsdlFile();
        if (wsdlFile == null || wsdlFile.equals("")) {
            throw new Exception("WSDL file is required.");
        }
        
        String endpointClassName = endpointClass.getName();
        ClassLoader classLoader = endpointClass.getClassLoader();
                        
        QName serviceQName = portInfo.getWsdlService();
        if (serviceQName == null) {
            serviceQName = JAXWSUtils.getServiceQName(endpointClass);
        }
        
        QName portQName = portInfo.getWsdlPort();
        if (portQName == null) {
            portQName = JAXWSUtils.getPortQName(endpointClass);
        }
                
        OASISCatalogManager catalogManager = new OASISCatalogManager();
        URL catalogURL = JAXWSUtils.getOASISCatalogURL(configurationBaseUrl, classLoader, this.catalogName);
        if (catalogURL != null) {
            catalogManager.setCatalogFiles(catalogURL.toString());
        }
        URL wsdlURL = getWsdlURL(wsdlFile, configurationBaseUrl, classLoader);
        WSDL4JWrapper wsdlWrapper = new WSDL4JWrapper(wsdlURL, this.configurationContext, catalogManager);
        Definition wsdlDefinition = wsdlWrapper.getDefinition(); 
        
        Service wsdlService = wsdlDefinition.getService(serviceQName);
        if (wsdlService == null) {
            throw new Exception("Service '" + serviceQName + "' not found in WSDL");
        }
        
        Port port = wsdlService.getPort(portQName.getLocalPart());
        if (port == null) {
            throw new Exception("Port '" + portQName.getLocalPart() + "' not found in WSDL");
        }
        
        String protocolBinding = null;
        if (portInfo.getProtocolBinding() != null) {
            protocolBinding = JAXWSUtils.getBindingURI(portInfo.getProtocolBinding());
        } else {
            protocolBinding = getBindingFromWSDL(port);
        }
        
        Class endPointClass = classLoader.loadClass(endpointClassName);
        JavaClassToDBCConverter converter = new JavaClassToDBCConverter(endPointClass);
        HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
       
        DescriptionBuilderComposite dbc = dbcMap.get(endpointClassName);
        dbc.setClassLoader(classLoader);
        dbc.setWsdlDefinition(wsdlDefinition);
        dbc.setClassName(endpointClassName);
        dbc.setCustomWsdlGenerator(new WSDLGeneratorImpl(wsdlDefinition));
        dbc.setCatalogManager(catalogManager);
        
        if (dbc.getWebServiceAnnot() != null) { //information specified in .wsdl should overwrite annotation.
            WebServiceAnnot serviceAnnot = dbc.getWebServiceAnnot();
            serviceAnnot.setPortName(portQName.getLocalPart());
            serviceAnnot.setServiceName(serviceQName.getLocalPart());
            serviceAnnot.setTargetNamespace(serviceQName.getNamespaceURI());
            processServiceBinding(dbc, protocolBinding);
        } else if (dbc.getWebServiceProviderAnnot() != null) { 
            WebServiceProviderAnnot serviceProviderAnnot = dbc.getWebServiceProviderAnnot(); 
            serviceProviderAnnot.setPortName(portQName.getLocalPart());
            serviceProviderAnnot.setServiceName(serviceQName.getLocalPart());
            serviceProviderAnnot.setTargetNamespace(serviceQName.getNamespaceURI());
            processServiceBinding(dbc, protocolBinding);
        }

        if (portInfo.isMTOMEnabled() != null) {
            dbc.setIsMTOMEnabled(portInfo.isMTOMEnabled().booleanValue());
        }
        
        AxisService service = getService(dbcMap);       
        
        service.setName(serviceQName.getLocalPart());
        service.setEndpointName(portQName.getLocalPart());
        
        for(Iterator<AxisOperation> opIterator = service.getOperations() ; opIterator.hasNext() ;){
            AxisOperation operation = opIterator.next();
            operation.setMessageReceiver(this.messageReceiver);
            String MEP = operation.getMessageExchangePattern();
            if (!WSDLUtil.isOutputPresentForMEP(MEP)) {
                List<MethodDescriptionComposite> mdcList = dbc.getMethodDescriptionComposite(operation.getName().toString());
                for(Iterator<MethodDescriptionComposite> mIterator = mdcList.iterator(); mIterator.hasNext();){
                    MethodDescriptionComposite mdc = mIterator.next();
                    //TODO: JAXWS spec says need to check Holder param exist before taking a method as OneWay
                    mdc.setOneWayAnnot(true);
                }
            }
        }

        return service;
    }

    private String getBindingFromWSDL(Port port) {        
        Binding binding = port.getBinding();
        List extElements = binding.getExtensibilityElements();
        Iterator extElementsIterator = extElements.iterator();
        String bindingS = javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING; //this is the default.
        while (extElementsIterator.hasNext()) {
            Object o = extElementsIterator.next();
            if (o instanceof SOAPBinding) {
                SOAPBinding sp = (SOAPBinding)o;
                if (sp.getElementType().getNamespaceURI().equals("http://schemas.xmlsoap.org/wsdl/soap/")) {
                    bindingS = javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING;
                } 
            } else if (o instanceof SOAP12Binding) {
                SOAP12Binding sp = (SOAP12Binding)o;
                if (sp.getElementType().getNamespaceURI().equals("http://schemas.xmlsoap.org/wsdl/soap12/")) {
                    bindingS = javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING;
                }
            } else if (o instanceof HTTPBinding) {
                HTTPBinding sp = (HTTPBinding)o;
                if (sp.getElementType().getNamespaceURI().equals("http://schemas.xmlsoap.org/wsdl/http/")) {
                    bindingS = javax.xml.ws.http.HTTPBinding.HTTP_BINDING;
                }               
            }
        }
        return bindingS;
    }

    private void processServiceBinding(DescriptionBuilderComposite dbc, String bindingFromWSDL) {
        if (dbc.getBindingTypeAnnot() == null || bindingFromWSDL == null || bindingFromWSDL.length() == 0) {
            return;
        }
        String bindingFromAnnotation = dbc.getBindingTypeAnnot().value();
        if (bindingFromAnnotation.equals(bindingFromWSDL)) {
            return;
        }
        if (bindingFromWSDL.equals(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING)) {
            if (!bindingFromAnnotation.equals(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_MTOM_BINDING)) {
                dbc.getBindingTypeAnnot().setValue(bindingFromWSDL);
            }
        } else if (bindingFromWSDL.equals(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)) {
            if (!bindingFromAnnotation.equals(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
                dbc.getBindingTypeAnnot().setValue(bindingFromWSDL);
            }
        } else {
            dbc.getBindingTypeAnnot().setValue(bindingFromWSDL);
        }
    }

    private AxisService getService(HashMap<String, DescriptionBuilderComposite> dbcMap) {
        return getEndpointDescription(dbcMap).getAxisService();
    }
        
    private EndpointDescription getEndpointDescription(HashMap<String, DescriptionBuilderComposite> dbcMap) {
        List<ServiceDescription> serviceDescList = DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap, this.configurationContext);
        if (serviceDescList == null || serviceDescList.isEmpty()) {
            throw new RuntimeException("No service");
        }
        ServiceDescription serviceDescription = serviceDescList.get(0);
        EndpointDescription[] edArray = serviceDescription.getEndpointDescriptions();  
        if (edArray == null || edArray.length == 0) {
            throw new RuntimeException("No endpoint");
        }
        return edArray[0];
    }

    private static class WSDLGeneratorImpl implements WsdlGenerator {
        private Definition def;

        public WSDLGeneratorImpl(Definition def) {
            this.def = def;
        }
        
        public WsdlComposite generateWsdl(String implClass, EndpointDescription endpointDesc)
            throws WebServiceException {
            // Need WSDL generation code
            WsdlComposite composite = new WsdlComposite();
            composite.setWsdlFileName(implClass);
            HashMap<String, Definition> testMap = new HashMap<String, Definition>();
            testMap.put(composite.getWsdlFileName(), def);
            composite.setWsdlDefinition(testMap);
            return composite;
        }
    }
        
    public static URL getWsdlURL(String wsdlFile, URL configurationBaseUrl, ClassLoader classLoader) {
        URL wsdlURL = null;
        if (wsdlFile != null) {
            try {
                wsdlURL = new URL(wsdlFile);
            } catch (MalformedURLException e) {
                // Not a URL, try as a resource
                wsdlURL = classLoader.getResource(wsdlFile);

                if (wsdlURL == null && configurationBaseUrl != null) {
                    // Cannot get it as a resource, try with
                    // configurationBaseUrl
                    try {
                        wsdlURL = new URL(configurationBaseUrl, wsdlFile);
                    } catch (MalformedURLException ee) {
                        // ignore
                    }
                }
            }
        }
        return wsdlURL;
    }
        
    public static EndpointDescription getEndpointDescription(AxisService service) {
        Parameter param = service.getParameter(EndpointDescription.AXIS_SERVICE_PARAMETER);
        return (param == null) ? null : (EndpointDescription) param.getValue();
    }
    
    public static boolean isSOAP11(AxisService service) {
        EndpointDescription desc = AxisServiceGenerator.getEndpointDescription(service);
        return javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING.equals(desc.getBindingType()) ||
               javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_MTOM_BINDING.equals(desc.getBindingType());
    }
    
    public static boolean isHTTP(AxisService service) {
        EndpointDescription desc = AxisServiceGenerator.getEndpointDescription(service);
        return javax.xml.ws.http.HTTPBinding.HTTP_BINDING.equals(desc.getBindingType());
    }
         
}
