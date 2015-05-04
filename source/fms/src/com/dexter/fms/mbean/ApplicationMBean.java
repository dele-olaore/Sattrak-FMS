package com.dexter.fms.mbean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
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

import org.json.JSONArray;
import org.json.JSONObject;

import com.dexter.common.util.Emailer;
import com.dexter.common.util.Hasher;
import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.ApplicationSetup;
import com.dexter.fms.model.MDash;
import com.dexter.fms.model.MDashRole;
import com.dexter.fms.model.MFunction;
import com.dexter.fms.model.MRole;
import com.dexter.fms.model.MRoleFunction;
import com.dexter.fms.model.MRoleReport;
import com.dexter.fms.model.Module;
import com.dexter.fms.model.Notification;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.PartnerSubscription;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.PartnerUserRole;
import com.dexter.fms.model.Report;
import com.dexter.fms.model.app.Budget;
import com.dexter.fms.model.app.CorporateTrip;
import com.dexter.fms.model.app.DriverLicense;
import com.dexter.fms.model.app.ExpenseType;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleFuelData;
import com.dexter.fms.model.app.VehicleFueling;
import com.dexter.fms.model.app.VehicleLicense;
import com.dexter.fms.model.app.VehicleLocationData;
import com.dexter.fms.model.app.VehicleOdometerData;
import com.dexter.fms.model.app.VehicleStatusEnum;
import com.dexter.fms.model.app.VehicleTrackerData;
import com.dexter.fms.model.app.VehicleTrackerEventData;
import com.dexter.fms.model.ref.DocumentType;
import com.dexter.fms.model.ref.FuelType;
import com.dexter.fms.model.ref.ItemType;
import com.dexter.fms.model.ref.LicenseType;
import com.dexter.fms.model.ref.ServiceType;
import com.dexter.fms.model.ref.TransactionType;
import com.dexter.fms.model.ref.VehicleWarning;

