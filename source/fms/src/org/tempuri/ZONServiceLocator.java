/**
 * ZONServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.tempuri;

public class ZONServiceLocator extends org.apache.axis.client.Service implements org.tempuri.ZONService {

    public ZONServiceLocator() {
    }


    public ZONServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ZONServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for BasicHttpBinding_IZONService
    private java.lang.String BasicHttpBinding_IZONService_address = "http://zoncontrol.dnsalias.com/ZONIntegrationWCFService/ZONService.svc";

    public java.lang.String getBasicHttpBinding_IZONServiceAddress() {
        return BasicHttpBinding_IZONService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String BasicHttpBinding_IZONServiceWSDDServiceName = "BasicHttpBinding_IZONService";

    public java.lang.String getBasicHttpBinding_IZONServiceWSDDServiceName() {
        return BasicHttpBinding_IZONServiceWSDDServiceName;
    }

    public void setBasicHttpBinding_IZONServiceWSDDServiceName(java.lang.String name) {
        BasicHttpBinding_IZONServiceWSDDServiceName = name;
    }

    public org.tempuri.IZONService getBasicHttpBinding_IZONService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(BasicHttpBinding_IZONService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getBasicHttpBinding_IZONService(endpoint);
    }

    public org.tempuri.IZONService getBasicHttpBinding_IZONService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.tempuri.BasicHttpBinding_IZONServiceStub _stub = new org.tempuri.BasicHttpBinding_IZONServiceStub(portAddress, this);
            _stub.setPortName(getBasicHttpBinding_IZONServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setBasicHttpBinding_IZONServiceEndpointAddress(java.lang.String address) {
        BasicHttpBinding_IZONService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.tempuri.IZONService.class.isAssignableFrom(serviceEndpointInterface)) {
                org.tempuri.BasicHttpBinding_IZONServiceStub _stub = new org.tempuri.BasicHttpBinding_IZONServiceStub(new java.net.URL(BasicHttpBinding_IZONService_address), this);
                _stub.setPortName(getBasicHttpBinding_IZONServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("BasicHttpBinding_IZONService".equals(inputPortName)) {
            return getBasicHttpBinding_IZONService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "ZONService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "BasicHttpBinding_IZONService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("BasicHttpBinding_IZONService".equals(portName)) {
            setBasicHttpBinding_IZONServiceEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
