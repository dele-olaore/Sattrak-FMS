package com.dexter.fms.model.fuelcard;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.dexter.fms.model.app.Vehicle;

@Entity
@Table(name="TRACKERRECORD_TB")
public class TrackerRecord implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
    @NotNull
    @GeneratedValue
	private Long id;
	
	private int unitID;
	private String unitName;
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date tranTime;
	private double initFuelLvl;
	private double quantity;
	private double finalFuelLvl;
	private String address;
	private String model;
	private String comp;
	private String year;
	private String tranType;
	private double odometer;
	private double cost;
	
	@ManyToOne
	private Vehicle vehicle;
	
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	public TrackerRecord()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getUnitID() {
		return unitID;
	}

	public void setUnitID(int unitID) {
		this.unitID = unitID;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public Date getTranTime() {
		return tranTime;
	}

	public void setTranTime(Date tranTime) {
		this.tranTime = tranTime;
	}

	public double getInitFuelLvl() {
		return initFuelLvl;
	}

	public void setInitFuelLvl(double initFuelLvl) {
		this.initFuelLvl = initFuelLvl;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public double getFinalFuelLvl() {
		return finalFuelLvl;
	}

	public void setFinalFuelLvl(double finalFuelLvl) {
		this.finalFuelLvl = finalFuelLvl;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getComp() {
		return comp;
	}

	public void setComp(String comp) {
		this.comp = comp;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getTranType() {
		return tranType;
	}

	public void setTranType(String tranType) {
		this.tranType = tranType;
	}

	public double getOdometer() {
		return odometer;
	}

	public void setOdometer(double odometer) {
		this.odometer = odometer;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public Date getCrt_dt() {
		return crt_dt;
	}

	public void setCrt_dt(Date crt_dt) {
		this.crt_dt = crt_dt;
	}
	
}
