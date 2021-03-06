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
import com.dexter.fms.model.ApplicationType;
import com.dexter.fms.model.ApplicationTypeDash;
import com.dexter.fms.model.ApplicationTypeFunction;
import com.dexter.fms.model.ApplicationTypeModule;
import com.dexter.fms.model.ApplicationTypeReport;
import com.dexter.fms.model.ApplicationTypeVersion;
import com.dexter.fms.model.Audit;
import com.dexter.fms.model.MDash;
import com.dexter.fms.model.MFunction;
import com.dexter.fms.model.Module;
import com.dexter.fms.model.Report;

@ManagedBean(name = "appTypeBean")
@SessionScoped
public class AppTypesMBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	final Logger logger = Logger.getLogger("fms-AppTypesMBean");
	
	private FacesMessage msg = null;
	
	private ApplicationType appType;
	private ApplicationTypeVersion appTypeVersion;
	private ApplicationTypeModule appTypeModule;
	
	private Vector<ApplicationType> appTypes;
	private Vector<ApplicationTypeVersion> allAppTypeVersions;
	
	private Long module_id;
	private Module module;
	private Vector<Module> modules;
	private Vector<Report> reports;
	private Vector<MDash> dashs;
	
	private MFunction function;
	
	private Long appType_id, appTypeVersion_id;
	private Long subType_id;
	private Vector<ApplicationTypeModule> appTypeModules;
	private Vector<ApplicationTypeReport> appTypeReports;
	private Vector<ApplicationTypeDash> appTypeDashs;
	
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
		if(getFunction() != null) {
			setModule_id(getFunction().getModule().getId());
		}
	}
	
	@SuppressWarnings("unchecked")
	public String saveAppTypeVersionEdit()
	{
		if(getAppTypeVersion().getId() != null &&
				getAppTypeVersion().getVersionName() != null && getAppTypeVersion().getVersionName().trim().length() > 0) {
			String naration = "Editing application type version: " + getAppTypeVersion().getVersionName();
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			
			boolean ret = gDAO.update(getAppTypeVersion());
			if(ret)
			{
				for(Module m : getModules())
				{
					boolean exist = false;
					for(Module rf : getAppTypeVersion().getModules())
					{
						if(m.getId().longValue() == rf.getId().longValue())
						{
							exist = true;
							break;
						}
					}
					
					if(m.isSelected() && !exist)
					{
						ApplicationTypeModule atm = new ApplicationTypeModule();
						atm.setAppTypeVersion(getAppTypeVersion());
						atm.setModule(m);
						atm.setCrt_dt(new Date());
						atm.setCreatedBy(dashBean.getUser());
						
						gDAO.save(atm);
						
						for(MFunction f : m.getFunctions())
						{
							ApplicationTypeFunction atf = new ApplicationTypeFunction();
							atf.setAppTypeModule(atm);
							atf.setCreatedBy(dashBean.getUser());
							atf.setCrt_dt(new Date());
							atf.setFunction(f);
							gDAO.save(atf);
						}
					}
					else if(!m.isSelected() && exist)
					{
						// delete it here
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("module", m);
						params.put("appTypeVersion", getAppTypeVersion());
						Object rfObj = gDAO.search("ApplicationTypeModule", params);
						if(rfObj != null)
						{
							Vector<ApplicationTypeModule> rfList = (Vector<ApplicationTypeModule>)rfObj;

							for(ApplicationTypeModule e : rfList)
							{
								params = new Hashtable<String, Object>();
								params.put("appTypeModule", e);
								Object rfObj2 = gDAO.search("ApplicationTypeFunction", params);
								if(rfObj2 != null)
								{
									Vector<ApplicationTypeFunction> rfList2 = (Vector<ApplicationTypeFunction>)rfObj2;
									for(ApplicationTypeFunction ef : rfList2)
									{
										ret = gDAO.remove(ef);
									}
								}
								ret = gDAO.remove(e);
							}
						}
					}
				}
				
				for(Report r : getReports())
				{
					boolean exist = false;
					for(Report rf : getAppTypeVersion().getReports())
					{
						if(r.getId().longValue() == rf.getId().longValue())
						{
							exist = true;
							break;
						}
					}
					
					if(r.isSelected() && !exist)
					{
						ApplicationTypeReport atr = new ApplicationTypeReport();
						atr.setAppTypeVersion(getAppTypeVersion());
						atr.setReport(r);
						atr.setCrt_dt(new Date());
						atr.setCreatedBy(dashBean.getUser());
						
						gDAO.save(atr);
					}
					else if(!r.isSelected() && exist)
					{
						// delete it here
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("report", r);
						params.put("appTypeVersion", getAppTypeVersion());
						Object rfObj = gDAO.search("ApplicationTypeReport", params);
						if(rfObj != null)
						{
							Vector<ApplicationTypeReport> rfList = (Vector<ApplicationTypeReport>)rfObj;

							for(ApplicationTypeReport e : rfList)
							{
								gDAO.remove(e);
							}
						}
					}
				}
				
				for(MDash d : getDashs())
				{
					boolean exist = false;
					for(MDash rf : getAppTypeVersion().getDashs())
					{
						if(d.getId().longValue() == rf.getId().longValue())
						{
							exist = true;
							break;
						}
					}
					
					if(d.isSelected() && !exist)
					{
						ApplicationTypeDash atd = new ApplicationTypeDash();
						atd.setAppTypeVersion(getAppTypeVersion());
						atd.setDash(d);
						atd.setCrt_dt(new Date());
						atd.setCreatedBy(dashBean.getUser());
						
						gDAO.save(atd);
					}
					else if(!d.isSelected() && exist)
					{
						// delete it here
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("dash", d);
						params.put("appTypeVersion", getAppTypeVersion());
						Object rfObj = gDAO.search("ApplicationTypeDash", params);
						if(rfObj != null)
						{
							Vector<ApplicationTypeDash> rfList = (Vector<ApplicationTypeDash>)rfObj;

							for(ApplicationTypeDash e : rfList)
							{
								gDAO.remove(e);
							}
						}
					}
				}
				
				gDAO.commit();
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Application type version updated successfully!.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setDashs(null);
				setReports(null);
				setModules(null);
				setAppType(null);
				setAppTypeVersion(null);
				setAppTypes(null);
				setAllAppTypeVersions(null);
				
				naration += ", Status: Success";
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Create failed: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				naration += ", Status: Failed: " + gDAO.getMessage();
			}
			
			saveAudit(naration);
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		return "manage_app_types?faces-redirect=true";
	}
	
	public String initAppTypeVersionEdit()
	{
		if(getAppTypeVersion().getId() != null)
		{
			setAppType(getAppTypeVersion().getAppType());
			
			for(Module m : getModules())
			{
				boolean exist = false;
				Module aptm = null;
				for(Module atm : getAppTypeVersion().getModules())
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
				for(Report aptr : getAppTypeVersion().getReports())
				{
					if(aptr.getId().longValue() == r.getId().longValue())
					{
						r.setSelected(true);
						break;
					}
				}
			}
			
			for(MDash d : getDashs())
			{
				for(MDash aptd : getAppTypeVersion().getDashs())
				{
					if(aptd.getId().longValue() == d.getId().longValue())
					{
						d.setSelected(true);
						break;
					}
				}
			}
			
			return "edit_apptype?faces-redirect=true";
		}
		else
		{
			return "manage_app_types?faces-redirect=true";
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
			String naration = "Create new module: " + getModule().getName();
			
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
				
				naration += ", Status: Success";
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Create failed: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				naration += ", Status: Failed: " + gDAO.getMessage();
			}
			gDAO.destroy();
			
			saveAudit(naration);
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public String saveAppTypeVersion()
	{
		if(getAppType().getId() != null && getAppType().getName() != null && getAppType().getName().trim().length() > 0 &&
				getAppTypeVersion().getVersionName() != null && getAppTypeVersion().getVersionName().trim().length() > 0)
		{
			String naration = "Create new application type version: " + getAppType().getName() + " " + getAppTypeVersion().getVersionName();
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			
			getAppTypeVersion().setAppType(getAppType());
			getAppTypeVersion().setCreatedBy(dashBean.getUser());
			getAppTypeVersion().setCrt_dt(new Date());
			boolean ret = gDAO.save(getAppTypeVersion());
			if(ret)
			{
				for(Module m : getModules())
				{
					if(m.isSelected())
					{
						ApplicationTypeModule atm = new ApplicationTypeModule();
						atm.setAppTypeVersion(getAppTypeVersion());
						atm.setModule(m);
						atm.setCrt_dt(new Date());
						atm.setCreatedBy(dashBean.getUser());
						
						gDAO.save(atm);
						
						for(MFunction f : m.getFunctions())
						{
							//if(f.isSelected())
							//{
								ApplicationTypeFunction atf = new ApplicationTypeFunction();
								atf.setAppTypeModule(atm);
								atf.setCreatedBy(dashBean.getUser());
								atf.setCrt_dt(new Date());
								atf.setFunction(f);
								gDAO.save(atf);
							//}
						}
					}
				}
				
				for(Report r : getReports())
				{
					if(r.isSelected())
					{
						ApplicationTypeReport atr = new ApplicationTypeReport();
						atr.setAppTypeVersion(getAppTypeVersion());
						atr.setReport(r);
						atr.setCrt_dt(new Date());
						atr.setCreatedBy(dashBean.getUser());
						
						gDAO.save(atr);
					}
				}
				
				for(MDash d : getDashs())
				{
					if(d.isSelected())
					{
						ApplicationTypeDash atd = new ApplicationTypeDash();
						atd.setAppTypeVersion(getAppTypeVersion());
						atd.setDash(d);
						atd.setCrt_dt(new Date());
						atd.setCreatedBy(dashBean.getUser());
						
						gDAO.save(atd);
					}
				}
				
				gDAO.commit();
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Application type version created successfully!.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setDashs(null);
				setReports(null);
				setModules(null);
				setAppType(null);
				setAppTypeVersion(null);
				setAppTypes(null);
				setAllAppTypeVersions(null);
				
				naration += ", Status: Success";
				saveAudit(naration);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Create failed: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				naration += ", Status: Failed: " + gDAO.getMessage();
				saveAudit(naration);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		return "manage_app_types?faces-redirect=true";
	}
	
	public String saveAppType()
	{
		if(getAppType().getName() != null && getAppType().getName().trim().length() > 0)
		{
			String naration = "Create new app type: " + getAppType().getName() + ", " + getAppType().getDescription();
			getAppType().setCrt_dt(new Date());
			getAppType().setCreatedBy(dashBean.getUser());
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = gDAO.save(getAppType());
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Application type created successfully!.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setDashs(null);
				setReports(null);
				setModules(null);
				setAppType(null);
				setAppTypeVersion(null);
				setAppTypes(null);
				setAllAppTypeVersions(null);
				
				naration += ", Status: Success";
				/*getAppTypeVersion().setAppType(getAppType());
				getAppTypeVersion().setCreatedBy(dashBean.getUser());
				getAppTypeVersion().setCrt_dt(new Date());
				ret = gDAO.save(getAppTypeVersion());
				if(ret)
				{
					for(Module m : getModules())
					{
						if(m.isSelected())
						{
							ApplicationTypeModule atm = new ApplicationTypeModule();
							atm.setAppTypeVersion(getAppTypeVersion());
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
							atr.setAppTypeVersion(getAppTypeVersion());
							atr.setReport(r);
							atr.setCrt_dt(new Date());
							atr.setCreatedBy(dashBean.getUser());
							
							gDAO.save(atr);
						}
					}
					
					gDAO.commit();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Application type created successfully!.");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					
					setReports(null);
					setModules(null);
					setAppType(null);
					setAppTypeVersion(null);
					setAppTypes(null);
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Create failed: " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}*/
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Create failed: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				naration += ", Status: Failed: " + gDAO.getMessage();
			}
			gDAO.destroy();
			
			saveAudit(naration);
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		return "manage_app_types?faces-redirect=true";
	}

	public ApplicationType getAppType() {
		if(appType == null)
			appType = new ApplicationType();
		return appType;
	}

	public void setAppType(ApplicationType appType) {
		this.appType = appType;
	}

	public ApplicationTypeVersion getAppTypeVersion() {
		if(appTypeVersion == null)
			appTypeVersion = new ApplicationTypeVersion();
		return appTypeVersion;
	}

	public void setAppTypeVersion(ApplicationTypeVersion appTypeVersion) {
		this.appTypeVersion = appTypeVersion;
	}

	public ApplicationTypeModule getAppTypeModule() {
		if(appTypeModule == null)
			appTypeModule = new ApplicationTypeModule();
		return appTypeModule;
	}

	public void setAppTypeModule(ApplicationTypeModule appTypeModule) {
		this.appTypeModule = appTypeModule;
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
					Object appvobj = gDAO.search("ApplicationTypeVersion", params);
					if(appvobj != null)
					{
						Vector<ApplicationTypeVersion> appvList = (Vector<ApplicationTypeVersion>)appvobj;
						e.setVersions(appvList);
					}
					if(e.getVersions() != null && e.getVersions().size() > 0)
					{
						for(ApplicationTypeVersion atv : e.getVersions())
						{
							params = new Hashtable<String, Object>();
							params.put("appTypeVersion", atv);
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
								atv.setModules(eModules);
							}
							
							params = new Hashtable<String, Object>();
							params.put("appTypeVersion", atv);
							Object robj = gDAO.search("ApplicationTypeReport", params);
							if(robj != null)
							{
								Vector<ApplicationTypeReport> atrList = (Vector<ApplicationTypeReport>)robj;
								for(ApplicationTypeReport atr : atrList)
								{
									atv.getReports().add(atr.getReport());
								}
							}
							
							params = new Hashtable<String, Object>();
							params.put("appTypeVersion", atv);
							Object dobj = gDAO.search("ApplicationTypeDash", params);
							if(dobj != null)
							{
								Vector<ApplicationTypeDash> atdList = (Vector<ApplicationTypeDash>)dobj;
								for(ApplicationTypeDash atd : atdList)
								{
									atv.getDashs().add(atd.getDash());
								}
							}
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

	public Vector<ApplicationTypeVersion> getAllAppTypeVersions() {
		if(allAppTypeVersions == null)
		{
			Vector<ApplicationType> appTypes = getAppTypes();
			if(appTypes != null && appTypes.size() > 0)
			{
				allAppTypeVersions = new Vector<ApplicationTypeVersion>();
				for(ApplicationType e : appTypes)
				{
					allAppTypeVersions.addAll(e.getVersions());
				}
			}
		}
		return allAppTypeVersions;
	}

	public void setAllAppTypeVersions(
			Vector<ApplicationTypeVersion> allAppTypeVersions) {
		this.allAppTypeVersions = allAppTypeVersions;
	}

	@SuppressWarnings("unchecked")
	public Vector<ApplicationTypeModule> getAppTypeModules() {
		if(appTypeModules == null || appTypeModules.size() == 0)
		{
			if(getAppTypeVersion_id() != null)
			{
				appTypeModules = new Vector<ApplicationTypeModule>();
				
				GeneralDAO gDAO = new GeneralDAO();
				Object atObj = gDAO.find(ApplicationTypeVersion.class, getAppTypeVersion_id());
				if(atObj != null)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("appTypeVersion", (ApplicationTypeVersion)atObj);
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
			if(getAppTypeVersion_id() != null)
			{
				appTypeReports = new Vector<ApplicationTypeReport>();
				
				GeneralDAO gDAO = new GeneralDAO();
				Object atObj = gDAO.find(ApplicationTypeVersion.class, getAppTypeVersion_id());
				if(atObj != null)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("appTypeVersion", (ApplicationTypeVersion)atObj);
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
	public Vector<ApplicationTypeDash> getAppTypeDashs() {
		if(appTypeDashs == null || appTypeDashs.size() == 0)
		{
			if(getAppTypeVersion_id() != null)
			{
				appTypeDashs = new Vector<ApplicationTypeDash>();
				
				GeneralDAO gDAO = new GeneralDAO();
				Object atObj = gDAO.find(ApplicationTypeVersion.class, getAppTypeVersion_id());
				if(atObj != null)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("appTypeVersion", (ApplicationTypeVersion)atObj);
					Object mobj = gDAO.search("ApplicationTypeDash", params);
					if(mobj != null)
					{
						appTypeDashs = (Vector<ApplicationTypeDash>)mobj;
					}
				}
			}
		}
		return appTypeDashs;
	}

	public void setAppTypeDashs(Vector<ApplicationTypeDash> appTypeDashs) {
		this.appTypeDashs = appTypeDashs;
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

	@SuppressWarnings("unchecked")
	public Vector<MDash> getDashs() {
		if(dashs == null)
		{
			Object obj = new GeneralDAO().findAll("MDash");
			if(obj != null)
			{
				dashs = (Vector<MDash>)obj;
			}
		}
		return dashs;
	}

	public void setDashs(Vector<MDash> dashs) {
		this.dashs = dashs;
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

	public Long getAppTypeVersion_id() {
		return appTypeVersion_id;
	}

	public void setAppTypeVersion_id(Long appTypeVersion_id) {
		this.appTypeVersion_id = appTypeVersion_id;
	}

	public Long getSubType_id() {
		return subType_id;
	}

	public void setSubType_id(Long subType_id) {
		this.subType_id = subType_id;
	}
}
