package com.dexter.fms.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.dexter.fms.model.ref.Department;
import com.dexter.fms.model.ref.Region;
import com.dexter.fms.model.ref.Unit;

@Entity
public class PartnerPersonel implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue
	private Long id;
	private String staff_id;
	private String firstname;
	private String lastname;
	
	private String phone;
	private String email;
	
	private String nok_name;
	private String nok_address;
	private String nok_phone;
	private String nok_email;
	
	private byte[] photo;
	
	@ManyToOne
	private Department department;
	@ManyToOne
	private Unit unit;
	private boolean unitHead;
	private boolean fleetManager;
	@ManyToOne
	private Region region;
	
	@ManyToOne
	private PartnerPersonel reportsTo;
	
	private String fileno;
	@Temporal(TemporalType.DATE)
	private Date dob;
	private int age;
	private String position;
	@Temporal(TemporalType.DATE)
	private Date hiredDate;
	private String address;
	
	private boolean hasUser;
	private boolean hasDriver;
	
	private String availabilityStatus; // AVAILABLE, ON LEAVE, SUSPENDED, NOT-AVAILABLE, SACKED, RESIGNED
	
	@ManyToOne
	private Partner partner;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	@Transient
	private boolean selected;
	
	public PartnerPersonel()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStaff_id() {
		return staff_id;
	}

	public void setStaff_id(String staff_id) {
		this.staff_id = staff_id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
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

	public String getNok_name() {
		return nok_name;
	}

	public void setNok_name(String nok_name) {
		this.nok_name = nok_name;
	}

	public String getNok_address() {
		return nok_address;
	}

	public void setNok_address(String nok_address) {
		this.nok_address = nok_address;
	}

	public String getNok_phone() {
		return nok_phone;
	}

	public void setNok_phone(String nok_phone) {
		this.nok_phone = nok_phone;
	}

	public String getNok_email() {
		return nok_email;
	}

	public void setNok_email(String nok_email) {
		this.nok_email = nok_email;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public boolean isUnitHead() {
		return unitHead;
	}

	public void setUnitHead(boolean unitHead) {
		this.unitHead = unitHead;
	}

	public boolean isFleetManager() {
		return fleetManager;
	}

	public void setFleetManager(boolean fleetManager) {
		this.fleetManager = fleetManager;
	}

	public PartnerPersonel getReportsTo() {
		return reportsTo;
	}

	public void setReportsTo(PartnerPersonel reportsTo) {
		this.reportsTo = reportsTo;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public String getFileno() {
		return fileno;
	}

	public void setFileno(String fileno) {
		this.fileno = fileno;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		//this.age = age;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public Date getHiredDate() {
		return hiredDate;
	}

	public void setHiredDate(Date hiredDate) {
		this.hiredDate = hiredDate;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isHasUser() {
		return hasUser;
	}

	public void setHasUser(boolean hasUser) {
		this.hasUser = hasUser;
	}

	public boolean isHasDriver() {
		return hasDriver;
	}

	public void setHasDriver(boolean hasDriver) {
		this.hasDriver = hasDriver;
	}

	public String getAvailabilityStatus() {
		return availabilityStatus;
	}

	public void setAvailabilityStatus(String availabilityStatus) {
		this.availabilityStatus = availabilityStatus;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
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

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public Date getDob() {
		return dob;
	}

	public void setDob(Date dob) {
		this.dob = dob;
		if(dob != null)
		{
			try
			{
				Calendar cdob = Calendar.getInstance(), cnow = Calendar.getInstance();
				cdob.setTime(dob);
				long ayearInMillis = 1000L*60L*60L*24L*365L;
				long ageInMillis = cnow.getTimeInMillis() - cdob.getTimeInMillis();
				long ageL = ageInMillis/ayearInMillis;
				age = Integer.parseInt(String.valueOf(ageL));
			} catch(Exception ex){}
		}
	}
	
}
