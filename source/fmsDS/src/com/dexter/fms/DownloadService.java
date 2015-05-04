package com.dexter.fms;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.persistence.Query;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleFuelData;
import com.dexter.fms.model.app.VehicleFueling;
import com.dexter.fms.model.app.VehicleLocationData;
import com.dexter.fms.model.app.VehicleOdometerData;
import com.dexter.fms.model.app.VehicleTrackerData;
import com.dexter.fms.model.app.VehicleTrackerEventData;

public class DownloadService extends Thread
{
	private static Logger logger = Logger.getLogger("fmsDS-DownloadService");
    
    static
    {
        //allow connection to all URL
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
        new javax.net.ssl.HostnameVerifier()
        {
            @Override
            public boolean verify(String hostname,
                    javax.net.ssl.SSLSession sslSession)
            {
                return true;
            }
        });
    }
    
    Timer trackerEventTimer, trackerRealTimeTimer;
    TimerTask trackerEventTask, trackerRealTimeTask;
    
    private String userName = "admin1", userHost = "v5", userOrg = "zon_sattrak_telematics", password = "sattrak123", lastGMTTime = null;
    
    public DownloadService()
    {
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
                		logger.info("tracker event service result: " + resultCode);
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
                                    tranTimeCal.add(Calendar.HOUR, 1);
                                    
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
                		
                		int resultCode = commonResultObj.getInt("ResultCode");
                		logger.info("tracker realtime service result: " + resultCode);
                		System.out.println("result code: " + resultCode);
                		if(resultCode == 0)
	                    {
                			String maxGMTUpdateTime = resultJSON.getString("MaxGmtUpdateTime");
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
                                    tranTimeCal.add(Calendar.HOUR, 1);
                                    
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
		
		
		
    }
    
    @Override
    public void run()
    {
    	trackerEventTimer.scheduleAtFixedRate(trackerEventTask, new Date(), 1000 * 60 * 5); // every 5 minutes after first call
        logger.info("Tracker Events Download timer thread: Started");
        
        trackerRealTimeTimer.scheduleAtFixedRate(trackerRealTimeTask, new Date(), 1000 * 60 * 1); // every 1 minutes after first call
        logger.info("Tracker RealTimeData Download timer thread: Started");
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
    
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
    	{
            PrintStream out = new PrintStream(new FileOutputStream("log.txt"));
            System.setOut(out);
        } catch(Exception ex){}
    	
        DownloadService dService = new DownloadService();
        dService.start();
        DownloadService.logger.info("FMS Downloader Thread Started.");
	}
}
