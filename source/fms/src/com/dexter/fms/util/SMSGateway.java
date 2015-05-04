package com.dexter.fms.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SMSGateway
{
	//private static final String url = "https://www.bbnplace.com/sms/bulksms/bulksms.php?username=ayo.oyeleye@fets.com.ng&password=fets123";
	private static final String url = "http://www.joybulksms.com/smsapi/smsapi.php?action=send&username=XXXXX&password=YYYY&msgtype=0";
	
	@SuppressWarnings("deprecation")
	public static synchronized boolean sendSMS(String sender, String mobileNumber, String message)
	{
		boolean ret = false;
		
		String postedurl = url + "&sender=" + URLEncoder.encode(sender) + "&phones=" + URLEncoder.encode(mobileNumber) + "&message=" + URLEncoder.encode(message);
		URL url = null;
		HttpURLConnection conn = null;
		try
		{
			url = new URL(postedurl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
	
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output;
			while ((output = br.readLine()) != null)
			{
				if(output.trim().startsWith("700"))
				{
					ret = true;
				}
			}
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				try{conn.disconnect();}catch(Exception ignore){}
			}
		}
		
		return ret;
	}
}
