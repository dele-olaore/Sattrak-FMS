package com.dexter.fms.dao.fuelcard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.persistence.Query;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleDriver;
import com.dexter.fms.model.app.VehicleParameters;
import com.dexter.fms.model.fuelcard.BankRecord;
import com.dexter.fms.model.fuelcard.CardBalanceNotification;
import com.dexter.fms.model.fuelcard.TrackerRecord;
import com.dexter.fms.model.ref.Region;
import com.dexter.fms.util.Env;

public class ReportDAO
{
	/*
	 * Private members for database access, retrieval and manipulation.
	 * */
	private Connection con;
	private PreparedStatement statement;
	private ResultSet result;
	
	//private int output = -1;
	
	public ReportDAO()
	{}
	
	/**
	 * Used internally to connect to the database.
	 * */
	private void connectToDB() throws Exception
	{
		closeConnection();
		
		con = Env.getConnection();
	}
	
	/**
	 * Used internally to close the connection after use. 
	 * */
	private void closeConnection()
	{
		if(result != null)
		{
			try
			{
				result.close();
				result = null;
			}
			catch(Exception ignore){}
		}
		if(con != null)
		{
			try
			{
				con.close();
				con = null;
			}
			catch(Exception ignore){}
		}
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String[]> searchDailyLogSheetRegion(Date d1, Date d2, Region region)
	{
		ArrayList<String[]> list = new ArrayList<String[]>();
		ArrayList<Vehicle> cars = new ArrayList<Vehicle>();
		if(region != null)
		{
			Vector<Vehicle> rcars = new Vector<Vehicle>();
			GeneralDAO gDAO = new GeneralDAO();
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("region", region);
			params.put("active", true);
			Object obj = gDAO.search("Vehicle", params);
			if(obj != null)
				rcars = (Vector<Vehicle>)obj;
			gDAO.destroy();
			cars.addAll(rcars);
		}
		else
		{
			Vector<Vehicle> rcars = new Vector<Vehicle>();
			GeneralDAO gDAO = new GeneralDAO();
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("active", true);
			Object obj = gDAO.search("Vehicle", params);
			if(obj != null)
				rcars = (Vector<Vehicle>)obj;
			gDAO.destroy();
			cars.addAll(rcars);
		}
		
		for(Vehicle c : cars)
		{
			list.addAll(searchDailyLogSheet(d1, d2, c));
		}
		
		return list;
	}
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public ArrayList<String[]> searchDailyLogSheet(Date d1, Date d2, Vehicle car)
	{
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		try
		{
			d2.setHours(23);
			d2.setMinutes(59);
			d2.setSeconds(59);
		}
		catch(Exception ex){}
		
		try
		{
			Vector<BankRecord> brList = new BankRecordDAO().search(d1, d2, car);
			Vector<TrackerRecord> trList = new TrackerRecordDAO().search(d1, d2, car);
			
			BankRecord curBankRec = null;
			TrackerRecord curTrackerRec = null;
			
			TrackerRecord prevTrackerRec = null;
			
			if(brList.size() == trList.size())
			{
				for(int i=0; i<brList.size(); i++)
				{
					curBankRec = brList.get(i);
					curTrackerRec = trList.get(i);
					
					String[] e = new String[15];
					
					e[12] = car.getRegistrationNo();
					e[14] = (car.getRegion()!=null)?car.getRegion().getName():"N/A";
					
					e[13] = "N/A";
					if(curBankRec.getVehicle() != null) {
						GeneralDAO gDAO = new GeneralDAO();
						Query q = gDAO.createQuery("Select e from VehicleDriver e where e.vehicle=:vehicle and e.active=:active");
						q.setParameter("vehicle", curBankRec.getVehicle());
						q.setParameter("active", true);
						Object obj = gDAO.search(q, 0);
						if(obj != null) {
							List<VehicleDriver> vdlist = (List<VehicleDriver>)obj;
							for(VehicleDriver vd : vdlist) {
								e[13] = vd.getDriver().getPersonel().getFirstname() + " " + vd.getDriver().getPersonel().getLastname();
							}
						}
						gDAO.destroy();
					}
					
					e[0] = curBankRec.getTranTime().toLocaleString(); // date and time
					e[1] = curBankRec.getCardAcceptorLoc(); // location
					e[11] = curTrackerRec.getAddress();
					
					try
					{
						double cardBal = curBankRec.getCardBal();
						double tranAmt = curBankRec.getTranAmt();
						
						double amtOnCard = cardBal+tranAmt;
						BigDecimal bg = new BigDecimal(amtOnCard);
						bg = bg.setScale(2, RoundingMode.HALF_UP);
						amtOnCard = bg.doubleValue();
						
						e[2] = ""+amtOnCard; // amount on card, which is cardbalance+tranamt
					}
					catch(Exception ex)
					{
						e[2] = "ERROR"; // number error
					}
					
					if(prevTrackerRec == null)
						e[3] = "N/A";
					else if(prevTrackerRec != null)
						e[3] = ""+prevTrackerRec.getOdometer(); // previous odometer
					
					e[4] = ""+curTrackerRec.getOdometer(); // current odometer
					
					try
					{
						double prev = Double.parseDouble(e[3]);
						double curr = Double.parseDouble(e[4]);
						double covered = curr - prev;
						
						BigDecimal bg = new BigDecimal(covered);
						bg = bg.setScale(2, RoundingMode.HALF_UP);
						covered = bg.doubleValue();
						
						e[5] = ""+covered; // covered = current-previous
					}
					catch(Exception ex)
					{
						e[5] = "N/A";
					}
					
					try
					{
						double tamt = curBankRec.getTranAmt();
						BigDecimal bg = new BigDecimal(tamt);
						bg = bg.setScale(2, RoundingMode.HALF_UP);
						tamt = bg.doubleValue();
						e[6] = ""+tamt; // amount /100.00
					}
					catch(Exception ex)
					{
						e[6] = ""+curBankRec.getTranAmt();
					}
					
					try
					{
						double init = curTrackerRec.getInitFuelLvl();
						double finalf = curTrackerRec.getFinalFuelLvl();
						
						double bought = finalf-init;
						BigDecimal bg = new BigDecimal(bought);
						bg = bg.setScale(2, RoundingMode.HALF_UP);
						bought = bg.doubleValue();
						
						e[7] = ""+bought; // litres bought = cost/rate
					}
					catch(Exception ex)
					{
						e[7] = "N/A";
					}
					
					try
					{
						double bal = curBankRec.getCardBal();
						BigDecimal bg = new BigDecimal(bal);
						bg = bg.setScale(2, RoundingMode.HALF_UP);
						bal = bg.doubleValue();
						e[8] = ""+bal; // balance on card /100.00
					}
					catch(Exception ex)
					{
						e[8] = ""+curBankRec.getCardBal();
					}
					
					e[9] = ""+curTrackerRec.getInitFuelLvl();
					e[10] = ""+curTrackerRec.getFinalFuelLvl();
					
					prevTrackerRec = curTrackerRec;
					
					list.add(e);
				}
			}
			else if(brList.size() > trList.size())
			{
				for(int i=0; i<brList.size(); i++)
				{
					curBankRec = brList.get(i);
					curTrackerRec = null;
					
					for(TrackerRecord tr : trList)
					{
						long diff = curBankRec.getTranTime().getTime() - tr.getTranTime().getTime();
						diff = Math.abs(diff);
						
						if(diff <= (1000*60*14)) // 14 minutes
						{
							curTrackerRec = tr;
							break;
						}
					}
					
					String[] e = new String[15];
					e[14] = (car.getRegion()!=null)?car.getRegion().getName():"N/A";
					e[12] = car.getRegistrationNo();
					
					e[13] = "N/A";
					if(curBankRec.getVehicle() != null) {
						GeneralDAO gDAO = new GeneralDAO();
						Query q = gDAO.createQuery("Select e from VehicleDriver e where e.vehicle=:vehicle and e.active=:active");
						q.setParameter("vehicle", curBankRec.getVehicle());
						q.setParameter("active", true);
						Object obj = gDAO.search(q, 0);
						if(obj != null) {
							List<VehicleDriver> vdlist = (List<VehicleDriver>)obj;
							for(VehicleDriver vd : vdlist) {
								e[13] = vd.getDriver().getPersonel().getFirstname() + " " + vd.getDriver().getPersonel().getLastname();
							}
						}
						gDAO.destroy();
					}
					
					e[0] = curBankRec.getTranTime().toLocaleString(); // date and time
					e[1] = curBankRec.getCardAcceptorLoc(); // location
					
					if(curTrackerRec != null)
						e[11] = curTrackerRec.getAddress();
					else
						e[11] = "N/A";
					
					try
					{
						double cardBal = curBankRec.getCardBal();
						double tranAmt = curBankRec.getTranAmt();
						
						double amtOnCard = cardBal+tranAmt;
						BigDecimal bg = new BigDecimal(amtOnCard);
						bg = bg.setScale(2, RoundingMode.HALF_UP);
						amtOnCard = bg.doubleValue();
						
						e[2] = ""+amtOnCard; // amount on card, which is cardbalance+tranamt
					}
					catch(Exception ex)
					{
						e[2] = "ERROR"; // number error
					}
					
					if(curTrackerRec != null)
					{
						if(prevTrackerRec == null)
							e[3] = "N/A";
						else if(prevTrackerRec != null)
						{
							try
							{
								e[3] = ""+prevTrackerRec.getOdometer(); // previous odometer
							}
							catch(Exception ex)
							{
								e[3] = "N/A";
							}
						}
						e[4] = ""+curTrackerRec.getOdometer(); // current odometer
						
						try
						{
							double prev = Double.parseDouble(e[3]);
							double curr = Double.parseDouble(e[4]);
							double covered = curr - prev;
							BigDecimal bg = new BigDecimal(covered);
							bg = bg.setScale(2, RoundingMode.HALF_UP);
							covered = bg.doubleValue();
							
							e[5] = ""+covered; // covered = current-previous
						}
						catch(Exception ex)
						{
							e[5] = "N/A";
						}
					}
					else
					{
						e[3] = "N/A";
						e[4] = "N/A";
						e[5] = "N/A";
					}
					
					try
					{
						e[6] = ""+(curBankRec.getTranAmt()); // amount /100.00
					}
					catch(Exception ex)
					{
						e[6] = ""+curBankRec.getTranAmt(); // amount
					}
					
					if(curTrackerRec != null)
					{
						try
						{
							double init = curTrackerRec.getInitFuelLvl();
							double finalf = curTrackerRec.getFinalFuelLvl();
							
							double bought = finalf-init;
							BigDecimal bg = new BigDecimal(bought);
							bg = bg.setScale(2, RoundingMode.HALF_UP);
							bought = bg.doubleValue();
							e[7] = ""+bought; // litres bought = cost/rate
						}
						catch(Exception ex)
						{
							e[7] = "N/A";
						}
					}
					else
					{
						e[7] = "N/A";
					}
					
					try
					{
						e[8] = ""+(curBankRec.getCardBal()); // balance on card /100.00
					}
					catch(Exception ex)
					{
						e[8] = ""+curBankRec.getCardBal(); // balance on card
					}
					
					if(curTrackerRec != null)
					{
						e[9] = ""+curTrackerRec.getInitFuelLvl();
						e[10] = ""+curTrackerRec.getFinalFuelLvl();
					}
					else
					{
						e[9] = "N/A";
						e[10] = "N/A";
					}
					
					prevTrackerRec = curTrackerRec;
					
					list.add(e);
				}
			}
			else
			{
				for(int i=0; i<trList.size(); i++)
				{
					curBankRec = null;
					curTrackerRec = trList.get(i);
					
					for(BankRecord br : brList)
					{
						long diff = curTrackerRec.getTranTime().getTime() - br.getTranTime().getTime();
						diff = Math.abs(diff);
						
						if(diff <= (1000*60*14)) // 14 minutes
						{
							curBankRec = br;
							break;
						}
					}
					
					String[] e = new String[15];
					e[14] = (car.getRegion()!=null)?car.getRegion().getName():"N/A";
					e[12] = car.getRegistrationNo();
					
					e[13] = "N/A";
					if(curTrackerRec.getVehicle() != null) {
						GeneralDAO gDAO = new GeneralDAO();
						Query q = gDAO.createQuery("Select e from VehicleDriver e where e.vehicle=:vehicle and e.active=:active");
						q.setParameter("vehicle", curTrackerRec.getVehicle());
						q.setParameter("active", true);
						Object obj = gDAO.search(q, 0);
						if(obj != null) {
							List<VehicleDriver> vdlist = (List<VehicleDriver>)obj;
							for(VehicleDriver vd : vdlist) {
								e[13] = vd.getDriver().getPersonel().getFirstname() + " " + vd.getDriver().getPersonel().getLastname();
							}
						}
						gDAO.destroy();
					}
					
					e[0] = curTrackerRec.getTranTime().toLocaleString(); // date and time
					e[11] = curTrackerRec.getAddress();
					
					if(curBankRec != null)
					{
						e[1] = curBankRec.getCardAcceptorLoc(); // location
						
						try
						{
							double cardBal = curBankRec.getCardBal();
							double tranAmt = curBankRec.getTranAmt();
							
							double amtOnCard = cardBal+tranAmt;
							BigDecimal bg = new BigDecimal(amtOnCard);
							bg = bg.setScale(2, RoundingMode.HALF_UP);
							amtOnCard = bg.doubleValue();
							
							e[2] = ""+amtOnCard; // amount on card, which is cardbalance+tranamt
						}
						catch(Exception ex)
						{
							e[2] = "ERROR"; // number error
						}
					}
					else
					{
						e[1] = "N/A";
						e[2] = "N/A";
					}
					
					if(prevTrackerRec == null)
						e[3] = "N/A";
					else if(prevTrackerRec != null)
					{
						try
						{
							e[3] = ""+prevTrackerRec.getOdometer(); // previous odometer
						}
						catch(Exception ex)
						{
							e[3] = "N/A";
						}
					}
					e[4] = ""+curTrackerRec.getOdometer(); // current odometer
					
					try
					{
						double prev = Double.parseDouble(e[3]);
						double curr = Double.parseDouble(e[4]);
						double covered = curr - prev;
						BigDecimal bg = new BigDecimal(covered);
						bg = bg.setScale(2, RoundingMode.HALF_UP);
						covered = bg.doubleValue();
						e[5] = ""+covered; // covered = current-previous
					}
					catch(Exception ex)
					{
						e[5] = "N/A";
					}
					
					if(curBankRec != null)
					{
						try
						{
							e[6] = ""+(curBankRec.getTranAmt()); // amount /100.00
						}
						catch(Exception ex)
						{
							e[6] = ""+curBankRec.getTranAmt(); // amount
						}
					}
					else
						e[6] = "N/A";
					
					try
					{
						double init = curTrackerRec.getInitFuelLvl();
						double finalf = curTrackerRec.getFinalFuelLvl();
						
						double bought = finalf-init;
						BigDecimal bg = new BigDecimal(bought);
						bg = bg.setScale(2, RoundingMode.HALF_UP);
						bought = bg.doubleValue();
						e[7] = ""+bought; // litres bought = cost/rate
					}
					catch(Exception ex)
					{
						e[7] = "N/A";
					}
					
					if(curBankRec != null)
					{
						try
						{
							e[8] = ""+(curBankRec.getCardBal()); // balance on card /100.00
						}
						catch(Exception ex)
						{
							e[8] = ""+curBankRec.getCardBal(); // balance on card
						}
					}
					else
						e[8] = "N/A";
					
					e[9] = ""+curTrackerRec.getInitFuelLvl();
					e[10] = ""+curTrackerRec.getFinalFuelLvl();
					
					prevTrackerRec = curTrackerRec;
					
					list.add(e);
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			closeConnection();
		}
		
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String[]> searchFuelPurchaseReport(Date d1, Date d2, Region region)
	{
		ArrayList<String[]> list = new ArrayList<String[]>();
		ArrayList<Vehicle> cars = new ArrayList<Vehicle>();
		if(region != null) {
			Vector<Vehicle> rcars = new Vector<Vehicle>();
			GeneralDAO gDAO = new GeneralDAO();
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("region", region);
			params.put("active", true);
			Object obj = gDAO.search("Vehicle", params);
			if(obj != null)
				rcars = (Vector<Vehicle>)obj;
			gDAO.destroy();
			cars.addAll(rcars);
		} else {
			Vector<Vehicle> rcars = new Vector<Vehicle>();
			GeneralDAO gDAO = new GeneralDAO();
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("active", true);
			Object obj = gDAO.search("Vehicle", params);
			if(obj != null)
				rcars = (Vector<Vehicle>)obj;
			gDAO.destroy();
			cars.addAll(rcars);
		}
		
		for(Vehicle c : cars)
		{
			list.addAll(searchFuelPurchaseReport(d1, d2, c.getId().longValue()));
		}
		
		return list;
	}
	
	public double getCardBalance(String cardPan)
	{
		double bal = 0;
		
		try
		{
			connectToDB();
			statement = con.prepareStatement("select cardbal from bankrecord_tb where cardpan = ? " +
					"and trantime = (select max(trantime) from bankrecord_tb where cardpan = ?)");
			statement.setString(1, cardPan);
			statement.setString(2, cardPan);
			
			result = statement.executeQuery();
			while(result.next())
			{
				bal = result.getDouble(1);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			closeConnection();
		}
		
		return bal;
	}
	
	@SuppressWarnings("deprecation")
	public ArrayList<String[]> searchFuelPurchaseReport(Date d1, Date d2, long vehicle_id)
	{
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		try
		{
			String y1 = ""+(d1.getYear()+1900), m1 = ""+(d1.getMonth()+1), dd1 = ""+d1.getDate();
			String y2 = ""+(d2.getYear()+1900), m2 = ""+(d2.getMonth()+1), dd2 = ""+d2.getDate();
			
			m1 = (m1.length() == 1) ? "0"+m1 : m1;
			dd1 = (dd1.length() == 1) ? "0"+dd1 : dd1;
			m2 = (m2.length() == 1) ? "0"+m2 : m2;
			dd2 = (dd2.length() == 1) ? "0"+dd2 : dd2;
			
			ArrayList<String[]> all = new ArrayList<String[]>();
			
			connectToDB();
			
			/*
			 * 
				select d.ID, d.LOCATION, d.COST, d.FINALFUELLVL, d.FUELRATE, d.INITFUELLVL, d.ODOMETER, d.QUANTITY, d.TRANTIME, d.DRIVER_ID, d.CAR_ID, " +
					"t.ID, t.ADDRESS, t.COMP, t.COST, t.FINALFUELLVL, t.INITFUELLVL, t.MODEL, t.ODOMETER, t.QUANTITY, t.TRANTIME, t.TRANTYPE, t.UNITNAME, t.YEAR, " +
					"d.REGNUMBER, u.FULLNAME from [dbo].[DRIVERRECORD_TB] d inner join [dbo].[TRACKERRECORD_TB] t on t.UNITNAME = d.REGNUMBER and d.ID = t.ID inner join [dbo].[USER_TB] u on u.ID = d.DRIVER_ID " +
					"where d.TRANTIME between ? and ?
			 * */
			// 60 minutes
			statement = con.prepareStatement("SELECT tr.trantime, cr.regnumber, 'ADMIN', '', '', tr.odometer, " +
					"tr.finalfuellvl-tr.initfuellvl as fuelqty, br.tranamt, tr.initfuellvl, tr.finalfuellvl," +
					"'PETROL', br.cardacceptorloc, ur.fullname, reg.name FROM trackerrecord_tb tr " +
					"inner join car_tb cr on cr.ID = tr.VEHICLE_ID " + 
					"left outer join user_tb ur on ur.ID = cr.assignedUser_id " + 
					"left outer join region_tb reg on reg.ID = cr.REGION_ID " +
					"left outer join bankrecord_tb br on br.VEHICLE_ID = tr.VEHICLE_ID and " +
					"ABS(TIMESTAMPDIFF(minute, tr.trantime, br.trantime)) <= 60 " +
					"where (tr.trantime between ? and ?) and tr.VEHICLE_ID = ? order by tr.trantime");
			statement.setTimestamp(1, Timestamp.valueOf(y1+"-"+m1+"-"+dd1+" 00:00:00"));
			statement.setTimestamp(2, Timestamp.valueOf(y2+"-"+m2+"-"+dd2+" 23:59:59"));
			statement.setLong(3, vehicle_id);
			
			result = statement.executeQuery();
			ResultSetMetaData rsMeta = result.getMetaData();
			int colCount = rsMeta.getColumnCount();
			
			while(result.next())
			{
				String[] e = new String[colCount];
				for(int i=0; i<colCount; i++)
					e[i] = result.getString(i+1);
				
				all.add(e);
			}
			int size = all.size();
			for(int i=0; i<size; i++)
			{
				String[] rec = all.get(i);
				
				String[] e = new String[17];
				
				e[0] = rec[0]; // date and time
				e[1] = rec[1]; // vin, reg number
				e[2] = rec[2]; // created by
				
				if(i == 0)
				{
					e[3] = "N/A";
					e[4] = "N/A";
				}
				else if(i > 0)
				{
					e[3] = all.get(i)[3]; // previous odometer
					e[4] = all.get(i-1)[5]; // previous odometer
				}
				
				e[5] = rec[4]; // driver odometer
				e[6] = rec[5]; // tracker odometer
				
				try
				{
					double prev = Double.parseDouble(e[4]);
					double curr = Double.parseDouble(e[6]);
					double covered = curr - prev;
					BigDecimal bg = new BigDecimal(covered);
					bg = bg.setScale(2, RoundingMode.HALF_UP);
					covered = bg.doubleValue();
					e[7] = ""+covered; // covered = current-previous
				}
				catch(Exception ex)
				{
					e[7] = "N/A";
				}
				
				try
				{
					//double rate = Double.parseDouble(rec[4]);
					//double cost = Double.parseDouble(rec[2]);
					
					//double bought = cost/rate;
					//e[8] = ""+bought; // litres bought = cost/rate
					e[8] = rec[6];
				}
				catch(Exception ex)
				{
					e[8] = "N/A";
				}
				
				try
				{
					e[9] = ""+Double.parseDouble(rec[7]); // amount /100.00
				}
				catch(Exception ex)
				{
					e[9] = rec[7]; // amount
				}
				
				// (Current Odometer reading - Previous Odometer reading)/(Final Fuel Level - Initial Fuel Level)
				try
				{
					double curOdo = Double.parseDouble(e[6]);
					double prevOdo = Double.parseDouble(e[4]);
					
					double finFuelLvl = Double.parseDouble(rec[9]);
					double iniFuelLvl = Double.parseDouble(rec[8]);
					
					double cal = ((curOdo-prevOdo) / (finFuelLvl-iniFuelLvl));
					BigDecimal bg = new BigDecimal(cal);
					bg = bg.setScale(2, RoundingMode.HALF_UP);
					cal = bg.doubleValue();
					e[13] = ""+cal;
					//Double.parseDouble(e[13]);
				}
				catch(Exception ex)
				{
					e[13] = "N/A";
				}
				
				e[10] = "PETROL"; // fuel brand
				e[11] = rec[11]; // dealer
				e[12] = rec[12]; // purchase by
				if(e[12] == null || (e[12]!=null && e[12].trim().equalsIgnoreCase("null")))
					e[12] = "N/A";
				
				e[14] = rec[8];
				e[15] = rec[9];
				
				e[16] = rec[13]; // region
				if(e[16] == null || (e[16]!=null && e[16].trim().equalsIgnoreCase("null")))
					e[16] = "N/A";
				
				list.add(e);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			closeConnection();
		}
		
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String[]> searchFuelPurchaseReportSummary(Date d1, Date d2, Region region)
	{
		ArrayList<String[]> list = new ArrayList<String[]>();
		ArrayList<Vehicle> cars = new ArrayList<Vehicle>();
		Vector<Vehicle> rcars = new Vector<Vehicle>();
		GeneralDAO gDAO = new GeneralDAO();
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		if(region != null)
			params.put("region", region);
		params.put("active", true);
		Object obj = gDAO.search("Vehicle", params);
		if(obj != null)
			rcars = (Vector<Vehicle>)obj;
		gDAO.destroy();
		cars.addAll(rcars);
		
		for(Vehicle c : cars)
		{
			list.addAll(searchFuelPurchaseReportSummary(d1, d2, c.getId().longValue()));
		}
		
		return list;
	}
	
	@SuppressWarnings("deprecation")
	public ArrayList<String[]> searchFuelPurchaseReportSummary(Date d1, Date d2, long vehicle_id)
	{
		ArrayList<String[]> list = new ArrayList<String[]>(), listToReturn = new ArrayList<String[]>();
		
		try
		{
			String y1 = ""+(d1.getYear()+1900), m1 = ""+(d1.getMonth()+1), dd1 = ""+d1.getDate();
			String y2 = ""+(d2.getYear()+1900), m2 = ""+(d2.getMonth()+1), dd2 = ""+d2.getDate();
			
			m1 = (m1.length() == 1) ? "0"+m1 : m1;
			dd1 = (dd1.length() == 1) ? "0"+dd1 : dd1;
			m2 = (m2.length() == 1) ? "0"+m2 : m2;
			dd2 = (dd2.length() == 1) ? "0"+dd2 : dd2;
			
			ArrayList<String[]> all = new ArrayList<String[]>();
			
			connectToDB();
			
			/*
			 * 
				select d.ID, d.LOCATION, d.COST, d.FINALFUELLVL, d.FUELRATE, d.INITFUELLVL, d.ODOMETER, d.QUANTITY, d.TRANTIME, d.DRIVER_ID, d.CAR_ID, " +
					"t.ID, t.ADDRESS, t.COMP, t.COST, t.FINALFUELLVL, t.INITFUELLVL, t.MODEL, t.ODOMETER, t.QUANTITY, t.TRANTIME, t.TRANTYPE, t.UNITNAME, t.YEAR, " +
					"d.REGNUMBER, u.FULLNAME from [dbo].[DRIVERRECORD_TB] d inner join [dbo].[TRACKERRECORD_TB] t on t.UNITNAME = d.REGNUMBER and d.ID = t.ID inner join [dbo].[USER_TB] u on u.ID = d.DRIVER_ID " +
					"where d.TRANTIME between ? and ?
			 * */
			// 60 minutes
			statement = con.prepareStatement("SELECT tr.trantime, cr.regnumber, 'ADMIN', '', '', tr.odometer, " +
					"tr.finalfuellvl-tr.initfuellvl as fuelqty, br.tranamt, tr.initfuellvl, tr.finalfuellvl," +
					"'PETROL', br.cardacceptorloc, ur.fullname, reg.name FROM trackerrecord_tb tr " +
					"inner join car_tb cr on cr.ID = tr.VEHICLE_ID " + 
					"left outer join user_tb ur on ur.ID = cr.assignedUser_id " + 
					"left outer join region_tb reg on reg.ID = cr.REGION_ID " +
					"left outer join bankrecord_tb br on br.VEHICLE_ID = tr.VEHICLE_ID and " +
					"ABS(TIMESTAMPDIFF(minute, tr.trantime, br.trantime)) <= 60 " +
					"where (tr.trantime between ? and ?) and tr.VEHICLE_ID = ? order by tr.trantime");
			statement.setTimestamp(1, Timestamp.valueOf(y1+"-"+m1+"-"+dd1+" 00:00:00"));
			statement.setTimestamp(2, Timestamp.valueOf(y2+"-"+m2+"-"+dd2+" 23:59:59"));
			statement.setLong(3, vehicle_id);
			
			result = statement.executeQuery();
			ResultSetMetaData rsMeta = result.getMetaData();
			int colCount = rsMeta.getColumnCount();
			
			while(result.next())
			{
				String[] e = new String[colCount];
				for(int i=0; i<colCount; i++)
					e[i] = result.getString(i+1);
				
				all.add(e);
			}
			int size = all.size();
			for(int i=0; i<size; i++)
			{
				String[] rec = all.get(i);
				
				String[] e = new String[17];
				
				e[0] = rec[0]; // date and time
				e[1] = rec[1]; // vin, reg number
				e[2] = rec[2]; // created by
				
				if(i == 0)
				{
					e[3] = "N/A";
					e[4] = "N/A";
				}
				else if(i > 0)
				{
					e[3] = all.get(i)[3]; // previous odometer
					e[4] = all.get(i-1)[5]; // previous odometer
				}
				
				e[5] = rec[4]; // driver odometer
				e[6] = rec[5]; // tracker odometer
				
				try
				{
					double prev = Double.parseDouble(e[4]);
					double curr = Double.parseDouble(e[6]);
					double covered = curr - prev;
					BigDecimal bg = new BigDecimal(covered);
					bg = bg.setScale(2, RoundingMode.HALF_UP);
					covered = bg.doubleValue();
					e[7] = ""+covered; // covered = current-previous
				}
				catch(Exception ex)
				{
					e[7] = "N/A";
				}
				
				try
				{
					//double rate = Double.parseDouble(rec[4]);
					//double cost = Double.parseDouble(rec[2]);
					
					//double bought = cost/rate;
					//e[8] = ""+bought; // litres bought = cost/rate
					e[8] = rec[6];
				}
				catch(Exception ex)
				{
					e[8] = "N/A";
				}
				
				try
				{
					e[9] = ""+Double.parseDouble(rec[7]); // amount /100.00
				}
				catch(Exception ex)
				{
					e[9] = rec[7]; // amount
				}
				
				// (Current Odometer reading - Previous Odometer reading)/(Final Fuel Level - Initial Fuel Level)
				try
				{
					double curOdo = Double.parseDouble(e[6]);
					double prevOdo = Double.parseDouble(e[4]);
					
					double finFuelLvl = Double.parseDouble(rec[9]);
					double iniFuelLvl = Double.parseDouble(rec[8]);
					
					double cal = ((curOdo-prevOdo) / (finFuelLvl-iniFuelLvl));
					BigDecimal bg = new BigDecimal(cal);
					bg = bg.setScale(2, RoundingMode.HALF_UP);
					cal = bg.doubleValue();
					e[13] = ""+cal;
					//Double.parseDouble(e[13]);
				}
				catch(Exception ex)
				{
					e[13] = "N/A";
				}
				
				e[10] = "PETROL"; // fuel brand
				e[11] = rec[11]; // dealer
				e[12] = rec[12]; // purchase by
				
				if(e[12] == null || (e[12]!=null && e[12].trim().equalsIgnoreCase("null")))
					e[12] = "N/A";
				
				e[14] = rec[8];
				e[15] = rec[9];
				
				e[16] = rec[13];
				if(e[16] == null || (e[16]!=null && e[16].trim().equalsIgnoreCase("null")))
					e[16] = "N/A";
				
				list.add(e);
			}
			
			String regionName = null, regNumber = null, start_km = null, end_km = null, total_distance = null;
			double total_fuelcost = 0, total_fuelqty = 0;
			for(int i=0; i<list.size(); i++)
			{
				String[] e = list.get(i);
				if(regNumber == null)
					regNumber = e[1];
				if(regionName == null)
					regionName = e[16];
				if(start_km == null)
				{
					try
					{
						start_km = ""+Double.parseDouble(e[6]);
					}catch(Exception ex){start_km = null;}
				}
				try
				{
					end_km = ""+Double.parseDouble(e[6]);
				}catch(Exception ex){}
				try
				{
					total_fuelcost += Double.parseDouble(e[9]);
				}catch(Exception ex){}
				try
				{
					total_fuelqty += Double.parseDouble(e[8]);
				}catch(Exception ex){}
			}
			if(start_km != null && end_km != null)
			{
				total_distance = ""+(Double.parseDouble(end_km)-Double.parseDouble(start_km));
			}
			if(regionName == null)
				regionName = "N/A";
			if(regNumber == null)
				regNumber = "N/A";
			if(start_km == null)
				start_km = "N/A";
			if(end_km == null)
				end_km = "N/A";
			if(total_distance == null)
				total_distance = "N/A";
			String[] rec = new String[]{regionName, regNumber, start_km, end_km, total_distance, ""+total_fuelcost, ""+total_fuelqty};
			if(regNumber != null)
				listToReturn.add(rec);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			closeConnection();
		}
		
		return listToReturn;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String[]> exceptionTransactionsRegion(Date d1, Date d2, Region region)
	{
		ArrayList<String[]> list = new ArrayList<String[]>();
		ArrayList<Vehicle> cars = new ArrayList<Vehicle>();
		Vector<Vehicle> rcars = new Vector<Vehicle>();
		GeneralDAO gDAO = new GeneralDAO();
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		if(region != null)
			params.put("region", region);
		params.put("active", true);
		Object obj = gDAO.search("Vehicle", params);
		if(obj != null)
			rcars = (Vector<Vehicle>)obj;
		gDAO.destroy();
		cars.addAll(rcars);
		
		for(Vehicle c : cars)
		{
			list.addAll(exceptionTransactions(d1, d2, c));
		}
		
		return list;
	}
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public ArrayList<String[]> exceptionTransactions(Date d1, Date d2, Vehicle car)
	{
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		// Tracker record without bank record
		// Tracker record with final fuel level more than vehicle calibrated capacity
		// Bank record without tracker record
		
		try
		{
			d2.setHours(23);
			d2.setMinutes(59);
			d2.setSeconds(59);
		}
		catch(Exception ex){}
		
		SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		
		try
		{
			connectToDB();
			
			Vector<CardBalanceNotification> settings = new CardBalanceDAO().getSettings();
			
			// Bank record without tracker record
			Vector<BankRecord> brList = new BankRecordDAO().search(d1, d2, car);
			for(BankRecord br : brList)
			{
				if(br.getVehicle() != null)
				{
					BigDecimal amt = new BigDecimal(br.getTranAmt());
                    amt = amt.setScale(2, RoundingMode.HALF_UP);
                    
					CardBalanceNotification setting = null;
					for(CardBalanceNotification e : settings)
					{
						if(e.getRegion() != null && br.getVehicle().getRegion() != null &&
								e.getRegion().getId().longValue() == br.getVehicle().getRegion().getId().longValue())
						{
							setting = e;
							break;
						}
					}
					
					// 60 minutes
					statement = con.prepareStatement("SELECT * FROM trackerrecord_tb " +
							"where VEHICLE_ID = ? and " +
							"ABS(TIMESTAMPDIFF(minute, trantime, ?)) <= 60 ");
					statement.setLong(1, br.getVehicle().getId());
					statement.setTimestamp(2, Timestamp.valueOf(sdformat.format(br.getTranTime())));// br.getTranTime().getTime()
					
					result = statement.executeQuery();
					boolean found = false;
					while(result.next())
					{
						found = true;
						if(setting != null)
                        {
                            double unitP = 1;
                            
                            String ftype = "PETROL";
                            Hashtable<String, Object> params = new Hashtable<String, Object>();
                            params.put("vehicle", br.getVehicle());
                            GeneralDAO gDAO = new GeneralDAO();
                            Object obj = gDAO.search("VehicleParameters", params);
                            if(obj != null) {
                            	List<VehicleParameters> vplist = (List<VehicleParameters>)obj;
                            	for(VehicleParameters vp : vplist) {
                            		ftype = vp.getFuelType().getName();
                            	}
                            }
                            gDAO.destroy();
                            
                            if(ftype.equalsIgnoreCase("PETROL"))
                                unitP = setting.getPetrolUnitPrice();
                            else
                                unitP = setting.getDisealUnitPrice();
                            
                            double trackerQuantity = result.getDouble("quantity");
                            double estLitres = br.getTranAmt()/unitP;
                            try{
                            BigDecimal trackerQuantityBD = new BigDecimal(trackerQuantity);
                            trackerQuantityBD = trackerQuantityBD.setScale(2, RoundingMode.HALF_UP);
                            trackerQuantity = trackerQuantityBD.doubleValue();
                            } catch(Exception ex){}
                            try{
                            BigDecimal estLitresBD = new BigDecimal(estLitres);
                            estLitresBD = estLitresBD.setScale(2, RoundingMode.HALF_UP);
                            estLitres = estLitresBD.doubleValue();
                            } catch(Exception ex){}
                            
                            if(Math.abs(trackerQuantity-estLitres) <= 15);
                            else
                            {
                            	String[] e = new String[4];
        						e[0] = "Transaction amount not inline with purchased litres";
        						e[1] = br.getVehicle().getRegistrationNo();
        						e[2] = br.getTranTime().toLocaleString();
        						e[3] = "Transaction Amount: " + NumberFormat.getInstance().format(amt.doubleValue()) + ", Unit Price: " + NumberFormat.getInstance().format(unitP) + ", Expected Litres: " + NumberFormat.getInstance().format(estLitres) + ", Found Litres: " + NumberFormat.getInstance().format(trackerQuantity) + ", Diff: " + NumberFormat.getInstance().format(estLitres-trackerQuantity);
        						list.add(e);
                            }
                        }
					}
					if(!found)
					{
						// this is an exception
						String[] e = new String[4];
						e[0] = "Fuel card used without vehicle refuel";
						e[1] = br.getVehicle().getRegistrationNo();
						e[2] = br.getTranTime().toLocaleString();
						try
						{
						e[3] = "Card Pan: " + br.getCardPan() + ", Amount: " + NumberFormat.getInstance().format(amt.doubleValue());
						}
						catch(Exception ex)
						{
							e[3] = "Card Pan: " + br.getCardPan() + ", Amount: " + NumberFormat.getInstance().format(amt.doubleValue());
						}
						list.add(e);
					}
				}
			}
			
			Vector<TrackerRecord> trList = new TrackerRecordDAO().search(d1, d2, car);
			for(TrackerRecord tr : trList)
			{
				if(tr.getVehicle() != null)
				{
					// 60 minutes
					statement = con.prepareStatement("SELECT * FROM bankrecord_tb " +
							"where VEHICLE_ID = ? and " +
							"ABS(TIMESTAMPDIFF(minute, trantime, ?)) <= 60 ");
					statement.setLong(1, tr.getVehicle().getId());
					statement.setTimestamp(2, Timestamp.valueOf(sdformat.format(tr.getTranTime())));// tr.getTranTime().getTime()
					
					result = statement.executeQuery();
					boolean found = false;
					while(result.next())
						found = true;
					if(!found)
					{
						// this is an exception
						double litres = (tr.getFinalFuelLvl()-tr.getInitFuelLvl());
						try{
                        BigDecimal big_litres = new BigDecimal(litres);
                        big_litres = big_litres.setScale(2, RoundingMode.HALF_UP);
                        litres = big_litres.doubleValue();
                        } catch(Exception ex){}
                            
                        if(litres > 10)
                        {
							String[] e = new String[4];
							e[0] = "Vehicle refueled without fuel card";
							e[1] = tr.getVehicle().getRegistrationNo();
							e[2] = tr.getTranTime().toLocaleString();
							e[3] = "Litres: " + NumberFormat.getInstance().format(litres);
							list.add(e);
                        }
					}
					
					VehicleParameters vparam = null;
					Hashtable<String, Object> params = new Hashtable<String, Object>();
                    params.put("vehicle", tr.getVehicle());
                    GeneralDAO gDAO = new GeneralDAO();
                    Object obj = gDAO.search("VehicleParameters", params);
                    if(obj != null) {
                    	List<VehicleParameters> vplist = (List<VehicleParameters>)obj;
                    	for(VehicleParameters vp : vplist) {
                    		vparam = vp;
                    	}
                    }
                    gDAO.destroy();
					
					BigDecimal fl = new BigDecimal(tr.getFinalFuelLvl());
                    fl = fl.setScale(2, RoundingMode.HALF_UP);
                    if(vparam != null && vparam.getCalibratedcapacity() > 0 && fl.doubleValue() > vparam.getCalibratedcapacity())
					{
						String[] e = new String[4];
						e[0] = "Fuel level above calibrated fuel capacity";
						e[1] = tr.getVehicle().getRegistrationNo();
						e[2] = tr.getTranTime().toLocaleString();
						e[3] = "Final fuel level: " + NumberFormat.getInstance().format(fl.doubleValue()) + ", Calibrated capacity: " + vparam.getCalibratedcapacity();
						list.add(e);
					}
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return list;
	}
	
	@SuppressWarnings("deprecation")
	public ArrayList<String[]> highestFuelConsumption(Date d1, Date d2)
	{
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		try
		{
			d2.setHours(23);
			d2.setMinutes(59);
			d2.setSeconds(59);
		}
		catch(Exception ig){}
		
		try
		{
			Hashtable<Integer, ArrayList<TrackerRecord>> vehicleTRList = new Hashtable<Integer, ArrayList<TrackerRecord>>();
			
			Vector<TrackerRecord> trList = new TrackerRecordDAO().search(d1, d2);
			for(TrackerRecord tr : trList)
			{
				ArrayList<TrackerRecord> arrL = new ArrayList<TrackerRecord>();
				if(vehicleTRList.containsKey(tr.getUnitID()))
					arrL = vehicleTRList.get(tr.getUnitID());
				
				arrL.add(tr);
				vehicleTRList.put(tr.getUnitID(), arrL);
			}
			Integer[] keys = new Integer[vehicleTRList.keySet().size()];
			keys = vehicleTRList.keySet().toArray(keys);
			Hashtable<Double, Integer> allList = new Hashtable<Double, Integer>();
			for(int i=0; i<keys.length; i++)
			{
				ArrayList<TrackerRecord> vtrsList = vehicleTRList.get(keys[i]);
				double vtconsumption = 0.0;
				for(int a=0; a<vtrsList.size(); a++)
				{
					TrackerRecord tr = vtrsList.get(a);
					if(a == 0) // this is the first record
					{}
					else // inbetween records
					{
						double curcomp = vtrsList.get(a-1).getFinalFuelLvl()-tr.getInitFuelLvl();
						vtconsumption += curcomp;
					}
				}
				allList.put(vtconsumption, keys[i]);
			}
			Double[] comsupvalues = new Double[allList.size()];
			comsupvalues = allList.keySet().toArray(comsupvalues);
			Arrays.sort(comsupvalues);
			for(int i=(comsupvalues.length-1); i>=0; i--) // take from the highest down
			{
				int key = allList.get(comsupvalues[i]);
				ArrayList<TrackerRecord> vtrsList = vehicleTRList.get(key);
				String[] e = new String[]{vtrsList.get(0).getVehicle().getRegistrationNo(), ""+comsupvalues[i], (vtrsList.get(0).getVehicle().getRegion()!=null) ? vtrsList.get(0).getVehicle().getRegion().getName() : "N/A"};
				list.add(e);
				if(i == comsupvalues.length-10) // top ten
					break;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return list;
	}
	
	@SuppressWarnings("deprecation")
	public ArrayList<String[]> highestFuelPurchase(Date d1, Date d2)
	{
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		try
		{
			connectToDB();
			
			String y1 = ""+(d1.getYear()+1900), m1 = ""+(d1.getMonth()+1), dd1 = ""+d1.getDate();
			String y2 = ""+(d2.getYear()+1900), m2 = ""+(d2.getMonth()+1), dd2 = ""+d2.getDate();
			
			m1 = (m1.length() == 1) ? "0"+m1 : m1;
			dd1 = (dd1.length() == 1) ? "0"+dd1 : dd1;
			m2 = (m2.length() == 1) ? "0"+m2 : m2;
			dd2 = (dd2.length() == 1) ? "0"+dd2 : dd2;
			
			statement = con.prepareStatement("select br.vehicle_id, c.regnumber, (sum(br.tranamt)) as total from bankrecord_tb br " +
					"inner join car_tb c on c.id = br.vehicle_id " +
					"where br.trantime between ? and ? " +
					"group by br.vehicle_id order by total desc;");
			statement.setTimestamp(1, Timestamp.valueOf(y1+"-"+m1+"-"+dd1+" 00:00:00"));
			statement.setTimestamp(2, Timestamp.valueOf(y2+"-"+m2+"-"+dd2+" 23:59:59"));
			
			result = statement.executeQuery();
			ResultSetMetaData rsMeta = result.getMetaData();
			int colCount = rsMeta.getColumnCount();
			
			while(result.next())
			{
				String[] e = new String[colCount];
				for(int i=0; i<colCount; i++)
					e[i] = result.getString(i+1);
				
				list.add(e);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			closeConnection();
		}
		
		return list;
	}
	
	@SuppressWarnings("deprecation")
	public ArrayList<String[]> longestDistance(Date d1, Date d2)
	{
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		try
		{
			d2.setHours(23);
			d2.setMinutes(59);
			d2.setSeconds(59);
		}
		catch(Exception ig){}
		
		try
		{
			Hashtable<Integer, ArrayList<TrackerRecord>> vehicleTRList = new Hashtable<Integer, ArrayList<TrackerRecord>>();
			
			Vector<TrackerRecord> trList = new TrackerRecordDAO().search(d1, d2);
			for(TrackerRecord tr : trList)
			{
				ArrayList<TrackerRecord> arrL = new ArrayList<TrackerRecord>();
				if(vehicleTRList.containsKey(tr.getUnitID()))
					arrL = vehicleTRList.get(tr.getUnitID());
				
				arrL.add(tr);
				vehicleTRList.put(tr.getUnitID(), arrL);
			}
			Integer[] keys = new Integer[vehicleTRList.keySet().size()];
			keys = vehicleTRList.keySet().toArray(keys);
			Hashtable<Double, Integer> allList = new Hashtable<Double, Integer>();
			for(int i=0; i<keys.length; i++)
			{
				ArrayList<TrackerRecord> vtrsList = vehicleTRList.get(keys[i]);
				double vtdistance = 0.0;
				for(int a=0; a<vtrsList.size(); a++)
				{
					TrackerRecord tr = vtrsList.get(a);
					if(a == 0) // this is the first record
					{}
					else // inbetween records
					{
						double curdistance = tr.getOdometer() - vtrsList.get(a-1).getOdometer();
						vtdistance += curdistance;
					}
				}
				BigDecimal bg = new BigDecimal(vtdistance);
				bg = bg.setScale(2, RoundingMode.HALF_UP);
				vtdistance = bg.doubleValue();
				allList.put(vtdistance, keys[i]);
			}
			Double[] comsupvalues = new Double[allList.size()];
			comsupvalues = allList.keySet().toArray(comsupvalues);
			Arrays.sort(comsupvalues);
			for(int i=(comsupvalues.length-1); i>=0; i--) // take from the highest down
			{
				int key = allList.get(comsupvalues[i]);
				ArrayList<TrackerRecord> vtrsList = vehicleTRList.get(key);
				String[] e = new String[]{vtrsList.get(0).getVehicle().getRegistrationNo(), ""+comsupvalues[i]};
				list.add(e);
				if(i == comsupvalues.length-10) // top ten
					break;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return list;
	}
	
	@SuppressWarnings("deprecation")
	public ArrayList<String[]> bestEfficiency(Date d1, Date d2)
	{
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		try
		{
			d2.setHours(23);
			d2.setMinutes(59);
			d2.setSeconds(59);
		}
		catch(Exception ig){}
		
		try
		{
			Hashtable<Integer, ArrayList<TrackerRecord>> vehicleTRList = new Hashtable<Integer, ArrayList<TrackerRecord>>();
			
			Vector<TrackerRecord> trList = new TrackerRecordDAO().search(d1, d2);
			for(TrackerRecord tr : trList)
			{
				ArrayList<TrackerRecord> arrL = new ArrayList<TrackerRecord>();
				if(vehicleTRList.containsKey(tr.getUnitID()))
					arrL = vehicleTRList.get(tr.getUnitID());
				
				arrL.add(tr);
				vehicleTRList.put(tr.getUnitID(), arrL);
			}
			Integer[] keys = new Integer[vehicleTRList.keySet().size()];
			keys = vehicleTRList.keySet().toArray(keys);
			Hashtable<Double, Integer> allList = new Hashtable<Double, Integer>();
			for(int i=0; i<keys.length; i++)
			{
				ArrayList<TrackerRecord> vtrsList = vehicleTRList.get(keys[i]);
				double vtefficiency = 0.0;
				for(int a=0; a<vtrsList.size(); a++)
				{
					TrackerRecord tr = vtrsList.get(a);
					if(a == 0) // this is the first record
					{}
					else // inbetween records
					{
						double curodochange = tr.getOdometer()-vtrsList.get(a-1).getOdometer();
						double curcomp = vtrsList.get(a-1).getFinalFuelLvl()-tr.getInitFuelLvl();
						vtefficiency += (curodochange/curcomp);
					}
				}
				BigDecimal bg = new BigDecimal(vtefficiency);
				bg = bg.setScale(2, RoundingMode.HALF_UP);
				vtefficiency = bg.doubleValue();
				allList.put(vtefficiency, keys[i]);
			}
			Double[] comsupvalues = new Double[allList.size()];
			comsupvalues = allList.keySet().toArray(comsupvalues);
			Arrays.sort(comsupvalues);
			for(int i=(comsupvalues.length-1); i>=0; i--) // take from the highest down
			{
				int key = allList.get(comsupvalues[i]);
				ArrayList<TrackerRecord> vtrsList = vehicleTRList.get(key);
				String[] e = new String[]{vtrsList.get(0).getVehicle().getRegistrationNo(), ""+comsupvalues[i]};
				list.add(e);
				if(i == comsupvalues.length-10) // top ten
					break;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return list;
	}
	
	@SuppressWarnings("deprecation")
	public ArrayList<String[]> searchReport(Date tranDate)
	{
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		try
		{
			connectToDB();
			
			statement = con.prepareStatement("select * from transactions where [Transaction Date] between ? and ?");
			
			String yr = ""+(1900 + tranDate.getYear());
			String mm = ""+(tranDate.getMonth()+1);
			if(mm.length() == 1)
				mm = "0"+mm;
			String dd = ""+tranDate.getDate();
			if(dd.length() == 1)
				dd = "0"+dd;
			
			String date = yr+"-"+mm+"-"+dd;
			statement.setTimestamp(1, Timestamp.valueOf(date + " 00:00:000"));
			statement.setTimestamp(2, Timestamp.valueOf(date + " 23:59:000"));
			
			result = statement.executeQuery();
			ResultSetMetaData rsMeta = result.getMetaData();
			int colCount = rsMeta.getColumnCount();
			
			/*String[] cols = new String[colCount];
			for(int i=0; i<colCount; i++)
				cols[i] = rsMeta.getColumnLabel(i+1);
			list.add(cols);*/
			
			while(result.next())
			{
				String[] e = new String[colCount];
				for(int i=0; i<colCount; i++)
					e[i] = result.getString(i+1);
				
				list.add(e);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			closeConnection();
		}
		
		return list;
	}
	
	@SuppressWarnings("deprecation")
	public ArrayList<String[]> searchReport2(Date tranDate)
	{
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		try
		{
			connectToDB();
			
			statement = con.prepareStatement("select * from fuel_efficiency where [Transaction Date] between ? and ?");
			
			String yr = ""+(1900 + tranDate.getYear());
			String mm = ""+(tranDate.getMonth()+1);
			if(mm.length() == 1)
				mm = "0"+mm;
			String dd = ""+tranDate.getDate();
			if(dd.length() == 1)
				dd = "0"+dd;
			
			String date = yr+"-"+mm+"-"+dd;
			//statement.setDate(1, new java.sql.Date(tranDate.getTime()));
			statement.setTimestamp(1, Timestamp.valueOf(date + " 00:00:000"));
			statement.setTimestamp(2, Timestamp.valueOf(date + " 23:59:000"));
			
			result = statement.executeQuery();
			ResultSetMetaData rsMeta = result.getMetaData();
			int colCount = rsMeta.getColumnCount();
			
			/*String[] cols = new String[colCount];
			for(int i=0; i<colCount; i++)
				cols[i] = rsMeta.getColumnLabel(i+1);
			list.add(cols);*/
			
			while(result.next())
			{
				String[] e = new String[colCount];
				for(int i=0; i<colCount; i++)
					e[i] = result.getString(i+1);
				
				list.add(e);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			closeConnection();
		}
		
		return list;
	}
}
