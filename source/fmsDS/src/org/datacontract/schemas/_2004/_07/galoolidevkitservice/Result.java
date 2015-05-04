
package org.datacontract.schemas._2004._07.galoolidevkitservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfArrayOfstring;


/**
 * <p>Java class for Result complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Result">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DataSet" type="{http://schemas.microsoft.com/2003/10/Serialization/Arrays}ArrayOfArrayOfstring" minOccurs="0"/>
 *         &lt;element name="ResultCode" type="{http://schemas.datacontract.org/2004/07/galooliDevKitService}Result.eResultCode" minOccurs="0"/>
 *         &lt;element name="ResultDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Result", propOrder = {
    "dataSet",
    "resultCode",
    "resultDescription"
})
public class Result {

    @XmlElementRef(name = "DataSet", namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfArrayOfstring> dataSet;
    @XmlElement(name = "ResultCode")
    protected ResultEResultCode resultCode;
    @XmlElementRef(name = "ResultDescription", namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", type = JAXBElement.class, required = false)
    protected JAXBElement<String> resultDescription;

    /**
     * Gets the value of the dataSet property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfArrayOfstring }{@code >}
     *     
     */
    public JAXBElement<ArrayOfArrayOfstring> getDataSet() {
        return dataSet;
    }

    /**
     * Sets the value of the dataSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfArrayOfstring }{@code >}
     *     
     */
    public void setDataSet(JAXBElement<ArrayOfArrayOfstring> value) {
        this.dataSet = value;
    }

    /**
     * Gets the value of the resultCode property.
     * 
     * @return
     *     possible object is
     *     {@link ResultEResultCode }
     *     
     */
    public ResultEResultCode getResultCode() {
        return resultCode;
    }

    /**
     * Sets the value of the resultCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResultEResultCode }
     *     
     */
    public void setResultCode(ResultEResultCode value) {
        this.resultCode = value;
    }

    /**
     * Gets the value of the resultDescription property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getResultDescription() {
        return resultDescription;
    }

    /**
     * Sets the value of the resultDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setResultDescription(JAXBElement<String> value) {
        this.resultDescription = value;
    }

}
