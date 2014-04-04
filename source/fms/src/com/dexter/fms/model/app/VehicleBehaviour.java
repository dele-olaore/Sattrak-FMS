package com.dexter.fms.model.app;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.ref.VehicleWarning;

@Entity
public class VehicleBehaviour implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private Vehicle vehicle;
	@ManyToOne
	private PartnerDriver driver;
	@Temporal(TemporalType.TIMESTAMP)
	private Date eventDate;
	@ManyToOne
	private VehicleWarning warning;
	private int warningValue;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	@Transient
	private boolean selected;
	
	public VehicleBehaviour()
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

	public PartnerDriver getDriver() {
		return driver;
	}

	public void setDriver(PartnerDriver driver) {
		this.driver = driver;
	}

	public Date getEventDate() {
		return eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}

	public VehicleWarning getWarning() {
		return warning;
	}

	public void setWarning(VehicleWarning warning) {
		this.warning = warning;
	}

	public int getWarningValue() {
		return warningValue;
	}

	public void setWarningValue(int warningValue) {
		this.warningValue = warningValue;
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
	
}
