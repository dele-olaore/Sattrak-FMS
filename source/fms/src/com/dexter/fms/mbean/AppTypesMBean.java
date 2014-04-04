package com.dexter.fms.mbean;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.RowEditEvent;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.ApplicationSubscriptionModule;
import com.dexter.fms.model.ApplicationSubscriptionReport;
import com.dexter.fms.model.ApplicationType;
import com.dexter.fms.model.ApplicationTypeFunction;
import com.dexter.fms.model.ApplicationTypeModule;
import com.dexter.fms.model.ApplicationTypeReport;
import com.dexter.fms.model.Audit;
import com.dexter.fms.model.MFunction;
import com.dexter.fms.model.Module;
import com.dexter.fms.model.Report;
import com.dexter.fms.model.SubscriptionPackage;
import com.dexter.fms.model.SubscriptionType;

@ManagedBean(name = "appTypeBean")
@SessionScoped
public class AppTypesMBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	final Logger logger = Logger.getLogger("fms-AppTypesMBean");
	
	private FacesMessage msg = null;
	
	private ApplicationType appType;
	private ApplicationTypeModule appTypeModule;
	
	private SubscriptionType subType;
	
	private Vector<ApplicationType> appTypes;
	private Vector<SubscriptionType> subTypes;
	
	private Long module_id;
	private Module module;
	private Vector<Module> modules;
	private Vector<Report> reports;
	
	private MFunction function;
	
	private Long appType_id;
	private Long subType_id;
	private Vector<ApplicationTypeModule> appTypeModules;
	private Vector<ApplicationTypeReport> appTypeReports;
	private SubscriptionPackage subPackage;
	private Vector<SubscriptionPackage> subPackages;
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public AppTypesMBean()
	{}
	
	private void saveAudit(String narration)
	{
		Audit audit = new Audit();
		audit.setAction_dt(new Date());
		audit.setUser(dashBean.getUser());
		audit.setNarration(narration);
		
		GeneralDAO gDAO = new GeneralDAO();
		gDAO.startTransaction();
		if(gDAO.save(audit))
			gDAO.commit();
		else
			gDAO.rollback();
		gDAO.destroy();
	}
	
	public void saveEditFunction()
	{
		if(getFunction() != null && getModule_id() != null)
		{
			String status = "";
			Module m = null;
			for(Module e : getModules())
			{
				if(e.getId().longValue() == getModule_id().longValue())
				{
					m = e;
					break;
				}
			}
			
			String narration = "Change module for function '" + getFunction().getName() + "' from '" + getFunction().getModule().getName() + "' to ";
			
			if(m != null)
			{
				narration += "'" + m.getName() + "'";
				
				getFunction().setModule(m);
				GeneralDAO gDAO = new GeneralDAO();
				gDAO.startTransaction();
				boolean ret = gDAO.update(getFunction());
				if(ret)
				{
					gDAO.commit();
					status = "Success";
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Entity updated successfully.");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					
					setFunction(null);
					setModule_id(null);
					setModules(null);
				}
				else
				{
					gDAO.rollback();
					status = "Failed";
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to update entity. " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				gDAO.destroy();
			}
			else
			{
				narration += "UNKNOWN-MODULE";
				status = "Error";
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Unknown module selected!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			
			saveAudit(narration + " Status: " + status);
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void initFunctionModuleChange()
	{
		if(getFunction() != null)
		{
			setModule_id(getFunction().getModule().getId());
		}
	}
	
	public String initAppTypeEdit()
	{
		if(getAppType().getId() != null)
		{
			for(Module m : getModules())
			{
				boolean exist = false;
				Module aptm = null;
				for(Module atm : getAppType().getModules())
				{
					if(atm.getId().longValue() == m.getId().longValue())
					{
						exist = true;
						aptm = atm;
						break;
					}
				}
				if(exist)
				{
					m.setSelected(true);
					Vector<MFunction> mfList = m.getFunctions();
					for(MFunction mf : mfList)
					{
						for(MFunction atf : aptm.getFunctions())
						{
							if(atf.getId().longValue() == mf.getId().longValue())
							{
								mf.setSelected(true);
								break;
							}
						}
					}
				}
			}
			
			for(Report r : getReports())
			{
				for(Report aptr : getAppType().getReports())
				{
					if(aptr.getId().longValue() == r.getId().longValue())
					{
						r.setSelected(true);
						break;
					}
				}
			}
			return "edit_apptype";
		}
		else
		{
			return "manage_app_types";
		}
	}
	
	public void onEdit(RowEditEvent event)
	{
		GeneralDAO gDAO = new GeneralDAO();
		boolean ret = false;
		Object eventSource = event.getObject();
		
		gDAO.startTransaction();
		ret = gDAO.update(eventSource);
		
		if(ret)
		{
			gDAO.commit();
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Entity updated successfully.");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		else
		{
			gDAO.rollback();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to update entity. " + gDAO.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		gDAO.destroy();
	}
	
	public void onCancel(RowEditEvent event)
	{
		msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Update canceled!");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void updateModulesFunctionsChecked(Long id)
	{
		for(Module m : getModules())
		{
			if(m.getId().longValue() == id.longValue())
			{
				for(MFunction f : m.getFunctions())
				{
					f.setSelected(m.isSelected());
				}
				break;
			}
		}
	}
	
	public void selectAllATModules(boolean b)
	{
		for(Module e : getModules())
		{
			e.setSelected(b);
			updateModulesFunctionsChecked(e.getId());
		}
	}
	
	public void selectAllModules(boolean b)
	{
		for(ApplicationTypeModule e : getAppTypeModules())
		{
			e.setSelected(b);
		}
	}
	
	public void saveModule()
	{
		if(getModule().getName() != null && getModule().getDisplay_name() != null)
		{
			getModule().setActive(true);
			getModule().setCrt_dt(new Date());
			getModule().setCreatedBy(dashBean.getUser());
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = gDAO.save(getModule());
			if(ret)
			{
				gDAO.commit();
				setModule(null);
				setModules(null);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Module created successfully!.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Create failed: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public String saveAppType()
	{
		if(getAppType().getName() != null)
		{
			getAppType().setCrt_dt(new Date());
			getAppType().setCreatedBy(dashBean.getUser());
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = gDAO.save(getAppType());
			if(ret)
			{
				for(Module m : getModules())
				{
					if(m.isSelected())
					{
						ApplicationTypeModule atm = new ApplicationTypeModule();
						atm.setAppType(getAppType());
						atm.setModule(m);
						atm.setCrt_dt(new Date());
						atm.setCreatedBy(dashBean.getUser());
						
						gDAO.save(atm);
						
						for(MFunction f : m.getFunctions())
						{
							if(f.isSelected())
							{
								ApplicationTypeFunction atf = new ApplicationTypeFunction();
								atf.setAppTypeModule(atm);
								atf.setCreatedBy(dashBean.getUser());
								atf.setCrt_dt(new Date());
								atf.setFunction(f);
								gDAO.save(atf);
							}
						}
					}
				}
				
				for(Report r : getReports())
				{
					if(r.isSelected())
					{
						ApplicationTypeReport atr = new ApplicationTypeReport();
						atr.setAppType(getAppType());
						atr.setReport(r);
						atr.setCrt_dt(new Date());
						atr.setCreatedBy(dashBean.getUser());
						
						gDAO.save(atr);
					}
				}
				
				setModules(null);
				setAppType(null);
				setAppTypes(null);
				gDAO.commit();
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Application type created successfully!.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Create failed: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		
		return "manage_app_types";
	}
	
	public String saveSubType()
	{
		if(getSubType().getName() != null)
		{
			getSubType().setCrt_dt(new Date());
			getSubType().setCreatedBy(dashBean.getUser());
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = gDAO.save(getSubType());
			if(ret)
			{
				setSubType(null);
				setSubTypes(null);
				gDAO.commit();
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Subscription type created successfully!.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Create failed: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		return "manage_app_types";
	}
	
	public void savePackage()
	{
		if(getAppType_id() != null && getSubType_id() != null)
		{
			SubscriptionPackage subPackage = new SubscriptionPackage();
			
			GeneralDAO gDAO = new GeneralDAO();
			Object atObj = gDAO.find(ApplicationType.class, getAppType_id());
			Object sbObj = gDAO.find(SubscriptionType.class, getSubType_id());
			if(atObj != null && sbObj != null)
			{
				subPackage.setAppType((ApplicationType)atObj);
				subPackage.setSubType((SubscriptionType)sbObj);
				
				subPackage.setCreatedBy(dashBean.getUser());
				subPackage.setCrt_dt(new Date());
				subPackage.setName(subPackage.getAppType().getName() + "-" + subPackage.getSubType().getName());
				
				gDAO.startTransaction();
				
				boolean ret = gDAO.save(subPackage);
				if(ret)
				{
					for(ApplicationTypeModule atm : appTypeModules)
					{
						if(atm.isSelected())
						{
							ApplicationSubscriptionModule sub = new ApplicationSubscriptionModule();
							sub.setAppTypeModule(atm);
							sub.setCrt_dt(new Date());
							sub.setSubType(subPackage.getSubType());
							
							ret = gDAO.save(sub);
							if(!ret)
								break;
						}
					}
					
					if(!ret)
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Create failed for package: " + gDAO.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else
					{
						gDAO.commit();
						setSubPackages(null);
						setAppType_id(null);
						setSubType_id(null);
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Package created successfully.");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Create failed for package: " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
		}
	}

	public ApplicationType getAppType() {
		if(appType == null)
			appType = new ApplicationType();
		return appType;
	}

	public void setAppType(ApplicationType appType) {
		this.appType = appType;
	}

	public ApplicationTypeModule getAppTypeModule() {
		if(appTypeModule == null)
			appTypeModule = new ApplicationTypeModule();
		return appTypeModule;
	}

	public void setAppTypeModule(ApplicationTypeModule appTypeModule) {
		this.appTypeModule = appTypeModule;
	}

	public SubscriptionType getSubType() {
		if(subType == null)
			subType = new SubscriptionType();
		return subType;
	}

	public void setSubType(SubscriptionType subType) {
		this.subType = subType;
	}

	@SuppressWarnings("unchecked")
	public Vector<ApplicationType> getAppTypes() {
		if(appTypes == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Object obj = gDAO.findAll("ApplicationType");
			if(obj != null)
			{
				appTypes = (Vector<ApplicationType>)obj;
			}
			if(appTypes != null)
			{
				for(ApplicationType e : appTypes)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("appType", e);
					Object mobj = gDAO.search("ApplicationTypeModule", params);
					if(mobj != null)
					{
						Vector<ApplicationTypeModule> atmlist = (Vector<ApplicationTypeModule>) mobj;
						Vector<Module> eModules = new Vector<Module>();
						for(ApplicationTypeModule atm : atmlist)
						{
							Module mdule = atm.getModule();
							
							params = new Hashtable<String, Object>();
							params.put("appTypeModule", atm);
							Object fobj = gDAO.search("ApplicationTypeFunction", params);
							if(fobj != null)
							{
								Vector<MFunction> mfunctions = new Vector<MFunction>();
								Vector<ApplicationTypeFunction> atflist = (Vector<ApplicationTypeFunction>) fobj;
								for(ApplicationTypeFunction atf : atflist)
								{
									mfunctions.add(atf.getFunction());
								}
								mdule.setFunctions(mfunctions);
							}
							eModules.add(mdule);
						}
						e.setModules(eModules);
					}
					
					params = new Hashtable<String, Object>();
					params.put("appType", e);
					Object robj = gDAO.search("ApplicationTypeReport", params);
					if(robj != null)
					{
						Vector<ApplicationTypeReport> atrList = (Vector<ApplicationTypeReport>)robj;
						for(ApplicationTypeReport atr : atrList)
						{
							e.getReports().add(atr.getReport());
						}
					}
				}
			}
		}
		return appTypes;
	}

	public void setAppTypes(Vector<ApplicationType> appTypes) {
		this.appTypes = appTypes;
	}

	@SuppressWarnings("unchecked")
	public Vector<ApplicationTypeModule> getAppTypeModules() {
		if(appTypeModules == null || appTypeModules.size() == 0)
		{
			if(getAppType_id() != null)
			{
				appTypeModules = new Vector<ApplicationTypeModule>();
				
				GeneralDAO gDAO = new GeneralDAO();
				Object atObj = gDAO.find(ApplicationType.class, getAppType_id());
				if(atObj != null)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("appType", (ApplicationType)atObj);
					Object mobj = gDAO.search("ApplicationTypeModule", params);
					if(mobj != null)
					{
						appTypeModules = (Vector<ApplicationTypeModule>)mobj;
					}
				}
			}
		}
		return appTypeModules;
	}

	public void setAppTypeModules(Vector<ApplicationTypeModule> appTypeModules) {
		this.appTypeModules = appTypeModules;
	}

	@SuppressWarnings("unchecked")
	public Vector<ApplicationTypeReport> getAppTypeReports() {
		if(appTypeReports == null || appTypeReports.size() == 0)
		{
			if(getAppType_id() != null)
			{
				appTypeReports = new Vector<ApplicationTypeReport>();
				
				GeneralDAO gDAO = new GeneralDAO();
				Object atObj = gDAO.find(ApplicationType.class, getAppType_id());
				if(atObj != null)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("appType", (ApplicationType)atObj);
					Object mobj = gDAO.search("ApplicationTypeReport", params);
					if(mobj != null)
					{
						appTypeReports = (Vector<ApplicationTypeReport>)mobj;
					}
				}
			}
		}
		return appTypeReports;
	}

	public void setAppTypeReports(Vector<ApplicationTypeReport> appTypeReports) {
		this.appTypeReports = appTypeReports;
	}

	@SuppressWarnings("unchecked")
	public Vector<SubscriptionType> getSubTypes() {
		if(subTypes == null)
		{
			Object obj = new GeneralDAO().findAll("SubscriptionType");
			if(obj != null)
			{
				subTypes = (Vector<SubscriptionType>)obj;
			}
		}
		return subTypes;
	}

	public void setSubTypes(Vector<SubscriptionType> subTypes) {
		this.subTypes = subTypes;
	}

	public Long getModule_id() {
		return module_id;
	}

	public void setModule_id(Long module_id) {
		this.module_id = module_id;
	}

	public Module getModule() {
		if(module == null)
			module = new Module();
		return module;
	}

	public void setModule(Module module) {
		this.module = module;
	}

	@SuppressWarnings("unchecked")
	public Vector<Module> getModules() {
		if(modules == null)
		{
			Object obj = new GeneralDAO().findAll("Module");
			if(obj != null)
			{
				modules = (Vector<Module>)obj;
				for(Module m : modules)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("module", (Module)m);
					Object mobj = new GeneralDAO().search("MFunction", params);
					if(mobj != null)
					{
						m.setFunctions((Vector<MFunction>)mobj);
					}
				}
			}
		}
		return modules;
	}

	public void setModules(Vector<Module> modules) {
		this.modules = modules;
	}

	@SuppressWarnings("unchecked")
	public Vector<Report> getReports() {
		if(reports == null)
		{
			Object obj = new GeneralDAO().findAll("Report");
			if(obj != null)
			{
				reports = (Vector<Report>)obj;
			}
		}
		return reports;
	}

	public void setReports(Vector<Report> reports) {
		this.reports = reports;
	}

	public MFunction getFunction() {
		return function;
	}

	public void setFunction(MFunction function) {
		this.function = function;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}

	public Long getAppType_id() {
		return appType_id;
	}

	public void setAppType_id(Long appType_id) {
		this.appType_id = appType_id;
	}

	public Long getSubType_id() {
		return subType_id;
	}

	public void setSubType_id(Long subType_id) {
		this.subType_id = subType_id;
	}

	public SubscriptionPackage getSubPackage() {
		if(subPackage == null)
			subPackage = new SubscriptionPackage();
		return subPackage;
	}

	public void setSubPackage(SubscriptionPackage subPackage) {
		this.subPackage = subPackage;
	}

	@SuppressWarnings("unchecked")
	public Vector<SubscriptionPackage> getSubPackages() {
		if(subPackages == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Object obj = gDAO.findAll("SubscriptionPackage");
			if(obj != null)
			{
				subPackages = (Vector<SubscriptionPackage>)obj;
			}
			if(subPackages != null)
			{
				for(SubscriptionPackage p : subPackages)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("subType", p.getSubType());
					params.put("appTypeModule.appType", p.getAppType());
					Object asmobj = gDAO.search("ApplicationSubscriptionModule", params);
					if(asmobj != null)
					{
						p.setModules((Vector<ApplicationSubscriptionModule>)asmobj);
					}
					
					params = new Hashtable<String, Object>();
					params.put("subType", p.getSubType());
					params.put("appTypeReport.appType", p.getAppType());
					Object asrobj = gDAO.search("ApplicationSubscriptionReport", params);
					if(asrobj != null)
					{
						p.setReports((Vector<ApplicationSubscriptionReport>)asrobj);
					}
				}
			}
		}
		return subPackages;
	}

	public void setSubPackages(Vector<SubscriptionPackage> subPackages) {
		this.subPackages = subPackages;
	}
	
}
