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
import javax.validation.constraints.NotNull;

import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.ref.Vendor;

@Entity
public class WorkOrderVendor implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private WorkOrder workOrder;
	@ManyToOne
	private Vendor vendor;
	private String submittionStatus; // PENDING, SUBMITTED, DECLINED
	private int days_of_completion, negotiated_days_of_completion; // how long it'll take from when the work starts
	private BigDecimal cost, negotiated_cost;
	private String comment;
	private byte[] vendorDocument, negotiatedDocument;
	
	private boolean requesterRecommended, approverApproved; // is this the vendor recommended by the requester. Is this the vendor approved by the approver
	private String requesterComment, approverComment; // requester comment on its recommended vendor. approver comment on its approved vendor.
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt, vendorResponse_dt;
	@ManyToOne
	private PartnerUser createdBy;
	
	public WorkOrderVendor() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public WorkOrder getWorkOrder() {
		return workOrder;
	}

	public void setWorkOrder(WorkOrder workOrder) {
		this.workOrder = workOrder;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public String getSubmittionStatus() {
		return submittionStatus;
	}

	public void setSubmittionStatus(String submittionStatus) {
		this.submittionStatus = submittionStatus;
	}

	public int getDays_of_completion() {
		return days_of_completion;
	}

	public void setDays_of_completion(int days_of_completion) {
		this.days_of_completion = days_of_completion;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public byte[] getVendorDocument() {
		return vendorDocument;
	}

	public void setVendorDocument(byte[] vendorDocument) {
		this.vendorDocument = vendorDocument;
	}

	public byte[] getNegotiatedDocument() {
		return negotiatedDocument;
	}

	public void setNegotiatedDocument(byte[] negotiatedDocument) {
		this.negotiatedDocument = negotiatedDocument;
	}

	public boolean isRequesterRecommended() {
		return requesterRecommended;
	}

	public void setRequesterRecommended(boolean requesterRecommended) {
		this.requesterRecommended = requesterRecommended;
	}

	public boolean isApproverApproved() {
		return approverApproved;
	}

	public void setApproverApproved(boolean approverApproved) {
		this.approverApproved = approverApproved;
	}

	public String getRequesterComment() {
		return requesterComment;
	}

	public void setRequesterComment(String requesterComment) {
		this.requesterComment = requesterComment;
	}

	public String getApproverComment() {
		return approverComment;
	}

	public void setApproverComment(String approverComment) {
		this.approverComment = approverComment;
	}

	public Date getCrt_dt() {
		return crt_dt;
	}

	public void setCrt_dt(Date crt_dt) {
		this.crt_dt = crt_dt;
	}

	public Date getVendorResponse_dt() {
		return vendorResponse_dt;
	}

	public void setVendorResponse_dt(Date vendorResponse_dt) {
		this.vendorResponse_dt = vendorResponse_dt;
	}

	public PartnerUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(PartnerUser createdBy) {
		this.createdBy = createdBy;
	}

	public int getNegotiated_days_of_completion() {
		return negotiated_days_of_completion;
	}

	public void setNegotiated_days_of_completion(int negotiated_days_of_completion) {
		this.negotiated_days_of_completion = negotiated_days_of_completion;
	}

	public BigDecimal getNegotiated_cost() {
		return negotiated_cost;
	}

	public void setNegotiated_cost(BigDecimal negotiated_cost) {
		this.negotiated_cost = negotiated_cost;
	}
	
}
