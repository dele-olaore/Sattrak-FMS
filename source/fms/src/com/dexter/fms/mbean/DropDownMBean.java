package com.dexter.fms.mbean;

import java.io.Serializable;
import java.util.Calendar;
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
import com.dexter.fms.model.PartnerApprover;
import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.app.ExpenseType;
import com.dexter.fms.model.app.Fleet;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleParameters;
import com.dexter.fms.model.app.VehicleStatusEnum;
import com.dexter.fms.model.ref.Department;
import com.dexter.fms.model.ref.Division;
import com.dexter.fms.model.ref.DocumentType;
import com.dexter.fms.model.ref.DriverGrade;
import com.dexter.fms.model.ref.EngineCapacity;
import com.dexter.fms.model.ref.LGA;
import com.dexter.fms.model.ref.LicenseType;
import com.dexter.fms.model.ref.Region;
import com.dexter.fms.model.ref.State;
import com.dexter.fms.model.ref.Subsidiary;
import com.dexter.fms.model.ref.TransactionType;
import com.dexter.fms.model.ref.Unit;
import com.dexter.fms.model.ref.VehicleModel;
import com.dexter.fms.model.ref.VehicleWarning;

@ManagedBean(name = "ddBean")
@SessionScoped
public class DropDownMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-DropDownMBean");
	
	@SuppressWarnings("unused")
	private FacesMessage msg = null;
	
	public DropDownMBean() {}
	
	@SuppressWarnings("unchecked")
	public Vector<DocumentType> getDocumentTypes()
	{
		//TODO This should be per partner
		GeneralDAO gDAO = new GeneralDAO();
		
		Object obj = gDAO.findAll("DocumentType");
		gDAO.destroy();
		if(obj != null) {
			return (Vector<DocumentType>)obj;
		}
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<PartnerDriver> getPartnerDrivers(Long partner_id) {
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
	public Vector<VehicleWarning> getVehicleWarnings() {
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
	public Vector<PartnerUser> getUsersWithFunction(Long partner_id, String page_url) {
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
	public Vector<TransactionType> getTransactionTypes() {
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
	public Vector<LicenseType> getLicenseTypes() {
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
	public Vector<ExpenseType> getExpenseTypes(long partner_id) {
		Vector<ExpenseType> expTypes = null;
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("partner.id", partner_id);
		params.put("systemObj", false);
		
		Object drvs = gDAO.search("ExpenseType", params);
		if(drvs != null) {
			expTypes = (Vector<ExpenseType>)drvs;
		}
		
		params = new Hashtable<String, Object>();
		params.put("systemObj", true);
		drvs = gDAO.search("ExpenseType", params);
		if(drvs != null) {
			if(expTypes == null)
				expTypes = new Vector<ExpenseType>();
			Vector<ExpenseType> drvsList = (Vector<ExpenseType>)drvs;
			for(ExpenseType e : drvsList) {
				expTypes.add(e);
			}
		}
		gDAO.destroy();
		
		return expTypes;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Fleet> getFleets(long partner_id) {
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("partner.id", partner_id);
		Object resultObj = gDAO.search("Fleet", params);
		gDAO.destroy();
		if(resultObj != null)
			return (Vector<Fleet>)resultObj;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Vehicle> getVehicles(Long partner_id, Long fleet_id, String regNo) {
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
	public Vector<Vehicle> getActiveVehicles(Long partner_id, Long fleet_id, String regNo) {
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
	public Vector<Partner> getPartnersWithoutSattrak() {
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
	public Vector<DriverGrade> getDriverGrades() {
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
	public Vector<Partner> getPartners() {
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
	public Vector<ApplicationType> getAllAppTypes() {
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
	
	@SuppressWarnings("unchecked")
	public Vector<State> getStates() {
		GeneralDAO gDAO = new GeneralDAO();
		Object obj = gDAO.findAll("State");
		gDAO.destroy();
		if(obj != null) {
			return (Vector<State>)obj;
		}
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<LGA> getStateLGAs(long state_id) {
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("state.id", state_id);
		
		Object obj = gDAO.search("LGA", params);
		gDAO.destroy();
		if(obj != null)
			return (Vector<LGA>)obj;
		else
			return null;
	}
	
	public Vector<String> getYearOfPurchase() {
		Vector<String> list = new Vector<String>();
		
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		for(int i=0; i<10; i++) {
			list.add(""+(year-i));
		}
		
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<EngineCapacity> getEngineCapacities() {
		GeneralDAO gDAO = new GeneralDAO();
		
		Object obj = gDAO.findAll("EngineCapacity");
		gDAO.destroy();
		if(obj != null)
			return (Vector<EngineCapacity>)obj;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<VehicleModel> getPartnerVehicleBrands(long partner_id) {
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("partner.id", partner_id);
		
		Object obj = gDAO.search("VehicleModel", params);
		gDAO.destroy();
		if(obj != null)
			return (Vector<VehicleModel>)obj;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Region> getPartnerRegions(long partner_id) {
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("partner.id", partner_id);
		
		Object obj = gDAO.search("Region", params);
		gDAO.destroy();
		if(obj != null)
			return (Vector<Region>)obj;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Division> getPartnerDivisions(long partner_id, long state_id, long lga_id) {
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("partner.id", partner_id);
		if(state_id > 0)
			params.put("state.id", state_id);
		if(lga_id > 0)
			params.put("lga.id", lga_id);
		
		Object obj = gDAO.search("Division", params);
		gDAO.destroy();
		if(obj != null)
			return (Vector<Division>)obj;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Subsidiary> getPartnerSubsidiaries(long partner_id, long state_id, long lga_id) {
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("partner.id", partner_id);
		if(state_id > 0)
			params.put("state.id", state_id);
		if(lga_id > 0)
			params.put("lga.id", lga_id);
		
		Object obj = gDAO.search("Subsidiary", params);
		gDAO.destroy();
		if(obj != null)
			return (Vector<Subsidiary>)obj;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Department> getDivisionDepartments(long partner_id, long division_id) {
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("partner.id", partner_id);
		if(division_id > 0)
			params.put("division.id", division_id);
		
		Object obj = gDAO.search("Department", params);
		gDAO.destroy();
		if(obj != null)
			return (Vector<Department>)obj;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Department> getSubsidiaryDepartments(long partner_id, long subsidiary_id) {
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("partner.id", partner_id);
		if(subsidiary_id > 0)
			params.put("subsidiary.id", subsidiary_id);
		
		Object obj = gDAO.search("Department", params);
		gDAO.destroy();
		if(obj != null)
			return (Vector<Department>)obj;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Unit> getDepartmentUnits(long partner_id, long department_id) {
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("partner.id", partner_id);
		if(department_id > 0)
			params.put("department.id", department_id);
		
		Object obj = gDAO.search("Unit", params);
		gDAO.destroy();
		if(obj != null)
			return (Vector<Unit>)obj;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<PartnerPersonel> getPartnerPersonels(long partner_id, long division_id, long subsidiary_id, long department_id,
			long unit_id) {
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("partner.id", partner_id);
		if(division_id > 0)
			params.put("department.division.id", division_id);
		if(subsidiary_id > 0)
			params.put("department.subsidiary.id", subsidiary_id);
		if(department_id > 0)
			params.put("department.id", department_id);
		if(unit_id > 0)
			params.put("unit.id", unit_id);
		
		Object obj = gDAO.search("PartnerPersonel", params);
		gDAO.destroy();
		if(obj != null)
			return (Vector<PartnerPersonel>)obj;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Vehicle> getPartnerVehicles(long partner_id, long region_id, long division_id, long subsidiary_id, long department_id, long unit_id, long fleet_id) {
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("vehicle.partner.id", partner_id);
		if(region_id > 0)
			params.put("region.id", region_id);
		if(division_id > 0)
			params.put("dept.division.id", division_id);
		if(subsidiary_id > 0)
			params.put("dept.subsidiary.id", subsidiary_id);
		if(department_id > 0)
			params.put("dept.id", department_id);
		if(unit_id > 0)
			params.put("unit.id", unit_id);
		if(fleet_id > 0)
			params.put("vehicle.fleet.id", fleet_id);
		
		Object obj = gDAO.search("VehicleParameters", params);
		if(obj != null) {
			Vector<VehicleParameters> vpList = (Vector<VehicleParameters>)obj;
			Vector<Vehicle> vList = new Vector<Vehicle>();
			for(VehicleParameters vp : vpList) {
				boolean exists = false;
				for(Vehicle v : vList) {
					if(v.getId().longValue() == vp.getVehicle().getId().longValue()) {
						exists = true;
						break;
					}
				}
				if(!exists)
					vList.add(vp.getVehicle());
			}
			gDAO.destroy();
			return vList;
		}
		else {
			gDAO.destroy();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Vector<PartnerPersonel> getApprovingUsers(long partner_id, double amount, String expenseType) {
		GeneralDAO gDAO = new GeneralDAO();
		Vector<PartnerPersonel> ppList = new Vector<PartnerPersonel>();
		Long expType_id = null;
		try {
			expType_id = Long.parseLong(expenseType);
		} catch(Exception ex) {}
		if(expType_id != null) {
			ppList = getApprovingUsers(partner_id, amount, expType_id);
		} else {
			try {
				Query q = gDAO.createQuery("Select e from PartnerApprover e where e.partner.id=:partner_id and e.expenseType.name=:expenseType");
				q.setParameter("partner_id", partner_id);
				q.setParameter("expenseType", expenseType);
				Object obj = gDAO.search(q, 0);
				if(obj != null) {
					Vector<PartnerApprover> partnerApprovers = (Vector<PartnerApprover>)obj;
					for(PartnerApprover pa : partnerApprovers) {
						if(pa.getAmountLimit() == 0 || pa.getAmountLimit() > amount) {
							ppList.add(pa.getPersonel());
						}
					}
				}
			} catch(Exception ex){ ex.printStackTrace(); }
			gDAO.destroy();
		}
		return ppList;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<PartnerPersonel> getApprovingUsers(long partner_id, double amount, long expType_id) {
		GeneralDAO gDAO = new GeneralDAO();
		Vector<PartnerPersonel> ppList = new Vector<PartnerPersonel>();
		try {
			Query q = gDAO.createQuery("Select e from PartnerApprover e where e.partner.id=:partner_id and e.expenseType.id=:expType_id");
			q.setParameter("partner_id", partner_id);
			q.setParameter("expType_id", expType_id);
			Object obj = gDAO.search(q, 0);
			if(obj != null) {
				Vector<PartnerApprover> partnerApprovers = (Vector<PartnerApprover>)obj;
				for(PartnerApprover pa : partnerApprovers) {
					if(pa.getAmountLimit() == 0 || pa.getAmountLimit() > amount) {
						ppList.add(pa.getPersonel());
					}
				}
			}
		} catch(Exception ex){ ex.printStackTrace(); }
		gDAO.destroy();
		return ppList;
	}
}
