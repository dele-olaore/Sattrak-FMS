package com.dexter.fms.model.app;

import java.io.Serializable;
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

import com.dexter.fms.model.PartnerUser;

@Entity
public class VehicleFuelingRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private Vehicle vehicle;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date fueling_dt;
	
	private String location;
	private double odometer;
	private double amt;
	private double litres;
	private double fuelLevel;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date request_dt;
	private String approvalStatus; // PENDING, APPROVED, DENIED
	/*@ManyToOne
	private PartnerUser approvalUser;
	private String approvalStatus; // PENDING, APPROVED, DENIED
	private String approvalComment; // comment for approval status
	@Temporal(TemporalType.TIMESTAMP)
	private Date approval_dt;*/
	
	@ManyToOne
	private VehicleFueling vehicleFueling;	
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	@Transient
	private boolean selected;
	@Transient
	private Vector<Approver> approvers;
	
	public VehicleFuelingRequest()
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

	public Date getFueling_dt() {
		return fueling_dt;
	}

	public void setFueling_dt(Date fueling_dt) {
		this.fueling_dt = fueling_dt;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public double getOdometer() {
		return odometer;
	}

	public void setOdometer(double odometer) {
		this.odometer = odometer;
	}

	public double getAmt() {
		return amt;
	}

	public void setAmt(double amt) {
		this.amt = amt;
	}

	public double getLitres() {
		return litres;
	}

	public void setLitres(double litres) {
		this.litres = litres;
	}

	public double getFuelLevel() {
		return fuelLevel;
	}

	public void setFuelLevel(double fuelLevel) {
		this.fuelLevel = fuelLevel;
	}

	public Date getRequest_dt() {
		return request_dt;
	}

	public void setRequest_dt(Date request_dt) {
		this.request_dt = request_dt;
	}

	/*public PartnerUser getApprovalUser() {
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
	}*/

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public VehicleFueling getVehicleFueling() {
		return vehicleFueling;
	}

	public void setVehicleFueling(VehicleFueling vehicleFueling) {
		this.vehicleFueling = vehicleFueling;
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

	public Vector<Approver> getApprovers() {
		return approvers;
	}

	public void setApprovers(Vector<Approver> approvers) {
		this.approvers = approvers;
	}
	
}
