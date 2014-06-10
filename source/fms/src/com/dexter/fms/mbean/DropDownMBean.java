package com.dexter.fms.mbean;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.persistence.Query;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.ApplicationType;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.app.Fleet;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleStatusEnum;
import com.dexter.fms.model.ref.DocumentType;
import com.dexter.fms.model.ref.DriverGrade;
import com.dexter.fms.model.ref.LicenseType;
import com.dexter.fms.model.ref.TransactionType;
import com.dexter.fms.model.ref.VehicleWarning;

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
	public Vector<DocumentType> getDocumentTypes()
	{
		//TODO This should be per partner
		GeneralDAO gDAO = new GeneralDAO();
		
		Object obj = gDAO.findAll("DocumentType");
		gDAO.destroy();
		if(obj != null)
		{
			return (Vector<DocumentType>)obj;
		}
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<PartnerDriver> getPartnerDrivers(Long partner_id)
	{
		//TODO This should be per partner
		GeneralDAO gDAO = new GeneralDAO();
		String qstr = "Select e from PartnerDriver e where e.partner.id=:partner_id";
		Query q = gDAO.createQuery(qstr);
		q.setParameter("partner_id", partner_id);
		
		Object retObj = gDAO.search(q, 0);
		gDAO.destroy();
		if(retObj != null)
			return (Vector<PartnerDriver>)retObj;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<VehicleWarning> getVehicleWarnings()
	{
		//TODO This should be per partner
		GeneralDAO gDAO = new GeneralDAO();
		
		Object obj = gDAO.findAll("VehicleWarning");
		gDAO.destroy();
		if(obj != null)
		{
			return (Vector<VehicleWarning>)obj;
		}
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<PartnerUser> getUsersWithFunction(Long partner_id, String page_url)
	{
		//TODO This should be per partner
		GeneralDAO gDAO = new GeneralDAO();
		String qstr = "Select e from PartnerUser e where e.id in (Select pur.user.id from PartnerUserRole pur where pur.role.id in (Select mr.role.id from MRoleFunction mr where mr.function.page_url=:page_url)) and e.partner.id=:partner_id";
		Query q = gDAO.createQuery(qstr);
		q.setParameter("page_url", page_url);
		q.setParameter("partner_id", partner_id);
		
		Object retObj = gDAO.search(q, 0);
		gDAO.destroy();
		if(retObj != null)
			return (Vector<PartnerUser>)retObj;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<TransactionType> getTransactionTypes()
	{
		//TODO This should be per partner
		GeneralDAO gDAO = new GeneralDAO();
		
		Object obj = gDAO.findAll("TransactionType");
		gDAO.destroy();
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
		gDAO.destroy();
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
		gDAO.destroy();
		if(resultObj != null)
			return (Vector<Vehicle>)resultObj;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Vehicle> getActiveVehicles(Long partner_id, Long fleet_id, String regNo)
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
		
		params.put("activeStatus", VehicleStatusEnum.ACTIVE.getStatus());
		
		Object resultObj = gDAO.search("Vehicle", params);
		gDAO.destroy();
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
		gDAO.destroy();
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
		gDAO.destroy();
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
		gDAO.destroy();
		if(obj != null)
		{
			return (Vector<Partner>)obj;
		}
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<ApplicationType> getAllAppTypes()
	{
		GeneralDAO gDAO = new GeneralDAO();
		Object obj = gDAO.findAll("ApplicationType");
		gDAO.destroy();
		if(obj != null)
		{
			return (Vector<ApplicationType>)obj;
		}
		else
			return null;
	}
}
