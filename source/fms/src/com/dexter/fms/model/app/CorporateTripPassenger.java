package com.dexter.fms.model.app;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.dexter.fms.model.PartnerPersonel;

@Entity
public class CorporateTripPassenger implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private CorporateTrip trip;
	@ManyToOne
	private PartnerPersonel passenger;
	
	public CorporateTripPassenger()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CorporateTrip getTrip() {
		return trip;
	}

	public void setTrip(CorporateTrip trip) {
		this.trip = trip;
	}

	public PartnerPersonel getPassenger() {
		return passenger;
	}

	public void setPassenger(PartnerPersonel passenger) {
		this.passenger = passenger;
	}
	
}