@ManagedBean(name = "appBean", eager=true)
@ApplicationScoped
public class ApplicationMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-ApplicationMBean");
	
	private FacesMessage msg = null;
	
	private List<PartnerUser> activeUsers;
	
	private boolean appSetup;
	
	private Timer timer, timer2, trackerEventTimer, trackerRealTimeTimer;
	private TimerTask task, task2, trackerEventTask, trackerRealTimeTask;
    
	//private int tracker_interval = 5; // interval in minutes
	//private Long lastReceivedId = -1L;
	
	private String userName = "admin1", userHost = "v5", userOrg = "zon_sattrak_telematics", password = "sattrak123", lastGMTTime = null;
	
	public ApplicationMBean()
	{
		// should start the interval downloads here...
    	task = new TimerTask()
    	{
            @Override
            public void run()
            {
            	logger.info("Starting partner subscription update at: " + new Date());
            	checkPartnerSubs();
            	logger.info("Finished partner subscription update at: " + new Date());
            	
                logger.info("Starting license update at: " + new Date());
                checkLicenses();
                logger.info("Finished license update at: " + new Date());
                
                logger.info("Starting budgets update at: " + new Date());
                checkBudgets();
                logger.info("Finished budgets update at: " + new Date());
                
                logger.info("Starting disposable vehicles check at: " + new Date());
                checkDisposableVehicles();
                logger.info("Finished disposable vehicles check at: " + new Date());
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
        
        trackerEventTimer = new Timer();
		trackerEventTask = new TimerTask()
		{
			@SuppressWarnings({ "deprecation" })
			@Override
			public void run()
			{
				logger.info("Starting tracker events info download from the tracker web service");
				
				String eventRequestedProps = "event_type,unit_id,event_time,event_name,event_text,event_value,h_address,h_distance,unit_name";
            	
            	String uri = "http://galoolitools.dnsalias.com/galooliDevKitService/galooliDevKitService.svc/json/GetEventsInformation?";
            	uri += "userName="+userName+"&";
            	uri += "userHost="+userHost+"&";
            	uri += "userOrg="+userOrg+"&";
            	uri += "password="+password+"&";
            	uri += "requestedUnits=any&";
            	uri += "requestedEvents=any&";//any
            	uri += "requestedPropertiesStr="+eventRequestedProps+"&";
            	
            	Calendar c = Calendar.getInstance();
            	c.add(Calendar.HOUR_OF_DAY, -1);
                //c.set(Calendar.MINUTE, 0);
                //c.set(Calendar.SECOND, 0);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String startTimeStr = sdf.format(c.getTime());
                c = Calendar.getInstance();
                //c.set(Calendar.HOUR_OF_DAY, 23);
                //c.set(Calendar.MINUTE, 59);
                //c.set(Calendar.SECOND, 59);
                String endTimeStr = sdf.format(c.getTime());
            	uri += "startTime=" + URLEncoder.encode(startTimeStr) + "&";
            	uri += "endTime=" + URLEncoder.encode(endTimeStr);
				
				try
				{
					URL url = new URL(uri);
                	HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                	connection.setRequestMethod("GET");
                	connection.setRequestProperty("Accept", "application/json");
                	InputStream is = connection.getInputStream();
                	
                	String resultString = null;
                	try {
                		resultString = getStringFromInputStream(is);
                	} catch(Exception ex){
                		ex.printStackTrace();
                	}
                	finally
                	{
                		try{
                			is.close();
                		}catch(Exception ig){}
                	}
                	
                	JSONObject resultJSON = null;
                	if(resultString != null)
                	{
	                	try {
	                		resultJSON = new JSONObject(resultString);
	                	} catch(Exception ex){
	                		ex.printStackTrace();
	                	}
                	}
                	
                	if(resultJSON != null)
                	{
                		JSONObject commonResultObj = resultJSON.getJSONObject("CommonResult");
                		
                		int resultCode = commonResultObj.getInt("ResultCode");
                		logger.info("tracker service result: " + resultCode);
                		System.out.println("result code: " + resultCode);
                		if(resultCode == 0)
	                    {
                			JSONArray dataSet = commonResultObj.getJSONArray("DataSet");
                			logger.info("Tracker records count: " + dataSet.length());
	                    	System.out.println("Tracker records count: " + dataSet.length());
	                        if(dataSet.length() > 0)
	                        {
	                        	GeneralDAO gDAO = new GeneralDAO();
	                        	gDAO.startTransaction();
	                        	for(int i=0; i<dataSet.length(); i++)
                                {
                                	JSONArray e_values = dataSet.getJSONArray(i);
                                	
                                	// event_type,unit_id,event_time,event_name,event_text,event_value,h_address,h_distance,unit_name
                                	String event_type = e_values.getString(0);
                                	String unit_id = e_values.getString(1);
                                	String event_time = e_values.getString(2);event_time = event_time.replaceAll("'", "");
                                	String event_name = e_values.getString(3);event_name = event_name.replaceAll("'", "");
                                	String event_text = e_values.getString(4);event_text = event_text.replaceAll("'", ""); // initial value
                                	String event_value = e_values.getString(5); // current value
                                	String h_address = e_values.getString(6);h_address = h_address.replaceAll("'", "");
                                	String h_distance = e_values.getString(7);
                                	String unit_name = e_values.getString(8);unit_name = unit_name.replaceAll("'", "");
                                	
                                	// the tran time coming from the tracker is in GTM, so we add 1 hour to it to get the GTM+1 value, which is our time zone
                                    Calendar tranTimeCal = Calendar.getInstance();
                                    tranTimeCal.setTime(sdf.parse(event_time));
                                    tranTimeCal.add(Calendar.HOUR, -1);
                                    
                                    Query q = gDAO.createQuery("Select e from Vehicle e where e.zonControlId=:zonControlId");
					            	q.setParameter("zonControlId", Integer.parseInt(unit_id));
					            	
					            	Object vObj = gDAO.search(q, 1);
					            	if(vObj != null)
					            	{
					            		Vehicle v = (Vehicle)vObj;
					            		
					            		VehicleTrackerEventData vted = new VehicleTrackerEventData();
					            		vted.setVehicle(v);
					            		vted.setCaptured_dt(tranTimeCal.getTime());
					            		vted.setCrt_dt(new Date());
					            		vted.setEvent_name(event_name);
					            		vted.setEvent_text(event_text);
					            		vted.setEvent_time(event_time);
					            		vted.setEvent_type(event_type);
					            		vted.setEvent_value(event_value);
					            		vted.setH_address(h_address);
					            		vted.setH_distance(h_distance);
					            		vted.setUnit_id(unit_id);
					            		vted.setUnit_name(unit_name);
					            		
					            		boolean exists = false;
	                                    // check if the current record exists before creating it, only do this for first time runs
	                                    Query checkQ = gDAO.createQuery("Select e from VehicleTrackerEventData e where e.vehicle = :vehicle and e.captured_dt = :tranTime");
	                                    checkQ.setParameter("vehicle", vted.getVehicle());
	                                    checkQ.setParameter("tranTime", vted.getCaptured_dt());
	                                    try
	                                    {
	                                        @SuppressWarnings("rawtypes")
	                                        List checkObj = checkQ.getResultList();
	                                        if(checkObj != null && checkObj.size() > 0)
	                                        {
	                                            exists = true;
	                                        }
	                                    }
	                                    catch(Exception ex)
	                                    {
	                                        ex.printStackTrace();
	                                    }
	                                    if(!exists)
	                                    	gDAO.save(vted);
                                	
	                                	if(event_type.equalsIgnoreCase("6001")) // Refuel event
	                                	{
    					            		if(v.getPartner().getFuelingType().equalsIgnoreCase("Automated") ||
    					            				v.getPartner().getFuelingType().equalsIgnoreCase("Both"))
    					            		{
	    					            		VehicleFueling vf = new VehicleFueling();
	    										vf.setAmt(0);
	    										vf.setCaptured_dt(tranTimeCal.getTime());
	    										vf.setCreatedBy(null);
	    										vf.setCrt_dt(new Date());
	    										vf.setFuelLevel(Double.parseDouble(event_value)+Double.parseDouble(event_text));
	    										vf.setLitres(Double.parseDouble(event_value));
	    										vf.setLocation(h_address);
	    										vf.setOdometer(Double.parseDouble(h_distance));
	    										vf.setSource("Tracker");
	    										vf.setVehicle(v);
	    										
	    										exists = false;
	    	                                    // check if the current record exists before creating it, only do this for first time runs
	    	                                    checkQ = gDAO.createQuery("Select e from VehicleFueling e where e.vehicle = :vehicle and e.captured_dt = :tranTime");
	    	                                    checkQ.setParameter("vehicle", vf.getVehicle());
	    	                                    checkQ.setParameter("tranTime", vf.getCaptured_dt());
	    	                                    try
	    	                                    {
	    	                                        @SuppressWarnings("rawtypes")
	    	                                        List checkObj = checkQ.getResultList();
	    	                                        if(checkObj != null && checkObj.size() > 0)
	    	                                        {
	    	                                            exists = true;
	    	                                        }
	    	                                    }
	    	                                    catch(Exception ex)
	    	                                    {
	    	                                        ex.printStackTrace();
	    	                                    }
	    	                                    if(!exists)
	    	                                    	gDAO.save(vf);
    					            		}
    					            		
    					            		VehicleFuelData vfd = new VehicleFuelData();
    					            		vfd.setCaptured_dt(tranTimeCal.getTime());
    					            		vfd.setCrt_dt(new Date());
    					            		vfd.setFuelLevel(Double.parseDouble(event_value)+Double.parseDouble(event_text));
    					            		vfd.setSource("Tracker");
    					            		vfd.setVehicle(v);
    					            		
    					            		exists = false;
    	                                    // check if the current record exists before creating it, only do this for first time runs
    	                                    checkQ = gDAO.createQuery("Select e from VehicleFuelData e where e.vehicle = :vehicle and e.captured_dt = :tranTime");
    	                                    checkQ.setParameter("vehicle", vfd.getVehicle());
    	                                    checkQ.setParameter("tranTime", vfd.getCaptured_dt());
    	                                    try
    	                                    {
    	                                        @SuppressWarnings("rawtypes")
    	                                        List checkObj = checkQ.getResultList();
    	                                        if(checkObj != null && checkObj.size() > 0)
    	                                        {
    	                                            exists = true;
    	                                        }
    	                                    }
    	                                    catch(Exception ex)
    	                                    {
    	                                        ex.printStackTrace();
    	                                    }
    	                                    if(!exists)
    	                                    	gDAO.save(vfd);
    					            	}
                                	}
                                }
	                        	gDAO.commit();
	                        	gDAO.destroy();
								logger.info("Commited database storage transaction for the downloaded tracker events");
	                        }
	                    }
                	}
				}
            	catch(RemoteException ex)
            	{
					ex.printStackTrace();
				}
				catch (MalformedURLException e)
				{
					e.printStackTrace();
				}
				catch (ProtocolException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}
			}
		};
		
		trackerRealTimeTimer = new Timer();
		trackerRealTimeTask = new TimerTask()
		{
			@SuppressWarnings({ "deprecation" })
			@Override
			public void run()
			{
				try
				{
					logger.info("Starting tracker real time info download from the tracker web service");
                	
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					
                	if(lastGMTTime == null)
                	{
                		Calendar c = Calendar.getInstance();
                		c.add(Calendar.HOUR_OF_DAY, -1);
                		/*c.set(Calendar.HOUR_OF_DAY, 0);
                		c.set(Calendar.MINUTE, 0);
                		c.set(Calendar.SECOND, 0);
                		c.set(Calendar.MILLISECOND, 0);*/
                		lastGMTTime = sdf.format(c.getTime());
                	}
                	
                	String realtimeRequestedProps = "address,distance,longitude,latitude,battery_voltage,engine_hours,speed,heading,hdop,status,unit_id";
                	String uri = "http://galoolitools.dnsalias.com/galooliDevKitService/galooliDevKitService.svc/json/GetRealTimeData?";
                	uri += "userName="+userName+"&";
                	uri += "userHost="+userHost+"&";
                	uri += "userOrg="+userOrg+"&";
                	uri += "password="+password+"&";
                	uri += "requestedPropertiesStr="+realtimeRequestedProps+"&";
                	uri += "lastGMTUpdateTime=" + URLEncoder.encode(lastGMTTime);
                	
                	URL url = new URL(uri);
                	HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                	connection.setRequestMethod("GET");
                	connection.setRequestProperty("Accept", "application/json");
                	InputStream is = connection.getInputStream();
                	
                	String resultString = null;
                	try {
                		resultString = getStringFromInputStream(is);
                	} catch(Exception ex){
                		ex.printStackTrace();
                	}
                	finally
                	{
                		try{
                			is.close();
                		}catch(Exception ig){}
                	}
                	
                	JSONObject resultJSON = null;
                	if(resultString != null)
                	{
	                	try {
	                		resultJSON = new JSONObject(resultString);
	                	} catch(Exception ex){
	                		ex.printStackTrace();
	                	}
                	}
                	
                	if(resultJSON != null)
                	{
                		JSONObject commonResultObj = resultJSON.getJSONObject("CommonResult");
                		String maxGMTUpdateTime = resultJSON.getString("MaxGmtUpdateTime");
                		
                		int resultCode = commonResultObj.getInt("ResultCode");
                		logger.info("tracker service result: " + resultCode);
                		System.out.println("result code: " + resultCode);
                		if(resultCode == 0)
	                    {
                			lastGMTTime = maxGMTUpdateTime;
                			JSONArray dataSet = commonResultObj.getJSONArray("DataSet");
                			logger.info("Tracker records count: " + dataSet.length());
	                    	System.out.println("Tracker records count: " + dataSet.length());
	                        if(dataSet.length() > 0)
	                        {
	                        	GeneralDAO gDAO = new GeneralDAO();
	                        	gDAO.startTransaction();
	                        	for(int i=0; i<dataSet.length(); i++)
                                {
                                	JSONArray e_values = dataSet.getJSONArray(i);
                                	
                                	// address,distance,longitude,latitude,battery_voltage,engine_hours,speed,heading,hdop,status,unit_id
                                	String event_time = e_values.getString(0);event_time = event_time.replaceAll("'", "");
                                	String address = e_values.getString(1);
                                	String distance = e_values.getString(2);
                                	String longitude = e_values.getString(3);
                                	String latitude = e_values.getString(4);
                                	String battery_voltage = e_values.getString(5);
                                	String engine_hours = e_values.getString(6);
                                	String speed = e_values.getString(7);
                                	String heading = e_values.getString(8);
                                	String hdop = e_values.getString(9);
                                	String status = e_values.getString(10);
                                	String unit_id = e_values.getString(11);
                                	
                                	// the tran time coming from the tracker is in GTM, so we add 1 hour to it to get the GTM+1 value, which is our time zone
                                    Calendar tranTimeCal = Calendar.getInstance();
                                    tranTimeCal.setTime(sdf.parse(event_time));
                                    tranTimeCal.add(Calendar.HOUR, -1);
                                    
                                    Query q = gDAO.createQuery("Select e from Vehicle e where e.zonControlId=:zonControlId");
					            	q.setParameter("zonControlId", Integer.parseInt(unit_id));
					            	
					            	Object vObj = gDAO.search(q, 1);
					            	if(vObj != null)
					            	{
					            		Vehicle v = (Vehicle)vObj;
					            		
					            		VehicleTrackerData vtd = new VehicleTrackerData();
					            		vtd.setVehicle(v);
					            		
					            		boolean exists = false;
	                                    // check if the current record exists before creating it, only do this for first time runs
	                                    Query checkQ = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle = :vehicle");
	                                    checkQ.setParameter("vehicle", vtd.getVehicle());
	                                    try
	                                    {
	                                        @SuppressWarnings("rawtypes")
	                                        List checkObj = checkQ.getResultList();
	                                        if(checkObj != null && checkObj.size() > 0)
	                                        {
	                                        	for(Object vtdObj : checkObj)
	                                        	{
	                                        		if(vtdObj instanceof VehicleTrackerData)
	                                        		{
	                                        			vtd = (VehicleTrackerData)vtdObj;
	                                        			exists = true;
	                                        		}
	                                        	}
	                                        }
	                                    }
	                                    catch(Exception ex)
	                                    {
	                                        ex.printStackTrace();
	                                    }
	                                    
	                                    vtd.setAddress(address);
	                                    vtd.setBatteryVoltage(Double.parseDouble(battery_voltage));
	                                    vtd.setCaptured_dt(tranTimeCal.getTime());
	                                    vtd.setCrt_dt(new Date());
	                                    vtd.setEngineHours(Double.parseDouble(engine_hours));
	                                    vtd.setHdop(Integer.parseInt(hdop));
	                                    vtd.setHeading(Integer.parseInt(heading));
	                                    vtd.setLat(Double.parseDouble(latitude));
	                                    vtd.setLon(Double.parseDouble(longitude));
	                                    vtd.setOdometer(Double.parseDouble(distance));
	                                    vtd.setSpeed(Double.parseDouble(speed));
	                                    vtd.setVehicleTStatus(status);
	                                    
	                                    if(exists)
	                                    	gDAO.update(vtd);
	                                    else
	                                    	gDAO.save(vtd);
	                                    
	                                    VehicleOdometerData vod = new VehicleOdometerData();
					            		vod.setVehicle(v);
					            		vod.setCaptured_dt(tranTimeCal.getTime());
					            		vod.setCrt_dt(new Date());
					            		vod.setOdometer(Double.parseDouble(distance));
					            		vod.setSource("Tracker");
					            		
					            		gDAO.save(vod);
					            		
					            		VehicleLocationData vld = new VehicleLocationData();
					            		vld.setVehicle(v);
					            		vld.setActive(true);
					            		vld.setAddress(address);
					            		vld.setCaptured_dt(tranTimeCal.getTime());
					            		vld.setCrt_dt(new Date());
					            		vld.setLat(Double.parseDouble(latitude));
					            		vld.setLon(Double.parseDouble(longitude));
					            		checkQ = gDAO.createQuery("Select e from VehicleLocationData e where e.vehicle = :vehicle and e.active=:active");
	                                    checkQ.setParameter("vehicle", vld.getVehicle());
	                                    checkQ.setParameter("active", true);
	                                    try
	                                    {
	                                        @SuppressWarnings("rawtypes")
	                                        List checkObj = checkQ.getResultList();
	                                        if(checkObj != null && checkObj.size() > 0)
	                                        {
	                                        	for(Object vldObj : checkObj)
	                                        	{
	                                        		if(vldObj instanceof VehicleLocationData)
	                                        		{
	                                        			VehicleLocationData vld_old = (VehicleLocationData)vldObj;
	                                        			vld_old.setActive(false);
	                                        			gDAO.update(vld_old);
	                                        		}
	                                        	}
	                                        }
	                                    }
	                                    catch(Exception ex)
	                                    {
	                                        ex.printStackTrace();
	                                    }
	                                    gDAO.save(vld);
					            	}
                                }
	                        	gDAO.commit();
	                        	gDAO.destroy();
								logger.info("Commited database storage transaction for the downloaded tracker realtime data");
	                        }
	                    }
                	}
				}
            	catch(RemoteException ex)
            	{
					ex.printStackTrace();
				}
				catch (MalformedURLException e)
				{
					e.printStackTrace();
				}
				catch (ProtocolException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}
			}
		};
		
		//trackerEventTimer.scheduleAtFixedRate(trackerEventTask, new Date(), 1000 * 60 * 5); // every 5 minutes after first call
		//trackerRealTimeTimer.scheduleAtFixedRate(trackerRealTimeTask, new Date(), 1000 * 60 * 1); // every 1 minutes after first call
	}
	
	public String getYear()
	{
		return ""+Calendar.getInstance().get(Calendar.YEAR);
	}
	
	@SuppressWarnings("unchecked")
	private void checkPartnerSubs()
	{
		GeneralDAO gDAO = new GeneralDAO();
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("expired", false);
		Object subsObj = gDAO.search("PartnerSubscription", params);
		if(subsObj != null)
		{
			Vector<PartnerSubscription> subs = (Vector<PartnerSubscription>)subsObj;
			gDAO.startTransaction();
			Date now = new Date();
			for(PartnerSubscription e : subs)
			{
				if(e.getEnd_dt() != null)
				{
					if(e.getEnd_dt().before(now))
					{
						e.setExpired(true);
						gDAO.update(e);
					}
				}
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
			VehicleLicense prevE = null;
			for(VehicleLicense e : subs)
			{
				Date now = new Date();
				boolean change = false;
				if(e.getLic_start_dt().before(now) && e.getLic_end_dt().after(now) && !e.isActive() && !e.isExpired())
				{
					e.setActive(true);
					e.setExpired(false);
					change = true;
					if(prevE != null && prevE.isActive())
					{
						prevE.setActive(false);
						prevE.setExpired(true);
						gDAO.update(prevE);
					}
				}
				else if(!e.isExpired() && e.getLic_end_dt().before(now))
				{
					e.setExpired(true);
					//e.setActive(false);
					change = true;
				}
				if(change)
					gDAO.update(e);
				
				prevE = e;
			}
		}
		gDAO.commit();
		gDAO.destroy();
		
		gDAO = new GeneralDAO();
		params = new Hashtable<String, Object>();
		params.put("expired", false);
		subsObj = gDAO.search("DriverLicense", params);
		if(subsObj != null)
		{
			Vector<DriverLicense> subs = (Vector<DriverLicense>)subsObj;
			gDAO.startTransaction();
			DriverLicense prevE = null;
			for(DriverLicense e : subs)
			{
				Date now = new Date();
				boolean change = false;
				if(e.getLic_start_dt() != null && e.getLic_start_dt().before(now) && e.getLic_end_dt() != null && e.getLic_end_dt().after(now) && !e.isActive() && !e.isExpired())
				{
					e.setActive(true);
					e.setExpired(false);
					change = true;
					
					if(prevE != null && prevE.isActive())
					{
						prevE.setActive(false);
						prevE.setExpired(true);
						gDAO.update(prevE);
					}
				}
				else if(!e.isExpired() && e.getLic_end_dt().before(now))
				{
					e.setExpired(true);
					//e.setActive(false);
					change = true;
				}
				if(change)
					gDAO.update(e);
				
				prevE = e;
			}
		}
		
		gDAO.commit();
		gDAO.destroy();
	}
	
	@SuppressWarnings("unchecked")
	private void checkTrips()
	{
		GeneralDAO gDAO = new GeneralDAO();
		
		Query q = gDAO.createQuery("Select e from CorporateTrip e where e.approvalStatus=:approvalStatus and e.approvalStatus2=:approvalStatus2 and e.tripStatus=:tripStatus and e.departureDateTime < :nowDateTime");
		q.setParameter("approvalStatus", "APPROVED");
		q.setParameter("approvalStatus2", "APPROVED");
		q.setParameter("tripStatus", "AWAITING");
		q.setParameter("nowDateTime", new Date());
		
		gDAO.startTransaction();
		
		Object tripsObj = gDAO.search(q, 0);
		if(tripsObj != null)
		{
			Vector<CorporateTrip> trips = (Vector<CorporateTrip>)tripsObj;
			for(CorporateTrip e : trips)
			{
				e.setTripStatus("ON_TRIP");
				e.getVehicle().setActiveStatus(VehicleStatusEnum.ON_TRIP.getStatus());
				
				gDAO.update(e.getVehicle());
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
				
				// TODO: Send a notification to the requester and the approver that this trip is over due
				Notification n = new Notification();
				n.setCrt_dt(new Date());
				n.setUser(e.getCreatedBy());
				n.setMessage("Your trip for destination '" + e.getArrivalLocation() + "' is overdue.");
				n.setNotified(false);
				n.setSubject("Trip completion overdue");
				n.setPage_url("trip_request");
				gDAO.save(n);
				n = new Notification();
				n.setCrt_dt(new Date());
				n.setUser(e.getApproveUser());
				n.setMessage(e.getStaff().getFirstname() + " " + e.getStaff().getLastname() + "'s trip for destination '" + e.getArrivalLocation() + "' is overdue.");
				n.setNotified(false);
				n.setSubject("Trip completion overdue");
				n.setPage_url("attend_trips");
				gDAO.save(n);
			}
		}
		
		gDAO.commit();
		
		/*Calendar c = Calendar.getInstance();
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
		}*/
	}
	
	@SuppressWarnings("unchecked")
	private void checkBudgets()
	{
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("active", true);
		
		Object objs = gDAO.search("Budget", params);
		if(objs != null)
		{
			Vector<Budget> budgets = (Vector<Budget>)objs;
			gDAO.startTransaction();
			for(Budget b : budgets)
			{
				if(b.getEnd_dt().before(new Date()))
				{
					b.setActive(false);
					gDAO.update(b);
				}
			}
			gDAO.commit();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void checkDisposableVehicles()
	{
		GeneralDAO gDAO = new GeneralDAO();
		
		Object pObjs = gDAO.findAll("Partner");
		if(pObjs != null)
		{
			Vector<Partner> partners = (Vector<Partner>)pObjs;
			for(Partner p : partners)
			{
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("active", true);
				params.put("partner", p);
				
				Object objs = gDAO.search("Vehicle", params);
				if(objs != null)
				{
					Vector<Vehicle> vehicles = (Vector<Vehicle>)objs;
					for(Vehicle v : vehicles)
					{
						if(!v.isDisposalAlertSent() && v.getFleet() != null && v.getFleet().getVehiclesActiveMonths() > 0)
						{
							if(v.getAgeInMonths() > 0 && v.getAgeInMonths() >= v.getFleet().getVehiclesActiveMonths())
							{
								if(v.getAssignee() != null)
								{
									params = new Hashtable<String, Object>();
									params.put("personel", v.getAssignee());
									Object uObj = gDAO.search("PartnerUser", params);
									PartnerUser u = null;
									if(uObj != null)
									{
										Vector<PartnerUser> uList = (Vector<PartnerUser>)uObj;
										if(uList.size() > 0)
											u = uList.get(0);
									}
									if(u != null)
									{
										Notification n = new Notification();
										n.setCrt_dt(new Date());
										n.setNotified(false);
										n.setSubject("Vehicle due for Disposal");
										n.setUser(u);
										n.setMessage("Vehicle: " + v.getRegistrationNo() + " is due for disposal!");
										n.setPage_url("manage_vehicles.xhtml");
										
										gDAO.startTransaction();
										gDAO.save(n);
										
										v.setDisposalAlertSent(true);
										v.setDisposalStatus("TO-BE-DISPOSED");
										gDAO.update(v);
										
										gDAO.commit();
									}
								}
							}
						}
					}
				}
			}
		}
		
		gDAO.destroy();
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
		
		VehicleWarning vw = new VehicleWarning();
		vw.setCrt_dt(new Date());
		vw.setSystemObj(true);
		vw.setName("LDW");
		vw.setDescription("Lane Departure Warning");
		gDAO.save(vw);
		vw = new VehicleWarning();
		vw.setCrt_dt(new Date());
		vw.setSystemObj(true);
		vw.setName("HWM");
		vw.setDescription("Headway monitoring");
		gDAO.save(vw);
		vw = new VehicleWarning();
		vw.setCrt_dt(new Date());
		vw.setSystemObj(true);
		vw.setName("FCW");
		vw.setDescription("Forward Collision Warning (incl. Urban Collision Warning)");
		gDAO.save(vw);
		vw = new VehicleWarning();
		vw.setCrt_dt(new Date());
		vw.setSystemObj(true);
		vw.setName("PED");
		vw.setDescription("Pedestrian Detection");
		gDAO.save(vw);
		vw = new VehicleWarning();
		vw.setCrt_dt(new Date());
		vw.setSystemObj(true);
		vw.setName("Over Speed");
		vw.setDescription("Speed over 100 KM/H is over speed");
		gDAO.save(vw);
		vw = new VehicleWarning();
		vw.setCrt_dt(new Date());
		vw.setSystemObj(true);
		vw.setName("Steering");
		vw.setDescription("Steering percentage during a drive");
		gDAO.save(vw);
		vw = new VehicleWarning();
		vw.setCrt_dt(new Date());
		vw.setSystemObj(true);
		vw.setName("Braking");
		vw.setDescription("Braking percentage during a drive");
		gDAO.save(vw);
		vw = new VehicleWarning();
		vw.setCrt_dt(new Date());
		vw.setSystemObj(true);
		vw.setName("Acceleration");
		vw.setDescription("Acceleration percentage during a drive");
		gDAO.save(vw);
		
		// create a default partner
		Partner sattrak = new Partner();
		sattrak.setName("Sattrak");
		sattrak.setSattrak(true);
		sattrak.setFuelingType("Both");
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
		
		Properties adminUserPros = new Properties();
		try
		{
			adminUserPros.load(ApplicationMBean.class.getResourceAsStream("/adminuser.properties"));
		}
		catch(Exception ex)
		{}
		
		//create default user
		final PartnerPersonel pp = new PartnerPersonel();
		if(adminUserPros.containsKey("admin.firstname"))
			pp.setFirstname(adminUserPros.getProperty("admin.firstname"));
		else
			pp.setFirstname("Victor");
		if(adminUserPros.containsKey("admin.lastname"))
			pp.setLastname(adminUserPros.getProperty("admin.lastname"));
		else
			pp.setLastname("Okere");
		if(adminUserPros.containsKey("admin.email"))
			pp.setEmail(adminUserPros.getProperty("admin.email"));
		else
			pp.setEmail("okerevictor@gmail.com");
		pp.setPartner(sattrak);
		pp.setCrt_dt(new Date());
		pp.setHasUser(true);
		gDAO.save(pp);
		logger.log(Level.INFO, "Saving user: 'admin'" + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		final PartnerUser adminUser = new PartnerUser();
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
		
		Thread notifyThread = new Thread()
		{
			public void run()
			{
				StringBuilder body = new StringBuilder("<html><body>");
				body.append("<p><strong>Dear ").append(pp.getFirstname()).append("</strong></p>");
				body.append("<p>Your user account details to access the Sattrak Fleet Management System are set up as below: -</p>");
				body.append("<br/><p>Username: <strong>").append(adminUser.getUsername()).append("</strong><br/>");
				body.append("Password: <strong>").append("admin").append("</strong></p>");
				body.append("<p>Regards</br>Sattrak FMS Portal</p>");
				body.append("</body></html>");
				try
				{	
					Emailer.sendEmail("fms@sattrakservices.com", new String[]{pp.getEmail()}, "FMS - Default Admin Setup", body.toString());
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		};
		notifyThread.start();
		
		sattrak.setCreatedBy(adminUser);
		gDAO.update(sattrak);
		
		pp.setCreatedBy(adminUser);
		gDAO.update(pp);
		
		DocumentType dt = new DocumentType();
		dt.setCrt_dt(new Date());
		dt.setName("Receipt");
		dt.setDescription("The purchase receipt.");
		dt.setCreatedBy(adminUser);
		gDAO.save(dt);
		
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
		fleetManagement.setDisplay_name("Fleet Group");
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
		logger.log(Level.INFO, "Saving function: 'Manage Departments'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function63 = new MFunction();
		function63.setName("Manage Personnel");
		function63.setDescription("Manage your personnels.");
		function63.setPage_url("manage_staffs");
		function63.setModule(userManagement);
		function63.setActive(true);
		function63.setCrt_dt(new Date());
		gDAO.save(function63);
		logger.log(Level.INFO, "Saving function: 'Manage Personnels'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
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
		
		MFunction manageFueling = new MFunction();
		manageFueling.setName("Manage Fueling");
		manageFueling.setDescription("View fueling information for your vehicles.");
		manageFueling.setPage_url("manage_v_fueling");
		manageFueling.setModule(transactions);
		manageFueling.setActive(true);
		manageFueling.setCrt_dt(new Date());
		gDAO.save(manageFueling);
		logger.log(Level.INFO, "Saving function: 'Manage Fueling'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction fuelingRequest = new MFunction();
		fuelingRequest.setName("Request Fueling");
		fuelingRequest.setDescription("Request for fueling for various vehicles.");
		fuelingRequest.setPage_url("request_fueling");
		fuelingRequest.setModule(transactions);
		fuelingRequest.setActive(true);
		fuelingRequest.setCrt_dt(new Date());
		gDAO.save(fuelingRequest);
		logger.log(Level.INFO, "Saving function: 'Fueling Request'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction fuelingApproval = new MFunction();
		fuelingApproval.setName("Approve Fueling");
		fuelingApproval.setDescription("Approve / Deny requests for fueling requests.");
		fuelingApproval.setPage_url("approve_fueling");
		fuelingApproval.setModule(transactions);
		fuelingApproval.setActive(true);
		fuelingApproval.setCrt_dt(new Date());
		gDAO.save(fuelingApproval);
		logger.log(Level.INFO, "Saving function: 'Fueling Approval'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function76 = new MFunction();
		function76.setName("License");
		function76.setDescription("Capture, view and update various kind of license registration/renewal information for your vehicles.");
		function76.setPage_url("manage_v_licenseinfo");
		function76.setModule(transactions);
		function76.setActive(true);
		function76.setCrt_dt(new Date());
		gDAO.save(function76);
		logger.log(Level.INFO, "Saving function: 'License Info'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction drvLic = new MFunction();
		drvLic.setName("Driver's License");
		drvLic.setDescription("Capture, view and update various drivers license registration/renewal information for your drivers.");
		drvLic.setPage_url("manage_drvlicense");
		drvLic.setModule(transactions);
		drvLic.setActive(true);
		drvLic.setCrt_dt(new Date());
		gDAO.save(drvLic);
		logger.log(Level.INFO, "Saving function: 'Driver License'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
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
		function78.setName("Driving Info");
		function78.setDescription("Capture vehicle warnings to determine drivers behaviour.");
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
		
		/*MFunction function792 = new MFunction();
		function792.setName("Trips Info");
		function792.setDescription("View current and historic trips information for your vehicles.");
		function792.setPage_url("manage_v_tripsinfo");
		function792.setModule(fleetManagement);
		function792.setActive(true);
		function792.setCrt_dt(new Date());
		gDAO.save(function792);
		logger.log(Level.INFO, "Saving function: 'Trips Info'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));*/
		
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
		function82.setName("Expenses");
		function82.setDescription("Capture and view your expenses.");
		function82.setPage_url("manage_expenses");
		function82.setModule(budgetManagement);
		function82.setActive(true);
		function82.setCrt_dt(new Date());
		gDAO.save(function82);
		logger.log(Level.INFO, "Saving function: 'Expenses'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction mngExpenses = new MFunction();
		mngExpenses.setName("Manage Expenses");
		mngExpenses.setDescription("View your expenses.");
		mngExpenses.setPage_url("manage_all_expenses");
		mngExpenses.setModule(budgetManagement);
		mngExpenses.setActive(true);
		mngExpenses.setCrt_dt(new Date());
		gDAO.save(mngExpenses);
		logger.log(Level.INFO, "Saving function: 'Manage Expenses'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction expenseRequest = new MFunction();
		expenseRequest.setName("Request Expense");
		expenseRequest.setDescription("Request for expenses.");
		expenseRequest.setPage_url("request_expenses");
		expenseRequest.setModule(budgetManagement);
		expenseRequest.setActive(true);
		expenseRequest.setCrt_dt(new Date());
		gDAO.save(expenseRequest);
		logger.log(Level.INFO, "Saving function: 'Request Expenses'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction expenseApproval = new MFunction();
		expenseApproval.setName("Approve Expense");
		expenseApproval.setDescription("Approve/Deny requests for expenses.");
		expenseApproval.setPage_url("approve_expenses");
		expenseApproval.setModule(budgetManagement);
		expenseApproval.setActive(true);
		expenseApproval.setCrt_dt(new Date());
		gDAO.save(expenseApproval);
		logger.log(Level.INFO, "Saving function: 'Approve Expenses'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function882 = new MFunction();
		function882.setName("Stock, Spares and Inventory");
		function882.setDescription("Manage your assets.");
		function882.setPage_url("manage_assets");
		function882.setModule(assetManagement);
		function882.setActive(true);
		function882.setCrt_dt(new Date());
		gDAO.save(function882);
		logger.log(Level.INFO, "Saving function: 'Manage Assets'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction disposalRequest = new MFunction();
		disposalRequest.setName("Request Disposal");
		disposalRequest.setDescription("Request for disposal for various vehicles.");
		disposalRequest.setPage_url("request_vdisposal");
		disposalRequest.setModule(assetManagement);
		disposalRequest.setActive(true);
		disposalRequest.setCrt_dt(new Date());
		gDAO.save(disposalRequest);
		logger.log(Level.INFO, "Saving function: 'Disposal Request'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction disposalApproval = new MFunction();
		disposalApproval.setName("Approve Disposal");
		disposalApproval.setDescription("Approve / Deny requests for disposal requests.");
		disposalApproval.setPage_url("approve_vdisposal");
		disposalApproval.setModule(assetManagement);
		disposalApproval.setActive(true);
		disposalApproval.setCrt_dt(new Date());
		gDAO.save(disposalApproval);
		logger.log(Level.INFO, "Saving function: 'Disposal Approval'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function883 = new MFunction();
		function883.setName("Manage Customers");
		function883.setDescription("Manage your customers.");
		function883.setPage_url("manage_customers");
		function883.setModule(appManagement);
		function883.setActive(true);
		function883.setCrt_dt(new Date());
		gDAO.save(function883);
		logger.log(Level.INFO, "Saving function: 'Manage Customers'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction cusBook = new MFunction();
		cusBook.setName("Customer Booking");
		cusBook.setDescription("Manage customer trip bookings.");
		cusBook.setPage_url("manage_cus_booking");
		cusBook.setModule(transactions);
		cusBook.setActive(true);
		cusBook.setCrt_dt(new Date());
		gDAO.save(cusBook);
		logger.log(Level.INFO, "Saving function: 'Customer Booking'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction topFunc1 = new MFunction();
		topFunc1.setName("Capture Top-up");
		topFunc1.setDescription("Insert top-ups.");
		topFunc1.setPage_url("insert_topup");
		topFunc1.setModule(transactions);
		topFunc1.setActive(true);
		topFunc1.setCrt_dt(new Date());
		//gDAO.save(topFunc1);
		logger.log(Level.INFO, "Saving function: 'Capture Top-up'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction topFunc2 = new MFunction();
		topFunc2.setName("Fuel Card Top-ups");
		topFunc2.setDescription("Manage your top-ups.");
		topFunc2.setPage_url("manage_topup");
		topFunc2.setModule(transactions);
		topFunc2.setActive(true);
		topFunc2.setCrt_dt(new Date());
		//gDAO.save(topFunc2);
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
		MFunction ttypeFunc = new MFunction();
		ttypeFunc.setName("License Types");
		ttypeFunc.setDescription("Manage supported license types.");
		ttypeFunc.setPage_url("manage_lictypes");
		ttypeFunc.setModule(appManagement);
		ttypeFunc.setActive(true);
		ttypeFunc.setCrt_dt(new Date());
		gDAO.save(ttypeFunc);
		logger.log(Level.INFO, "Saving function: 'License Types'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction dtypeFunc = new MFunction();
		dtypeFunc.setName("Document Types");
		dtypeFunc.setDescription("Manage supported document types.");
		dtypeFunc.setPage_url("manage_doctypes");
		dtypeFunc.setModule(appManagement);
		dtypeFunc.setActive(true);
		dtypeFunc.setCrt_dt(new Date());
		gDAO.save(dtypeFunc);
		logger.log(Level.INFO, "Saving function: 'Document Types'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction wtypeFunc = new MFunction();
		wtypeFunc.setName("Driving Warning Types");
		wtypeFunc.setDescription("Manage supported driving warning types.");
		wtypeFunc.setPage_url("manage_warntypes");
		wtypeFunc.setModule(appManagement);
		wtypeFunc.setActive(true);
		wtypeFunc.setCrt_dt(new Date());
		gDAO.save(wtypeFunc);
		logger.log(Level.INFO, "Saving function: 'Driving warning Types'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction auditFunc = new MFunction();
		auditFunc.setName("Audit Trail");
		auditFunc.setDescription("View various actions performed on the platform.");
		auditFunc.setPage_url("audit_trails");
		auditFunc.setModule(appManagement);
		auditFunc.setActive(true);
		auditFunc.setCrt_dt(new Date());
		gDAO.save(auditFunc);
		logger.log(Level.INFO, "Saving function: 'Audit Trail'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MFunction function9 = new MFunction();
		function9.setName("Create Transaction");
		function9.setDescription("Create a new transaction.");
		function9.setPage_url("create_transaction");
		function9.setModule(transactions);
		function9.setActive(true);
		function9.setCrt_dt(new Date());
		//gDAO.save(function9);
		logger.log(Level.INFO, "Saving function: 'Create Transaction'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		MFunction function10 = new MFunction();
		function10.setName("Manage Transactions");
		function10.setDescription("Update details of your transactions.");
		function10.setPage_url("manage_transactions");
		function10.setModule(transactions);
		function10.setActive(true);
		function10.setCrt_dt(new Date());
		//gDAO.save(function10);
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
		rf77.setFunction(manageFueling);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: manage fueling..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(fuelingRequest);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: fueling request..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(fuelingApproval);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: fueling approval..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function76);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 76..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(drvLic);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: drvLic..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
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
		/*rf77 = new MRoleFunction();
		rf77.setRole(adminRole);
		rf77.setFunction(function792);
		rf77.setCrt_dt(new Date());
		rf77.setCreatedBy(adminUser);
		gDAO.save(rf77);
		logger.log(Level.INFO, "Mapping role-function: 792..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));*/
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
		
		rf82 = new MRoleFunction();
		rf82.setRole(adminRole);
		rf82.setFunction(mngExpenses);
		rf82.setCrt_dt(new Date());
		rf82.setCreatedBy(adminUser);
		gDAO.save(rf82);
		logger.log(Level.INFO, "Mapping role-function: manage expenses..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		rf82 = new MRoleFunction();
		rf82.setRole(adminRole);
		rf82.setFunction(expenseRequest);
		rf82.setCrt_dt(new Date());
		rf82.setCreatedBy(adminUser);
		gDAO.save(rf82);
		logger.log(Level.INFO, "Mapping role-function: expense request..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf82 = new MRoleFunction();
		rf82.setRole(adminRole);
		rf82.setFunction(expenseApproval);
		rf82.setCrt_dt(new Date());
		rf82.setCreatedBy(adminUser);
		gDAO.save(rf82);
		logger.log(Level.INFO, "Mapping role-function: expense approval..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MRoleFunction rf882 = new MRoleFunction();
		rf882.setRole(adminRole);
		rf882.setFunction(function882);
		rf882.setCrt_dt(new Date());
		rf882.setCreatedBy(adminUser);
		gDAO.save(rf882);
		logger.log(Level.INFO, "Mapping role-function: 882..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MRoleFunction rf882_1 = new MRoleFunction();
		rf882_1.setRole(adminRole);
		rf882_1.setFunction(disposalRequest);
		rf882_1.setCrt_dt(new Date());
		rf882_1.setCreatedBy(adminUser);
		gDAO.save(rf882_1);
		logger.log(Level.INFO, "Mapping role-function: 882_1..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MRoleFunction rf882_2 = new MRoleFunction();
		rf882_2.setRole(adminRole);
		rf882_2.setFunction(disposalApproval);
		rf882_2.setCrt_dt(new Date());
		rf882_2.setCreatedBy(adminUser);
		gDAO.save(rf882_2);
		logger.log(Level.INFO, "Mapping role-function: 882_2..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MRoleFunction rf883 = new MRoleFunction();
		rf883.setRole(adminRole);
		rf883.setFunction(function883);
		rf883.setCrt_dt(new Date());
		rf883.setCreatedBy(adminUser);
		gDAO.save(rf883);
		logger.log(Level.INFO, "Mapping role-function: 883..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
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
		//gDAO.save(rfTopU);
		logger.log(Level.INFO, "Mapping role-function: TopU-2..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		MRoleFunction rf8 = new MRoleFunction();
		rf8.setRole(adminRole);
		rf8.setFunction(function8);
		rf8.setCrt_dt(new Date());
		rf8.setCreatedBy(adminUser);
		gDAO.save(rf8);
		logger.log(Level.INFO, "Mapping role-function: 8..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		rf8 = new MRoleFunction();
		rf8.setRole(adminRole);
		rf8.setFunction(ttypeFunc);
		rf8.setCrt_dt(new Date());
		rf8.setCreatedBy(adminUser);
		gDAO.save(rf8);
		logger.log(Level.INFO, "Mapping role-function: lic type..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf8 = new MRoleFunction();
		rf8.setRole(adminRole);
		rf8.setFunction(dtypeFunc);
		rf8.setCrt_dt(new Date());
		rf8.setCreatedBy(adminUser);
		gDAO.save(rf8);
		logger.log(Level.INFO, "Mapping role-function: doc type..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		rf8 = new MRoleFunction();
		rf8.setRole(adminRole);
		rf8.setFunction(wtypeFunc);
		rf8.setCrt_dt(new Date());
		rf8.setCreatedBy(adminUser);
		gDAO.save(rf8);
		logger.log(Level.INFO, "Mapping role-function: warning type..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		rf8 = new MRoleFunction();
		rf8.setRole(adminRole);
		rf8.setFunction(auditFunc);
		rf8.setCrt_dt(new Date());
		rf8.setCreatedBy(adminUser);
		gDAO.save(rf8);
		logger.log(Level.INFO, "Mapping role-function: audit trail..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
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
		
		MRoleFunction rcusbook = new MRoleFunction();
		rcusbook.setRole(adminRole);
		rcusbook.setFunction(cusBook);
		rcusbook.setCrt_dt(new Date());
		rcusbook.setCreatedBy(adminUser);
		gDAO.save(rcusbook);
		logger.log(Level.INFO, "Mapping role-function: customer booking..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
		
		// setup reports
		Report r = new Report();
		r.setModule("Users");
		r.setPage_url("reports_allusers");
		r.setTitle("All User Report");
		gDAO.save(r);
		MRoleReport mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Users");
		r.setPage_url("reports_driverlicdue");
		r.setTitle("Driver License Due");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Users");
		r.setPage_url("reports_accidents");
		r.setTitle("Accident Report");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Vehicle / Maintenance");
		r.setPage_url("reports_vbrands");
		r.setTitle("Vehicle Brands Report");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Vehicle / Maintenance");
		r.setPage_url("reports_vrmaint");
		r.setTitle("Routine Maintenance");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Vehicle / Maintenance");
		r.setPage_url("reports_vadhocmaint");
		r.setTitle("Adhoc Maintenance");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Vehicle / Maintenance");
		r.setPage_url("reports_vaccidented");
		r.setTitle("Accidented Vehicles");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Vehicle / Maintenance");
		r.setPage_url("reports_vlicdue");
		r.setTitle("Licences Due Report");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Vehicle / Maintenance");
		r.setPage_url("reports_vages");
		r.setTitle("Age of Vehicles");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Vehicle / Maintenance");
		r.setPage_url("reports_vbtreplace");
		r.setTitle("Battery/Tyre Replacement");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Corporate Trip");
		r.setPage_url("reports_vtrips");
		r.setTitle("Trip Report");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Fueling");
		r.setPage_url("reports_fdaterage");
		r.setTitle("Date Period");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Fueling");
		r.setPage_url("reports_fvehicle");
		r.setTitle("Vehicle");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Expense");
		r.setPage_url("reports_expense");
		r.setTitle("Expense Report");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		
		r = new Report();
		r.setModule("Accidented");
		r.setPage_url("reports_accidented_by_driver");
		r.setTitle("Accidented Vehicles by Driver");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Accidented");
		r.setPage_url("reports_accidented_by_status");
		r.setTitle("Accidented Vehicles by Status");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Accidented");
		r.setPage_url("reports_accidented_by_brand");
		r.setTitle("Accidented Vehicles by Brand");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Accidented");
		r.setPage_url("reports_accidents");
		r.setTitle("Accidented Vehicles List");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Drivers");
		r.setPage_url("reports_drivers_by_region");
		r.setTitle("Driver List by Regions");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Drivers");
		r.setPage_url("reports_drivers_by_dur_of_service");
		r.setTitle("Driver List by Duration of Service");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Drivers");
		r.setPage_url("reports_drivering_info");
		r.setTitle("Driving Behaviour Report");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Analysis");
		r.setPage_url("reports_fleet_cost_analysis");
		r.setTitle("Fleet Cost Analysis Report");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		r = new Report();
		r.setModule("Analysis");
		r.setPage_url("reports_fleet_fuel_cost_analysis");
		r.setTitle("Fleet Fuel Cost Analysis Report");
		gDAO.save(r);
		mr = new MRoleReport();
		mr.setCreatedBy(adminUser);
		mr.setCrt_dt(new Date());
		mr.setReport(r);
		mr.setRole(adminRole);
		gDAO.save(mr);
		
		MDash dash = new MDash();
		dash.setActive(true);
		dash.setCrt_dt(new Date());
		dash.setName("trackv");
		dash.setDescription("To track some vehicles");
		gDAO.save(dash);
		MDashRole dashRole = new MDashRole();
		dashRole.setCreatedBy(adminUser);
		dashRole.setCrt_dt(new Date());
		dashRole.setDash(dash);
		dashRole.setRole(adminRole);
		gDAO.save(dashRole);
		dash = new MDash();
		dash.setActive(true);
		dash.setCrt_dt(new Date());
		dash.setName("duelics");
		dash.setDescription("Notification for due licenses");
		gDAO.save(dash);
		dashRole = new MDashRole();
		dashRole.setCreatedBy(adminUser);
		dashRole.setCrt_dt(new Date());
		dashRole.setDash(dash);
		dashRole.setRole(adminRole);
		gDAO.save(dashRole);
		dash = new MDash();
		dash.setActive(true);
		dash.setCrt_dt(new Date());
		dash.setName("duemaints");
		dash.setDescription("Notification for due maintenances");
		gDAO.save(dash);
		dashRole = new MDashRole();
		dashRole.setCreatedBy(adminUser);
		dashRole.setCrt_dt(new Date());
		dashRole.setDash(dash);
		dashRole.setRole(adminRole);
		gDAO.save(dashRole);
		dash = new MDash();
		dash.setActive(true);
		dash.setCrt_dt(new Date());
		dash.setName("recentexpenses");
		dash.setDescription("Recent expenses analysis");
		gDAO.save(dash);
		dashRole = new MDashRole();
		dashRole.setCreatedBy(adminUser);
		dashRole.setCrt_dt(new Date());
		dashRole.setDash(dash);
		dashRole.setRole(adminRole);
		gDAO.save(dashRole);
		dash = new MDash();
		dash.setActive(true);
		dash.setCrt_dt(new Date());
		dash.setName("bookings");
		dash.setDescription("Company personnel trip bookings");
		gDAO.save(dash);
		dashRole = new MDashRole();
		dashRole.setCreatedBy(adminUser);
		dashRole.setCrt_dt(new Date());
		dashRole.setDash(dash);
		dashRole.setRole(adminRole);
		gDAO.save(dashRole);
		dash = new MDash();
		dash.setActive(true);
		dash.setCrt_dt(new Date());
		dash.setName("alerts");
		dash.setDescription("MISC Alerts");
		gDAO.save(dash);
		dashRole = new MDashRole();
		dashRole.setCreatedBy(adminUser);
		dashRole.setCrt_dt(new Date());
		dashRole.setDash(dash);
		dashRole.setRole(adminRole);
		gDAO.save(dashRole);
		
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
			setup.setAppName("Fleet Management System");
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
	
	// convert InputStream to String
    private static String getStringFromInputStream(InputStream is)
    {
    	BufferedReader br = null;
    	StringBuilder sb = new StringBuilder();
    	
    	String line;
    	try
    	{
    		br = new BufferedReader(new InputStreamReader(is));
    		while ((line = br.readLine()) != null) {
    			sb.append(line);
    		}
    	} catch (IOException e) {
    		e.printStackTrace();
    	} finally {
    		if (br != null) {
    			try {
    				br.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    	}
    	return sb.toString();
    }
}
