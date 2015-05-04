package com.dexter.fms.model.app;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.ref.VehicleMake;
import com.dexter.fms.model.ref.VehicleType;

@Entity
public class Fleet implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
	private Long id;
	
	private String name;
	private String description;
	private int vehiclesActiveMonths;
	private boolean defaultFleet;
	
	@ManyToOne
	private Partner partner;
	
	@ManyToOne
	private VehicleType vehicleType;
	@ManyToOne
	private VehicleMake vehicleMake;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	@Transient
	private List<Vehicle> vehicles;
	@Transient
	private BigDecimal fleetCost;
	
	public Fleet()
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getVehiclesActiveMonths() {
		return vehiclesActiveMonths;
	}

	public void setVehiclesActiveMonths(int vehiclesActiveMonths) {
		this.vehiclesActiveMonths = vehiclesActiveMonths;
	}

	public boolean isDefaultFleet() {
		return defaultFleet;
	}

	public void setDefaultFleet(boolean defaultFleet) {
		this.defaultFleet = defaultFleet;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public VehicleType getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(VehicleType vehicleType) {
		this.vehicleType = vehicleType;
	}

	public VehicleMake getVehicleMake() {
		return vehicleMake;
	}

	public void setVehicleMake(VehicleMake vehicleMake) {
		this.vehicleMake = vehicleMake;
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

	public List<Vehicle> getVehicles() {
		if(vehicles == null)
			vehicles = new ArrayList<Vehicle>();
		return vehicles;
	}

	public void setVehicles(List<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public BigDecimal getFleetCost() {
		return fleetCost;
	}

	public void setFleetCost(BigDecimal fleetCost) {
		this.fleetCost = fleetCost;
	}
	
}
