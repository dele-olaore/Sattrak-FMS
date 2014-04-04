/**
 * SATTRACKUnitEventResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.datacontract.schemas._2004._07.ZONIntegrationWCFService;

public class SATTRACKUnitEventResult  implements java.io.Serializable {
    private org.datacontract.schemas._2004._07.ZONIntegrationWCFService.EResult result;

    private org.datacontract.schemas._2004._07.ZONIntegrationWCFService.SAATRAKUnitEvent[] unitEvents;

    private java.lang.Long lastQueriedId;

    public SATTRACKUnitEventResult() {
    }

    public SATTRACKUnitEventResult(
           org.datacontract.schemas._2004._07.ZONIntegrationWCFService.EResult result,
           org.datacontract.schemas._2004._07.ZONIntegrationWCFService.SAATRAKUnitEvent[] unitEvents,
           java.lang.Long lastQueriedId) {
           this.result = result;
           this.unitEvents = unitEvents;
           this.lastQueriedId = lastQueriedId;
    }


    /**
     * Gets the result value for this SATTRACKUnitEventResult.
     * 
     * @return result
     */
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.EResult getResult() {
        return result;
    }


    /**
     * Sets the result value for this SATTRACKUnitEventResult.
     * 
     * @param result
     */
    public void setResult(org.datacontract.schemas._2004._07.ZONIntegrationWCFService.EResult result) {
        this.result = result;
    }


    /**
     * Gets the unitEvents value for this SATTRACKUnitEventResult.
     * 
     * @return unitEvents
     */
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.SAATRAKUnitEvent[] getUnitEvents() {
        return unitEvents;
    }


    /**
     * Sets the unitEvents value for this SATTRACKUnitEventResult.
     * 
     * @param unitEvents
     */
    public void setUnitEvents(org.datacontract.schemas._2004._07.ZONIntegrationWCFService.SAATRAKUnitEvent[] unitEvents) {
        this.unitEvents = unitEvents;
    }


    /**
     * Gets the lastQueriedId value for this SATTRACKUnitEventResult.
     * 
     * @return lastQueriedId
     */
    public java.lang.Long getLastQueriedId() {
        return lastQueriedId;
    }


    /**
     * Sets the lastQueriedId value for this SATTRACKUnitEventResult.
     * 
     * @param lastQueriedId
     */
    public void setLastQueriedId(java.lang.Long lastQueriedId) {
        this.lastQueriedId = lastQueriedId;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SATTRACKUnitEventResult)) return false;
        SATTRACKUnitEventResult other = (SATTRACKUnitEventResult) obj;
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
            ((this.unitEvents==null && other.getUnitEvents()==null) || 
             (this.unitEvents!=null &&
              java.util.Arrays.equals(this.unitEvents, other.getUnitEvents()))) &&
            ((this.lastQueriedId==null && other.getLastQueriedId()==null) || 
             (this.lastQueriedId!=null &&
              this.lastQueriedId.equals(other.getLastQueriedId())));
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
        if (getUnitEvents() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUnitEvents());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUnitEvents(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getLastQueriedId() != null) {
            _hashCode += getLastQueriedId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SATTRACKUnitEventResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "SATTRACKUnitEventResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("result");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "Result"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "eResult"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("unitEvents");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "UnitEvents"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "SAATRAKUnitEvent"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setItemQName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "SAATRAKUnitEvent"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastQueriedId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "lastQueriedId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
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
