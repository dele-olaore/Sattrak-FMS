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
public class VehicleRepairItem implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private VehicleRepair repair;
	@ManyToOne
	private Item item;
	
	private String repairType; // REPAIR / REPLACE
	private double repairCost; // cost of repair or replace
	private double surveyCostAtRepairTime; // the survey cost of the item as at this repair time
	private String repairDescription;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public VehicleRepairItem()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public VehicleRepair getRepair() {
		return repair;
	}

	public void setRepair(VehicleRepair repair) {
		this.repair = repair;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public String getRepairType() {
		return repairType;
	}

	public void setRepairType(String repairType) {
		this.repairType = repairType;
	}

	public double getRepairCost() {
		return repairCost;
	}

	public void setRepairCost(double repairCost) {
		this.repairCost = repairCost;
	}

	public double getSurveyCostAtRepairTime() {
		return surveyCostAtRepairTime;
	}

	public void setSurveyCostAtRepairTime(double surveyCostAtRepairTime) {
		this.surveyCostAtRepairTime = surveyCostAtRepairTime;
	}

	public String getRepairDescription() {
		return repairDescription;
	}

	public void setRepairDescription(String repairDescription) {
		this.repairDescription = repairDescription;
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
