package com.dexter.fms.model.app;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.ref.VehicleModel;
import com.dexter.fms.model.ref.Vendor;

@Entity
public class Vehicle implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private VehicleModel model;
	
	private int zonControlId;
	@Column(unique=true)
	private String registrationNo;
	private String engineNo;
	private String chasisNo;
	@Temporal(TemporalType.DATE)
	private Date purchaseDate;
	private BigDecimal purchaseAmt;
	@ManyToOne
	private Vendor vendor;
	
	@ManyToOne
	private Fleet fleet;
	@ManyToOne
	private Partner partner;
	@ManyToOne
	private PartnerPersonel assignee;
	
	private boolean active;
	private String activeStatus;
	
	private boolean disposalAlertSent; // marked when alert for this vehicle to be disposed is already sent
	private String disposalStatus; // Disposable status of this vehicle
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	@Transient
	private VehicleDriver currentDriver;
	@Transient
	private Vector<VehicleDriver> drivers;
	@Transient
	private Vector<VehicleParameters> params;
	@Transient
	private Vector<VehicleTrackerData> trackerData;
	@Transient
	private boolean selected;
	@Transient
	private int age, ageInMonths; // age in years, this should be purchased date - current date or should it be model year - cur year
	
	@Transient
	private VehicleLicense last_lic;
	@Transient
	private VehicleLicense last_insur;
	@Transient
	private BigDecimal maint_odometer;
	@Transient
	private VehicleRoutineMaintenance last_rout_maint;
	@Transient
	private VehicleRoutineMaintenanceSetup rout_setup;
	
	public Vehicle()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public VehicleModel getModel() {
		return model;
	}

	public void setModel(VehicleModel model) {
		this.model = model;
	}

	public int getZonControlId() {
		return zonControlId;
	}

	public void setZonControlId(int zonControlId) {
		this.zonControlId = zonControlId;
	}

	public String getRegistrationNo() {
		return registrationNo;
	}

	public void setRegistrationNo(String registrationNo) {
		this.registrationNo = registrationNo;
	}

	public String getEngineNo() {
		return engineNo;
	}

	public void setEngineNo(String engineNo) {
		this.engineNo = engineNo;
	}

	public String getChasisNo() {
		return chasisNo;
	}

	public void setChasisNo(String chasisNo) {
		this.chasisNo = chasisNo;
	}

	public Date getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public BigDecimal getPurchaseAmt() {
		return purchaseAmt;
	}

	public void setPurchaseAmt(BigDecimal purchaseAmt) {
		this.purchaseAmt = purchaseAmt;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public Fleet getFleet() {
		return fleet;
	}

	public void setFleet(Fleet fleet) {
		this.fleet = fleet;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getActiveStatus() {
		return activeStatus;
	}

	public void setActiveStatus(String activeStatus) {
		this.activeStatus = activeStatus;
	}

	public boolean isDisposalAlertSent() {
		return disposalAlertSent;
	}

	public void setDisposalAlertSent(boolean disposalAlertSent) {
		this.disposalAlertSent = disposalAlertSent;
	}

	public String getDisposalStatus() {
		return disposalStatus;
	}

	public void setDisposalStatus(String disposalStatus) {
		this.disposalStatus = disposalStatus;
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

	public VehicleDriver getCurrentDriver() {
		return currentDriver;
	}

	public void setCurrentDriver(VehicleDriver currentDriver) {
		this.currentDriver = currentDriver;
	}

	public Vector<VehicleDriver> getDrivers() {
		return drivers;
	}

	public void setDrivers(Vector<VehicleDriver> drivers) {
		this.drivers = drivers;
	}

	public Vector<VehicleParameters> getParams() {
		return params;
	}

	public void setParams(Vector<VehicleParameters> params) {
		this.params = params;
	}

	public Vector<VehicleTrackerData> getTrackerData() {
		return trackerData;
	}

	public void setTrackerData(Vector<VehicleTrackerData> trackerData) {
		this.trackerData = trackerData;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public int getAge() {
		if(getPurchaseDate() != null)
		{
			Calendar c = Calendar.getInstance(), cNow = Calendar.getInstance();
			c.setTime(getPurchaseDate());
			age = cNow.get(Calendar.YEAR) - c.get(Calendar.YEAR);
		}
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getAgeInMonths() {
		if(getPurchaseDate() != null)
		{
			long day = (1000 * 60 * 60 * 24); // 24 hours in milliseconds
			long time = day * 30; // 30 days for a month
			
			long pdtime = getPurchaseDate().getTime();
			ageInMonths = Integer.parseInt(""+(pdtime/time));
		}
		return ageInMonths;
	}

	public void setAgeInMonths(int ageInMonths) {
		this.ageInMonths = ageInMonths;
	}

	public VehicleLicense getLast_lic() {
		return last_lic;
	}

	public void setLast_lic(VehicleLicense last_lic) {
		this.last_lic = last_lic;
	}

	public VehicleLicense getLast_insur() {
		return last_insur;
	}

	public void setLast_insur(VehicleLicense last_insur) {
		this.last_insur = last_insur;
	}

	public VehicleRoutineMaintenance getLast_rout_maint() {
		return last_rout_maint;
	}

	public void setLast_rout_maint(VehicleRoutineMaintenance last_rout_maint) {
		this.last_rout_maint = last_rout_maint;
	}

	public VehicleRoutineMaintenanceSetup getRout_setup() {
		if(rout_setup == null)
			rout_setup = new VehicleRoutineMaintenanceSetup();
		return rout_setup;
	}

	public void setRout_setup(VehicleRoutineMaintenanceSetup rout_setup) {
		this.rout_setup = rout_setup;
	}

	public BigDecimal getMaint_odometer() {
		return maint_odometer;
	}

	public void setMaint_odometer(BigDecimal maint_odometer) {
		this.maint_odometer = maint_odometer;
	}

	public PartnerPersonel getAssignee() {
		return assignee;
	}

	public void setAssignee(PartnerPersonel assignee) {
		this.assignee = assignee;
	}
	
}
