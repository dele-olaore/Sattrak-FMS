package com.dexter.fms.model.app;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.ref.Vendor;

@Entity
public class VehicleAccidentRepair implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private VehicleAccident accident;
	
	@Temporal(TemporalType.DATE)
	private Date repairDate;
	
	private double estimatedCost;
	private double insuranceAmt;
	
	private boolean active;
	
	private String repairerType; // workshop / vendor
	private String repairStatus;
	
	@ManyToOne
	private Vendor insuranceComp;
	private String insuranceComment;
	@ManyToOne
	private Vendor repairComp;
	private String repairDetails;
	private double repairAmt;
	
	private byte[] afterRepairPhoto;
	private byte[] attachment;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public VehicleAccidentRepair()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public VehicleAccident getAccident() {
		return accident;
	}

	public void setAccident(VehicleAccident accident) {
		this.accident = accident;
	}

	public Date getRepairDate() {
		return repairDate;
	}

	public void setRepairDate(Date repairDate) {
		this.repairDate = repairDate;
	}

	public double getEstimatedCost() {
		return estimatedCost;
	}

	public void setEstimatedCost(double estimatedCost) {
		this.estimatedCost = estimatedCost;
	}

	public double getInsuranceAmt() {
		return insuranceAmt;
	}

	public void setInsuranceAmt(double insuranceAmt) {
		this.insuranceAmt = insuranceAmt;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getRepairerType() {
		return repairerType;
	}

	public void setRepairerType(String repairerType) {
		this.repairerType = repairerType;
	}

	public String getRepairStatus() {
		return repairStatus;
	}

	public void setRepairStatus(String repairStatus) {
		this.repairStatus = repairStatus;
	}

	public Vendor getInsuranceComp() {
		return insuranceComp;
	}

	public void setInsuranceComp(Vendor insuranceComp) {
		this.insuranceComp = insuranceComp;
	}

	public String getInsuranceComment() {
		return insuranceComment;
	}

	public void setInsuranceComment(String insuranceComment) {
		this.insuranceComment = insuranceComment;
	}

	public Vendor getRepairComp() {
		return repairComp;
	}

	public void setRepairComp(Vendor repairComp) {
		this.repairComp = repairComp;
	}

	public String getRepairDetails() {
		return repairDetails;
	}

	public void setRepairDetails(String repairDetails) {
		this.repairDetails = repairDetails;
	}

	public double getRepairAmt() {
		return repairAmt;
	}

	public void setRepairAmt(double repairAmt) {
		this.repairAmt = repairAmt;
	}

	public byte[] getAfterRepairPhoto() {
		return afterRepairPhoto;
	}

	public void setAfterRepairPhoto(byte[] afterRepairPhoto) {
		this.afterRepairPhoto = afterRepairPhoto;
	}

	public byte[] getAttachment() {
		return attachment;
	}

	public void setAttachment(byte[] attachment) {
		this.attachment = attachment;
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
