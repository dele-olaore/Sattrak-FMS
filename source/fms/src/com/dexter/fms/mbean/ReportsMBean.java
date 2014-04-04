package com.dexter.fms.mbean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.Query;
import javax.servlet.ServletContext;

import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerSetting;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.app.CorporateTrip;
import com.dexter.fms.model.app.DriverLicense;
import com.dexter.fms.model.app.Expense;
import com.dexter.fms.model.app.Fleet;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleAccident;
import com.dexter.fms.model.app.VehicleAdHocMaintenance;
import com.dexter.fms.model.app.VehicleFueling;
import com.dexter.fms.model.app.VehicleLicense;
import com.dexter.fms.model.app.VehicleRoutineMaintenance;
import com.dexter.fms.model.ref.Department;
import com.dexter.fms.model.ref.Region;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

@ManagedBean(name = "reportsBean")
@SessionScoped
public class ReportsMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-UserMBean");
	
	private FacesMessage msg = null;
	
	private Long partner_id;
	private Partner partner;
	
	private Long role_id;
	private Long region_id;
	private Long department_id;
	private Vector<Department> depts;
	private Vector<Region> regions;
	
	private Date start_dt, end_dt;
	private Long fleet_id;
	private Vector<Fleet> fleets;
	private Long vehicle_id;
	private String regNo;
	
	private Vector<PartnerUser> allUsers;
	private Vector<DriverLicense> dueDriversLic;
	private Vector<VehicleAccident> vehicleAccidents;
	private Vector<Vehicle> vehiclesByBrand;
	private Vector<VehicleRoutineMaintenance> rmaints;
	private Vector<VehicleAdHocMaintenance> adhocmaints;
	private Vector<VehicleAccident> activeAccidents;
	private Vector<Vehicle> accidentedVehicles;
	private Vector<VehicleLicense> dueVehicleLicenses;
	private Vector<Vehicle> vehiclesAges;
	
	private Vector<CorporateTrip> corTrips;
	private Vector<Expense> expenses;
	
	private Vector<VehicleFueling> fuelings;
	
	private String report_title;
	private String report_start_dt;
	private String report_end_dt;
	private String report_page = "/faces/reports_home.xhtml";
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public ReportsMBean()
	{}
	
	@SuppressWarnings("unchecked")
	public void search()
	{
		resetReportInfo();
		
		if(getPartner() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getPartner());
			
			if(getRegion_id() != null)
			{
				Object regionObj = gDAO.find(Region.class, getRegion_id());
				if(regionObj != null)
				{
					params.put("personel.region", (Region)regionObj);
				}
			}
			if(getDepartment_id() != null)
			{
				Object deptObj = gDAO.find(Department.class, getDepartment_id());
				if(deptObj != null)
				{
					params.put("personel.department", (Department)deptObj);
				}
			}
			
			Object dpsObj = gDAO.search("PartnerUser", params);
			if(dpsObj != null)
			{
				allUsers = (Vector<PartnerUser>)dpsObj;
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", allUsers.size() + " user(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("All User Report");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No user found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchDueDrvLicense()
	{
		resetReportInfo();
		
		if(getPartner() != null)
		{
			setDueDriversLic(new Vector<DriverLicense>());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Calendar c = Calendar.getInstance();
			Calendar c2 = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, -30);
			
			Query q = gDAO.createQuery("Select e from DriverLicense e where e.driver.partner=:partner and e.expired=:expired and e.active=:active and (e.lic_end_dt > :start_dt and e.lic_end_dt < :end_date)");
			q.setParameter("partner", getPartner());
			q.setParameter("expired", true);
			q.setParameter("active", true);
			q.setParameter("start_dt", c.getTime());
			q.setParameter("end_date", c2.getTime());
			
			Object licsObj = gDAO.search(q, 0);
			if(licsObj != null)
			{
				Vector<DriverLicense> lics = (Vector<DriverLicense>)licsObj;
				getDueDriversLic().addAll(lics);
			}
			
			c = Calendar.getInstance();
			c2 = Calendar.getInstance();
			c2.add(Calendar.DAY_OF_MONTH, 30);
			
			q = gDAO.createQuery("Select e from DriverLicense e where e.driver.partner=:partner and e.expired=:expired and e.active=:active and (e.lic_end_dt > :start_dt and e.lic_end_dt < :end_date)");
			q.setParameter("partner", getPartner());
			q.setParameter("expired", false);
			q.setParameter("active", true);
			q.setParameter("start_dt", c.getTime());
			q.setParameter("end_date", c2.getTime());
			
			licsObj = gDAO.search(q, 0);
			if(licsObj != null)
			{
				Vector<DriverLicense> lics = (Vector<DriverLicense>)licsObj;
				getDueDriversLic().addAll(lics);
			}
			
			if(getDueDriversLic().size() > 0)
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getDueDriversLic().size() + " recent/up-coming expired driver's license(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Driver License Due");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No recent/up-coming expired driver's license found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void searchAccidents()
	{
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null)
		{
			setVehicleAccidents(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from VehicleAccident e where (e.accident_dt between :start_dt and :end_dt) and e.vehicle.partner=:partner";
			Vehicle v = null;
			if(getVehicle_id() != null && getVehicle_id() > 0)
			{
				try
				{
					v = (Vehicle)gDAO.find(Vehicle.class, getVehicle_id());
				}
				catch(Exception ex){}
			}
			if(getVehicle_id() != null && getVehicle_id() > 0)
				str += " and e.vehicle = :vehicle";
			
			Query q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			if(getVehicle_id() != null && getVehicle_id() > 0)
				q.setParameter("vehicle", v);
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setVehicleAccidents((Vector<VehicleAccident>)drvs);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getVehicleAccidents().size() + " accident(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Accident Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No accident found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchVBrands()
	{
		resetReportInfo();
		
		if(getPartner() != null)
		{
			setVehiclesByBrand(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getPartner());
			params.put("active", true);
			
			if(getFleet_id() != null)
			{
				Object fleetObj = gDAO.find(Fleet.class, getFleet_id());
				if(fleetObj != null)
				{
					params.put("fleet", (Fleet)fleetObj);
				}
			}
			
			Object vehicles = gDAO.search("Vehicle", params);
			if(vehicles != null)
			{
				setVehiclesByBrand((Vector<Vehicle>)vehicles);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getVehiclesByBrand().size() + " vehicle(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Vehicle Brands Report");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No vehicles found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void searchRMaints()
	{
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null)
		{
			setRmaints(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from VehicleRoutineMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.partner=:partner";
			
			Fleet f = null;
			if(getFleet_id() != null && getFleet_id() > 0)
			{
				try
				{
					f = (Fleet)gDAO.find(Fleet.class, getFleet_id());
				}
				catch(Exception ex){}
			}
			if(getFleet_id() != null && getFleet_id() > 0)
				str += " and e.vehicle.fleet = :fleet";
			
			Vehicle v = null;
			if(getVehicle_id() != null && getVehicle_id() > 0)
			{
				try
				{
					v = (Vehicle)gDAO.find(Vehicle.class, getVehicle_id());
				}
				catch(Exception ex){}
			}
			if(getVehicle_id() != null && getVehicle_id() > 0)
				str += " and e.vehicle = :vehicle";
			
			Query q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			if(getFleet_id() != null && getFleet_id() > 0)
				q.setParameter("fleet", f);
			if(getVehicle_id() != null && getVehicle_id() > 0)
				q.setParameter("vehicle", v);
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setRmaints((Vector<VehicleRoutineMaintenance>)drvs);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getRmaints().size() + " routine maintenance(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Routine Maintenance Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No routine maintenance found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void searchAdHocMaints()
	{
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null)
		{
			setAdhocmaints(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from VehicleAdHocMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.partner=:partner";
			
			Fleet f = null;
			if(getFleet_id() != null && getFleet_id() > 0)
			{
				try
				{
					f = (Fleet)gDAO.find(Fleet.class, getFleet_id());
				}
				catch(Exception ex){}
			}
			if(getFleet_id() != null && getFleet_id() > 0)
				str += " and e.vehicle.fleet = :fleet";
			
			Vehicle v = null;
			if(getVehicle_id() != null && getVehicle_id() > 0)
			{
				try
				{
					v = (Vehicle)gDAO.find(Vehicle.class, getVehicle_id());
				}
				catch(Exception ex){}
			}
			if(getVehicle_id() != null && getVehicle_id() > 0)
				str += " and e.vehicle = :vehicle";
			
			Query q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			if(getFleet_id() != null && getFleet_id() > 0)
				q.setParameter("fleet", f);
			if(getVehicle_id() != null && getVehicle_id() > 0)
				q.setParameter("vehicle", v);
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setAdhocmaints((Vector<VehicleAdHocMaintenance>)drvs);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getAdhocmaints().size() + " ad-hoc maintenance(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Adhoc Maintenance Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No ad-hoc maintenance found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchAcciVehicles()
	{
		resetReportInfo();
		
		if(getPartner() != null)
		{
			setActiveAccidents(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("vehicle.partner", getPartner());
			params.put("active", true);
			if(getFleet_id() != null)
			{
				Object fleetObj = gDAO.find(Fleet.class, getFleet_id());
				if(fleetObj != null)
				{
					params.put("vehicle.fleet", (Fleet)fleetObj);
				}
			}
			
			Object vehiclesAccidents = gDAO.search("VehicleAccident", params);
			if(vehiclesAccidents != null)
			{
				setActiveAccidents((Vector<VehicleAccident>)vehiclesAccidents);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getActiveAccidents().size() + " vehicle accident(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Current Active Vehicle Accidents Report");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No vehicle accident found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchVehicleAges()
	{
		resetReportInfo();
		
		if(getPartner() != null)
		{
			setActiveAccidents(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getPartner());
			params.put("active", true);
			if(getFleet_id() != null)
			{
				Object fleetObj = gDAO.find(Fleet.class, getFleet_id());
				if(fleetObj != null)
				{
					params.put("fleet", (Fleet)fleetObj);
				}
			}
			
			Object vehicles = gDAO.search("Vehicle", params);
			if(vehicles != null)
			{
				setVehiclesAges((Vector<Vehicle>)vehicles);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getVehiclesAges().size() + " vehicle(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Ages of Vehicles Report");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No vehicle found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchLicDue()
	{
		resetReportInfo();
		
		if(getPartner() != null)
		{
			setDueVehicleLicenses(new Vector<VehicleLicense>());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Calendar c = Calendar.getInstance();
			Calendar c2 = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, -30);
			
			Query q = gDAO.createQuery("Select e from VehicleLicense e where e.vehicle.partner=:partner and e.expired=:expired and e.active=:active and (e.lic_end_dt > :start_dt and e.lic_end_dt < :end_date)");
			q.setParameter("partner", getPartner());
			q.setParameter("expired", true);
			q.setParameter("active", true);
			q.setParameter("start_dt", c.getTime());
			q.setParameter("end_date", c2.getTime());
			
			Object licsObj = gDAO.search(q, 0);
			if(licsObj != null)
			{
				Vector<VehicleLicense> lics = (Vector<VehicleLicense>)licsObj;
				getDueVehicleLicenses().addAll(lics);
			}
			
			c = Calendar.getInstance();
			c2 = Calendar.getInstance();
			c2.add(Calendar.DAY_OF_MONTH, 30);
			
			q = gDAO.createQuery("Select e from VehicleLicense e where e.vehicle.partner=:partner and e.expired=:expired and e.active=:active and (e.lic_end_dt > :start_dt and e.lic_end_dt < :end_date)");
			q.setParameter("partner", getPartner());
			q.setParameter("expired", false);
			q.setParameter("active", true);
			q.setParameter("start_dt", c.getTime());
			q.setParameter("end_date", c2.getTime());
			
			licsObj = gDAO.search(q, 0);
			if(licsObj != null)
			{
				Vector<VehicleLicense> lics = (Vector<VehicleLicense>)licsObj;
				getDueVehicleLicenses().addAll(lics);
			}
			
			if(getDueVehicleLicenses().size() > 0)
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getDueVehicleLicenses().size() + " recent/up-coming expired vehicle license(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Vehicle License Due");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No recent/up-coming expired vehicle license found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void searchCorTrips()
	{
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null)
		{
			setCorTrips(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from CorporateTrip e where (e.departureDateTime between :start_dt and :end_dt) and e.partner = :partner";
			
			Query q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setCorTrips((Vector<CorporateTrip>)drvs);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getCorTrips().size() + " trip(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Corporate Trip Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No trip found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void searchExpenses()
	{
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null)
		{
			setExpenses(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from Expense e where (e.expense_dt between :start_dt and :end_dt) and e.partner = :partner";
			
			Query q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setExpenses((Vector<Expense>)drvs);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getExpenses().size() + " expense(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Expenses Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No expense found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchFuelings()
	{
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null)
		{
			setFuelings(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from VehicleFueling e where (e.captured_dt between :start_dt and :end_dt) and e.partner = :partner";
			
			boolean fleet = false, veh = false;
			if(getFleet_id() != null)
			{
				str += " and e.vehicle.fleet.id = :fleet_id";
				fleet = true;
			}
			
			if(getVehicle_id() != null)
			{
				str += " and e.vehicle.id = :vehicle_id";
				veh = true;
			}
			
			Query q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			if(fleet)
				q.setParameter("fleet_id", getFleet_id());
			if(veh)
				q.setParameter("vehicle_id", getVehicle_id());
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setFuelings((Vector<VehicleFueling>)drvs);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getFuelings().size() + " fueling(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Fueling Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No fueling found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	private void resetReportInfo()
	{
		setReport_title(null);
		setReport_end_dt(null);
		setReport_start_dt(null);
	}
	
	public String gotoReportPage(String page, boolean subFunction)
	{
		setReport_page("/faces/"+page+".xhtml");
		if(!subFunction)
			dashBean.setFunction_page(page);
		return "reports";
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

	public Long getPartner_id() {
		return partner_id;
	}

	public void setPartner_id(Long partner_id) {
		this.partner_id = partner_id;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public Long getRole_id() {
		return role_id;
	}

	public void setRole_id(Long role_id) {
		this.role_id = role_id;
	}

	public Long getRegion_id() {
		return region_id;
	}

	public void setRegion_id(Long region_id) {
		this.region_id = region_id;
	}

	public Long getDepartment_id() {
		return department_id;
	}

	public void setDepartment_id(Long department_id) {
		this.department_id = department_id;
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

	public Date getStart_dt() {
		return start_dt;
	}

	public void setStart_dt(Date start_dt) {
		this.start_dt = start_dt;
	}

	public Date getEnd_dt() {
		return end_dt;
	}

	public void setEnd_dt(Date end_dt) {
		this.end_dt = end_dt;
	}

	public Long getFleet_id() {
		return fleet_id;
	}

	public void setFleet_id(Long fleet_id) {
		this.fleet_id = fleet_id;
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
				}
			}
		}
		return fleets;
	}
	
	public void createWord(int type, String filename)
	{
		try
		{
			FacesContext context = FacesContext.getCurrentInstance();
			XWPFDocument document = new XWPFDocument();
			
			XWPFParagraph paragraphOne = document.createParagraph();
	        paragraphOne.setAlignment(ParagraphAlignment.CENTER);
	        paragraphOne.setBorderBottom(Borders.SINGLE);
	        paragraphOne.setBorderTop(Borders.SINGLE);
	        paragraphOne.setBorderRight(Borders.SINGLE);
	        paragraphOne.setBorderLeft(Borders.SINGLE);
	        paragraphOne.setBorderBetween(Borders.SINGLE);
	        
	        XWPFRun paragraphOneRunOne = paragraphOne.createRun();
	        paragraphOneRunOne.setBold(true);
	        paragraphOneRunOne.setItalic(true);
	        paragraphOneRunOne.setText(getReport_title());
	        paragraphOneRunOne.addBreak();
	        
	        exportWordTable(type, document);
	        
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        document.write(baos);
	        
	        String fileName = filename + ".docx";
			
			writeFileToResponse(context.getExternalContext(), baos, fileName, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
			
			context.responseComplete();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void addHeaderCell(XWPFTableRow row, String text, boolean first)
	{
		XWPFParagraph tp = null;
		if(first)
			tp = row.getCell(0).getParagraphs().get(0);
		else
		{
			tp = row.addNewTableCell().getParagraphs().get(0);
		}
		XWPFRun tpr = tp.createRun();
		tpr.setBold(true);
		tpr.setItalic(true);
		tpr.setText(text);
	}
	
	private void exportWordTable(int type, XWPFDocument document)
	{
		XWPFTable table = document.createTable();
		switch(type)
		{
			case 1: // all users report
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "User name", true);
		        
				addHeaderCell(tableRowOne, "Full name", false);
				addHeaderCell(tableRowOne, "Email", false);
				addHeaderCell(tableRowOne, "Department", false);
				addHeaderCell(tableRowOne, "Region", false);
				addHeaderCell(tableRowOne, "Phone", false);
				addHeaderCell(tableRowOne, "Position", false);
				
				if(getAllUsers() != null)
				{
					for(PartnerUser e : getAllUsers())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getUsername());
						tableRow.getCell(1).setText(e.getPersonel().getFirstname() + " " + e.getPersonel().getLastname());
						tableRow.getCell(2).setText(e.getPersonel().getEmail());
						tableRow.getCell(3).setText((e.getPersonel().getDepartment()!=null) ? e.getPersonel().getDepartment().getName() : "N/A");
						tableRow.getCell(4).setText((e.getPersonel().getRegion()!=null) ? e.getPersonel().getRegion().getName() : "N/A");
						tableRow.getCell(5).setText(e.getPersonel().getPhone());
						tableRow.getCell(6).setText(e.getPersonel().getPosition());
					}
				}
				break;
			}
			case 2: // due driver's license
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Full Name", true);
		        
				addHeaderCell(tableRowOne, "Dept", false);
				addHeaderCell(tableRowOne, "Region", false);
				addHeaderCell(tableRowOne, "Phone", false);
				addHeaderCell(tableRowOne, "Position", false);
				addHeaderCell(tableRowOne, "License Due Date", false);
				
				if(getDueDriversLic() != null)
				{
					for(DriverLicense e : getDueDriversLic())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getDriver().getPersonel().getFirstname() + " " + e.getDriver().getPersonel().getLastname());
						tableRow.getCell(1).setText(e.getDriver().getPersonel().getDepartment()!=null ?e.getDriver().getPersonel().getDepartment().getName() : "N/A");
						tableRow.getCell(2).setText(e.getDriver().getPersonel().getRegion()!=null ?e.getDriver().getPersonel().getRegion().getName() : "N/A");
						tableRow.getCell(3).setText(e.getDriver().getPersonel().getPhone());
						tableRow.getCell(4).setText(e.getDriver().getPersonel().getPosition());
						tableRow.getCell(5).setText(e.getLic_end_dt().toLocaleString());
					}
				}
				break;
			}
			case 3: // accidents
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Reg No.", true);
		        
				addHeaderCell(tableRowOne, "Accident Date", false);
				addHeaderCell(tableRowOne, "Description", false);
				addHeaderCell(tableRowOne, "Driver", false);
				addHeaderCell(tableRowOne, "Driver Comment", false);
				addHeaderCell(tableRowOne, "Police Officer", false);
				addHeaderCell(tableRowOne, "Police Station", false);
				addHeaderCell(tableRowOne, "Police Comment", false);
				
				if(getVehicleAccidents() != null)
				{
					for(VehicleAccident e : getVehicleAccidents())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getVehicle().getRegistrationNo());
						tableRow.getCell(1).setText(e.getAccident_dt().toLocaleString());
						tableRow.getCell(2).setText(e.getAccidentDescription());
						tableRow.getCell(3).setText(e.getDriver_name());
						tableRow.getCell(4).setText(e.getDriverComment());
						tableRow.getCell(5).setText(e.getPoliceOfficer());
						tableRow.getCell(6).setText(e.getPoliceStation());
						tableRow.getCell(7).setText(e.getPoliceComment());
					}
				}
				break;
			}
			case 4: // vehicles by brand
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Make", true);
		        
				addHeaderCell(tableRowOne, "Model", false);
				addHeaderCell(tableRowOne, "Year", false);
				addHeaderCell(tableRowOne, "Reg No.", false);
				
				if(getVehiclesByBrand() != null)
				{
					for(Vehicle e : getVehiclesByBrand())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getModel().getMaker().getName());
						tableRow.getCell(1).setText(e.getModel().getName());
						tableRow.getCell(2).setText(e.getModel().getYear());
						tableRow.getCell(3).setText(e.getRegistrationNo());
					}
				}
				break;
			}
			case 5: // routine maintenance
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Reg No.", true);
		        
				addHeaderCell(tableRowOne, "Requester", false);
				addHeaderCell(tableRowOne, "Description", false);
				addHeaderCell(tableRowOne, "Start Date", false);
				addHeaderCell(tableRowOne, "Initial Cost", false);
				addHeaderCell(tableRowOne, "Status", false);
				addHeaderCell(tableRowOne, "Close Date", false);
				addHeaderCell(tableRowOne, "Final Cost", false);
				
				if(getRmaints() != null)
				{
					for(VehicleRoutineMaintenance e : getRmaints())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getVehicle().getRegistrationNo());
						tableRow.getCell(1).setText(e.getCreatedBy().getPersonel().getFirstname() + " " + e.getCreatedBy().getPersonel().getLastname());
						tableRow.getCell(2).setText(e.getDescription());
						tableRow.getCell(3).setText(e.getStart_dt().toLocaleString());
						tableRow.getCell(4).setText(e.getInitial_amount().toPlainString());
						tableRow.getCell(5).setText(e.getStatus());
						tableRow.getCell(6).setText(e.getClose_dt().toLocaleString());
						tableRow.getCell(7).setText(e.getClosed_amount().toPlainString());
					}
				}
				break;
			}
			case 6: // ad hoc maintenance
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Reg No.", true);
		        
				addHeaderCell(tableRowOne, "Requester", false);
				addHeaderCell(tableRowOne, "Description", false);
				addHeaderCell(tableRowOne, "Start Date", false);
				addHeaderCell(tableRowOne, "Initial Cost", false);
				addHeaderCell(tableRowOne, "Status", false);
				addHeaderCell(tableRowOne, "Close Date", false);
				addHeaderCell(tableRowOne, "Final Cost", false);
				
				if(getAdhocmaints() != null)
				{
					for(VehicleAdHocMaintenance e : getAdhocmaints())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getVehicle().getRegistrationNo());
						tableRow.getCell(1).setText(e.getCreatedBy().getPersonel().getFirstname() + " " + e.getCreatedBy().getPersonel().getLastname());
						tableRow.getCell(2).setText(e.getDescription());
						tableRow.getCell(3).setText(e.getStart_dt().toLocaleString());
						tableRow.getCell(4).setText(e.getInitial_cost().toPlainString());
						tableRow.getCell(5).setText(e.getStatus());
						tableRow.getCell(6).setText(e.getClose_dt().toLocaleString());
						tableRow.getCell(7).setText(e.getClosed_cost().toPlainString());
					}
				}
				break;
			}
			case 7: // active accidents
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Reg No.", true);
		        
				addHeaderCell(tableRowOne, "Accident Date", false);
				addHeaderCell(tableRowOne, "Description", false);
				addHeaderCell(tableRowOne, "Driver Name", false);
				addHeaderCell(tableRowOne, "Driver Comment", false);
				addHeaderCell(tableRowOne, "Police Name", false);
				addHeaderCell(tableRowOne, "Station Name", false);
				addHeaderCell(tableRowOne, "Police Comment", false);
				
				if(getActiveAccidents() != null)
				{
					for(VehicleAccident e : getActiveAccidents())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getVehicle().getRegistrationNo());
						tableRow.getCell(1).setText(e.getAccident_dt().toLocaleString());
						tableRow.getCell(2).setText(e.getAccidentDescription());
						tableRow.getCell(3).setText(e.getDriver_name());
						tableRow.getCell(4).setText(e.getDriverComment());
						tableRow.getCell(5).setText(e.getPoliceOfficer());
						tableRow.getCell(6).setText(e.getPoliceStation());
						tableRow.getCell(7).setText(e.getPoliceComment());
					}
				}
				break;
			}
			case 8: // coporate trips
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Requester", true);
		        
				addHeaderCell(tableRowOne, "Department", false);
				addHeaderCell(tableRowOne, "Reg No.", false);
				addHeaderCell(tableRowOne, "Departure Location", false);
				addHeaderCell(tableRowOne, "Arrival Location", false);
				addHeaderCell(tableRowOne, "Departure Date and Time", false);
				addHeaderCell(tableRowOne, "Est. Arrival Date and Time", false);
				addHeaderCell(tableRowOne, "Act. Arrival Date and Time", false);
				addHeaderCell(tableRowOne, "Status", false);
				
				if(getCorTrips() != null)
				{
					for(CorporateTrip e : getCorTrips())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getStaff().getFirstname() + " " + e.getStaff().getLastname());
						tableRow.getCell(1).setText((e.getStaff().getDepartment() != null) ? e.getStaff().getDepartment().getName() : "N/A");
						tableRow.getCell(2).setText(e.getVehicle()!=null ? e.getVehicle().getRegistrationNo() : "N/A");
						tableRow.getCell(3).setText(e.getDepartureLocation());
						tableRow.getCell(4).setText(e.getArrivalLocation());
						tableRow.getCell(5).setText(e.getDepartureDateTime().toLocaleString());
						tableRow.getCell(6).setText(e.getEstimatedArrivalDateTime().toLocaleString());
						tableRow.getCell(7).setText(e.getCompleteRequestDateTime()!=null ? e.getCompleteRequestDateTime().toLocaleString() : "N/A");
						tableRow.getCell(8).setText(e.getTripStatus());
					}
				}
				break;
			}
			case 9: // expense report
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Type", true);
		        
				addHeaderCell(tableRowOne, "Date", false);
				addHeaderCell(tableRowOne, "Amount", false);
				addHeaderCell(tableRowOne, "Detail", false);
				
				if(getExpenses() != null)
				{
					for(Expense e : getExpenses())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getType().getName());
						tableRow.getCell(0).setText(e.getExpense_dt().toLocaleString());
						tableRow.getCell(0).setText(""+e.getAmount());
						tableRow.getCell(0).setText(e.getRemarks());
					}
				}
				break;
			}
			case 10: // vehicle ages
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Make", true);
		        
				addHeaderCell(tableRowOne, "Model", false);
				addHeaderCell(tableRowOne, "Year", false);
				addHeaderCell(tableRowOne, "Reg No.", false);
				addHeaderCell(tableRowOne, "Date of Purchase", false);
				addHeaderCell(tableRowOne, "Age (in years)", false);
				
				if(getVehiclesAges() != null)
				{
					for(Vehicle e : getVehiclesAges())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getModel().getMaker() != null ? e.getModel().getMaker().getName() : "N/A");
						tableRow.getCell(1).setText(e.getModel() != null ? e.getModel().getName() : "N/A");
						tableRow.getCell(2).setText(e.getModel() != null ? e.getModel().getYear() : "N/A");
						tableRow.getCell(3).setText(e.getRegistrationNo());
						tableRow.getCell(4).setText(e.getPurchaseDate() != null ? e.getPurchaseDate().toLocaleString() : "N/A");
						tableRow.getCell(5).setText(""+e.getAge());
					}
				}
				break;
			}
			case 11: // vehicle fuelings
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Date", true);
		        
				addHeaderCell(tableRowOne, "Reg No.", false);
				addHeaderCell(tableRowOne, "Litres", false);
				addHeaderCell(tableRowOne, "Amount", false);
				addHeaderCell(tableRowOne, "Source", false);
				
				if(getFuelings() != null)
				{
					for(VehicleFueling e : getFuelings())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getCaptured_dt().toLocaleString());
						tableRow.getCell(1).setText(e.getVehicle().getRegistrationNo());
						tableRow.getCell(2).setText(""+e.getLitres());
						tableRow.getCell(3).setText(""+e.getAmt());
						tableRow.getCell(4).setText(e.getSource());
					}
				}
				break;
			}
			case 12: // due vehicle licenses
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Reg No.", true);
		        
				addHeaderCell(tableRowOne, "Fleet", false);
				addHeaderCell(tableRowOne, "Make", false);
				addHeaderCell(tableRowOne, "Model", false);
				addHeaderCell(tableRowOne, "License Due Date", false);
				
				if(getDueVehicleLicenses() != null)
				{
					for(VehicleLicense e : getDueVehicleLicenses())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getVehicle().getRegistrationNo());
						tableRow.getCell(1).setText(e.getVehicle().getFleet().getName());
						tableRow.getCell(2).setText(e.getVehicle().getModel()!=null ?e.getVehicle().getModel().getMaker().getName() : "N/A");
						tableRow.getCell(3).setText(e.getVehicle().getModel()!=null ?e.getVehicle().getModel().getName() : "N/A");
						tableRow.getCell(4).setText(e.getLic_end_dt().toLocaleString());
					}
				}
				break;
			}
		}
	}
	
	static class HeaderFooter extends PdfPageEventHelper
	{
		public void onEndPage(PdfWriter writer, Document document)
		{
			Rectangle rect = writer.getBoxSize("footer");
			BaseFont bf_times;
			try
			{
				bf_times = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1252", false);
				Font font = new Font(bf_times, 9);
				ColumnText.showTextAligned(writer.getDirectContent(),
					    Element.ALIGN_RIGHT, new Phrase(String.format("%d", writer.getPageNumber()), font),  rect.getRight(),
					    rect.getBottom() - 18, 0);
				ColumnText.showTextAligned(writer.getDirectContent(),
						Element.ALIGN_LEFT, new Phrase("Copyright " + Calendar.getInstance().get(Calendar.YEAR), font), 
						rect.getLeft(), rect.getBottom() - 18, 0);
			}
			catch (DocumentException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void createPDF(int type, String filename, int pageType)
	{
		try
		{
			FacesContext context = FacesContext.getCurrentInstance();
			Document document = new Document();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter writer = PdfWriter.getInstance(document, baos);
			writer.setPageEvent(new HeaderFooter());
			writer.setBoxSize("footer", new Rectangle(36, 54, 559, 788));
			if (!document.isOpen())
			{
				document.open();
			}
			
			switch(pageType)
			{
			case 1:
				document.setPageSize(PageSize.A4);
				break;
			case 2:
				document.setPageSize(PageSize.A4.rotate());
				break;
			}
			document.addAuthor("FMS");
			document.addCreationDate();
			document.addCreator("FMS");
			document.addSubject("Report");
			document.addTitle(getReport_title());
			
			PdfPTable headerTable = new PdfPTable(1);
			
			ServletContext servletContext = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
			String logo = servletContext.getRealPath("") + File.separator + "resources" + File.separator + "images" + File.separator + "satraklogo.jpg";
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", dashBean.getUser().getPartner());
			GeneralDAO gDAO = new GeneralDAO();
			Object pSettingsObj = gDAO.search("PartnerSetting", params);
			PartnerSetting setting = null;
			if(pSettingsObj != null)
			{
				Vector<PartnerSetting> pSettingsList = (Vector<PartnerSetting>)pSettingsObj;
				for(PartnerSetting e : pSettingsList)
				{
					setting = e;
				}
			}
			gDAO.destroy();
			
			PdfPCell c = null;
			if(setting != null && setting.getLogo() != null)
			{
				Image logoImg = Image.getInstance(setting.getLogo());
				logoImg.scaleToFit(212, 51);
				c = new PdfPCell(logoImg);
			}
			else
				c = new PdfPCell(Image.getInstance(logo));
			c.setBorder(0);
			c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			headerTable.addCell(c);
			
			Paragraph stars = new Paragraph(20);
			stars.add(Chunk.NEWLINE);
	        stars.setSpacingAfter(20);
			
			c = new PdfPCell(stars);
			c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			c.setBorder(0);
			headerTable.addCell(c);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{
				//font exception
			}
			Font font = new Font(helvetica, 16, Font.NORMAL|Font.BOLD);
			
			c = new PdfPCell(new Paragraph(getReport_title(), font));
			c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			c.setBorder(0);
			headerTable.addCell(c);
			
			if(getReport_start_dt() != null && getReport_end_dt() != null)
			{
				stars = new Paragraph(20);
				stars.add(Chunk.NEWLINE);
		        stars.setSpacingAfter(10);
				
				c = new PdfPCell(stars);
				c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				c.setBorder(0);
				headerTable.addCell(c);
				
				new Font(helvetica, 12, Font.NORMAL);
				Paragraph ch = new Paragraph("From " + getReport_start_dt() + " to " + getReport_end_dt(), font);
				c = new PdfPCell(ch);
				c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				headerTable.addCell(c);
			}
			stars = new Paragraph(20);
			stars.add(Chunk.NEWLINE);
	        stars.setSpacingAfter(20);
			
			c = new PdfPCell(stars);
			c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			c.setBorder(0);
			headerTable.addCell(c);
			document.add(headerTable);
			
			PdfPTable pdfTable = exportPDFTable(type);
			if(pdfTable != null)
				document.add(pdfTable);
			
			//Keep modifying your pdf file (add pages and more)
			
			document.close();
			String fileName = filename + ".pdf";
			
			writeFileToResponse(context.getExternalContext(), baos, fileName, "application/pdf");
			
			context.responseComplete();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	private PdfPTable exportPDFTable(int type)
	{
		int numberOfColumns = 0;
		PdfPTable pdfTable = null;
		switch(type)
		{
		case 1: // all users report
		{
			numberOfColumns = 7;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("User name", font));
			pdfTable.addCell(new Paragraph("Full name", font));
			pdfTable.addCell(new Paragraph("Email", font));
			pdfTable.addCell(new Paragraph("Department", font));
			pdfTable.addCell(new Paragraph("Region", font));
			pdfTable.addCell(new Paragraph("Phone", font));
			pdfTable.addCell(new Paragraph("Position", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getAllUsers() != null)
			{
				for(PartnerUser e : getAllUsers())
				{
					pdfTable.addCell(new Paragraph(e.getUsername(), font));
					pdfTable.addCell(new Paragraph(e.getPersonel().getFirstname() + " " + e.getPersonel().getLastname(), font));
					pdfTable.addCell(new Paragraph(e.getPersonel().getEmail(), font));
					pdfTable.addCell(new Paragraph((e.getPersonel().getDepartment()!=null) ? e.getPersonel().getDepartment().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph((e.getPersonel().getRegion()!=null) ? e.getPersonel().getRegion().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getPersonel().getPhone(), font));
					pdfTable.addCell(new Paragraph(e.getPersonel().getPosition(), font));
				}
			}
			break;
		}
		case 2: // due driver's license
		{
			numberOfColumns = 6;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Full Name", font));
			pdfTable.addCell(new Paragraph("Dept", font));
			pdfTable.addCell(new Paragraph("Region", font));
			pdfTable.addCell(new Paragraph("Phone", font));
			pdfTable.addCell(new Paragraph("Position", font));
			pdfTable.addCell(new Paragraph("License Due Date", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getDueDriversLic() != null)
			{
				for(DriverLicense e : getDueDriversLic())
				{
					pdfTable.addCell(new Paragraph(e.getDriver().getPersonel().getFirstname() + " " + e.getDriver().getPersonel().getLastname(), font));
					pdfTable.addCell(new Paragraph(e.getDriver().getPersonel().getDepartment()!=null ?e.getDriver().getPersonel().getDepartment().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getDriver().getPersonel().getRegion()!=null ?e.getDriver().getPersonel().getRegion().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getDriver().getPersonel().getPhone(), font));
					pdfTable.addCell(new Paragraph(e.getDriver().getPersonel().getPosition(), font));
					pdfTable.addCell(new Paragraph(e.getLic_end_dt().toLocaleString(), font));
				}
			}
			break;
		}
		case 3: // accidents
		{
			numberOfColumns = 8;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Accident Date", font));
			pdfTable.addCell(new Paragraph("Description", font));
			pdfTable.addCell(new Paragraph("Driver", font));
			pdfTable.addCell(new Paragraph("Driver Comment", font));
			pdfTable.addCell(new Paragraph("Police Officer", font));
			pdfTable.addCell(new Paragraph("Police Station", font));
			pdfTable.addCell(new Paragraph("Police Comment", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getVehicleAccidents() != null)
			{
				for(VehicleAccident e : getVehicleAccidents())
				{
					pdfTable.addCell(new Paragraph(e.getVehicle().getRegistrationNo(), font));
					pdfTable.addCell(new Paragraph(e.getAccident_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getAccidentDescription(), font));
					pdfTable.addCell(new Paragraph(e.getDriver_name(), font));
					pdfTable.addCell(new Paragraph(e.getDriverComment(), font));
					pdfTable.addCell(new Paragraph(e.getPoliceOfficer(), font));
					pdfTable.addCell(new Paragraph(e.getPoliceStation(), font));
					pdfTable.addCell(new Paragraph(e.getPoliceComment(), font));
				}
			}
			break;
		}
		case 4: // vehicles by brand
		{
			numberOfColumns = 4;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Make", font));
			pdfTable.addCell(new Paragraph("Model", font));
			pdfTable.addCell(new Paragraph("Year", font));
			pdfTable.addCell(new Paragraph("Reg No.", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getVehiclesByBrand() != null)
			{
				for(Vehicle e : getVehiclesByBrand())
				{
					pdfTable.addCell(new Paragraph(e.getModel().getMaker().getName(), font));
					pdfTable.addCell(new Paragraph(e.getModel().getName(), font));
					pdfTable.addCell(new Paragraph(e.getModel().getYear(), font));
					pdfTable.addCell(new Paragraph(e.getRegistrationNo(), font));
				}
			}
			break;
		}
		case 5: // routine maintenance
		{
			numberOfColumns = 8;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Requester", font));
			pdfTable.addCell(new Paragraph("Description", font));
			pdfTable.addCell(new Paragraph("Start Date", font));
			pdfTable.addCell(new Paragraph("Initial Cost", font));
			pdfTable.addCell(new Paragraph("Status", font));
			pdfTable.addCell(new Paragraph("Close Date", font));
			pdfTable.addCell(new Paragraph("Final Cost", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getRmaints() != null)
			{
				for(VehicleRoutineMaintenance e : getRmaints())
				{
					pdfTable.addCell(new Paragraph(e.getVehicle().getRegistrationNo(), font));
					pdfTable.addCell(new Paragraph(e.getCreatedBy().getPersonel().getFirstname() + " " + e.getCreatedBy().getPersonel().getLastname(), font));
					pdfTable.addCell(new Paragraph(e.getDescription(), font));
					pdfTable.addCell(new Paragraph(e.getStart_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getInitial_amount().toPlainString(), font));
					pdfTable.addCell(new Paragraph(e.getStatus(), font));
					pdfTable.addCell(new Paragraph(e.getClose_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getClosed_amount().toPlainString(), font));
				}
			}
			break;
		}
		case 6: // ad hoc maintenance
		{
			numberOfColumns = 8;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Requester", font));
			pdfTable.addCell(new Paragraph("Description", font));
			pdfTable.addCell(new Paragraph("Start Date", font));
			pdfTable.addCell(new Paragraph("Initial Cost", font));
			pdfTable.addCell(new Paragraph("Status", font));
			pdfTable.addCell(new Paragraph("Close Date", font));
			pdfTable.addCell(new Paragraph("Final Cost", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getAdhocmaints() != null)
			{
				for(VehicleAdHocMaintenance e : getAdhocmaints())
				{
					pdfTable.addCell(new Paragraph(e.getVehicle().getRegistrationNo(), font));
					pdfTable.addCell(new Paragraph(e.getCreatedBy().getPersonel().getFirstname() + " " + e.getCreatedBy().getPersonel().getLastname(), font));
					pdfTable.addCell(new Paragraph(e.getDescription(), font));
					pdfTable.addCell(new Paragraph(e.getStart_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getInitial_cost().toPlainString(), font));
					pdfTable.addCell(new Paragraph(e.getStatus(), font));
					pdfTable.addCell(new Paragraph(e.getClose_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getClosed_cost().toPlainString(), font));
				}
			}
			break;
		}
		case 7: // active accidents
		{
			numberOfColumns = 8;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Accident Date", font));
			pdfTable.addCell(new Paragraph("Description", font));
			pdfTable.addCell(new Paragraph("Driver Name", font));
			pdfTable.addCell(new Paragraph("Driver Comment", font));
			pdfTable.addCell(new Paragraph("Police Name", font));
			pdfTable.addCell(new Paragraph("Station Name", font));
			pdfTable.addCell(new Paragraph("Police Comment", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getActiveAccidents() != null)
			{
				for(VehicleAccident e : getActiveAccidents())
				{
					pdfTable.addCell(new Paragraph(e.getVehicle().getRegistrationNo(), font));
					pdfTable.addCell(new Paragraph(e.getAccident_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getAccidentDescription(), font));
					pdfTable.addCell(new Paragraph(e.getDriver_name(), font));
					pdfTable.addCell(new Paragraph(e.getDriverComment(), font));
					pdfTable.addCell(new Paragraph(e.getPoliceOfficer(), font));
					pdfTable.addCell(new Paragraph(e.getPoliceStation(), font));
					pdfTable.addCell(new Paragraph(e.getPoliceComment(), font));
				}
			}
			break;
		}
		case 8: // coporate trips
		{
			numberOfColumns = 9;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 6, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Requester", font));
			pdfTable.addCell(new Paragraph("Department", font));
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Departure Location", font));
			pdfTable.addCell(new Paragraph("Arrival Location", font));
			pdfTable.addCell(new Paragraph("Departure Date and Time", font));
			pdfTable.addCell(new Paragraph("Est. Arrival Date and Time", font));
			pdfTable.addCell(new Paragraph("Act. Arrival Date and Time", font));
			pdfTable.addCell(new Paragraph("Status", font));
			
			font = new Font(helvetica, 6, Font.NORMAL);
			
			if(getCorTrips() != null)
			{
				for(CorporateTrip e : getCorTrips())
				{
					pdfTable.addCell(new Paragraph(e.getStaff().getFirstname() + " " + e.getStaff().getLastname(), font));
					pdfTable.addCell(new Paragraph((e.getStaff().getDepartment() != null) ? e.getStaff().getDepartment().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getVehicle()!=null ? e.getVehicle().getRegistrationNo() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getDepartureLocation(), font));
					pdfTable.addCell(new Paragraph(e.getArrivalLocation(), font));
					pdfTable.addCell(new Paragraph(e.getDepartureDateTime().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getEstimatedArrivalDateTime().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getCompleteRequestDateTime()!=null ? e.getCompleteRequestDateTime().toLocaleString() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getTripStatus(), font));
				}
			}
			break;
		}
		case 9: // expense report
		{
			numberOfColumns = 4;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Type", font));
			pdfTable.addCell(new Paragraph("Date", font));
			pdfTable.addCell(new Paragraph("Amount", font));
			pdfTable.addCell(new Paragraph("Detail", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getExpenses() != null)
			{
				for(Expense e : getExpenses())
				{
					pdfTable.addCell(new Paragraph(e.getType().getName(), font));
					pdfTable.addCell(new Paragraph(e.getExpense_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(""+e.getAmount(), font));
					pdfTable.addCell(new Paragraph(e.getRemarks(), font));
				}
			}
			break;
		}
		case 10: // vehicle ages
		{
			numberOfColumns = 6;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Make", font));
			pdfTable.addCell(new Paragraph("Model", font));
			pdfTable.addCell(new Paragraph("Year", font));
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Date of Purchase", font));
			pdfTable.addCell(new Paragraph("Age (in years)", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getVehiclesAges() != null)
			{
				for(Vehicle e : getVehiclesAges())
				{
					pdfTable.addCell(new Paragraph(e.getModel().getMaker() != null ? e.getModel().getMaker().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getModel() != null ? e.getModel().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getModel() != null ? e.getModel().getYear() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getRegistrationNo(), font));
					pdfTable.addCell(new Paragraph(e.getPurchaseDate() != null ? e.getPurchaseDate().toLocaleString() : "N/A", font));
					pdfTable.addCell(new Paragraph(""+e.getAge(), font));
				}
			}
			break;
		}
		case 11: // vehicle fuelings
		{
			numberOfColumns = 5;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Date", font));
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Litres", font));
			pdfTable.addCell(new Paragraph("Amount", font));
			pdfTable.addCell(new Paragraph("Source", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getFuelings() != null)
			{
				for(VehicleFueling e : getFuelings())
				{
					pdfTable.addCell(new Paragraph(e.getCaptured_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getVehicle().getRegistrationNo(), font));
					pdfTable.addCell(new Paragraph(""+e.getLitres(), font));
					pdfTable.addCell(new Paragraph(""+e.getAmt(), font));
					pdfTable.addCell(new Paragraph(e.getSource(), font));
				}
			}
			break;
		}
		case 12: // due vehicle licenses
		{
			numberOfColumns = 5;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Fleet", font));
			pdfTable.addCell(new Paragraph("Make", font));
			pdfTable.addCell(new Paragraph("Model", font));
			pdfTable.addCell(new Paragraph("License Due Date", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getDueVehicleLicenses() != null)
			{
				for(VehicleLicense e : getDueVehicleLicenses())
				{
					pdfTable.addCell(new Paragraph(e.getVehicle().getRegistrationNo(), font));
					pdfTable.addCell(new Paragraph(e.getVehicle().getFleet().getName(), font));
					pdfTable.addCell(new Paragraph(e.getVehicle().getModel()!=null ?e.getVehicle().getModel().getMaker().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getVehicle().getModel()!=null ?e.getVehicle().getModel().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getLic_end_dt().toLocaleString(), font));
				}
			}
			break;
		}
		}
		
		pdfTable.setWidthPercentage(100);
		
		return pdfTable;
	}
	
	private void writeFileToResponse(ExternalContext externalContext, ByteArrayOutputStream baos, String fileName, String mimeType)
	{
		try
		{
			externalContext.responseReset();
			externalContext.setResponseContentType(mimeType);
			externalContext.setResponseHeader("Expires", "0");
			externalContext.setResponseHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
			externalContext.setResponseHeader("Pragma", "public");
			externalContext.setResponseHeader("Content-disposition", "attachment;filename=" + fileName);
			externalContext.setResponseContentLength(baos.size());
			OutputStream out = externalContext.getResponseOutputStream();
			baos.writeTo(out);
			externalContext.responseFlushBuffer();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setFleets(Vector<Fleet> fleets) {
		this.fleets = fleets;
	}

	public String getRegNo() {
		return regNo;
	}

	public void setRegNo(String regNo) {
		this.regNo = regNo;
	}

	public Long getVehicle_id() {
		return vehicle_id;
	}

	public void setVehicle_id(Long vehicle_id) {
		this.vehicle_id = vehicle_id;
	}

	public Vector<PartnerUser> getAllUsers() {
		return allUsers;
	}

	public void setAllUsers(Vector<PartnerUser> allUsers) {
		this.allUsers = allUsers;
	}

	public Vector<DriverLicense> getDueDriversLic() {
		return dueDriversLic;
	}

	public void setDueDriversLic(Vector<DriverLicense> dueDriversLic) {
		this.dueDriversLic = dueDriversLic;
	}

	public Vector<VehicleAccident> getVehicleAccidents() {
		return vehicleAccidents;
	}

	public void setVehicleAccidents(Vector<VehicleAccident> vehicleAccidents) {
		this.vehicleAccidents = vehicleAccidents;
	}

	public Vector<Vehicle> getVehiclesByBrand() {
		return vehiclesByBrand;
	}

	public void setVehiclesByBrand(Vector<Vehicle> vehiclesByBrand) {
		this.vehiclesByBrand = vehiclesByBrand;
	}

	public Vector<VehicleRoutineMaintenance> getRmaints() {
		return rmaints;
	}

	public void setRmaints(Vector<VehicleRoutineMaintenance> rmaints) {
		this.rmaints = rmaints;
	}

	public Vector<VehicleAdHocMaintenance> getAdhocmaints() {
		return adhocmaints;
	}

	public void setAdhocmaints(Vector<VehicleAdHocMaintenance> adhocmaints) {
		this.adhocmaints = adhocmaints;
	}

	public Vector<VehicleAccident> getActiveAccidents() {
		return activeAccidents;
	}

	public void setActiveAccidents(Vector<VehicleAccident> activeAccidents) {
		this.activeAccidents = activeAccidents;
	}

	public Vector<Vehicle> getAccidentedVehicles() {
		return accidentedVehicles;
	}

	public void setAccidentedVehicles(Vector<Vehicle> accidentedVehicles) {
		this.accidentedVehicles = accidentedVehicles;
	}

	public Vector<VehicleLicense> getDueVehicleLicenses() {
		return dueVehicleLicenses;
	}

	public void setDueVehicleLicenses(Vector<VehicleLicense> dueVehicleLicenses) {
		this.dueVehicleLicenses = dueVehicleLicenses;
	}

	public Vector<Vehicle> getVehiclesAges() {
		return vehiclesAges;
	}

	public void setVehiclesAges(Vector<Vehicle> vehiclesAges) {
		this.vehiclesAges = vehiclesAges;
	}

	public Vector<CorporateTrip> getCorTrips() {
		return corTrips;
	}

	public void setCorTrips(Vector<CorporateTrip> corTrips) {
		this.corTrips = corTrips;
	}

	public Vector<Expense> getExpenses() {
		return expenses;
	}

	public void setExpenses(Vector<Expense> expenses) {
		this.expenses = expenses;
	}

	public Vector<VehicleFueling> getFuelings() {
		return fuelings;
	}

	public void setFuelings(Vector<VehicleFueling> fuelings) {
		this.fuelings = fuelings;
	}

	public String getReport_title() {
		return report_title;
	}

	public void setReport_title(String report_title) {
		this.report_title = report_title;
	}

	public String getReport_start_dt() {
		return report_start_dt;
	}

	public void setReport_start_dt(String report_start_dt) {
		this.report_start_dt = report_start_dt;
	}

	public String getReport_end_dt() {
		return report_end_dt;
	}

	public void setReport_end_dt(String report_end_dt) {
		this.report_end_dt = report_end_dt;
	}

	public String getReport_page() {
		return report_page;
	}

	public void setReport_page(String report_page) {
		this.report_page = report_page;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
