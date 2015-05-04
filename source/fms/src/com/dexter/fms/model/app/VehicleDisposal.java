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

@Entity
public class VehicleDisposal implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private Vehicle vehicle;
	
	private double netbookVal;
	private double negoVal;
	private String soldTo;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date request_dt;
	@ManyToOne
	private PartnerUser approvalUser;
	private String approvalStatus; // PENDING, APPROVED, DENIED
	private String approvalComment; // comment for approval status
	@Temporal(TemporalType.TIMESTAMP)
	private Date approval_dt;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	@Transient
	private boolean selected;
	
	public VehicleDisposal()
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

	public double getNetbookVal() {
		return netbookVal;
	}

	public void setNetbookVal(double netbookVal) {
		this.netbookVal = netbookVal;
	}

	public double getNegoVal() {
		return negoVal;
	}

	public void setNegoVal(double negoVal) {
		this.negoVal = negoVal;
	}

	public String getSoldTo() {
		return soldTo;
	}

	public void setSoldTo(String soldTo) {
		this.soldTo = soldTo;
	}

	public Date getRequest_dt() {
		return request_dt;
	}

	public void setRequest_dt(Date request_dt) {
		this.request_dt = request_dt;
	}

	public PartnerUser getApprovalUser() {
		return approvalUser;
	}

	public void setApprovalUser(PartnerUser approvalUser) {
		this.approvalUser = approvalUser;
	}

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public String getApprovalComment() {
		return approvalComment;
	}

	public void setApprovalComment(String approvalComment) {
		this.approvalComment = approvalComment;
	}

	public Date getApproval_dt() {
		return approval_dt;
	}

	public void setApproval_dt(Date approval_dt) {
		this.approval_dt = approval_dt;
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

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
}
