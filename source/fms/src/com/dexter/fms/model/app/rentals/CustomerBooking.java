package com.dexter.fms.model.app.rentals;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.ref.RentalServiceType;
import com.dexter.fms.model.ref.VehicleType;

@Entity
public class CustomerBooking implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private Customer customer;
	@Temporal(TemporalType.TIMESTAMP)
	private Date book_dt;
	@Temporal(TemporalType.TIMESTAMP)
	private Date end_dt;
	private String pickupLocation;
	private String destination;
	private int noOfPersons;
	private String otherComment;
	@ManyToOne
	private VehicleType vehicleType;
	@ManyToOne
	private RentalServiceType serviceType;
	private String durationType;
	private int durationCount;
	
	private double amount;
	private byte[] invoice;
	
	private boolean paid;
	
	@ManyToOne
	private Vehicle vehicle;
	@ManyToOne
	private PartnerDriver driver;
	
	private String status; // PENDING_PAYMENT, AWAITING, ONGOING, COMPLETED, CANCELED
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	public CustomerBooking()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Date getBook_dt() {
		return book_dt;
	}

	public void setBook_dt(Date book_dt) {
		this.book_dt = book_dt;
	}

	public Date getEnd_dt() {
		return end_dt;
	}

	public void setEnd_dt(Date end_dt) {
		this.end_dt = end_dt;
	}

	public String getPickupLocation() {
		return pickupLocation;
	}

	public void setPickupLocation(String pickupLocation) {
		this.pickupLocation = pickupLocation;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public int getNoOfPersons() {
		return noOfPersons;
	}

	public void setNoOfPersons(int noOfPersons) {
		this.noOfPersons = noOfPersons;
	}

	public String getOtherComment() {
		return otherComment;
	}

	public void setOtherComment(String otherComment) {
		this.otherComment = otherComment;
	}

	public VehicleType getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(VehicleType vehicleType) {
		this.vehicleType = vehicleType;
	}

	public RentalServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(RentalServiceType serviceType) {
		this.serviceType = serviceType;
	}

	public String getDurationType() {
		return durationType;
	}

	public void setDurationType(String durationType) {
		this.durationType = durationType;
	}

	public int getDurationCount() {
		return durationCount;
	}

	public void setDurationCount(int durationCount) {
		this.durationCount = durationCount;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public byte[] getInvoice() {
		return invoice;
	}

	public void setInvoice(byte[] invoice) {
		this.invoice = invoice;
	}

	public boolean isPaid() {
		return paid;
	}

	public void setPaid(boolean paid) {
		this.paid = paid;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public PartnerDriver getDriver() {
		return driver;
	}

	public void setDriver(PartnerDriver driver) {
		this.driver = driver;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
