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

import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.ref.Department;
import com.dexter.fms.model.ref.Unit;

@Entity
public class Expense implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private ExpenseType type;
	private String remarks;
	private double amount;
	@Temporal(TemporalType.TIMESTAMP)
	private Date expense_dt;
	
	@ManyToOne
	private ExpenseRequest expR;
	
	@ManyToOne
	private Partner partner;
	@ManyToOne
	private Vehicle vehicle;
	@ManyToOne
	private PartnerPersonel personel;
	@ManyToOne
	private Department misDepartment;
	@ManyToOne
	private Unit misUnit;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public Expense()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ExpenseType getType() {
		return type;
	}

	public void setType(ExpenseType type) {
		this.type = type;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Date getExpense_dt() {
		return expense_dt;
	}

	public void setExpense_dt(Date expense_dt) {
		this.expense_dt = expense_dt;
	}

	public ExpenseRequest getExpR() {
		return expR;
	}

	public void setExpR(ExpenseRequest expR) {
		this.expR = expR;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public PartnerPersonel getPersonel() {
		return personel;
	}

	public void setPersonel(PartnerPersonel personel) {
		this.personel = personel;
	}

	public Department getMisDepartment() {
		return misDepartment;
	}

	public void setMisDepartment(Department misDepartment) {
		this.misDepartment = misDepartment;
	}

	public Unit getMisUnit() {
		return misUnit;
	}

	public void setMisUnit(Unit misUnit) {
		this.misUnit = misUnit;
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
