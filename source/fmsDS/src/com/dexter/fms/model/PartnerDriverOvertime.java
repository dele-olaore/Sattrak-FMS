package com.dexter.fms.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
public class PartnerDriverOvertime implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private PartnerDriver driver;
	
	@Temporal(TemporalType.DATE)
	private Date tranDate;
	private int overtimehours;
	private double amountPerHour;
	private String reason;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	@Transient
	private double amount;
	
	public PartnerDriverOvertime()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PartnerDriver getDriver() {
		return driver;
	}

	public void setDriver(PartnerDriver driver) {
		this.driver = driver;
	}

	public Date getTranDate() {
		return tranDate;
	}

	public void setTranDate(Date tranDate) {
		this.tranDate = tranDate;
	}

	public int getOvertimehours() {
		return overtimehours;
	}

	public void setOvertimehours(int overtimehours) {
		this.overtimehours = overtimehours;
	}

	public double getAmountPerHour() {
		return amountPerHour;
	}

	public void setAmountPerHour(double amountPerHour) {
		this.amountPerHour = amountPerHour;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
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

	public double getAmount() {
		amount = amountPerHour*overtimehours;
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}
	
}
