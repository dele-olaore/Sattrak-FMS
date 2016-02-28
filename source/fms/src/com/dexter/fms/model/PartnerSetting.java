package com.dexter.fms.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class PartnerSetting implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private Partner partner;
	
	private byte[] logo;
	private double overTimeAmountPerHour;
	private int maxMinutesToBookTrip, maxMinutesPendingTripApproval;
	
	public PartnerSetting() {}
	
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

	public byte[] getLogo() {
		return logo;
	}

	public void setLogo(byte[] logo) {
		this.logo = logo;
	}

	public double getOverTimeAmountPerHour() {
		return overTimeAmountPerHour;
	}

	public void setOverTimeAmountPerHour(double overTimeAmountPerHour) {
		this.overTimeAmountPerHour = overTimeAmountPerHour;
	}

	public int getMaxMinutesToBookTrip() {
		return maxMinutesToBookTrip;
	}

	public void setMaxMinutesToBookTrip(int maxMinutesToBookTrip) {
		this.maxMinutesToBookTrip = maxMinutesToBookTrip;
	}

	public int getMaxMinutesPendingTripApproval() {
		return maxMinutesPendingTripApproval;
	}

	public void setMaxMinutesPendingTripApproval(int maxMinutesPendingTripApproval) {
		this.maxMinutesPendingTripApproval = maxMinutesPendingTripApproval;
	}
	
}
