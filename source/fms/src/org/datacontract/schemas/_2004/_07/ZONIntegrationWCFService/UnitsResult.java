/**
 * UnitsResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.datacontract.schemas._2004._07.ZONIntegrationWCFService;

public class UnitsResult  implements java.io.Serializable {
    private java.util.Calendar lastUpdateTimeGMT;

    private org.datacontract.schemas._2004._07.ZONIntegrationWCFService.EResult result;

    private org.datacontract.schemas._2004._07.ZONIntegrationWCFService.Unit[] units;

    public UnitsResult() {
    }

    public UnitsResult(
           java.util.Calendar lastUpdateTimeGMT,
           org.datacontract.schemas._2004._07.ZONIntegrationWCFService.EResult result,
           org.datacontract.schemas._2004._07.ZONIntegrationWCFService.Unit[] units) {
           this.lastUpdateTimeGMT = lastUpdateTimeGMT;
           this.result = result;
           this.units = units;
    }


    /**
     * Gets the lastUpdateTimeGMT value for this UnitsResult.
     * 
     * @return lastUpdateTimeGMT
     */
    public java.util.Calendar getLastUpdateTimeGMT() {
        return lastUpdateTimeGMT;
    }


    /**
     * Sets the lastUpdateTimeGMT value for this UnitsResult.
     * 
     * @param lastUpdateTimeGMT
     */
    public void setLastUpdateTimeGMT(java.util.Calendar lastUpdateTimeGMT) {
        this.lastUpdateTimeGMT = lastUpdateTimeGMT;
    }


    /**
     * Gets the result value for this UnitsResult.
     * 
     * @return result
     */
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.EResult getResult() {
        return result;
    }


    /**
     * Sets the result value for this UnitsResult.
     * 
     * @param result
     */
    public void setResult(org.datacontract.schemas._2004._07.ZONIntegrationWCFService.EResult result) {
        this.result = result;
    }


    /**
     * Gets the units value for this UnitsResult.
     * 
     * @return units
     */
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.Unit[] getUnits() {
        return units;
    }


    /**
     * Sets the units value for this UnitsResult.
     * 
     * @param units
     */
    public void setUnits(org.datacontract.schemas._2004._07.ZONIntegrationWCFService.Unit[] units) {
        this.units = units;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UnitsResult)) return false;
        UnitsResult other = (UnitsResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.lastUpdateTimeGMT==null && other.getLastUpdateTimeGMT()==null) || 
             (this.lastUpdateTimeGMT!=null &&
              this.lastUpdateTimeGMT.equals(other.getLastUpdateTimeGMT()))) &&
            ((this.result==null && other.getResult()==null) || 
             (this.result!=null &&
              this.result.equals(other.getResult()))) &&
            ((this.units==null && other.getUnits()==null) || 
             (this.units!=null &&
              java.util.Arrays.equals(this.units, other.getUnits())));
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
        if (getLastUpdateTimeGMT() != null) {
            _hashCode += getLastUpdateTimeGMT().hashCode();
        }
        if (getResult() != null) {
            _hashCode += getResult().hashCode();
        }
        if (getUnits() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUnits());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUnits(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UnitsResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "UnitsResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastUpdateTimeGMT");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "LastUpdateTimeGMT"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("result");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Result"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "eResult"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("units");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Units"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Unit"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setItemQName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Unit"));
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
