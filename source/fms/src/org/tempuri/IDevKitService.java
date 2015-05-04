
package org.tempuri;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import org.datacontract.schemas._2004._07.galoolidevkitservice.ArrayOfFieldUserDescription;
import org.datacontract.schemas._2004._07.galoolidevkitservice.Result;
import org.datacontract.schemas._2004._07.galoolidevkitservice.ResultEventsInformation;
import org.datacontract.schemas._2004._07.galoolidevkitservice.ResultFieldsDescription;
import org.datacontract.schemas._2004._07.galoolidevkitservice.ResultRealTime;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.6-1b01 
 * Generated source version: 2.2
 * 
 */
@WebService(name = "IDevKitService", targetNamespace = "http://tempuri.org/")
@XmlSeeAlso({
    com.microsoft.schemas._2003._10.serialization.ObjectFactory.class,
    com.microsoft.schemas._2003._10.serialization.arrays.ObjectFactory.class,
    org.datacontract.schemas._2004._07.galoolidevkitservice.ObjectFactory.class,
    org.tempuri.ObjectFactory.class
})
public interface IDevKitService {


    /**
     * 
     * @param password
     * @param userOrg
     * @param userHost
     * @param userName
     * @return
     *     returns java.lang.String
     */
    @WebMethod(operationName = "sys_Test", action = "http://tempuri.org/IDevKitService/sys_Test")
    @WebResult(name = "sys_TestResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "sys_Test", targetNamespace = "http://tempuri.org/", className = "org.tempuri.SysTest")
    @ResponseWrapper(localName = "sys_TestResponse", targetNamespace = "http://tempuri.org/", className = "org.tempuri.SysTestResponse")
    public String sysTest(
        @WebParam(name = "userName", targetNamespace = "http://tempuri.org/")
        String userName,
        @WebParam(name = "userHost", targetNamespace = "http://tempuri.org/")
        String userHost,
        @WebParam(name = "userOrg", targetNamespace = "http://tempuri.org/")
        String userOrg,
        @WebParam(name = "password", targetNamespace = "http://tempuri.org/")
        String password);

    /**
     * 
     * @param str
     * @param password
     * @param userOrg
     * @param userHost
     * @param userName
     * @return
     *     returns java.lang.String
     */
    @WebMethod(operationName = "sys_TestEcho", action = "http://tempuri.org/IDevKitService/sys_TestEcho")
    @WebResult(name = "sys_TestEchoResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "sys_TestEcho", targetNamespace = "http://tempuri.org/", className = "org.tempuri.SysTestEcho")
    @ResponseWrapper(localName = "sys_TestEchoResponse", targetNamespace = "http://tempuri.org/", className = "org.tempuri.SysTestEchoResponse")
    public String sysTestEcho(
        @WebParam(name = "userName", targetNamespace = "http://tempuri.org/")
        String userName,
        @WebParam(name = "userHost", targetNamespace = "http://tempuri.org/")
        String userHost,
        @WebParam(name = "userOrg", targetNamespace = "http://tempuri.org/")
        String userOrg,
        @WebParam(name = "password", targetNamespace = "http://tempuri.org/")
        String password,
        @WebParam(name = "str", targetNamespace = "http://tempuri.org/")
        String str);

    /**
     * 
     * @param password
     * @param userOrg
     * @param userHost
     * @param userName
     * @return
     *     returns java.lang.String
     */
    @WebMethod(operationName = "sys_GetVersion", action = "http://tempuri.org/IDevKitService/sys_GetVersion")
    @WebResult(name = "sys_GetVersionResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "sys_GetVersion", targetNamespace = "http://tempuri.org/", className = "org.tempuri.SysGetVersion")
    @ResponseWrapper(localName = "sys_GetVersionResponse", targetNamespace = "http://tempuri.org/", className = "org.tempuri.SysGetVersionResponse")
    public String sysGetVersion(
        @WebParam(name = "userName", targetNamespace = "http://tempuri.org/")
        String userName,
        @WebParam(name = "userHost", targetNamespace = "http://tempuri.org/")
        String userHost,
        @WebParam(name = "userOrg", targetNamespace = "http://tempuri.org/")
        String userOrg,
        @WebParam(name = "password", targetNamespace = "http://tempuri.org/")
        String password);

    /**
     * 
     * @param password
     * @param userOrg
     * @param userHost
     * @param userName
     * @return
     *     returns java.lang.String
     */
    @WebMethod(operationName = "sys_VerifyUser", action = "http://tempuri.org/IDevKitService/sys_VerifyUser")
    @WebResult(name = "sys_VerifyUserResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "sys_VerifyUser", targetNamespace = "http://tempuri.org/", className = "org.tempuri.SysVerifyUser")
    @ResponseWrapper(localName = "sys_VerifyUserResponse", targetNamespace = "http://tempuri.org/", className = "org.tempuri.SysVerifyUserResponse")
    public String sysVerifyUser(
        @WebParam(name = "userName", targetNamespace = "http://tempuri.org/")
        String userName,
        @WebParam(name = "userHost", targetNamespace = "http://tempuri.org/")
        String userHost,
        @WebParam(name = "userOrg", targetNamespace = "http://tempuri.org/")
        String userOrg,
        @WebParam(name = "password", targetNamespace = "http://tempuri.org/")
        String password);

    /**
     * 
     * @param requestedPropertiesStr
     * @param password
     * @param userOrg
     * @param lastGMTUpdateTime
     * @param userHost
     * @param userName
     * @return
     *     returns org.datacontract.schemas._2004._07.galoolidevkitservice.ResultRealTime
     */
    @WebMethod(operationName = "GetRealTimeData", action = "http://tempuri.org/IDevKitService/GetRealTimeData")
    @WebResult(name = "GetRealTimeDataResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "GetRealTimeData", targetNamespace = "http://tempuri.org/", className = "org.tempuri.GetRealTimeData")
    @ResponseWrapper(localName = "GetRealTimeDataResponse", targetNamespace = "http://tempuri.org/", className = "org.tempuri.GetRealTimeDataResponse")
    public ResultRealTime getRealTimeData(
        @WebParam(name = "userName", targetNamespace = "http://tempuri.org/")
        String userName,
        @WebParam(name = "userHost", targetNamespace = "http://tempuri.org/")
        String userHost,
        @WebParam(name = "userOrg", targetNamespace = "http://tempuri.org/")
        String userOrg,
        @WebParam(name = "password", targetNamespace = "http://tempuri.org/")
        String password,
        @WebParam(name = "requestedPropertiesStr", targetNamespace = "http://tempuri.org/")
        String requestedPropertiesStr,
        @WebParam(name = "lastGMTUpdateTime", targetNamespace = "http://tempuri.org/")
        String lastGMTUpdateTime);

    /**
     * 
     * @param requestedPropertiesStr
     * @param requestedUnits
     * @param password
     * @param userOrg
     * @param startTime
     * @param userHost
     * @param endTime
     * @param userName
     * @param requestedEvents
     * @return
     *     returns org.datacontract.schemas._2004._07.galoolidevkitservice.ResultEventsInformation
     */
    @WebMethod(action = "http://tempuri.org/IDevKitService/getEventsInformation")
    @WebResult(name = "getEventsInformationResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "getEventsInformation", targetNamespace = "http://tempuri.org/", className = "org.tempuri.GetEventsInformation")
    @ResponseWrapper(localName = "getEventsInformationResponse", targetNamespace = "http://tempuri.org/", className = "org.tempuri.GetEventsInformationResponse")
    public ResultEventsInformation getEventsInformation(
        @WebParam(name = "userName", targetNamespace = "http://tempuri.org/")
        String userName,
        @WebParam(name = "userHost", targetNamespace = "http://tempuri.org/")
        String userHost,
        @WebParam(name = "userOrg", targetNamespace = "http://tempuri.org/")
        String userOrg,
        @WebParam(name = "password", targetNamespace = "http://tempuri.org/")
        String password,
        @WebParam(name = "requestedUnits", targetNamespace = "http://tempuri.org/")
        String requestedUnits,
        @WebParam(name = "requestedEvents", targetNamespace = "http://tempuri.org/")
        String requestedEvents,
        @WebParam(name = "startTime", targetNamespace = "http://tempuri.org/")
        String startTime,
        @WebParam(name = "endTime", targetNamespace = "http://tempuri.org/")
        String endTime,
        @WebParam(name = "requestedPropertiesStr", targetNamespace = "http://tempuri.org/")
        String requestedPropertiesStr);

    /**
     * 
     * @param password
     * @param userOrg
     * @param userHost
     * @param userName
     * @return
     *     returns org.datacontract.schemas._2004._07.galoolidevkitservice.ResultFieldsDescription
     */
    @WebMethod(operationName = "GetProperties", action = "http://tempuri.org/IDevKitService/GetProperties")
    @WebResult(name = "GetPropertiesResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "GetProperties", targetNamespace = "http://tempuri.org/", className = "org.tempuri.GetProperties")
    @ResponseWrapper(localName = "GetPropertiesResponse", targetNamespace = "http://tempuri.org/", className = "org.tempuri.GetPropertiesResponse")
    public ResultFieldsDescription getProperties(
        @WebParam(name = "userName", targetNamespace = "http://tempuri.org/")
        String userName,
        @WebParam(name = "userHost", targetNamespace = "http://tempuri.org/")
        String userHost,
        @WebParam(name = "userOrg", targetNamespace = "http://tempuri.org/")
        String userOrg,
        @WebParam(name = "password", targetNamespace = "http://tempuri.org/")
        String password);

    /**
     * 
     * @param requestedPropertiesStr
     * @param password
     * @param userOrg
     * @param unitId
     * @param startTime
     * @param userHost
     * @param endTime
     * @param userName
     * @return
     *     returns org.datacontract.schemas._2004._07.galoolidevkitservice.Result
     */
    @WebMethod(operationName = "GetTrips", action = "http://tempuri.org/IDevKitService/GetTrips")
    @WebResult(name = "GetTripsResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "GetTrips", targetNamespace = "http://tempuri.org/", className = "org.tempuri.GetTrips")
    @ResponseWrapper(localName = "GetTripsResponse", targetNamespace = "http://tempuri.org/", className = "org.tempuri.GetTripsResponse")
    public Result getTrips(
        @WebParam(name = "userName", targetNamespace = "http://tempuri.org/")
        String userName,
        @WebParam(name = "userHost", targetNamespace = "http://tempuri.org/")
        String userHost,
        @WebParam(name = "userOrg", targetNamespace = "http://tempuri.org/")
        String userOrg,
        @WebParam(name = "password", targetNamespace = "http://tempuri.org/")
        String password,
        @WebParam(name = "requestedPropertiesStr", targetNamespace = "http://tempuri.org/")
        String requestedPropertiesStr,
        @WebParam(name = "unitId", targetNamespace = "http://tempuri.org/")
        Integer unitId,
        @WebParam(name = "startTime", targetNamespace = "http://tempuri.org/")
        String startTime,
        @WebParam(name = "endTime", targetNamespace = "http://tempuri.org/")
        String endTime);

    /**
     * 
     * @return
     *     returns org.datacontract.schemas._2004._07.galoolidevkitservice.ArrayOfFieldUserDescription
     */
    @WebMethod(operationName = "dummy_getFieldDescription", action = "http://tempuri.org/IDevKitService/dummy_getFieldDescription")
    @WebResult(name = "dummy_getFieldDescriptionResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "dummy_getFieldDescription", targetNamespace = "http://tempuri.org/", className = "org.tempuri.DummyGetFieldDescription")
    @ResponseWrapper(localName = "dummy_getFieldDescriptionResponse", targetNamespace = "http://tempuri.org/", className = "org.tempuri.DummyGetFieldDescriptionResponse")
    public ArrayOfFieldUserDescription dummyGetFieldDescription();

}