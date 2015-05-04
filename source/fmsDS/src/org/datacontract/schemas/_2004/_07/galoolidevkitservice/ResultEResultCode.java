
package org.datacontract.schemas._2004._07.galoolidevkitservice;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Result.eResultCode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Result.eResultCode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="General_Error"/>
 *     &lt;enumeration value="Executed_Ok"/>
 *     &lt;enumeration value="User_Invalid_Credentials"/>
 *     &lt;enumeration value="User_Expired"/>
 *     &lt;enumeration value="User_Disabled"/>
 *     &lt;enumeration value="User_UnAuthorized"/>
 *     &lt;enumeration value="User_Invalid_Source_IP"/>
 *     &lt;enumeration value="User_No_Fleets_Assigned"/>
 *     &lt;enumeration value="Events_Unauthorized_Requested_Units_Numbers"/>
 *     &lt;enumeration value="Events_Time_Period_Exceeded_Limit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "Result.eResultCode")
@XmlEnum
public enum ResultEResultCode {

    @XmlEnumValue("General_Error")
    GENERAL_ERROR("General_Error"),
    @XmlEnumValue("Executed_Ok")
    EXECUTED_OK("Executed_Ok"),
    @XmlEnumValue("User_Invalid_Credentials")
    USER_INVALID_CREDENTIALS("User_Invalid_Credentials"),
    @XmlEnumValue("User_Expired")
    USER_EXPIRED("User_Expired"),
    @XmlEnumValue("User_Disabled")
    USER_DISABLED("User_Disabled"),
    @XmlEnumValue("User_UnAuthorized")
    USER_UN_AUTHORIZED("User_UnAuthorized"),
    @XmlEnumValue("User_Invalid_Source_IP")
    USER_INVALID_SOURCE_IP("User_Invalid_Source_IP"),
    @XmlEnumValue("User_No_Fleets_Assigned")
    USER_NO_FLEETS_ASSIGNED("User_No_Fleets_Assigned"),
    @XmlEnumValue("Events_Unauthorized_Requested_Units_Numbers")
    EVENTS_UNAUTHORIZED_REQUESTED_UNITS_NUMBERS("Events_Unauthorized_Requested_Units_Numbers"),
    @XmlEnumValue("Events_Time_Period_Exceeded_Limit")
    EVENTS_TIME_PERIOD_EXCEEDED_LIMIT("Events_Time_Period_Exceeded_Limit");
    private final String value;

    ResultEResultCode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ResultEResultCode fromValue(String v) {
        for (ResultEResultCode c: ResultEResultCode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
