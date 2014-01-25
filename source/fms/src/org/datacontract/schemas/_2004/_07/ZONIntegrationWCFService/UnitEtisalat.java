/**
 * UnitEtisalat.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.datacontract.schemas._2004._07.ZONIntegrationWCFService;

public class UnitEtisalat  implements java.io.Serializable {
    private java.lang.Double distance;

    private java.lang.Double fuel;

    private java.util.Calendar GPSTime;

    private java.lang.Integer heading;

    private java.lang.Double langitude;

    private java.lang.Double longitude;

    private java.lang.String plateNumber;

    private java.util.Calendar serverGmtUpdateTime;

    private java.lang.String simNumber;

    private java.lang.Double speed;

    private org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEtisalatEStatus status;

    private java.lang.Integer unitID;

    private java.util.Calendar unitTime;

    public UnitEtisalat() {
    }

    public UnitEtisalat(
           java.lang.Double distance,
           java.lang.Double fuel,
           java.util.Calendar GPSTime,
           java.lang.Integer heading,
           java.lang.Double langitude,
           java.lang.Double longitude,
           java.lang.String plateNumber,
           java.util.Calendar serverGmtUpdateTime,
           java.lang.String simNumber,
           java.lang.Double speed,
           org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEtisalatEStatus status,
           java.lang.Integer unitID,
           java.util.Calendar unitTime) {
           this.distance = distance;
           this.fuel = fuel;
           this.GPSTime = GPSTime;
           this.heading = heading;
           this.langitude = langitude;
           this.longitude = longitude;
           this.plateNumber = plateNumber;
           this.serverGmtUpdateTime = serverGmtUpdateTime;
           this.simNumber = simNumber;
           this.speed = speed;
           this.status = status;
           this.unitID = unitID;
           this.unitTime = unitTime;
    }


    /**
     * Gets the distance value for this UnitEtisalat.
     * 
     * @return distance
     */
    public java.lang.Double getDistance() {
        return distance;
    }


    /**
     * Sets the distance value for this UnitEtisalat.
     * 
     * @param distance
     */
    public void setDistance(java.lang.Double distance) {
        this.distance = distance;
    }


    /**
     * Gets the fuel value for this UnitEtisalat.
     * 
     * @return fuel
     */
    public java.lang.Double getFuel() {
        return fuel;
    }


    /**
     * Sets the fuel value for this UnitEtisalat.
     * 
     * @param fuel
     */
    public void setFuel(java.lang.Double fuel) {
        this.fuel = fuel;
    }


    /**
     * Gets the GPSTime value for this UnitEtisalat.
     * 
     * @return GPSTime
     */
    public java.util.Calendar getGPSTime() {
        return GPSTime;
    }


    /**
     * Sets the GPSTime value for this UnitEtisalat.
     * 
     * @param GPSTime
     */
    public void setGPSTime(java.util.Calendar GPSTime) {
        this.GPSTime = GPSTime;
    }


    /**
     * Gets the heading value for this UnitEtisalat.
     * 
     * @return heading
     */
    public java.lang.Integer getHeading() {
        return heading;
    }


    /**
     * Sets the heading value for this UnitEtisalat.
     * 
     * @param heading
     */
    public void setHeading(java.lang.Integer heading) {
        this.heading = heading;
    }


    /**
     * Gets the langitude value for this UnitEtisalat.
     * 
     * @return langitude
     */
    public java.lang.Double getLangitude() {
        return langitude;
    }


    /**
     * Sets the langitude value for this UnitEtisalat.
     * 
     * @param langitude
     */
    public void setLangitude(java.lang.Double langitude) {
        this.langitude = langitude;
    }


    /**
     * Gets the longitude value for this UnitEtisalat.
     * 
     * @return longitude
     */
    public java.lang.Double getLongitude() {
        return longitude;
    }


    /**
     * Sets the longitude value for this UnitEtisalat.
     * 
     * @param longitude
     */
    public void setLongitude(java.lang.Double longitude) {
        this.longitude = longitude;
    }


    /**
     * Gets the plateNumber value for this UnitEtisalat.
     * 
     * @return plateNumber
     */
    public java.lang.String getPlateNumber() {
        return plateNumber;
    }


    /**
     * Sets the plateNumber value for this UnitEtisalat.
     * 
     * @param plateNumber
     */
    public void setPlateNumber(java.lang.String plateNumber) {
        this.plateNumber = plateNumber;
    }


    /**
     * Gets the serverGmtUpdateTime value for this UnitEtisalat.
     * 
     * @return serverGmtUpdateTime
     */
    public java.util.Calendar getServerGmtUpdateTime() {
        return serverGmtUpdateTime;
    }


    /**
     * Sets the serverGmtUpdateTime value for this UnitEtisalat.
     * 
     * @param serverGmtUpdateTime
     */
    public void setServerGmtUpdateTime(java.util.Calendar serverGmtUpdateTime) {
        this.serverGmtUpdateTime = serverGmtUpdateTime;
    }


    /**
     * Gets the simNumber value for this UnitEtisalat.
     * 
     * @return simNumber
     */
    public java.lang.String getSimNumber() {
        return simNumber;
    }


    /**
     * Sets the simNumber value for this UnitEtisalat.
     * 
     * @param simNumber
     */
    public void setSimNumber(java.lang.String simNumber) {
        this.simNumber = simNumber;
    }


    /**
     * Gets the speed value for this UnitEtisalat.
     * 
     * @return speed
     */
    public java.lang.Double getSpeed() {
        return speed;
    }


    /**
     * Sets the speed value for this UnitEtisalat.
     * 
     * @param speed
     */
    public void setSpeed(java.lang.Double speed) {
        this.speed = speed;
    }


    /**
     * Gets the status value for this UnitEtisalat.
     * 
     * @return status
     */
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEtisalatEStatus getStatus() {
        return status;
    }


    /**
     * Sets the status value for this UnitEtisalat.
     * 
     * @param status
     */
    public void setStatus(org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEtisalatEStatus status) {
        this.status = status;
    }


    /**
     * Gets the unitID value for this UnitEtisalat.
     * 
     * @return unitID
     */
    public java.lang.Integer getUnitID() {
        return unitID;
    }


    /**
     * Sets the unitID value for this UnitEtisalat.
     * 
     * @param unitID
     */
    public void setUnitID(java.lang.Integer unitID) {
        this.unitID = unitID;
    }


    /**
     * Gets the unitTime value for this UnitEtisalat.
     * 
     * @return unitTime
     */
    public java.util.Calendar getUnitTime() {
        return unitTime;
    }


    /**
     * Sets the unitTime value for this UnitEtisalat.
     * 
     * @param unitTime
     */
    public void setUnitTime(java.util.Calendar unitTime) {
        this.unitTime = unitTime;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UnitEtisalat)) return false;
        UnitEtisalat other = (UnitEtisalat) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.distance==null && other.getDistance()==null) || 
             (this.distance!=null &&
              this.distance.equals(other.getDistance()))) &&
            ((this.fuel==null && other.getFuel()==null) || 
             (this.fuel!=null &&
              this.fuel.equals(other.getFuel()))) &&
            ((this.GPSTime==null && other.getGPSTime()==null) || 
             (this.GPSTime!=null &&
              this.GPSTime.equals(other.getGPSTime()))) &&
            ((this.heading==null && other.getHeading()==null) || 
             (this.heading!=null &&
              this.heading.equals(other.getHeading()))) &&
            ((this.langitude==null && other.getLangitude()==null) || 
             (this.langitude!=null &&
              this.langitude.equals(other.getLangitude()))) &&
            ((this.longitude==null && other.getLongitude()==null) || 
             (this.longitude!=null &&
              this.longitude.equals(other.getLongitude()))) &&
            ((this.plateNumber==null && other.getPlateNumber()==null) || 
             (this.plateNumber!=null &&
              this.plateNumber.equals(other.getPlateNumber()))) &&
            ((this.serverGmtUpdateTime==null && other.getServerGmtUpdateTime()==null) || 
             (this.serverGmtUpdateTime!=null &&
              this.serverGmtUpdateTime.equals(other.getServerGmtUpdateTime()))) &&
            ((this.simNumber==null && other.getSimNumber()==null) || 
             (this.simNumber!=null &&
              this.simNumber.equals(other.getSimNumber()))) &&
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
        if (getDistance() != null) {
            _hashCode += getDistance().hashCode();
        }
        if (getFuel() != null) {
            _hashCode += getFuel().hashCode();
        }
        if (getGPSTime() != null) {
            _hashCode += getGPSTime().hashCode();
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
        if (getPlateNumber() != null) {
            _hashCode += getPlateNumber().hashCode();
        }
        if (getServerGmtUpdateTime() != null) {
            _hashCode += getServerGmtUpdateTime().hashCode();
        }
        if (getSimNumber() != null) {
            _hashCode += getSimNumber().hashCode();
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
        new org.apache.axis.description.TypeDesc(UnitEtisalat.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "UnitEtisalat"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("distance");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Distance"));
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
        elemField.setFieldName("plateNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "PlateNumber"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("serverGmtUpdateTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "ServerGmtUpdateTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("simNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "SimNumber"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
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
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "UnitEtisalat.eStatus"));
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
