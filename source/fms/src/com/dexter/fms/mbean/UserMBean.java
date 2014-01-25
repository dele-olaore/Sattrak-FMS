package com.dexter.fms.mbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
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
import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.PartnerSubscription;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.PartnerUserRole;
import com.dexter.fms.model.app.DriverLicense;
import com.dexter.fms.model.ref.Department;
import com.dexter.fms.model.ref.DriverGrade;
import com.dexter.fms.model.ref.Region;

@ManagedBean(name = "userBean")
@SessionScoped
public class UserMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-UserMBean");
	
	private FacesMessage msg = null;
	
	private Long partner_id, partner_id2, partner_id3;
	private Partner partner;
	
	private Department dept;
	private Vector<Department> depts;
	private Region region;
	private Vector<Region> regions;
	
	private Long personel_dept_id;
	private Long personel_region_id;
	private Long personel_id;
	private PartnerPersonel personel;
	private UploadedFile partnerPersonelPhoto;
	private Vector<PartnerPersonel> personels, personelsWithoutUsers;
	
	private Long driverGrade_id;
	private String drvLicenseNo;
	private Date drvLicenseExpiryDate;
	private String guarantor;
	private UploadedFile certFile, driverslicPhoto;
	private PartnerDriver driver;
	private Vector<PartnerDriver> drivers;
	
	private DriverGrade driverGrade;
	private Vector<DriverGrade> driverGrades;
	
	private PartnerSubscription sub;
	
	private MRole mrole;
	private Vector<MRole> mroles;
	private Vector<MFunction> partnerFunctions;
	
	private String cpassword;
	private PartnerUser user;
	private Vector<PartnerUser> users;
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public UserMBean()
	{}
	
	public void save(int i)
	{
		GeneralDAO gDAO = new GeneralDAO();
		boolean ret = false;
		boolean validated = false;
		switch(i)
		{
			case 1: // department
			{
				if(getDept().getName() != null)
				{
					if(getPartner_id2() != null)
						setPartner_id(getPartner_id2());
					
					getDept().setPartner(getPartner());
					getDept().setCreatedBy(dashBean.getUser());
					getDept().setCrt_dt(new Date());
					gDAO.startTransaction();
					ret = gDAO.save(getDept());
					if(ret)
					{
						//setPartner_id(null);
						setPersonel_dept_id(getDept().getId());
						setDept(null);
						setDepts(null);
					}
					validated = true;
				}
				break;
			}
			case 2: // region
			{
				if(getRegion().getName() != null)
				{
					if(getPartner_id3() != null)
						setPartner_id(getPartner_id3());
					
					getRegion().setPartner(getPartner());
					getRegion().setCreatedBy(dashBean.getUser());
					getRegion().setCrt_dt(new Date());
					gDAO.startTransaction();
					ret = gDAO.save(getRegion());
					if(ret)
					{
						//setPartner_id(null);
						setPersonel_region_id(getRegion().getId());
						setRegion(null);
						setRegions(null);
					}
					validated = true;
				}
				break;
			}
			case 3: // personel
			{
				if(getPersonel().getFirstname() != null && getPersonel().getLastname() != null)
				{
					if(getPartnerPersonelPhoto() != null)
					{
						getPersonel().setPhoto(getPartnerPersonelPhoto().getContents());
					}
					getPersonel().setPartner(getPartner());
					getPersonel().setCreatedBy(dashBean.getUser());
					getPersonel().setCrt_dt(new Date());
					
					if(getPersonel_dept_id() != null)
					{
						Object obj = gDAO.find(Department.class, getPersonel_dept_id());
						if(obj != null)
							getPersonel().setDepartment((Department)obj);
					}
					
					if(getPersonel_region_id() != null)
					{
						Object obj = gDAO.find(Region.class, getPersonel_region_id());
						if(obj != null)
							getPersonel().setRegion((Region)obj);
					}
					
					gDAO.startTransaction();
					ret = gDAO.save(getPersonel());
					if(ret)
					{
						if(getPersonel().isHasDriver())
						{
							PartnerDriver driver = new PartnerDriver();
							driver.setActive(true);
							driver.setCreatedBy(dashBean.getUser());
							driver.setCrt_dt(new Date());
							driver.setPartner(getPartner());
							driver.setPersonel(getPersonel());
							
							driver.setDrvLicenseNo(getDrvLicenseNo());
							driver.setGuarantor(getGuarantor());
							if(getCertFile() != null)
							{
								driver.setCertificationFile(getCertFile().getContents());
							}
							
							if(getDriverGrade_id() != null && getDriverGrade_id() > 0)
							{
								Object dgObj = gDAO.find(DriverGrade.class, getDriverGrade_id());
								if(dgObj != null)
									driver.setDriverGrade((DriverGrade)dgObj);
							}
							
							ret = gDAO.save(driver);
							
							if(ret && getDrvLicenseExpiryDate() != null)
							{
								DriverLicense dl = new DriverLicense();
								dl.setCreatedBy(dashBean.getUser());
								dl.setCrt_dt(new Date());
								dl.setLic_end_dt(getDrvLicenseExpiryDate());
								if(getDriverslicPhoto() != null)
								{
									dl.setDocument(getDriverslicPhoto().getContents());
								}
								boolean active = false, expired = false;
								if(getDrvLicenseExpiryDate().after(new Date()))
								{
									active = true;
								}
								else
								{
									expired = true;
								}
								dl.setActive(active);
								dl.setExpired(expired);
								dl.setDriver(driver);
								gDAO.save(dl);
							}
						}
						
						if(getPersonel().isHasUser())
						{
							if(getUser().getUsername() != null && getUser().getPassword() != null)
							{
								if(getUser().getPassword().equals(getCpassword()))
								{
									getUser().setActive(true);
									getUser().setCreatedBy(dashBean.getUser());
									getUser().setCrt_dt(new Date());
									getUser().setPassword(Hasher.getHashValue(getUser().getPassword()));
									getUser().setPartner(getPartner());
									if(getPartner() != null)
										getUser().setPartner_code(getPartner().getCode());
									getUser().setPersonel(getPersonel());
									
									ret = gDAO.save(getUser());
									if(ret)
									{
										for(MRole mr : getMroles())
										{
											if(mr.isSelected())
											{
												PartnerUserRole pur = new PartnerUserRole();
												pur.setCreatedBy(dashBean.getUser());
												pur.setCrt_dt(new Date());
												pur.setRole(mr);
												pur.setUser(getUser());
												
												gDAO.save(pur);
											}
										}
									}
									else
									{
										break;
									}
								}
								else
								{
									gDAO.rollback();
									validated = false;
									msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Password fields are not the same!");
									FacesContext.getCurrentInstance().addMessage(null, msg);
								}
							}
							else
							{
								gDAO.rollback();
								validated = false;
							}
						}
						
						setDrvLicenseNo(null);
						setGuarantor(null);
						setCertFile(null);
						setDriverslicPhoto(null);
						setDrvLicenseExpiryDate(null);
						setCpassword(null);
						setUser(null);
						setPersonel_dept_id(null);
						setPersonel_region_id(null);
						setPartnerPersonelPhoto(null);
						setPersonel(null);
						setPersonels(null);
					}
					validated = true;
				}
				break;
			}
			case 4:
			{
				if(getPersonel_id() != null && getUser().getUsername() != null && getUser().getPassword() != null)
				{
					if(getUser().getPassword().equals(getCpassword()))
					{
						getUser().setActive(true);
						getUser().setCreatedBy(dashBean.getUser());
						getUser().setCrt_dt(new Date());
						getUser().setPassword(Hasher.getHashValue(getUser().getPassword()));
						getUser().setPartner(getPartner());
						if(getPartner() != null)
							getUser().setPartner_code(getPartner().getCode());
						
						Object pObj = gDAO.find(PartnerPersonel.class, getPersonel_id());
						if(pObj != null)
						{
							getUser().setPersonel((PartnerPersonel)pObj);
						}
						
						ret = gDAO.save(getUser());
						if(ret)
						{
							setUsers(null);
							setUser(null);
							setCpassword(null);
							setPersonel_id(null);
						}
					}
					validated = true;
				}
				break;
			}
			case 5:
			{
				if(getMrole().getName() != null)
				{
					getMrole().setCreatedBy(dashBean.getUser());
					getMrole().setCrt_dt(new Date());
					getMrole().setDefaultRole(false);
					getMrole().setPartner(getPartner());
					gDAO.startTransaction();
					ret = gDAO.save(getMrole());
					if(ret)
					{
						System.out.println("true");
						for(MFunction f : getPartnerFunctions())
						{
							if(f.isSelected())
							{
								System.out.println("selected");
								MRoleFunction mrf = new MRoleFunction();
								mrf.setCreatedBy(dashBean.getUser());
								mrf.setCrt_dt(new Date());
								mrf.setFunction(f);
								mrf.setRole(getMrole());
								
								gDAO.save(mrf);
								System.out.println("saved");
							}
							else
								System.out.println("not selected");
						}
						
						setMrole(null);
						setMroles(null);
						setPartnerFunctions(null);
					}
					validated = true;
				}
				break;
			}
		}
		if(validated)
		{
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Entity created successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create entity. " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public Long getPartner_id() {
		return partner_id;
	}

	public void setPartner_id(Long partner_id) {
		this.partner_id = partner_id;
	}
	
	public Long getPartner_id2() {
		return partner_id2;
	}

	public void setPartner_id2(Long partner_id2) {
		this.partner_id2 = partner_id2;
	}

	public Long getPartner_id3() {
		return partner_id3;
	}

	public void setPartner_id3(Long partner_id3) {
		this.partner_id3 = partner_id3;
	}

	@SuppressWarnings("unchecked")
	public Partner getPartner() {
		if(!dashBean.getUser().getPartner().isSattrak())
		{
			partner = dashBean.getUser().getPartner();
			sub = dashBean.getSubscription();
		}
		else
		{
			sub = null;
			if(getPartner_id() != null)
			{
				partner = (Partner)new GeneralDAO().find(Partner.class, getPartner_id());
				if(partner!= null && !partner.isSattrak())
				{
					GeneralDAO gDAO = new GeneralDAO();
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("partner", partner);
					params.put("active", true);
					params.put("expired", false);
					Object foundSubs = gDAO.search("PartnerSubscription", params);
					if(foundSubs != null)
					{
						sub = ((Vector<PartnerSubscription>)foundSubs).get(0);
					}
				}
			}
		}
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public Department getDept() {
		if(dept == null)
			dept = new Department();
		return dept;
	}

	public void setDept(Department dept) {
		this.dept = dept;
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

	public Region getRegion() {
		if(region == null)
			region = new Region();
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
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

	public Long getPersonel_dept_id() {
		return personel_dept_id;
	}

	public void setPersonel_dept_id(Long personel_dept_id) {
		this.personel_dept_id = personel_dept_id;
	}

	public Long getPersonel_region_id() {
		return personel_region_id;
	}

	public void setPersonel_region_id(Long personel_region_id) {
		this.personel_region_id = personel_region_id;
	}

	public Long getPersonel_id() {
		return personel_id;
	}

	public void setPersonel_id(Long personel_id) {
		this.personel_id = personel_id;
	}

	public PartnerPersonel getPersonel() {
		if(personel == null)
			personel = new PartnerPersonel();
		return personel;
	}

	public void setPersonel(PartnerPersonel personel) {
		this.personel = personel;
	}

	public UploadedFile getPartnerPersonelPhoto() {
		return partnerPersonelPhoto;
	}

	public void setPartnerPersonelPhoto(UploadedFile partnerPersonelPhoto) {
		this.partnerPersonelPhoto = partnerPersonelPhoto;
	}

	@SuppressWarnings("unchecked")
	public Vector<PartnerPersonel> getPersonels() {
		boolean research = true;
		if(personels == null || personels.size() == 0)
			research = true;
		else if(personels.size() > 0)
		{
			if(getPartner() != null)
			{
				if(personels.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			personels = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				
				if(getPersonel_dept_id() != null && getPersonel_dept_id() > 0)
				{
					Object dp = gDAO.find(Department.class, getPersonel_dept_id());
					if(dp != null)
						params.put("department", (Department)dp);
				}
				
				if(getPersonel_region_id() != null && getPersonel_region_id() > 0)
				{
					Object rg = gDAO.find(Region.class, getPersonel_region_id());
					if(rg != null)
						params.put("region", (Region)rg);
				}
				
				Object dpsObj = gDAO.search("PartnerPersonel", params);
				if(dpsObj != null)
				{
					personels = (Vector<PartnerPersonel>)dpsObj;
				}
			}
		}
		return personels;
	}

	public void setPersonels(Vector<PartnerPersonel> personels) {
		this.personels = personels;
	}

	@SuppressWarnings("unchecked")
	public Vector<PartnerPersonel> getPersonelsWithoutUsers() {
		boolean research = true;
		if(personelsWithoutUsers == null || personelsWithoutUsers.size() == 0)
			research = true;
		else if(personelsWithoutUsers.size() > 0)
		{
			if(getPartner() != null)
			{
				if(personelsWithoutUsers.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			personelsWithoutUsers = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				params.put("hasUser", false);
				Object dpsObj = gDAO.search("PartnerPersonel", params);
				if(dpsObj != null)
				{
					personelsWithoutUsers = (Vector<PartnerPersonel>)dpsObj;
				}
			}
		}
		return personelsWithoutUsers;
	}

	public void setPersonelsWithoutUsers(
			Vector<PartnerPersonel> personelsWithoutUsers) {
		this.personelsWithoutUsers = personelsWithoutUsers;
	}

	public Long getDriverGrade_id() {
		return driverGrade_id;
	}

	public void setDriverGrade_id(Long driverGrade_id) {
		this.driverGrade_id = driverGrade_id;
	}

	public String getDrvLicenseNo() {
		return drvLicenseNo;
	}

	public void setDrvLicenseNo(String drvLicenseNo) {
		this.drvLicenseNo = drvLicenseNo;
	}

	public Date getDrvLicenseExpiryDate() {
		return drvLicenseExpiryDate;
	}

	public void setDrvLicenseExpiryDate(Date drvLicenseExpiryDate) {
		this.drvLicenseExpiryDate = drvLicenseExpiryDate;
	}

	public String getGuarantor() {
		return guarantor;
	}

	public void setGuarantor(String guarantor) {
		this.guarantor = guarantor;
	}

	public UploadedFile getCertFile() {
		return certFile;
	}

	public void setCertFile(UploadedFile certFile) {
		this.certFile = certFile;
	}

	public UploadedFile getDriverslicPhoto() {
		return driverslicPhoto;
	}

	public void setDriverslicPhoto(UploadedFile driverslicPhoto) {
		this.driverslicPhoto = driverslicPhoto;
	}

	public PartnerDriver getDriver() {
		if(driver == null)
			driver = new PartnerDriver();
		return driver;
	}

	public void setDriver(PartnerDriver driver) {
		this.driver = driver;
	}

	@SuppressWarnings("unchecked")
	public Vector<PartnerDriver> getDrivers() {
		boolean research = true;
		if(drivers == null || drivers.size() == 0)
			research = true;
		else if(drivers.size() > 0)
		{
			if(getPartner() != null)
			{
				if(drivers.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			drivers = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				Object dpsObj = gDAO.search("PartnerDriver", params);
				if(dpsObj != null)
				{
					drivers = (Vector<PartnerDriver>)dpsObj;
				}
			}
		}
		return drivers;
	}

	public void setDrivers(Vector<PartnerDriver> drivers) {
		this.drivers = drivers;
	}

	public DriverGrade getDriverGrade() {
		if(driverGrade == null)
			driverGrade = new DriverGrade();
		return driverGrade;
	}

	public void setDriverGrade(DriverGrade driverGrade) {
		this.driverGrade = driverGrade;
	}

	@SuppressWarnings("unchecked")
	public Vector<DriverGrade> getDriverGrades() {
		boolean research = true;
		if(driverGrades == null || driverGrades.size() == 0)
			research = true;
		else if(driverGrades.size() > 0)
		{
			if(getPartner() != null)
			{
				if(driverGrades.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			driverGrades = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				Object dpsObj = gDAO.search("DriverGrade", params);
				if(dpsObj != null)
				{
					driverGrades = (Vector<DriverGrade>)dpsObj;
				}
			}
		}
		return driverGrades;
	}

	public void setDriverGrades(Vector<DriverGrade> driverGrades) {
		this.driverGrades = driverGrades;
	}

	public PartnerSubscription getSub() {
		return sub;
	}

	public void setSub(PartnerSubscription sub) {
		this.sub = sub;
	}

	public MRole getMrole() {
		if(mrole == null)
			mrole = new MRole();
		return mrole;
	}

	public void setMrole(MRole mrole) {
		this.mrole = mrole;
	}

	@SuppressWarnings("unchecked")
	public Vector<MRole> getMroles() {
		boolean research = true;
		if(mroles == null || mroles.size() == 0)
			research = true;
		else if(mroles.size() > 0)
		{
			if(getPartner() != null)
			{
				if(mroles.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			mroles = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				Object dpsObj = gDAO.search("MRole", params);
				if(dpsObj != null)
				{
					mroles = (Vector<MRole>)dpsObj;
					for(MRole mr : mroles)
					{
						List<MFunction> mrFunctions = new ArrayList<MFunction>();
						params = new Hashtable<String, Object>();
						params.put("role", mr);
						Object mrfsObj = gDAO.search("MRoleFunction", params);
						if(mrfsObj != null)
						{
							Vector<MRoleFunction> mrfsList = (Vector<MRoleFunction>)mrfsObj;
							for(MRoleFunction mrf : mrfsList)
							{
								mrFunctions.add(mrf.getFunction());
							}
						}
						mr.setFunctions(mrFunctions);
					}
				}
			}
		}
		return mroles;
	}

	public void setMroles(Vector<MRole> mroles) {
		this.mroles = mroles;
	}
	
	public void resetMRoles()
	{
		setMroles(null);
	}

	@SuppressWarnings("unchecked")
	public Vector<MFunction> getPartnerFunctions() {
		boolean research = false;
		if(partnerFunctions == null || partnerFunctions.size() == 0)
			research = true;
		
		if(research)
		{
			GeneralDAO gDAO = new GeneralDAO();
			partnerFunctions = new Vector<MFunction>();
			
			if(getPartner() != null && !getPartner().isSattrak())
			{
				if(getSub() != null)
				{
					// this is a subscription based loading of the functions
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("appTypeModule.appType", getSub().getSubPackage().getAppType());
					params.put("subType", getSub().getSubPackage().getSubType());
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
								partnerFunctions.addAll(fsList);
							}
						}
					}
				}
			}
			else if(getPartner() != null)// load all functions, this is a sattrak user
			{
				Object fsObj = gDAO.findAll("MFunction");
				if(fsObj != null)
				{
					partnerFunctions = (Vector<MFunction>)fsObj;
				}
			}
		}
		return partnerFunctions;
	}

	public void setPartnerFunctions(Vector<MFunction> partnerFunctions) {
		this.partnerFunctions = partnerFunctions;
	}

	public String getCpassword() {
		return cpassword;
	}

	public void setCpassword(String cpassword) {
		this.cpassword = cpassword;
	}

	public PartnerUser getUser() {
		if(user == null)
			user = new PartnerUser();
		return user;
	}

	public void setUser(PartnerUser user) {
		this.user = user;
	}

	@SuppressWarnings("unchecked")
	public Vector<PartnerUser> getUsers() {
		boolean research = true;
		if(users == null || users.size() == 0)
			research = true;
		else if(users.size() > 0)
		{
			if(getPartner() != null)
			{
				if(users.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			users = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				Object dpsObj = gDAO.search("PartnerUser", params);
				if(dpsObj != null)
				{
					users = (Vector<PartnerUser>)dpsObj;
					if(users != null && users.size() > 0)
					{
						for(PartnerUser pu : users)
						{
							List<MRole> puroles = new ArrayList<MRole>();
							params = new Hashtable<String, Object>();
							params.put("user", pu);
							Object purObj = gDAO.search("PartnerUserRole", params);
							if(purObj != null)
							{
								Vector<PartnerUserRole> purList = (Vector<PartnerUserRole>)purObj;
								for(PartnerUserRole pur : purList)
								{
									puroles.add(pur.getRole());
								}
							}
							pu.setRoles(puroles);
						}
					}
				}
			}
		}
		return users;
	}

	public void setUsers(Vector<PartnerUser> users) {
		this.users = users;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
