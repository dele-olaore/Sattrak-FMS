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

import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerUser;

@Entity
public class VehicleAccident implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private Vehicle vehicle;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date accident_dt;
	
	@ManyToOne
	private PartnerDriver assignedDriver;
	private String driver_name;
	private String accidentDescription;
	private String driverComment;
	
	private String policeStation;
	private String policeOfficer;
	private String policePhone;
	private String policeComment;
	@Temporal(TemporalType.DATE)
	private Date dateTakenToStation;
	
	@Temporal(TemporalType.DATE)
	private Date dateReported;
	@Temporal(TemporalType.DATE)
	private Date dateReleased;
	
	private boolean active;
	
	private boolean requiresRepairOrReplace;
	private boolean repairApproved;
	private String action_description; // description of type of repiar required, e.g. repair, replace
	private String repairerType; // workshop / vendor
	
	private byte[] accidentPhoto;
	private byte[] document;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public VehicleAccident()
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

	public Date getAccident_dt() {
		return accident_dt;
	}

	public void setAccident_dt(Date accident_dt) {
		this.accident_dt = accident_dt;
	}

	public PartnerDriver getAssignedDriver() {
		return assignedDriver;
	}

	public void setAssignedDriver(PartnerDriver assignedDriver) {
		this.assignedDriver = assignedDriver;
	}

	public String getDriver_name() {
		return driver_name;
	}

	public void setDriver_name(String driver_name) {
		this.driver_name = driver_name;
	}

	public String getAccidentDescription() {
		return accidentDescription;
	}

	public void setAccidentDescription(String accidentDescription) {
		this.accidentDescription = accidentDescription;
	}

	public String getDriverComment() {
		return driverComment;
	}

	public void setDriverComment(String driverComment) {
		this.driverComment = driverComment;
	}

	public String getPoliceStation() {
		return policeStation;
	}

	public void setPoliceStation(String policeStation) {
		this.policeStation = policeStation;
	}

	public String getPoliceOfficer() {
		return policeOfficer;
	}

	public void setPoliceOfficer(String policeOfficer) {
		this.policeOfficer = policeOfficer;
	}

	public String getPolicePhone() {
		return policePhone;
	}

	public void setPolicePhone(String policePhone) {
		this.policePhone = policePhone;
	}

	public String getPoliceComment() {
		return policeComment;
	}

	public void setPoliceComment(String policeComment) {
		this.policeComment = policeComment;
	}

	public Date getDateTakenToStation() {
		return dateTakenToStation;
	}

	public void setDateTakenToStation(Date dateTakenToStation) {
		this.dateTakenToStation = dateTakenToStation;
	}

	public Date getDateReported() {
		return dateReported;
	}

	public void setDateReported(Date dateReported) {
		this.dateReported = dateReported;
	}

	public Date getDateReleased() {
		return dateReleased;
	}

	public void setDateReleased(Date dateReleased) {
		this.dateReleased = dateReleased;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isRequiresRepairOrReplace() {
		return requiresRepairOrReplace;
	}

	public void setRequiresRepairOrReplace(boolean requiresRepairOrReplace) {
		this.requiresRepairOrReplace = requiresRepairOrReplace;
	}

	public boolean isRepairApproved() {
		return repairApproved;
	}

	public void setRepairApproved(boolean repairApproved) {
		this.repairApproved = repairApproved;
	}

	public String getAction_description() {
		return action_description;
	}

	public void setAction_description(String action_description) {
		this.action_description = action_description;
	}

	public String getRepairerType() {
		return repairerType;
	}

	public void setRepairerType(String repairerType) {
		this.repairerType = repairerType;
	}

	public byte[] getAccidentPhoto() {
		return accidentPhoto;
	}

	public void setAccidentPhoto(byte[] accidentPhoto) {
		this.accidentPhoto = accidentPhoto;
	}

	public byte[] getDocument() {
		return document;
	}

	public void setDocument(byte[] document) {
		this.document = document;
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
