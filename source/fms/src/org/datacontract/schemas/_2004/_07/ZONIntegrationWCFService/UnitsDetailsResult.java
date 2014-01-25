/**
 * UnitsDetailsResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.datacontract.schemas._2004._07.ZONIntegrationWCFService;

public class UnitsDetailsResult  implements java.io.Serializable {
    private org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitsDetailsResultEResult result;

    private org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitInfo[] unitsInfo;

    public UnitsDetailsResult() {
    }

    public UnitsDetailsResult(
           org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitsDetailsResultEResult result,
           org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitInfo[] unitsInfo) {
           this.result = result;
           this.unitsInfo = unitsInfo;
    }


    /**
     * Gets the result value for this UnitsDetailsResult.
     * 
     * @return result
     */
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitsDetailsResultEResult getResult() {
        return result;
    }


    /**
     * Sets the result value for this UnitsDetailsResult.
     * 
     * @param result
     */
    public void setResult(org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitsDetailsResultEResult result) {
        this.result = result;
    }


    /**
     * Gets the unitsInfo value for this UnitsDetailsResult.
     * 
     * @return unitsInfo
     */
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitInfo[] getUnitsInfo() {
        return unitsInfo;
    }


    /**
     * Sets the unitsInfo value for this UnitsDetailsResult.
     * 
     * @param unitsInfo
     */
    public void setUnitsInfo(org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitInfo[] unitsInfo) {
        this.unitsInfo = unitsInfo;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UnitsDetailsResult)) return false;
        UnitsDetailsResult other = (UnitsDetailsResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.result==null && other.getResult()==null) || 
             (this.result!=null &&
              this.result.equals(other.getResult()))) &&
            ((this.unitsInfo==null && other.getUnitsInfo()==null) || 
             (this.unitsInfo!=null &&
              java.util.Arrays.equals(this.unitsInfo, other.getUnitsInfo())));
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
        if (getResult() != null) {
            _hashCode += getResult().hashCode();
        }
        if (getUnitsInfo() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUnitsInfo());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUnitsInfo(), i);
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
        new org.apache.axis.description.TypeDesc(UnitsDetailsResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "UnitsDetailsResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("result");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Result"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "UnitsDetailsResult.eResult"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("unitsInfo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "UnitsInfo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "UnitInfo"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setItemQName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "UnitInfo"));
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
