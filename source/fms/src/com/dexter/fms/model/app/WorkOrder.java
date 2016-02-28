package com.dexter.fms.model.app;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.ref.Vendor;

@Entity
public class WorkOrder implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	private String workOrderNumber;
	private String summaryDetailsOfWorkOrder;
	private String workOrderType; // Routine or Adhoc
	private boolean finished;
	private String status; // SETUP, REQUEST-NEGOTIATION, NEGOTIATED, REQUEST-FINAL1, REQUEST, NEW, CANCELED, IN-PROGRESS, COMPLETED
	@ManyToOne
	private Vendor vendor;
	private byte[] vendorDocument;
	@Temporal(TemporalType.TIMESTAMP)
	private Date proposedCompletion_dt; // this date should be estimated when the work starts for this workorder, it should be the start date plus the amount of days the selected vendor said the work will be completed
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date maxBidSubmission_dt;
	
	@ManyToOne
	private Partner partner;
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt, negotiated_dt, approve_dt, finalApprove_dt;
	@ManyToOne
	private PartnerUser createdBy, negotiatedBy, approveBy, finalApproveBy;
	private String negotiateComment, approveComment, finalApproveComment;
	
	@Transient
	private Vector<WorkOrderVehicle> vehicles;
	@Transient
	private WorkOrderVehicle selectedVehicle;
	@Transient
	private Vector<WorkOrderVendor> wvendors, approved_wvendors;
	@Transient
	private String approveVendorComment;
	@Transient
	private boolean due;
	@Transient
	private int dueDays, dueLvl, negotiated_days_of_completion;
	@Transient
	private String dueDesc;
	@Transient
	private BigDecimal negotiated_cost;
	
	public WorkOrder()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getWorkOrderNumber() {
		return workOrderNumber;
	}

	public void setWorkOrderNumber(String workOrderNumber) {
		this.workOrderNumber = workOrderNumber;
	}

	public String getSummaryDetailsOfWorkOrder() {
		return summaryDetailsOfWorkOrder;
	}

	public void setSummaryDetailsOfWorkOrder(String summaryDetailsOfWorkOrder) {
		this.summaryDetailsOfWorkOrder = summaryDetailsOfWorkOrder;
	}

	public String getWorkOrderType() {
		return workOrderType;
	}

	public void setWorkOrderType(String workOrderType) {
		this.workOrderType = workOrderType;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public Date getNegotiated_dt() {
		return negotiated_dt;
	}

	public void setNegotiated_dt(Date negotiated_dt) {
		this.negotiated_dt = negotiated_dt;
	}

	public PartnerUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(PartnerUser createdBy) {
		this.createdBy = createdBy;
	}

	public Vector<WorkOrderVehicle> getVehicles() {
		if(vehicles == null)
			vehicles = new Vector<WorkOrderVehicle>();
		return vehicles;
	}

	public void setVehicles(Vector<WorkOrderVehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public WorkOrderVehicle getSelectedVehicle() {
		return selectedVehicle;
	}

	public void setSelectedVehicle(WorkOrderVehicle selectedVehicle) {
		this.selectedVehicle = selectedVehicle;
	}

	public Date getProposedCompletion_dt() {
		return proposedCompletion_dt;
	}

	public void setProposedCompletion_dt(Date proposedCompletion_dt) {
		this.proposedCompletion_dt = proposedCompletion_dt;
	}

	public Date getMaxBidSubmission_dt() {
		return maxBidSubmission_dt;
	}

	public void setMaxBidSubmission_dt(Date maxBidSubmission_dt) {
		this.maxBidSubmission_dt = maxBidSubmission_dt;
	}

	public Vector<WorkOrderVendor> getWvendors() {
		return wvendors;
	}

	public void setWvendors(Vector<WorkOrderVendor> wvendors) {
		this.wvendors = wvendors;
	}

	public Vector<WorkOrderVendor> getApproved_wvendors() {
		if(getWvendors() != null) {
			approved_wvendors = new Vector<WorkOrderVendor>();
			for(WorkOrderVendor wv : getWvendors()) {
				if(wv.isApproverApproved())
					approved_wvendors.add(wv);
			}
		}
		return approved_wvendors;
	}

	public void setApproved_wvendors(Vector<WorkOrderVendor> approved_wvendors) {
		this.approved_wvendors = approved_wvendors;
	}

	public Date getApprove_dt() {
		return approve_dt;
	}

	public void setApprove_dt(Date approve_dt) {
		this.approve_dt = approve_dt;
	}

	public Date getFinalApprove_dt() {
		return finalApprove_dt;
	}

	public void setFinalApprove_dt(Date finalApprove_dt) {
		this.finalApprove_dt = finalApprove_dt;
	}

	public PartnerUser getApproveBy() {
		return approveBy;
	}

	public void setApproveBy(PartnerUser approveBy) {
		this.approveBy = approveBy;
	}

	public PartnerUser getNegotiatedBy() {
		return negotiatedBy;
	}

	public void setNegotiatedBy(PartnerUser negotiatedBy) {
		this.negotiatedBy = negotiatedBy;
	}

	public PartnerUser getFinalApproveBy() {
		return finalApproveBy;
	}

	public void setFinalApproveBy(PartnerUser finalApproveBy) {
		this.finalApproveBy = finalApproveBy;
	}

	public String getApproveVendorComment() {
		return approveVendorComment;
	}

	public void setApproveVendorComment(String approveVendorComment) {
		this.approveVendorComment = approveVendorComment;
	}

	public String getNegotiateComment() {
		return negotiateComment;
	}

	public void setNegotiateComment(String negotiateComment) {
		this.negotiateComment = negotiateComment;
	}

	public String getApproveComment() {
		return approveComment;
	}

	public void setApproveComment(String approveComment) {
		this.approveComment = approveComment;
	}

	public String getFinalApproveComment() {
		return finalApproveComment;
	}

	public void setFinalApproveComment(String finalApproveComment) {
		this.finalApproveComment = finalApproveComment;
	}

	public boolean isDue() {
		if(proposedCompletion_dt != null) {
			if(new Date().after(proposedCompletion_dt))
				due = true;
		}
		return due;
	}

	public void setDue(boolean due) {
		this.due = due;
	}

	public int getDueDays() {
		if(isDue()) {
			Calendar c = Calendar.getInstance(), c2 = Calendar.getInstance();
			c2.setTime(proposedCompletion_dt);
			dueDays = 0;
			while(c.after(c2)) {
				dueDays += 1;
				c2.add(Calendar.DATE, 1);
			}
		}
		return dueDays;
	}

	public void setDueDays(int dueDays) {
		this.dueDays = dueDays;
	}

	public int getDueLvl() {
		if(isDue()) {
			getDueDesc();
		}
		return dueLvl;
	}

	public void setDueLvl(int dueLvl) {
		this.dueLvl = dueLvl;
	}

	public String getDueDesc() {
		int days = getDueDays();
		if(days > 0) {
			if(days >= 14 && days < 28) {
				dueLvl = 1;
				dueDesc = "Due: Two Weeks or more";
			} else if(days >= 28 && days < 56) {
				dueLvl = 2;
				dueDesc = "Due: One month or more";
			} else if(days >= 56) {
				dueLvl = 3;
				dueDesc = "Due: Two months or more";
			}
		}
		return dueDesc;
	}

	public void setDueDesc(String dueDesc) {
		this.dueDesc = dueDesc;
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
