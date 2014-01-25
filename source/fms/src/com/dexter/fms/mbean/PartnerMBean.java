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

import org.primefaces.model.UploadedFile;

import com.dexter.common.util.Hasher;
import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.ApplicationSubscriptionModule;
import com.dexter.fms.model.MFunction;
import com.dexter.fms.model.MRole;
import com.dexter.fms.model.MRoleFunction;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.PartnerSubscription;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.PartnerUserRole;
import com.dexter.fms.model.SubscriptionPackage;
import com.dexter.fms.model.app.Fleet;

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
	
	private Vector<Partner> partners;
	private Vector<PartnerSubscription> partnerSubscriptions;
	
	private Long partner_id;
	private Long package_id;
	private Date subStdt;
	private UploadedFile subDocument;
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public PartnerMBean()
	{}
	
	@SuppressWarnings("unchecked")
	public void saveSubscription()
	{
		if(getPartner_id() != null && getPackage_id() != null && getSubStdt() != null)
		{
			PartnerSubscription sub = new PartnerSubscription();
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Object pObj = gDAO.find(Partner.class, getPartner_id());
			if(pObj != null)
			{
				Object pkgObj = gDAO.find(SubscriptionPackage.class, getPackage_id());
				if(pkgObj != null)
				{
					sub.setPartner((Partner)pObj);
					sub.setSubPackage((SubscriptionPackage)pkgObj);
					sub.setStart_dt(getSubStdt());
					sub.setCrt_dt(new Date());
					sub.setExpired(false);
					
					if(getSubDocument() != null)
						sub.setInvoice(getSubDocument().getContents());
					
					Calendar c = Calendar.getInstance();
					c.setTime(getSubStdt());
					c.add(Calendar.YEAR, 1);
					
					Date end_dt = c.getTime();
					sub.setEnd_dt(end_dt);
					
					Date now = new Date();
					
					if(getSubStdt().before(now) && end_dt.after(now))
						sub.setActive(true);
					else if(end_dt.before(now))
						sub.setExpired(true);
					
					gDAO.startTransaction();
					boolean ret = gDAO.save(sub);
					if(ret)
					{
						//TODO: now we need to get the admin user of the partner, and either assigns all the functions available to this subscription to his existing role, 
						// or create a new role for him and then assign all the functions in this subscription to that role
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("partner", (Partner)pObj);
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
										
										SubscriptionPackage subp = (SubscriptionPackage)pkgObj;
										
										params = new Hashtable<String, Object>();
										params.put("appTypeModule.appType", subp.getAppType());
										params.put("subType", subp.getSubType());
										Object mdsObj = gDAO.search("ApplicationSubscriptionModule", params);
										if(mdsObj != null)
										{
											Vector<ApplicationSubscriptionModule> mdsList = (Vector<ApplicationSubscriptionModule>)mdsObj;
											for(ApplicationSubscriptionModule e : mdsList)
											{
												params = new Hashtable<String, Object>();
												params.put("module", e.getAppTypeModule().getModule());
												Object fsObj = gDAO.search("MFunction", params);
												if(fsObj != null)
												{
													Vector<MFunction> fsList = (Vector<MFunction>)fsObj;
													
													for(MFunction f : fsList)
													{
														MRoleFunction mrf = new MRoleFunction();
														mrf.setCreatedBy(dashBean.getUser());
														mrf.setCrt_dt(new Date());
														mrf.setFunction(f);
														mrf.setRole(role.getRole());
														
														gDAO.save(mrf);
													}
													
													Vector<MRole> prolesList = new Vector<MRole>();
													params = new Hashtable<String, Object>();
													params.put("partner", (Partner)pObj);
													Object prolesObj = gDAO.search("MRole", params);
													if(prolesObj != null)
														prolesList = (Vector<MRole>)prolesObj;
													for(MRole mr : prolesList)
													{
														if(mr.getId() == role.getRole().getId())
															continue;
														params = new Hashtable<String, Object>();
														params.put("role", mr);
														Object mrfsObj = gDAO.search("MRoleFunction", params);
														if(mrfsObj != null)
														{
															Vector<MRoleFunction> mrfsList = (Vector<MRoleFunction>)mrfsObj;
															for(MRoleFunction mrf : mrfsList)
															{
																boolean exist = false;
																for(MFunction f : fsList)
																{
																	if(f.getId() == mrf.getFunction().getId())
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
						
						setPartner_id(null);
						setPackage_id(null);
						setSubStdt(null);
						setSubDocument(null);
						setPartners(null);
						
						gDAO.commit();
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Subscription created successfully.");
						FacesContext.getCurrentInstance().addMessage(null, msg);
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
					ret = gDAO.save(getPartnerPersonel());
					
					getPartnerUser().setPersonel(getPartnerPersonel());
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

	public Long getPartner_id() {
		return partner_id;
	}

	public void setPartner_id(Long partner_id) {
		this.partner_id = partner_id;
	}

	public Long getPackage_id() {
		return package_id;
	}

	public void setPackage_id(Long package_id) {
		this.package_id = package_id;
	}

	public Date getSubStdt() {
		return subStdt;
	}

	public void setSubStdt(Date subStdt) {
		this.subStdt = subStdt;
	}

	public UploadedFile getSubDocument() {
		return subDocument;
	}

	public void setSubDocument(UploadedFile subDocument) {
		this.subDocument = subDocument;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
