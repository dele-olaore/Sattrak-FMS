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
public class PartnerDriverQuery implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private PartnerDriver driver;
	
	@Temporal(TemporalType.DATE)
	private Date tranDate;
	private String queryRemarks;
	private String queryGrade; // LOW/MEDIUM/HIGH
	
	private String driverResponse; // driver's own comment on the query
	@Temporal(TemporalType.TIMESTAMP)
	private Date driverResponseDate, finalQueryRemarksDate;
	private String finalQueryRemarks;
	private boolean punishDriver;
	
	private String status; // PENDING, DRIVER_RESPONDED, CLOSED
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public PartnerDriverQuery()
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

	public String getQueryRemarks() {
		return queryRemarks;
	}

	public void setQueryRemarks(String queryRemarks) {
		this.queryRemarks = queryRemarks;
	}

	public String getQueryGrade() {
		return queryGrade;
	}

	public void setQueryGrade(String queryGrade) {
		this.queryGrade = queryGrade;
	}

	public String getDriverResponse() {
		return driverResponse;
	}

	public void setDriverResponse(String driverResponse) {
		this.driverResponse = driverResponse;
	}

	public Date getDriverResponseDate() {
		return driverResponseDate;
	}

	public void setDriverResponseDate(Date driverResponseDate) {
		this.driverResponseDate = driverResponseDate;
	}

	public Date getFinalQueryRemarksDate() {
		return finalQueryRemarksDate;
	}

	public void setFinalQueryRemarksDate(Date finalQueryRemarksDate) {
		this.finalQueryRemarksDate = finalQueryRemarksDate;
	}

	public String getFinalQueryRemarks() {
		return finalQueryRemarks;
	}

	public void setFinalQueryRemarks(String finalQueryRemarks) {
		this.finalQueryRemarks = finalQueryRemarks;
	}

	public boolean isPunishDriver() {
		return punishDriver;
	}

	public void setPunishDriver(boolean punishDriver) {
		this.punishDriver = punishDriver;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
