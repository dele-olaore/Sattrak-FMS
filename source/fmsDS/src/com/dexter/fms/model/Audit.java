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
public class Audit implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private PartnerUser user;
	@ManyToOne
	private MFunction function;
	private String narration;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date action_dt;
	
	public Audit()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PartnerUser getUser() {
		return user;
	}

	public void setUser(PartnerUser user) {
		this.user = user;
	}

	public MFunction getFunction() {
		return function;
	}

	public void setFunction(MFunction function) {
		this.function = function;
	}

	public String getNarration() {
		return narration;
	}

	public void setNarration(String narration) {
		this.narration = narration;
	}

	public Date getAction_dt() {
		return action_dt;
	}

	public void setAction_dt(Date action_dt) {
		this.action_dt = action_dt;
	}
	
}
