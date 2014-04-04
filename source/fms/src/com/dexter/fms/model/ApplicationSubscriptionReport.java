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
public class ApplicationSubscriptionReport implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private ApplicationTypeReport appTypeReport;
	
	@ManyToOne
	private SubscriptionType subType;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	public ApplicationSubscriptionReport()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ApplicationTypeReport getAppTypeReport() {
		return appTypeReport;
	}

	public void setAppTypeReport(ApplicationTypeReport appTypeReport) {
		this.appTypeReport = appTypeReport;
	}

	public SubscriptionType getSubType() {
		return subType;
	}

	public void setSubType(SubscriptionType subType) {
		this.subType = subType;
	}

	public Date getCrt_dt() {
		return crt_dt;
	}

	public void setCrt_dt(Date crt_dt) {
		this.crt_dt = crt_dt;
	}
	
}
