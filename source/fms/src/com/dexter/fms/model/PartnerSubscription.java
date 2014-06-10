package com.dexter.fms.model;

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
public class PartnerSubscription implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	@ManyToOne
	private Partner partner;
	@ManyToOne
	private ApplicationTypeVersion appTypeVersion;
	private double subscriptionFee;
	
	private boolean demo; // indicates if this subscription is a demo. If it is, then the duration is the amount of days specified in the days varable
	private int days;
	@Temporal(TemporalType.TIMESTAMP)
	private Date start_dt;
	@Temporal(TemporalType.TIMESTAMP)
	private Date end_dt;
	private boolean expired;
	
	private boolean active;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public PartnerSubscription()
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

	public ApplicationTypeVersion getAppTypeVersion() {
		return appTypeVersion;
	}

	public void setAppTypeVersion(ApplicationTypeVersion appTypeVersion) {
		this.appTypeVersion = appTypeVersion;
	}

	public double getSubscriptionFee() {
		return subscriptionFee;
	}

	public void setSubscriptionFee(double subscriptionFee) {
		this.subscriptionFee = subscriptionFee;
	}

	public boolean isDemo() {
		return demo;
	}

	public void setDemo(boolean demo) {
		this.demo = demo;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public Date getStart_dt() {
		return start_dt;
	}

	public void setStart_dt(Date start_dt) {
		this.start_dt = start_dt;
	}

	public Date getEnd_dt() {
		return end_dt;
	}

	public void setEnd_dt(Date end_dt) {
		this.end_dt = end_dt;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
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
