package com.dexter.fms.mbean;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.Query;

import org.primefaces.context.RequestContext;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;
//import org.primefaces.model.chart.Axis;
//import org.primefaces.model.chart.AxisType;
//import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
/*import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import org.primefaces.model.menu.Submenu;*/

import com.dexter.common.util.Hasher;
import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.dao.ReportDAO;
import com.dexter.fms.model.Audit;
import com.dexter.fms.model.MDashRole;
import com.dexter.fms.model.MFunction;
import com.dexter.fms.model.MRoleFunction;
import com.dexter.fms.model.MRoleReport;
import com.dexter.fms.model.Module;
import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerLicense;
import com.dexter.fms.model.PartnerSetting;
import com.dexter.fms.model.PartnerSubscription;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.PartnerUserRole;
import com.dexter.fms.model.PartnerUserSetting;
import com.dexter.fms.model.Report;
import com.dexter.fms.model.app.Approver;
import com.dexter.fms.model.app.Budget;
import com.dexter.fms.model.app.CorporateTrip;
import com.dexter.fms.model.app.DashboardVehicle;
import com.dexter.fms.model.app.DriverLicense;
import com.dexter.fms.model.app.Expense;
import com.dexter.fms.model.app.ExpenseType;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleAdHocMaintenanceRequest;
import com.dexter.fms.model.app.VehicleDriver;
import com.dexter.fms.model.app.VehicleLicense;
import com.dexter.fms.model.app.VehicleLocationData;
import com.dexter.fms.model.app.VehicleMaintenanceRequest;
import com.dexter.fms.model.app.VehicleRoutineMaintenanceSetup;
import com.dexter.fms.model.app.VehicleTrackerData;
import com.dexter.fms.model.app.WorkOrder;

