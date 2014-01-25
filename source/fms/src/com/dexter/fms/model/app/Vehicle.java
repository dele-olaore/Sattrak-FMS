package com.dexter.fms.model.app;

import java.io.Serializable;
import java.math.BigDecimal;
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
import javax.validation.constraints.NotNull;

import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.ref.VehicleModel;
import com.dexter.fms.model.ref.Vendor;

@Entity
public class Vehicle implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private VehicleModel model;
	
	@Column(unique=true)
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
	
	private boolean active;
	private String activeStatus;
	
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
	
}
