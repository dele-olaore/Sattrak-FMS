package com.dexter.fms.model.app;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.ref.Vendor;

@Entity
public class Inspection implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue
	private Long id;
	@Column(unique=true)
	private String inspectionNumber;
	@ManyToOne
	private Vehicle vehicle;
	private String summaryDetailsOfInspection;
	private String vendorRemark;
	@ManyToOne
	private Vendor vendor;
	private byte[] vendorDocument;
	@ManyToOne
	private Partner partner;
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	@ManyToOne
	private PartnerUser createdBy;
	
	public Inspection()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getInspectionNumber() {
		return inspectionNumber;
	}

	public void setInspectionNumber(String inspectionNumber) {
		this.inspectionNumber = inspectionNumber;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public String getSummaryDetailsOfInspection() {
		return summaryDetailsOfInspection;
	}

	public void setSummaryDetailsOfInspection(String summaryDetailsOfInspection) {
		this.summaryDetailsOfInspection = summaryDetailsOfInspection;
	}

	public String getVendorRemark() {
		return vendorRemark;
	}

	public void setVendorRemark(String vendorRemark) {
		this.vendorRemark = vendorRemark;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public byte[] getVendorDocument() {
		return vendorDocument;
	}

	public void setVendorDocument(byte[] vendorDocument) {
		this.vendorDocument = vendorDocument;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public Date getCrt_dt() {
		return crt_dt;
	}

	public void setCrt_dt(Date crt_dt) {
		this.crt_dt = crt_dt;
	}

	public PartnerUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(PartnerUser createdBy) {
		this.createdBy = createdBy;
	}
	
}
