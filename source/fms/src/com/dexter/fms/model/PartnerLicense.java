package com.dexter.fms.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
public class PartnerLicense implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private Partner partner;
	private int currentLicenseCount;
	private int purchasedLicenseCount;
	private int finalLicenseCount;
	private double unitLicensePrice;
	private double totalLicensePrice;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public PartnerLicense()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public int getCurrentLicenseCount() {
		return currentLicenseCount;
	}

	public void setCurrentLicenseCount(int currentLicenseCount) {
		this.currentLicenseCount = currentLicenseCount;
	}

	public int getPurchasedLicenseCount() {
		return purchasedLicenseCount;
	}

	public void setPurchasedLicenseCount(int purchasedLicenseCount) {
		this.purchasedLicenseCount = purchasedLicenseCount;
	}

	public int getFinalLicenseCount() {
		return finalLicenseCount;
	}

	public void setFinalLicenseCount(int finalLicenseCount) {
		this.finalLicenseCount = finalLicenseCount;
	}

	public double getUnitLicensePrice() {
		return unitLicensePrice;
	}

	public void setUnitLicensePrice(double unitLicensePrice) {
		this.unitLicensePrice = unitLicensePrice;
	}

	public double getTotalLicensePrice() {
		return totalLicensePrice;
	}

	public void setTotalLicensePrice(double totalLicensePrice) {
		this.totalLicensePrice = totalLicensePrice;
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
