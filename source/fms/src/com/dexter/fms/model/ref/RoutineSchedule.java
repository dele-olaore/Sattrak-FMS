package com.dexter.fms.model.ref;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.dexter.fms.model.PartnerUser;

@Entity
public class RoutineSchedule implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private VehicleModel vmodel;
	
	/*
	 * I need to figure out how to handle this. 
	 * I need to consider km interval and also the age of the vehicle from purchased date.
	 * */
	
	private BigDecimal min_odometer, max_odometer; // at what odometer is this maintenance required
	private long age_in_weeks;
	private String description; // what maintenance should be done
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public RoutineSchedule()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public VehicleModel getVmodel() {
		return vmodel;
	}

	public void setVmodel(VehicleModel vmodel) {
		this.vmodel = vmodel;
	}

	public BigDecimal getMin_odometer() {
		return min_odometer;
	}

	public void setMin_odometer(BigDecimal min_odometer) {
		this.min_odometer = min_odometer;
	}

	public BigDecimal getMax_odometer() {
		return max_odometer;
	}

	public void setMax_odometer(BigDecimal max_odometer) {
		this.max_odometer = max_odometer;
	}

	public long getAge_in_weeks() {
		return age_in_weeks;
	}

	public void setAge_in_weeks(long age_in_weeks) {
		this.age_in_weeks = age_in_weeks;
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
	
}
