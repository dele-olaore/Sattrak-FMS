package com.dexter.fms.model.fuelcard;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.app.Vehicle;

@Entity
@Table(name="DRIVERRECORD_TB")
public class DriverRecord implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
    @NotNull
    @GeneratedValue
	private Long id;
	
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date tranTime;
	private BigDecimal initFuelLvl;
	private BigDecimal quantity;
	private BigDecimal finalFuelLvl;
	private BigDecimal odometer;
	private BigDecimal fuelRate;
	private BigDecimal cost;
	
	private String location;
	
	@ManyToOne
	private PartnerDriver driver;
	
	@ManyToOne
	private Vehicle car;
	
	private String regNumber;
	private String refNumber;
	
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	public DriverRecord()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getTranTime() {
		return tranTime;
	}

	public void setTranTime(Date tranTime) {
		this.tranTime = tranTime;
	}

	public BigDecimal getInitFuelLvl() {
		return initFuelLvl;
	}

	public void setInitFuelLvl(BigDecimal initFuelLvl) {
		this.initFuelLvl = initFuelLvl;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getFinalFuelLvl() {
		return finalFuelLvl;
	}

	public void setFinalFuelLvl(BigDecimal finalFuelLvl) {
		this.finalFuelLvl = finalFuelLvl;
	}

	public BigDecimal getOdometer() {
		return odometer;
	}

	public void setOdometer(BigDecimal odometer) {
		this.odometer = odometer;
	}

	public BigDecimal getFuelRate() {
		return fuelRate;
	}

	public void setFuelRate(BigDecimal fuelRate) {
		this.fuelRate = fuelRate;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public PartnerDriver getDriver() {
		return driver;
	}

	public void setDriver(PartnerDriver driver) {
		this.driver = driver;
	}

	public Vehicle getCar() {
		return car;
	}

	public void setCar(Vehicle car) {
		this.car = car;
	}

	public String getRegNumber() {
		return regNumber;
	}

	public void setRegNumber(String regNumber) {
		this.regNumber = regNumber;
	}

	public String getRefNumber() {
		return refNumber;
	}

	public void setRefNumber(String refNumber) {
		this.refNumber = refNumber;
	}

	public Date getCrt_dt() {
		return crt_dt;
	}

	public void setCrt_dt(Date crt_dt) {
		this.crt_dt = crt_dt;
	}
	
}
