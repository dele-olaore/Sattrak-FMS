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

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.ApplicationSubscriptionModule;
import com.dexter.fms.model.ApplicationType;
import com.dexter.fms.model.ApplicationTypeModule;
import com.dexter.fms.model.MFunction;
import com.dexter.fms.model.Module;
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
	
	private Vector<Module> modules;
	
	private Long appType_id;
	private Long subType_id;
	private Vector<ApplicationTypeModule> appTypeModules;
	private SubscriptionPackage subPackage;
	private Vector<SubscriptionPackage> subPackages;
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public AppTypesMBean()
	{}
	
	public void selectAllATModules(boolean b)
	{
		for(Module e : getModules())
		{
			e.setSelected(b);
		}
	}
	
	public void selectAllModules(boolean b)
	{
		for(ApplicationTypeModule e : getAppTypeModules())
		{
			e.setSelected(b);
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
							eModules.add(atm.getModule());
						}
						e.setModules(eModules);
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
				}
			}
		}
		return subPackages;
	}

	public void setSubPackages(Vector<SubscriptionPackage> subPackages) {
		this.subPackages = subPackages;
	}
	
}
