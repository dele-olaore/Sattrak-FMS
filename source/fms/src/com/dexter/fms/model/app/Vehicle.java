package com.dexter.fms.model.app;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.dto.TrackerEventSummary;
import com.dexter.fms.dto.VehicleBehaviorSummary;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.ref.EngineCapacity;
import com.dexter.fms.model.ref.Region;
import com.dexter.fms.model.ref.VehicleModel;
import com.dexter.fms.model.ref.Vendor;

@Entity
public class Vehicle implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
    @GeneratedValue
	private Long id;
	
	@ManyToOne
	private VehicleModel model;
	
	private String zonControlId;
	@Column(unique=true)
	private String registrationNo;
	private String engineNo;
	@ManyToOne
	private EngineCapacity engineCapacity;
	private String chasisNo;
	@Temporal(TemporalType.DATE)
	private Date purchaseDate;
	private BigDecimal purchaseAmt;
	@ManyToOne
	private Vendor vendor;
	
	@ManyToOne
	private Fleet fleet;
	@ManyToOne
	private Partner partner;
	@ManyToOne
	private PartnerPersonel assignee; // this has to be the personel the vehicle was assigned to. But the way it is, it has to be like the driver way so we can keep history
	
	private boolean active;
	private String activeStatus;
	
	private boolean disposalAlertSent; // marked when alert for this vehicle to be disposed is already sent
	private String disposalStatus; // Disposable status of this vehicle
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	@ManyToOne
	private PartnerUser createdBy;
	
	@Transient
	private VehicleDriver currentDriver;
	@Transient
	private Vector<VehicleDriver> drivers;
	@Transient
	private Vector<VehicleParameters> params;
	@Transient
	private Vector<VehicleTrackerData> trackerData;
	@Transient
	private Vector<VehicleTrackerEventData> trackerEventData;
	@Transient
	private Vector<TrackerEventSummary> trackerEventSummary;
	@Transient
	private Vector<VehicleBehaviour> vehicleBehaviorData;
	@Transient
	private Vector<VehicleBehaviorSummary> vehicleBehaviorSummary;
	@Transient
	private boolean selected;
	@Transient
	private int age, ageInMonths; // age in years, this should be purchased date - current date or should it be model year - cur year
	
	@Transient
	private VehicleLicense last_lic;
	@Transient
	private VehicleLicense last_insur;
	@Transient
	private BigDecimal maint_odometer, sales;
	@Transient
	private VehicleRoutineMaintenance last_rout_maint;
	@Transient
	private VehicleRoutineMaintenanceSetup rout_setup;
	
	@Transient
	private Region region;
	
	@Transient
	private double distance_covered, max_speed, average_speed, maint_cost, fuel_cost, other_cost, total_cost;
	@Transient
	private String utilizationStatus;
	@Transient
	private int trips, no_of_trips, no_of_passengers, no_of_drivers;
	@Transient
	private double distance, fuel_consumed, km_per_liter, working_time;
	@Transient
	private Date salesDate;
	
	public Vehicle()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public VehicleModel getModel() {
		return model;
	}

	public void setModel(VehicleModel model) {
		this.model = model;
	}

	public String getZonControlId() {
		return zonControlId;
	}

	public void setZonControlId(String zonControlId) {
		this.zonControlId = zonControlId;
	}

	public String getRegistrationNo() {
		return registrationNo;
	}

	public void setRegistrationNo(String registrationNo) {
		this.registrationNo = registrationNo;
	}

	public String getEngineNo() {
		return engineNo;
	}

	public void setEngineNo(String engineNo) {
		this.engineNo = engineNo;
	}

	public EngineCapacity getEngineCapacity() {
		return engineCapacity;
	}

	public void setEngineCapacity(EngineCapacity engineCapacity) {
		this.engineCapacity = engineCapacity;
	}

	public String getChasisNo() {
		return chasisNo;
	}

	public void setChasisNo(String chasisNo) {
		this.chasisNo = chasisNo;
	}

	public Date getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public BigDecimal getPurchaseAmt() {
		return purchaseAmt;
	}

	public void setPurchaseAmt(BigDecimal purchaseAmt) {
		this.purchaseAmt = purchaseAmt;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public Fleet getFleet() {
		return fleet;
	}

	public void setFleet(Fleet fleet) {
		this.fleet = fleet;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getActiveStatus() {
		return activeStatus;
	}

	public void setActiveStatus(String activeStatus) {
		this.activeStatus = activeStatus;
	}

	public boolean isDisposalAlertSent() {
		return disposalAlertSent;
	}

	public void setDisposalAlertSent(boolean disposalAlertSent) {
		this.disposalAlertSent = disposalAlertSent;
	}

	public String getDisposalStatus() {
		return disposalStatus;
	}

	public void setDisposalStatus(String disposalStatus) {
		this.disposalStatus = disposalStatus;
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

	public VehicleDriver getCurrentDriver() {
		return currentDriver;
	}

	public void setCurrentDriver(VehicleDriver currentDriver) {
		this.currentDriver = currentDriver;
	}

	public Vector<VehicleDriver> getDrivers() {
		return drivers;
	}

	public void setDrivers(Vector<VehicleDriver> drivers) {
		this.drivers = drivers;
	}

	public Vector<VehicleParameters> getParams() {
		return params;
	}

	public void setParams(Vector<VehicleParameters> params) {
		this.params = params;
	}

	public Vector<VehicleTrackerData> getTrackerData() {
		return trackerData;
	}

	public void setTrackerData(Vector<VehicleTrackerData> trackerData) {
		this.trackerData = trackerData;
	}

	public Vector<VehicleTrackerEventData> getTrackerEventData() {
		return trackerEventData;
	}

	public void setTrackerEventData(Vector<VehicleTrackerEventData> trackerEventData) {
		this.trackerEventData = trackerEventData;
	}

	public Vector<TrackerEventSummary> getTrackerEventSummary() {
		return trackerEventSummary;
	}

	public void setTrackerEventSummary(
			Vector<TrackerEventSummary> trackerEventSummary) {
		this.trackerEventSummary = trackerEventSummary;
	}

	public Vector<VehicleBehaviour> getVehicleBehaviorData() {
		return vehicleBehaviorData;
	}

	public void setVehicleBehaviorData(Vector<VehicleBehaviour> vehicleBehaviorData) {
		this.vehicleBehaviorData = vehicleBehaviorData;
	}

	public Vector<VehicleBehaviorSummary> getVehicleBehaviorSummary() {
		return vehicleBehaviorSummary;
	}

	public void setVehicleBehaviorSummary(
			Vector<VehicleBehaviorSummary> vehicleBehaviorSummary) {
		this.vehicleBehaviorSummary = vehicleBehaviorSummary;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public int getAge() {
		if(getPurchaseDate() != null)
		{
			Calendar c = Calendar.getInstance(), cNow = Calendar.getInstance();
			c.setTime(getPurchaseDate());
			age = cNow.get(Calendar.YEAR) - c.get(Calendar.YEAR);
		}
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getAgeInMonths() {
		if(getPurchaseDate() != null)
		{
			long day = (1000 * 60 * 60 * 24); // 24 hours in milliseconds
			long time = day * 30; // 30 days for a month
			
			long pdtime = getPurchaseDate().getTime();
			ageInMonths = Integer.parseInt(""+(pdtime/time));
		}
		return ageInMonths;
	}

	public void setAgeInMonths(int ageInMonths) {
		this.ageInMonths = ageInMonths;
	}

	public VehicleLicense getLast_lic() {
		return last_lic;
	}

	public void setLast_lic(VehicleLicense last_lic) {
		this.last_lic = last_lic;
	}

	public VehicleLicense getLast_insur() {
		return last_insur;
	}

	public void setLast_insur(VehicleLicense last_insur) {
		this.last_insur = last_insur;
	}

	public VehicleRoutineMaintenance getLast_rout_maint() {
		return last_rout_maint;
	}

	public void setLast_rout_maint(VehicleRoutineMaintenance last_rout_maint) {
		this.last_rout_maint = last_rout_maint;
	}

	public VehicleRoutineMaintenanceSetup getRout_setup() {
		if(rout_setup == null)
			rout_setup = new VehicleRoutineMaintenanceSetup();
		return rout_setup;
	}

	public void setRout_setup(VehicleRoutineMaintenanceSetup rout_setup) {
		this.rout_setup = rout_setup;
	}

	public BigDecimal getMaint_odometer() {
		return maint_odometer;
	}

	public void setMaint_odometer(BigDecimal maint_odometer) {
		this.maint_odometer = maint_odometer;
	}

	public BigDecimal getSales() {
		return sales;
	}

	public void setSales(BigDecimal sales) {
		this.sales = sales;
	}

	public PartnerPersonel getAssignee() {
		return assignee;
	}

	public void setAssignee(PartnerPersonel assignee) {
		this.assignee = assignee;
	}

	@SuppressWarnings("unchecked")
	public Region getRegion() {
		if(region == null) {
			Hashtable<String, Object> params = new Hashtable<String, Object>();
            params.put("vehicle", this);
            GeneralDAO gDAO = new GeneralDAO();
            Object obj = gDAO.search("VehicleParameters", params);
            if(obj != null) {
            	List<VehicleParameters> vplist = (List<VehicleParameters>)obj;
            	for(VehicleParameters vp : vplist) {
            		region = vp.getRegion();
            	}
            }
            gDAO.destroy();
		}
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public double getDistance_covered() {
		return distance_covered;
	}

	public void setDistance_covered(double distance_covered) {
		this.distance_covered = distance_covered;
	}

	public int getTrips() {
		return trips;
	}

	public void setTrips(int trips) {
		this.trips = trips;
	}

	public double getMax_speed() {
		return max_speed;
	}

	public void setMax_speed(double max_speed) {
		this.max_speed = max_speed;
	}

	public double getAverage_speed() {
		return average_speed;
	}

	public void setAverage_speed(double average_speed) {
		this.average_speed = average_speed;
	}

	public double getMaint_cost() {
		return maint_cost;
	}

	public void setMaint_cost(double maint_cost) {
		this.maint_cost = maint_cost;
	}

	public double getFuel_cost() {
		return fuel_cost;
	}

	public void setFuel_cost(double fuel_cost) {
		this.fuel_cost = fuel_cost;
	}

	public double getOther_cost() {
		return other_cost;
	}

	public void setOther_cost(double other_cost) {
		this.other_cost = other_cost;
	}

	public double getTotal_cost() {
		return total_cost;
	}

	public void setTotal_cost(double total_cost) {
		this.total_cost = total_cost;
	}

	public String getUtilizationStatus() {
		return utilizationStatus;
	}

	public void setUtilizationStatus(String utilizationStatus) {
		this.utilizationStatus = utilizationStatus;
	}

	public int getNo_of_trips() {
		return no_of_trips;
	}

	public void setNo_of_trips(int no_of_trips) {
		this.no_of_trips = no_of_trips;
	}

	public int getNo_of_passengers() {
		return no_of_passengers;
	}

	public void setNo_of_passengers(int no_of_passengers) {
		this.no_of_passengers = no_of_passengers;
	}

	public int getNo_of_drivers() {
		return no_of_drivers;
	}

	public void setNo_of_drivers(int no_of_drivers) {
		this.no_of_drivers = no_of_drivers;
	}

	public double getWorking_time() {
		return working_time;
	}

	public void setWorking_time(double working_time) {
		this.working_time = working_time;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getFuel_consumed() {
		return fuel_consumed;
	}

	public void setFuel_consumed(double fuel_consumed) {
		this.fuel_consumed = fuel_consumed;
	}

	public double getKm_per_liter() {
		return km_per_liter;
	}

	public void setKm_per_liter(double km_per_liter) {
		this.km_per_liter = km_per_liter;
	}

	public Date getSalesDate() {
		return salesDate;
	}

	public void setSalesDate(Date salesDate) {
		this.salesDate = salesDate;
	}
	
}
