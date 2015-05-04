package com.dexter.fms.model.app;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.dexter.fms.model.PartnerUser;

@Entity
public class WorkOrderItem implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue
	private Long id;
	@ManyToOne
	private WorkOrderVehicle workOrderVehicle;
	@ManyToOne
	private Item item;
	
	private int count;
	private String action; // REPAIR, REPLACE or REMOVE
	
	private double initEstAmount;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	@ManyToOne
	private PartnerUser createdBy;
	
	public WorkOrderItem()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public WorkOrderVehicle getWorkOrderVehicle() {
		return workOrderVehicle;
	}

	public void setWorkOrderVehicle(WorkOrderVehicle workOrderVehicle) {
		this.workOrderVehicle = workOrderVehicle;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public double getInitEstAmount() {
		return initEstAmount;
	}

	public void setInitEstAmount(double initEstAmount) {
		this.initEstAmount = initEstAmount;
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
