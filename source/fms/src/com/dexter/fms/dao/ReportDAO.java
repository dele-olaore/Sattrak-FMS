package com.dexter.fms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import com.dexter.fms.model.app.VehicleStatusEnum;
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
	
	public long getVehiclesWithoutDriversCount(Long partner_id)
	{
		long count = 0;
		
		try
		{
			connectToDB();
			
			statement = con.prepareStatement("Select count(*) as count from vehicle where id not in (select vehicle_id from vehicledriver where active = 1 and end_dt = null) " +
					"and active = 1 and partner_id = ? and activestatus = ?");
			statement.setLong(1, partner_id);
			statement.setString(2, VehicleStatusEnum.ACTIVE.getStatus());
			
			result = statement.executeQuery();
			while(result.next())
			{
				count = result.getLong(1);
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
		
		return count;
	}
	
	public long getDriversWithoutLicenseCount(Long partner_id)
	{
		long count = 0;
		
		try
		{
			connectToDB();
			
			statement = con.prepareStatement("Select count(*) as count from partnerdriver where id not in " +
					"(select driver_id from driverlicense where active = 1 and expired = 0) and active = 1 and partner_id = ?");
			statement.setLong(1, partner_id);
			
			result = statement.executeQuery();
			while(result.next())
			{
				count = result.getLong(1);
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
		
		return count;
	}
	
	public long getDriversWithoutVehiclesCount(Long partner_id)
	{
		long count = 0;
		
		try
		{
			connectToDB();
			
			statement = con.prepareStatement("Select count(*) as count from partnerdriver where id not in " +
					"(select driver_id from vehicledriver where active = 1 and end_dt = null) and active = 1 and partner_id = ?");
			statement.setLong(1, partner_id);
			
			result = statement.executeQuery();
			while(result.next())
			{
				count = result.getLong(1);
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
		
		return count;
	}
	
	public long getPendingOvertimeRequestCount(long partner_id)
	{
		long count = 0;
		
		try
		{
			connectToDB();
			
			statement = con.prepareStatement("Select count(*) as count from PartnerDriverOvertimeRequest where partner_id = ? and approvalStatus = 'PENDING'");
			statement.setLong(1, partner_id);
			
			result = statement.executeQuery();
			while(result.next())
			{
				count = result.getLong(1);
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
		
		return count;
	}
	
	public long getPendingFuelingRequestCount(Long user_id)
	{
		long count = 0;
		
		try
		{
			connectToDB();
			
			statement = con.prepareStatement("Select count(*) as count from VehicleFuelingRequest where APPROVALUSER_ID = ? and APPROVALSTATUS = 'PENDING'");
			statement.setLong(1, user_id);
			
			result = statement.executeQuery();
			while(result.next())
			{
				count = result.getLong(1);
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
		
		return count;
	}
	
	public long getPendingExpenseRequestCount(Long user_id)
	{
		long count = 0;
		
		try
		{
			connectToDB();
			
			statement = con.prepareStatement("Select count(*) as count from ExpenseRequest where APPROVALUSER_ID = ? and APPROVALSTATUS = 'PENDING'");
			statement.setLong(1, user_id);
			
			result = statement.executeQuery();
			while(result.next())
			{
				count = result.getLong(1);
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
		
		return count;
	}
	
	public Vector<String[]> getBestDrivers(int count, long partnerId, Date start_dt, Date end_dt)
	{
		Vector<String[]> list = new Vector<String[]>();
		
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			connectToDB();
			
			statement = con.prepareStatement("select pd.id, sum(vb.warningValue) as score from partnerdriver pd left outer join vehiclebehaviour vb on vb.driver_id = pd.id " +
					"and vb.eventdate between ? and ? where pd.partner_id = ? group by pd.id order by score");
			statement.setTimestamp(1, Timestamp.valueOf(sdf.format(start_dt)));
			statement.setTimestamp(2, Timestamp.valueOf(sdf.format(end_dt)));
			statement.setLong(3, partnerId);
			
			result = statement.executeQuery();
			int pos = 1;
			while(result.next())
			{
				String[] e = new String[]{""+result.getLong(1), ""+result.getInt(2)};
				list.add(e);
				
				if(pos == count)
					break;
				pos += 1;
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
	
	public Vector<String[]> getWorstDrivers(int count, long partnerId, Date start_dt, Date end_dt)
	{
		Vector<String[]> list = new Vector<String[]>();
		
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			connectToDB();
			
			statement = con.prepareStatement("select pd.id, sum(vb.warningValue) as score from partnerdriver pd left outer join vehiclebehaviour vb on vb.driver_id = pd.id " +
					"and vb.eventdate between ? and ? where pd.partner_id = ? group by pd.id order by score desc");
			statement.setTimestamp(1, Timestamp.valueOf(sdf.format(start_dt)));
			statement.setTimestamp(2, Timestamp.valueOf(sdf.format(end_dt)));
			statement.setLong(3, partnerId);
			
			result = statement.executeQuery();
			int pos = 1;
			while(result.next())
			{
				String[] e = new String[]{""+result.getLong(1), ""+result.getInt(2)};
				list.add(e);
				
				if(pos == count)
					break;
				pos += 1;
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