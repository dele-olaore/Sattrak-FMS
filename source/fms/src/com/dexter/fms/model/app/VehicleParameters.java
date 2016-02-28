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
import com.dexter.fms.model.ref.Department;
import com.dexter.fms.model.ref.FuelType;
import com.dexter.fms.model.ref.Region;
import com.dexter.fms.model.ref.Unit;

@Entity
public class VehicleParameters implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private Vehicle vehicle;
	
	private String tyresize;
	private double tankcapacity;
	private double calibratedcapacity;
	private double fuelCompKML;
	private double fuelComp100KML;
	private String color;
	private byte[] photo;
	
	@ManyToOne
	private FuelType fuelType;
	
	private String simno;
	private String unitofmeasure;
	private String cardno;
	
	@ManyToOne
	private Unit unit;
	@ManyToOne
	private Department dept;
	@ManyToOne
	private Region region;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	private boolean active;
	
	public VehicleParameters()
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

	public String getTyresize() {
		return tyresize;
	}

	public void setTyresize(String tyresize) {
		this.tyresize = tyresize;
	}

	public double getTankcapacity() {
		return tankcapacity;
	}

	public void setTankcapacity(double tankcapacity) {
		this.tankcapacity = tankcapacity;
	}

	public double getCalibratedcapacity() {
		return calibratedcapacity;
	}

	public void setCalibratedcapacity(double calibratedcapacity) {
		this.calibratedcapacity = calibratedcapacity;
	}

	public double getFuelCompKML() {
		return fuelCompKML;
	}

	public void setFuelCompKML(double fuelCompKML) {
		this.fuelCompKML = fuelCompKML;
	}

	public double getFuelComp100KML() {
		return fuelComp100KML;
	}

	public void setFuelComp100KML(double fuelComp100KML) {
		this.fuelComp100KML = fuelComp100KML;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public FuelType getFuelType() {
		return fuelType;
	}

	public void setFuelType(FuelType fuelType) {
		this.fuelType = fuelType;
	}

	public String getSimno() {
		return simno;
	}

	public void setSimno(String simno) {
		this.simno = simno;
	}

	public String getUnitofmeasure() {
		return unitofmeasure;
	}

	public void setUnitofmeasure(String unitofmeasure) {
		this.unitofmeasure = unitofmeasure;
	}

	public String getCardno() {
		return cardno;
	}

	public void setCardno(String cardno) {
		this.cardno = cardno;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public Department getDept() {
		return dept;
	}

	public void setDept(Department dept) {
		this.dept = dept;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
}
