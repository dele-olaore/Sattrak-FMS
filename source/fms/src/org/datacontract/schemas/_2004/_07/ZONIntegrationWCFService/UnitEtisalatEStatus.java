/**
 * UnitEtisalatEStatus.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.datacontract.schemas._2004._07.ZONIntegrationWCFService;

public class UnitEtisalatEStatus implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected UnitEtisalatEStatus(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _Off = "Off";
    public static final java.lang.String _On = "On";
    public static final java.lang.String _Moving = "Moving";
    public static final java.lang.String _Stop = "Stop";
    public static final java.lang.String _Idle = "Idle";
    public static final java.lang.String _Alert = "Alert";
    public static final java.lang.String _NotConnected = "NotConnected";
    public static final UnitEtisalatEStatus Off = new UnitEtisalatEStatus(_Off);
    public static final UnitEtisalatEStatus On = new UnitEtisalatEStatus(_On);
    public static final UnitEtisalatEStatus Moving = new UnitEtisalatEStatus(_Moving);
    public static final UnitEtisalatEStatus Stop = new UnitEtisalatEStatus(_Stop);
    public static final UnitEtisalatEStatus Idle = new UnitEtisalatEStatus(_Idle);
    public static final UnitEtisalatEStatus Alert = new UnitEtisalatEStatus(_Alert);
    public static final UnitEtisalatEStatus NotConnected = new UnitEtisalatEStatus(_NotConnected);
    public java.lang.String getValue() { return _value_;}
    public static UnitEtisalatEStatus fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        UnitEtisalatEStatus enumeration = (UnitEtisalatEStatus)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static UnitEtisalatEStatus fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UnitEtisalatEStatus.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/ZONIntegrationWCFService", "UnitEtisalat.eStatus"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
