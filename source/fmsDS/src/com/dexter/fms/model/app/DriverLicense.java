package com.dexter.fms.model.app;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.ref.TransactionType;
import com.dexter.fms.model.ref.Vendor;

@Entity
public class DriverLicense implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private PartnerDriver driver;
	
	@ManyToOne
	private TransactionType tranType; // registration / renewal
	
	private BigDecimal tranAmt;
	@Temporal(TemporalType.DATE)
	private Date tran_dt;
	@Temporal(TemporalType.DATE)
	private Date lic_start_dt;
	@Temporal(TemporalType.DATE)
	private Date lic_end_dt;
	
	private String drvLicenseNo;
	
	private byte[] document;
	
	@ManyToOne
	private Vendor vendor;
	
	private boolean active;
	private boolean expired;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public DriverLicense()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PartnerDriver getDriver() {
		return driver;
	}

	public void setDriver(PartnerDriver driver) {
		this.driver = driver;
	}

	public TransactionType getTranType() {
		return tranType;
	}

	public void setTranType(TransactionType tranType) {
		this.tranType = tranType;
	}

	public BigDecimal getTranAmt() {
		return tranAmt;
	}

	public void setTranAmt(BigDecimal tranAmt) {
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

	public String getDrvLicenseNo() {
		return drvLicenseNo;
	}

	public void setDrvLicenseNo(String drvLicenseNo) {
		this.drvLicenseNo = drvLicenseNo;
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
