package com.dexter.fms.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.dexter.fms.model.ref.DriverGrade;

@Entity
public class PartnerDriver implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@OneToOne
	private PartnerPersonel personel;
	
	private String drvLicenseNo;
	@Temporal(TemporalType.DATE)
	private Date drvLicenseExpiryDate;
	private String guarantor;
	private byte[] certificationFile;
	
	@ManyToOne
	private DriverGrade driverGrade;
	
	@ManyToOne
	private Partner partner;
	
	private boolean active;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public PartnerDriver()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PartnerPersonel getPersonel() {
		return personel;
	}

	public void setPersonel(PartnerPersonel personel) {
		this.personel = personel;
	}

	public String getDrvLicenseNo() {
		return drvLicenseNo;
	}

	public void setDrvLicenseNo(String drvLicenseNo) {
		this.drvLicenseNo = drvLicenseNo;
	}

	public Date getDrvLicenseExpiryDate() {
		return drvLicenseExpiryDate;
	}

	public void setDrvLicenseExpiryDate(Date drvLicenseExpiryDate) {
		this.drvLicenseExpiryDate = drvLicenseExpiryDate;
	}

	public String getGuarantor() {
		return guarantor;
	}

	public void setGuarantor(String guarantor) {
		this.guarantor = guarantor;
	}

	public byte[] getCertificationFile() {
		return certificationFile;
	}

	public void setCertificationFile(byte[] certificationFile) {
		this.certificationFile = certificationFile;
	}

	public DriverGrade getDriverGrade() {
		return driverGrade;
	}

	public void setDriverGrade(DriverGrade driverGrade) {
		this.driverGrade = driverGrade;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
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
