package com.dexter.fms.mbean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.persistence.Query;
import javax.xml.rpc.ServiceException;

import org.datacontract.schemas._2004._07.ZONIntegrationWCFService.EResult;
import org.datacontract.schemas._2004._07.ZONIntegrationWCFService.SATTRACKUnitEventResult;
import org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEvent;
import org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEventEEvent;
import org.tempuri.IZONService;
import org.tempuri.ZONServiceLocator;

import com.dexter.common.util.Hasher;
import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.ApplicationSetup;
import com.dexter.fms.model.MFunction;
import com.dexter.fms.model.MRole;
import com.dexter.fms.model.MRoleFunction;
import com.dexter.fms.model.Module;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.PartnerSubscription;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.PartnerUserRole;
import com.dexter.fms.model.app.CorporateTrip;
import com.dexter.fms.model.app.ExpenseType;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleFuelData;
import com.dexter.fms.model.app.VehicleFueling;
import com.dexter.fms.model.app.VehicleLicense;
import com.dexter.fms.model.app.VehicleLocationData;
import com.dexter.fms.model.app.VehicleOdometerData;
import com.dexter.fms.model.ref.FuelType;
import com.dexter.fms.model.ref.ItemType;
import com.dexter.fms.model.ref.LicenseType;
import com.dexter.fms.model.ref.ServiceType;
import com.dexter.fms.model.ref.TransactionType;

