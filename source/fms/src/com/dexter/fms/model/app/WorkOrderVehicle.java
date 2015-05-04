package com.dexter.fms.model.app;

import java.io.Serializable;
import java.math.BigDecimal;
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

import com.dexter.fms.model.PartnerUser;

@Entity
public class WorkOrderVehicle implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	@ManyToOne
	private WorkOrder workOrder;
	@ManyToOne
	private Vehicle vehicle;
	private BigDecimal currentVehOdometer;
	private String detailsOfWork;
	
	private double initEstAmount;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	@ManyToOne
	private PartnerUser createdBy;
	
	@Transient
	private Vector<WorkOrderItem> items;
	@Transient
	private WorkOrderItem selectedItem;
	@Transient
	private String itemsToRepair, itemsToReplace;
	@Transient
	private BigDecimal odometer, initial_amount, closed_amount, interval_odometer, alert_interval_odometer;
	@Transient
	private Date close_dt;
	@Transient
	private boolean nextRMSetup;
	
	public WorkOrderVehicle()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public WorkOrder getWorkOrder() {
		return workOrder;
	}

	public void setWorkOrder(WorkOrder workOrder) {
		this.workOrder = workOrder;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public BigDecimal getCurrentVehOdometer() {
		return currentVehOdometer;
	}

	public void setCurrentVehOdometer(BigDecimal currentVehOdometer) {
		this.currentVehOdometer = currentVehOdometer;
	}

	public String getDetailsOfWork() {
		return detailsOfWork;
	}

	public void setDetailsOfWork(String detailsOfWork) {
		this.detailsOfWork = detailsOfWork;
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

	public Vector<WorkOrderItem> getItems() {
		if(items == null)
			items = new Vector<WorkOrderItem>();
		return items;
	}

	public void setItems(Vector<WorkOrderItem> items) {
		this.items = items;
		
		if(items != null && items.size() > 0)
		{
			String toRepair = "", toReplace = "";
			int toRepairCount = 0, toReplaceCount = 0;
			for(WorkOrderItem e : items)
			{
				if(e.getAction().equalsIgnoreCase("REPAIR"))
				{
					toRepair += e.getItem().getName() + ", ";
					toRepairCount+=1;
				}
				else if(e.getAction().equalsIgnoreCase("REPLACE"))
				{
					toReplace += e.getItem().getName() + ", ";
					toReplaceCount+=1;
				}
			}
			setItemsToRepair(toRepairCount + " (" + toRepair + ")");
			setItemsToReplace(toReplaceCount + " (" + toReplace + ")");
		}
	}

	public WorkOrderItem getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(WorkOrderItem selectedItem) {
		this.selectedItem = selectedItem;
	}

	public String getItemsToRepair() {
		return itemsToRepair;
	}

	public void setItemsToRepair(String itemsToRepair) {
		this.itemsToRepair = itemsToRepair;
	}

	public String getItemsToReplace() {
		return itemsToReplace;
	}

	public void setItemsToReplace(String itemsToReplace) {
		this.itemsToReplace = itemsToReplace;
	}

	public BigDecimal getOdometer() {
		return odometer;
	}

	public void setOdometer(BigDecimal odometer) {
		this.odometer = odometer;
	}

	public BigDecimal getInitial_amount() {
		return initial_amount;
	}

	public void setInitial_amount(BigDecimal initial_amount) {
		this.initial_amount = initial_amount;
	}

	public BigDecimal getClosed_amount() {
		return closed_amount;
	}

	public void setClosed_amount(BigDecimal closed_amount) {
		this.closed_amount = closed_amount;
	}

	public BigDecimal getInterval_odometer() {
		return interval_odometer;
	}

	public void setInterval_odometer(BigDecimal interval_odometer) {
		this.interval_odometer = interval_odometer;
	}

	public BigDecimal getAlert_interval_odometer() {
		return alert_interval_odometer;
	}

	public void setAlert_interval_odometer(BigDecimal alert_interval_odometer) {
		this.alert_interval_odometer = alert_interval_odometer;
	}

	public Date getClose_dt() {
		return close_dt;
	}

	public void setClose_dt(Date close_dt) {
		this.close_dt = close_dt;
	}

	public boolean isNextRMSetup() {
		return nextRMSetup;
	}

	public void setNextRMSetup(boolean nextRMSetup) {
		this.nextRMSetup = nextRMSetup;
	}
	
}
