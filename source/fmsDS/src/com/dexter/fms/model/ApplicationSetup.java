package com.dexter.fms.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ApplicationSetup implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue
	private Long id;
	
	private String appName;
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	private String version;
	private boolean appSetup;
	
	public ApplicationSetup()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Date getCrt_dt() {
		return crt_dt;
	}

	public void setCrt_dt(Date crt_dt) {
		this.crt_dt = crt_dt;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isAppSetup() {
		return appSetup;
	}

	public void setAppSetup(boolean appSetup) {
		this.appSetup = appSetup;
	}
	
}
