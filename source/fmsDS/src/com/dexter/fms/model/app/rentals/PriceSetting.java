package com.dexter.fms.model.app.rentals;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.ref.RentalServiceType;
import com.dexter.fms.model.ref.VehicleType;

@Entity
public class PriceSetting implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private Partner partner;
	
	@ManyToOne
	private VehicleType vehicleType;
	private double amountPerDurationType;
	private String durationType;
	@ManyToOne
	private RentalServiceType serviceType; // ONE_WAY_TRIP, RETURN_TRIP
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public PriceSetting()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public double getAmountPerDurationType() {
		return amountPerDurationType;
	}

	public void setAmountPerDurationType(double amountPerDurationType) {
		this.amountPerDurationType = amountPerDurationType;
	}

	public String getDurationType() {
		return durationType;
	}

	public void setDurationType(String durationType) {
		this.durationType = durationType;
	}

	public RentalServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(RentalServiceType serviceType) {
		this.serviceType = serviceType;
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
