
package org.tempuri;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.datacontract.schemas._2004._07.galoolidevkitservice.ArrayOfFieldUserDescription;
import org.datacontract.schemas._2004._07.galoolidevkitservice.Result;
import org.datacontract.schemas._2004._07.galoolidevkitservice.ResultEventsInformation;
import org.datacontract.schemas._2004._07.galoolidevkitservice.ResultFieldsDescription;
import org.datacontract.schemas._2004._07.galoolidevkitservice.ResultRealTime;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.tempuri package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _SysVerifyUserUserOrg_QNAME = new QName("http://tempuri.org/", "userOrg");
    private final static QName _SysVerifyUserPassword_QNAME = new QName("http://tempuri.org/", "password");
    private final static QName _SysVerifyUserUserHost_QNAME = new QName("http://tempuri.org/", "userHost");
    private final static QName _SysVerifyUserUserName_QNAME = new QName("http://tempuri.org/", "userName");
    private final static QName _GetRealTimeDataLastGMTUpdateTime_QNAME = new QName("http://tempuri.org/", "lastGMTUpdateTime");
    private final static QName _GetRealTimeDataRequestedPropertiesStr_QNAME = new QName("http://tempuri.org/", "requestedPropertiesStr");
    private final static QName _GetEventsInformationResponseGetEventsInformationResult_QNAME = new QName("http://tempuri.org/", "getEventsInformationResult");
    private final static QName _GetEventsInformationRequestedUnits_QNAME = new QName("http://tempuri.org/", "requestedUnits");
    private final static QName _GetEventsInformationEndTime_QNAME = new QName("http://tempuri.org/", "endTime");
    private final static QName _GetEventsInformationStartTime_QNAME = new QName("http://tempuri.org/", "startTime");
    private final static QName _GetEventsInformationRequestedEvents_QNAME = new QName("http://tempuri.org/", "requestedEvents");
    private final static QName _GetPropertiesResponseGetPropertiesResult_QNAME = new QName("http://tempuri.org/", "GetPropertiesResult");
    private final static QName _SysTestEchoStr_QNAME = new QName("http://tempuri.org/", "str");
    private final static QName _GetRealTimeDataResponseGetRealTimeDataResult_QNAME = new QName("http://tempuri.org/", "GetRealTimeDataResult");
    private final static QName _SysTestEchoResponseSysTestEchoResult_QNAME = new QName("http://tempuri.org/", "sys_TestEchoResult");
    private final static QName _SysGetVersionResponseSysGetVersionResult_QNAME = new QName("http://tempuri.org/", "sys_GetVersionResult");
    private final static QName _SysVerifyUserResponseSysVerifyUserResult_QNAME = new QName("http://tempuri.org/", "sys_VerifyUserResult");
    private final static QName _SysTestResponseSysTestResult_QNAME = new QName("http://tempuri.org/", "sys_TestResult");
    private final static QName _GetTripsResponseGetTripsResult_QNAME = new QName("http://tempuri.org/", "GetTripsResult");
    private final static QName _DummyGetFieldDescriptionResponseDummyGetFieldDescriptionResult_QNAME = new QName("http://tempuri.org/", "dummy_getFieldDescriptionResult");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.tempuri
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetRealTimeDataResponse }
     * 
     */
    public GetRealTimeDataResponse createGetRealTimeDataResponse() {
        return new GetRealTimeDataResponse();
    }

    /**
     * Create an instance of {@link GetEventsInformation }
     * 
     */
    public GetEventsInformation createGetEventsInformation() {
        return new GetEventsInformation();
    }

    /**
     * Create an instance of {@link GetRealTimeData }
     * 
     */
    public GetRealTimeData createGetRealTimeData() {
        return new GetRealTimeData();
    }

    /**
     * Create an instance of {@link GetProperties }
     * 
     */
    public GetProperties createGetProperties() {
        return new GetProperties();
    }

    /**
     * Create an instance of {@link GetTripsResponse }
     * 
     */
    public GetTripsResponse createGetTripsResponse() {
        return new GetTripsResponse();
    }

    /**
     * Create an instance of {@link SysTestEcho }
     * 
     */
    public SysTestEcho createSysTestEcho() {
        return new SysTestEcho();
    }

    /**
     * Create an instance of {@link GetTrips }
     * 
     */
    public GetTrips createGetTrips() {
        return new GetTrips();
    }

    /**
     * Create an instance of {@link SysTestEchoResponse }
     * 
     */
    public SysTestEchoResponse createSysTestEchoResponse() {
        return new SysTestEchoResponse();
    }

    /**
     * Create an instance of {@link GetPropertiesResponse }
     * 
     */
    public GetPropertiesResponse createGetPropertiesResponse() {
        return new GetPropertiesResponse();
    }

    /**
     * Create an instance of {@link SysVerifyUser }
     * 
     */
    public SysVerifyUser createSysVerifyUser() {
        return new SysVerifyUser();
    }

    /**
     * Create an instance of {@link SysGetVersionResponse }
     * 
     */
    public SysGetVersionResponse createSysGetVersionResponse() {
        return new SysGetVersionResponse();
    }

    /**
     * Create an instance of {@link GetEventsInformationResponse }
     * 
     */
    public GetEventsInformationResponse createGetEventsInformationResponse() {
        return new GetEventsInformationResponse();
    }

    /**
     * Create an instance of {@link SysTestResponse }
     * 
     */
    public SysTestResponse createSysTestResponse() {
        return new SysTestResponse();
    }

    /**
     * Create an instance of {@link DummyGetFieldDescriptionResponse }
     * 
     */
    public DummyGetFieldDescriptionResponse createDummyGetFieldDescriptionResponse() {
        return new DummyGetFieldDescriptionResponse();
    }

    /**
     * Create an instance of {@link SysVerifyUserResponse }
     * 
     */
    public SysVerifyUserResponse createSysVerifyUserResponse() {
        return new SysVerifyUserResponse();
    }

    /**
     * Create an instance of {@link SysGetVersion }
     * 
     */
    public SysGetVersion createSysGetVersion() {
        return new SysGetVersion();
    }

    /**
     * Create an instance of {@link SysTest }
     * 
     */
    public SysTest createSysTest() {
        return new SysTest();
    }

    /**
     * Create an instance of {@link DummyGetFieldDescription }
     * 
     */
    public DummyGetFieldDescription createDummyGetFieldDescription() {
        return new DummyGetFieldDescription();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userOrg", scope = SysVerifyUser.class)
    public JAXBElement<String> createSysVerifyUserUserOrg(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserOrg_QNAME, String.class, SysVerifyUser.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "password", scope = SysVerifyUser.class)
    public JAXBElement<String> createSysVerifyUserPassword(String value) {
        return new JAXBElement<String>(_SysVerifyUserPassword_QNAME, String.class, SysVerifyUser.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userHost", scope = SysVerifyUser.class)
    public JAXBElement<String> createSysVerifyUserUserHost(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserHost_QNAME, String.class, SysVerifyUser.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userName", scope = SysVerifyUser.class)
    public JAXBElement<String> createSysVerifyUserUserName(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserName_QNAME, String.class, SysVerifyUser.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userOrg", scope = GetRealTimeData.class)
    public JAXBElement<String> createGetRealTimeDataUserOrg(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserOrg_QNAME, String.class, GetRealTimeData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "lastGMTUpdateTime", scope = GetRealTimeData.class)
    public JAXBElement<String> createGetRealTimeDataLastGMTUpdateTime(String value) {
        return new JAXBElement<String>(_GetRealTimeDataLastGMTUpdateTime_QNAME, String.class, GetRealTimeData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "password", scope = GetRealTimeData.class)
    public JAXBElement<String> createGetRealTimeDataPassword(String value) {
        return new JAXBElement<String>(_SysVerifyUserPassword_QNAME, String.class, GetRealTimeData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "requestedPropertiesStr", scope = GetRealTimeData.class)
    public JAXBElement<String> createGetRealTimeDataRequestedPropertiesStr(String value) {
        return new JAXBElement<String>(_GetRealTimeDataRequestedPropertiesStr_QNAME, String.class, GetRealTimeData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userHost", scope = GetRealTimeData.class)
    public JAXBElement<String> createGetRealTimeDataUserHost(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserHost_QNAME, String.class, GetRealTimeData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userName", scope = GetRealTimeData.class)
    public JAXBElement<String> createGetRealTimeDataUserName(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserName_QNAME, String.class, GetRealTimeData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResultEventsInformation }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "getEventsInformationResult", scope = GetEventsInformationResponse.class)
    public JAXBElement<ResultEventsInformation> createGetEventsInformationResponseGetEventsInformationResult(ResultEventsInformation value) {
        return new JAXBElement<ResultEventsInformation>(_GetEventsInformationResponseGetEventsInformationResult_QNAME, ResultEventsInformation.class, GetEventsInformationResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userOrg", scope = GetEventsInformation.class)
    public JAXBElement<String> createGetEventsInformationUserOrg(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserOrg_QNAME, String.class, GetEventsInformation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "password", scope = GetEventsInformation.class)
    public JAXBElement<String> createGetEventsInformationPassword(String value) {
        return new JAXBElement<String>(_SysVerifyUserPassword_QNAME, String.class, GetEventsInformation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "requestedUnits", scope = GetEventsInformation.class)
    public JAXBElement<String> createGetEventsInformationRequestedUnits(String value) {
        return new JAXBElement<String>(_GetEventsInformationRequestedUnits_QNAME, String.class, GetEventsInformation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "requestedPropertiesStr", scope = GetEventsInformation.class)
    public JAXBElement<String> createGetEventsInformationRequestedPropertiesStr(String value) {
        return new JAXBElement<String>(_GetRealTimeDataRequestedPropertiesStr_QNAME, String.class, GetEventsInformation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userHost", scope = GetEventsInformation.class)
    public JAXBElement<String> createGetEventsInformationUserHost(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserHost_QNAME, String.class, GetEventsInformation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "endTime", scope = GetEventsInformation.class)
    public JAXBElement<String> createGetEventsInformationEndTime(String value) {
        return new JAXBElement<String>(_GetEventsInformationEndTime_QNAME, String.class, GetEventsInformation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "startTime", scope = GetEventsInformation.class)
    public JAXBElement<String> createGetEventsInformationStartTime(String value) {
        return new JAXBElement<String>(_GetEventsInformationStartTime_QNAME, String.class, GetEventsInformation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "requestedEvents", scope = GetEventsInformation.class)
    public JAXBElement<String> createGetEventsInformationRequestedEvents(String value) {
        return new JAXBElement<String>(_GetEventsInformationRequestedEvents_QNAME, String.class, GetEventsInformation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userName", scope = GetEventsInformation.class)
    public JAXBElement<String> createGetEventsInformationUserName(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserName_QNAME, String.class, GetEventsInformation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResultFieldsDescription }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "GetPropertiesResult", scope = GetPropertiesResponse.class)
    public JAXBElement<ResultFieldsDescription> createGetPropertiesResponseGetPropertiesResult(ResultFieldsDescription value) {
        return new JAXBElement<ResultFieldsDescription>(_GetPropertiesResponseGetPropertiesResult_QNAME, ResultFieldsDescription.class, GetPropertiesResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userOrg", scope = SysTestEcho.class)
    public JAXBElement<String> createSysTestEchoUserOrg(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserOrg_QNAME, String.class, SysTestEcho.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "password", scope = SysTestEcho.class)
    public JAXBElement<String> createSysTestEchoPassword(String value) {
        return new JAXBElement<String>(_SysVerifyUserPassword_QNAME, String.class, SysTestEcho.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "str", scope = SysTestEcho.class)
    public JAXBElement<String> createSysTestEchoStr(String value) {
        return new JAXBElement<String>(_SysTestEchoStr_QNAME, String.class, SysTestEcho.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userHost", scope = SysTestEcho.class)
    public JAXBElement<String> createSysTestEchoUserHost(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserHost_QNAME, String.class, SysTestEcho.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userName", scope = SysTestEcho.class)
    public JAXBElement<String> createSysTestEchoUserName(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserName_QNAME, String.class, SysTestEcho.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userOrg", scope = SysTest.class)
    public JAXBElement<String> createSysTestUserOrg(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserOrg_QNAME, String.class, SysTest.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "password", scope = SysTest.class)
    public JAXBElement<String> createSysTestPassword(String value) {
        return new JAXBElement<String>(_SysVerifyUserPassword_QNAME, String.class, SysTest.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userHost", scope = SysTest.class)
    public JAXBElement<String> createSysTestUserHost(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserHost_QNAME, String.class, SysTest.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userName", scope = SysTest.class)
    public JAXBElement<String> createSysTestUserName(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserName_QNAME, String.class, SysTest.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResultRealTime }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "GetRealTimeDataResult", scope = GetRealTimeDataResponse.class)
    public JAXBElement<ResultRealTime> createGetRealTimeDataResponseGetRealTimeDataResult(ResultRealTime value) {
        return new JAXBElement<ResultRealTime>(_GetRealTimeDataResponseGetRealTimeDataResult_QNAME, ResultRealTime.class, GetRealTimeDataResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userOrg", scope = SysGetVersion.class)
    public JAXBElement<String> createSysGetVersionUserOrg(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserOrg_QNAME, String.class, SysGetVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "password", scope = SysGetVersion.class)
    public JAXBElement<String> createSysGetVersionPassword(String value) {
        return new JAXBElement<String>(_SysVerifyUserPassword_QNAME, String.class, SysGetVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userHost", scope = SysGetVersion.class)
    public JAXBElement<String> createSysGetVersionUserHost(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserHost_QNAME, String.class, SysGetVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userName", scope = SysGetVersion.class)
    public JAXBElement<String> createSysGetVersionUserName(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserName_QNAME, String.class, SysGetVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userOrg", scope = GetProperties.class)
    public JAXBElement<String> createGetPropertiesUserOrg(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserOrg_QNAME, String.class, GetProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "password", scope = GetProperties.class)
    public JAXBElement<String> createGetPropertiesPassword(String value) {
        return new JAXBElement<String>(_SysVerifyUserPassword_QNAME, String.class, GetProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userHost", scope = GetProperties.class)
    public JAXBElement<String> createGetPropertiesUserHost(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserHost_QNAME, String.class, GetProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userName", scope = GetProperties.class)
    public JAXBElement<String> createGetPropertiesUserName(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserName_QNAME, String.class, GetProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "sys_TestEchoResult", scope = SysTestEchoResponse.class)
    public JAXBElement<String> createSysTestEchoResponseSysTestEchoResult(String value) {
        return new JAXBElement<String>(_SysTestEchoResponseSysTestEchoResult_QNAME, String.class, SysTestEchoResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "sys_GetVersionResult", scope = SysGetVersionResponse.class)
    public JAXBElement<String> createSysGetVersionResponseSysGetVersionResult(String value) {
        return new JAXBElement<String>(_SysGetVersionResponseSysGetVersionResult_QNAME, String.class, SysGetVersionResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userOrg", scope = GetTrips.class)
    public JAXBElement<String> createGetTripsUserOrg(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserOrg_QNAME, String.class, GetTrips.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "password", scope = GetTrips.class)
    public JAXBElement<String> createGetTripsPassword(String value) {
        return new JAXBElement<String>(_SysVerifyUserPassword_QNAME, String.class, GetTrips.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "requestedPropertiesStr", scope = GetTrips.class)
    public JAXBElement<String> createGetTripsRequestedPropertiesStr(String value) {
        return new JAXBElement<String>(_GetRealTimeDataRequestedPropertiesStr_QNAME, String.class, GetTrips.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userHost", scope = GetTrips.class)
    public JAXBElement<String> createGetTripsUserHost(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserHost_QNAME, String.class, GetTrips.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "endTime", scope = GetTrips.class)
    public JAXBElement<String> createGetTripsEndTime(String value) {
        return new JAXBElement<String>(_GetEventsInformationEndTime_QNAME, String.class, GetTrips.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "startTime", scope = GetTrips.class)
    public JAXBElement<String> createGetTripsStartTime(String value) {
        return new JAXBElement<String>(_GetEventsInformationStartTime_QNAME, String.class, GetTrips.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "userName", scope = GetTrips.class)
    public JAXBElement<String> createGetTripsUserName(String value) {
        return new JAXBElement<String>(_SysVerifyUserUserName_QNAME, String.class, GetTrips.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "sys_VerifyUserResult", scope = SysVerifyUserResponse.class)
    public JAXBElement<String> createSysVerifyUserResponseSysVerifyUserResult(String value) {
        return new JAXBElement<String>(_SysVerifyUserResponseSysVerifyUserResult_QNAME, String.class, SysVerifyUserResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "sys_TestResult", scope = SysTestResponse.class)
    public JAXBElement<String> createSysTestResponseSysTestResult(String value) {
        return new JAXBElement<String>(_SysTestResponseSysTestResult_QNAME, String.class, SysTestResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Result }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "GetTripsResult", scope = GetTripsResponse.class)
    public JAXBElement<Result> createGetTripsResponseGetTripsResult(Result value) {
        return new JAXBElement<Result>(_GetTripsResponseGetTripsResult_QNAME, Result.class, GetTripsResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfFieldUserDescription }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "dummy_getFieldDescriptionResult", scope = DummyGetFieldDescriptionResponse.class)
    public JAXBElement<ArrayOfFieldUserDescription> createDummyGetFieldDescriptionResponseDummyGetFieldDescriptionResult(ArrayOfFieldUserDescription value) {
        return new JAXBElement<ArrayOfFieldUserDescription>(_DummyGetFieldDescriptionResponseDummyGetFieldDescriptionResult_QNAME, ArrayOfFieldUserDescription.class, DummyGetFieldDescriptionResponse.class, value);
    }

}
