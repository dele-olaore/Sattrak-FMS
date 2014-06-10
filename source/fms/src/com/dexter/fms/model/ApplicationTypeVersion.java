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
public class ApplicationTypeVersion implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	@ManyToOne
	private ApplicationType appType;
	private String versionName;
	private String description;

	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	@Transient
	private boolean selected;
	
	@Transient
	private Vector<Module> modules;
	@Transient
	private Vector<Report> reports;
	
	public ApplicationTypeVersion()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ApplicationType getAppType() {
		return appType;
	}

	public void setAppType(ApplicationType appType) {
		this.appType = appType;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
	
	public Vector<Module> getModules() {
		if(modules == null)
			modules = new Vector<Module>();
		return modules;
	}

	public void setModules(Vector<Module> modules) {
		this.modules = modules;
	}

	public Vector<Report> getReports() {
		if(reports == null)
			reports = new Vector<Report>();
		return reports;
	}

	public void setReports(Vector<Report> reports) {
		this.reports = reports;
	}
}
