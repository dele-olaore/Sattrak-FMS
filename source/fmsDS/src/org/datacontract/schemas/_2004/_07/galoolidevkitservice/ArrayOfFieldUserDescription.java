
package org.datacontract.schemas._2004._07.galoolidevkitservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfFieldUserDescription complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfFieldUserDescription">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FieldUserDescription" type="{http://schemas.datacontract.org/2004/07/galooliDevKitService}FieldUserDescription" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfFieldUserDescription", propOrder = {
    "fieldUserDescription"
})
public class ArrayOfFieldUserDescription {

    @XmlElement(name = "FieldUserDescription", nillable = true)
    protected List<FieldUserDescription> fieldUserDescription;

    /**
     * Gets the value of the fieldUserDescription property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fieldUserDescription property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFieldUserDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FieldUserDescription }
     * 
     * 
     */
    public List<FieldUserDescription> getFieldUserDescription() {
        if (fieldUserDescription == null) {
            fieldUserDescription = new ArrayList<FieldUserDescription>();
        }
        return this.fieldUserDescription;
    }

}
