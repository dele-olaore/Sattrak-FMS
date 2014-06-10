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
public class Partner implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	private String name;
	private String code;
	
	private boolean isSattrak;
	private String fuelingType; // Manual, Automated, Both
	
	private String addr1;
	private String addr2;
	private String phone;
	private String email;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	@Transient
	private Vector<PartnerUser> users;
	@Transient
	private Vector<PartnerSubscription> subscriptions;
	@Transient
	private PartnerLicense license;
	
	public Partner()
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public boolean isSattrak() {
		return isSattrak;
	}

	public void setSattrak(boolean isSattrak) {
		this.isSattrak = isSattrak;
	}

	public String getFuelingType() {
		return fuelingType;
	}

	public void setFuelingType(String fuelingType) {
		this.fuelingType = fuelingType;
	}

	public String getAddr1() {
		return addr1;
	}

	public void setAddr1(String addr1) {
		this.addr1 = addr1;
	}

	public String getAddr2() {
		return addr2;
	}

	public void setAddr2(String addr2) {
		this.addr2 = addr2;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public Vector<PartnerUser> getUsers() {
		return users;
	}

	public void setUsers(Vector<PartnerUser> users) {
		this.users = users;
	}

	public Vector<PartnerSubscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(Vector<PartnerSubscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public PartnerLicense getLicense() {
		return license;
	}

	public void setLicense(PartnerLicense license) {
		this.license = license;
	}
	
}
