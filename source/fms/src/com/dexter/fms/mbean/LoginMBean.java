package com.dexter.fms.mbean;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import com.dexter.common.util.Hasher;
import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.MRoleFunction;
import com.dexter.fms.model.PartnerSubscription;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.PartnerUserRole;

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
	
	public String logout()
	{
		String ret = "index";
		
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
				if(foundUser.getPassword().equals(Hasher.getHashValue(getPassword())))
				{
					foundUser.setSession(((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getSession());
					boolean r = appBean.addLoggedInUser(foundUser);
					if(!r)
					{
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
								PartnerSubscription sub = ((Vector<PartnerSubscription>)foundSubs).get(0);
								dashBean.setSubscription(sub);
							}
							else
							{
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
								Hashtable<String, Object> params3 = new Hashtable<String, Object>();
								params3.put("role", pur.getRole());
								Object foundRoleFunctions = new GeneralDAO().search("MRoleFunction", params3);
								if(foundRoleFunctions != null)
								{
									Vector<MRoleFunction> rflist = (Vector<MRoleFunction>)foundRoleFunctions;
									if(dashBean.getRolesFunctions() == null)
										dashBean.setRolesFunctions(rflist);
									else
										dashBean.getRolesFunctions().addAll(rflist);
								}
							}
							
							msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Authentication successful!");
							FacesContext.getCurrentInstance().addMessage(null, msg);
						}
						ret = "dashboard";
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Authentication failed!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "User does not exist. No record found.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "User does not exist.");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
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
