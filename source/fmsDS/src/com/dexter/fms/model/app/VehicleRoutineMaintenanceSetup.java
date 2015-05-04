package com.dexter.fms.model.app;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.dexter.fms.model.PartnerUser;

@Entity
public class VehicleRoutineMaintenanceSetup implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private Vehicle vehicle;
	
	private BigDecimal last_m_odometer; // the last odometer the routine maintenance was done
	private BigDecimal interval_odometer; // eg. at every 5000 KM
	private BigDecimal alert_interval_odometer; // eg. at every 4500 KM
	
	private BigDecimal odometer; // the next odometer the routine maintenance should be done
	private BigDecimal alert_odometer; // alert when the vehicles odometer gets to or greater than this value
	
	private boolean active;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public VehicleRoutineMaintenanceSetup()
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

	public BigDecimal getLast_m_odometer() {
		return last_m_odometer;
	}

	public void setLast_m_odometer(BigDecimal last_m_odometer) {
		this.last_m_odometer = last_m_odometer;
	}

	public BigDecimal getInterval_odometer() {
		return interval_odometer;
	}

	public void setInterval_odometer(BigDecimal interval_odometer) {
		this.interval_odometer = interval_odometer;
	}

	public BigDecimal getAlert_interval_odometer() {
		return alert_interval_odometer;
	}

	public void setAlert_interval_odometer(BigDecimal alert_interval_odometer) {
		this.alert_interval_odometer = alert_interval_odometer;
	}

	public BigDecimal getOdometer() {
		return odometer;
	}

	public void setOdometer(BigDecimal odometer) {
		this.odometer = odometer;
	}

	public BigDecimal getAlert_odometer() {
		return alert_odometer;
	}

	public void setAlert_odometer(BigDecimal alert_odometer) {
		this.alert_odometer = alert_odometer;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
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
