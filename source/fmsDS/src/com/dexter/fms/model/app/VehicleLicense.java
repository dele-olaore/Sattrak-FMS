package com.dexter.fms.model.app;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.ref.LicenseType;
import com.dexter.fms.model.ref.TransactionType;
import com.dexter.fms.model.ref.Vendor;

// should have licensetype: license/insurance, transactiontype: registration/renewal, and documents attached
@Entity
public class VehicleLicense implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private Vehicle vehicle;
	
	@ManyToOne
	private LicenseType licType; // License / insurance
	private String subLicType; // empty for vehicle license, Premium insurace, Third Party insurance, driver's license
	
	@ManyToOne
	private TransactionType tranType; // registration / renewal
	
	private double tranAmt;
	@Temporal(TemporalType.DATE)
	private Date tran_dt;
	@Temporal(TemporalType.DATE)
	private Date lic_start_dt;
	@Temporal(TemporalType.DATE)
	private Date lic_end_dt;
	
	private byte[] document;
	
	@ManyToOne
	private Vendor vendor;
	
	private boolean active;
	private boolean expired;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public VehicleLicense()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public LicenseType getLicType() {
		return licType;
	}

	public void setLicType(LicenseType licType) {
		this.licType = licType;
	}

	public String getSubLicType() {
		return subLicType;
	}

	public void setSubLicType(String subLicType) {
		this.subLicType = subLicType;
	}

	public TransactionType getTranType() {
		return tranType;
	}

	public void setTranType(TransactionType tranType) {
		this.tranType = tranType;
	}

	public double getTranAmt() {
		return tranAmt;
	}

	public void setTranAmt(double tranAmt) {
		this.tranAmt = tranAmt;
	}

	public Date getTran_dt() {
		return tran_dt;
	}

	public void setTran_dt(Date tran_dt) {
		this.tran_dt = tran_dt;
	}

	public Date getLic_start_dt() {
		return lic_start_dt;
	}

	public void setLic_start_dt(Date lic_start_dt) {
		this.lic_start_dt = lic_start_dt;
	}

	public Date getLic_end_dt() {
		return lic_end_dt;
	}

	public void setLic_end_dt(Date lic_end_dt) {
		this.lic_end_dt = lic_end_dt;
	}

	public byte[] getDocument() {
		return document;
	}

	public void setDocument(byte[] document) {
		this.document = document;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
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
