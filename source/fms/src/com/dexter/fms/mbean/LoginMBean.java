package com.dexter.fms.mbean;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import com.dexter.common.util.Hasher;
import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.Audit;
import com.dexter.fms.model.MDashRole;
import com.dexter.fms.model.MRoleFunction;
import com.dexter.fms.model.MRoleReport;
import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerLicense;
import com.dexter.fms.model.PartnerSubscription;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.PartnerUserRole;
import com.dexter.fms.model.PartnerUserSetting;

@ManagedBean(name = "loginBean")
@RequestScoped
public class LoginMBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	final Logger logger = Logger.getLogger("fms-LoginMBean");
	
	private FacesMessage msg = null;
	
	private String username;
	private String password;
	private String partner_code;
	private boolean autologin;
	
	@ManagedProperty("#{appBean}")
	ApplicationMBean appBean;
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public LoginMBean()
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
	
	public String logout()
	{
		String ret = "index?faces-redirect=true";
		
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		
		msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Log out successful.");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public String login()
	{
		String ret = "index";
		
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("username", getUsername());
		params.put("partner_code", getPartner_code());
		
		Object foundUsers = gDAO.search("PartnerUser", params);
		if(foundUsers != null)
		{
			Vector<PartnerUser> list = (Vector<PartnerUser>)foundUsers;
			if(list.size()>0)
			{
				PartnerUser foundUser = list.get(0);
				System.out.println("Found password: " + foundUser.getPassword() + ", Entered password: " + Hasher.getHashValue(getPassword()));
				if(foundUser.getPassword().equals(Hasher.getHashValue(getPassword())))
				{
					foundUser.setSession(((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getSession());
					boolean r = appBean.addLoggedInUser(foundUser);
					if(!r)
					{
						saveAudit("LOGIN: Duplicate login detected for user: " + getUsername() + ", Partner: " + foundUser.getPartner().getName());
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Authentication successful! However, you are already logged in on a different instance.");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else
					{
						((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getSession().setAttribute("loggedIn", true);
						dashBean.setUser(foundUser);
						boolean proceed = true;
						if(!foundUser.getPartner().isSattrak())
						{
							// This user is not a sattrak user, so we check if its partner has a valid subscription
							params = new Hashtable<String, Object>();
							params.put("partner", foundUser.getPartner());
							params.put("active", true);
							params.put("expired", false);
							Object foundSubs = gDAO.search("PartnerSubscription", params);
							if(foundSubs != null)
							{
								Vector<PartnerSubscription> subs = (Vector<PartnerSubscription>)foundSubs;
								if(subs.size() > 0)
								{
									for(PartnerSubscription sub : subs)
									{
										dashBean.setSubscription(sub);
									}
								}
								else
								{
									dashBean.setUser(null);
									proceed = false;
									msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Authentication successful but no active subscription!");
									FacesContext.getCurrentInstance().addMessage(null, msg);
								}
							}
							else
							{
								dashBean.setUser(null);
								proceed = false;
								msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Authentication successful but no active subscription!");
								FacesContext.getCurrentInstance().addMessage(null, msg);
							}
						}
						if(proceed)
						{
							Hashtable<String, Object> params2 = new Hashtable<String, Object>();
							params2.put("user", foundUser);
							Object foundRoles = gDAO.search("PartnerUserRole", params2);
							if(foundRoles != null)
								dashBean.setUserRoles((Vector<PartnerUserRole>)foundRoles);
							
							for(PartnerUserRole pur : dashBean.getUserRoles())
							{
								Query q = gDAO.createQuery("Select e from MRoleFunction e where e.role = :role and e.function.active=true order by e.function.displayIndex, e.function.name");
								q.setParameter("role", pur.getRole());
								//Hashtable<String, Object> params3 = new Hashtable<String, Object>();
								//params3.put("role", pur.getRole());
								Object foundRoleFunctions = gDAO.search(q, 0); //new GeneralDAO().search("MRoleFunction", params3);
								if(foundRoleFunctions != null)
								{
									Vector<MRoleFunction> rflist = (Vector<MRoleFunction>)foundRoleFunctions;
									if(dashBean.getRolesFunctions() == null)
										dashBean.setRolesFunctions(rflist);
									else
										dashBean.getRolesFunctions().addAll(rflist);
								}
								
								Hashtable<String, Object> params3 = new Hashtable<String, Object>();
								params3.put("role", pur.getRole());
								Object foundRoleReports = gDAO.search("MRoleReport", params3);
								if(foundRoleReports != null)
								{
									Vector<MRoleReport> rflist = (Vector<MRoleReport>)foundRoleReports;
									if(dashBean.getRolesReports() == null)
										dashBean.setRolesReports(rflist);
									else
										dashBean.getRolesReports().addAll(rflist);
								}
								
								Hashtable<String, Object> params4 = new Hashtable<String, Object>();
								params4.put("role", pur.getRole());
								params4.put("dash.active", true);
								Object foundRoleDashs = gDAO.search("MDashRole", params4);
								if(foundRoleDashs != null)
								{
									Vector<MDashRole> rflist = (Vector<MDashRole>)foundRoleDashs;
									if(dashBean.getRolesDashs() == null)
										dashBean.setRolesDashs(rflist);
									else
										dashBean.getRolesDashs().addAll(rflist);
									
									dashBean.updateDashsToShow();
								}
							}
							
							Hashtable<String, Object> paramsSetting = new Hashtable<String, Object>();
							paramsSetting.put("user", foundUser);
							Object foundSetting = gDAO.search("PartnerUserSetting", paramsSetting);
							if(foundSetting != null)
							{
								Vector<PartnerUserSetting> uslist = (Vector<PartnerUserSetting>)foundSetting;
								for(PartnerUserSetting e : uslist)
								{
									dashBean.setTheme(e.getTheme());
									dashBean.setHeadercolor(e.getHeadercolor());
								}
							}
							
							params2 = new Hashtable<String, Object>();
							params2.put("partner", foundUser.getPartner());
							Object foundLics = gDAO.search("PartnerLicense", params2);
							if(foundLics != null)
							{
								Vector<PartnerLicense> lics = (Vector<PartnerLicense>)foundLics;
								for(PartnerLicense e : lics)
									dashBean.setPartnerLicense(e);
							}
							
							if(foundUser.getPersonel().isHasDriver())
							{
								params2 = new Hashtable<String, Object>();
								params2.put("personel", foundUser.getPersonel());
								Object foundDrvs = gDAO.search("PartnerDriver", params2);
								if(foundDrvs != null)
								{
									Vector<PartnerDriver> drvlist = (Vector<PartnerDriver>)foundDrvs;
									for(PartnerDriver e : drvlist)
										dashBean.setDriver(e);
								}
							}
							
							msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Authentication successful!");
							FacesContext.getCurrentInstance().addMessage(null, msg);
							saveAudit("LOGIN: Authentication successful for user: " + getUsername() + ", Partner: " + foundUser.getPartner().getName());
						
							if(foundUser.isActivated())
							{
								ret = "dashboard?faces-redirect=true";
							}
							else
							{
								ret = "index";
								msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Authentication successful! However you need to reset your initial password before you can continue.");
								FacesContext.getCurrentInstance().addMessage(null, msg);
								saveAudit("LOGIN: Authentication successful for user: " + getUsername() + ", Partner: " + foundUser.getPartner().getName() + ". But needs to activate account.");
							}
						}
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Authentication failed!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					saveAudit("LOGIN: Authentication failed for user: " + getUsername());
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "User does not exist. No record found.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				saveAudit("LOGIN: User does not exist: " + getUsername());
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "User does not exist.");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			saveAudit("LOGIN: User does not exist: " + getUsername());
		}
		gDAO.destroy();
		
		return ret;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPartner_code() {
		return partner_code;
	}

	public void setPartner_code(String partner_code) {
		this.partner_code = partner_code;
	}

	public boolean isAutologin() {
		return autologin;
	}

	public void setAutologin(boolean autologin) {
		this.autologin = autologin;
	}

	public ApplicationMBean getAppBean() {
		return appBean;
	}

	public void setAppBean(ApplicationMBean appBean) {
		this.appBean = appBean;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
