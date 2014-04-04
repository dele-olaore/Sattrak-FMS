package com.dexter.fms.mbean;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

import com.dexter.common.util.Hasher;
import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.MFunction;
import com.dexter.fms.model.MRoleFunction;
import com.dexter.fms.model.MRoleReport;
import com.dexter.fms.model.Module;
import com.dexter.fms.model.PartnerSubscription;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.PartnerUserRole;
import com.dexter.fms.model.PartnerUserSetting;
import com.dexter.fms.model.Report;
import com.dexter.fms.model.app.DashboardVehicle;
import com.dexter.fms.model.app.DriverLicense;
import com.dexter.fms.model.app.Expense;
import com.dexter.fms.model.app.ExpenseType;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleAdHocMaintenanceRequest;
import com.dexter.fms.model.app.VehicleLicense;
import com.dexter.fms.model.app.VehicleLocationData;
import com.dexter.fms.model.app.VehicleRoutineMaintenanceSetup;

@ManagedBean(name = "dashboardBean")
@SessionScoped
public class DashboardMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-DashboardMBean");
	
	private FacesMessage msg = null;
	
	private PartnerSubscription subscription;
	private PartnerUser user;
	private Vector<PartnerUserRole> userRoles;
	private Vector<MRoleFunction> rolesFunctions;
	private Vector<MRoleReport> rolesReports;
	
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
	private CartesianChartModel expsLinearModel;
	
	private Vector<String[]> upcomingAndRecentExpirations;
	private Vector<String[]> upcomingMaintenances;
	
	public DashboardMBean()
	{
		model = new DefaultDashboardModel();
		
		DashboardColumn column1 = new DefaultDashboardColumn();
		
        column1.addWidget("trackv");
		column1.addWidget("duelics");
		column1.addWidget("duemaints");
        column1.addWidget("recentexpenses");
        column1.addWidget("bookings");
        
        model.addColumn(column1);
        
        vtrackpollinterval = 120;
        defaultCenterCoor = "6.427887,3.4287645";
        centerCoor = defaultCenterCoor;
        
        expsBack = expsBackTypes[0];
        
        theme = "aristo";
        headercolor = "header_main";
	}
	
	@SuppressWarnings("unchecked")
	public void updateTheme()
	{
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
	        {
	            //Logger.getLogger(getClass().getName(), null).log(Level.SEVERE, null, ex);
	        }
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
		 {
			 //Logger.getLogger(getClass().getName(), null).log(Level.SEVERE, null, ex);
		 }
	 }

	public String gotoPage(String page, boolean subFunction)
	{
		if(!subFunction)
			function_page = page;
		
		//HttpServletRequest hs = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		//FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		//String gotoPage = hs.getContextPath() + "/faces/" + page + ".xhtml";
		//redirector(gotoPage);
		
		return page;
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
						
						ret = "dashboard";
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

	public PartnerUser getUser() {
		return user;
	}

	public void setUser(PartnerUser user) {
		this.user = user;
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
					if(modulesKeys.size() == 8) // maximum 8 main modules, so add to other modules
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

	public MapModel getVtrackingModel() {
		vtrackingModel = new DefaultMapModel();
		
		for(VehicleLocationData vld : getDashVehiclesLocs())
		{
			try
			{
				LatLng coord1 = new LatLng(vld.getLat(), vld.getLon());
				//Basic marker
				vtrackingModel.addOverlay(new Marker(coord1, vld.getVehicle().getRegistrationNo()));
			}
			catch(Exception ex)
			{}
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
		setSelectedDashVehicle(null);
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
	
	public void resetExpLicenses()
	{
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
			c.add(Calendar.DAY_OF_MONTH, -30);
			
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
					String[] str = new String[5];
					str[0] = "Vehicle License";
					str[1] = e.getVehicle().getRegistrationNo();
					str[2] = e.getLicType().getName() + " | " + e.getSubLicType();
					str[3] = dtf.format(e.getLic_end_dt());
					str[4] = "Expired";
					
					upcomingAndRecentExpirations.add(str);
				}
			}
			
			c = Calendar.getInstance();
			c2 = Calendar.getInstance();
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
					String[] str = new String[5];
					str[0] = "Vehicle License";
					str[1] = e.getVehicle().getRegistrationNo();
					str[2] = e.getLicType().getName() + " | " + e.getSubLicType();
					str[3] = dtf.format(e.getLic_end_dt());
					str[4] = "Soon to Expire";
					
					upcomingAndRecentExpirations.add(str);
				}
			}
			
			c = Calendar.getInstance();
			c2 = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, -30);
			
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
					String[] str = new String[5];
					str[0] = "Driver's License";
					str[1] = e.getDriver().getPersonel().getFirstname() + " " + e.getDriver().getPersonel().getLastname();
					str[2] = "Driver's License";
					str[3] = dtf.format(e.getLic_end_dt());
					str[4] = "Expired";
					
					upcomingAndRecentExpirations.add(str);
				}
			}
			
			c = Calendar.getInstance();
			c2 = Calendar.getInstance();
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
					String[] str = new String[5];
					str[0] = "Vehicle License";
					str[1] = e.getDriver().getPersonel().getFirstname() + " " + e.getDriver().getPersonel().getLastname();
					str[2] = "Driver's License";
					str[3] = dtf.format(e.getLic_end_dt());
					str[4] = "Soon to Expire";
					
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
					// TODO: Get the current odometer reading
					Query q = gDAO.createQuery("Select MAX(e.odometer) from VehicleOdometerData e where e.vehicle=:vehicle");
					q.setParameter("vehicle", v);
					
					Object ret = gDAO.search(q, 1);
					if(ret != null)
					{
						try
						{
							currentOdometer = (Double)ret;
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
						}
					}
					
					if(currentOdometer > 0)
					{
						// TODO: Get the current routine setup that is still active, that means its not done
						params = new Hashtable<String, Object>();
						params.put("vehicle", v);
						params.put("active", true);
						
						Object maintsObj = gDAO.search("VehicleRoutineMaintenanceSetup", params);
						if(maintsObj != null)
						{
							Vector<VehicleRoutineMaintenanceSetup> maints = (Vector<VehicleRoutineMaintenanceSetup>)maintsObj;
							for(VehicleRoutineMaintenanceSetup vms : maints)
							{
								if(vms.getAlert_odometer().doubleValue() < currentOdometer)
								{
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
	
}
