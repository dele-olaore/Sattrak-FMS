
package org.datacontract.schemas._2004._07.galoolidevkitservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfArrayOfstring;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfstring;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.datacontract.schemas._2004._07.galoolidevkitservice package. 
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

    private final static QName _ArrayOfFieldUserDescription_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "ArrayOfFieldUserDescription");
    private final static QName _ResultEResultCode_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "Result.eResultCode");
    private final static QName _ResultFieldsDescription_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "ResultFieldsDescription");
    private final static QName _ResultRealTime_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "ResultRealTime");
    private final static QName _FieldUserDescription_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "FieldUserDescription");
    private final static QName _ResultEventsInformation_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "ResultEventsInformation");
    private final static QName _Result_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "Result");
    private final static QName _ResultFieldsDescriptionCommonResult_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "CommonResult");
    private final static QName _ResultFieldsDescriptionFieldsDescription_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "FieldsDescription");
    private final static QName _ResultResultDescription_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "ResultDescription");
    private final static QName _ResultDataSet_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "DataSet");
    private final static QName _FieldUserDescriptionRelatedMethods_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "RelatedMethods");
    private final static QName _FieldUserDescriptionFieldName_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "FieldName");
    private final static QName _FieldUserDescriptionTableDescription_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "TableDescription");
    private final static QName _FieldUserDescriptionDescription_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "Description");
    private final static QName _ResultRealTimeMaxGmtUpdateTime_QNAME = new QName("http://schemas.datacontract.org/2004/07/galooliDevKitService", "MaxGmtUpdateTime");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.datacontract.schemas._2004._07.galoolidevkitservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ResultRealTime }
     * 
     */
    public ResultRealTime createResultRealTime() {
        return new ResultRealTime();
    }

    /**
     * Create an instance of {@link Result }
     * 
     */
    public Result createResult() {
        return new Result();
    }

    /**
     * Create an instance of {@link ResultFieldsDescription }
     * 
     */
    public ResultFieldsDescription createResultFieldsDescription() {
        return new ResultFieldsDescription();
    }

    /**
     * Create an instance of {@link ResultEventsInformation }
     * 
     */
    public ResultEventsInformation createResultEventsInformation() {
        return new ResultEventsInformation();
    }

    /**
     * Create an instance of {@link ArrayOfFieldUserDescription }
     * 
     */
    public ArrayOfFieldUserDescription createArrayOfFieldUserDescription() {
        return new ArrayOfFieldUserDescription();
    }

    /**
     * Create an instance of {@link FieldUserDescription }
     * 
     */
    public FieldUserDescription createFieldUserDescription() {
        return new FieldUserDescription();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfFieldUserDescription }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "ArrayOfFieldUserDescription")
    public JAXBElement<ArrayOfFieldUserDescription> createArrayOfFieldUserDescription(ArrayOfFieldUserDescription value) {
        return new JAXBElement<ArrayOfFieldUserDescription>(_ArrayOfFieldUserDescription_QNAME, ArrayOfFieldUserDescription.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResultEResultCode }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "Result.eResultCode")
    public JAXBElement<ResultEResultCode> createResultEResultCode(ResultEResultCode value) {
        return new JAXBElement<ResultEResultCode>(_ResultEResultCode_QNAME, ResultEResultCode.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResultFieldsDescription }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "ResultFieldsDescription")
    public JAXBElement<ResultFieldsDescription> createResultFieldsDescription(ResultFieldsDescription value) {
        return new JAXBElement<ResultFieldsDescription>(_ResultFieldsDescription_QNAME, ResultFieldsDescription.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResultRealTime }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "ResultRealTime")
    public JAXBElement<ResultRealTime> createResultRealTime(ResultRealTime value) {
        return new JAXBElement<ResultRealTime>(_ResultRealTime_QNAME, ResultRealTime.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FieldUserDescription }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "FieldUserDescription")
    public JAXBElement<FieldUserDescription> createFieldUserDescription(FieldUserDescription value) {
        return new JAXBElement<FieldUserDescription>(_FieldUserDescription_QNAME, FieldUserDescription.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResultEventsInformation }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "ResultEventsInformation")
    public JAXBElement<ResultEventsInformation> createResultEventsInformation(ResultEventsInformation value) {
        return new JAXBElement<ResultEventsInformation>(_ResultEventsInformation_QNAME, ResultEventsInformation.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Result }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "Result")
    public JAXBElement<Result> createResult(Result value) {
        return new JAXBElement<Result>(_Result_QNAME, Result.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Result }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "CommonResult", scope = ResultFieldsDescription.class)
    public JAXBElement<Result> createResultFieldsDescriptionCommonResult(Result value) {
        return new JAXBElement<Result>(_ResultFieldsDescriptionCommonResult_QNAME, Result.class, ResultFieldsDescription.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfFieldUserDescription }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "FieldsDescription", scope = ResultFieldsDescription.class)
    public JAXBElement<ArrayOfFieldUserDescription> createResultFieldsDescriptionFieldsDescription(ArrayOfFieldUserDescription value) {
        return new JAXBElement<ArrayOfFieldUserDescription>(_ResultFieldsDescriptionFieldsDescription_QNAME, ArrayOfFieldUserDescription.class, ResultFieldsDescription.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Result }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "CommonResult", scope = ResultEventsInformation.class)
    public JAXBElement<Result> createResultEventsInformationCommonResult(Result value) {
        return new JAXBElement<Result>(_ResultFieldsDescriptionCommonResult_QNAME, Result.class, ResultEventsInformation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "ResultDescription", scope = Result.class)
    public JAXBElement<String> createResultResultDescription(String value) {
        return new JAXBElement<String>(_ResultResultDescription_QNAME, String.class, Result.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfArrayOfstring }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "DataSet", scope = Result.class)
    public JAXBElement<ArrayOfArrayOfstring> createResultDataSet(ArrayOfArrayOfstring value) {
        return new JAXBElement<ArrayOfArrayOfstring>(_ResultDataSet_QNAME, ArrayOfArrayOfstring.class, Result.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfstring }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "RelatedMethods", scope = FieldUserDescription.class)
    public JAXBElement<ArrayOfstring> createFieldUserDescriptionRelatedMethods(ArrayOfstring value) {
        return new JAXBElement<ArrayOfstring>(_FieldUserDescriptionRelatedMethods_QNAME, ArrayOfstring.class, FieldUserDescription.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "FieldName", scope = FieldUserDescription.class)
    public JAXBElement<String> createFieldUserDescriptionFieldName(String value) {
        return new JAXBElement<String>(_FieldUserDescriptionFieldName_QNAME, String.class, FieldUserDescription.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "TableDescription", scope = FieldUserDescription.class)
    public JAXBElement<String> createFieldUserDescriptionTableDescription(String value) {
        return new JAXBElement<String>(_FieldUserDescriptionTableDescription_QNAME, String.class, FieldUserDescription.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "Description", scope = FieldUserDescription.class)
    public JAXBElement<String> createFieldUserDescriptionDescription(String value) {
        return new JAXBElement<String>(_FieldUserDescriptionDescription_QNAME, String.class, FieldUserDescription.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Result }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "CommonResult", scope = ResultRealTime.class)
    public JAXBElement<Result> createResultRealTimeCommonResult(Result value) {
        return new JAXBElement<Result>(_ResultFieldsDescriptionCommonResult_QNAME, Result.class, ResultRealTime.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", name = "MaxGmtUpdateTime", scope = ResultRealTime.class)
    public JAXBElement<String> createResultRealTimeMaxGmtUpdateTime(String value) {
        return new JAXBElement<String>(_ResultRealTimeMaxGmtUpdateTime_QNAME, String.class, ResultRealTime.class, value);
    }

}
