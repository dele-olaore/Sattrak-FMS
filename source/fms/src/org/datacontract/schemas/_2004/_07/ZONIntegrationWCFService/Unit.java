/**
 * Unit.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.datacontract.schemas._2004._07.ZONIntegrationWCFService;

public class Unit  implements java.io.Serializable {
    private java.lang.String address;

    private java.lang.Double distance;

    private java.lang.Double engineHours;

    private java.lang.Integer fleetID;

    private java.lang.Double fuel;

    private java.util.Calendar GPSTime;

    private java.lang.Integer groupID;

    private java.lang.Integer HDOP;

    private java.lang.Integer heading;

    private java.lang.Double langitude;

    private java.lang.Double longitude;

    private java.lang.Double speed;

    private org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEStatus status;

    private java.lang.Integer unitID;

    private java.util.Calendar unitTime;

    public Unit() {
    }

    public Unit(
           java.lang.String address,
           java.lang.Double distance,
           java.lang.Double engineHours,
           java.lang.Integer fleetID,
           java.lang.Double fuel,
           java.util.Calendar GPSTime,
           java.lang.Integer groupID,
           java.lang.Integer HDOP,
           java.lang.Integer heading,
           java.lang.Double langitude,
           java.lang.Double longitude,
           java.lang.Double speed,
           org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEStatus status,
           java.lang.Integer unitID,
           java.util.Calendar unitTime) {
           this.address = address;
           this.distance = distance;
           this.engineHours = engineHours;
           this.fleetID = fleetID;
           this.fuel = fuel;
           this.GPSTime = GPSTime;
           this.groupID = groupID;
           this.HDOP = HDOP;
           this.heading = heading;
           this.langitude = langitude;
           this.longitude = longitude;
           this.speed = speed;
           this.status = status;
           this.unitID = unitID;
           this.unitTime = unitTime;
    }


    /**
     * Gets the address value for this Unit.
     * 
     * @return address
     */
    public java.lang.String getAddress() {
        return address;
    }


    /**
     * Sets the address value for this Unit.
     * 
     * @param address
     */
    public void setAddress(java.lang.String address) {
        this.address = address;
    }


    /**
     * Gets the distance value for this Unit.
     * 
     * @return distance
     */
    public java.lang.Double getDistance() {
        return distance;
    }


    /**
     * Sets the distance value for this Unit.
     * 
     * @param distance
     */
    public void setDistance(java.lang.Double distance) {
        this.distance = distance;
    }


    /**
     * Gets the engineHours value for this Unit.
     * 
     * @return engineHours
     */
    public java.lang.Double getEngineHours() {
        return engineHours;
    }


    /**
     * Sets the engineHours value for this Unit.
     * 
     * @param engineHours
     */
    public void setEngineHours(java.lang.Double engineHours) {
        this.engineHours = engineHours;
    }


    /**
     * Gets the fleetID value for this Unit.
     * 
     * @return fleetID
     */
    public java.lang.Integer getFleetID() {
        return fleetID;
    }


    /**
     * Sets the fleetID value for this Unit.
     * 
     * @param fleetID
     */
    public void setFleetID(java.lang.Integer fleetID) {
        this.fleetID = fleetID;
    }


    /**
     * Gets the fuel value for this Unit.
     * 
     * @return fuel
     */
    public java.lang.Double getFuel() {
        return fuel;
    }


    /**
     * Sets the fuel value for this Unit.
     * 
     * @param fuel
     */
    public void setFuel(java.lang.Double fuel) {
        this.fuel = fuel;
    }


    /**
     * Gets the GPSTime value for this Unit.
     * 
     * @return GPSTime
     */
    public java.util.Calendar getGPSTime() {
        return GPSTime;
    }


    /**
     * Sets the GPSTime value for this Unit.
     * 
     * @param GPSTime
     */
    public void setGPSTime(java.util.Calendar GPSTime) {
        this.GPSTime = GPSTime;
    }


    /**
     * Gets the groupID value for this Unit.
     * 
     * @return groupID
     */
    public java.lang.Integer getGroupID() {
        return groupID;
    }


    /**
     * Sets the groupID value for this Unit.
     * 
     * @param groupID
     */
    public void setGroupID(java.lang.Integer groupID) {
        this.groupID = groupID;
    }


    /**
     * Gets the HDOP value for this Unit.
     * 
     * @return HDOP
     */
    public java.lang.Integer getHDOP() {
        return HDOP;
    }


    /**
     * Sets the HDOP value for this Unit.
     * 
     * @param HDOP
     */
    public void setHDOP(java.lang.Integer HDOP) {
        this.HDOP = HDOP;
    }


    /**
     * Gets the heading value for this Unit.
     * 
     * @return heading
     */
    public java.lang.Integer getHeading() {
        return heading;
    }


    /**
     * Sets the heading value for this Unit.
     * 
     * @param heading
     */
    public void setHeading(java.lang.Integer heading) {
        this.heading = heading;
    }


    /**
     * Gets the langitude value for this Unit.
     * 
     * @return langitude
     */
    public java.lang.Double getLangitude() {
        return langitude;
    }


    /**
     * Sets the langitude value for this Unit.
     * 
     * @param langitude
     */
    public void setLangitude(java.lang.Double langitude) {
        this.langitude = langitude;
    }


    /**
     * Gets the longitude value for this Unit.
     * 
     * @return longitude
     */
    public java.lang.Double getLongitude() {
        return longitude;
    }


    /**
     * Sets the longitude value for this Unit.
     * 
     * @param longitude
     */
    public void setLongitude(java.lang.Double longitude) {
        this.longitude = longitude;
    }


    /**
     * Gets the speed value for this Unit.
     * 
     * @return speed
     */
    public java.lang.Double getSpeed() {
        return speed;
    }


    /**
     * Sets the speed value for this Unit.
     * 
     * @param speed
     */
    public void setSpeed(java.lang.Double speed) {
        this.speed = speed;
    }


    /**
     * Gets the status value for this Unit.
     * 
     * @return status
     */
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEStatus getStatus() {
        return status;
    }


    /**
     * Sets the status value for this Unit.
     * 
     * @param status
     */
    public void setStatus(org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEStatus status) {
        this.status = status;
    }


    /**
     * Gets the unitID value for this Unit.
     * 
     * @return unitID
     */
    public java.lang.Integer getUnitID() {
        return unitID;
    }


    /**
     * Sets the unitID value for this Unit.
     * 
     * @param unitID
     */
    public void setUnitID(java.lang.Integer unitID) {
        this.unitID = unitID;
    }


    /**
     * Gets the unitTime value for this Unit.
     * 
     * @return unitTime
     */
    public java.util.Calendar getUnitTime() {
        return unitTime;
    }


    /**
     * Sets the unitTime value for this Unit.
     * 
     * @param unitTime
     */
    public void setUnitTime(java.util.Calendar unitTime) {
        this.unitTime = unitTime;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Unit)) return false;
        Unit other = (Unit) obj;
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
            ((this.fleetID==null && other.getFleetID()==null) || 
             (this.fleetID!=null &&
              this.fleetID.equals(other.getFleetID()))) &&
            ((this.fuel==null && other.getFuel()==null) || 
             (this.fuel!=null &&
              this.fuel.equals(other.getFuel()))) &&
            ((this.GPSTime==null && other.getGPSTime()==null) || 
             (this.GPSTime!=null &&
              this.GPSTime.equals(other.getGPSTime()))) &&
            ((this.groupID==null && other.getGroupID()==null) || 
             (this.groupID!=null &&
              this.groupID.equals(other.getGroupID()))) &&
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
              this.unitID.equals(other.getUnitID()))) &&
            ((this.unitTime==null && other.getUnitTime()==null) || 
             (this.unitTime!=null &&
              this.unitTime.equals(other.getUnitTime())));
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
        if (getFleetID() != null) {
            _hashCode += getFleetID().hashCode();
        }
        if (getFuel() != null) {
            _hashCode += getFuel().hashCode();
        }
        if (getGPSTime() != null) {
            _hashCode += getGPSTime().hashCode();
        }
        if (getGroupID() != null) {
            _hashCode += getGroupID().hashCode();
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
        if (getUnitTime() != null) {
            _hashCode += getUnitTime().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Unit.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Unit"));
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
        elemField.setFieldName("fleetID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "FleetID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
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
        elemField.setFieldName("groupID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "GroupID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
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
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Unit.eStatus"));
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
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("unitTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "UnitTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
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
