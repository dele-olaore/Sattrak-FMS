package com.dexter.fms.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class PartnerUserSetting implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private PartnerUser user;
	
	private String theme;
	private String headercolor;
	
	public PartnerUserSetting()
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

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getHeadercolor() {
		return headercolor;
	}

	public void setHeadercolor(String headercolor) {
		this.headercolor = headercolor;
	}
	
}