@ManagedBean(name = "dashboardBean")
@SessionScoped
public class DashboardMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-DashboardMBean");
	
	private FacesMessage msg = null;
	
	private PartnerSubscription subscription;
	private PartnerLicense partnerLicense;
	private PartnerUser user;
	private PartnerDriver driver;
	private Vector<PartnerUserRole> userRoles;
	private Vector<MRoleFunction> rolesFunctions;
	private Vector<MRoleReport> rolesReports;
	private Vector<MDashRole> rolesDashs;
	
	private Hashtable<String, Vector<MFunction>> userModulesFunctions;
	
	private String[] reportsModules;
	private Hashtable<String, Vector<Report>> moduleReports;
	
	private String oldpassword;
	private String newpassword;
	private String cpassword;
	
	private String function_page = "dashboard";
	
	private String theme;
	private String headercolor;
	private DashboardModel model;
	
	private MapModel vtrackingModel;
	private Marker marker;
	private Vector<DashboardVehicle> dashVehicles;
	private DashboardVehicle selectedDashVehicle;
	private Vector<VehicleLocationData> dashVehiclesLocs;
	private String defaultCenterCoor = "6.427887,3.4287645";
	private String centerCoor;
	private int vtrackpollinterval = 120;
	
	private Vector<ExpenseType> expTypes;
	private Vector<Expense> exps;
	private String[] expsBackTypes = new String[]{"7 Days", "7 Weeks", "7 Months"};
	private String expsBack;
	
	private double minY = 0, maxY = 100;
	private CartesianChartModel expsLinearModel, expsBudgetModel;
	//private BarChartModel expsBudgetModel;
	
	private Vector<String[]> upcomingAndRecentExpirations;
	private Vector<String[]> upcomingMaintenances;
	
	private PartnerDriver bestDriver;
	private Vector<PartnerDriver> bestDrivers, worstDrivers;
	
	private VehicleTrackerData markerTrackerData = null;
	
	//private MenuModel menuModel;
	
	public DashboardMBean()
	{
		model = new DefaultDashboardModel();
		
		vtrackpollinterval = 120;
        defaultCenterCoor = "6.427887,3.4287645";
        centerCoor = defaultCenterCoor;
        
        expsBack = expsBackTypes[0];
        
        theme = "aristo";
        headercolor = "header_main";
	}
	
	public void keepSessionAlive()
	{
		System.out.println("Keeping session alive for user: " + getUser().getUsername());
	}
	
	@SuppressWarnings("unchecked")
	public PartnerSetting getSetting() {
		PartnerSetting setting = null;
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("partner", getUser().getPartner());
		GeneralDAO gDAO = new GeneralDAO();
		Object pSettingsObj = gDAO.search("PartnerSetting", params);
		if(pSettingsObj != null) {
			Vector<PartnerSetting> pSettingsList = (Vector<PartnerSetting>)pSettingsObj;
			for(PartnerSetting e : pSettingsList) {
				setting = e;
			}
		}
		gDAO.destroy();
		return setting;
	}
	
	public void saveAudit(String narration, String page_url, Vehicle vehicle)
	{
		Audit audit = new Audit();
		audit.setAction_dt(new Date());
		audit.setUser(getUser());
		audit.setNarration(narration);
		
		if(vehicle != null)
			audit.setVehicle(vehicle);
		
		GeneralDAO gDAO = new GeneralDAO();
		
		if(page_url != null && !page_url.isEmpty()) {
			try {
				Query q = gDAO.createQuery("Select e from MFunction e where e.page_url=:page_url");
				q.setParameter("page_url", page_url);
				Object obj = gDAO.search(q, 1);
				if(obj != null) {
					audit.setFunction((MFunction)obj);
				}
			} catch(Exception ex) {}
		}
		
		gDAO.startTransaction();
		if(gDAO.save(audit))
			gDAO.commit();
		else
			gDAO.rollback();
		gDAO.destroy();
	}
	
	public void updateDashsToShow()
	{
		DashboardColumn column1 = new DefaultDashboardColumn();
		column1.addWidget("shortcuts");
		
		if(getRolesDashs() != null && getRolesDashs().size() > 0)
		{
			for(MDashRole mdr : getRolesDashs())
			{
				column1.addWidget(mdr.getDash().getName());
			}
			model.addColumn(column1);
		}
        /*column1.addWidget("trackv");
		column1.addWidget("duelics");
		column1.addWidget("duemaints");
        column1.addWidget("recentexpenses");
        column1.addWidget("bookings");*/
	}
	
	public long getVehiclesWithoutDriversCount() {
		return new ReportDAO().getVehiclesWithoutDriversCount(getUser().getPartner().getId());
	}
	public long getDriversWithoutLicenseCount() {
		return new ReportDAO().getDriversWithoutLicenseCount(getUser().getPartner().getId());
	}
	public Vector<String[]> getDriversWithoutLicenseList() {
		return new ReportDAO().getDriversWithoutLicenseList(getUser().getPartner().getId());
	}
	public long getDriversWithoutVehiclesCount() {
		return new ReportDAO().getDriversWithoutVehiclesCount(getUser().getPartner().getId());
	}
	public long getPendingOvertimeRequestCount() {
		return new ReportDAO().getPendingOvertimeRequestCount(getUser().getPartner().getId());
	}
	@SuppressWarnings("unchecked")
	public long getPendingTripRequestCount() {
		long count = 0;
		GeneralDAO gDAO = new GeneralDAO();
		Object tripsObj = null;
		if(getUser().getPersonel().isUnitHead()) {
			Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and e.staff.unit=:myunit and e.approvalStatus=:approvalStatus and e.departureDateTime > :nowDateTime");
			q.setParameter("partner", getUser().getPartner());
			q.setParameter("myunit", getUser().getPersonel().getUnit());
			q.setParameter("approvalStatus", "PENDING");
			q.setParameter("nowDateTime", new Date());
			tripsObj = gDAO.search(q, 0);
		} else if(getUser().getPersonel().isFleetManager()) {
			Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and e.approvalStatus2=:approvalStatus2 and e.approvalStatus=:approvalStatus and e.departureDateTime > :nowDateTime");
			q.setParameter("partner", getUser().getPartner());
			q.setParameter("approvalStatus", "APPROVED");
			q.setParameter("approvalStatus2", "PENDING");
			q.setParameter("nowDateTime", new Date());
			tripsObj = gDAO.search(q, 0);
		}
		if(tripsObj != null) {
			Vector<CorporateTrip> trips = (Vector<CorporateTrip>)tripsObj;
			count = trips.size();
		}
		gDAO.destroy();
		return count;
	}
	@SuppressWarnings("unchecked")
	public long getOngoingTripCount() {
		long count = 0;
		if(getUser().getPersonel().isFleetManager()) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and e.approvalStatus=:approvalStatus and (e.tripStatus=:tripStatus or e.tripStatus=:tripStatus2)");
			q.setParameter("partner", getUser().getPartner());
			q.setParameter("approvalStatus", "APPROVED");
			q.setParameter("tripStatus", "ON_TRIP");
			q.setParameter("tripStatus2", "SHOULD_BE_COMPLETED");
			
			Object tripsObj = gDAO.search(q, 0);
			if(tripsObj != null) {
				Vector<CorporateTrip> trips = (Vector<CorporateTrip>)tripsObj;
				count = trips.size();
			}
			gDAO.destroy();
		}
		return count;
	}
	@SuppressWarnings("unchecked")
	public long getOverdueTripCount() {
		long count = 0;
		if(getUser().getPersonel().isFleetManager()) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and e.approvalStatus=:approvalStatus and (e.tripStatus=:tripStatus2)");
			q.setParameter("partner", getUser().getPartner());
			q.setParameter("approvalStatus", "APPROVED");
			q.setParameter("tripStatus2", "SHOULD_BE_COMPLETED");
			
			Object tripsObj = gDAO.search(q, 0);
			if(tripsObj != null) {
				Vector<CorporateTrip> trips = (Vector<CorporateTrip>)tripsObj;
				count = trips.size();
			}
			gDAO.destroy();
		}
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<WorkOrder> getDueWorkOrders() {
		Vector<WorkOrder> workorders = new Vector<WorkOrder>();
		GeneralDAO gDAO = new GeneralDAO();
		
		Query q = gDAO.createQuery("Select e from WorkOrder e where e.status = :status and e.partner.id=:partner_id");
		q.setParameter("status", "IN-PROGRESS");
		q.setParameter("partner_id", getUser().getPartner().getId());
		Object obj = gDAO.search(q, 0);
		if(obj != null) {
			Vector<WorkOrder> list = (Vector<WorkOrder>)obj;
			for(WorkOrder wo : list) {
				if(wo.getProposedCompletion_dt() != null) {
					Date now = new Date();
					if(now.after(wo.getProposedCompletion_dt())) {
						workorders.add(wo);
					}
				}
			}
		}
		
		gDAO.destroy();
		
		return workorders;
	}
	
	public long getExpiredLicenseCount()
	{
		long count = 0;
		for(String[] e : getUpcomingAndRecentExpirations())
		{
			if(e[4] != null && e[4].equalsIgnoreCase("Expired"))
				count++;
		}
		return count;
	}
	public long getSoonToExpireLicenseCount()
	{
		long count = 0;
		for(String[] e : getUpcomingAndRecentExpirations())
		{
			if(e[4] != null && e[4].equalsIgnoreCase("Soon to Expire"))
				count++;
		}
		return count;
	}
	public long getExpiringLicenseCount()
	{
		long count = 0;
		for(String[] e : getUpcomingAndRecentExpirations())
		{
			if(e[4] != null && e[4].equalsIgnoreCase("Expiring"))
				count++;
		}
		return count;
	}
	public long getTotalDueRoutineMaint()
	{
		long count = 0;
		for(String[] e : getUpcomingMaintenances())
		{
			if(e[0] != null && e[0].equalsIgnoreCase("Routine"))
				count++;
		}
		return count;
	}
	public long getTotalUpcomingAndDueAdHocMaint() {
		long count = 0;
		for(String[] e : getUpcomingMaintenances())
		{
			if(e[0] != null && e[0].equalsIgnoreCase("AdHoc"))
				count++;
		}
		return count;
	}
	public long getPendingFuelingRequestCount() {
		return new ReportDAO().getPendingFuelingRequestCount(getUser().getId());
	}
	public long getPendingExpenseRequestCount() {
		return new ReportDAO().getPendingExpenseRequestCount(getUser().getId());
	}
	public long getTotalPendingAccidentRepairs() {
		return new ReportDAO().getPendingAccidentRepairRequestCount(getUser().getPartner().getId().longValue());
	}
	
	@SuppressWarnings("unchecked")
	public long getTotalPendingMaintenanceRequests() {
		long count = 0;
		GeneralDAO gDAO = new GeneralDAO();
		Query q = gDAO.createQuery("Select e from Approver e where e.entityName='VehicleMaintenanceRequest' and e.approvalUser=:user and e.approvalStatus='PENDING' order by e.crt_dt");
		q.setParameter("user", getUser());
		Object obj = gDAO.search(q, 0);
		if(obj != null) {
			Vector<Approver> apList = (Vector<Approver>)obj;
			if(apList.size() > 0) {
				for(Approver ap : apList) {
					Object mobj = gDAO.find(VehicleMaintenanceRequest.class, ap.getEntityId());
					if(mobj != null) {
						count += 1;
					}
				}
			}
		}
		gDAO.destroy();
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public long getTotalApprovedRMaintenanceNoWorkorder() {
		long count = 0;
		GeneralDAO gDAO = new GeneralDAO();
		Query q = gDAO.createQuery("Select e from Approver e where e.entityName='VehicleMaintenanceRequest' and e.approvalUser=:user and e.approvalStatus='APPROVED' order by e.crt_dt");
		q.setParameter("user", getUser());
		Object obj = gDAO.search(q, 0);
		if(obj != null) {
			Vector<Approver> apList = (Vector<Approver>)obj;
			if(apList.size() > 0) {
				for(Approver ap : apList) {
					Object mobj = gDAO.find(VehicleMaintenanceRequest.class, ap.getEntityId());
					if(mobj != null) {
						VehicleMaintenanceRequest vmr = (VehicleMaintenanceRequest)mobj;
						if(vmr.getMaintenanceType().equalsIgnoreCase("Routine"))
							count += 1;
					}
				}
			}
		}
		gDAO.destroy();
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public long getTotalApprovedAHMaintenanceNoWorkorder() {
		long count = 0;
		GeneralDAO gDAO = new GeneralDAO();
		Query q = gDAO.createQuery("Select e from Approver e where e.entityName='VehicleMaintenanceRequest' and e.approvalUser=:user and e.approvalStatus='APPROVED' order by e.crt_dt");
		q.setParameter("user", getUser());
		Object obj = gDAO.search(q, 0);
		if(obj != null) {
			Vector<Approver> apList = (Vector<Approver>)obj;
			if(apList.size() > 0) {
				for(Approver ap : apList) {
					Object mobj = gDAO.find(VehicleMaintenanceRequest.class, ap.getEntityId());
					if(mobj != null) {
						VehicleMaintenanceRequest vmr = (VehicleMaintenanceRequest)mobj;
						if(vmr.getMaintenanceType().equalsIgnoreCase("Adhoc"))
							count += 1;
					}
				}
			}
		}
		gDAO.destroy();
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public long getMyPendAttendedWorkOrdersCount() {
		long count = 0;
		GeneralDAO gDAO = new GeneralDAO();
		
		String str = "Select e from WorkOrder e where e.status=:status and e.partner=:partner and e.approveBy=:approveBy";
		Query q = gDAO.createQuery(str);
		q.setParameter("partner", getUser().getPartner());
		q.setParameter("status", "REQUEST");
		q.setParameter("approveBy", getUser());
		Object obj = gDAO.search(q, 0);
		if(obj != null) {
			Vector<WorkOrder> apList = (Vector<WorkOrder>)obj;
			count = apList.size();
		}
		gDAO.destroy();
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public void updateTheme() {
		PartnerUserSetting sett = new PartnerUserSetting();
		
		GeneralDAO gDAO = new GeneralDAO();
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("user", getUser());
		Object setObj = gDAO.search("PartnerUserSetting", params);
		if(setObj != null)
		{
			Vector<PartnerUserSetting> setList = (Vector<PartnerUserSetting>)setObj;
			for(PartnerUserSetting e : setList)
				sett = e;
		}
		
		sett.setUser(getUser());
		sett.setTheme(getTheme());
		sett.setHeadercolor(getHeadercolor());
		gDAO.startTransaction();
		boolean ret = false;
		if(sett.getId() != null)
			ret = gDAO.update(sett);
		else
			ret = gDAO.save(sett);
		if(ret)
		{
			gDAO.commit();
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "UI Setting change successful.");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		else
		{
			gDAO.rollback();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "UI Setting change failed. " + gDAO.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		gDAO.destroy();
	}
	
	/**
	  * 
	  * @param url
	  */
	 public void redirector(String url)
	 {
	        FacesContext fc = FacesContext.getCurrentInstance();
	        ExternalContext ec = fc.getExternalContext();
	        try 
	        {
	            ec.redirect(url);
	        } 
	        catch (IOException ex)
	        {}
	    }

	 /*
	  * 
	  */
	 public void forwarder(String url) 
	 {
		 FacesContext fc = FacesContext.getCurrentInstance();
		 try
		 {
			 fc.getExternalContext().dispatch(url);
		 } 
		 catch (Exception ex) 
		 {}
	 }

	public String gotoPage(String page, boolean subFunction)
	{
		if(!subFunction)
			function_page = page;
		
		return page+"?faces-redirect=true";
	}
	
	@SuppressWarnings("unchecked")
	public String getScrollMessage()
	{
		String ret = "";
		if(function_page != null)
		{
			if(function_page.equalsIgnoreCase("dashboard"))
				ret = "Welcome to the Dashboard...";
			else if(function_page.equalsIgnoreCase("faq"))
				ret = "Frequently asked questions...";
			else
			{
				MFunction func = null;
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("page_url", function_page);
				Object mf = gDAO.search("MFunction", params);
				if(mf != null)
				{
					Vector<MFunction> mfs = (Vector<MFunction>)mf;
					for(MFunction e : mfs)
					{
						func = e;
					}
				}
				if(func != null)
				{
					ret = func.getModule().getDescription() + "... " + func.getDescription();
				}
			}
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public boolean currentModuleHeader(String module_nm)
	{
		boolean ret = false;
		
		if(module_nm.equals(function_page))
			ret = true;
		else
		{
			GeneralDAO gDAO = new GeneralDAO();
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("page_url", function_page);
			Object mf = gDAO.search("MFunction", params);
			if(mf != null)
			{
				Vector<MFunction> mfs = (Vector<MFunction>)mf;
				for(MFunction e : mfs)
				{
					if(e.getModule().getName().equals(module_nm))
					{
						ret = true;
						break;
					}
				}
			}
		}
		return ret;
	}
	
	public String changeInitPassword()
	{
		String ret = "index";
		if(getNewpassword() != null && getOldpassword() != null && getCpassword() != null)
		{
			if(getNewpassword().equals(getCpassword()))
			{
				if(Hasher.getHashValue(getOldpassword()).equals(getUser().getPassword()))
				{
					getUser().setPassword(Hasher.getHashValue(getNewpassword()));
					getUser().setActivated(true);
					GeneralDAO gDAO = new GeneralDAO();
					gDAO.startTransaction();
					boolean retb = gDAO.update(getUser());
					if(retb)
					{
						gDAO.commit();
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Password change successful.");
						FacesContext.getCurrentInstance().addMessage(null, msg);
						
						ret = "dashboard?faces-redirect=true";
					}
					else
					{
						gDAO.rollback();
						getUser().setActivated(false);
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Password change failed. " + gDAO.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Authentication failed.");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "New password fields are not the same.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "All fields with the '*' are required.");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		return ret;
	}
	
	public void changePassword()
	{
		if(getNewpassword() != null && getOldpassword() != null && getCpassword() != null)
		{
			if(getNewpassword().equals(getCpassword()))
			{
				if(Hasher.getHashValue(getOldpassword()).equals(getUser().getPassword()))
				{
					getUser().setPassword(Hasher.getHashValue(getNewpassword()));
					GeneralDAO gDAO = new GeneralDAO();
					gDAO.startTransaction();
					boolean ret = gDAO.update(getUser());
					if(ret)
					{
						gDAO.commit();
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Password change successful.");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Password change failed. " + gDAO.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Authentication failed.");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "New password fields are not the same.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "All fields with the '*' are required.");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public PartnerSubscription getSubscription() {
		return subscription;
	}

	public void setSubscription(PartnerSubscription subscription) {
		this.subscription = subscription;
	}

	public PartnerLicense getPartnerLicense() {
		return partnerLicense;
	}

	public void setPartnerLicense(PartnerLicense partnerLicense) {
		this.partnerLicense = partnerLicense;
	}

	public PartnerUser getUser() {
		return user;
	}

	public void setUser(PartnerUser user) {
		this.user = user;
	}

	public PartnerDriver getDriver() {
		return driver;
	}

	public void setDriver(PartnerDriver driver) {
		this.driver = driver;
	}

	public Vector<PartnerUserRole> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(Vector<PartnerUserRole> userRoles) {
		this.userRoles = userRoles;
	}

	public Vector<MRoleFunction> getRolesFunctions() {
		return rolesFunctions;
	}

	public void setRolesFunctions(Vector<MRoleFunction> rolesFunctions) {
		this.rolesFunctions = rolesFunctions;
	}
	
	public Vector<MRoleReport> getRolesReports() {
		return rolesReports;
	}

	public void setRolesReports(Vector<MRoleReport> rolesReports) {
		this.rolesReports = rolesReports;
	}

	public Vector<MDashRole> getRolesDashs() {
		return rolesDashs;
	}

	public void setRolesDashs(Vector<MDashRole> rolesDashs) {
		this.rolesDashs = rolesDashs;
	}

	public String getModuleDisplayName(String mdule)
	{
		String display_nm = "";
		if(getRolesFunctions() != null)
		{
			for(MRoleFunction e : getRolesFunctions())
			{
				String module_nm = e.getFunction().getModule().getName();
				if(module_nm.equals(mdule))
				{
					display_nm = module_nm;
					if(e.getFunction().getModule().getDisplay_name() != null)
						display_nm = e.getFunction().getModule().getDisplay_name();
					break;
				}
			}
		}
		return display_nm;
	}
	
	public String getModuleIconName(String mdule)
	{
		String icon_nm = "";
		if(getRolesFunctions() != null)
		{
			for(MRoleFunction e : getRolesFunctions())
			{
				String module_nm = e.getFunction().getModule().getName();
				if(module_nm.equals(mdule))
				{
					if(e.getFunction().getModule().getIcon_url() != null)
						icon_nm = e.getFunction().getModule().getIcon_url();
					break;
				}
			}
		}
		return icon_nm;
	}
	
	public Vector<MFunction> getModuleFunctions(String module_nm)
	{
		if(getUserModulesFunctions().containsKey(module_nm))
		{
			return getUserModulesFunctions().get(module_nm);
		}
		else
			return null;
	}
	
	private List<String> modulesKeys;
	private List<String> otherModulesKeys;
	
	@SuppressWarnings("unchecked")
	public List<String> getModulesKeys()
	{
		if(modulesKeys == null || modulesKeys.size() == 0)
		{
			int max = 8;
			if(getUser().getPartner().getFuelingType().equalsIgnoreCase("Both") ||
					getUser().getPartner().getFuelingType().equalsIgnoreCase("Automated")) 
				max = 7;
			modulesKeys = new ArrayList<String>();
		
			GeneralDAO gDAO = new GeneralDAO();
			
			Query q = gDAO.createQuery("Select e from Module e where e.name IN :nameList order by e.displayIndex, e.name");
			q.setParameter("nameList", getUserModulesFunctions().keySet());
			Object mObjs = gDAO.search(q, 0);
			if(mObjs != null)
			{
				Vector<Module> mList = (Vector<Module>)mObjs;
				for(Module m : mList)
				{
					if(modulesKeys.size() == max) // maximum 8 main modules, so add to other modules
					{
						if(otherModulesKeys == null)
							otherModulesKeys = new ArrayList<String>();
						otherModulesKeys.add(m.getName());
					}
					else
					{
						modulesKeys.add(m.getName());
					}
				}
			}
		}
		return modulesKeys;
	}

	public List<String> getOtherModulesKeys() {
		if(otherModulesKeys == null)
			getModulesKeys();
		return otherModulesKeys;
	}

	public Hashtable<String, Vector<MFunction>> getUserModulesFunctions() {
		if(userModulesFunctions == null)
		{
			userModulesFunctions = new Hashtable<String, Vector<MFunction>>();
			
			if(getRolesFunctions() != null)
			{
				for(MRoleFunction e : getRolesFunctions())
				{
					String module_nm = e.getFunction().getModule().getName();
					Vector<MFunction> mf = new Vector<MFunction>();
					if(userModulesFunctions.containsKey(module_nm))
					{
						mf = userModulesFunctions.get(module_nm);
					}
					
					mf.add(e.getFunction());
					
					userModulesFunctions.put(module_nm, mf);
				}
			}
		}
		return userModulesFunctions;
	}

	public void setUserModulesFunctions(
			Hashtable<String, Vector<MFunction>> userModulesFunctions) {
		this.userModulesFunctions = userModulesFunctions;
	}

	public String[] getReportsModules() {
		if(reportsModules == null)
		{
			if(getRolesReports() != null && getRolesReports().size() > 0)
			{
				Vector<String> rm = new Vector<String>();
				for(MRoleReport mrr : getRolesReports())
				{
					if(!rm.contains(mrr.getReport().getModule()))
					{
						rm.add(mrr.getReport().getModule());
					}
				}
				reportsModules = new String[rm.size()];
				for(int i=0; i<reportsModules.length; i++)
					reportsModules[i] = rm.get(i);
			}
		}
		return reportsModules;
	}

	public void setReportsModules(String[] reportsModules) {
		this.reportsModules = reportsModules;
	}

	public Vector<Report> getModuleReports(String modulename) {
		try
		{
			return getModuleReports().get(modulename);
		}
		catch(Exception ex)
		{
			return null;
		}
	}

	public Hashtable<String, Vector<Report>> getModuleReports() {
		if(moduleReports == null)
		{
			moduleReports = new Hashtable<String, Vector<Report>>();
			if(getReportsModules() != null)
			{
				for(String e : getReportsModules())
				{
					Vector<Report> v = new Vector<Report>();
					for(MRoleReport mrr : getRolesReports())
					{
						if(e.equals(mrr.getReport().getModule()))
						{
							v.add(mrr.getReport());
						}
					}
					moduleReports.put(e, v);
				}
			}
		}
		return moduleReports;
	}

	public void setModuleReports(Hashtable<String, Vector<Report>> moduleReports) {
		this.moduleReports = moduleReports;
	}

	public String getOldpassword() {
		return oldpassword;
	}

	public void setOldpassword(String oldpassword) {
		this.oldpassword = oldpassword;
	}

	public String getNewpassword() {
		return newpassword;
	}

	public void setNewpassword(String newpassword) {
		this.newpassword = newpassword;
	}

	public String getCpassword() {
		return cpassword;
	}

	public void setCpassword(String cpassword) {
		this.cpassword = cpassword;
	}

	public String getFunction_page() {
		return function_page;
	}

	public void setFunction_page(String function_page) {
		this.function_page = function_page;
	}
	
	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getHeadercolor() {
		return headercolor;
	}

	public void setHeadercolor(String headercolor) {
		this.headercolor = headercolor;
	}

	public DashboardModel getModel() {
		return model;
	}
	
	@SuppressWarnings("unchecked")
	public void onMarkerSelect(OverlaySelectEvent event)
	{
		try
		{
			marker = (Marker) event.getOverlay();
			markerTrackerData = null;
			if(marker != null)
			{
				String regNum = marker.getTitle();
				GeneralDAO gDAO = new GeneralDAO();
				Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.registrationNo = :regNum");
				q.setParameter("regNum", regNum);
				Object listObj = gDAO.search(q, 0);
				if(listObj != null)
				{
					List<VehicleTrackerData> list = (List<VehicleTrackerData>)listObj;
					for(VehicleTrackerData e : list)
						markerTrackerData = e;
				}
				gDAO.destroy();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void updateMarkers()
	{
		DashboardVehicle dv = getSelectedDashVehicle();
		int center_index = 0;
		for(VehicleLocationData vld : getDashVehiclesLocs())
		{
			boolean exists = false;
			if(getVtrackingModel().getMarkers() != null)
			{
				for(int i = 0; i < getVtrackingModel().getMarkers().size(); i++)
				{
					if(getVtrackingModel().getMarkers().get(i).getTitle().equalsIgnoreCase(vld.getVehicle().getRegistrationNo()))
					{
						exists = true;
						getVtrackingModel().getMarkers().get(i).setLatlng(new LatLng(vld.getLat(), vld.getLon()));
						// this is the selected vehicle, try to make the map follow it to whereever it's going
						if(dv != null && dv.getVehicle().getId().longValue() == vld.getVehicle().getId().longValue())
						{
							setCenterCoor(vld.getLat() + "," + vld.getLon());
							center_index = i;
						}
						break;
					}
				}
			}
			if(!exists)
			{
				try
				{
					LatLng coord1 = new LatLng(vld.getLat(), vld.getLon());
					//Basic marker
					Marker marker = new Marker(coord1, vld.getVehicle().getRegistrationNo());
					getVtrackingModel().addOverlay(marker);
				}
				catch(Exception ex)
				{}
			}
		}
		
		if(getVtrackingModel().getMarkers() != null)
		{
			for(int i = 0; i < getVtrackingModel().getMarkers().size(); i++)
			{
				RequestContext.getCurrentInstance().addCallbackParam("marker" + i, getVtrackingModel().getMarkers().get(i));
				RequestContext.getCurrentInstance().addCallbackParam("position" + i, getVtrackingModel().getMarkers().get(i).getLatlng());
				
				if(i == center_index)
				{
					RequestContext.getCurrentInstance().addCallbackParam("centerposition" + i, getVtrackingModel().getMarkers().get(i).getLatlng());
				}
			}
		}
	}
	
	public MapModel getVtrackingModel() {
		if(vtrackingModel == null)
		{
			vtrackingModel = new DefaultMapModel();
			updateMarkers();
		}
		
		return vtrackingModel;
	}

	public void setVtrackingModel(MapModel vtrackingModel) {
		this.vtrackingModel = vtrackingModel;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}

	@SuppressWarnings("unchecked")
	public Vector<DashboardVehicle> getDashVehicles() {
		if(dashVehicles == null)
		{
			dashVehicles = new Vector<DashboardVehicle>();
			
			GeneralDAO gDAO = new GeneralDAO();
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("user", getUser());
			Object dvsObj = gDAO.search("DashboardVehicle", params);
			if(dvsObj != null)
			{
				dashVehicles = (Vector<DashboardVehicle>)dvsObj;
			}
		}
		
		return dashVehicles;
	}

	public void setDashVehicles(Vector<DashboardVehicle> dashVehicles) {
		this.dashVehicles = dashVehicles;
	}

	public DashboardVehicle getSelectedDashVehicle() {
		return selectedDashVehicle;
	}

	public void setSelectedDashVehicle(DashboardVehicle selectedDashVehicle) {
		this.selectedDashVehicle = selectedDashVehicle;
	}
	
	public void onDashDelSelect()
	{
		DashboardVehicle dv = getSelectedDashVehicle();
		if(dv != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			gDAO.remove(dv);
			
			try
			{
				gDAO.commit();
			}
			catch(Exception ex){
				gDAO.rollback();
			}
			gDAO.destroy();
			
			reloadVTrack();
		}
	}
	
	public void onDashLocSelect()
	{
		try
		{
			DashboardVehicle dv = getSelectedDashVehicle();
			if(dv != null)
			{
				for(VehicleLocationData vld : getDashVehiclesLocs())
				{
					if(vld.getVehicle().getId() == dv.getVehicle().getId())
					{
						setCenterCoor(vld.getLat() + "," + vld.getLon());
						break;
					}
				}
			}
		}
		catch(Exception ex)
		{
			setCenterCoor(defaultCenterCoor);
		}
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleLocationData> getDashVehiclesLocs() {
		if(dashVehiclesLocs == null)
		{
			dashVehiclesLocs = new Vector<VehicleLocationData>();
			if(getDashVehicles() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Vector<DashboardVehicle> dvsList = getDashVehicles();
				for(DashboardVehicle dv : dvsList)
				{
					if(dv != null && dv.getVehicle() != null)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("active", true);
						params.put("vehicle", dv.getVehicle());
						Object vlocObj = gDAO.search("VehicleLocationData", params);
						if(vlocObj != null)
						{
							Vector<VehicleLocationData> vloc = (Vector<VehicleLocationData>)vlocObj;
							if(vloc.size() > 0)
							{
								dashVehiclesLocs.add(vloc.get(0));
							}
						}
					}
				}
			}
		}
		
		if(dashVehiclesLocs.size() > 0)
			setCenterCoor(dashVehiclesLocs.get(0).getLat() + "," + dashVehiclesLocs.get(0).getLon());
		else
			setCenterCoor(defaultCenterCoor);
		
		return dashVehiclesLocs;
	}

	public void setDashVehiclesLocs(Vector<VehicleLocationData> dashVehiclesLocs) {
		this.dashVehiclesLocs = dashVehiclesLocs;
	}

	public String getCenterCoor() {
		return centerCoor;
	}

	public void setCenterCoor(String centerCoor) {
		this.centerCoor = centerCoor;
	}
	
	public void reloadVTrack()
	{
		setDashVehiclesLocs(null);
		setDashVehicles(null);
		updateMarkers();
	}

	public int getVtrackpollinterval() {
		return vtrackpollinterval;
	}

	public void setVtrackpollinterval(int vtrackpollinterval) {
		this.vtrackpollinterval = vtrackpollinterval;
	}
	
	public void setVTrackIntV()
	{
		if(getVtrackpollinterval() < 60)
		{
			setVtrackpollinterval(60);
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Interval set to minimum of 60 seconds.");
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Interval set successfully.");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void resetExps()
	{
		setExps(null);
		expsLinearModel = null;
	}

	@SuppressWarnings("unchecked")
	public Vector<ExpenseType> getExpTypes() {
		if(expTypes == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getUser().getPartner());
			params.put("systemObj", false);
			
			Object drvs = gDAO.search("ExpenseType", params);
			if(drvs != null)
			{
				expTypes = (Vector<ExpenseType>)drvs;
			}
			
			params = new Hashtable<String, Object>();
			params.put("systemObj", true);
			drvs = gDAO.search("ExpenseType", params);
			if(drvs != null)
			{
				if(expTypes == null)
					expTypes = new Vector<ExpenseType>();
				Vector<ExpenseType> drvsList = (Vector<ExpenseType>)drvs;
				for(ExpenseType e : drvsList)
				{
					expTypes.add(e);
				}
			}
		}
		return expTypes;
	}

	public void setExpTypes(Vector<ExpenseType> expTypes) {
		this.expTypes = expTypes;
	}

	public Vector<Expense> getExps() {
		if(exps == null)
		{
			
		}
		return exps;
	}

	public void setExps(Vector<Expense> exps) {
		this.exps = exps;
	}

	public void SetHowBack()
	{
		expsLinearModel = null;
	}
	
	public String getExpsBack() {
		return expsBack;
	}

	public void setExpsBack(String expsBack) {
		this.expsBack = expsBack;
	}

	public String[] getExpsBackTypes() {
		return expsBackTypes;
	}

	public double getMinY() {
		return minY;
	}

	public void setMinY(double minY) {
		this.minY = minY;
	}

	public double getMaxY() {
		return maxY;
	}

	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}

	@SuppressWarnings("unchecked")
	public CartesianChartModel getExpsLinearModel() {
		if(expsLinearModel == null)
		{
			expsLinearModel = new CartesianChartModel();
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Calendar can = Calendar.getInstance();
			can.set(Calendar.HOUR_OF_DAY, 0);
			can.set(Calendar.MINUTE, 0);
			can.set(Calendar.SECOND, 0);
			Calendar cNow = Calendar.getInstance();
			cNow.set(Calendar.HOUR_OF_DAY, 0);
			cNow.set(Calendar.MINUTE, 0);
			cNow.set(Calendar.SECOND, 0);
			
			int index = -1;
			if(getExpsBack() != null)
			{
				for(int i=0; i<expsBackTypes.length; i++)
				{
					if(getExpsBack().equals(expsBackTypes[i]))
					{
						index = i;
						break;
					}
				}
			}
			if(index >= 0)
			{
				for(ExpenseType et : getExpTypes())
				{
					can = Calendar.getInstance();
					can.set(Calendar.HOUR_OF_DAY, 0);
					can.set(Calendar.MINUTE, 0);
					can.set(Calendar.SECOND, 0);
					if(index == 0)
						can.add(Calendar.DATE, -7);
					else if(index == 1)
						can.add(Calendar.WEEK_OF_YEAR, -7);
					else if(index == 2)
						can.add(Calendar.MONTH, -7);
					
					ChartSeries etSeries = new ChartSeries();
					etSeries.setLabel(et.getName());
					while(can.compareTo(cNow) <= 0)
					{
						Query q = null;
						if(index == 0)
						{
							Calendar can2 = Calendar.getInstance();
							can2.setTime(can.getTime());
							can2.set(Calendar.HOUR_OF_DAY, can2.getMaximum(Calendar.HOUR_OF_DAY));
							can2.set(Calendar.MINUTE, can2.getMaximum(Calendar.MINUTE));
							can2.set(Calendar.SECOND, can2.getMaximum(Calendar.SECOND));
							
							q = gDAO.createQuery("Select e from Expense e where e.partner=:partner and e.type=:type and (e.expense_dt between :dt and :dt2) and e.createdBy=:createdBy");
							q.setParameter("partner", getUser().getPartner());
							q.setParameter("type", et);
							q.setParameter("dt", can.getTime());
							q.setParameter("dt2", can2.getTime());
							q.setParameter("createdBy", getUser());
						}
						else if(index == 1)
						{
							Calendar can2 = Calendar.getInstance();
							can2.setTime(can.getTime());
							can2.set(Calendar.HOUR_OF_DAY, can2.getMaximum(Calendar.HOUR_OF_DAY));
							can2.set(Calendar.MINUTE, can2.getMaximum(Calendar.MINUTE));
							can2.set(Calendar.SECOND, can2.getMaximum(Calendar.SECOND));
							can2.add(Calendar.WEEK_OF_YEAR, -1);
							
							q = gDAO.createQuery("Select e from Expense e where e.partner=:partner and e.type=:type and (e.expense_dt between :dt and :dt2) and e.createdBy=:createdBy");
							q.setParameter("partner", getUser().getPartner());
							q.setParameter("type", et);
							q.setParameter("dt", can2.getTime());
							q.setParameter("dt2", can.getTime());
							q.setParameter("createdBy", getUser());
						}
						else if(index == 2)
						{
							Calendar can2 = Calendar.getInstance();
							can2.setTime(can.getTime());
							can2.set(Calendar.HOUR_OF_DAY, can2.getMaximum(Calendar.HOUR_OF_DAY));
							can2.set(Calendar.MINUTE, can2.getMaximum(Calendar.MINUTE));
							can2.set(Calendar.SECOND, can2.getMaximum(Calendar.SECOND));
							can2.add(Calendar.MONTH, -1);
							
							q = gDAO.createQuery("Select e from Expense e where e.partner=:partner and e.type=:type and (e.expense_dt between :dt and :dt2) and e.createdBy=:createdBy");
							q.setParameter("partner", getUser().getPartner());
							q.setParameter("type", et);
							q.setParameter("dt", can2.getTime());
							q.setParameter("dt2", can.getTime());
							q.setParameter("createdBy", getUser());
						}
						Object objs = gDAO.search(q, 0);
						BigDecimal sum = new BigDecimal(0.00);
						if(objs != null)
						{
							Vector<Expense> etVals = (Vector<Expense>)objs;
							if(etVals.size() > 0)
							{
								for(Expense e : etVals)
								{
									sum = sum.add(new BigDecimal(e.getAmount()));
								}
							}
						}
						if(sum.doubleValue() < getMinY())
							setMinY(sum.doubleValue());
						if(sum.doubleValue() > getMaxY())
							setMaxY(sum.doubleValue()+10);
						
						String key = "";
						if(index == 0 || index == 1)
						{
							key = can.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + "-" + can.get(Calendar.DATE);
						}
						else if(index == 2)
						{
							key = can.get(Calendar.YEAR) + "-" + can.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);
						}
						etSeries.set(key, sum.doubleValue());
						if(index == 0)
							can.add(Calendar.DATE, 1);
						else if(index == 1)
							can.add(Calendar.WEEK_OF_YEAR, 1);
						else if(index == 2)
							can.add(Calendar.MONTH, 1);
					}
					expsLinearModel.addSeries(etSeries);
				}
			}
			gDAO.destroy();
		}
		return expsLinearModel;
	}
	
	public void resetBudgetExpenses() {
		expsBudgetModel = null;
	}
	
	@SuppressWarnings("unchecked")
	public CartesianChartModel getExpsBudgetModel() {
		if(expsBudgetModel == null) {
			ChartSeries etSeries = new ChartSeries(), bgSeries = new ChartSeries();
			etSeries.setLabel("Expense Type");
			bgSeries.setLabel("Budget");
			
			for(ExpenseType et : getExpTypes()) {
				Budget b = null;
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getUser().getPartner());
				params.put("active", true);
				params.put("type", et);
				
				Object drvs = gDAO.search("Budget", params);
				if(drvs != null) {
					Vector<Budget> budgets = (Vector<Budget>)drvs;
					for(Budget budget : budgets)
						b = budget;
				}
				
				if(b != null) {
					if(b.getAmount().doubleValue() > getMaxY())
						setMaxY(b.getAmount().doubleValue()+10);
					
					Query q = gDAO.createQuery("Select e from Expense e where e.partner=:partner and e.type=:type and (e.expense_dt between :dt and :dt2)");
					q.setParameter("partner", getUser().getPartner());
					q.setParameter("type", et);
					q.setParameter("dt", b.getStart_dt());
					q.setParameter("dt2", b.getEnd_dt());
					
					Object objs = gDAO.search(q, 0);
					BigDecimal sum = new BigDecimal(0.00);
					if(objs != null)
					{
						Vector<Expense> etVals = (Vector<Expense>)objs;
						for(Expense e : etVals)
						{
							sum = sum.add(new BigDecimal(e.getAmount()));
						}
						
						if(sum.doubleValue() > getMaxY())
							setMaxY(sum.doubleValue()+10);
					}
					
					etSeries.set(et.getName(), sum.doubleValue());
					bgSeries.set(et.getName(), b.getAmount().doubleValue());
				}
			}
			
			expsBudgetModel = new CartesianChartModel();
			expsBudgetModel.addSeries(bgSeries);
			expsBudgetModel.addSeries(etSeries);
			
			/*expsBudgetModel.setTitle("Active Budget and Expenses");
			expsBudgetModel.setLegendPosition("ne");
	         
	        Axis xAxis = expsBudgetModel.getAxis(AxisType.X);
	        xAxis.setLabel("Expense");
	         
	        Axis yAxis = expsBudgetModel.getAxis(AxisType.Y);
	        yAxis.setLabel("Value");
	        yAxis.setMin(0);
	        yAxis.setMax(getMaxY()+10);*/
		}
		return expsBudgetModel;
	}

	public void resetExpLicenses() {
		setUpcomingAndRecentExpirations(null);
	}

	@SuppressWarnings("unchecked")
	public Vector<String[]> getUpcomingAndRecentExpirations() {
		if(upcomingAndRecentExpirations == null)
		{
			upcomingAndRecentExpirations = new Vector<String[]>();
			
			GeneralDAO gDAO = new GeneralDAO();
			
			DateFormat dtf = DateFormat.getDateTimeInstance();
			
			Calendar c = Calendar.getInstance();
			Calendar c2 = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, -60); // expired 2 months back
			
			Query q = gDAO.createQuery("Select e from VehicleLicense e where e.vehicle.partner=:partner and e.expired=:expired and e.active=:active and (e.lic_end_dt > :start_dt and e.lic_end_dt < :end_date)");
			q.setParameter("partner", getUser().getPartner());
			q.setParameter("expired", true);
			q.setParameter("active", true);
			q.setParameter("start_dt", c.getTime());
			q.setParameter("end_date", c2.getTime());
			
			Object licsObj = gDAO.search(q, 0);
			if(licsObj != null)
			{
				Vector<VehicleLicense> lics = (Vector<VehicleLicense>)licsObj;
				for(VehicleLicense e : lics)
				{
					String[] str = new String[6];
					str[0] = "Vehicle License";
					str[1] = e.getVehicle().getRegistrationNo();
					str[2] = e.getLicType().getName() + " | " + e.getSubLicType();
					str[3] = dtf.format(e.getLic_end_dt());
					str[4] = "Expired";
					str[5] = ""+e.getId();
					
					upcomingAndRecentExpirations.add(str);
				}
			}
			
			c = Calendar.getInstance();
			c2 = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, 15);
			c2.add(Calendar.DAY_OF_MONTH, 30);
			
			q = gDAO.createQuery("Select e from VehicleLicense e where e.vehicle.partner=:partner and e.expired=:expired and e.active=:active and (e.lic_end_dt > :start_dt and e.lic_end_dt < :end_date)");
			q.setParameter("partner", getUser().getPartner());
			q.setParameter("expired", false);
			q.setParameter("active", true);
			q.setParameter("start_dt", c.getTime());
			q.setParameter("end_date", c2.getTime());
			
			licsObj = gDAO.search(q, 0);
			if(licsObj != null)
			{
				Vector<VehicleLicense> lics = (Vector<VehicleLicense>)licsObj;
				for(VehicleLicense e : lics)
				{
					String[] str = new String[6];
					str[0] = "Vehicle License";
					str[1] = e.getVehicle().getRegistrationNo();
					str[2] = e.getLicType().getName() + " | " + e.getSubLicType();
					str[3] = dtf.format(e.getLic_end_dt());
					str[4] = "Soon to Expire";
					str[5] = ""+e.getId();
					
					upcomingAndRecentExpirations.add(str);
				}
			}
			
			c = Calendar.getInstance();
			c2 = Calendar.getInstance();
			c2.add(Calendar.DAY_OF_MONTH, 15);
			
			q = gDAO.createQuery("Select e from VehicleLicense e where e.vehicle.partner=:partner and e.expired=:expired and e.active=:active and (e.lic_end_dt > :start_dt and e.lic_end_dt < :end_date)");
			q.setParameter("partner", getUser().getPartner());
			q.setParameter("expired", false);
			q.setParameter("active", true);
			q.setParameter("start_dt", c.getTime());
			q.setParameter("end_date", c2.getTime());
			
			licsObj = gDAO.search(q, 0);
			if(licsObj != null)
			{
				Vector<VehicleLicense> lics = (Vector<VehicleLicense>)licsObj;
				for(VehicleLicense e : lics)
				{
					String[] str = new String[6];
					str[0] = "Vehicle License";
					str[1] = e.getVehicle().getRegistrationNo();
					str[2] = e.getLicType().getName() + " | " + e.getSubLicType();
					str[3] = dtf.format(e.getLic_end_dt());
					str[4] = "Expiring";
					str[5] = ""+e.getId();
					
					upcomingAndRecentExpirations.add(str);
				}
			}
			
			c = Calendar.getInstance();
			c2 = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, -60); // 2 months
			
			q = gDAO.createQuery("Select e from DriverLicense e where e.driver.partner=:partner and e.expired=:expired and e.active=:active and (e.lic_end_dt > :start_dt and e.lic_end_dt < :end_date)");
			q.setParameter("partner", getUser().getPartner());
			q.setParameter("expired", true);
			q.setParameter("active", true);
			q.setParameter("start_dt", c.getTime());
			q.setParameter("end_date", c2.getTime());
			
			licsObj = gDAO.search(q, 0);
			if(licsObj != null)
			{
				Vector<DriverLicense> lics = (Vector<DriverLicense>)licsObj;
				for(DriverLicense e : lics)
				{
					String[] str = new String[6];
					str[0] = "Driver License";
					str[1] = e.getDriver().getPersonel().getFirstname() + " " + e.getDriver().getPersonel().getLastname();
					str[2] = "Driver License";
					str[3] = dtf.format(e.getLic_end_dt());
					str[4] = "Expired";
					str[5] = ""+e.getId();
					
					upcomingAndRecentExpirations.add(str);
				}
			}
			
			c = Calendar.getInstance();
			c2 = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, 15);
			c2.add(Calendar.DAY_OF_MONTH, 30);
			
			q = gDAO.createQuery("Select e from DriverLicense e where e.driver.partner=:partner and e.expired=:expired and e.active=:active and (e.lic_end_dt > :start_dt and e.lic_end_dt < :end_date)");
			q.setParameter("partner", getUser().getPartner());
			q.setParameter("expired", false);
			q.setParameter("active", true);
			q.setParameter("start_dt", c.getTime());
			q.setParameter("end_date", c2.getTime());
			
			licsObj = gDAO.search(q, 0);
			if(licsObj != null)
			{
				Vector<DriverLicense> lics = (Vector<DriverLicense>)licsObj;
				for(DriverLicense e : lics)
				{
					String[] str = new String[6];
					str[0] = "Driver License";
					str[1] = e.getDriver().getPersonel().getFirstname() + " " + e.getDriver().getPersonel().getLastname();
					str[2] = "Driver License";
					str[3] = dtf.format(e.getLic_end_dt());
					str[4] = "Soon to Expire";
					str[5] = ""+e.getId();
					
					upcomingAndRecentExpirations.add(str);
				}
			}
			
			c = Calendar.getInstance();
			c2 = Calendar.getInstance();
			c2.add(Calendar.DAY_OF_MONTH, 15);
			
			q = gDAO.createQuery("Select e from DriverLicense e where e.driver.partner=:partner and e.expired=:expired and e.active=:active and (e.lic_end_dt > :start_dt and e.lic_end_dt < :end_date)");
			q.setParameter("partner", getUser().getPartner());
			q.setParameter("expired", false);
			q.setParameter("active", true);
			q.setParameter("start_dt", c.getTime());
			q.setParameter("end_date", c2.getTime());
			
			licsObj = gDAO.search(q, 0);
			if(licsObj != null)
			{
				Vector<DriverLicense> lics = (Vector<DriverLicense>)licsObj;
				for(DriverLicense e : lics)
				{
					String[] str = new String[6];
					str[0] = "Driver License";
					str[1] = e.getDriver().getPersonel().getFirstname() + " " + e.getDriver().getPersonel().getLastname();
					str[2] = "Driver License";
					str[3] = dtf.format(e.getLic_end_dt());
					str[4] = "Expiring";
					str[5] = ""+e.getId();
					
					upcomingAndRecentExpirations.add(str);
				}
			}
		}
		return upcomingAndRecentExpirations;
	}

	public void setUpcomingAndRecentExpirations(
			Vector<String[]> upcomingAndRecentExpirations) {
		this.upcomingAndRecentExpirations = upcomingAndRecentExpirations;
	}

	public void resetExpMaints()
	{
		setUpcomingMaintenances(null);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<String[]> getUpcomingMaintenances() {
		if(upcomingMaintenances == null)
		{
			upcomingMaintenances = new Vector<String[]>();
			
			GeneralDAO gDAO = new GeneralDAO();
			
			DateFormat dtf = DateFormat.getDateTimeInstance();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getUser().getPartner());
			params.put("active", true);
			
			Object vehiclesObj = gDAO.search("Vehicle", params);
			if(vehiclesObj != null)
			{
				Vector<Vehicle> vehicles = (Vector<Vehicle>)vehiclesObj;
				for(Vehicle v : vehicles)
				{
					double currentOdometer = 0;
					// Get the current odometer reading
					Query q = gDAO.createQuery("Select MAX(e.odometer) from VehicleOdometerData e where e.vehicle=:vehicle");
					q.setParameter("vehicle", v);
					
					Object ret = gDAO.search(q, 1);
					if(ret != null) {
						try {
							currentOdometer = (Double)ret;
						} catch(Exception ex) {
							ex.printStackTrace();
						}
					}
					
					if(currentOdometer > 0) {
						// Get the current routine setup that is still active, that means its not done
						params = new Hashtable<String, Object>();
						params.put("vehicle", v);
						params.put("active", true);
						
						Object maintsObj = gDAO.search("VehicleRoutineMaintenanceSetup", params);
						if(maintsObj != null) {
							Vector<VehicleRoutineMaintenanceSetup> maints = (Vector<VehicleRoutineMaintenanceSetup>)maintsObj;
							for(VehicleRoutineMaintenanceSetup vms : maints) {
								if(vms.getAlert_odometer().doubleValue() < currentOdometer) {
									String[] str = new String[5];
									str[0] = "Routine";
									str[1] = v.getRegistrationNo();
									str[2] = ""+currentOdometer;
									str[3] = ""+vms.getAlert_odometer().doubleValue();
									str[4] = ""+vms.getOdometer();
									
									upcomingMaintenances.add(str);
								}
							}
						}
					}
					
					Calendar c = Calendar.getInstance();
					Calendar c2 = Calendar.getInstance();
					c2.add(Calendar.DAY_OF_MONTH, 30);
					
					q = gDAO.createQuery("Select e from VehicleAdHocMaintenanceRequest e where e.vehicle.partner=:partner and e.active=:active and (e.maintenance_dt > :start_date and e.maintenance_dt < :end_date)");
					q.setParameter("partner", getUser().getPartner());
					q.setParameter("active", true);
					q.setParameter("start_date", c.getTime());
					q.setParameter("end_date", c2.getTime());
					
					Object licsObj = gDAO.search(q, 0);
					if(licsObj != null)
					{
						Vector<VehicleAdHocMaintenanceRequest> lics = (Vector<VehicleAdHocMaintenanceRequest>)licsObj;
						for(VehicleAdHocMaintenanceRequest e : lics)
						{
							String[] str = new String[5];
							str[0] = "AdHoc";
							str[1] = v.getRegistrationNo();
							str[2] = "";
							str[3] = dtf.format(e.getMaintenance_dt());
							str[4] = e.getDescription();
							
							upcomingMaintenances.add(str);
						}
					}
				}
			}
		}
		return upcomingMaintenances;
	}

	public void setUpcomingMaintenances(Vector<String[]> upcomingMaintenances) {
		this.upcomingMaintenances = upcomingMaintenances;
	}

	private Calendar lastUpdatedDate = Calendar.getInstance();
	public PartnerDriver getBestDriver() {
		Calendar now = Calendar.getInstance();
		if(bestDriver == null || (now.get(Calendar.MONTH) != lastUpdatedDate.get(Calendar.MONTH)))
		{
			Vector<PartnerDriver> list = getBestDrivers();
			if(list != null && list.size() > 0)
				bestDriver = list.get(0);
		}
		return bestDriver;
	}

	public void setBestDriver(PartnerDriver bestDriver) {
		this.bestDriver = bestDriver;
	}

	@SuppressWarnings("unchecked")
	public Vector<PartnerDriver> getBestDrivers() {
		Calendar now = Calendar.getInstance();
		if(bestDrivers == null || (now.get(Calendar.MONTH) != lastUpdatedDate.get(Calendar.MONTH)))
		{
			now.add(Calendar.MONTH, -1);
			now.set(Calendar.HOUR_OF_DAY, now.getMinimum(Calendar.HOUR_OF_DAY));
			now.set(Calendar.MINUTE, now.getMinimum(Calendar.MINUTE));
			now.set(Calendar.SECOND, now.getMinimum(Calendar.SECOND));
			Date start_dt = now.getTime();
			now.add(Calendar.MONTH, 1);
			now.set(Calendar.HOUR_OF_DAY, now.getMaximum(Calendar.HOUR_OF_DAY));
			now.set(Calendar.MINUTE, now.getMaximum(Calendar.MINUTE));
			now.set(Calendar.SECOND, now.getMaximum(Calendar.SECOND));
			Date end_dt = now.getTime();
			
			GeneralDAO gDAO = new GeneralDAO();
			ReportDAO rDAO = new ReportDAO();
			Vector<String[]> list = rDAO.getBestDrivers(3, getUser().getPartner().getId().longValue(), start_dt, end_dt);
			if(list != null && list.size() > 0)
			{
				bestDrivers = new Vector<PartnerDriver>();
				for(String[] e : list)
				{
					try
					{
						Object pdObj = gDAO.find(PartnerDriver.class, Long.parseLong(e[0]));
						if(pdObj != null)
						{
							PartnerDriver pd = (PartnerDriver)pdObj;
							try
							{
								pd.setScore(Integer.parseInt(e[1]));
							} catch(Exception ex){}
							
							Hashtable<String, Object> param = new Hashtable<String, Object>();
							param.put("driver", pd);
							param.put("active", true);
							Object vdObj = gDAO.search("VehicleDriver", param);
							if(vdObj != null)
							{
								Vector<VehicleDriver> vd = (Vector<VehicleDriver>)vdObj;
								if(vd != null && vd.size() > 0)
									pd.setVehicle(vd.get(0).getVehicle());
							}
							
							bestDrivers.add(pd);
						}
					} catch(Exception ex){}
				}
			}
			gDAO.destroy();
		}
		
		return bestDrivers;
	}

	public void setBestDrivers(Vector<PartnerDriver> bestDrivers) {
		this.bestDrivers = bestDrivers;
	}

	@SuppressWarnings("unchecked")
	public Vector<PartnerDriver> getWorstDrivers() {
		Calendar now = Calendar.getInstance();
		if(worstDrivers == null || (now.get(Calendar.MONTH) != lastUpdatedDate.get(Calendar.MONTH)))
		{
			now.add(Calendar.MONTH, -1);
			now.set(Calendar.HOUR_OF_DAY, now.getMinimum(Calendar.HOUR_OF_DAY));
			now.set(Calendar.MINUTE, now.getMinimum(Calendar.MINUTE));
			now.set(Calendar.SECOND, now.getMinimum(Calendar.SECOND));
			Date start_dt = now.getTime();
			now.add(Calendar.MONTH, 1);
			now.set(Calendar.HOUR_OF_DAY, now.getMaximum(Calendar.HOUR_OF_DAY));
			now.set(Calendar.MINUTE, now.getMaximum(Calendar.MINUTE));
			now.set(Calendar.SECOND, now.getMaximum(Calendar.SECOND));
			Date end_dt = now.getTime();
			
			GeneralDAO gDAO = new GeneralDAO();
			ReportDAO rDAO = new ReportDAO();
			Vector<String[]> list = rDAO.getWorstDrivers(3, getUser().getPartner().getId().longValue(), start_dt, end_dt);
			if(list != null && list.size() > 0)
			{
				worstDrivers = new Vector<PartnerDriver>();
				for(String[] e : list)
				{
					try
					{
						Object pdObj = gDAO.find(PartnerDriver.class, Long.parseLong(e[0]));
						if(pdObj != null)
						{
							PartnerDriver pd = (PartnerDriver)pdObj;
							try
							{
								pd.setScore(Integer.parseInt(e[1]));
							} catch(Exception ex){}
							
							Hashtable<String, Object> param = new Hashtable<String, Object>();
							param.put("driver", pd);
							param.put("active", true);
							Object vdObj = gDAO.search("VehicleDriver", param);
							if(vdObj != null)
							{
								Vector<VehicleDriver> vd = (Vector<VehicleDriver>)vdObj;
								if(vd != null && vd.size() > 0)
									pd.setVehicle(vd.get(0).getVehicle());
							}
							
							worstDrivers.add(pd);
						}
					} catch(Exception ex){}
				}
			}
			gDAO.destroy();
		}
		return worstDrivers;
	}

	public void setWorstDrivers(Vector<PartnerDriver> worstDrivers) {
		this.worstDrivers = worstDrivers;
	}

	public VehicleTrackerData getMarkerTrackerData() {
		return markerTrackerData;
	}

	public void setMarkerTrackerData(VehicleTrackerData markerTrackerData) {
		this.markerTrackerData = markerTrackerData;
	}

	/*public MenuModel getMenuModel() {
		if(menuModel == null) {
			menuModel = new DefaultMenuModel();
			
			Submenu submenu = new DefaultSubMenu("More Modules");
			for(String m : otherModulesKeys) {
				Submenu submenu1 = new DefaultSubMenu(getModuleDisplayName(m));
				for(MFunction f : getModuleFunctions(m)) {
					DefaultMenuItem mitem = new DefaultMenuItem();
					mitem.setAjax(false);
					mitem.setValue(f.getName());
					mitem.setCommand("#{dashboardBean.gotoPage(" + f.getPage_url() + ", false)}");
					if(user != null)
						submenu1.getElements().add(mitem);
				}
				submenu.getElements().add(submenu1);
			}
			menuModel.getElements().add(submenu);
		}
		return menuModel;
	}

	public void setMenuModel(MenuModel menuModel) {
		this.menuModel = menuModel;
	}*/
}
