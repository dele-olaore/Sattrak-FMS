package com.dexter.fms.model.ref;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

import javax.persistence.CascadeType;
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
import com.dexter.fms.model.app.Vehicle;

// should include the year
@Entity
public class VehicleModel implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	private String name;
	private String year;
	@ManyToOne
	private Partner partner;
	
	@ManyToOne(cascade = {CascadeType.PERSIST})
	private VehicleMake maker;
	@ManyToOne(cascade = {CascadeType.PERSIST})
	private VehicleType type;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	@Transient
	private Vector<Vehicle> vehicles;
	
	public VehicleModel()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public VehicleMake getMaker() {
		return maker;
	}

	public void setMaker(VehicleMake maker) {
		this.maker = maker;
	}

	public VehicleType getType() {
		return type;
	}

	public void setType(VehicleType type) {
		this.type = type;
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

	public Vector<Vehicle> getVehicles() {
		return vehicles;
	}

	public void setVehicles(Vector<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}
	
}
