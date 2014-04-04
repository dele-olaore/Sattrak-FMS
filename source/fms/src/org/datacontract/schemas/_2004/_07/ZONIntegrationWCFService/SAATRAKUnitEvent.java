/**
 * SAATRAKUnitEvent.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.datacontract.schemas._2004._07.ZONIntegrationWCFService;

public class SAATRAKUnitEvent  implements java.io.Serializable {
    private java.lang.String address;

    private java.lang.Double distance;

    private java.lang.Double engineHours;

    private java.lang.Double eventFinalValue;

    private java.lang.Double eventInitValue;

    private java.lang.String eventName;

    private java.util.Calendar eventTime;

    private org.datacontract.schemas._2004._07.ZONIntegrationWCFService.SAATRAKUnitEventEEvent eventType;

    private java.lang.Double eventValue;

    private java.lang.Double fuel;

    private java.util.Calendar GPSTime;

    private java.lang.Integer HDOP;

    private java.lang.Integer heading;

    private java.lang.Double langitude;

    private java.lang.Double longitude;

    private java.lang.Double speed;

    private org.datacontract.schemas._2004._07.ZONIntegrationWCFService.SAATRAKUnitEventEStatus status;

    private java.lang.Integer unitID;

    public SAATRAKUnitEvent() {
    }

    public SAATRAKUnitEvent(
           java.lang.String address,
           java.lang.Double distance,
           java.lang.Double engineHours,
           java.lang.Double eventFinalValue,
           java.lang.Double eventInitValue,
           java.lang.String eventName,
           java.util.Calendar eventTime,
           org.datacontract.schemas._2004._07.ZONIntegrationWCFService.SAATRAKUnitEventEEvent eventType,
           java.lang.Double eventValue,
           java.lang.Double fuel,
           java.util.Calendar GPSTime,
           java.lang.Integer HDOP,
           java.lang.Integer heading,
           java.lang.Double langitude,
           java.lang.Double longitude,
           java.lang.Double speed,
           org.datacontract.schemas._2004._07.ZONIntegrationWCFService.SAATRAKUnitEventEStatus status,
           java.lang.Integer unitID) {
           this.address = address;
           this.distance = distance;
           this.engineHours = engineHours;
           this.eventFinalValue = eventFinalValue;
           this.eventInitValue = eventInitValue;
           this.eventName = eventName;
           this.eventTime = eventTime;
           this.eventType = eventType;
           this.eventValue = eventValue;
           this.fuel = fuel;
           this.GPSTime = GPSTime;
           this.HDOP = HDOP;
           this.heading = heading;
           this.langitude = langitude;
           this.longitude = longitude;
           this.speed = speed;
           this.status = status;
           this.unitID = unitID;
    }


    /**
     * Gets the address value for this SAATRAKUnitEvent.
     * 
     * @return address
     */
    public java.lang.String getAddress() {
        return address;
    }


    /**
     * Sets the address value for this SAATRAKUnitEvent.
     * 
     * @param address
     */
    public void setAddress(java.lang.String address) {
        this.address = address;
    }


    /**
     * Gets the distance value for this SAATRAKUnitEvent.
     * 
     * @return distance
     */
    public java.lang.Double getDistance() {
        return distance;
    }


    /**
     * Sets the distance value for this SAATRAKUnitEvent.
     * 
     * @param distance
     */
    public void setDistance(java.lang.Double distance) {
        this.distance = distance;
    }


    /**
     * Gets the engineHours value for this SAATRAKUnitEvent.
     * 
     * @return engineHours
     */
    public java.lang.Double getEngineHours() {
        return engineHours;
    }


    /**
     * Sets the engineHours value for this SAATRAKUnitEvent.
     * 
     * @param engineHours
     */
    public void setEngineHours(java.lang.Double engineHours) {
        this.engineHours = engineHours;
    }


    /**
     * Gets the eventFinalValue value for this SAATRAKUnitEvent.
     * 
     * @return eventFinalValue
     */
    public java.lang.Double getEventFinalValue() {
        return eventFinalValue;
    }


    /**
     * Sets the eventFinalValue value for this SAATRAKUnitEvent.
     * 
     * @param eventFinalValue
     */
    public void setEventFinalValue(java.lang.Double eventFinalValue) {
        this.eventFinalValue = eventFinalValue;
    }


    /**
     * Gets the eventInitValue value for this SAATRAKUnitEvent.
     * 
     * @return eventInitValue
     */
    public java.lang.Double getEventInitValue() {
        return eventInitValue;
    }


    /**
     * Sets the eventInitValue value for this SAATRAKUnitEvent.
     * 
     * @param eventInitValue
     */
    public void setEventInitValue(java.lang.Double eventInitValue) {
        this.eventInitValue = eventInitValue;
    }


    /**
     * Gets the eventName value for this SAATRAKUnitEvent.
     * 
     * @return eventName
     */
    public java.lang.String getEventName() {
        return eventName;
    }


    /**
     * Sets the eventName value for this SAATRAKUnitEvent.
     * 
     * @param eventName
     */
    public void setEventName(java.lang.String eventName) {
        this.eventName = eventName;
    }


    /**
     * Gets the eventTime value for this SAATRAKUnitEvent.
     * 
     * @return eventTime
     */
    public java.util.Calendar getEventTime() {
        return eventTime;
    }


    /**
     * Sets the eventTime value for this SAATRAKUnitEvent.
     * 
     * @param eventTime
     */
    public void setEventTime(java.util.Calendar eventTime) {
        this.eventTime = eventTime;
    }


    /**
     * Gets the eventType value for this SAATRAKUnitEvent.
     * 
     * @return eventType
     */
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.SAATRAKUnitEventEEvent getEventType() {
        return eventType;
    }


    /**
     * Sets the eventType value for this SAATRAKUnitEvent.
     * 
     * @param eventType
     */
    public void setEventType(org.datacontract.schemas._2004._07.ZONIntegrationWCFService.SAATRAKUnitEventEEvent eventType) {
        this.eventType = eventType;
    }


    /**
     * Gets the eventValue value for this SAATRAKUnitEvent.
     * 
     * @return eventValue
     */
    public java.lang.Double getEventValue() {
        return eventValue;
    }


    /**
     * Sets the eventValue value for this SAATRAKUnitEvent.
     * 
     * @param eventValue
     */
    public void setEventValue(java.lang.Double eventValue) {
        this.eventValue = eventValue;
    }


    /**
     * Gets the fuel value for this SAATRAKUnitEvent.
     * 
     * @return fuel
     */
    public java.lang.Double getFuel() {
        return fuel;
    }


    /**
     * Sets the fuel value for this SAATRAKUnitEvent.
     * 
     * @param fuel
     */
    public void setFuel(java.lang.Double fuel) {
        this.fuel = fuel;
    }


    /**
     * Gets the GPSTime value for this SAATRAKUnitEvent.
     * 
     * @return GPSTime
     */
    public java.util.Calendar getGPSTime() {
        return GPSTime;
    }


    /**
     * Sets the GPSTime value for this SAATRAKUnitEvent.
     * 
     * @param GPSTime
     */
    public void setGPSTime(java.util.Calendar GPSTime) {
        this.GPSTime = GPSTime;
    }


    /**
     * Gets the HDOP value for this SAATRAKUnitEvent.
     * 
     * @return HDOP
     */
    public java.lang.Integer getHDOP() {
        return HDOP;
    }


    /**
     * Sets the HDOP value for this SAATRAKUnitEvent.
     * 
     * @param HDOP
     */
    public void setHDOP(java.lang.Integer HDOP) {
        this.HDOP = HDOP;
    }


    /**
     * Gets the heading value for this SAATRAKUnitEvent.
     * 
     * @return heading
     */
    public java.lang.Integer getHeading() {
        return heading;
    }


    /**
     * Sets the heading value for this SAATRAKUnitEvent.
     * 
     * @param heading
     */
    public void setHeading(java.lang.Integer heading) {
        this.heading = heading;
    }


    /**
     * Gets the langitude value for this SAATRAKUnitEvent.
     * 
     * @return langitude
     */
    public java.lang.Double getLangitude() {
        return langitude;
    }


    /**
     * Sets the langitude value for this SAATRAKUnitEvent.
     * 
     * @param langitude
     */
    public void setLangitude(java.lang.Double langitude) {
        this.langitude = langitude;
    }


    /**
     * Gets the longitude value for this SAATRAKUnitEvent.
     * 
     * @return longitude
     */
    public java.lang.Double getLongitude() {
        return longitude;
    }


    /**
     * Sets the longitude value for this SAATRAKUnitEvent.
     * 
     * @param longitude
     */
    public void setLongitude(java.lang.Double longitude) {
        this.longitude = longitude;
    }


    /**
     * Gets the speed value for this SAATRAKUnitEvent.
     * 
     * @return speed
     */
    public java.lang.Double getSpeed() {
        return speed;
    }


    /**
     * Sets the speed value for this SAATRAKUnitEvent.
     * 
     * @param speed
     */
    public void setSpeed(java.lang.Double speed) {
        this.speed = speed;
    }


    /**
     * Gets the status value for this SAATRAKUnitEvent.
     * 
     * @return status
     */
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.SAATRAKUnitEventEStatus getStatus() {
        return status;
    }


    /**
     * Sets the status value for this SAATRAKUnitEvent.
     * 
     * @param status
     */
    public void setStatus(org.datacontract.schemas._2004._07.ZONIntegrationWCFService.SAATRAKUnitEventEStatus status) {
        this.status = status;
    }


    /**
     * Gets the unitID value for this SAATRAKUnitEvent.
     * 
     * @return unitID
     */
    public java.lang.Integer getUnitID() {
        return unitID;
    }


    /**
     * Sets the unitID value for this SAATRAKUnitEvent.
     * 
     * @param unitID
     */
    public void setUnitID(java.lang.Integer unitID) {
        this.unitID = unitID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SAATRAKUnitEvent)) return false;
        SAATRAKUnitEvent other = (SAATRAKUnitEvent) obj;
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
            ((this.engineHours==null && other.getEngineHours()==null) || 
             (this.engineHours!=null &&
              this.engineHours.equals(other.getEngineHours()))) &&
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
            ((this.fuel==null && other.getFuel()==null) || 
             (this.fuel!=null &&
              this.fuel.equals(other.getFuel()))) &&
            ((this.GPSTime==null && other.getGPSTime()==null) || 
             (this.GPSTime!=null &&
              this.GPSTime.equals(other.getGPSTime()))) &&
            ((this.HDOP==null && other.getHDOP()==null) || 
             (this.HDOP!=null &&
              this.HDOP.equals(other.getHDOP()))) &&
            ((this.heading==null && other.getHeading()==null) || 
             (this.heading!=null &&
              this.heading.equals(other.getHeading()))) &&
            ((this.langitude==null && other.getLangitude()==null) || 
             (this.langitude!=null &&
              this.langitude.equals(other.getLangitude()))) &&
            ((this.longitude==null && other.getLongitude()==null) || 
             (this.longitude!=null &&
              this.longitude.equals(other.getLongitude()))) &&
            ((this.speed==null && other.getSpeed()==null) || 
             (this.speed!=null &&
              this.speed.equals(other.getSpeed()))) &&
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
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
        if (getEngineHours() != null) {
            _hashCode += getEngineHours().hashCode();
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
        if (getFuel() != null) {
            _hashCode += getFuel().hashCode();
        }
        if (getGPSTime() != null) {
            _hashCode += getGPSTime().hashCode();
        }
        if (getHDOP() != null) {
            _hashCode += getHDOP().hashCode();
        }
        if (getHeading() != null) {
            _hashCode += getHeading().hashCode();
        }
        if (getLangitude() != null) {
            _hashCode += getLangitude().hashCode();
        }
        if (getLongitude() != null) {
            _hashCode += getLongitude().hashCode();
        }
        if (getSpeed() != null) {
            _hashCode += getSpeed().hashCode();
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getUnitID() != null) {
            _hashCode += getUnitID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SAATRAKUnitEvent.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "SAATRAKUnitEvent"));
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
        elemField.setFieldName("engineHours");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "EngineHours"));
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
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "SAATRAKUnitEvent.eEvent"));
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
        elemField.setFieldName("fuel");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Fuel"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("GPSTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "GPSTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("HDOP");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "HDOP"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("heading");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Heading"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("langitude");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Langitude"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("longitude");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Longitude"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("speed");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Speed"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "SAATRAKUnitEvent.eStatus"));
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
