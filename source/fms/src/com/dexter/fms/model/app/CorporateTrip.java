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

import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.ref.LGA;
import com.dexter.fms.model.ref.State;

@Entity
public class CorporateTrip implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private Partner partner;
	
	@ManyToOne
	private PartnerPersonel staff;
	@Temporal(TemporalType.TIMESTAMP)
	private Date departureDateTime;
	private String departureLocation;
	private String purpose;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date estimatedArrivalDateTime;
	private String arrivalLocation;
	
	@ManyToOne
	private State arrivalState;
	@ManyToOne
	private LGA arrivalLga;
	
	private String approvalStatus; // PENDING, APPROVED, DECLINED, NOT_ATTENDED
	private String approvalReason; // Reason for the approval or decline
	@Temporal(TemporalType.TIMESTAMP)
	private Date attendedDate;
	@ManyToOne
	private PartnerUser approveUser;
	
	private String approvalStatus2; // PENDING, APPROVED, DECLINED, NOT_ATTENDED
	private String approvalReason2; // Reason for the approval or decline
	@Temporal(TemporalType.TIMESTAMP)
	private Date attendedDate2;
	@ManyToOne
	private PartnerUser approveUser2;
	
	private String tripStatus; // PENDING, AWAITING, ON_TRIP, SHOULD_BE_COMPLETED, COMPLETION_REQUEST, COMPLETED, CANCELED
	@Temporal(TemporalType.TIMESTAMP)
	private Date completeRequestDateTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date completedDateTime, checkInSubmitDateTime;
	private String checkInComment;
	
	@ManyToOne
	private Vehicle vehicle;
	@ManyToOne
	private PartnerDriver driver;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public CorporateTrip()
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

	public PartnerPersonel getStaff() {
		return staff;
	}

	public void setStaff(PartnerPersonel staff) {
		this.staff = staff;
	}

	public Date getDepartureDateTime() {
		return departureDateTime;
	}

	public void setDepartureDateTime(Date departureDateTime) {
		this.departureDateTime = departureDateTime;
	}

	public String getDepartureLocation() {
		return departureLocation;
	}

	public void setDepartureLocation(String departureLocation) {
		this.departureLocation = departureLocation;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public Date getEstimatedArrivalDateTime() {
		return estimatedArrivalDateTime;
	}

	public void setEstimatedArrivalDateTime(Date estimatedArrivalDateTime) {
		this.estimatedArrivalDateTime = estimatedArrivalDateTime;
	}

	public String getArrivalLocation() {
		return arrivalLocation;
	}

	public void setArrivalLocation(String arrivalLocation) {
		this.arrivalLocation = arrivalLocation;
	}

	public State getArrivalState() {
		return arrivalState;
	}

	public void setArrivalState(State arrivalState) {
		this.arrivalState = arrivalState;
	}

	public LGA getArrivalLga() {
		return arrivalLga;
	}

	public void setArrivalLga(LGA arrivalLga) {
		this.arrivalLga = arrivalLga;
	}

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public String getApprovalReason() {
		return approvalReason;
	}

	public void setApprovalReason(String approvalReason) {
		this.approvalReason = approvalReason;
	}

	public Date getAttendedDate() {
		return attendedDate;
	}

	public void setAttendedDate(Date attendedDate) {
		this.attendedDate = attendedDate;
	}

	public PartnerUser getApproveUser() {
		return approveUser;
	}

	public void setApproveUser(PartnerUser approveUser) {
		this.approveUser = approveUser;
	}

	public String getApprovalStatus2() {
		return approvalStatus2;
	}

	public void setApprovalStatus2(String approvalStatus2) {
		this.approvalStatus2 = approvalStatus2;
	}

	public String getApprovalReason2() {
		return approvalReason2;
	}

	public void setApprovalReason2(String approvalReason2) {
		this.approvalReason2 = approvalReason2;
	}

	public Date getAttendedDate2() {
		return attendedDate2;
	}

	public void setAttendedDate2(Date attendedDate2) {
		this.attendedDate2 = attendedDate2;
	}

	public PartnerUser getApproveUser2() {
		return approveUser2;
	}

	public void setApproveUser2(PartnerUser approveUser2) {
		this.approveUser2 = approveUser2;
	}

	public String getTripStatus() {
		return tripStatus;
	}

	public void setTripStatus(String tripStatus) {
		this.tripStatus = tripStatus;
	}

	public Date getCompleteRequestDateTime() {
		return completeRequestDateTime;
	}

	public void setCompleteRequestDateTime(Date completeRequestDateTime) {
		this.completeRequestDateTime = completeRequestDateTime;
	}

	public Date getCompletedDateTime() {
		return completedDateTime;
	}

	public void setCompletedDateTime(Date completedDateTime) {
		this.completedDateTime = completedDateTime;
	}

	public Date getCheckInSubmitDateTime() {
		return checkInSubmitDateTime;
	}

	public void setCheckInSubmitDateTime(Date checkInSubmitDateTime) {
		this.checkInSubmitDateTime = checkInSubmitDateTime;
	}

	public String getCheckInComment() {
		return checkInComment;
	}

	public void setCheckInComment(String checkInComment) {
		this.checkInComment = checkInComment;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public PartnerDriver getDriver() {
		return driver;
	}

	public void setDriver(PartnerDriver driver) {
		this.driver = driver;
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
