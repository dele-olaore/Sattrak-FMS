package com.dexter.fms.mbean;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.event.RowEditEvent;
import org.primefaces.model.UploadedFile;

import com.dexter.common.util.Emailer;
import com.dexter.common.util.Hasher;
import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.ApplicationTypeDash;
import com.dexter.fms.model.ApplicationTypeFunction;
import com.dexter.fms.model.ApplicationTypeModule;
import com.dexter.fms.model.ApplicationTypeReport;
import com.dexter.fms.model.ApplicationTypeVersion;
import com.dexter.fms.model.MDashRole;
import com.dexter.fms.model.MRole;
import com.dexter.fms.model.MRoleFunction;
import com.dexter.fms.model.MRoleReport;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerLicense;
import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.PartnerSetting;
import com.dexter.fms.model.PartnerSubscription;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.PartnerUserRole;
import com.dexter.fms.model.app.Fleet;
import com.dexter.fms.model.ref.Division;

@ManagedBean(name = "partnerBean")
@SessionScoped
public class PartnerMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-PartnerMBean");
	
	private FacesMessage msg = null;
	
	private Partner partner;
	private PartnerPersonel partnerPersonel;
	private PartnerUser partnerUser;
	private String cpassword;
	private UploadedFile partnerUserPhoto;
	private PartnerSubscription subscription;
	private PartnerLicense partnerLicense;
	
	private UploadedFile partnerLogo;
	private PartnerSetting setting;
	
	private Vector<Partner> partners;
	private Vector<PartnerSubscription> partnerSubscriptions;
	private Vector<PartnerLicense> partnerLicenses;
	
	private Long partner_id;
	private Long appType_id, appTypeVersion_id, subType_id;
	private Date subStdt;
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public PartnerMBean()
	{}
	
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
	
	@SuppressWarnings("unchecked")
	public void saveSetting()
	{
		if(getPartnerLogo() != null)
		{
			PartnerSetting sett = new PartnerSetting();
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", dashBean.getUser().getPartner());
			
			Object pSettingsObj = gDAO.search("PartnerSetting", params);
			if(pSettingsObj != null)
			{
				Vector<PartnerSetting> pSettingsList = (Vector<PartnerSetting>)pSettingsObj;
				for(PartnerSetting e : pSettingsList)
				{
					sett = e;
				}
			}
			
			sett.setLogo(getPartnerLogo().getContents());
			sett.setPartner(dashBean.getUser().getPartner());
			gDAO.startTransaction();
			if(sett.getId() != null)
				gDAO.update(sett);
			else
				gDAO.save(sett);
			gDAO.commit();
			gDAO.destroy();
			
			setSetting(null);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void savePartnerLicense()
	{
		if(getPartner_id() != null && getPartnerLicense().getPurchasedLicenseCount() > 0)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object partnerObj = gDAO.find(Partner.class, getPartner_id());
			if(partnerObj != null)
			{
				Partner partner = (Partner)partnerObj;
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", partner);
				Object foundLics = gDAO.search("PartnerLicense", params);
				PartnerLicense currentLicense = null;
				if(foundLics != null)
				{
					Vector<PartnerLicense> lics = (Vector<PartnerLicense>)foundLics;
					for(PartnerLicense lic : lics)
						currentLicense = lic;
				}
				
				getPartnerLicense().setCreatedBy(dashBean.getUser());
				getPartnerLicense().setCrt_dt(new Date());
				getPartnerLicense().setCurrentLicenseCount((currentLicense != null) ? currentLicense.getFinalLicenseCount() : 0);
				getPartnerLicense().setFinalLicenseCount(getPartnerLicense().getPurchasedLicenseCount() + getPartnerLicense().getCurrentLicenseCount());
				getPartnerLicense().setPartner(partner);
				try{
				getPartnerLicense().setTotalLicensePrice(getPartnerLicense().getUnitLicensePrice()*getPartnerLicense().getPurchasedLicenseCount());
				} catch(Exception ex){}
				gDAO.startTransaction();
				boolean ret = gDAO.save(getPartnerLicense());
				if(ret)
				{
					gDAO.commit();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getPartnerLicense().getPurchasedLicenseCount() + " license(s) added for partner successfully!");
					
					setPartners(null);
					setPartner(null);
					setPartner_id(null);
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Create failed. " + gDAO.getMessage());
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Unknown partner!");
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Fields with '*' sign are required!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void saveSubscription()
	{
		if(getPartner_id() != null && getAppTypeVersion_id() != null && getSubStdt() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object pObj = gDAO.find(Partner.class, getPartner_id());
			if(pObj != null)
			{
				Object apvObj = gDAO.find(ApplicationTypeVersion.class, getAppTypeVersion_id());
				if(apvObj != null)
				{
					ApplicationTypeVersion apv = (ApplicationTypeVersion)apvObj;
					getSubscription().setAppTypeVersion(apv);
					getSubscription().setPartner((Partner)pObj);
					getSubscription().setStart_dt(getSubStdt());
					getSubscription().setCrt_dt(new Date());
					getSubscription().setCreatedBy(dashBean.getUser());
					getSubscription().setExpired(false);
					
					Calendar c = Calendar.getInstance();
					c.setTime(getSubStdt());
					if(getSubscription().isDemo())
					{
						c.add(Calendar.DATE, getSubscription().getDays());
						Date end_dt = c.getTime();
						getSubscription().setEnd_dt(end_dt);
					}
					else
					{
						getSubscription().setEnd_dt(null);
					}
					getSubscription().setActive(true);
					
					Date now = new Date();
					if(getSubStdt().before(now) && (getSubscription().getEnd_dt() == null || (getSubscription().getEnd_dt() != null && getSubscription().getEnd_dt().after(now))))
					{
						getSubscription().setExpired(false);
						// TODO: Update the current active to in-active
					}
					else if(getSubscription().getEnd_dt() != null && getSubscription().getEnd_dt().before(now))
					{
						getSubscription().setExpired(true);
					}
					
					gDAO.startTransaction();
					boolean ret = gDAO.save(getSubscription());
					if(ret)
					{
						//TODO: This is where we check if this subscription is active then we add all the functions and reports of the subscription application version to the admin role of this partner
						//if(getSubscription().isActive() && !getSubscription().isExpired())
						activateSubs(getSubscription(), gDAO);
						
						gDAO.commit();
						
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Subscription created successfully.");
						FacesContext.getCurrentInstance().addMessage(null, msg);
						
						setPartner_id(null);
						setAppTypeVersion_id(null);
						setSubStdt(null);
						setPartners(null);
						setSubscription(null);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Create failed. " + gDAO.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Invalid package selected!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Invalid partner selected!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Partner, Package and Start date are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void activateSubs(PartnerSubscription sub, GeneralDAO gDAO)
	{
		//TODO: now we need to get the admin user of the partner, and either assigns all the functions available to this subscription to his existing role, 

		// or create a new role for him and then assign all the functions in this subscription to that role
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("partner", sub.getPartner());
		params.put("admin", true);
		Object uObj = gDAO.search("PartnerUser", params);
		if(uObj != null)
		{
			Vector<PartnerUser> uList = (Vector<PartnerUser>)uObj;
			if(uList.size() > 0)
			{
				PartnerUser adminUser = uList.get(0);
				params = new Hashtable<String, Object>();
				params.put("user", adminUser);
				params.put("defaultRole", true);

				Object rObj = gDAO.search("PartnerUserRole", params);
				if(rObj != null)
				{
					Vector<PartnerUserRole> rList = (Vector<PartnerUserRole>)rObj;
					if(rList.size() > 0)
					{
						PartnerUserRole role = rList.get(0);
						// clear the previous functions attached to the admin role of the partner
						params = new Hashtable<String, Object>();
						params.put("role", role.getRole());
						Object mrfObj = gDAO.search("MRoleFunction", params);
						if(mrfObj != null)
						{
							Vector<MRoleFunction> mrfList = (Vector<MRoleFunction>)mrfObj;
							for(MRoleFunction e : mrfList)
							{
								gDAO.remove(e);
							}
						}
						// clear the previous reports attached to the main role of the partner
						params = new Hashtable<String, Object>();
						params.put("role", role.getRole());
						Object mrrObj = gDAO.search("MRoleReport", params);
						if(mrrObj != null)
						{
							Vector<MRoleReport> mrrList = (Vector<MRoleReport>)mrrObj;
							for(MRoleReport e : mrrList)
							{
								gDAO.remove(e);
							}
						}
						// clear the previous dashs attached to the main role of the partner
						params = new Hashtable<String, Object>();
						params.put("role", role.getRole());
						Object mdrObj = gDAO.search("MDashRole", params);
						if(mdrObj != null)
						{
							Vector<MDashRole> mdrList = (Vector<MDashRole>)mdrObj;
							for(MDashRole e : mdrList)
							{
								gDAO.remove(e);
							}
						}
						
						// now attach the dashs from the subscription's app type version to the role
						params = new Hashtable<String, Object>();
						params.put("appTypeVersion", sub.getAppTypeVersion());
						Object asrObj2 = gDAO.search("ApplicationTypeDash", params);
						if(asrObj2 != null)
						{
							Vector<ApplicationTypeDash> mdsList = (Vector<ApplicationTypeDash>)asrObj2;
							for(ApplicationTypeDash e : mdsList)
							{
								MDashRole mrr = new MDashRole();
								mrr.setCreatedBy(sub.getCreatedBy());
								mrr.setCrt_dt(new Date());
								mrr.setDash(e.getDash());
								mrr.setRole(role.getRole());
								
								gDAO.save(mrr);
							}
							
							Vector<MRole> prolesList = new Vector<MRole>();
							params = new Hashtable<String, Object>();
							params.put("partner", sub.getPartner());
							Object prolesObj = gDAO.search("MRole", params);
							if(prolesObj != null)
								prolesList = (Vector<MRole>)prolesObj;
							for(MRole mr : prolesList)
							{
								if(mr.getId() == role.getRole().getId())
									continue;
								params = new Hashtable<String, Object>();
								params.put("role", mr);
								Object mrrsObj = gDAO.search("MDashRole", params);
								if(mrrsObj != null)
								{
									Vector<MDashRole> mrrsList = (Vector<MDashRole>)mrrsObj;
									for(MDashRole mrr : mrrsList)
									{
										boolean exist = false;
										for(ApplicationTypeDash f : mdsList)
										{
											if(f.getDash().getId().longValue() == mrr.getDash().getId().longValue())
											{
												exist = true;
												break;
											}
										}
										if(!exist)
											gDAO.remove(mrr);
									}
								}
							}
						}
						
						// now attach the reports from the subscription's app type version to the role
						params = new Hashtable<String, Object>();
						params.put("appTypeVersion", sub.getAppTypeVersion());
						Object asrObj = gDAO.search("ApplicationTypeReport", params);
						if(asrObj != null)
						{
							Vector<ApplicationTypeReport> mdsList = (Vector<ApplicationTypeReport>)asrObj;
							for(ApplicationTypeReport e : mdsList)
							{
								MRoleReport mrr = new MRoleReport();
								mrr.setCreatedBy(sub.getCreatedBy());
								mrr.setCrt_dt(new Date());
								mrr.setReport(e.getReport());
								mrr.setRole(role.getRole());
								
								gDAO.save(mrr);
							}
							
							Vector<MRole> prolesList = new Vector<MRole>();
							params = new Hashtable<String, Object>();
							params.put("partner", sub.getPartner());
							Object prolesObj = gDAO.search("MRole", params);
							if(prolesObj != null)
								prolesList = (Vector<MRole>)prolesObj;
							for(MRole mr : prolesList)
							{
								if(mr.getId() == role.getRole().getId())
									continue;
								params = new Hashtable<String, Object>();
								params.put("role", mr);
								Object mrrsObj = gDAO.search("MRoleReport", params);
								if(mrrsObj != null)
								{
									Vector<MRoleReport> mrrsList = (Vector<MRoleReport>)mrrsObj;
									for(MRoleReport mrr : mrrsList)
									{
										boolean exist = false;
										for(ApplicationTypeReport f : mdsList)
										{
											if(f.getReport().getId().longValue() == mrr.getReport().getId().longValue())
											{
												exist = true;
												break;
											}
										}
										if(!exist)
											gDAO.remove(mrr);
									}
								}
							}
						}
						
						params = new Hashtable<String, Object>();
						params.put("appTypeVersion", sub.getAppTypeVersion());
						Object mdsObj = gDAO.search("ApplicationTypeModule", params);
						if(mdsObj != null)
						{
							Vector<ApplicationTypeModule> mdsList = (Vector<ApplicationTypeModule>)mdsObj;
							for(ApplicationTypeModule e : mdsList)
							{
								params = new Hashtable<String, Object>();
								params.put("appTypeModule", e);
								Object fsObj = gDAO.search("ApplicationTypeFunction", params);
								if(fsObj != null)
								{
									Vector<ApplicationTypeFunction> atflist = (Vector<ApplicationTypeFunction>) fsObj;
									for(ApplicationTypeFunction f : atflist)
									{
										MRoleFunction mrf = new MRoleFunction();
										mrf.setCreatedBy(sub.getCreatedBy());
										mrf.setCrt_dt(new Date());
										mrf.setFunction(f.getFunction());
										mrf.setRole(role.getRole());
										
										gDAO.save(mrf);
									}
									
									Vector<MRole> prolesList = new Vector<MRole>();
									params = new Hashtable<String, Object>();
									params.put("partner", sub.getPartner());
									Object prolesObj = gDAO.search("MRole", params);
									if(prolesObj != null)
										prolesList = (Vector<MRole>)prolesObj;
									for(MRole mr : prolesList)
									{
										if(mr.getId() == role.getRole().getId())
											continue;
										params = new Hashtable<String, Object>();
										params.put("role", mr);
										params.put("function.module", e.getModule());
										Object mrfsObj = gDAO.search("MRoleFunction", params);
										if(mrfsObj != null)
										{
											Vector<MRoleFunction> mrfsList = (Vector<MRoleFunction>)mrfsObj;
											for(MRoleFunction mrf : mrfsList)
											{
												boolean exist = false;
												for(ApplicationTypeFunction f : atflist)
												{
													if(f.getFunction().getId().longValue() == mrf.getFunction().getId().longValue())
													{
														exist = true;
														break;
													}
												}
												if(!exist)
													gDAO.remove(mrf);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void savePartner()
	{
		if(getPartner().getName() != null && getPartnerUser().getUsername() != null && getPartnerUser().getPassword() != null &&
				getPartnerPersonel().getFirstname() != null && getPartnerPersonel().getLastname() != null)
		{
			if(getPartnerUser().getPassword().equals(getCpassword()))
			{
				getPartner().setCreatedBy(dashBean.getUser());
				getPartner().setCrt_dt(new Date());
				
				GeneralDAO gDAO = new GeneralDAO();
				
				gDAO.startTransaction();
				
				boolean ret = gDAO.save(getPartner());
				if(ret)
				{
					long count = 1;
					Object partners = gDAO.findAll("Partner");
					if(partners != null)
					{
						List<Partner> partnersList = (List<Partner>)partners;
						if(partnersList != null)
							count = partnersList.size();
					}
					
					String p_id_str = ""+count;
					switch(p_id_str.length())
					{
					case 1:
					{
						p_id_str = "P0000" + p_id_str;
						break;
					}
					case 2:
					{
						p_id_str = "P000" + p_id_str;
						break;
					}
					case 3:
					{
						p_id_str = "P00" + p_id_str;
						break;
					}
					case 4:
					{
						p_id_str = "P0" + p_id_str;
						break;
					}
					case 5:
					{
						p_id_str = "P" + p_id_str;
						break;
					}
					default:
					{
						p_id_str = "P" + p_id_str;
						break;
					}
					}
					
					getPartner().setCode(p_id_str);
					gDAO.update(getPartner());
					logger.log(Level.INFO, "Updating partner with code: '" + p_id_str + "'..." + ((gDAO.getMessage() != null && gDAO.getMessage().length() > 0) ? " Message: " + gDAO.getMessage() : "Done"));
					
					if(getPartnerLogo() != null)
					{
						PartnerSetting sett = new PartnerSetting();
						sett.setPartner(getPartner());
						sett.setLogo(getPartnerLogo().getContents());
						
						gDAO.save(sett);
					}
					
					// create a default fleet for the partner
					Fleet defaultFleet = new Fleet();
					defaultFleet.setDefaultFleet(true);
					defaultFleet.setPartner(getPartner());
					defaultFleet.setCreatedBy(dashBean.getUser());
					defaultFleet.setCrt_dt(new Date());
					defaultFleet.setDescription("This is your default fleet.");
					defaultFleet.setName("Default Fleet");
					
					gDAO.save(defaultFleet);
					
					getPartnerPersonel().setPartner(getPartner());
					getPartnerPersonel().setCreatedBy(dashBean.getUser());
					getPartnerPersonel().setCrt_dt(new Date());
					if(getPartnerUserPhoto() != null)
					{
						getPartnerPersonel().setPhoto(getPartnerUserPhoto().getContents());
					}
					getPartnerPersonel().setHasUser(true);
					ret = gDAO.save(getPartnerPersonel());
					
					getPartnerUser().setPersonel(getPartnerPersonel());
					final String pwordB4Hash = getPartnerUser().getPassword();
					getPartnerUser().setPassword(Hasher.getHashValue(getPartnerUser().getPassword()));
					getPartnerUser().setPartner(getPartner());
					getPartnerUser().setPartner_code(getPartner().getCode());
					getPartnerUser().setCreatedBy(dashBean.getUser());
					getPartnerUser().setCrt_dt(new Date());
					getPartnerUser().setActive(true);
					getPartnerUser().setAdmin(true);
					
					ret = gDAO.save(getPartnerUser());
					if(ret)
					{
						MRole partnerAdminRole = new MRole();
						partnerAdminRole.setCrt_dt(new Date());
						partnerAdminRole.setCreatedBy(getPartnerUser());
						partnerAdminRole.setDefaultRole(true);
						partnerAdminRole.setDescription("Default admin role for Partner.");
						partnerAdminRole.setName(getPartner().getName() + "-Admin");
						partnerAdminRole.setPartner(getPartner());
						
						gDAO.save(partnerAdminRole);
						
						PartnerUserRole userRole = new PartnerUserRole();
						userRole.setCreatedBy(getPartnerUser());
						userRole.setCrt_dt(new Date());
						userRole.setDefaultRole(true);
						userRole.setRole(partnerAdminRole);
						userRole.setUser(getPartnerUser());
						
						gDAO.save(userRole);
						
						gDAO.commit();
						
						HttpServletRequest origRequest = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
						
						final StringBuilder body = new StringBuilder("<html><body>");
						body.append("<p>Dear <strong>").append(getPartnerPersonel().getFirstname()).append("</strong></p>");
						body.append("<p>Your partner account '").append(getPartner().getName()).append("' is set up on the Sattrak Fleet Management System as below: -</p>");
						body.append("<p>Web Address: ").append(origRequest.getProtocol()+"://"+origRequest.getServerName()+":"+origRequest.getServerPort()+"/fms/faces/index.xhtml").append("</br></br>");
						body.append("Username: ").append(getPartnerUser().getUsername()).append("</br></br>");
						body.append("Password: ").append(pwordB4Hash).append("</br></br>");
						body.append("Partner Code: ").append(getPartner().getCode()).append("</br></br></p>");
						body.append("<p>Regards</br></br>Sattrak FMS Portal</p>");
						body.append("</body></html>");
						
						final String email = getPartnerPersonel().getEmail();
						Thread notifyThread = new Thread()
						{
							public void run()
							{
								try
								{
									if(email != null)
										Emailer.sendEmail("fms@sattrakservices.com", new String[]{email}, "FMS - Account Setup", body.toString());
								}
								catch(Exception ex)
								{
									ex.printStackTrace();
								}
							}
						};
						notifyThread.start();
						
						setPartnerPersonel(null);
						setPartners(null);
						setPartnerUser(null);
						setPartner(null);
						setPartnerUserPhoto(null);
						setCpassword(null);
						
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Partner created successfully.");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Failed to create the partner: " + gDAO.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Failed to create the partner: " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Password fields are not the same!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "All fields with '*' are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public Partner getPartner() {
		if(partner == null)
			partner = new Partner();
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public PartnerPersonel getPartnerPersonel() {
		if(partnerPersonel == null)
			partnerPersonel = new PartnerPersonel();
		return partnerPersonel;
	}

	public void setPartnerPersonel(PartnerPersonel partnerPersonel) {
		this.partnerPersonel = partnerPersonel;
	}

	public PartnerUser getPartnerUser() {
		if(partnerUser == null)
			partnerUser = new PartnerUser();
		return partnerUser;
	}

	public void setPartnerUser(PartnerUser partnerUser) {
		this.partnerUser = partnerUser;
	}

	public String getCpassword() {
		return cpassword;
	}

	public void setCpassword(String cpassword) {
		this.cpassword = cpassword;
	}

	public UploadedFile getPartnerUserPhoto() {
		return partnerUserPhoto;
	}

	public void setPartnerUserPhoto(UploadedFile partnerUserPhoto) {
		this.partnerUserPhoto = partnerUserPhoto;
	}

	public PartnerSubscription getSubscription() {
		if(subscription == null)
			subscription = new PartnerSubscription();
		return subscription;
	}

	public void setSubscription(PartnerSubscription subscription) {
		this.subscription = subscription;
	}

	public PartnerLicense getPartnerLicense() {
		if(partnerLicense == null)
			partnerLicense = new PartnerLicense();
		return partnerLicense;
	}

	public void setPartnerLicense(PartnerLicense partnerLicense) {
		this.partnerLicense = partnerLicense;
	}

	public UploadedFile getPartnerLogo() {
		return partnerLogo;
	}

	public void setPartnerLogo(UploadedFile partnerLogo) {
		this.partnerLogo = partnerLogo;
	}

	@SuppressWarnings("unchecked")
	public PartnerSetting getSetting() {
		if(setting == null)
		{
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", dashBean.getUser().getPartner());
			GeneralDAO gDAO = new GeneralDAO();
			Object pSettingsObj = gDAO.search("PartnerSetting", params);
			if(pSettingsObj != null)
			{
				Vector<PartnerSetting> pSettingsList = (Vector<PartnerSetting>)pSettingsObj;
				for(PartnerSetting e : pSettingsList)
				{
					setting = e;
				}
			}
			gDAO.destroy();
		}
		return setting;
	}

	public void setSetting(PartnerSetting setting) {
		this.setting = setting;
	}

	@SuppressWarnings("unchecked")
	public Vector<Partner> getPartners() {
		if(partners == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Object obj = gDAO.findAll("Partner");
			if(obj != null)
			{
				partners = (Vector<Partner>)obj;
				for(Partner e : partners)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("partner", e);
					Object uObj = gDAO.search("PartnerUser", params);
					if(uObj != null)
						e.setUsers((Vector<PartnerUser>)uObj);
					
					params = new Hashtable<String, Object>();
					params.put("partner", e);
					Object subsObj = gDAO.search("PartnerSubscription", params);
					if(subsObj != null)
						e.setSubscriptions((Vector<PartnerSubscription>)subsObj);
					
					params = new Hashtable<String, Object>();
					params.put("partner", e);
					Object foundLics = gDAO.search("PartnerLicense", params);
					if(foundLics != null)
					{
						Vector<PartnerLicense> lics = (Vector<PartnerLicense>)foundLics;
						for(PartnerLicense lic : lics)
							e.setLicense(lic);
					}
				}
			}
		}
		return partners;
	}

	public void setPartners(Vector<Partner> partners) {
		this.partners = partners;
	}

	public Vector<PartnerSubscription> getPartnerSubscriptions() {
		return partnerSubscriptions;
	}

	public void setPartnerSubscriptions(
			Vector<PartnerSubscription> partnerSubscriptions) {
		this.partnerSubscriptions = partnerSubscriptions;
	}

	@SuppressWarnings("unchecked")
	public Vector<PartnerLicense> getPartnerLicenses() {
		boolean research = true;
		if(partnerLicenses == null || partnerLicenses.size() == 0)
			research = true;
		else if(partnerLicenses.size() > 0)
		{
			if(getPartner_id() != null)
			{
				if(partnerLicenses.get(0).getPartner().getId() == getPartner_id())
					research = false;
			}
		}
		if(research)
		{
			partnerLicenses = null;
			if(getPartner_id() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner.id", getPartner_id());
				Object foundLics = gDAO.search("PartnerLicense", params);
				if(foundLics != null)
				{
					partnerLicenses = (Vector<PartnerLicense>)foundLics;
				}
			}
		}
		return partnerLicenses;
	}

	public void setPartnerLicenses(Vector<PartnerLicense> partnerLicenses) {
		this.partnerLicenses = partnerLicenses;
	}

	public Long getPartner_id() {
		return partner_id;
	}

	public void setPartner_id(Long partner_id) {
		this.partner_id = partner_id;
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

	public Date getSubStdt() {
		return subStdt;
	}

	public void setSubStdt(Date subStdt) {
		this.subStdt = subStdt;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
