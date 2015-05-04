package com.dexter.fms.model.app;

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
public class VehicleTrackerEventData implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private Vehicle vehicle;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date captured_dt;
	
	private String event_type, unit_id, event_time, event_name, event_text, event_value, h_address, h_distance, unit_name;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	public VehicleTrackerEventData()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public Date getCaptured_dt() {
		return captured_dt;
	}

	public void setCaptured_dt(Date captured_dt) {
		this.captured_dt = captured_dt;
	}

	public String getEvent_type() {
		return event_type;
	}

	public void setEvent_type(String event_type) {
		this.event_type = event_type;
	}

	public String getUnit_id() {
		return unit_id;
	}

	public void setUnit_id(String unit_id) {
		this.unit_id = unit_id;
	}

	public String getEvent_time() {
		return event_time;
	}

	public void setEvent_time(String event_time) {
		this.event_time = event_time;
	}

	public String getEvent_name() {
		return event_name;
	}

	public void setEvent_name(String event_name) {
		this.event_name = event_name;
	}

	public String getEvent_text() {
		return event_text;
	}

	public void setEvent_text(String event_text) {
		this.event_text = event_text;
	}

	public String getEvent_value() {
		return event_value;
	}

	public void setEvent_value(String event_value) {
		this.event_value = event_value;
	}

	public String getH_address() {
		return h_address;
	}

	public void setH_address(String h_address) {
		this.h_address = h_address;
	}

	public String getH_distance() {
		return h_distance;
	}

	public void setH_distance(String h_distance) {
		this.h_distance = h_distance;
	}

	public String getUnit_name() {
		return unit_name;
	}

	public void setUnit_name(String unit_name) {
		this.unit_name = unit_name;
	}

	public Date getCrt_dt() {
		return crt_dt;
	}

	public void setCrt_dt(Date crt_dt) {
		this.crt_dt = crt_dt;
	}
	
}
