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

@Entity
public class Approver implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	private String entityName; // The request entity that this approver is for, eg. ExpenseRequest, OvertimeRequest and so on
	private Long entityId; // The Id of the entity, so we can load
	private int approverLevel; // 1 - for first level, 2 - for second level. If a approver is second level, then the first level must approve before it gets to the second level approver
	
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
	
	public Approver() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public int getApproverLevel() {
		return approverLevel;
	}

	public void setApproverLevel(int approverLevel) {
		this.approverLevel = approverLevel;
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
	
}
