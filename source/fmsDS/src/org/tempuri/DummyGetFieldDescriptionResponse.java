
package org.tempuri;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.datacontract.schemas._2004._07.galoolidevkitservice.ArrayOfFieldUserDescription;


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
 *         &lt;element name="dummy_getFieldDescriptionResult" type="{http://schemas.datacontract.org/2004/07/galooliDevKitService}ArrayOfFieldUserDescription" minOccurs="0"/>
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
    "dummyGetFieldDescriptionResult"
})
@XmlRootElement(name = "dummy_getFieldDescriptionResponse")
public class DummyGetFieldDescriptionResponse {

    @XmlElementRef(name = "dummy_getFieldDescriptionResult", namespace = "http://tempuri.org/", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfFieldUserDescription> dummyGetFieldDescriptionResult;

    /**
     * Gets the value of the dummyGetFieldDescriptionResult property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfFieldUserDescription }{@code >}
     *     
     */
    public JAXBElement<ArrayOfFieldUserDescription> getDummyGetFieldDescriptionResult() {
        return dummyGetFieldDescriptionResult;
    }

    /**
     * Sets the value of the dummyGetFieldDescriptionResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfFieldUserDescription }{@code >}
     *     
     */
    public void setDummyGetFieldDescriptionResult(JAXBElement<ArrayOfFieldUserDescription> value) {
        this.dummyGetFieldDescriptionResult = value;
    }

}