@ManagedBean(name = "appBean")
@ApplicationScoped
public class ApplicationMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-ApplicationMBean");
	
	private FacesMessage msg = null;
	
	private List<PartnerUser> activeUsers;
	
	private boolean appSetup;
	
	private Timer timer, timer2, trackerTimer;
    private TimerTask task, task2, trackerTask;
    
    private int tracker_interval = 2; // interval in minutes
	private Long lastReceivedId = 0L;
	
	public ApplicationMBean()
	{
		// should start the interval downloads here...
    	task = new TimerTask()
    	{
            @Override
            public void run()
            {
                logger.info("Starting subscription update at: " + new Date());
                checkSubscriptions();
                logger.info("Finished subscription update at: " + new Date());
                
                logger.info("Starting license update at: " + new Date());
                checkLicenses();
                logger.info("Finished license update at: " + new Date());
            }
        };
        
        task2 = new TimerTask()
    	{
            @Override
            public void run()
            {
                logger.info("Starting trips update at: " + new Date());
                checkTrips();
                logger.info("Finished trips update at: " + new Date());
            }
        };
        
        timer = new Timer();
        timer.scheduleAtFixedRate(task, new Date(), 1000 * 60 * 60 * 1); // 1 hour(s)
        
        timer2 = new Timer();
        timer2.scheduleAtFixedRate(task2, new Date(), 1000 * 60 * 5); // 5 minutes
        
        trackerTimer = new Timer();
		trackerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				logger.info("Starting tracker info download from the tracker web service");
				
				ZONServiceLocator zonLocator = new ZONServiceLocator();
				try
				{
					Calendar c = Calendar.getInstance();
					Calendar c2 = Calendar.getInstance();
					
					c.add(Calendar.MINUTE, -tracker_interval);
					
					IZONService zonService = zonLocator.getBasicHttpBinding_IZONService();
					SATTRACKUnitEventResult result = zonService.SATTRAK_GetDailyEvents(c, c2, lastReceivedId);
					logger.info("tracker service result: " + result.getResult());
					if(result.getResult() == EResult.Ok)
					{
						lastReceivedId = result.getLastQueriedId();
						UnitEvent[] unitEvents = result.getUnitEvents();
						if(unitEvents != null && unitEvents.length > 0)
						{
							GeneralDAO gDAO = new GeneralDAO();
							
							try
					    	{
					    		gDAO.startTransaction();
					            for(UnitEvent e : unitEvents)
								{
					            	Query q = gDAO.createQuery("Select e from Vehicle e where e.zonControlId=:zonControlId");
					            	q.setParameter("zonControlId", e.getUnitID());
					            	
					            	Object vObj = gDAO.search(q, 1);
					            	if(vObj != null)
					            	{
					            		Vehicle v = (Vehicle)vObj;
					            		
					            		VehicleLocationData vld = new VehicleLocationData();
					            		vld.setVehicle(v);
					            		vld.setCaptured_dt(e.getEventTime().getTime());
					            		vld.setCrt_dt(new Date());
					            		//TODO: Where do we get the lon and lat from
					            		//vld.setLat(lat);
					            		//vld.setLon(lon);
					            		//gDAO.save(vld);
					            		
					            		VehicleOdometerData vod = new VehicleOdometerData();
					            		vod.setVehicle(v);
					            		vod.setCaptured_dt(e.getEventTime().getTime());
					            		vod.setCrt_dt(new Date());
					            		vod.setOdometer(e.getDistance());
					            		vod.setSource("Tracker");
					            		
					            		gDAO.save(vod);
					            		
					            		if(e.getEventType().getValue().equals(UnitEventEEvent.Fuel_Refueled) || 
					            				e.getEventType().getValue().equals(UnitEventEEvent.Fuel_Drop))
					            		{
					            			VehicleFueling vf = new VehicleFueling();
					            			vf.setVehicle(v);
					            			vf.setCaptured_dt(e.getEventTime().getTime());
						            		vf.setCrt_dt(new Date());
						            		vf.setOdometer(e.getDistance());
						            		vf.setFuelLevel(e.getEventFinalValue());
						            		vf.setAmt(e.getEventValue());
						            		vf.setLitres(e.getEventFinalValue() - e.getEventInitValue());
						            		vf.setLocation(e.getAddress());
						            		
						            		gDAO.save(vf);
						            		
						            		if(e.getEventType().getValue().equals(UnitEventEEvent.Fuel_Refueled))
						            		{
						            			// TODO: send email alert here
						            		}
						            		
						            		VehicleFuelData vfd = new VehicleFuelData();
						            		vfd.setCaptured_dt(e.getEventTime().getTime());
						            		vfd.setCrt_dt(new Date());
						            		vfd.setFuelLevel(e.getEventFinalValue());
						            		vfd.setSource("Tracker");
						            		vfd.setVehicle(v);
						            		
						            		gDAO.save(vfd);
						            		
						            		if(e.getEventFinalValue() == 0)
						            		{
						            			// TODO: alert here, empty tank
						            		}
					            		}
					            	}
								}
								gDAO.commit();
								logger.info("Commited database storage transaction for the downloaded events");
					    	}
							catch(Exception ex)
					    	{
					    		gDAO.rollback();
					    		logger.log(Level.SEVERE, "Persist failed for galoli event record. " + ex);
					    	}
							finally
							{
								gDAO.destroy();
								logger.info("Closed database connection.");
							}
						}
					}
				}
				catch(ServiceException ex)
            	{
					ex.printStackTrace();
				}
            	catch(RemoteException ex)
            	{
					ex.printStackTrace();
				}
			}
		};
		
		Calendar c = Calendar.getInstance();
    	trackerTimer.scheduleAtFixedRate(trackerTask, c.getTime(), 1000 * 60 * tracker_interval); // every 2 minutes after first call
	}
	
	@SuppressWarnings("unchecked")
	private void checkSubscriptions()
	{
		GeneralDAO gDAO = new GeneralDAO();
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("expired", false);
		Object subsObj = gDAO.search("PartnerSubscription", params);
		if(subsObj != null)
		{
			Vector<PartnerSubscription> subs = (Vector<PartnerSubscription>)subsObj;
			gDAO.startTransaction();
			for(PartnerSubscription e : subs)
			{
				Date now = new Date();
				boolean change = false;
				if(e.getStart_dt().before(now) && e.getEnd_dt().after(now) && !e.isActive())
				{
					e.setActive(true);
					e.setExpired(false);
					change = true;
				}
				else if(e.isActive() && e.getEnd_dt().before(now))
				{
					e.setExpired(true);
					e.setActive(false);
					change = true;
				}
				if(change)
					gDAO.update(e);
			}
			gDAO.commit();
			gDAO.destroy();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void checkLicenses()
	{
		GeneralDAO gDAO = new GeneralDAO();
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("expired", false);
		Object subsObj = gDAO.search("VehicleLicense", params);
		if(subsObj != null)
		{
			Vector<VehicleLicense> subs = (Vector<VehicleLicense>)subsObj;
			gDAO.startTransaction();
			for(VehicleLicense e : subs)
			{
				Date now = new Date();
				boolean change = false;
				if(e.getLic_start_dt().before(now) && e.getLic_end_dt().after(now) && !e.isActive())
				{
					e.setActive(true);
					e.setExpired(false);
					change = true;
				}
				else if(e.isActive() && e.getLic_end_dt().before(now))
				{
					e.setExpired(true);
					e.setActive(false);
					change = true;
				}
				if(change)
					gDAO.update(e);
			}
			gDAO.commit();
			gDAO.destroy();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void checkTrips()
	{
		GeneralDAO gDAO = new GeneralDAO();
		
		Query q = gDAO.createQuery("Select e from CorporateTrip e where e.approvalStatus=:approvalStatus and e.tripStatus=:tripStatus and e.departureDateTime < :nowDateTime");
		q.setParameter("approvalStatus", "APPROVED");
		q.setParameter("tripStatus", null);
		q.setParameter("nowDateTime", new Date());
		
		gDAO.startTransaction();
		
		Object tripsObj = gDAO.search(q, 0);
		if(tripsObj != null)
		{
			Vector<CorporateTrip> trips = (Vector<CorporateTrip>)tripsObj;
			for(CorporateTrip e : trips)
			{
				e.setTripStatus("ON_TRIP");
				gDAO.update(e);
			}
		}
		
		q = gDAO.createQuery("Select e from CorporateTrip e where e.approvalStatus=:approvalStatus and e.tripStatus=:tripStatus and e.estimatedArrivalDateTime < :nowDateTime");
		q.setParameter("approvalStatus", "APPROVED");
		q.setParameter("tripStatus", "ON_TRIP");
		q.setParameter("nowDateTime", new Date());
		
		tripsObj = gDAO.search(q, 0);
		if(tripsObj != null)
		{
			Vector<CorporateTrip> trips = (Vector<CorporateTrip>)tripsObj;
			for(CorporateTrip e : trips)
			{
				e.setTripStatus("SHOULD_BE_COMPLETED");
				gDAO.update(e);
			}
		}
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -7);
		
		q = gDAO.createQuery("Select e from CorporateTrip e where e.approvalStatus=:approvalStatus and e.tripStatus=:tripStatus and e.estimatedArrivalDateTime < :maxwaitDateTime");
		q.setParameter("approvalStatus", "APPROVED");
		q.setParameter("tripStatus", "SHOULD_BE_COMPLETED");
		q.setParameter("maxwaitDateTime", c.getTime());
		
		tripsObj = gDAO.search(q, 0);
		if(tripsObj != null)
		{
			Vector<CorporateTrip> trips = (Vector<CorporateTrip>)tripsObj;
			for(CorporateTrip e : trips)
			{
				e.setTripStatus("COMPLETED");
				e.setCompletedDateTime(new Date());
				gDAO.update(e);
			}
		}
	}
	
	public String getRandomNumber()
	{
		return ""+new Random().nextInt(10000);
	}
	
	public boolean alreadyLoggedIn(PartnerUser user)
	{
		boolean ret = false;
		
		for(PartnerUser u : getActiveUsers())
		{
			if(u.getUsername().equals(user.getUsername()) && u.getPartner_code().equals(user.getPartner_code()))
			{
				ret = true;
				break;
			}
		}
		
		return ret;
	}
	
	public boolean addLoggedInUser(PartnerUser user)
	{
		boolean ret = false;
		
		//ret = alreadyLoggedIn(user);
		if(!ret)
		{
			//getActiveUsers().add(user);
			ret = true;
		}
		else
			ret = false;
		
		return ret;
	}
	
	public void forceLoggedOut(PartnerUser user)
	{
		for(PartnerUser u : getActiveUsers())
		{
			if(u.getUsername().equals(user.getUsername()) && u.getPartner_code().equals(user.getPartner_code()))
			{
				u.getSession().invalidate();
				getActiveUsers().remove(u);
				break;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setup()
	{
		logger.log(Level.INFO, "Starting setup process...");
		GeneralDAO gDAO = new GeneralDAO();
		gDAO.startTransaction();
		logger.log(Level.INFO, "Transaction started");
		
		//Refs
		FuelType ft = new FuelType();
		ft.setName("Disel");
		gDAO.save(ft);
		ft = new FuelType();
		ft.setName("Petrol");
		gDAO.save(ft);
		
		LicenseType lt = new LicenseType();
		lt.setName("Vehicle License");
		gDAO.save(lt);
		lt = new LicenseType();
		lt.setName("Premium Insurance");
		gDAO.save(lt);
		lt = new LicenseType();
		lt.setName("Third-Party Insurance");
		gDAO.save(lt);
		lt = new LicenseType();
		lt.setName("Road Worthiness");
		gDAO.save(lt);
		lt = new LicenseType();
		lt.setName("Hackney Permit");
		gDAO.save(lt);
		
		TransactionType tt = new TransactionType();
		tt.setName("Registration");
		gDAO.save(tt);
		tt = new TransactionType();
		tt.setName("Renewal");
		gDAO.save(tt);
		
		ServiceType st = new ServiceType();
		st.setName("Vehicle Sales");
		st.setCrt_dt(new Date());
		st.setDescription("Render sales of vehicles");
		st.setSystemObj(true);
		gDAO.save(st);
		st = new ServiceType();
		st.setName("Vehicle Servicing");
		st.setCrt_dt(new Date());
		st.setDescription("Render maintenance service of vehicles");
		st.setSystemObj(true);
		gDAO.save(st);
		st = new ServiceType();
		st.setName("Insurance");
		st.setCrt_dt(new Date());
		st.setDescription("Render insurance of various types for vehicles");
		st.setSystemObj(true);
		gDAO.save(st);
		st = new ServiceType();
		st.setName("Vehicle Parts Sales");
		st.setCrt_dt(new Date());
		st.setDescription("Render sales of various vehicle parts, e.g. Tires, Battery and so on");
		st.setSystemObj(true);
		gDAO.save(st);
		st = new ServiceType();
		st.setName("Vehicle Repair");
		st.setCrt_dt(new Date());
		st.setDescription("Render repairs services for vehicles");
		st.setSystemObj(true);
		gDAO.save(st);
		st = new ServiceType();
		st.setName("Vehicle License");
		st.setCrt_dt(new Date());
		st.setDescription("Render various types of vehicle license");
		st.setSystemObj(true);
		gDAO.save(st);
		
		ItemType ity = new ItemType();
		ity.setName("Head");
		gDAO.save(ity);
		ity = new ItemType();
		ity.setName("Tail");
		gDAO.save(ity);
		
		ExpenseType et = new ExpenseType();
		et.setCrt_dt(new Date());
		et.setDescription("Fueling");
		et.setName("Fueling");
		et.setSystemObj(true);
		gDAO.save(et);
		
		// create a default partner
		Partner sattrak = new Partner();
		sattrak.setName("Sattrak");
		sattrak.setSattrak(true);
		sattrak.setCrt_dt(new Date());
		gDAO.save(sattrak);
		logger.log(Level.INFO, "Saving partner..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		Long p_id = sattrak.getId();
		if(p_id != null)
		{
			long count = 1;
			Object partners = gDAO.findAll("Partner");
			if(partners != null)
			{
				List<Partner> partnersList = (List<Partner>)partners;
				if(partnersList != null)
					count = partnersList.size();
			}
			
			String p_id_str = ""+count;
			switch(p_id_str.length())
			{
			case 1:
			{
				p_id_str = "P0000" + p_id_str;
				break;
			}
			case 2:
			{
				p_id_str = "P000" + p_id_str;
				break;
			}
			case 3:
			{
				p_id_str = "P00" + p_id_str;
				break;
			}
			case 4:
			{
				p_id_str = "P0" + p_id_str;
				break;
			}
			case 5:
			{
				p_id_str = "P" + p_id_str;
				break;
			}
			default:
			{
				p_id_str = "P" + p_id_str;
				break;
			}
			}
			
			sattrak.setCode(p_id_str);
			gDAO.update(sattrak);
			logger.log(Level.INFO, "Updating partner with code: '" + p_id_str + "'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		}
		
		//create default user
		PartnerPersonel pp = new PartnerPersonel();
		pp.setFirstname("Victor");
		pp.setLastname("Okere");
		pp.setEmail("okerevictor@gmail.com");
		pp.setPartner(sattrak);
		pp.setCrt_dt(new Date());
		pp.setHasUser(true);
		gDAO.save(pp);
		logger.log(Level.INFO, "Saving user: 'admin'" + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		PartnerUser adminUser = new PartnerUser();
		adminUser.setPersonel(pp);
		adminUser.setPartner(sattrak);
		adminUser.setPartner_code(sattrak.getCode());
		adminUser.setUsername("admin");
		adminUser.setPassword(Hasher.getHashValue("admin"));
		adminUser.setActive(true);
		adminUser.setCrt_dt(new Date());
		adminUser.setAdmin(true);
		gDAO.save(adminUser);
		logger.log(Level.INFO, "Saving user: 'admin'" + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		sattrak.setCreatedBy(adminUser);
		gDAO.update(sattrak);
		
		pp.setCreatedBy(adminUser);
		gDAO.update(pp);
		
		// create modules
		Module appManagement = new Module();
		appManagement.setName("Application Management");
		appManagement.setDisplay_name("Settings");
		appManagement.setIcon_url("applications.png");
		appManagement.setDescription("Application Management module consists of functions related to seting up the platform for use.");
		appManagement.setMain_page_url("app_management");
		appManagement.setActive(true);
		appManagement.setCrt_dt(new Date());
		appManagement.setCreatedBy(adminUser);
		gDAO.save(appManagement);
		logger.log(Level.INFO, "Saving module: 'Application Management'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		Module partnerManagement = new Module();
		partnerManagement.setName("Partners Management");
		partnerManagement.setDisplay_name("Partners");
		partnerManagement.setIcon_url("group-2.png");
		partnerManagement.setDescription("Partners Management module consists of functions for managing your customers accounts.");
		partnerManagement.setMain_page_url("partner_management");
		partnerManagement.setActive(true);
		partnerManagement.setCrt_dt(new Date());
		partnerManagement.setCreatedBy(adminUser);
		gDAO.save(partnerManagement);
		logger.log(Level.INFO, "Saving module: 'Partners Management'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		Module userManagement = new Module();
		userManagement.setName("Users Management");
		userManagement.setDisplay_name("Users");
		userManagement.setIcon_url("admin-user-2.png");
		userManagement.setDescription("Users Management module consists of functions for managing your customers accounts.");
		userManagement.setMain_page_url("user_management");
		userManagement.setActive(true);
		userManagement.setCrt_dt(new Date());
		userManagement.setCreatedBy(adminUser);
		gDAO.save(userManagement);
		logger.log(Level.INFO, "Saving module: 'Users Management'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		Module fleetManagement = new Module();
		fleetManagement.setName("Fleets Management");
		fleetManagement.setDisplay_name("Fleets");
		fleetManagement.setIcon_url("truck-2.png");
		fleetManagement.setDescription("Fleets Management module consists of functions for managing your fleets.");
		fleetManagement.setMain_page_url("fleet_management");
		fleetManagement.setActive(true);
		fleetManagement.setCrt_dt(new Date());
		fleetManagement.setCreatedBy(adminUser);
		gDAO.save(fleetManagement);
		logger.log(Level.INFO, "Saving module: 'Fleets Management'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		Module vehicleManagement = new Module();
		vehicleManagement.setName("Vehicles Management");
		vehicleManagement.setDisplay_name("Vehicles MGT.");
		vehicleManagement.setIcon_url("tools.png");
		vehicleManagement.setDescription("Vehicles Management module consists of functions for managing your vehicles for example routine & ad hoc maintenance and accidents.");
		vehicleManagement.setMain_page_url("vehicles_management");
		vehicleManagement.setActive(true);
		vehicleManagement.setCrt_dt(new Date());
		vehicleManagement.setCreatedBy(adminUser);
		gDAO.save(vehicleManagement);
		logger.log(Level.INFO, "Saving module: 'Vehicles Management'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		Module budgetManagement = new Module();
		budgetManagement.setName("Budget and Expense Management");
		budgetManagement.setDisplay_name("Budgets, Expenses");
		budgetManagement.setIcon_url("money.png");
		budgetManagement.setDescription("Budget and Expense Management module consists of functions for managing your budget and expenses.");
		budgetManagement.setMain_page_url("expense_management");
		budgetManagement.setActive(true);
		budgetManagement.setCrt_dt(new Date());
		budgetManagement.setCreatedBy(adminUser);
		gDAO.save(budgetManagement);
		logger.log(Level.INFO, "Saving module: 'Budget and Expense Management'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		Module assetManagement = new Module();
		assetManagement.setName("Asset Management");
		assetManagement.setDisplay_name("Assets");
		assetManagement.setIcon_url("apartment-building.png");
		assetManagement.setDescription("Asset Management module consists of functions for managing your assets.");
		assetManagement.setMain_page_url("asset_management");
		assetManagement.setActive(true);
		assetManagement.setCrt_dt(new Date());
		assetManagement.setCreatedBy(adminUser);
		gDAO.save(assetManagement);
		logger.log(Level.INFO, "Saving module: 'Asset Management'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		Module transactions = new Module();
		transactions.setName("Transactions");
		transactions.setDisplay_name("Transactions");
		transactions.setIcon_url("books.png");
		transactions.setDescription("Transactions module has functions for managing various transaction with vendor's including registration/renewal of vehicle license and insurance and so on.");
		transactions.setMain_page_url("transactions_management");
		transactions.setActive(true);
		transactions.setCrt_dt(new Date());
		transactions.setCreatedBy(adminUser);
		gDAO.save(transactions);
		logger.log(Level.INFO, "Saving module: 'Transactions'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		// create functions
		MFunction function1 = new MFunction();
		function1.setName("Manage partners");
		function1.setDescription("Manage your customers.");
		function1.setPage_url("manage_partners");
		function1.setModule(partnerManagement);
		function1.setActive(true);
		function1.setCrt_dt(new Date());
		gDAO.save(function1);
		logger.log(Level.INFO, "Saving function: 'Manage partner'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function2 = new MFunction();
		function2.setName("Manage Partners Subscription");
		function2.setDescription("Manage your customers subscriptions.");
		function2.setPage_url("manage_partner_subs");
		function2.setModule(partnerManagement);
		function2.setActive(true);
		function2.setCrt_dt(new Date());
		gDAO.save(function2);
		logger.log(Level.INFO, "Saving function: 'Manage Partner Subscription'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function3 = new MFunction();
		function3.setName("Manage Application Types");
		function3.setDescription("Manage the various kinds of applications the platform provides.");
		function3.setPage_url("manage_app_types");
		function3.setModule(appManagement);
		function3.setActive(true);
		function3.setCrt_dt(new Date());
		gDAO.save(function3);
		logger.log(Level.INFO, "Saving function: 'Manage Application Types'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function4 = new MFunction();
		function4.setName("Manage Modules");
		function4.setDescription("Manage the categories of the functions the platform provides.");
		function4.setPage_url("manage_modules");
		function4.setModule(appManagement);
		function4.setActive(true);
		function4.setCrt_dt(new Date());
		gDAO.save(function4);
		logger.log(Level.INFO, "Saving function: 'Manage Modules'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function5 = new MFunction();
		function5.setName("Manage Functions");
		function5.setDescription("Change the categories of the functions the platform provides.");
		function5.setPage_url("manage_functions");
		function5.setModule(appManagement);
		function5.setActive(true);
		function5.setCrt_dt(new Date());
		gDAO.save(function5);
		logger.log(Level.INFO, "Saving function: 'Manage Functions'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function51 = new MFunction();
		function51.setName("Manage Vehicle Models");
		function51.setDescription("Create and manage various vehicle models.");
		function51.setPage_url("manage_vehicle_types_makes");
		function51.setModule(appManagement);
		function51.setActive(true);
		function51.setCrt_dt(new Date());
		gDAO.save(function51);
		logger.log(Level.INFO, "Saving function: 'Manage Vehicle Models'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function61 = new MFunction();
		function61.setName("Manage Departments");
		function61.setDescription("Manage your company departments.");
		function61.setPage_url("manage_depts");
		function61.setModule(userManagement);
		function61.setActive(true);
		function61.setCrt_dt(new Date());
		gDAO.save(function61);
		logger.log(Level.INFO, "Saving function: 'Manage Departments'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function62 = new MFunction();
		function62.setName("Manage Regions");
		function62.setDescription("Manage your region distribution.");
		function62.setPage_url("manage_regions");
		function62.setModule(userManagement);
		function62.setActive(true);
		function62.setCrt_dt(new Date());
		gDAO.save(function62);
		logger.log(Level.INFO, "Saving function: 'Manage Personels'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function63 = new MFunction();
		function63.setName("Manage Personels");
		function63.setDescription("Manage your personels.");
		function63.setPage_url("manage_staffs");
		function63.setModule(userManagement);
		function63.setActive(true);
		function63.setCrt_dt(new Date());
		gDAO.save(function63);
		logger.log(Level.INFO, "Saving function: 'Manage Personels'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function64 = new MFunction();
		function64.setName("Manage Drivers");
		function64.setDescription("Manage your drivers.");
		function64.setPage_url("manage_drivers");
		function64.setModule(userManagement);
		function64.setActive(true);
		function64.setCrt_dt(new Date());
		gDAO.save(function64);
		logger.log(Level.INFO, "Saving function: 'Manage Drivers'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function6 = new MFunction();
		function6.setName("Manage Roles");
		function6.setDescription("Manage your user roles.");
		function6.setPage_url("manage_roles");
		function6.setModule(userManagement);
		function6.setActive(true);
		function6.setCrt_dt(new Date());
		gDAO.save(function6);
		logger.log(Level.INFO, "Saving function: 'Manage Roles'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function7 = new MFunction();
		function7.setName("Manage Users");
		function7.setDescription("Manage your users.");
		function7.setPage_url("manage_users");
		function7.setModule(userManagement);
		function7.setActive(true);
		function7.setCrt_dt(new Date());
		gDAO.save(function7);
		logger.log(Level.INFO, "Saving function: 'Manage Users'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function71 = new MFunction();
		function71.setName("Manage Fleets");
		function71.setDescription("Manage your fleets.");
		function71.setPage_url("manage_fleets");
		function71.setModule(fleetManagement);
		function71.setActive(true);
		function71.setCrt_dt(new Date());
		gDAO.save(function71);
		logger.log(Level.INFO, "Saving function: 'Manage Fleets'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function72 = new MFunction();
		function72.setName("Manage Vehicles");
		function72.setDescription("Manage your vehicles.");
		function72.setPage_url("manage_vehicles");
		function72.setModule(fleetManagement);
		function72.setActive(true);
		function72.setCrt_dt(new Date());
		gDAO.save(function72);
		logger.log(Level.INFO, "Saving function: 'Manage Vehicles'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function731 = new MFunction();
		function731.setName("Tracker Info");
		function731.setDescription("View the latest tracker information for your vehicles.");
		function731.setPage_url("latest_v_trackerinfo");
		function731.setModule(fleetManagement);
		function731.setActive(true);
		function731.setCrt_dt(new Date());
		gDAO.save(function731);
		logger.log(Level.INFO, "Saving function: 'Tracker Info'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function73 = new MFunction();
		function73.setName("Tracker Info - Archive");
		function73.setDescription("View tracker information for your vehicles.");
		function73.setPage_url("manage_v_trackerinfo");
		function73.setModule(fleetManagement);
		function73.setActive(true);
		function73.setCrt_dt(new Date());
		gDAO.save(function73);
		logger.log(Level.INFO, "Saving function: 'Tracker Info - Archive'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function74 = new MFunction();
		function74.setName("Setup Routine Maintenace");
		function74.setDescription("Setup routine maintenance for your vehicles.");
		function74.setPage_url("setup_v_rmaintenance");
		function74.setModule(vehicleManagement);
		function74.setActive(true);
		function74.setCrt_dt(new Date());
		gDAO.save(function74);
		logger.log(Level.INFO, "Saving function: 'Setup Routine Maintenace'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function742 = new MFunction();
		function742.setName("Manage Routine Maintenace");
		function742.setDescription("Start and manage routine maintenance for your vehicles.");
		function742.setPage_url("manage_v_rmaintenance");
		function742.setModule(vehicleManagement);
		function742.setActive(true);
		function742.setCrt_dt(new Date());
		gDAO.save(function742);
		logger.log(Level.INFO, "Saving function: 'Manage Routine Maintenace'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function743 = new MFunction();
		function743.setName("Request Ad-Hoc Maintenace");
		function743.setDescription("Request ad hoc maintenance for your vehicles.");
		function743.setPage_url("request_v_ahmaintenance");
		function743.setModule(vehicleManagement);
		function743.setActive(true);
		function743.setCrt_dt(new Date());
		gDAO.save(function743);
		logger.log(Level.INFO, "Saving function: 'Request Ad-Hoc Maintenace'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function744 = new MFunction();
		function744.setName("Manage Ad-Hoc Maintenace");
		function744.setDescription("Manage ad hoc maintenance for your vehicles.");
		function744.setPage_url("manage_v_ahmaintenance");
		function744.setModule(vehicleManagement);
		function744.setActive(true);
		function744.setCrt_dt(new Date());
		gDAO.save(function744);
		logger.log(Level.INFO, "Saving function: 'Manage Ad-Hoc Maintenace'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function741 = new MFunction();
		function741.setName("Accidents");
		function741.setDescription("Capture and update accidents information for your vehicles.");
		function741.setPage_url("manage_v_accidents");
		function741.setModule(vehicleManagement);
		function741.setActive(true);
		function741.setCrt_dt(new Date());
		gDAO.save(function741);
		logger.log(Level.INFO, "Saving function: 'Accidents'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function745 = new MFunction();
		function745.setName("Approve Repairs");
		function745.setDescription("Approve or deny accident repairs.");
		function745.setPage_url("manage_pending_accidents");
		function745.setModule(vehicleManagement);
		function745.setActive(true);
		function745.setCrt_dt(new Date());
		gDAO.save(function745);
		logger.log(Level.INFO, "Saving function: 'Approve Repairs'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function75 = new MFunction();
		function75.setName("Fueling");
		function75.setDescription("Capture and view fueling information for your vehicles.");
		function75.setPage_url("manage_v_fuelinfo");
		function75.setModule(transactions);
		function75.setActive(true);
		function75.setCrt_dt(new Date());
		gDAO.save(function75);
		logger.log(Level.INFO, "Saving function: 'Fueling Info'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function76 = new MFunction();
		function76.setName("License");
		function76.setDescription("Capture, view and update various kind of license registration/renewal information for your vehicles.");
		function76.setPage_url("manage_v_licenseinfo");
		function76.setModule(transactions);
		function76.setActive(true);
		function76.setCrt_dt(new Date());
		gDAO.save(function76);
		logger.log(Level.INFO, "Saving function: 'License Info'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction reportsFunction = new MFunction();
		reportsFunction.setName("Reports");
		reportsFunction.setDescription("Various kind of reports");
		reportsFunction.setPage_url("reports");
		reportsFunction.setModule(transactions);
		reportsFunction.setActive(true);
		reportsFunction.setCrt_dt(new Date());
		gDAO.save(reportsFunction);
		logger.log(Level.INFO, "Saving function: 'Reports'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function77 = new MFunction();
		function77.setName("Location Info");
		function77.setDescription("View current and historic location information for your vehicles.");
		function77.setPage_url("manage_v_locationinfo");
		function77.setModule(fleetManagement);
		function77.setActive(true);
		function77.setCrt_dt(new Date());
		gDAO.save(function77);
		logger.log(Level.INFO, "Saving function: 'Location Info'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function78 = new MFunction();
		function78.setName("Driver Info");
		function78.setDescription("View current and historic driver information for your vehicles.");
		function78.setPage_url("manage_v_driverinfo");
		function78.setModule(fleetManagement);
		function78.setActive(true);
		function78.setCrt_dt(new Date());
		gDAO.save(function78);
		logger.log(Level.INFO, "Saving function: 'Driver Info'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function79 = new MFunction();
		function79.setName("Trip Request");
		function79.setDescription("Make a trip request.");
		function79.setPage_url("trip_request");
		function79.setModule(fleetManagement);
		function79.setActive(true);
		function79.setCrt_dt(new Date());
		gDAO.save(function79);
		logger.log(Level.INFO, "Saving function: 'Trip Request'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function791 = new MFunction();
		function791.setName("Attend to Trips");
		function791.setDescription("Approve or dis-approve trips.");
		function791.setPage_url("attend_trips");
		function791.setModule(fleetManagement);
		function791.setActive(true);
		function791.setCrt_dt(new Date());
		gDAO.save(function791);
		logger.log(Level.INFO, "Saving function: 'Attend to Trips'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function792 = new MFunction();
		function792.setName("Trips Info");
		function792.setDescription("View current and historic trips information for your vehicles.");
		function792.setPage_url("manage_v_tripsinfo");
		function792.setModule(fleetManagement);
		function792.setActive(true);
		function792.setCrt_dt(new Date());
		gDAO.save(function792);
		logger.log(Level.INFO, "Saving function: 'Trips Info'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function811 = new MFunction();
		function811.setName("Manage Expense Types");
		function811.setDescription("Manage your expense types.");
		function811.setPage_url("manage_expense_types");
		function811.setModule(budgetManagement);
		function811.setActive(true);
		function811.setCrt_dt(new Date());
		gDAO.save(function811);
		logger.log(Level.INFO, "Saving function: 'Manage Expense Types'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function81 = new MFunction();
		function81.setName("Manage Budgets");
		function81.setDescription("Manage your budgets.");
		function81.setPage_url("manage_budgets");
		function81.setModule(budgetManagement);
		function81.setActive(true);
		function81.setCrt_dt(new Date());
		gDAO.save(function81);
		logger.log(Level.INFO, "Saving function: 'Manage Budgets'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function82 = new MFunction();
		function82.setName("Manage Expense");
		function82.setDescription("Manage your expenses.");
		function82.setPage_url("manage_expenses");
		function82.setModule(budgetManagement);
		function82.setActive(true);
		function82.setCrt_dt(new Date());
		gDAO.save(function82);
		logger.log(Level.INFO, "Saving function: 'Manage Expenses'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function882 = new MFunction();
		function882.setName("Manage Assets");
		function882.setDescription("Manage your assets.");
		function882.setPage_url("manage_assets");
		function882.setModule(assetManagement);
		function882.setActive(true);
		function882.setCrt_dt(new Date());
		gDAO.save(function882);
		logger.log(Level.INFO, "Saving function: 'Manage Assets'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction topFunc1 = new MFunction();
		topFunc1.setName("Capture Top-up");
		topFunc1.setDescription("Insert top-ups.");
		topFunc1.setPage_url("insert_topup");
		topFunc1.setModule(transactions);
		topFunc1.setActive(true);
		topFunc1.setCrt_dt(new Date());
		gDAO.save(topFunc1);
		logger.log(Level.INFO, "Saving function: 'Capture Top-up'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction topFunc2 = new MFunction();
		topFunc2.setName("Fuel Card Top-ups");
		topFunc2.setDescription("Manage your top-ups.");
		topFunc2.setPage_url("manage_topup");
		topFunc2.setModule(transactions);
		topFunc2.setActive(true);
		topFunc2.setCrt_dt(new Date());
		gDAO.save(topFunc2);
		logger.log(Level.INFO, "Saving function: 'Manage Top-ups'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function8 = new MFunction();
		function8.setName("Manage Vendors");
		function8.setDescription("Manage your vendors.");
		function8.setPage_url("manage_vendors");
		function8.setModule(appManagement);
		function8.setActive(true);
		function8.setCrt_dt(new Date());
		gDAO.save(function8);
		logger.log(Level.INFO, "Saving function: 'Manage Vendors'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function9 = new MFunction();
		function9.setName("Create Transaction");
		function9.setDescription("Create a new transaction.");
		function9.setPage_url("create_transaction");
		function9.setModule(transactions);
		function9.setActive(true);
		function9.setCrt_dt(new Date());
		gDAO.save(function9);
		logger.log(Level.INFO, "Saving function: 'Create Transaction'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function10 = new MFunction();
		function10.setName("Manage Transactions");
		function10.setDescription("Update details of your transactions.");
		function10.setPage_url("manage_transactions");
		function10.setModule(transactions);
		function10.setActive(true);
		function10.setCrt_dt(new Date());
		gDAO.save(function10);
		logger.log(Level.INFO, "Saving function: 'Manage Transactions'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		//create role
		MRole adminRole = new MRole();
		adminRole.setName("Administrator");
		adminRole.setDescription("Sattrak admin.");
		adminRole.setDefaultRole(true);
		adminRole.setPartner(sattrak);
		adminRole.setCrt_dt(new Date());
		adminRole.setCreatedBy(adminUser);
		gDAO.save(adminRole);
		logger.log(Level.INFO, "Saving role: 'Administrator'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MRoleFunction rf1 = new MRoleFunction();
		rf1.setRole(adminRole);
		rf1.setFunction(function3);
		rf1.setCrt_dt(new Date());
		rf1.setCreatedBy(adminUser);
		gDAO.save(rf1);
		logger.log(Level.INFO, "Mapping role-function: 1..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MRoleFunction rf2 = new MRoleFunction();
		rf2.setRole(adminRole);
		rf2.setFunction(function4);
		rf2.setCrt_dt(new Date());
		rf2.setCreatedBy(adminUser);
		gDAO.save(rf2);
		logger.log(Level.INFO, "Mapping role-function: 2..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MRoleFunction rf3 = new MRoleFunction();
		rf3.setRole(adminRole);
		rf3.setFunction(function5);
		rf3.setCrt_dt(new Date());
		rf3.setCreatedBy(adminUser);
		gDAO.save(rf3);
		logger.log(Level.INFO, "Mapping role-function: 3..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf3 = new MRoleFunction();
		rf3.setRole(adminRole);
		rf3.setFunction(function51);
		rf3.setCrt_dt(new Date());
		rf3.setCreatedBy(adminUser);
		gDAO.save(rf3);
		logger.log(Level.INFO, "Mapping role-function: 51..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf3 = new MRoleFunction();
		rf3.setRole(adminRole);
		rf3.setFunction(function61);
		rf3.setCrt_dt(new Date());
		rf3.setCreatedBy(adminUser);
		gDAO.save(rf3);
		logger.log(Level.INFO, "Mapping role-function: 61..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf3 = new MRoleFunction();
		rf3.setRole(adminRole);
		rf3.setFunction(function62);
		rf3.setCrt_dt(new Date());
		rf3.setCreatedBy(adminUser);
		gDAO.save(rf3);
		logger.log(Level.INFO, "Mapping role-function: 62..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf3 = new MRoleFunction();
		rf3.setRole(adminRole);
		rf3.setFunction(function63);
		rf3.setCrt_dt(new Date());
		rf3.setCreatedBy(adminUser);
		gDAO.save(rf3);
		logger.log(Level.INFO, "Mapping role-function: 63..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf3 = new MRoleFunction();
		rf3.setRole(adminRole);
		rf3.setFunction(function64);
		rf3.setCrt_dt(new Date());
		rf3.setCreatedBy(adminUser);
		gDAO.save(rf3);
		logger.log(Level.INFO, "Mapping role-function: 64..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MRoleFunction rf4 = new MRoleFunction();
		rf4.setRole(adminRole);
		rf4.setFunction(function6);
		rf4.setCrt_dt(new Date());
		rf4.setCreatedBy(adminUser);
		gDAO.save(rf4);
		logger.log(Level.INFO, "Mapping role-function: 4..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MRoleFunction rf5 = new MRoleFunction();
		rf5.setRole(adminRole);
		rf5.setFunction(function7);
		rf5.setCrt_dt(new Date());
		rf5.setCreatedBy(adminUser);
		gDAO.save(rf5);
		logger.log(Level.INFO, "Mapping role-function: 5..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MRoleFunction rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function71);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 71..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function72);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 72..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function731);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 731..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function73);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 73..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function74);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 74..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function742);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 742..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function743);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 743..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function744);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 744..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function741);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 741..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		// 
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function745);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 745..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function75);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 75..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function76);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 76..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function77);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 77..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function78);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 78..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function79);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 79..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function791);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 791..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function792);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 792..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MRoleFunction rf6 = new MRoleFunction();
		rf6.setRole(adminRole);
		rf6.setFunction(function1);
		rf6.setCrt_dt(new Date());
		rf6.setCreatedBy(adminUser);
		gDAO.save(rf6);
		logger.log(Level.INFO, "Mapping role-function: 6..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MRoleFunction rf7 = new MRoleFunction();
		rf7.setRole(adminRole);
		rf7.setFunction(function2);
		rf7.setCrt_dt(new Date());
		rf7.setCreatedBy(adminUser);
		gDAO.save(rf7);
		logger.log(Level.INFO, "Mapping role-function: 7..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MRoleFunction rf811 = new MRoleFunction();
		rf811.setRole(adminRole);
		rf811.setFunction(function811);
		rf811.setCrt_dt(new Date());
		rf811.setCreatedBy(adminUser);
		gDAO.save(rf811);
		logger.log(Level.INFO, "Mapping role-function: 811..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MRoleFunction rf81 = new MRoleFunction();
		rf81.setRole(adminRole);
		rf81.setFunction(function81);
		rf81.setCrt_dt(new Date());
		rf81.setCreatedBy(adminUser);
		gDAO.save(rf81);
		logger.log(Level.INFO, "Mapping role-function: 81..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MRoleFunction rf82 = new MRoleFunction();
		rf82.setRole(adminRole);
		rf82.setFunction(function82);
		rf82.setCrt_dt(new Date());
		rf82.setCreatedBy(adminUser);
		gDAO.save(rf82);
		logger.log(Level.INFO, "Mapping role-function: 82..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MRoleFunction rf882 = new MRoleFunction();
		rf882.setRole(adminRole);
		rf882.setFunction(function882);
		rf882.setCrt_dt(new Date());
		rf882.setCreatedBy(adminUser);
		gDAO.save(rf882);
		logger.log(Level.INFO, "Mapping role-function: 882..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MRoleFunction rfTopU = new MRoleFunction();
		rfTopU.setRole(adminRole);
		rfTopU.setFunction(topFunc1);
		rfTopU.setCrt_dt(new Date());
		rfTopU.setCreatedBy(adminUser);
		//gDAO.save(rfTopU);
		logger.log(Level.INFO, "Mapping role-function: TopU..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rfTopU = new MRoleFunction();
		rfTopU.setRole(adminRole);
		rfTopU.setFunction(topFunc2);
		rfTopU.setCrt_dt(new Date());
		rfTopU.setCreatedBy(adminUser);
		gDAO.save(rfTopU);
		logger.log(Level.INFO, "Mapping role-function: TopU-2..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MRoleFunction rf8 = new MRoleFunction();
		rf8.setRole(adminRole);
		rf8.setFunction(function8);
		rf8.setCrt_dt(new Date());
		rf8.setCreatedBy(adminUser);
		gDAO.save(rf8);
		logger.log(Level.INFO, "Mapping role-function: 8..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MRoleFunction rf9 = new MRoleFunction();
		rf9.setRole(adminRole);
		rf9.setFunction(function9);
		rf9.setCrt_dt(new Date());
		rf9.setCreatedBy(adminUser);
		//gDAO.save(rf9);
		logger.log(Level.INFO, "Mapping role-function: 9..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MRoleFunction rf10 = new MRoleFunction();
		rf10.setRole(adminRole);
		rf10.setFunction(function10);
		rf10.setCrt_dt(new Date());
		rf10.setCreatedBy(adminUser);
		//gDAO.save(rf10);
		logger.log(Level.INFO, "Mapping role-function: 10..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MRoleFunction rreport = new MRoleFunction();
		rreport.setRole(adminRole);
		rreport.setFunction(reportsFunction);
		rreport.setCrt_dt(new Date());
		rreport.setCreatedBy(adminUser);
		gDAO.save(rreport);
		logger.log(Level.INFO, "Mapping role-function: reports..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		
		
		PartnerUserRole adminUserRole = new PartnerUserRole();
		adminUserRole.setRole(adminRole);
		adminUserRole.setUser(adminUser);
		adminUserRole.setCrt_dt(new Date());
		adminUserRole.setDefaultRole(true);
		adminUserRole.setCreatedBy(adminUser);
		gDAO.save(adminUserRole);
		logger.log(Level.INFO, "Mapping user-role: admin" + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		if(!gDAO.isHasError())
		{
			ApplicationSetup setup = new ApplicationSetup();
			setup.setAppName("FMS");
			setup.setAppSetup(true);
			setup.setCrt_dt(new Date());
			setup.setVersion("1.0");
			gDAO.save(setup);
		}
		
		gDAO.commit();
		
		msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Transaction completed successfully! Please review the logs for details.");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public List<PartnerUser> getActiveUsers() {
		if(activeUsers == null)
			activeUsers = new ArrayList<PartnerUser>();
		return activeUsers;
	}

	public void setActiveUsers(List<PartnerUser> activeUsers) {
		this.activeUsers = activeUsers;
	}

	@SuppressWarnings("unchecked")
	public boolean isAppSetup() {
		if(!appSetup)
		{
			Object allSetups = new GeneralDAO().findAll("ApplicationSetup");
			if(allSetups != null)
			{
				List<ApplicationSetup> list = (List<ApplicationSetup>)allSetups;
				if(list.size() > 0)
				{
					for(ApplicationSetup e : list)
					{
						appSetup = e.isAppSetup();
					}
				}
			}
		}
		return appSetup;
	}

	public void setAppSetup(boolean appSetup) {
		this.appSetup = appSetup;
	}
	
}
