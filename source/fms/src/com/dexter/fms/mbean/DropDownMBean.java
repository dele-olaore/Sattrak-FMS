package com.dexter.fms.mbean;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.ApplicationType;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.SubscriptionPackage;
import com.dexter.fms.model.SubscriptionType;
import com.dexter.fms.model.app.Fleet;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.ref.DriverGrade;
import com.dexter.fms.model.ref.LicenseType;
import com.dexter.fms.model.ref.TransactionType;

@ManagedBean(name = "ddBean")
@SessionScoped
public class DropDownMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-DropDownMBean");
	
	@SuppressWarnings("unused")
	private FacesMessage msg = null;
	
	public DropDownMBean()
	{}
	
	@SuppressWarnings("unchecked")
	public Vector<TransactionType> getTransactionTypes()
	{
		//TODO This should be per partner
		GeneralDAO gDAO = new GeneralDAO();
		
		Object obj = gDAO.findAll("TransactionType");
		if(obj != null)
		{
			return (Vector<TransactionType>)obj;
		}
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<LicenseType> getLicenseTypes()
	{
		//TODO This should be per partner
		GeneralDAO gDAO = new GeneralDAO();
		
		Object obj = gDAO.findAll("LicenseType");
		if(obj != null)
		{
			return (Vector<LicenseType>)obj;
		}
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Vehicle> getVehicles(Long partner_id, Long fleet_id, String regNo)
	{
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		
		if(partner_id != null && partner_id > 0)
		{
			Object partnerObj = gDAO.find(Partner.class, partner_id);
			if(partnerObj != null)
				params.put("partner", (Partner)partnerObj);
		}
		
		if(fleet_id != null && fleet_id > 0)
		{
			Object fleetObj = gDAO.find(Fleet.class, fleet_id);
			if(fleetObj != null)
				params.put("fleet", (Fleet)fleetObj);
		}
		
		if(regNo != null && regNo.trim().length() > 0)
			params.put("registrationNo", regNo);
		
		Object resultObj = gDAO.search("Vehicle", params);
		if(resultObj != null)
			return (Vector<Vehicle>)resultObj;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Partner> getPartnersWithoutSattrak()
	{
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("isSattrak", false);
		
		Object obj = gDAO.search("Partner", params);
		if(obj != null)
		{
			return (Vector<Partner>)obj;
		}
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<DriverGrade> getDriverGrades()
	{
		//TODO This should be per partner
		GeneralDAO gDAO = new GeneralDAO();
		
		Object obj = gDAO.findAll("DriverGrade");
		if(obj != null)
		{
			return (Vector<DriverGrade>)obj;
		}
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Partner> getPartners()
	{
		GeneralDAO gDAO = new GeneralDAO();
		
		Object obj = gDAO.findAll("Partner");
		if(obj != null)
		{
			return (Vector<Partner>)obj;
		}
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<SubscriptionPackage> getAllPackages()
	{
		GeneralDAO gDAO = new GeneralDAO();
		Object obj = gDAO.findAll("SubscriptionPackage");
		if(obj != null)
		{
			return (Vector<SubscriptionPackage>)obj;
		}
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<ApplicationType> getAllAppTypes()
	{
		GeneralDAO gDAO = new GeneralDAO();
		Object obj = gDAO.findAll("ApplicationType");
		if(obj != null)
		{
			return (Vector<ApplicationType>)obj;
		}
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<SubscriptionType> getAllSubTypes()
	{
		GeneralDAO gDAO = new GeneralDAO();
		Object obj = gDAO.findAll("SubscriptionType");
		if(obj != null)
		{
			return (Vector<SubscriptionType>)obj;
		}
		else
			return null;
	}
}
