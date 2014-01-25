package com.dexter.fms.mbean;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.persistence.Query;

import org.primefaces.model.UploadedFile;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.app.DashboardVehicle;
import com.dexter.fms.model.app.Expense;
import com.dexter.fms.model.app.ExpenseType;
import com.dexter.fms.model.app.Fleet;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleAccident;
import com.dexter.fms.model.app.VehicleAdHocMaintenance;
import com.dexter.fms.model.app.VehicleAdHocMaintenanceRequest;
import com.dexter.fms.model.app.VehicleDriver;
import com.dexter.fms.model.app.VehicleFuelData;
import com.dexter.fms.model.app.VehicleFueling;
import com.dexter.fms.model.app.VehicleLicense;
import com.dexter.fms.model.app.VehicleLocationData;
import com.dexter.fms.model.app.VehicleOdometerData;
import com.dexter.fms.model.app.VehicleParameters;
import com.dexter.fms.model.app.VehicleRoutineMaintenance;
import com.dexter.fms.model.app.VehicleRoutineMaintenanceSetup;
import com.dexter.fms.model.app.VehicleStatusEnum;
import com.dexter.fms.model.app.VehicleTrackerData;
import com.dexter.fms.model.ref.Department;
import com.dexter.fms.model.ref.FuelType;
import com.dexter.fms.model.ref.LicenseType;
import com.dexter.fms.model.ref.Region;
import com.dexter.fms.model.ref.ServiceType;
import com.dexter.fms.model.ref.TransactionType;
import com.dexter.fms.model.ref.VehicleMake;
import com.dexter.fms.model.ref.VehicleModel;
import com.dexter.fms.model.ref.VehicleType;
import com.dexter.fms.model.ref.Vendor;
import com.dexter.fms.model.ref.VendorServices;

