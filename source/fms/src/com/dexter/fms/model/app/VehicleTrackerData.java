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

@Entity
public class VehicleTrackerData implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private Vehicle vehicle;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date captured_dt;
	// address,distance,longitude,latitude,battery_voltage,engine_hours,speed,heading,hdop,status
	private String address;
	private double odometer;
	private double lon;
	private double lat;
	private double batteryVoltage;
	private double engineHours;
	private double speed;
	private int heading;
	private int hdop;
	private String vehicleTStatus;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	private double fuelLevel;
	
	public VehicleTrackerData()
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

	public Date getCaptured_dt() {
		return captured_dt;
	}

	public void setCaptured_dt(Date captured_dt) {
		this.captured_dt = captured_dt;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getOdometer() {
		return odometer;
	}

	public void setOdometer(double odometer) {
		this.odometer = odometer;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getBatteryVoltage() {
		return batteryVoltage;
	}

	public void setBatteryVoltage(double batteryVoltage) {
		this.batteryVoltage = batteryVoltage;
	}

	public double getEngineHours() {
		return engineHours;
	}

	public void setEngineHours(double engineHours) {
		this.engineHours = engineHours;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public int getHeading() {
		return heading;
	}

	public void setHeading(int heading) {
		this.heading = heading;
	}

	public int getHdop() {
		return hdop;
	}

	public void setHdop(int hdpo) {
		this.hdop = hdpo;
	}

	public String getVehicleTStatus() {
		return vehicleTStatus;
	}

	public void setVehicleTStatus(String vehicleTStatus) {
		this.vehicleTStatus = vehicleTStatus;
	}

	public Date getCrt_dt() {
		return crt_dt;
	}

	public void setCrt_dt(Date crt_dt) {
		this.crt_dt = crt_dt;
	}

	public double getFuelLevel() {
		return fuelLevel;
	}

	public void setFuelLevel(double fuelLevel) {
		this.fuelLevel = fuelLevel;
	}
	
}
