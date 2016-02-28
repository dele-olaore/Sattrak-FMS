package com.dexter.fms.model.fuelcard;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="TRACKERLASTID_TB")
public class TrackerLastID implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
    @NotNull
    @GeneratedValue
	private Long id;
	
	private Long lastTrackerID;
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date tranTime;
	
	public TrackerLastID()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getLastTrackerID() {
		return lastTrackerID;
	}

	public void setLastTrackerID(Long lastTrackerID) {
		this.lastTrackerID = lastTrackerID;
	}

	public Date getTranTime() {
		return tranTime;
	}

	public void setTranTime(Date tranTime) {
		this.tranTime = tranTime;
	}
	
}
