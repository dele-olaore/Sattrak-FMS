/**
 * UnitEvent.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.datacontract.schemas._2004._07.ZONIntegrationWCFService;

public class UnitEvent  implements java.io.Serializable {
    private java.lang.String address;

    private java.lang.Double distance;

    private java.lang.Double eventFinalValue;

    private java.lang.Double eventInitValue;

    private java.lang.String eventName;

    private java.util.Calendar eventTime;

    private org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEventEEvent eventType;

    private java.lang.Double eventValue;

    private java.lang.Integer unitID;

    public UnitEvent() {
    }

    public UnitEvent(
           java.lang.String address,
           java.lang.Double distance,
           java.lang.Double eventFinalValue,
           java.lang.Double eventInitValue,
           java.lang.String eventName,
           java.util.Calendar eventTime,
           org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEventEEvent eventType,
           java.lang.Double eventValue,
           java.lang.Integer unitID) {
           this.address = address;
           this.distance = distance;
           this.eventFinalValue = eventFinalValue;
           this.eventInitValue = eventInitValue;
           this.eventName = eventName;
           this.eventTime = eventTime;
           this.eventType = eventType;
           this.eventValue = eventValue;
           this.unitID = unitID;
    }


    /**
     * Gets the address value for this UnitEvent.
     * 
     * @return address
     */
    public java.lang.String getAddress() {
        return address;
    }


    /**
     * Sets the address value for this UnitEvent.
     * 
     * @param address
     */
    public void setAddress(java.lang.String address) {
        this.address = address;
    }


    /**
     * Gets the distance value for this UnitEvent.
     * 
     * @return distance
     */
    public java.lang.Double getDistance() {
        return distance;
    }


    /**
     * Sets the distance value for this UnitEvent.
     * 
     * @param distance
     */
    public void setDistance(java.lang.Double distance) {
        this.distance = distance;
    }


    /**
     * Gets the eventFinalValue value for this UnitEvent.
     * 
     * @return eventFinalValue
     */
    public java.lang.Double getEventFinalValue() {
        return eventFinalValue;
    }


    /**
     * Sets the eventFinalValue value for this UnitEvent.
     * 
     * @param eventFinalValue
     */
    public void setEventFinalValue(java.lang.Double eventFinalValue) {
        this.eventFinalValue = eventFinalValue;
    }


    /**
     * Gets the eventInitValue value for this UnitEvent.
     * 
     * @return eventInitValue
     */
    public java.lang.Double getEventInitValue() {
        return eventInitValue;
    }


    /**
     * Sets the eventInitValue value for this UnitEvent.
     * 
     * @param eventInitValue
     */
    public void setEventInitValue(java.lang.Double eventInitValue) {
        this.eventInitValue = eventInitValue;
    }


    /**
     * Gets the eventName value for this UnitEvent.
     * 
     * @return eventName
     */
    public java.lang.String getEventName() {
        return eventName;
    }


    /**
     * Sets the eventName value for this UnitEvent.
     * 
     * @param eventName
     */
    public void setEventName(java.lang.String eventName) {
        this.eventName = eventName;
    }


    /**
     * Gets the eventTime value for this UnitEvent.
     * 
     * @return eventTime
     */
    public java.util.Calendar getEventTime() {
        return eventTime;
    }


    /**
     * Sets the eventTime value for this UnitEvent.
     * 
     * @param eventTime
     */
    public void setEventTime(java.util.Calendar eventTime) {
        this.eventTime = eventTime;
    }


    /**
     * Gets the eventType value for this UnitEvent.
     * 
     * @return eventType
     */
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEventEEvent getEventType() {
        return eventType;
    }


    /**
     * Sets the eventType value for this UnitEvent.
     * 
     * @param eventType
     */
    public void setEventType(org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEventEEvent eventType) {
        this.eventType = eventType;
    }


    /**
     * Gets the eventValue value for this UnitEvent.
     * 
     * @return eventValue
     */
    public java.lang.Double getEventValue() {
        return eventValue;
    }


    /**
     * Sets the eventValue value for this UnitEvent.
     * 
     * @param eventValue
     */
    public void setEventValue(java.lang.Double eventValue) {
        this.eventValue = eventValue;
    }


    /**
     * Gets the unitID value for this UnitEvent.
     * 
     * @return unitID
     */
    public java.lang.Integer getUnitID() {
        return unitID;
    }


    /**
     * Sets the unitID value for this UnitEvent.
     * 
     * @param unitID
     */
    public void setUnitID(java.lang.Integer unitID) {
        this.unitID = unitID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UnitEvent)) return false;
        UnitEvent other = (UnitEvent) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.address==null && other.getAddress()==null) || 
             (this.address!=null &&
              this.address.equals(other.getAddress()))) &&
            ((this.distance==null && other.getDistance()==null) || 
             (this.distance!=null &&
              this.distance.equals(other.getDistance()))) &&
            ((this.eventFinalValue==null && other.getEventFinalValue()==null) || 
             (this.eventFinalValue!=null &&
              this.eventFinalValue.equals(other.getEventFinalValue()))) &&
            ((this.eventInitValue==null && other.getEventInitValue()==null) || 
             (this.eventInitValue!=null &&
              this.eventInitValue.equals(other.getEventInitValue()))) &&
            ((this.eventName==null && other.getEventName()==null) || 
             (this.eventName!=null &&
              this.eventName.equals(other.getEventName()))) &&
            ((this.eventTime==null && other.getEventTime()==null) || 
             (this.eventTime!=null &&
              this.eventTime.equals(other.getEventTime()))) &&
            ((this.eventType==null && other.getEventType()==null) || 
             (this.eventType!=null &&
              this.eventType.equals(other.getEventType()))) &&
            ((this.eventValue==null && other.getEventValue()==null) || 
             (this.eventValue!=null &&
              this.eventValue.equals(other.getEventValue()))) &&
            ((this.unitID==null && other.getUnitID()==null) || 
             (this.unitID!=null &&
              this.unitID.equals(other.getUnitID())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getAddress() != null) {
            _hashCode += getAddress().hashCode();
        }
        if (getDistance() != null) {
            _hashCode += getDistance().hashCode();
        }
        if (getEventFinalValue() != null) {
            _hashCode += getEventFinalValue().hashCode();
        }
        if (getEventInitValue() != null) {
            _hashCode += getEventInitValue().hashCode();
        }
        if (getEventName() != null) {
            _hashCode += getEventName().hashCode();
        }
        if (getEventTime() != null) {
            _hashCode += getEventTime().hashCode();
        }
        if (getEventType() != null) {
            _hashCode += getEventType().hashCode();
        }
        if (getEventValue() != null) {
            _hashCode += getEventValue().hashCode();
        }
        if (getUnitID() != null) {
            _hashCode += getUnitID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UnitEvent.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "UnitEvent"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("address");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Address"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("distance");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Distance"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eventFinalValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "EventFinalValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eventInitValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "EventInitValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eventName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "EventName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eventTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "EventTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eventType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "EventType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "UnitEvent.eEvent"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eventValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "EventValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("unitID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "UnitID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
