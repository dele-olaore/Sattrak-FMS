
package org.datacontract.schemas._2004._07.galoolidevkitservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ResultFieldsDescription complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResultFieldsDescription">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CommonResult" type="{http://schemas.datacontract.org/2004/07/galooliDevKitService}Result" minOccurs="0"/>
 *         &lt;element name="FieldsDescription" type="{http://schemas.datacontract.org/2004/07/galooliDevKitService}ArrayOfFieldUserDescription" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultFieldsDescription", propOrder = {
    "commonResult",
    "fieldsDescription"
})
public class ResultFieldsDescription {

    @XmlElementRef(name = "CommonResult", namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", type = JAXBElement.class, required = false)
    protected JAXBElement<Result> commonResult;
    @XmlElementRef(name = "FieldsDescription", namespace = "http://schemas.datacontract.org/2004/07/galooliDevKitService", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfFieldUserDescription> fieldsDescription;

    /**
     * Gets the value of the commonResult property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Result }{@code >}
     *     
     */
    public JAXBElement<Result> getCommonResult() {
        return commonResult;
    }

    /**
     * Sets the value of the commonResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Result }{@code >}
     *     
     */
    public void setCommonResult(JAXBElement<Result> value) {
        this.commonResult = value;
    }

    /**
     * Gets the value of the fieldsDescription property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfFieldUserDescription }{@code >}
     *     
     */
    public JAXBElement<ArrayOfFieldUserDescription> getFieldsDescription() {
        return fieldsDescription;
    }

    /**
     * Sets the value of the fieldsDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfFieldUserDescription }{@code >}
     *     
     */
    public void setFieldsDescription(JAXBElement<ArrayOfFieldUserDescription> value) {
        this.fieldsDescription = value;
    }

}
