package com.dexter.fms.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
public class SubscriptionPackage implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	private String name;
	@ManyToOne
	private ApplicationType appType;
	@ManyToOne
	private SubscriptionType subType;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	@Transient
	private Vector<ApplicationSubscriptionModule> modules;
	@Transient
	private Vector<ApplicationSubscriptionReport> reports;
	
	public SubscriptionPackage()
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

	public ApplicationType getAppType() {
		return appType;
	}

	public void setAppType(ApplicationType appType) {
		this.appType = appType;
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

	public PartnerUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(PartnerUser createdBy) {
		this.createdBy = createdBy;
	}

	public Vector<ApplicationSubscriptionModule> getModules() {
		return modules;
	}

	public void setModules(Vector<ApplicationSubscriptionModule> modules) {
		this.modules = modules;
	}

	public Vector<ApplicationSubscriptionReport> getReports() {
		return reports;
	}

	public void setReports(Vector<ApplicationSubscriptionReport> reports) {
		this.reports = reports;
	}
	
}