@ManagedBean(name = "fleetBean")
@SessionScoped
public class FleetMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-FleetMBean");
	
	private FacesMessage msg = null;
	
	private Long partner_id;
	private Partner partner;
	
	private Long vehicleType_id;
	private Long vehicleMake_id;
	private VehicleType vehicleType;
	private Vector<VehicleType> vehicleTypes;
	private VehicleMake vehicleMake;
	private Vector<VehicleMake> vehicleMakes;
	private VehicleModel vmodel;
	private Vector<VehicleModel> vmodels;
	
	private Fleet fleet;
	private Vector<Fleet> fleets;
	
	private Long fleet_id;
	private Long vehicleModel_id;
	private Long vendor_id;
	private Long department_id;
	private Long region_id;
	private Long fuelType_id;
	private Vector<Department> depts;
	private Vector<Region> regions;
	private Vector<FuelType> fuelTypes;
	
	private UploadedFile vehiclePhoto;
	private Vector<Vendor> vsalesVendors, vserviceVendors, vrepairVendors, vinsuranceVendors, vlicensesVendors;
	private Vehicle vehicle, selectedVehicle;
	private VehicleParameters vehicleParam;
	private Vector<Vehicle> vehicles, tvehicles, tvehicles2;
	
	private Long driver_id;
	private Vector<PartnerDriver> partnerDrivers;
	
	private Long vehicle_id;
	private String regNo;
	private Date stdt, eddt;
	
	private boolean nextRMSetup;
	private VehicleRoutineMaintenanceSetup routineSetup;
	private VehicleRoutineMaintenance routine;
	private Vector<VehicleRoutineMaintenance> routines;
	
	private VehicleAdHocMaintenanceRequest adhocRequest;
	private Vector<VehicleAdHocMaintenanceRequest> adHocRequests;
	private VehicleAdHocMaintenance adhocMain;
	private Vector<VehicleAdHocMaintenance> adhocMains;
	
	private Long insuranceComp_id, repairComp_id;
	private UploadedFile accidentPhoto, repairedPhoto, accidentDocument;
	private boolean accidentStatus;
	private VehicleAccident accident;
	private Vector<VehicleAccident> accidents;
	
	private MapModel vtrackingModel;
	private Marker marker;
	private Vector<VehicleLocationData> vehiclesLocs;
	private String defaultCenterCoor = "6.427887,3.4287645";
	private String centerCoor;
	private int vtrackpollinterval = 120;
	
	private VehicleFueling fueling;
	private Vector<VehicleFueling> fuelings;
	
	private Long licenseType_id;
	private Long transactionType_id;
	private UploadedFile licenseDocument;
	private VehicleLicense license;
	private Vector<VehicleLicense> licenses;
	
	private boolean selectAll;
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public FleetMBean()
	{
		vtrackpollinterval = 120;
        defaultCenterCoor = "6.427887,3.4287645";
        centerCoor = defaultCenterCoor;
	}
	
	public void saveLicense()
	{
		if(getVehicle_id() != null && getLicense().getTran_dt() != null && getLicense().getLic_start_dt() != null &&
				getLicenseType_id() != null && getLicenseType_id() > 0 && getTransactionType_id() != null && 
				getTransactionType_id() > 0)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object vObj = gDAO.find(Vehicle.class, getVehicle_id());
			if(vObj != null)
			{
				getLicense().setVehicle((Vehicle)vObj);
				
				Object ltObj = gDAO.find(LicenseType.class, getLicenseType_id());
				if(ltObj != null)
					getLicense().setLicType((LicenseType)ltObj);
				
				Object ttObj = gDAO.find(TransactionType.class, getTransactionType_id());
				if(ttObj != null)
					getLicense().setTranType((TransactionType)ttObj);
				
				Object vendorObj = gDAO.find(Vendor.class, getVendor_id());
				if(vendorObj != null)
					getLicense().setVendor((Vendor)vendorObj);
				
				if(getLicenseDocument() != null)
					getLicense().setDocument(getLicenseDocument().getContents());
				
				Calendar c = Calendar.getInstance();
				c.setTime(getLicense().getLic_start_dt());
				c.add(Calendar.YEAR, 1);
				getLicense().setLic_end_dt(c.getTime());
				
				if(getLicense().getLic_end_dt().after(new Date()))
				{
					getLicense().setActive(true);
					getLicense().setExpired(false);
				}
				else
				{
					getLicense().setActive(false);
					getLicense().setExpired(true);
				}
				
				gDAO.startTransaction();
				boolean ret = false;
				if(getLicense().getId() != null)
				{
					getLicense().setCreatedBy(dashBean.getUser());
					gDAO.update(getLicense());
				}
				else
				{
					getLicense().setCrt_dt(new Date());
					getLicense().setCreatedBy(dashBean.getUser());
					gDAO.save(getLicense());
				}
				
				if(ret)
				{
					gDAO.commit();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "License captured/updated successfully!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					
					setLicense(null);
					setLicenses(null);
					setVehicle_id(null);
					setLicenseType_id(null);
					setTransactionType_id(null);
					setLicenseDocument(null);
					setVendor_id(null);
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Error occured during save: " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				gDAO.destroy();
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid vehicle selected!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Also ensure you selected a vehicle!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void saveFueling()
	{
		if(getVehicle_id() != null && getFueling().getCaptured_dt() != null && getFueling().getAmt() > 0 &&
				getFueling().getLitres() > 0)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object vObj = gDAO.find(Vehicle.class, getVehicle_id());
			if(vObj != null)
			{
				getFueling().setVehicle((Vehicle)vObj);
			
				gDAO.startTransaction();
				boolean ret = false;
				if(getFueling().getId() != null)
				{
					getFueling().setCreatedBy(dashBean.getUser());
					gDAO.update(getFueling());
				}
				else
				{
					getFueling().setCrt_dt(new Date());
					getFueling().setCreatedBy(dashBean.getUser());
					gDAO.save(getFueling());
				}
				
				if(ret)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("name", "Fueling");
					params.put("systemObj", true);
					Object expTypesObj = gDAO.search("ExpenseType", params);
					if(expTypesObj != null)
					{
						Vector<ExpenseType> expTypes = (Vector<ExpenseType>)expTypesObj;
						if(expTypes.size() > 0)
						{
							// insert an expense for this fueling transaction
							Expense exp = new Expense();
							exp.setAmount(getFueling().getAmt());
							exp.setCreatedBy(dashBean.getUser());
							exp.setCrt_dt(new Date());
							exp.setExpense_dt(getFueling().getCaptured_dt());
							exp.setPartner(getPartner());
							exp.setRemarks("Fueling for vehicle: " + getFueling().getVehicle().getRegistrationNo() + " on " + getFueling().getCaptured_dt());
							
							exp.setType(expTypes.get(0));
							
							gDAO.save(exp);
						}
					}
					
					if(getFueling().getOdometer() > 0)
					{
						VehicleOdometerData vod = new VehicleOdometerData();
						vod.setCaptured_dt(getFueling().getCaptured_dt());
						vod.setCrt_dt(new Date());
						vod.setOdometer(getFueling().getOdometer());
						vod.setSource("Fueling");
						vod.setVehicle(getFueling().getVehicle());
						
						gDAO.save(vod);
					}
					
					if(getFueling().getFuelLevel() > 0)
					{
						VehicleFuelData vfd = new VehicleFuelData();
						vfd.setCaptured_dt(getFueling().getCaptured_dt());
						vfd.setCrt_dt(new Date());
						vfd.setFuelLevel(getFueling().getFuelLevel());
						vfd.setSource("Fueling");
						vfd.setVehicle(getFueling().getVehicle());
						
						gDAO.save(vfd);
					}
					
					gDAO.commit();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Fueling captured/updated successfully!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					
					setFueling(null);
					setFuelings(null);
					setVehicle_id(null);
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Error occured during save: " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				gDAO.destroy();
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid vehicle selected!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Also ensure you selected a vehicle!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void saveAccident()
	{
		if(getVehicle_id() != null && getAccident().getAccident_dt() != null && getAccident().getAccidentDescription() != null &&
				getAccident().getDriverComment() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object vObj = gDAO.find(Vehicle.class, getVehicle_id());
			if(vObj != null)
			{
				getAccident().setVehicle((Vehicle)vObj);
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle", getAccident().getVehicle());
				params.put("active", true);
				
				Object drvObj = gDAO.search("VehicleDriver", params);
				if(drvObj != null)
				{
					Vector<VehicleDriver> drvList = (Vector<VehicleDriver>)drvObj;
					if(drvList != null && drvList.size() > 0)
					{
						getAccident().setAssignedDriver(drvList.get(0).getDriver());
					}
				}
			}
			/*if(getRepairComp_id() != null)
			{
				Object repairVendor = gDAO.find(Vendor.class, getRepairComp_id());
				if(repairVendor != null)
					getAccident().setRepairComp((Vendor)repairVendor);
			}
			if(getInsuranceComp_id() != null)
			{
				Object vendor = gDAO.find(Vendor.class, getInsuranceComp_id());
				if(vendor != null)
					getAccident().setInsuranceComp((Vendor)vendor);
			}*/
			if(getAccidentPhoto() != null)
			{
				getAccident().setAccidentPhoto(getAccidentPhoto().getContents());
			}
			/*if(getRepairedPhoto() != null)
			{
				getAccident().setAfterRepairPhoto(getRepairedPhoto().getContents());
			}*/
			if(getAccidentDocument() != null)
			{
				getAccident().setDocument(getAccidentDocument().getContents());
			}
			
			if(isAccidentStatus())
				getAccident().setActive(false);
			
			gDAO.startTransaction();
			boolean ret = false;
			if(getAccident().getId() != null)
			{
				getAccident().setCreatedBy(dashBean.getUser());
				gDAO.update(getAccident());
			}
			else
			{
				getAccident().setCrt_dt(new Date());
				getAccident().setCreatedBy(dashBean.getUser());
				gDAO.save(getAccident());
			}
			
			if(ret)
			{
				getAccident().getVehicle().setActive(false);
				getAccident().getVehicle().setActiveStatus(VehicleStatusEnum.ACCIDENTED.getStatus());
				
				gDAO.update(getAccident().getVehicle());
				
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Accident captured/updated successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setAccidentStatus(false);
				setRepairComp_id(null);
				setInsuranceComp_id(null);
				setAccidentPhoto(null);
				setRepairedPhoto(null);
				setAccidentDocument(null);
				setAccident(null);
				setAccidents(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Error occured during save: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Also ensure you selected a vehicle!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void finishAdHocMaintenance()
	{
		if(getAdhocMain().getId() != null && getAdhocMain().getClose_dt() != null &&
				getAdhocMain().getClosed_cost() != null && getAdhocMain().getClosed_cost().doubleValue() > 0)
		{
			getAdhocMain().setActive(false);
			getAdhocMain().setStatus("Finished");
			
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = gDAO.update(getAdhocMain());
			if(ret)
			{
				gDAO.commit();
				setAdhocMain(null);
				setAdhocMains(null);
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Ad-Hoc maintenance started for successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during maintenance completion: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Also ensure you selected a started Ad-Hoc maintenance!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void startAdHocMaintenance()
	{
		if(getAdhocRequest().getId() != null && getAdhocMain().getStart_dt() != null &&
				getAdhocMain().getDescription() != null && getAdhocMain().getInitial_cost() != null && 
				getAdhocMain().getInitial_cost().doubleValue() > 0)
		{
			getAdhocMain().setCreatedBy(dashBean.getUser());
			getAdhocMain().setCrt_dt(new Date());
			getAdhocMain().setVehicle(getAdhocRequest().getVehicle());
			getAdhocMain().setActive(true);
			getAdhocMain().setStatus("Started");
			
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = gDAO.save(getAdhocMain());
			if(ret)
			{
				getAdhocRequest().setActive(false);
				getAdhocRequest().setStatus("Attended");
				gDAO.update(getAdhocRequest());
				
				gDAO.commit();
				setAdhocMain(null);
				setAdhocMains(null);
				setAdhocRequest(null);
				setAdHocRequests(null);
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Ad-Hoc maintenance started for successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during maintenance start: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Also ensure you selected an existing Ad-Hoc Request!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void requestAdHocMaintenance()
	{
		if(getAdhocRequest().getDescription() != null && getAdhocRequest().getMaintenance_dt() != null)
		{
			if(getVehicles() != null && getVehicles().size() > 0)
			{
				int count = 0;
				boolean ret = false;
				GeneralDAO gDAO = new GeneralDAO();
				gDAO.startTransaction();
				for(Vehicle v : getVehicles())
				{
					if(v.isSelected())
					{
						count++;
						getAdhocRequest().setActive(true);
						getAdhocRequest().setCreatedBy(dashBean.getUser());
						getAdhocRequest().setCrt_dt(new Date());
						getAdhocRequest().setStatus("Pending");
						getAdhocRequest().setVehicle(v);
						
						ret = gDAO.save(getAdHocRequests());
						if(!ret)
							break;
					}
				}
				if(count > 0)
				{
					if(ret)
					{
						gDAO.commit();
						setAdhocRequest(null);
						resetVehicles();
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Ad-Hoc maintenance request submitted for " + count + " vehicle(s) successfully!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during maintenance request: " + gDAO.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No vehicle selected!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No vehicle found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void finishRMaintenance()
	{
		if(getRoutine().getOdometer() != null && getRoutine().getOdometer().doubleValue() > 0 && 
				getRoutine().getDescription() != null && getRoutine().getInitial_amount() != null &&
				getRoutine().getInitial_amount().doubleValue() > 0 && getRoutine().getStart_dt() != null &&
				getRoutine().getClose_dt() != null && getRoutine().getClosed_amount() != null &&
				getRoutine().getClosed_amount().doubleValue() > 0)
		{
			getRoutine().setFinished(true);
			getRoutine().setStatus("Finished");
			boolean ret = false;
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			ret = gDAO.update(getRoutine());
			if(ret)
			{
				// vehicle is no longer under maintenance
				getRoutine().getVehicle().setActive(true);
				getRoutine().getVehicle().setActiveStatus(VehicleStatusEnum.ACTIVE.getStatus());
				ret = gDAO.update(getRoutine().getVehicle());
			}
			
			if(ret)
			{
				if(isNextRMSetup())
				{
					VehicleRoutineMaintenanceSetup vr = new VehicleRoutineMaintenanceSetup();
					vr.setAlert_odometer(getRoutineSetup().getAlert_odometer());
					vr.setOdometer(getRoutineSetup().getOdometer());
					vr.setActive(true);
					vr.setCreatedBy(dashBean.getUser());
					vr.setCrt_dt(new Date());
					vr.setVehicle(getRoutine().getVehicle());
					ret = gDAO.save(vr);
					if(ret)
					{
						setNextRMSetup(false);
						setRoutineSetup(null);
					}
				}
				if(ret)
				{
					gDAO.commit();
					setRoutine(null);
					setVendor_id(null);
					resetRoutines();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Routine maintenance finished for vehicle successfully!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during maintenance finish: " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during maintenance finish: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void startRMaintenance()
	{
		if(getRoutine().getOdometer() != null && getRoutine().getOdometer().doubleValue() > 0 && 
				getRoutine().getDescription() != null && getRoutine().getInitial_amount() != null &&
				getRoutine().getInitial_amount().doubleValue() > 0 && getRoutine().getStart_dt() != null)
		{
			if(getVehicles() != null && getVehicles().size() > 0)
			{
				int count = 0;
				boolean ret = false;
				GeneralDAO gDAO = new GeneralDAO();
				gDAO.startTransaction();
				for(Vehicle v : getVehicles())
				{
					if(v.isSelected())
					{
						VehicleRoutineMaintenance vr = new VehicleRoutineMaintenance();
						count++;
						vr.setOdometer(getRoutine().getOdometer());
						vr.setDescription(getRoutine().getDescription());
						vr.setFinished(false);
						vr.setInitial_amount(getRoutine().getInitial_amount());
						vr.setStart_dt(getRoutine().getStart_dt());
						vr.setStatus("Start");
						vr.setCreatedBy(dashBean.getUser());
						vr.setCrt_dt(new Date());
						vr.setVehicle(v);
						
						if(getVendor_id() != null && getVendor_id() > 0)
						{
							Object vobj = gDAO.find(Vendor.class, getVendor_id());
							if(vobj != null)
								vr.setVendor((Vendor)vobj);
						}
						
						ret = gDAO.save(vr);
						if(!ret)
							break;
						else if(vr.getStatus().equalsIgnoreCase("Start"))
						{
							// vehicle is under maintenance
							v.setActive(false);
							v.setActiveStatus(VehicleStatusEnum.UNDER_MAINTENANCE.getStatus());
							ret = gDAO.update(v);
							if(!ret)
								break;
						}
						
						if(ret)
						{
							// reset all the active maintenance setup for this vehicle to in-active
							Hashtable<String, Object> params = new Hashtable<String, Object>();
							params.put("vehicle", v);
							params.put("active", true);
							
							Object vrmsObj = gDAO.search("VehicleRoutineMaintenanceSetup", params);
							if(vrmsObj != null)
							{
								Vector<VehicleRoutineMaintenanceSetup> vrmsList = (Vector<VehicleRoutineMaintenanceSetup>)vrmsObj;
								if(vrmsList.size()>0)
								{
									for(VehicleRoutineMaintenanceSetup vrm : vrmsList)
									{
										if(vrm.getOdometer().doubleValue() <= getRoutine().getOdometer().doubleValue())
										{
											vrm.setActive(false);
											gDAO.update(vrm);
										}
									}
								}
							}
							
						}
					}
				}
				if(count > 0)
				{
					if(ret)
					{
						gDAO.commit();
						setRoutine(null);
						setVendor_id(null);
						resetVehicles();
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Routine maintenance started for " + count + " vehicle(s) successfully!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during maintenance start: " + gDAO.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No vehicle selected!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No vehicle found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void setupRMaintenance()
	{
		if(getRoutineSetup().getAlert_odometer() != null && getRoutineSetup().getAlert_odometer().doubleValue() > 0 && 
				getRoutineSetup().getOdometer() != null && getRoutineSetup().getOdometer().doubleValue() > 0)
		{
			if(getVehicles() != null && getVehicles().size() > 0)
			{
				int count = 0;
				boolean ret = false;
				GeneralDAO gDAO = new GeneralDAO();
				gDAO.startTransaction();
				for(Vehicle v : getVehicles())
				{
					if(v.isSelected())
					{
						VehicleRoutineMaintenanceSetup vr = new VehicleRoutineMaintenanceSetup();
						count++;
						vr.setAlert_odometer(getRoutineSetup().getAlert_odometer());
						vr.setOdometer(getRoutineSetup().getOdometer());
						vr.setActive(true);
						vr.setCreatedBy(dashBean.getUser());
						vr.setCrt_dt(new Date());
						vr.setVehicle(v);
						ret = gDAO.save(vr);
						if(!ret)
							break;
					}
				}
				if(count > 0)
				{
					if(ret)
					{
						gDAO.commit();
						setRoutineSetup(null);
						resetVehicles();
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Routine maintenance setup for " + count + " vehicle(s) successfully!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during setup: " + gDAO.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No vehicle selected!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No vehicle found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void searchTrackerInfo()
	{
		if(getPartner() != null && getFleet_id() != null && getFleet_id() > 0 && getStdt() != null && getEddt() != null)
		{
			setTvehicles(null);
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void searchTrackerInfo2()
	{
		if(getPartner() != null && getFleet_id() != null && getFleet_id() > 0)
		{
			setTvehicles2(null);
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Vehicle> getTvehicles() {
		if(tvehicles == null)
		{
			tvehicles = new Vector<Vehicle>();
			
			if(getPartner() != null && getFleet_id() != null && getFleet_id() > 0)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				
				if(getFleet_id() != null && getFleet_id() > 0)
				{
					Object fobj = gDAO.find(Fleet.class, getFleet_id());
					if(fobj != null)
						params.put("fleet", (Fleet)fobj);
				}
				
				boolean found_one = false;
				if(getVehicle_id() != null && getVehicle_id() > 0)
				{
					Object vobj = gDAO.find(Vehicle.class, getVehicle_id());
					if(vobj != null)
					{
						tvehicles = new Vector<Vehicle>();
						tvehicles.add((Vehicle)vobj);
					}
					found_one = true;
				}
				else if(getRegNo() != null && getRegNo().trim().length() > 0)
				{
					params.put("registrationNo", getRegNo());
				}
				if(!found_one)
				{
					Object vhsObj = gDAO.search("Vehicle", params);
					if(vhsObj != null)
					{
						tvehicles = (Vector<Vehicle>)vhsObj;
					}
				}
				
				if(tvehicles != null && tvehicles.size() > 0)
				{
					for(Vehicle v : tvehicles)
					{
						Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle = :vehicle and (e.captured_dt between :stdt and :eddt) order by e.captured_dt desc");
						
						q.setParameter("vehicle", v);
						q.setParameter("stdt", getStdt());
						q.setParameter("eddt", getEddt());
						
						Object retObj = gDAO.search(q, 0);
						if(retObj != null)
							v.setTrackerData((Vector<VehicleTrackerData>)retObj);
					}
				}
			}
		}
		return tvehicles;
	}

	public void setTvehicles(Vector<Vehicle> tvehicles) {
		this.tvehicles = tvehicles;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vehicle> getTvehicles2() {
		if(tvehicles2 == null)
		{
			tvehicles2 = new Vector<Vehicle>();
			
			if(getPartner() != null && getFleet_id() != null && getFleet_id() > 0)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				
				if(getFleet_id() != null && getFleet_id() > 0)
				{
					Object fobj = gDAO.find(Fleet.class, getFleet_id());
					if(fobj != null)
						params.put("fleet", (Fleet)fobj);
				}
				
				boolean found_one = false;
				if(getVehicle_id() != null && getVehicle_id() > 0)
				{
					Object vobj = gDAO.find(Vehicle.class, getVehicle_id());
					if(vobj != null)
					{
						tvehicles2 = new Vector<Vehicle>();
						tvehicles2.add((Vehicle)vobj);
					}
					found_one = true;
				}
				else if(getRegNo() != null && getRegNo().trim().length() > 0)
				{
					params.put("registrationNo", getRegNo());
				}
				if(!found_one)
				{
					Object vhsObj = gDAO.search("Vehicle", params);
					if(vhsObj != null)
					{
						tvehicles2 = (Vector<Vehicle>)vhsObj;
					}
				}
				
				if(tvehicles2 != null && tvehicles2.size() > 0)
				{
					for(Vehicle v : tvehicles2)
					{
						Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle = :vehicle order by e.captured_dt desc");
						
						q.setParameter("vehicle", v);
						
						Object retObj = gDAO.search(q, 1);
						if(retObj != null)
							v.setTrackerData((Vector<VehicleTrackerData>)retObj);
					}
				}
			}
		}
		return tvehicles2;
	}

	public void setTvehicles2(Vector<Vehicle> tvehicles2) {
		this.tvehicles2 = tvehicles2;
	}

	@SuppressWarnings("unchecked")
	public void addVehicleTrack()
	{
		if(getSelectedVehicle().getId() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			boolean exists = false;
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("vehicle", getSelectedVehicle());
			params.put("user", dashBean.getUser());
			Object dvObj = gDAO.search("DashboardVehicle", params);
			if(dvObj != null)
			{
				Vector<DashboardVehicle> dvList = (Vector<DashboardVehicle>)dvObj;
				if(dvList != null && dvList.size() > 0)
				{
					exists = true;
				}
			}
			
			if(!exists)
			{
				DashboardVehicle dv = new DashboardVehicle();
				dv.setCrt_dt(new Date());
				dv.setUser(dashBean.getUser());
				dv.setVehicle(getSelectedVehicle());
				gDAO.startTransaction();
				boolean ret = gDAO.save(dv);
				if(ret)
				{
					gDAO.commit();
					
					dashBean.setDashVehicles(null);
					
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Vehicle added to dashboard successfully.");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to add vehicle. " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Vehicle already tracked on dashboard.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void assignVehicleDriver()
	{
		if(getDriver_id() != null)
		{
			if(getSelectedVehicle().getId() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Object drvObj = gDAO.find(PartnerDriver.class, getDriver_id());
				if(drvObj != null)
				{
					PartnerDriver drv = (PartnerDriver)drvObj;
					
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("driver", drv);
					params.put("active", true);
					
					Object drvVs = gDAO.search("VehicleDriver", params);
					boolean wasPreviousAssigned = false;
					gDAO.startTransaction();
					if(drvVs != null)
					{
						Vector<VehicleDriver> drvVsList = (Vector<VehicleDriver>)drvVs;
						for(VehicleDriver vd : drvVsList)
						{
							wasPreviousAssigned = true;
							vd.setActive(false);
							gDAO.update(vd);
						}
					}
					
					VehicleDriver vd = new VehicleDriver();
					vd.setActive(true);
					vd.setCreatedBy(dashBean.getUser());
					vd.setCrt_dt(new Date());
					vd.setDriver(drv);
					vd.setStart_dt(new Date());
					vd.setVehicle(getSelectedVehicle());
					boolean ret = gDAO.save(vd);
					if(ret)
					{
						gDAO.commit();
						setSelectedVehicle(null);
						setDriver_id(null);
						setPartnerDrivers(null);
						setVehicles(null);
						
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Driver assigned successfully." + ((wasPreviousAssigned) ? " NOTE: This driver was un-assigned from his/her current vehicle." : ""));
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create vehicle. " + gDAO.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No selected driver!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No selected vehicle!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void saveVehicle()
	{
		if(getVehicle().getRegistrationNo() != null && getVehicle().getChasisNo() != null && getVehicle().getEngineNo() != null
				&& getVehicleModel_id() != null && getFleet_id() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object modelObj = gDAO.find(VehicleModel.class, getVehicleModel_id());
			if(modelObj != null)
				getVehicle().setModel((VehicleModel)modelObj);
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid model selected!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			
			Object fleetObj = gDAO.find(Fleet.class, getFleet_id());
			if(fleetObj != null)
			{
				getVehicle().setFleet((Fleet)fleetObj);
				if(getVehicle().getFleet().getVehicleMake() != null)
				{
					// check the selected make to ensure its allowed in this fleet
					if(getVehicle().getModel().getMaker().getId() == getVehicle().getFleet().getVehicleMake().getId());
					else
					{
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Vehicles of maker '" + getVehicle().getModel().getMaker().getName() + "' is not allowed in the selected fleet!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
						return;
					}
				}
				
				if(getVehicle().getFleet().getVehicleType() != null)
				{
					// check the selected make to ensure its allowed in this fleet
					if(getVehicle().getModel().getType().getId() == getVehicle().getFleet().getVehicleType().getId());
					else
					{
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Vehicles of type '" + getVehicle().getModel().getType().getName() + "' is not allowed in the selected fleet!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
						return;
					}
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid fleet selected!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			
			if(getVendor_id() != null)
			{
				Object vendorObj = gDAO.find(Vendor.class, getVendor_id());
				if(vendorObj != null)
					getVehicle().setVendor((Vendor)vendorObj);
			}
			
			getVehicle().setActive(true);
			getVehicle().setActiveStatus(VehicleStatusEnum.ACTIVE.getStatus());
			getVehicle().setCreatedBy(dashBean.getUser());
			getVehicle().setCrt_dt(new Date());
			getVehicle().setPartner(getPartner());
			
			gDAO.startTransaction();
			boolean ret = gDAO.save(getVehicle());
			if(ret)
			{
				getVehicleParam().setCreatedBy(dashBean.getUser());
				getVehicleParam().setCrt_dt(new Date());
				getVehicleParam().setVehicle(getVehicle());
				
				if(getDepartment_id() != null)
				{
					Object dpt = gDAO.find(Department.class, getDepartment_id());
					if(dpt != null)
						getVehicleParam().setDept((Department)dpt);
				}
				if(getRegion_id() != null)
				{
					Object rg = gDAO.find(Region.class, getRegion_id());
					if(rg != null)
						getVehicleParam().setRegion((Region)rg);
				}
				if(getFuelType_id() != null)
				{
					Object rg = gDAO.find(FuelType.class, getFuelType_id());
					if(rg != null)
						getVehicleParam().setFuelType((FuelType)rg);
				}
				if(getVehiclePhoto() != null)
				{
					getVehicleParam().setPhoto(getVehiclePhoto().getContents());
				}
				
				gDAO.save(getVehicleParam());
				
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Vehicle created successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setVehiclePhoto(null);
				setFuelType_id(null);
				setDepartment_id(null);
				setRegion_id(null);
				setVehicleParam(null);
				setVehicle(null);
				setVehicles(null);
				setFleets(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create vehicle. " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void save(int i)
	{
		GeneralDAO gDAO = new GeneralDAO();
		boolean ret = false;
		boolean validated = false;
		switch(i)
		{
			case 1: // vehicle type
			{
				if(getVehicleType().getName() != null)
				{
					getVehicleType().setCreatedBy(dashBean.getUser());
					getVehicleType().setCrt_dt(new Date());
					gDAO.startTransaction();
					ret = gDAO.save(getVehicleType());
					if(ret)
					{
						setVehicleType(null);
						setVehicleTypes(null);
					}
					validated = true;
				}
				break;
			}
			case 2: // vehicle make
			{
				if(getVehicleMake().getName() != null)
				{
					getVehicleMake().setCreatedBy(dashBean.getUser());
					getVehicleMake().setCrt_dt(new Date());
					gDAO.startTransaction();
					ret = gDAO.save(getVehicleMake());
					if(ret)
					{
						setVehicleMake(null);
						setVehicleMakes(null);
					}
					validated = true;
				}
				break;
			}
			case 3: // vehicle models
			{
				if(getVmodel().getName() != null && getVehicleType_id() != null && getVehicleMake_id() != null)
				{
					getVmodel().setCreatedBy(dashBean.getUser());
					getVmodel().setCrt_dt(new Date());
					
					if(getVehicleMake_id() > 0)
					{
						Object obj = gDAO.find(VehicleMake.class, getVehicleMake_id());
						if(obj != null)
						{
							getVmodel().setMaker((VehicleMake)obj);
						}
					}
					if(getVehicleType_id() > 0)
					{
						Object obj = gDAO.find(VehicleType.class, getVehicleType_id());
						if(obj != null)
						{
							getVmodel().setType((VehicleType)obj);
						}
					}
					gDAO.startTransaction();
					ret = gDAO.save(getVmodel());
					if(ret)
					{
						setVmodel(null);
						setVmodels(null);
					}
					validated = true;
				}
				break;
			}
		}
		if(validated)
		{
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Entity created successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create entity. " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void saveFleet()
	{
		if(getFleet().getName() != null)
		{
			getFleet().setDefaultFleet(false);
			getFleet().setCreatedBy(dashBean.getUser());
			getFleet().setCrt_dt(new Date());
			getFleet().setPartner(getPartner());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			if(getVehicleMake_id() > 0)
			{
				Object obj = gDAO.find(VehicleMake.class, getVehicleMake_id());
				if(obj != null)
				{
					getFleet().setVehicleMake((VehicleMake)obj);
				}
			}
			if(getVehicleType_id() > 0)
			{
				Object obj = gDAO.find(VehicleType.class, getVehicleType_id());
				if(obj != null)
				{
					getFleet().setVehicleType((VehicleType)obj);
				}
			}
			
			gDAO.startTransaction();
			boolean ret = gDAO.save(getFleet());
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Fleet created successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setFleet(null);
				setFleets(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create entity. " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public Partner getPartner() {
		if(!dashBean.getUser().getPartner().isSattrak())
		{
			partner = dashBean.getUser().getPartner();
		}
		else
		{
			if(getPartner_id() != null)
			{
				partner = (Partner)new GeneralDAO().find(Partner.class, getPartner_id());
			}
		}
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}
	
	public Long getPartner_id() {
		return partner_id;
	}

	public void setPartner_id(Long partner_id) {
		this.partner_id = partner_id;
	}

	public Long getVehicleType_id() {
		return vehicleType_id;
	}

	public void setVehicleType_id(Long vehicleType_id) {
		this.vehicleType_id = vehicleType_id;
	}

	public Long getVehicleMake_id() {
		return vehicleMake_id;
	}

	public void setVehicleMake_id(Long vehicleMake_id) {
		this.vehicleMake_id = vehicleMake_id;
	}

	public VehicleType getVehicleType() {
		if(vehicleType == null)
			vehicleType = new VehicleType();
		return vehicleType;
	}

	public void setVehicleType(VehicleType vehicleType) {
		this.vehicleType = vehicleType;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleType> getVehicleTypes() {
		if(vehicleTypes == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object dpsObj = gDAO.findAll("VehicleType");
			if(dpsObj != null)
			{
				vehicleTypes = (Vector<VehicleType>)dpsObj;
				for(VehicleType e : vehicleTypes)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("model.type", e);
					Object objs = gDAO.search("Vehicle", params);
					if(objs != null)
						e.setVehicles((Vector<Vehicle>)objs);
				}
			}
		}
		return vehicleTypes;
	}

	public void setVehicleTypes(Vector<VehicleType> vehicleTypes) {
		this.vehicleTypes = vehicleTypes;
	}

	public VehicleMake getVehicleMake() {
		if(vehicleMake == null)
			vehicleMake = new VehicleMake();
		return vehicleMake;
	}

	public void setVehicleMake(VehicleMake vehicleMake) {
		this.vehicleMake = vehicleMake;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleMake> getVehicleMakes() {
		if(vehicleMakes == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object dpsObj = gDAO.findAll("VehicleMake");
			if(dpsObj != null)
			{
				vehicleMakes = (Vector<VehicleMake>)dpsObj;
				for(VehicleMake e : vehicleMakes)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("model.maker", e);
					Object objs = gDAO.search("Vehicle", params);
					if(objs != null)
						e.setVehicles((Vector<Vehicle>)objs);
				}
			}
		}
		return vehicleMakes;
	}

	public void setVehicleMakes(Vector<VehicleMake> vehicleMakes) {
		this.vehicleMakes = vehicleMakes;
	}

	public VehicleModel getVmodel() {
		if(vmodel == null)
			vmodel = new VehicleModel();
		return vmodel;
	}

	public void setVmodel(VehicleModel vmodel) {
		this.vmodel = vmodel;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleModel> getVmodels() {
		boolean research = true;
		if(vmodels == null || vmodels.size() == 0)
			research = true;
		else if(vmodels.size() > 0)
		{
			if(vmodels.get(0).getMaker().getId() == getVehicleMake_id() && vmodels.get(0).getType().getId() == getVehicleType_id())
				research = false;
		}
		if(research)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			if(getVehicleMake_id() != null)
			{
				Object obj = gDAO.find(VehicleMake.class, getVehicleMake_id());
				if(obj != null)
					params.put("maker", (VehicleMake)obj);
			}
			if(getVehicleType_id() != null)
			{
				Object obj = gDAO.find(VehicleType.class, getVehicleType_id());
				if(obj != null)
					params.put("type", (VehicleType)obj);
			}
			Object foundObjs = gDAO.search("VehicleModel", params);
			if(foundObjs != null)
			{
				vmodels = (Vector<VehicleModel>)foundObjs;
				for(VehicleModel e : vmodels)
				{
					params = new Hashtable<String, Object>();
					params.put("model", e);
					Object objs = gDAO.search("Vehicle", params);
					if(objs != null)
						e.setVehicles((Vector<Vehicle>)objs);
				}
			}
		}
		return vmodels;
	}

	public void setVmodels(Vector<VehicleModel> vmodels) {
		this.vmodels = vmodels;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}

	public Fleet getFleet() {
		if(fleet == null)
			fleet = new Fleet();
		return fleet;
	}

	public void setFleet(Fleet fleet) {
		this.fleet = fleet;
	}
	
	public void resetFleets()
	{
		setFleets(null);
	}

	@SuppressWarnings("unchecked")
	public Vector<Fleet> getFleets() {
		boolean research = true;
		if(fleets == null || fleets.size() == 0)
			research = true;
		else if(fleets.size() > 0)
		{
			if(getPartner() != null)
			{
				if(fleets.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			fleets = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				Object dpsObj = gDAO.search("Fleet", params);
				if(dpsObj != null)
				{
					fleets = (Vector<Fleet>)dpsObj;
					for(Fleet f : fleets)
					{
						params = new Hashtable<String, Object>();
						params.put("fleet", f);
						Object vhsObj = gDAO.search("Vehicle", params);
						if(vhsObj != null)
						{
							f.setVehicles((Vector<Vehicle>)vhsObj);
						}
					}
				}
			}
		}
		return fleets;
	}

	public void setFleets(Vector<Fleet> fleets) {
		this.fleets = fleets;
	}

	public Long getFleet_id() {
		return fleet_id;
	}

	public void setFleet_id(Long fleet_id) {
		this.fleet_id = fleet_id;
	}

	public Long getVehicleModel_id() {
		return vehicleModel_id;
	}

	public void setVehicleModel_id(Long vehicleModel_id) {
		this.vehicleModel_id = vehicleModel_id;
	}

	public Long getVendor_id() {
		return vendor_id;
	}

	public void setVendor_id(Long vendor_id) {
		this.vendor_id = vendor_id;
	}

	public Long getDepartment_id() {
		return department_id;
	}

	public void setDepartment_id(Long department_id) {
		this.department_id = department_id;
	}

	public Long getRegion_id() {
		return region_id;
	}

	public void setRegion_id(Long region_id) {
		this.region_id = region_id;
	}

	public Long getFuelType_id() {
		return fuelType_id;
	}

	public void setFuelType_id(Long fuelType_id) {
		this.fuelType_id = fuelType_id;
	}

	@SuppressWarnings("unchecked")
	public Vector<Department> getDepts() {
		boolean research = true;
		if(depts == null || depts.size() == 0)
			research = true;
		else if(depts.size() > 0)
		{
			if(getPartner() != null)
			{
				if(depts.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			depts = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				Object dpsObj = gDAO.search("Department", params);
				if(dpsObj != null)
				{
					depts = (Vector<Department>)dpsObj;
				}
			}
		}
		return depts;
	}

	public void setDepts(Vector<Department> depts) {
		this.depts = depts;
	}

	@SuppressWarnings("unchecked")
	public Vector<Region> getRegions() {
		boolean research = true;
		if(regions == null || regions.size() == 0)
			research = true;
		else if(regions.size() > 0)
		{
			if(getPartner() != null)
			{
				if(regions.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			regions = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				Object dpsObj = gDAO.search("Region", params);
				if(dpsObj != null)
				{
					regions = (Vector<Region>)dpsObj;
				}
			}
		}
		return regions;
	}

	public void setRegions(Vector<Region> regions) {
		this.regions = regions;
	}

	@SuppressWarnings("unchecked")
	public Vector<FuelType> getFuelTypes() {
		if(fuelTypes == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Object fts = gDAO.findAll("FuelType");
			if(fts != null)
				fuelTypes = (Vector<FuelType>)fts;
		}
		return fuelTypes;
	}

	public void setFuelTypes(Vector<FuelType> fuelTypes) {
		this.fuelTypes = fuelTypes;
	}

	public UploadedFile getVehiclePhoto() {
		return vehiclePhoto;
	}

	public void setVehiclePhoto(UploadedFile vehiclePhoto) {
		this.vehiclePhoto = vehiclePhoto;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vendor> getVsalesVendors() {
		boolean research = true;
		if(vsalesVendors == null || vsalesVendors.size() == 0)
			research = true;
		else if(vsalesVendors.size() > 0)
		{
			if(getPartner() != null)
			{
				if(vsalesVendors.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			vsalesVendors = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Object stypes = gDAO.findAll("ServiceType");
				if(stypes != null)
				{
					Vector<ServiceType> stypesList = (Vector<ServiceType>)stypes;
					ServiceType st = null;
					for(ServiceType e : stypesList)
					{
						if(e.getName().trim().equalsIgnoreCase("vehicle sales"))
						{
							st = e;
							break;
						}
					}
					if(st != null)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("vendor.partner", getPartner());
						params.put("serviceType", st);
						Object dpsObj = gDAO.search("VendorServices", params);
						if(dpsObj != null)
						{
							Vector<VendorServices> vss = (Vector<VendorServices>)dpsObj;
							vsalesVendors = new Vector<Vendor>();
							for(VendorServices vs : vss)
							{
								vsalesVendors.add(vs.getVendor());
							}
						}
					}
				}
			}
		}
		return vsalesVendors;
	}

	public void setVsalesVendors(Vector<Vendor> vsalesVendors) {
		this.vsalesVendors = vsalesVendors;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vendor> getVserviceVendors() {
		boolean research = true;
		if(vserviceVendors == null || vserviceVendors.size() == 0)
			research = true;
		else if(vserviceVendors.size() > 0)
		{
			if(getPartner() != null)
			{
				if(vserviceVendors.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			vserviceVendors = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Object stypes = gDAO.findAll("ServiceType");
				if(stypes != null)
				{
					Vector<ServiceType> stypesList = (Vector<ServiceType>)stypes;
					ServiceType st = null;
					for(ServiceType e : stypesList)
					{
						if(e.getName().trim().equalsIgnoreCase("vehicle servicing"))
						{
							st = e;
							break;
						}
					}
					if(st != null)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("vendor.partner", getPartner());
						params.put("serviceType", st);
						Object dpsObj = gDAO.search("VendorServices", params);
						if(dpsObj != null)
						{
							Vector<VendorServices> vss = (Vector<VendorServices>)dpsObj;
							vserviceVendors = new Vector<Vendor>();
							for(VendorServices vs : vss)
							{
								vserviceVendors.add(vs.getVendor());
							}
						}
					}
				}
			}
		}
		return vserviceVendors;
	}

	public void setVserviceVendors(Vector<Vendor> vserviceVendors) {
		this.vserviceVendors = vserviceVendors;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vendor> getVrepairVendors() {
		boolean research = true;
		if(vrepairVendors == null || vrepairVendors.size() == 0)
			research = true;
		else if(vrepairVendors.size() > 0)
		{
			if(getPartner() != null)
			{
				if(vrepairVendors.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			vrepairVendors = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Object stypes = gDAO.findAll("ServiceType");
				if(stypes != null)
				{
					Vector<ServiceType> stypesList = (Vector<ServiceType>)stypes;
					ServiceType st = null;
					for(ServiceType e : stypesList)
					{
						if(e.getName().trim().equalsIgnoreCase("vehicle repair"))
						{
							st = e;
							break;
						}
					}
					if(st != null)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("vendor.partner", getPartner());
						params.put("serviceType", st);
						Object dpsObj = gDAO.search("VendorServices", params);
						if(dpsObj != null)
						{
							Vector<VendorServices> vss = (Vector<VendorServices>)dpsObj;
							vrepairVendors = new Vector<Vendor>();
							for(VendorServices vs : vss)
							{
								vrepairVendors.add(vs.getVendor());
							}
						}
					}
				}
			}
		}
		return vrepairVendors;
	}

	public void setVrepairVendors(Vector<Vendor> vrepairVendors) {
		this.vrepairVendors = vrepairVendors;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vendor> getVinsuranceVendors() {
		boolean research = true;
		if(vinsuranceVendors == null || vinsuranceVendors.size() == 0)
			research = true;
		else if(vinsuranceVendors.size() > 0)
		{
			if(getPartner() != null)
			{
				if(vinsuranceVendors.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			vinsuranceVendors = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Object stypes = gDAO.findAll("ServiceType");
				if(stypes != null)
				{
					Vector<ServiceType> stypesList = (Vector<ServiceType>)stypes;
					ServiceType st = null;
					for(ServiceType e : stypesList)
					{
						if(e.getName().trim().equalsIgnoreCase("insurance"))
						{
							st = e;
							break;
						}
					}
					if(st != null)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("vendor.partner", getPartner());
						params.put("serviceType", st);
						Object dpsObj = gDAO.search("VendorServices", params);
						if(dpsObj != null)
						{
							Vector<VendorServices> vss = (Vector<VendorServices>)dpsObj;
							vinsuranceVendors = new Vector<Vendor>();
							for(VendorServices vs : vss)
							{
								vinsuranceVendors.add(vs.getVendor());
							}
						}
					}
				}
			}
		}
		return vinsuranceVendors;
	}

	public void setVinsuranceVendors(Vector<Vendor> vinsuranceVendors) {
		this.vinsuranceVendors = vinsuranceVendors;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vendor> getVlicensesVendors() {
		boolean research = true;
		if(vlicensesVendors == null || vlicensesVendors.size() == 0)
			research = true;
		else if(vlicensesVendors.size() > 0)
		{
			if(getPartner() != null)
			{
				if(vlicensesVendors.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			vlicensesVendors = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Object stypes = gDAO.findAll("ServiceType");
				if(stypes != null)
				{
					Vector<ServiceType> stypesList = (Vector<ServiceType>)stypes;
					ServiceType st = null, stvl = null;
					for(ServiceType e : stypesList)
					{
						if(e.getName().trim().equalsIgnoreCase("insurance"))
						{
							st = e;
							if(stvl != null)
								break;
						}
						else if(e.getName().trim().equalsIgnoreCase("vehicle license"))
						{
							stvl = e;
							if(st != null)
								break;
						}
					}
					if(st != null && stvl != null)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("vendor.partner", getPartner());
						params.put("serviceType", st);
						Object dpsObj = gDAO.search("VendorServices", params);
						if(dpsObj != null)
						{
							Vector<VendorServices> vss = (Vector<VendorServices>)dpsObj;
							vlicensesVendors = new Vector<Vendor>();
							for(VendorServices vs : vss)
							{
								vlicensesVendors.add(vs.getVendor());
							}
						}
						
						params = new Hashtable<String, Object>();
						params.put("vendor.partner", getPartner());
						params.put("serviceType", stvl);
						dpsObj = gDAO.search("VendorServices", params);
						if(dpsObj != null)
						{
							Vector<VendorServices> vss = (Vector<VendorServices>)dpsObj;
							if(vlicensesVendors == null)
								vlicensesVendors = new Vector<Vendor>();
							for(VendorServices vs : vss)
							{
								Vendor vd = vs.getVendor();
								boolean exists = false;
								for(Vendor v : vlicensesVendors)
								{
									if(v.getId() == vd.getId())
									{
										exists = true;
										break;
									}
								}
								if(!exists)
									vlicensesVendors.add(vd);
							}
						}
					}
				}
			}
		}
		return vlicensesVendors;
	}

	public void setVlicensesVendors(Vector<Vendor> vlicensesVendors) {
		this.vlicensesVendors = vlicensesVendors;
	}

	public Vehicle getVehicle() {
		if(vehicle == null)
			vehicle = new Vehicle();
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public Vehicle getSelectedVehicle() {
		if(selectedVehicle == null)
			selectedVehicle = new Vehicle();
		return selectedVehicle;
	}

	public void setSelectedVehicle(Vehicle selectedVehicle) {
		this.selectedVehicle = selectedVehicle;
	}

	public VehicleParameters getVehicleParam() {
		if(vehicleParam == null)
			vehicleParam = new VehicleParameters();
		return vehicleParam;
	}

	public void setVehicleParam(VehicleParameters vehicleParam) {
		this.vehicleParam = vehicleParam;
	}

	public void resetVehicles()
	{
		setVehicles(null);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Vehicle> getVehicles() {
		if(vehicles == null)
		{
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				
				if(getFleet_id() != null)
				{
					Object fobj = gDAO.find(Fleet.class, getFleet_id());
					if(fobj != null)
						params.put("fleet", (Fleet)fobj);
				}
				
				Object vhsObj = gDAO.search("Vehicle", params);
				if(vhsObj != null)
				{
					vehicles = (Vector<Vehicle>)vhsObj;
					for(Vehicle v : vehicles)
					{
						params = new Hashtable<String, Object>();
						params.put("vehicle", v);
						Object vDrvs = gDAO.search("VehicleDriver", params);
						if(vDrvs != null)
						{
							v.setDrivers((Vector<VehicleDriver>)vDrvs);
							for(VehicleDriver vd : v.getDrivers())
							{
								if(vd.isActive())
								{
									v.setCurrentDriver(vd);
									break;
								}
							}
						}
						params = new Hashtable<String, Object>();
						params.put("vehicle", v);
						Object vParams = gDAO.search("VehicleParameters", params);
						if(vParams != null)
						{
							Vector<VehicleParameters> vParamsList = (Vector<VehicleParameters>)vParams;
							v.setParams(vParamsList);
						}
					}
				}
			}
		}
		return vehicles;
	}

	public void setVehicles(Vector<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public Long getDriver_id() {
		return driver_id;
	}

	public void setDriver_id(Long driver_id) {
		this.driver_id = driver_id;
	}

	@SuppressWarnings("unchecked")
	public Vector<PartnerDriver> getPartnerDrivers() {
		boolean research = true;
		if(partnerDrivers == null || partnerDrivers.size() == 0)
			research = true;
		else if(partnerDrivers.size() > 0)
		{
			if(getPartner() != null)
			{
				if(partnerDrivers.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			partnerDrivers = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				params.put("active", true);
				
				Object drvs = gDAO.search("PartnerDriver", params);
				if(drvs != null)
				{
					partnerDrivers = (Vector<PartnerDriver>)drvs;
				}
			}
		}
		return partnerDrivers;
	}

	public void setPartnerDrivers(Vector<PartnerDriver> partnerDrivers) {
		this.partnerDrivers = partnerDrivers;
	}

	public Long getVehicle_id() {
		return vehicle_id;
	}

	public void setVehicle_id(Long vehicle_id) {
		this.vehicle_id = vehicle_id;
	}

	public String getRegNo() {
		return regNo;
	}

	public void setRegNo(String regNo) {
		this.regNo = regNo;
	}

	public Date getStdt() {
		return stdt;
	}

	public void setStdt(Date stdt) {
		this.stdt = stdt;
	}

	public Date getEddt() {
		return eddt;
	}

	public void setEddt(Date eddt) {
		this.eddt = eddt;
	}

	public boolean isNextRMSetup() {
		return nextRMSetup;
	}

	public void setNextRMSetup(boolean nextRMSetup) {
		this.nextRMSetup = nextRMSetup;
	}

	public VehicleRoutineMaintenanceSetup getRoutineSetup() {
		if(routineSetup == null)
			routineSetup = new VehicleRoutineMaintenanceSetup();
		return routineSetup;
	}

	public void setRoutineSetup(VehicleRoutineMaintenanceSetup routineSetup) {
		this.routineSetup = routineSetup;
	}

	public VehicleRoutineMaintenance getRoutine() {
		if(routine == null)
			routine = new VehicleRoutineMaintenance();
		return routine;
	}

	public void setRoutine(VehicleRoutineMaintenance routine) {
		this.routine = routine;
	}
	
	public void resetRoutines()
	{
		setRoutines(null);
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleRoutineMaintenance> getRoutines() {
		boolean research = true;
		if(routines == null || routines.size() == 0)
			research = true;
		else if(routines.size() > 0)
		{
			if(getPartner() != null)
			{
				if(routines.get(0).getVehicle().getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			routines = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle.partner", getPartner());
				params.put("finished", false);
				
				Object drvs = gDAO.search("VehicleRoutineMaintenance", params);
				if(drvs != null)
				{
					routines = (Vector<VehicleRoutineMaintenance>)drvs;
				}
			}
		}
		return routines;
	}

	public void setRoutines(Vector<VehicleRoutineMaintenance> routines) {
		this.routines = routines;
	}

	public VehicleAdHocMaintenanceRequest getAdhocRequest() {
		if(adhocRequest == null)
			adhocRequest = new VehicleAdHocMaintenanceRequest();
		return adhocRequest;
	}

	public void setAdhocRequest(VehicleAdHocMaintenanceRequest adhocRequest) {
		this.adhocRequest = adhocRequest;
	}
	
	public void resetAdHocRequests()
	{
		setAdHocRequests(null);
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleAdHocMaintenanceRequest> getAdHocRequests() {
		boolean research = true;
		if(adHocRequests == null || adHocRequests.size() == 0)
			research = true;
		else if(adHocRequests.size() > 0)
		{
			if(getPartner() != null)
			{
				if(adHocRequests.get(0).getVehicle().getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			adHocRequests = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle.partner", getPartner());
				params.put("active", true);
				
				Object drvs = gDAO.search("VehicleAdHocMaintenanceRequest", params);
				if(drvs != null)
				{
					adHocRequests = (Vector<VehicleAdHocMaintenanceRequest>)drvs;
				}
			}
		}
		return adHocRequests;
	}

	public void setAdHocRequests(
			Vector<VehicleAdHocMaintenanceRequest> adHocRequests) {
		this.adHocRequests = adHocRequests;
	}

	public VehicleAdHocMaintenance getAdhocMain() {
		if(adhocMain == null)
			adhocMain = new VehicleAdHocMaintenance();
		return adhocMain;
	}

	public void setAdhocMain(VehicleAdHocMaintenance adhocMain) {
		this.adhocMain = adhocMain;
	}
	
	public void resetAdHocMains()
	{
		setAdhocMains(null);
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleAdHocMaintenance> getAdhocMains() {
		boolean research = true;
		if(adhocMains == null || adhocMains.size() == 0)
			research = true;
		else if(adhocMains.size() > 0)
		{
			if(getPartner() != null)
			{
				if(adhocMains.get(0).getVehicle().getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			adhocMains = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle.partner", getPartner());
				params.put("active", true);
				
				Object drvs = gDAO.search("VehicleAdHocMaintenance", params);
				if(drvs != null)
				{
					adhocMains = (Vector<VehicleAdHocMaintenance>)drvs;
				}
			}
		}
		return adhocMains;
	}

	public void setAdhocMains(Vector<VehicleAdHocMaintenance> adhocMains) {
		this.adhocMains = adhocMains;
	}

	public Long getInsuranceComp_id() {
		return insuranceComp_id;
	}

	public void setInsuranceComp_id(Long insuranceComp_id) {
		this.insuranceComp_id = insuranceComp_id;
	}

	public Long getRepairComp_id() {
		return repairComp_id;
	}

	public void setRepairComp_id(Long repairComp_id) {
		this.repairComp_id = repairComp_id;
	}

	public UploadedFile getAccidentPhoto() {
		return accidentPhoto;
	}

	public void setAccidentPhoto(UploadedFile accidentPhoto) {
		this.accidentPhoto = accidentPhoto;
	}

	public UploadedFile getRepairedPhoto() {
		return repairedPhoto;
	}

	public void setRepairedPhoto(UploadedFile repairedPhoto) {
		this.repairedPhoto = repairedPhoto;
	}

	public UploadedFile getAccidentDocument() {
		return accidentDocument;
	}

	public void setAccidentDocument(UploadedFile accidentDocument) {
		this.accidentDocument = accidentDocument;
	}

	public boolean isAccidentStatus() {
		return accidentStatus;
	}

	public void setAccidentStatus(boolean accidentStatus) {
		this.accidentStatus = accidentStatus;
	}

	public VehicleAccident getAccident() {
		if(accident == null)
			accident = new VehicleAccident();
		return accident;
	}

	public void setAccident(VehicleAccident accident) {
		this.accident = accident;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleAccident> getAccidents() {
		boolean research = true;
		if(accidents == null || accidents.size() == 0)
			research = true;
		else if(accidents.size() > 0)
		{
			if(getPartner() != null)
			{
				if(accidents.get(0).getVehicle().getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			accidents = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle.partner", getPartner());
				params.put("active", true);
				
				Object drvs = gDAO.search("VehicleAccident", params);
				if(drvs != null)
				{
					accidents = (Vector<VehicleAccident>)drvs;
				}
			}
		}
		return accidents;
	}

	public void setAccidents(Vector<VehicleAccident> accidents) {
		this.accidents = accidents;
	}

	public void setVTrackIntV()
	{
		if(getVtrackpollinterval() < 60)
		{
			setVtrackpollinterval(60);
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Interval set to minimum of 60 seconds.");
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Interval set successfully.");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void onLocSelect()
	{
		try
		{
			Vehicle v = getSelectedVehicle();
			if(v != null)
			{
				for(VehicleLocationData vld : getVehiclesLocs())
				{
					if(vld.getVehicle().getId() == v.getId())
					{
						setCenterCoor(vld.getLat() + "," + vld.getLon());
						break;
					}
				}
			}
		}
		catch(Exception ex)
		{
			setCenterCoor(defaultCenterCoor);
		}
	}
	
	public MapModel getVtrackingModel() {
		vtrackingModel = new DefaultMapModel();
		
		for(VehicleLocationData vld : getVehiclesLocs())
		{
			try
			{
				LatLng coord1 = new LatLng(vld.getLat(), vld.getLon());
				//Basic marker
				vtrackingModel.addOverlay(new Marker(coord1, vld.getVehicle().getRegistrationNo()));
			}
			catch(Exception ex)
			{}
		}
		return vtrackingModel;
	}

	public void setVtrackingModel(MapModel vtrackingModel) {
		this.vtrackingModel = vtrackingModel;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}
	
	public void resetVehicleLocs(int i)
	{
		setVehiclesLocs(null);
		if(i == 1)
			resetVehicles();
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleLocationData> getVehiclesLocs() {
		if(vehiclesLocs == null)
		{
			vehiclesLocs = new Vector<VehicleLocationData>();
			if(getVehicles() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Vector<Vehicle> dvsList = getVehicles();
				if(dvsList != null)
				for(Vehicle v : dvsList)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("active", true);
					params.put("vehicle", v);
					Object vlocObj = gDAO.search("VehicleLocationData", params);
					if(vlocObj != null)
					{
						Vector<VehicleLocationData> vloc = (Vector<VehicleLocationData>)vlocObj;
						if(vloc.size() > 0)
						{
							vehiclesLocs.add(vloc.get(0));
						}
					}
					
				}
			}
		}
		
		if(vehiclesLocs.size() > 0)
			setCenterCoor(vehiclesLocs.get(0).getLat() + "," + vehiclesLocs.get(0).getLon());
		else
			setCenterCoor(defaultCenterCoor);
		return vehiclesLocs;
	}

	public void setVehiclesLocs(Vector<VehicleLocationData> vehiclesLocs) {
		this.vehiclesLocs = vehiclesLocs;
	}

	public String getCenterCoor() {
		return centerCoor;
	}

	public void setCenterCoor(String centerCoor) {
		this.centerCoor = centerCoor;
	}

	public int getVtrackpollinterval() {
		return vtrackpollinterval;
	}

	public void setVtrackpollinterval(int vtrackpollinterval) {
		this.vtrackpollinterval = vtrackpollinterval;
	}

	public VehicleFueling getFueling() {
		if(fueling == null)
			fueling = new VehicleFueling();
		return fueling;
	}

	public void setFueling(VehicleFueling fueling) {
		this.fueling = fueling;
	}

	public void resetFuelings()
	{
		setFuelings(null);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<VehicleFueling> getFuelings() {
		boolean research = true;
		if(fuelings == null || fuelings.size() == 0)
			research = true;
		else if(fuelings.size() > 0)
		{
			if(getPartner() != null)
			{
				if(fuelings.get(0).getVehicle().getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			fuelings = null;
			if(getPartner() != null && getStdt() != null && getEddt() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from VehicleFueling e where e.vehicle.partner=:partner and e.captured_dt between :stdt and :eddt");
				q.setParameter("partner", getPartner());
				q.setParameter("stdt", getStdt());
				q.setParameter("eddt", getEddt());
				
				Object drvs = gDAO.search(q, 0);
				if(drvs != null)
				{
					fuelings = (Vector<VehicleFueling>)drvs;
				}
			}
		}
		return fuelings;
	}

	public void setFuelings(Vector<VehicleFueling> fuelings) {
		this.fuelings = fuelings;
	}

	public Long getLicenseType_id() {
		return licenseType_id;
	}

	public void setLicenseType_id(Long licenseType_id) {
		this.licenseType_id = licenseType_id;
	}

	public Long getTransactionType_id() {
		return transactionType_id;
	}

	public void setTransactionType_id(Long transactionType_id) {
		this.transactionType_id = transactionType_id;
	}

	public UploadedFile getLicenseDocument() {
		return licenseDocument;
	}

	public void setLicenseDocument(UploadedFile licenseDocument) {
		this.licenseDocument = licenseDocument;
	}

	public VehicleLicense getLicense() {
		if(license == null)
			license = new VehicleLicense();
		return license;
	}

	public void setLicense(VehicleLicense license) {
		this.license = license;
	}

	public void resetLicenses()
	{
		setLicenses(null);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<VehicleLicense> getLicenses() {
		boolean research = true;
		if(licenses == null || licenses.size() == 0)
			research = true;
		else if(licenses.size() > 0)
		{
			if(getPartner() != null)
			{
				if(licenses.get(0).getVehicle().getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			licenses = null;
			if(getPartner() != null && getStdt() != null && getEddt() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from VehicleLicense e where e.vehicle.partner=:partner and e.tran_dt between :stdt and :eddt");
				q.setParameter("partner", getPartner());
				q.setParameter("stdt", getStdt());
				q.setParameter("eddt", getEddt());
				
				Object drvs = gDAO.search(q, 0);
				if(drvs != null)
				{
					licenses = (Vector<VehicleLicense>)drvs;
				}
			}
		}
		return licenses;
	}

	public void setLicenses(Vector<VehicleLicense> licenses) {
		this.licenses = licenses;
	}

	public boolean isSelectAll() {
		return selectAll;
	}

	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}
	
}
