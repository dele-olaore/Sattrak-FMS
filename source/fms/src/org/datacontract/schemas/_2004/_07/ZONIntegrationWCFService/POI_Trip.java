/**
 * POI_Trip.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.datacontract.schemas._2004._07.ZONIntegrationWCFService;

public class POI_Trip  implements java.io.Serializable {
    private java.util.Calendar dateTime_Enter;

    private java.util.Calendar dateTime_Leave;

    private java.lang.Integer POI_ID;

    private java.lang.Double POI_Lat;

    private java.lang.Double POI_Lng;

    private java.lang.String plate_Number;

    private java.lang.Integer unitID;

    public POI_Trip() {
    }

    public POI_Trip(
           java.util.Calendar dateTime_Enter,
           java.util.Calendar dateTime_Leave,
           java.lang.Integer POI_ID,
           java.lang.Double POI_Lat,
           java.lang.Double POI_Lng,
           java.lang.String plate_Number,
           java.lang.Integer unitID) {
           this.dateTime_Enter = dateTime_Enter;
           this.dateTime_Leave = dateTime_Leave;
           this.POI_ID = POI_ID;
           this.POI_Lat = POI_Lat;
           this.POI_Lng = POI_Lng;
           this.plate_Number = plate_Number;
           this.unitID = unitID;
    }


    /**
     * Gets the dateTime_Enter value for this POI_Trip.
     * 
     * @return dateTime_Enter
     */
    public java.util.Calendar getDateTime_Enter() {
        return dateTime_Enter;
    }


    /**
     * Sets the dateTime_Enter value for this POI_Trip.
     * 
     * @param dateTime_Enter
     */
    public void setDateTime_Enter(java.util.Calendar dateTime_Enter) {
        this.dateTime_Enter = dateTime_Enter;
    }


    /**
     * Gets the dateTime_Leave value for this POI_Trip.
     * 
     * @return dateTime_Leave
     */
    public java.util.Calendar getDateTime_Leave() {
        return dateTime_Leave;
    }


    /**
     * Sets the dateTime_Leave value for this POI_Trip.
     * 
     * @param dateTime_Leave
     */
    public void setDateTime_Leave(java.util.Calendar dateTime_Leave) {
        this.dateTime_Leave = dateTime_Leave;
    }


    /**
     * Gets the POI_ID value for this POI_Trip.
     * 
     * @return POI_ID
     */
    public java.lang.Integer getPOI_ID() {
        return POI_ID;
    }


    /**
     * Sets the POI_ID value for this POI_Trip.
     * 
     * @param POI_ID
     */
    public void setPOI_ID(java.lang.Integer POI_ID) {
        this.POI_ID = POI_ID;
    }


    /**
     * Gets the POI_Lat value for this POI_Trip.
     * 
     * @return POI_Lat
     */
    public java.lang.Double getPOI_Lat() {
        return POI_Lat;
    }


    /**
     * Sets the POI_Lat value for this POI_Trip.
     * 
     * @param POI_Lat
     */
    public void setPOI_Lat(java.lang.Double POI_Lat) {
        this.POI_Lat = POI_Lat;
    }


    /**
     * Gets the POI_Lng value for this POI_Trip.
     * 
     * @return POI_Lng
     */
    public java.lang.Double getPOI_Lng() {
        return POI_Lng;
    }


    /**
     * Sets the POI_Lng value for this POI_Trip.
     * 
     * @param POI_Lng
     */
    public void setPOI_Lng(java.lang.Double POI_Lng) {
        this.POI_Lng = POI_Lng;
    }


    /**
     * Gets the plate_Number value for this POI_Trip.
     * 
     * @return plate_Number
     */
    public java.lang.String getPlate_Number() {
        return plate_Number;
    }


    /**
     * Sets the plate_Number value for this POI_Trip.
     * 
     * @param plate_Number
     */
    public void setPlate_Number(java.lang.String plate_Number) {
        this.plate_Number = plate_Number;
    }


    /**
     * Gets the unitID value for this POI_Trip.
     * 
     * @return unitID
     */
    public java.lang.Integer getUnitID() {
        return unitID;
    }


    /**
     * Sets the unitID value for this POI_Trip.
     * 
     * @param unitID
     */
    public void setUnitID(java.lang.Integer unitID) {
        this.unitID = unitID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof POI_Trip)) return false;
        POI_Trip other = (POI_Trip) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.dateTime_Enter==null && other.getDateTime_Enter()==null) || 
             (this.dateTime_Enter!=null &&
              this.dateTime_Enter.equals(other.getDateTime_Enter()))) &&
            ((this.dateTime_Leave==null && other.getDateTime_Leave()==null) || 
             (this.dateTime_Leave!=null &&
              this.dateTime_Leave.equals(other.getDateTime_Leave()))) &&
            ((this.POI_ID==null && other.getPOI_ID()==null) || 
             (this.POI_ID!=null &&
              this.POI_ID.equals(other.getPOI_ID()))) &&
            ((this.POI_Lat==null && other.getPOI_Lat()==null) || 
             (this.POI_Lat!=null &&
              this.POI_Lat.equals(other.getPOI_Lat()))) &&
            ((this.POI_Lng==null && other.getPOI_Lng()==null) || 
             (this.POI_Lng!=null &&
              this.POI_Lng.equals(other.getPOI_Lng()))) &&
            ((this.plate_Number==null && other.getPlate_Number()==null) || 
             (this.plate_Number!=null &&
              this.plate_Number.equals(other.getPlate_Number()))) &&
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
        if (getDateTime_Enter() != null) {
            _hashCode += getDateTime_Enter().hashCode();
        }
        if (getDateTime_Leave() != null) {
            _hashCode += getDateTime_Leave().hashCode();
        }
        if (getPOI_ID() != null) {
            _hashCode += getPOI_ID().hashCode();
        }
        if (getPOI_Lat() != null) {
            _hashCode += getPOI_Lat().hashCode();
        }
        if (getPOI_Lng() != null) {
            _hashCode += getPOI_Lng().hashCode();
        }
        if (getPlate_Number() != null) {
            _hashCode += getPlate_Number().hashCode();
        }
        if (getUnitID() != null) {
            _hashCode += getUnitID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(POI_Trip.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "POI_Trip"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dateTime_Enter");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "DateTime_Enter"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dateTime_Leave");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "DateTime_Leave"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("POI_ID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "POI_ID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("POI_Lat");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "POI_Lat"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("POI_Lng");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "POI_Lng"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("plate_Number");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Plate_Number"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
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
