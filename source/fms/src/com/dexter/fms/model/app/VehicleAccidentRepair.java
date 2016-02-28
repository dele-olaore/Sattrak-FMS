package com.dexter.fms.model.app;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
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
	
	private String repairType; // REPAIR / REPLACE / GROUNDED
	private boolean requiresAdHocRepair;
	
	@ManyToOne
	private WorkOrder workOrder;
	
	private String replacementRegNo; // if vehicle is to be replaced, this accepts the new vehicle reg number
	
	private double repairAmt;
	private double partnerAmt; // amount contributed by the partner for the repair
	private double insuranceAmt; // amount contributed by the insurance comp for the repair
	
	private boolean active;
	
	private String repairerType; // workshop / vendor
	private String repairStatus; // START / COMPLETED
	
	@ManyToOne
	private Vendor insuranceComp;
	private String insuranceComment;
	@ManyToOne
	private Vendor repairComp;
	
	private String repairDetails;
	
	private byte[] afterRepairPhoto;
	private byte[] attachment;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	@Transient
	private String adhocWorkOrderNum;
	
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

	public String getRepairType() {
		return repairType;
	}

	public void setRepairType(String repairType) {
		this.repairType = repairType;
	}

	public boolean isRequiresAdHocRepair() {
		return requiresAdHocRepair;
	}

	public void setRequiresAdHocRepair(boolean requiresAdHocRepair) {
		this.requiresAdHocRepair = requiresAdHocRepair;
	}

	public WorkOrder getWorkOrder() {
		return workOrder;
	}

	public void setWorkOrder(WorkOrder workOrder) {
		this.workOrder = workOrder;
	}

	public String getReplacementRegNo() {
		return replacementRegNo;
	}

	public void setReplacementRegNo(String replacementRegNo) {
		this.replacementRegNo = replacementRegNo;
	}

	public double getRepairAmt() {
		return repairAmt;
	}

	public void setRepairAmt(double repairAmt) {
		this.repairAmt = repairAmt;
	}

	public double getPartnerAmt() {
		return partnerAmt;
	}

	public void setPartnerAmt(double partnerAmt) {
		this.partnerAmt = partnerAmt;
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

	public String getAdhocWorkOrderNum() {
		return adhocWorkOrderNum;
	}

	public void setAdhocWorkOrderNum(String adhocWorkOrderNum) {
		this.adhocWorkOrderNum = adhocWorkOrderNum;
	}

}
