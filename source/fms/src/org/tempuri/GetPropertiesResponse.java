
package org.tempuri;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.datacontract.schemas._2004._07.galoolidevkitservice.ResultFieldsDescription;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GetPropertiesResult" type="{http://schemas.datacontract.org/2004/07/galooliDevKitService}ResultFieldsDescription" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getPropertiesResult"
})
@XmlRootElement(name = "GetPropertiesResponse")
public class GetPropertiesResponse {

    @XmlElementRef(name = "GetPropertiesResult", namespace = "http://tempuri.org/", type = JAXBElement.class, required = false)
    protected JAXBElement<ResultFieldsDescription> getPropertiesResult;

    /**
     * Gets the value of the getPropertiesResult property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ResultFieldsDescription }{@code >}
     *     
     */
    public JAXBElement<ResultFieldsDescription> getGetPropertiesResult() {
        return getPropertiesResult;
    }

    /**
     * Sets the value of the getPropertiesResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ResultFieldsDescription }{@code >}
     *     
     */
    public void setGetPropertiesResult(JAXBElement<ResultFieldsDescription> value) {
        this.getPropertiesResult = value;
    }

}
