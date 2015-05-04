package com.dexter.fms.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class MRoleReport implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
	private Long id;
	@ManyToOne
	private MRole role;
	@ManyToOne
	private Report report;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public MRoleReport()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public MRole getRole() {
		return role;
	}

	public void setRole(MRole role) {
		this.role = role;
	}

	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = report;
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
