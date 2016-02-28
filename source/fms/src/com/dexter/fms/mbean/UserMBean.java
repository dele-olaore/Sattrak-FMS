package com.dexter.fms.mbean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.persistence.Query;
import javax.servlet.ServletContext;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import com.dexter.common.util.Emailer;
import com.dexter.common.util.Hasher;
import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.ApplicationTypeDash;
import com.dexter.fms.model.ApplicationTypeFunction;
import com.dexter.fms.model.ApplicationTypeReport;
import com.dexter.fms.model.Audit;
import com.dexter.fms.model.MDash;
import com.dexter.fms.model.MDashRole;
import com.dexter.fms.model.MFunction;
import com.dexter.fms.model.MRole;
import com.dexter.fms.model.MRoleFunction;
import com.dexter.fms.model.MRoleReport;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerDriverOvertime;
import com.dexter.fms.model.PartnerDriverOvertimeRequest;
import com.dexter.fms.model.PartnerDriverQuery;
import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.PartnerSetting;
import com.dexter.fms.model.PartnerSubscription;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.PartnerUserRole;
import com.dexter.fms.model.Report;
import com.dexter.fms.model.app.DriverLicense;
import com.dexter.fms.model.app.VehicleDriver;
import com.dexter.fms.model.ref.Department;
import com.dexter.fms.model.ref.Division;
import com.dexter.fms.model.ref.DriverGrade;
import com.dexter.fms.model.ref.Region;
import com.dexter.fms.model.ref.Subsidiary;
import com.dexter.fms.model.ref.Unit;
import com.dexter.fms.util.SMSGateway;

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
	
	private Subsidiary subsidiary;
	private Vector<Subsidiary> subsidiaries;
	private Division division;
	private Vector<Division> divisions;
	private Unit unit;
	private Vector<Unit> units, deptUnits;
	
	private Long personel_dept_id;
	private Long personel_region_id;
	private Long personel_unit_id;
	private Long department_id;
	private Long division_id;
	private Long subsidiary_id;
	private Long personel_id, reportsTo_id;
	private PartnerPersonel personel;
	private UploadedFile partnerPersonelPhoto;
	private Vector<PartnerPersonel> personels, personelsWithoutUsers, unitHead;
	private StreamedContent personelsExcelTemplate;
	private UploadedFile personelsBatchExcel;
	private boolean autoCreate;
	
	private Long driverGrade_id, driver_id;
	private String drvLicenseNo, drvNo;
	private Date drvLicenseExpiryDate;
	private String guarantor;
	private UploadedFile certFile, driverslicPhoto;
	private PartnerDriver driver, selDriver;
	private Vector<PartnerDriver> drivers;
	
	private Long approver_id;
	private PartnerDriverOvertime overtime;
	private Date overtimeStDate, overtimeEndDate;
	private Vector<PartnerDriverOvertime> overtimes, myOvertimes;
	
	private PartnerDriverOvertimeRequest overtimeReq, selectedOvertimeReq;
	private Vector<PartnerDriverOvertimeRequest> myOvertimeReqs, pendingOvertimeReqs;
	
	private PartnerDriverQuery dvrQuery, selDvrQuery;
	private Date queryStDate, queryEndDate;
	private String queryStatus, finalQueryRemarks;
	private boolean punishDriver;
	private Vector<PartnerDriverQuery> dvrQueries, myPendingQueries;
	
	private DriverGrade driverGrade;
	private Vector<DriverGrade> driverGrades;
	
	private PartnerSubscription sub;
	
	private MRole mrole;
	private Vector<MRole> mroles;
	private Vector<MFunction> partnerFunctions;
	private Vector<Report> partnerReports;
	private Vector<MDash> partnerDashs;
	
	private String cpassword;
	private PartnerUser user;
	private Vector<PartnerUser> users;
	
	private Long vehicle_id;
	private Date audit_st, audit_end;
	private Vector<Audit> audits;
	
	private String approvalStatus, approvalComment;
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public UserMBean()
	{
		InputStream stream = ((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/resources/templates/fms_batchload_personel.xls");  
		personelsExcelTemplate = new DefaultStreamedContent(stream, "application/vnd.ms-excel", "fms_batchload_personel.xls");
	}
	
	@SuppressWarnings("unchecked")
	public void BatchLoadPersonels()
	{
		if(getPartner() != null && getPersonelsBatchExcel() != null)
		{
			String naration = "Batch load staffs: ";
			GeneralDAO gDAO = new GeneralDAO();
			try
			{
				ByteArrayInputStream byteIn = new ByteArrayInputStream(getPersonelsBatchExcel().getContents());
				//Get the workbook instance for XLS file
				HSSFWorkbook workbook = new HSSFWorkbook(byteIn);
				//Get first sheet from the workbook
				HSSFSheet sheet = workbook.getSheetAt(0);
				
				//Get iterator to all the rows in current sheet starting from row 2
				Iterator<Row> rowIterator = sheet.iterator();
				int pos = 1;
				
				gDAO.startTransaction();
				boolean ret = false;
				while(rowIterator.hasNext())
				{
					Row row = rowIterator.next();
					String staffID = "", firstname = "", lastname = "", department = "", region = "", address = "", phone = "", email = "", isAUser = "";
					String username = "", password = "", cpassword = "", role = "";
					String isADriver="", driver_grade="", driver_license_no="", driver_license_expiry_date="", guarantor="";
					if(pos > 1)
					{
						//Get iterator to all cells of current row
						Iterator<Cell> cellIterator = row.cellIterator();
						while(cellIterator.hasNext())
						{
							Cell cell = cellIterator.next();
							String val = "";
							switch(cell.getCellType())
							{
							case Cell.CELL_TYPE_BLANK:
								val = "";
								break;
							case Cell.CELL_TYPE_BOOLEAN:
								val = ""+cell.getBooleanCellValue();
								break;
							case Cell.CELL_TYPE_ERROR:
								val = "";
								break;
							case Cell.CELL_TYPE_NUMERIC:
								val = ""+cell.getNumericCellValue();
								break;
							case Cell.CELL_TYPE_STRING:
								val = cell.getStringCellValue();
								break;
							default:
							{
								try
								{
								val = cell.getStringCellValue();
								} catch(Exception ex){}
								break;
							}
							}
							switch(cell.getColumnIndex())
							{
							case 0:
								staffID = val;
								break;
							case 1:
								firstname = val;
								break;
							case 2: 
								lastname = val;
								break;
							case 3:
								department = val;
								break;
							case 4:
								region = val;
								break;
							case 5:
								address = val;
								break;
							case 6:
								phone = val;
								break;
							case 7:
								email = val;
								break;
							case 8:
								isAUser = val;
								break;
							case 9:
								username = val;
								break;
							case 10:
								password = val;
								break;
							case 11:
								cpassword = val;
								break;
							case 12:
								role = val;
								break;
							case 13:
								isADriver = val;
								break;
							case 14:
								driver_grade = val;
								break;
							case 15:
								driver_license_no = val;
								break;
							case 16:
								driver_license_expiry_date = val;
								break;
							case 17:
								guarantor = val;
								break;
							}
						}
						
						PartnerPersonel pp = new PartnerPersonel();
						pp.setPartner(getPartner());
						pp.setCreatedBy(dashBean.getUser());
						pp.setCrt_dt(new Date());
						pp.setStaff_id(staffID);
						pp.setFirstname(firstname);
						pp.setLastname(lastname);
						pp.setAddress(address);
						pp.setPhone(phone);
						pp.setEmail(email);
						
						if(department != null && department.trim().length() > 0)
						{
							Query q = gDAO.createQuery("Select e from Department e where e.partner = :partner and e.name = :name");
							q.setParameter("partner", getPartner());
							q.setParameter("name", department);
							Object objs = gDAO.search(q, 0);
							if(objs != null)
							{
								Vector<Department> objsList = (Vector<Department>)objs;
								for(Department e : objsList)
									pp.setDepartment(e);
							}
							if(pp.getDepartment() == null && isAutoCreate())
							{
								Department d = new Department();
								d.setCreatedBy(dashBean.getUser());
								d.setCrt_dt(new Date());
								d.setName(department);
								d.setPartner(getPartner());
								ret = gDAO.save(d);
								if(ret)
									pp.setDepartment(d);
								else
									break;
							}
							else if(pp.getDepartment() == null && !isAutoCreate())
							{
								ret = false;
								gDAO.setMessage("Department: '" + department + "' does not exist for user: " + username);
								break;
							}
						}
						
						if(region != null && region.trim().length() > 0)
						{
							Query q = gDAO.createQuery("Select e from Region e where e.partner = :partner and e.name = :name");
							q.setParameter("partner", getPartner());
							q.setParameter("name", region);
							Object objs = gDAO.search(q, 0);
							if(objs != null)
							{
								Vector<Region> objsList = (Vector<Region>)objs;
								for(Region e : objsList)
									pp.setRegion(e);
							}
							if(pp.getRegion() == null && isAutoCreate())
							{
								Region r = new Region();
								r.setCreatedBy(dashBean.getUser());
								r.setCrt_dt(new Date());
								r.setName(region);
								r.setPartner(getPartner());
								ret = gDAO.save(r);
								if(ret)
									pp.setRegion(r);
								else
									break;
							}
							else if(pp.getRegion() == null && !isAutoCreate())
							{
								ret = false;
								gDAO.setMessage("Region: '" + region + "' does not exist for user: " + username);
								break;
							}
						}
						
						MRole r = null;
						if(isAUser != null && isAUser.trim().equalsIgnoreCase("true"))
						{
							Query q = gDAO.createQuery("Select e from MRole e where e.partner = :partner and e.name = :name");
							q.setParameter("partner", getPartner());
							q.setParameter("name", role);
							Object objs = gDAO.search(q, 0);
							if(objs != null)
							{
								Vector<MRole> objsList = (Vector<MRole>)objs;
								for(MRole e : objsList)
									r = e;
							}
							
							if(r == null)
							{
								ret = false;
								gDAO.setMessage("Role: '" + role + "' must exist for user: " + username);
								break;
							}
						}
						
						ret = gDAO.save(pp);
						if(!ret)
							break;
						else
						{
							if(isAUser != null && isAUser.trim().equalsIgnoreCase("true"))
							{
								if(username != null && username.trim().length() > 0 && password != null && password.trim().length() > 0
										&& role != null && role.trim().length() > 0)
								{
									if(password.equals(cpassword))
									{
										PartnerUser pu = new PartnerUser();
										pu.setActive(true);
										pu.setAdmin(false);
										pu.setCreatedBy(dashBean.getUser());
										pu.setCrt_dt(new Date());
										pu.setPartner(getPartner());
										pu.setPartner_code(getPartner().getCode());
										pu.setPassword(Hasher.getHashValue(password));
										pu.setPersonel(pp);
										pu.setUsername(username);
										ret = gDAO.save(pu);
										if(ret)
										{
											if(r != null)
											{
												PartnerUserRole pur = new PartnerUserRole();
												pur.setCreatedBy(dashBean.getUser());
												pur.setCrt_dt(new Date());
												pur.setDefaultRole(false);
												pur.setRole(r);
												pur.setUser(pu);
												ret = gDAO.save(pur);
												if(!ret)
													break;
											}
											else
											{
												ret = false;
												gDAO.setMessage("Role: '" + role + "' does not exist for user: " + username);
												break;
											}
											
											pp.setHasUser(true);
											ret = gDAO.update(pp);
											if(!ret)
												break;
										}
										else
											break;
									}
									else
									{
										ret = false;
										gDAO.setMessage("Passwords are not the same for user: " + username);
										break;
									}
								}
								else
								{
									ret = false;
									gDAO.setMessage("All fields are required to create a user account for user: " + username);
									break;
								}
							}
							
							if(isADriver != null && isADriver.trim().equalsIgnoreCase("true"))
							{
								PartnerDriver driver = new PartnerDriver();
								driver.setActive(true);
								driver.setCreatedBy(dashBean.getUser());
								driver.setCrt_dt(new Date());
								driver.setPartner(getPartner());
								driver.setPersonel(pp);
								
								driver.setDrvLicenseNo(driver_license_no);
								driver.setGuarantor(guarantor);
								
								if(driver_grade != null && driver_grade.trim().length() > 0)
								{
									Query q = gDAO.createQuery("Select e from DriverGrade e where e.partner = :partner and e.name = :name");
									q.setParameter("partner", getPartner());
									q.setParameter("name", driver_grade);
									Object objs = gDAO.search(q, 0);
									if(objs != null)
									{
										Vector<DriverGrade> objsList = (Vector<DriverGrade>)objs;
										for(DriverGrade e : objsList)
											driver.setDriverGrade(e);
									}
									if(driver.getDriverGrade() == null && isAutoCreate())
									{
										DriverGrade dg = new DriverGrade();
										dg.setCreatedBy(dashBean.getUser());
										dg.setCrt_dt(new Date());
										dg.setName(region);
										dg.setPartner(getPartner());
										ret = gDAO.save(dg);
										if(ret)
											driver.setDriverGrade(dg);
										else
											break;
									}
									else if(driver.getDriverGrade() == null && !isAutoCreate())
									{
										ret = false;
										gDAO.setMessage("Driver grade: '" + driver_grade + "' does not exist for user: " + username);
										break;
									}
								}
								
								ret = gDAO.save(driver);
								if(!ret)
									break;
								else
								{
									pp.setHasDriver(true);
									ret = gDAO.update(pp);
									if(!ret)
										break;
								}
								if(driver_license_expiry_date != null)
								{
									Date expiryDt = null;
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
									try
									{
										expiryDt = sdf.parse(driver_license_expiry_date);
									} catch(Exception ex){}
									
									if(expiryDt != null)
									{
										DriverLicense dl = new DriverLicense();
										dl.setDrvLicenseNo(driver_license_no);
										dl.setCreatedBy(dashBean.getUser());
										dl.setCrt_dt(new Date());
										dl.setLic_end_dt(expiryDt);
										
										boolean active = false, expired = false;
										if(expiryDt.after(new Date()))
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
										ret = gDAO.save(dl);
										if(!ret)
											break;
									}
								}
							}
						}
					}
					else
						pos += 1;
				}
				if(ret)
				{
					gDAO.commit();
					naration += " Status: Success";
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "All personel created successfully.");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					
					setPersonels(null);
					setPersonelsWithoutUsers(null);
				}
				else
				{
					gDAO.rollback();
					naration += " Status: Failed: " + gDAO.getMessage();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create all personel. " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Severe error occured. " + ex.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			finally
			{
				gDAO.destroy();
				dashBean.saveAudit(naration, "", null);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void edit(int i)
	{
		GeneralDAO gDAO = new GeneralDAO();
		boolean ret = false, validated = false;
		Hashtable<String, Object> params = null;
		switch(i)
		{
			case 1:
			{
				if(getPersonel().getFirstname() != null && getPersonel().getLastname() != null &&
						getPersonel().getStaff_id() != null && !getPersonel().getStaff_id().isEmpty())
				{
					if(getPartnerPersonelPhoto() != null)
					{
						getPersonel().setPhoto(getPartnerPersonelPhoto().getContents());
					}
					getPersonel().setPartner(getPartner());
					
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
					ret = gDAO.update(getPersonel());
					if(ret)
					{
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
			case 2: // edit driver details
			{
				validated = true;
				if(getCertFile() != null)
				{
					getDriver().setCertificationFile(getCertFile().getContents());
				}
				if(getDriverGrade_id() != null && getDriverGrade_id() > 0)
				{
					Object dgObj = gDAO.find(DriverGrade.class, getDriverGrade_id());
					if(dgObj != null)
						getDriver().setDriverGrade((DriverGrade)dgObj);
				}
				gDAO.startTransaction();
				ret = gDAO.update(getDriver());
				if(ret)
				{
					setDrvLicenseNo(null);
					setGuarantor(null);
					setPersonel(null);
					setPersonels(null);
				}
				break;
			}
			case 3: // edit role
			{
				gDAO.startTransaction();
				for(MFunction f : getPartnerFunctions())
				{
					boolean exist = false;
					for(MFunction rf : getMrole().getFunctions())
					{
						if(f.getId().longValue() == rf.getId().longValue())
						{
							exist = true;
							break;
						}
					}
					
					if(f.isSelected() && !exist)
					{
						MRoleFunction mrf = new MRoleFunction();
						mrf.setCreatedBy(dashBean.getUser());
						mrf.setCrt_dt(new Date());
						mrf.setFunction(f);
						mrf.setRole(getMrole());
						
						ret = gDAO.save(mrf);
					}
					else if(!f.isSelected() && exist)
					{
						// delete it here
						params = new Hashtable<String, Object>();
						params.put("role", getMrole());
						params.put("function", f);
						Object rfObj = gDAO.search("MRoleFunction", params);
						if(rfObj != null)
						{
							Vector<MRoleFunction> rfList = (Vector<MRoleFunction>)rfObj;

							for(MRoleFunction e : rfList)
							{
								ret = gDAO.remove(e);
							}
						}
					}
				}
				
				for(Report f : getPartnerReports())
				{
					boolean exist = false;
					for(Report rr : getMrole().getReports())
					{
						if(f.getId().longValue() == rr.getId().longValue())
						{
							exist = true;
							break;
						}
					}
					
					if(f.isSelected() && !exist)
					{
						MRoleReport mrf = new MRoleReport();
						mrf.setCreatedBy(dashBean.getUser());
						mrf.setCrt_dt(new Date());
						mrf.setReport(f);
						mrf.setRole(getMrole());
						
						ret = gDAO.save(mrf);
					}
					else if(!f.isSelected() && exist)
					{
						// delete it here
						params = new Hashtable<String, Object>();
						params.put("role", getMrole());
						params.put("report", f);
						Object rfObj = gDAO.search("MRoleReport", params);
						if(rfObj != null)
						{
							Vector<MRoleReport> rfList = (Vector<MRoleReport>)rfObj;

							for(MRoleReport e : rfList)
							{
								ret = gDAO.remove(e);
							}
						}
					}
				}
				
				for(MDash f : getPartnerDashs())
				{
					boolean exist = false;
					for(MDash rr : getMrole().getDashs())
					{
						if(f.getId().longValue() == rr.getId().longValue())
						{
							exist = true;
							break;
						}
					}
					
					if(f.isSelected() && !exist)
					{
						MDashRole mrf = new MDashRole();
						mrf.setCreatedBy(dashBean.getUser());
						mrf.setCrt_dt(new Date());
						mrf.setDash(f);
						mrf.setRole(getMrole());
						
						ret = gDAO.save(mrf);
					}
					else if(!f.isSelected() && exist)
					{
						// delete it here
						params = new Hashtable<String, Object>();
						params.put("role", getMrole());
						params.put("dash", f);
						Object rfObj = gDAO.search("MDashRole", params);
						if(rfObj != null)
						{
							Vector<MDashRole> rfList = (Vector<MDashRole>)rfObj;

							for(MDashRole e : rfList)
							{
								ret = gDAO.remove(e);
							}
						}
					}
				}
				validated = true;
				setMrole(null);
				setMroles(null);
				setPartnerFunctions(null);
				setPartnerReports(null);
				setPartnerDashs(null);
				
				break;
			}
			case 4: // edit user role
			{
				gDAO.startTransaction();
				for(MRole r : getMroles())
				{
					boolean exist = false;
					for(MRole ur : getUser().getRoles())
					{
						if(ur.getId().longValue() == r.getId().longValue())
						{
							exist = true;
							break;
						}
					}
					
					if(r.isSelected() && !exist)
					{
						PartnerUserRole pur = new PartnerUserRole();
						pur.setCreatedBy(dashBean.getUser());
						pur.setCrt_dt(new Date());
						pur.setDefaultRole(false);
						pur.setRole(r);
						pur.setUser(getUser());
						ret = gDAO.save(pur);
					}
					else if(!r.isSelected() && exist)
					{
						// delete
						params = new Hashtable<String, Object>();
						params.put("role", r);
						params.put("user", getUser());
						Object rfObj = gDAO.search("PartnerUserRole", params);
						if(rfObj != null)
						{
							Vector<PartnerUserRole> rfList = (Vector<PartnerUserRole>)rfObj;

							for(PartnerUserRole e : rfList)
							{
								ret = gDAO.remove(e);
							}
						}
					}
				}
				validated = true;
				setUser(null);
				setUsers(null);
				setMroles(null);
				break;
			}
		}
		
		if(validated)
		{
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
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void initUserRoleEdit()
	{
		if(getUser().getId() != null)
		{
			for(MRole r : getMroles())
			{
				for(MRole ur : getUser().getRoles())
				{
					if(ur.getId().longValue() == r.getId().longValue())
					{
						r.setSelected(true);
						break;
					}
				}
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid entity selected!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void initRoleFunctionEdit()
	{
		if(getMrole().getId() != null)
		{
			for(MFunction f : getPartnerFunctions())
			{
				for(MFunction rf : getMrole().getFunctions())
				{
					if(rf.getId().longValue() == f.getId().longValue())
					{
						f.setSelected(true);
						break;
					}
				}
			}
			
			for(Report r : getPartnerReports())
			{
				for(Report rr : getMrole().getReports())
				{
					if(rr.getId().longValue() == r.getId().longValue())
					{
						r.setSelected(true);
						break;
					}
				}
			}
			
			for(MDash r : getPartnerDashs())
			{
				for(MDash rr : getMrole().getDashs())
				{
					if(rr.getId().longValue() == r.getId().longValue())
					{
						r.setSelected(true);
						break;
					}
				}
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid entity selected!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public String initStaffEdit()
	{
		if(getPersonel().getId() != null)
		{
			setPartner_id(getPersonel().getPartner().getId());
			if(getPersonel().getDepartment() != null)
				setPersonel_dept_id(getPersonel().getDepartment().getId());
			if(getPersonel().getRegion() != null)
				setPersonel_region_id(getPersonel().getRegion().getId());
			if(getPersonel().getUnit() != null)
				setPersonel_unit_id(getPersonel().getUnit().getId());
			
			return "edit_staff?faces-redirect=true";
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid entity selected!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return "manage_staffs";
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
	
	@SuppressWarnings("unchecked")
	public void save(int i)
	{
		String naration = "";
		GeneralDAO gDAO = new GeneralDAO();
		boolean ret = false;
		boolean validated = false;
		switch(i)
		{
			case 1: // department
			{
				if(getDept().getName() != null && getPartner() != null)
				{
					naration = "Create department: " + getDept().getName();
					if(getDivision_id() != null)
					{
						Object obj = gDAO.find(Division.class, getDivision_id());
						if(obj != null)
							getDept().setDivision((Division)obj);
					}
					else if(getSubsidiary_id() != null)
					{
						Object obj = gDAO.find(Subsidiary.class, getSubsidiary_id());
						if(obj != null)
							getDept().setSubsidiary((Subsidiary)obj);
					}
					
					getDept().setPartner(getPartner());
					getDept().setCreatedBy(dashBean.getUser());
					getDept().setCrt_dt(new Date());
					gDAO.startTransaction();
					ret = gDAO.save(getDept());
					if(ret)
					{
						setPersonel_dept_id(getDept().getId());
						setDept(null);
						setDepts(null);
						setDivision_id(null);
						setSubsidiary_id(null);
					}
					validated = true;
				}
				break;
			}
			case 2: // region
			{
				if(getRegion().getName() != null && getPartner() != null)
				{
					naration = "Create region: " + getRegion().getName();
					getRegion().setPartner(getPartner());
					getRegion().setCreatedBy(dashBean.getUser());
					getRegion().setCrt_dt(new Date());
					gDAO.startTransaction();
					ret = gDAO.save(getRegion());
					if(ret)
					{
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
				if(getPersonel().getFirstname() != null && getPersonel().getLastname() != null &&
						getPersonel().getStaff_id() != null && !getPersonel().getStaff_id().isEmpty())
				{
					naration = "Create staff: " + getPersonel().getFirstname() + " " + getPersonel().getLastname();
					if(getPartnerPersonelPhoto() != null)
					{
						getPersonel().setPhoto(getPartnerPersonelPhoto().getContents());
					}
					
					if(getReportsTo_id() != null && getReportsTo_id()>0L)
					{
						Object obj = gDAO.find(PartnerPersonel.class, getReportsTo_id());
						if(obj != null)
							getPersonel().setReportsTo((PartnerPersonel)obj);
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
					
					if(getPersonel_unit_id() != null)
					{
						Object obj = gDAO.find(Unit.class, getPersonel_unit_id());
						if(obj != null)
							getPersonel().setUnit((Unit)obj);
					}
					
					DriverGrade dg = null;
					if(getDriverGrade_id() != null && getDriverGrade_id() > 0)
					{
						Object dgObj = gDAO.find(DriverGrade.class, getDriverGrade_id());
						if(dgObj != null)
							dg = (DriverGrade)dgObj;
					}
					
					gDAO.startTransaction();
					ret = gDAO.save(getPersonel());
					boolean error = false;
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
							
							driver.setDriverGrade(dg);
							
							ret = gDAO.save(driver);
							
							if(ret && getDrvLicenseExpiryDate() != null)
							{
								DriverLicense dl = new DriverLicense();
								dl.setDrvLicenseNo(getDrvLicenseNo());
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
							if(getUser().getUsername() != null && getUser().getPassword() != null && getPersonel().getEmail() != null && getPersonel().getEmail().trim().length() > 0)
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
									error = true;
									msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Password fields are not the same!");
									FacesContext.getCurrentInstance().addMessage(null, msg);
								}
							}
							else
							{
								gDAO.rollback();
								error = true;
								msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Username, Password and Email fields are required for a user account to be created!");
								FacesContext.getCurrentInstance().addMessage(null, msg);
							}
						}
						
						if(!error)
						{
						setDrvLicenseNo(null);
						setGuarantor(null);
						setCertFile(null);
						setDriverslicPhoto(null);
						setDrvLicenseExpiryDate(null);
						setCpassword(null);
						setUser(null);
						setPersonel_dept_id(null);
						setPersonel_region_id(null);
						setPersonel_unit_id(null);
						setPartnerPersonelPhoto(null);
						setPersonel(null);
						setPersonels(null);
						}
					}
					if(!error)
						validated = true;
				}
				break;
			}
			case 4: // new user by selecting a personel from drop down
			{
				if(getPersonel_id() != null && getUser().getUsername() != null && getUser().getPassword() != null)
				{
					if(getUser().getPassword().equals(getCpassword()))
					{
						naration += "Create new user: " + getUser().getUsername();
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
						gDAO.startTransaction();
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
			case 5: // role creation
			{
				if(getMrole().getName() != null)
				{
					naration = "Create new role: " + getMrole().getName();
					getMrole().setCreatedBy(dashBean.getUser());
					getMrole().setCrt_dt(new Date());
					getMrole().setDefaultRole(false);
					getMrole().setPartner(getPartner());
					gDAO.startTransaction();
					ret = gDAO.save(getMrole());
					if(ret)
					{
						for(MFunction f : getPartnerFunctions())
						{
							if(f.isSelected())
							{
								MRoleFunction mrf = new MRoleFunction();
								mrf.setCreatedBy(dashBean.getUser());
								mrf.setCrt_dt(new Date());
								mrf.setFunction(f);
								mrf.setRole(getMrole());
								
								gDAO.save(mrf);
							}
						}
						
						for(Report f : getPartnerReports())
						{
							if(f.isSelected())
							{
								MRoleReport mrf = new MRoleReport();
								mrf.setCreatedBy(dashBean.getUser());
								mrf.setCrt_dt(new Date());
								mrf.setReport(f);
								mrf.setRole(getMrole());
								
								gDAO.save(mrf);
							}
						}
						
						for(MDash f : getPartnerDashs())
						{
							if(f.isSelected())
							{
								MDashRole mrf = new MDashRole();
								mrf.setCreatedBy(dashBean.getUser());
								mrf.setCrt_dt(new Date());
								mrf.setDash(f);
								mrf.setRole(getMrole());
								
								gDAO.save(mrf);
							}
						}
						
						setMrole(null);
						setMroles(null);
						setPartnerFunctions(null);
						setPartnerReports(null);
						setPartnerDashs(null);
					}
					validated = true;
				}
				break;
			}
			case 6: // driver overtime
			{
				if(getDriver() != null && getOvertimeReq().getStart_time() != null && getOvertimeReq().getReason() != null && 
						getOvertimeReq().getTranDate() != null && getOvertimeReq().getEnd_time() != null &&
						getApprover_id() != null) {
					naration = "Submit driver overtime: " + getDriver().getPersonel().getFirstname() + " " + getDriver().getPersonel().getLastname();
					validated = true;
					Query q2 = gDAO.createQuery("Select e from PartnerUser e where e.personel.id=:pp_id");
					q2.setParameter("pp_id", getApprover_id());
					Object puObj = gDAO.search(q2, 0);
					if(puObj != null) {
						Vector<PartnerUser> puList = (Vector<PartnerUser>)puObj;
						for(PartnerUser pu : puList)
							getOvertimeReq().setApprovedBy(pu);
					}
					if(getOvertimeReq().getApprovedBy() != null && getOvertimeReq().getAmountPerHour() > 0 &&
							getOvertimeReq().getOvertimehours() > 0) {
						getOvertimeReq().setApprovalStatus("PENDING");
						getOvertimeReq().setCreatedBy(dashBean.getUser());
						getOvertimeReq().setCrt_dt(new Date());
						getOvertimeReq().setDriver(getDriver());
						getOvertimeReq().setPartner(getDriver().getPartner());
						gDAO.startTransaction();
						ret = gDAO.save(getOvertimeReq());
						if(ret) {
							StringBuilder sb = new StringBuilder("<html><body>");
							sb.append("<p>Hello,</p>");
							sb.append("<p>Overtime request for driver: ").append(getOvertimeReq().getDriver().getPersonel().getFirstname()).append(" ").append(getOvertimeReq().getDriver().getPersonel().getLastname()).append(" was submitted and requires your approval. Please login to the portal to attend to this request!");
							sb.append("<p>Regards<br/>FMS</p>");
							sb.append("</body></html>");
							try {
								Emailer.sendEmail("fms@sattrakservices.com", new String[]{getOvertimeReq().getApprovedBy().getPersonel().getEmail()}, "Driver Overtime Request ", sb.toString());
							} catch(Exception ex) {
								ex.printStackTrace();
							}
							try {
								SMSGateway.sendSMS("FMS", getOvertimeReq().getApprovedBy().getPersonel().getPhone(), "Overtime request for driver " + getOvertimeReq().getDriver().getPersonel().getFirstname() + " " + getOvertimeReq().getDriver().getPersonel().getLastname() + " was submitted and requires your approval. Please login to the portal to attend to this request!");
							} catch(Exception ex) {
								ex.printStackTrace();
							}
							setOvertimeReq(null);
							setOvertimes(null);
							setMyOvertimeReqs(null);
						}
					} else {
						ret = false;
						naration += " Status: Failed: Approver, overtime duration hours or amount per hour is not valid! Please contact your system administrator!";
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Approver, overtime duration hours or amount per hour is not valid! Please contact your system administrator!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				break;
			}
			case 7: // driver query
			{
				//TODO: A new function is required for driver to respond and then a final query is submitted.
				if(getDriver() != null && getDvrQuery().getQueryGrade() != null && getDvrQuery().getQueryRemarks() != null 
						&& getDvrQuery().getTranDate() != null)
				{
					naration = "Submit driver query: " + getDriver().getPersonel().getFirstname() + " " + getDriver().getPersonel().getLastname();
					validated = true;
					getDvrQuery().setStatus("PENDING");
					getDvrQuery().setCreatedBy(dashBean.getUser());
					getDvrQuery().setCrt_dt(new Date());
					getDvrQuery().setDriver(getDriver());
					gDAO.startTransaction();
					ret = gDAO.save(getDvrQuery());
					if(ret) {
						StringBuilder sb = new StringBuilder("<html><body>");
						sb.append("<p>Hello,</p>");
						sb.append("<p>You have been queried: Grade: ").append(getDvrQuery().getQueryGrade()).append(" Query: ").append(getDvrQuery().getQueryRemarks()).append(". You can login to the platform to submit your own comment to defend yourself. If you don't put your comment in soon enough, the query may be made purnishable!");
						sb.append("<p>Regards<br/>FMS</p>");
						sb.append("</body></html>");
						try {
							Emailer.sendEmail("fms@sattrakservices.com", new String[]{getDvrQuery().getDriver().getPersonel().getEmail()}, "Driver Query", sb.toString());
						} catch(Exception ex) {
							ex.printStackTrace();
						}
						try {
							SMSGateway.sendSMS("FMS", getDvrQuery().getDriver().getPersonel().getPhone(), "You have been queried. You can login to the platform to submit your own comment to defend yourself. If you don't put your comment in soon enough, the query may be made purnishable!");
						} catch(Exception ex) {
							ex.printStackTrace();
						}
						
						setDvrQuery(null);
						setDvrQueries(null);
					}
				}
				break;
			}
			case 77: // make final decision on driver query
			{
				if(getDriver() != null && getSelDvrQuery() != null && getSelDvrQuery().getId() != null && getFinalQueryRemarks() != null)
				{
					getSelDvrQuery().setFinalQueryRemarks(getFinalQueryRemarks());
					getSelDvrQuery().setPunishDriver(isPunishDriver());
					
					naration = "Submit final decision on driver query: " + getDriver().getPersonel().getFirstname() + " " + getDriver().getPersonel().getLastname();
					validated = true;
					getSelDvrQuery().setStatus("CLOSED");
					getSelDvrQuery().setFinalQueryRemarksDate(new Date());
					gDAO.startTransaction();
					ret = gDAO.update(getSelDvrQuery());
					if(ret) {
						StringBuilder sb = new StringBuilder("<html><body>");
						sb.append("<p>Hello,</p>");
						sb.append("<p>A final decision has been submitted on your query for date: ").append(getSelDvrQuery().getTranDate()).append(". Final Decision: ").append(getSelDvrQuery().getFinalQueryRemarks()).append(" Purnishable: ").append(getSelDvrQuery().isPunishDriver() ? "Yes." : "No.");
						sb.append("<p>Regards<br/>FMS</p>");
						sb.append("</body></html>");
						try {
							Emailer.sendEmail("fms@sattrakservices.com", new String[]{getSelDvrQuery().getDriver().getPersonel().getEmail()}, "Final Decision on Driver Query", sb.toString());
						} catch(Exception ex) {
							ex.printStackTrace();
						}
						try {
							SMSGateway.sendSMS("FMS", getSelDvrQuery().getDriver().getPersonel().getPhone(), "A final decision that is " + (getSelDvrQuery().isPunishDriver() ? "purnishable" : "not purnishable") + " has been made on your query submitted on " + getSelDvrQuery().getTranDate());
						} catch(Exception ex) {
							ex.printStackTrace();
						}
						
						setSelDvrQuery(null);
						setDvrQueries(null);
					}
				}
				break;
			}
			case 777: // driver response to query
			{
				if(dashBean.getDriver() != null && getSelDvrQuery() != null && getSelDvrQuery().getId() != null && 
						getSelDvrQuery().getDriverResponse() != null)
				{
					naration = "Submit query response for query on: " + getSelDvrQuery().getTranDate();
					validated = true;
					getSelDvrQuery().setStatus("DRIVER_RESPONDED");
					getSelDvrQuery().setDriverResponseDate(new Date());
					gDAO.startTransaction();
					ret = gDAO.update(getSelDvrQuery());
					if(ret) {
						StringBuilder sb = new StringBuilder("<html><body>");
						sb.append("<p>Hello,</p>");
						sb.append("<p>Driver ").append(getSelDvrQuery().getDriver().getPersonel().getFirstname()).append(" ").append(getSelDvrQuery().getDriver().getPersonel().getLastname()).append(" had responded to query you submitted for him/her on ").append(getSelDvrQuery().getTranDate()).append(".<br/><br/>Driver's Response: ").append(getSelDvrQuery().getDriverResponse()).append("<br/><br/>You can login to the platform to submit your final decision on the query.");
						sb.append("<p>Regards<br/>FMS</p>");
						sb.append("</body></html>");
						try {
							Emailer.sendEmail("fms@sattrakservices.com", new String[]{getSelDvrQuery().getCreatedBy().getPersonel().getEmail()}, "Driver Response to Query", sb.toString());
						} catch(Exception ex) {
							ex.printStackTrace();
						}
						try {
							SMSGateway.sendSMS("FMS", getSelDvrQuery().getCreatedBy().getPersonel().getPhone(), "Driver " + (getSelDvrQuery().getDriver().getPersonel().getFirstname() + " " + getSelDvrQuery().getDriver().getPersonel().getLastname()) + " has responded to query you submitted for him/her on " + getSelDvrQuery().getTranDate());
						} catch(Exception ex) {
							ex.printStackTrace();
						}
						
						setSelDvrQuery(null);
						setMyPendingQueries(null);
					}
				}
				break;
			}
			case 8: // plain new user
			{
				if(getPersonel().getId() != null && getUser().getUsername() != null && getUser().getPassword() != null)
				{
					validated = true;
					if(getUser().getPassword().equals(getCpassword()))
					{
						naration = "Create user: " + getUser().getUsername();
						getUser().setActive(true);
						getUser().setCreatedBy(dashBean.getUser());
						getUser().setCrt_dt(new Date());
						getUser().setPassword(Hasher.getHashValue(getUser().getPassword()));
						getUser().setPartner(getPartner());
						if(getPartner() != null)
							getUser().setPartner_code(getPartner().getCode());
						getUser().setPersonel(getPersonel());
						
						gDAO.startTransaction();
						ret = gDAO.save(getUser());
						if(ret)
						{
							getPersonel().setHasUser(true);
							gDAO.update(getPersonel());
							
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
							
							setUser(null);
							setPersonel(null);
							setPersonels(null);
						}
					}
					else
					{
						validated = false;
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Password fields are not the same!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				break;
			}
			case 9: // plain new driver
			{
				try {
					naration = "Create driver: " + getPersonel().getFirstname() + " " + getPersonel().getLastname();
				} catch(Exception ex){}
				validated = true;
				PartnerDriver driver = new PartnerDriver();
				driver.setActive(true);
				driver.setCreatedBy(dashBean.getUser());
				driver.setCrt_dt(new Date());
				driver.setPartner(getPartner());
				driver.setPersonel(getPersonel());
				
				driver.setDriverNo(getDrvNo());
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
				gDAO.startTransaction();
				ret = gDAO.save(driver);
				getPersonel().setHasDriver(true);
				gDAO.update(getPersonel());
				if(ret)
				{
					if(getDrvLicenseExpiryDate() != null)
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
					
					setDrvLicenseNo(null);
					setGuarantor(null);
					setCertFile(null);
					setDriverslicPhoto(null);
					setDrvLicenseExpiryDate(null);
					setPersonel(null);
					setPersonels(null);
				}
				break;
			}
			case 10: // subsidiary
			{
				if(getSubsidiary().getName() != null && getPartner() != null)
				{
					naration = "Create subsidiary: " + getSubsidiary().getName();
					getSubsidiary().setPartner(getPartner());
					getSubsidiary().setCreatedBy(dashBean.getUser());
					getSubsidiary().setCrt_dt(new Date());
					gDAO.startTransaction();
					ret = gDAO.save(getSubsidiary());
					if(ret)
					{
						setSubsidiary(null);
						setSubsidiaries(null);
					}
					validated = true;
				}
				break;
			}
			case 11: // division
			{
				if(getDivision().getName() != null && getPartner() != null)
				{
					naration = "Create division: " + getDivision().getName();
					getDivision().setPartner(getPartner());
					getDivision().setCreatedBy(dashBean.getUser());
					getDivision().setCrt_dt(new Date());
					gDAO.startTransaction();
					ret = gDAO.save(getDivision());
					if(ret)
					{
						setDivision(null);
						setDivisions(null);
					}
					validated = true;
				}
				break;
			}
			case 12: // unit
			{
				if(getUnit().getName() != null && getPartner() != null)
				{
					naration = "Create unit: " + getUnit().getName();
					if(getDepartment_id() != null)
					{
						Object obj = gDAO.find(Department.class, getDepartment_id());
						if(obj != null)
							getUnit().setDepartment((Department)obj);
					}
					if(getUnit().getDepartment() != null)
					{
						getUnit().setPartner(getPartner());
						getUnit().setCreatedBy(dashBean.getUser());
						getUnit().setCrt_dt(new Date());
						gDAO.startTransaction();
						ret = gDAO.save(getUnit());
						if(ret)
						{
							setUnit(null);
							setUnits(null);
							setDepartment_id(null);
						}
						validated = true;
					}
					else
					{
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Unit must belong to a department!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				break;
			}
			case 13: // create overtime request
			{
				if(getOvertimeReq().getStart_time() != null && getOvertimeReq().getReason() != null && 
						getOvertimeReq().getTranDate() != null && getOvertimeReq().getEnd_time() != null &&
						getApprover_id() != null) {
					naration = "Create driver overtime request: ";
					try {
						naration += dashBean.getDriver().getPersonel().getFirstname() + " " + dashBean.getDriver().getPersonel().getLastname();
					} catch(Exception ex){}
					validated = true;
					
					Query q2 = gDAO.createQuery("Select e from PartnerUser e where e.personel.id=:pp_id");
					q2.setParameter("pp_id", getApprover_id());
					Object puObj = gDAO.search(q2, 0);
					if(puObj != null) {
						Vector<PartnerUser> puList = (Vector<PartnerUser>)puObj;
						for(PartnerUser pu : puList)
							getOvertimeReq().setApprovedBy(pu);
					}
					/*q2 = gDAO.createQuery("Select e from PartnerSetting e where e.partner.id=:partner_id");
					q2.setParameter("partner_id", dashBean.getUser().getPartner().getId());
					Object psObj = gDAO.search(q2, 0);
					if(psObj != null) {
						PartnerSetting sett = null;
						Vector<PartnerSetting> psList = (Vector<PartnerSetting>)psObj;
						for(PartnerSetting ps : psList)
							sett = ps;
						if(sett != null) {
							double amtHour = sett.getOverTimeAmountPerHour();
							long timediff = getOvertimeReq().getEnd_time().getTime() - getOvertimeReq().getStart_time().getTime();
							long hour = 1000*60*60;
							int divide = 0;
							try {
								divide = Integer.parseInt(""+timediff/hour);
							} catch(Exception ex){}
							if(divide <= 0)
								divide = 1;
							getOvertimeReq().setAmountPerHour(amtHour);
							getOvertimeReq().setOvertimehours(divide);
						}
					}*/
					if(getOvertimeReq().getApprovedBy() != null && getOvertimeReq().getAmountPerHour() > 0 &&
							getOvertimeReq().getOvertimehours() > 0) {
						getOvertimeReq().setApprovalStatus("PENDING");
						getOvertimeReq().setCreatedBy(dashBean.getUser());
						getOvertimeReq().setCrt_dt(new Date());
						getOvertimeReq().setDriver(dashBean.getDriver());
						getOvertimeReq().setPartner(dashBean.getUser().getPartner());
						gDAO.startTransaction();
						ret = gDAO.save(getOvertimeReq());
						if(ret) {
							StringBuilder sb = new StringBuilder("<html><body>");
							sb.append("<p>Hello,</p>");
							sb.append("<p>Driver ").append(getOvertimeReq().getDriver().getPersonel().getFirstname()).append(" ").append(getOvertimeReq().getDriver().getPersonel().getLastname()).append(" submitted a overtime request that requires your approval. Please login to the portal to attend to this request!");
							sb.append("<p>Regards<br/>FMS</p>");
							sb.append("</body></html>");
							try {
								Emailer.sendEmail("fms@sattrakservices.com", new String[]{getOvertimeReq().getApprovedBy().getPersonel().getEmail()}, "Driver Overtime Request ", sb.toString());
							} catch(Exception ex) {
								ex.printStackTrace();
							}
							try {
								SMSGateway.sendSMS("FMS", getOvertimeReq().getApprovedBy().getPersonel().getPhone(), "Driver " + getOvertimeReq().getDriver().getPersonel().getFirstname() + " " + getOvertimeReq().getDriver().getPersonel().getLastname() + " submitted a overtime request that requires your approval. Please login to the portal to attend to this request!");
							} catch(Exception ex) {
								ex.printStackTrace();
							}
							setOvertimeReq(null);
							setMyOvertimeReqs(null);
						}
					} else {
						ret = false;
						naration += " Status: Failed: Approver, overtime duration hours or amount per hour is not valid! Please contact your system administrator!";
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Approver, overtime duration hours or amount per hour is not valid! Please contact your system administrator!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				
				break;
			}
			case 14: // attend to overtime requests
			{
				int selcount = 0;
				if(getPendingOvertimeReqs() != null && getPendingOvertimeReqs().size() > 0) {
					naration = "Attend to overtime request: ";
					gDAO.startTransaction();
					for(PartnerDriverOvertimeRequest e : getPendingOvertimeReqs()) {
						if(e.isSelected()) {
							naration += e.getDriver().getPersonel().getFirstname() + " " + e.getDriver().getPersonel().getLastname() + " (" + e.getTranDate() + ")";
							selcount+=1;
							e.setApprovalStatus(getApprovalStatus());
							e.setApprovedBy(dashBean.getUser());
							e.setApprovedDate(new Date());
							ret = gDAO.update(e);
							if(ret) {
								if(getApprovalStatus().equalsIgnoreCase("APPROVED")) {
									PartnerDriverOvertime ov = new PartnerDriverOvertime();
									ov.setStart_time(e.getStart_time());
									ov.setEnd_time(e.getEnd_time());
									ov.setAmountPerHour(e.getAmountPerHour());
									ov.setCreatedBy(dashBean.getUser());
									ov.setCrt_dt(new Date());
									ov.setDriver(e.getDriver());
									ov.setOvertimehours(e.getOvertimehours());
									ov.setReason(e.getReason());
									ov.setTranDate(e.getTranDate());
									
									ret = gDAO.save(ov);
									if(!ret)
										break;
								}
							}
							else
								break;
						}
					}
					if(selcount == 0) {
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No pending overtime request was selected!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else {
						validated = true;
						setPendingOvertimeReqs(null);
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No pending overtime request found!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				break;
			}
		}
		if(validated)
		{
			if(ret)
			{
				gDAO.commit();
				naration += " Status: Success";
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Entity created successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				if(!naration.contains("Status:"))
					naration += " Status: Failed: " + gDAO.getMessage();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create entity. " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			
			gDAO.destroy();
			dashBean.saveAudit(naration, "", null);
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
		if(!dashBean.getUser().getPartner().isSattrak()) {
			partner = dashBean.getUser().getPartner();
			sub = dashBean.getSubscription();
		}
		else {
			sub = null;
			if(getPartner_id() != null) {
				GeneralDAO gDAO = new GeneralDAO();
				partner = (Partner)gDAO.find(Partner.class, getPartner_id());
				if(partner != null && !partner.isSattrak()) {
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("partner", partner);
					params.put("active", true);
					params.put("expired", false);
					Object foundSubs = gDAO.search("PartnerSubscription", params);
					if(foundSubs != null) {
						Vector<PartnerSubscription> subList = new Vector<PartnerSubscription>();
						if(subList.size() > 0)
							sub = ((Vector<PartnerSubscription>)foundSubs).get(0);
					}
				}
				gDAO.destroy();
			} else {
				partner = dashBean.getUser().getPartner();
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
				if(depts.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
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
				if(regions.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
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

	public Subsidiary getSubsidiary() {
		if(subsidiary == null)
			subsidiary = new Subsidiary();
		return subsidiary;
	}

	public void setSubsidiary(Subsidiary subsidiary) {
		this.subsidiary = subsidiary;
	}

	@SuppressWarnings("unchecked")
	public Vector<Subsidiary> getSubsidiaries() {
		boolean research = true;
		if(subsidiaries == null || subsidiaries.size() == 0)
			research = true;
		else if(subsidiaries.size() > 0)
		{
			if(getPartner() != null)
			{
				if(subsidiaries.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			subsidiaries = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				Object dpsObj = gDAO.search("Subsidiary", params);
				if(dpsObj != null)
				{
					subsidiaries = (Vector<Subsidiary>)dpsObj;
				}
			}
		}
		return subsidiaries;
	}

	public void setSubsidiaries(Vector<Subsidiary> subsidiaries) {
		this.subsidiaries = subsidiaries;
	}

	public Division getDivision() {
		if(division == null)
			division = new Division();
		return division;
	}

	public void setDivision(Division division) {
		this.division = division;
	}

	@SuppressWarnings("unchecked")
	public Vector<Division> getDivisions() {
		boolean research = true;
		if(divisions == null || divisions.size() == 0)
			research = true;
		else if(divisions.size() > 0)
		{
			if(getPartner() != null)
			{
				if(divisions.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			divisions = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				Object dpsObj = gDAO.search("Division", params);
				if(dpsObj != null)
				{
					divisions = (Vector<Division>)dpsObj;
				}
			}
		}
		return divisions;
	}

	public void setDivisions(Vector<Division> divisions) {
		this.divisions = divisions;
	}

	public Unit getUnit() {
		if(unit == null)
			unit = new Unit();
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	@SuppressWarnings("unchecked")
	public Vector<Unit> getUnits() {
		boolean research = true;
		if(units == null || units.size() == 0)
			research = true;
		else if(units.size() > 0)
		{
			if(getPartner() != null)
			{
				if(units.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			units = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				Object dpsObj = gDAO.search("Unit", params);
				if(dpsObj != null)
				{
					units = (Vector<Unit>)dpsObj;
				}
			}
		}
		return units;
	}

	public void setUnits(Vector<Unit> units) {
		this.units = units;
	}

	@SuppressWarnings("unchecked")
	public Vector<Unit> getDeptUnits() {
		boolean research = true;
		if(deptUnits == null || deptUnits.size() == 0)
			research = true;
		else if(deptUnits.size() > 0)
		{
			if(getPersonel_dept_id() != null && getPersonel_dept_id() > 0L)
			{
				if(deptUnits.get(0).getDepartment() != null && deptUnits.get(0).getDepartment().getId().longValue() == getPersonel_dept_id().longValue())
					research = false;
			}
		}
		if(research)
		{
			deptUnits = null;
			GeneralDAO gDAO = new GeneralDAO();
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("department.id", (getPersonel_dept_id()!=null) ? getPersonel_dept_id() : 0L);
			Object dpsObj = gDAO.search("Unit", params);
			if(dpsObj != null)
			{
				deptUnits = (Vector<Unit>)dpsObj;
			}
		}
		return deptUnits;
	}

	public void setDeptUnits(Vector<Unit> deptUnits) {
		this.deptUnits = deptUnits;
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

	public Long getDepartment_id() {
		return department_id;
	}

	public void setDepartment_id(Long department_id) {
		this.department_id = department_id;
	}

	public Long getPersonel_unit_id() {
		return personel_unit_id;
	}

	public void setPersonel_unit_id(Long personel_unit_id) {
		this.personel_unit_id = personel_unit_id;
	}

	public Long getDivision_id() {
		return division_id;
	}

	public void setDivision_id(Long division_id) {
		this.division_id = division_id;
	}

	public Long getSubsidiary_id() {
		return subsidiary_id;
	}

	public void setSubsidiary_id(Long subsidiary_id) {
		this.subsidiary_id = subsidiary_id;
	}

	public Long getPersonel_id() {
		return personel_id;
	}

	public void setPersonel_id(Long personel_id) {
		this.personel_id = personel_id;
	}

	public Long getReportsTo_id() {
		return reportsTo_id;
	}

	public void setReportsTo_id(Long reportsTo_id) {
		this.reportsTo_id = reportsTo_id;
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
				if(personels.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
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
				
				if(getPersonel_dept_id() != null && getPersonel_dept_id() > 0) {
					Object dp = gDAO.find(Department.class, getPersonel_dept_id());
					if(dp != null)
						params.put("department", (Department)dp);
				}
				
				if(getPersonel_unit_id() != null && getPersonel_unit_id() > 0) {
					Object obj = gDAO.find(Unit.class, getPersonel_unit_id());
					if(obj != null)
						params.put("unit", (Unit)obj);
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
				if(personelsWithoutUsers.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
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

	@SuppressWarnings("unchecked")
	public Vector<PartnerPersonel> getUnitHead() {
		boolean research = true;
		if(unitHead == null || unitHead.size() == 0)
			research = true;
		else if(unitHead.size() > 0)
		{
			if(getPersonel_unit_id() != null && getPersonel_unit_id() > 0L)
			{
				if(unitHead.get(0).getUnit() != null && unitHead.get(0).getUnit().getId().longValue() == getPersonel_unit_id().longValue())
					research = false;
			}
		}
		if(research)
		{
			unitHead = null;
			if(getPartner() != null && getPersonel_unit_id() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				params.put("unit.id", getPersonel_unit_id());
				params.put("unitHead", true);
				Object dpsObj = gDAO.search("PartnerPersonel", params);
				if(dpsObj != null)
				{
					unitHead = (Vector<PartnerPersonel>)dpsObj;
				}
			}
		}
		return unitHead;
	}

	public void setUnitHead(Vector<PartnerPersonel> unitHead) {
		this.unitHead = unitHead;
	}

	public StreamedContent getPersonelsExcelTemplate() {
		return personelsExcelTemplate;
	}

	public void setPersonelsExcelTemplate(StreamedContent personelsExcelTemplate) {
		this.personelsExcelTemplate = personelsExcelTemplate;
	}

	public UploadedFile getPersonelsBatchExcel() {
		return personelsBatchExcel;
	}

	public void setPersonelsBatchExcel(UploadedFile personelsBatchExcel) {
		this.personelsBatchExcel = personelsBatchExcel;
	}

	public boolean isAutoCreate() {
		return autoCreate;
	}

	public void setAutoCreate(boolean autoCreate) {
		this.autoCreate = autoCreate;
	}

	public Long getDriverGrade_id() {
		return driverGrade_id;
	}

	public void setDriverGrade_id(Long driverGrade_id) {
		this.driverGrade_id = driverGrade_id;
	}

	public Long getDriver_id() {
		return driver_id;
	}

	public void setDriver_id(Long driver_id) {
		this.driver_id = driver_id;
	}

	public String getDrvLicenseNo() {
		return drvLicenseNo;
	}

	public void setDrvLicenseNo(String drvLicenseNo) {
		this.drvLicenseNo = drvLicenseNo;
	}

	public String getDrvNo() {
		return drvNo;
	}

	public void setDrvNo(String drvNo) {
		this.drvNo = drvNo;
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
	
	public void setCurDriver(long driver_id) {
		System.out.println("settting driver_id: " + driver_id);
		if(drivers != null) {
			for(PartnerDriver pd : drivers) {
				if(pd.getId().longValue() == driver_id) {
					driver = pd;
					System.out.println("set.");
					break;
				}
			}
		}
	}

	public PartnerDriver getDriver() {
		if(driver == null)
			driver = new PartnerDriver();
		return driver;
	}

	public void setDriver(PartnerDriver driver) {
		this.driver = driver;
		System.out.println("setting driver");
	}

	public PartnerDriver getSelDriver() {
		return selDriver;
	}

	public void setSelDriver(PartnerDriver selDriver) {
		this.selDriver = selDriver;
	}

	@SuppressWarnings("unchecked")
	public Vector<PartnerDriver> getDrivers() {
		boolean research = true;
		if(drivers == null || drivers.size() == 0)
			research = true;
		else if(drivers.size() > 0) {
			if(getPartner() != null) {
				if(drivers.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research) {
			drivers = null;
			if(getPartner() != null) {
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				Object dpsObj = gDAO.search("PartnerDriver", params);
				if(dpsObj != null)
				{
					drivers = (Vector<PartnerDriver>)dpsObj;
					for(PartnerDriver pd : drivers)
					{
						params = new Hashtable<String, Object>();
						params.put("driver", pd);
						params.put("active", true);
						
						Object pdvObj = gDAO.search("VehicleDriver", params);
						if(pdvObj != null)
						{
							Vector<VehicleDriver> pdvList = (Vector<VehicleDriver>)pdvObj;
							for(VehicleDriver vd : pdvList)
								pd.setVehicle(vd.getVehicle());
						}
					}
				}
			}
		}
		return drivers;
	}

	public void setDrivers(Vector<PartnerDriver> drivers) {
		this.drivers = drivers;
	}

	public Long getApprover_id() {
		return approver_id;
	}

	public void setApprover_id(Long approver_id) {
		this.approver_id = approver_id;
	}

	public PartnerDriverOvertime getOvertime() {
		if(overtime == null)
			overtime = new PartnerDriverOvertime();
		return overtime;
	}

	public void setOvertime(PartnerDriverOvertime overtime) {
		this.overtime = overtime;
	}

	public Date getOvertimeStDate() {
		return overtimeStDate;
	}

	public void setOvertimeStDate(Date overtimeStDate) {
		this.overtimeStDate = overtimeStDate;
	}

	public Date getOvertimeEndDate() {
		return overtimeEndDate;
	}

	public void setOvertimeEndDate(Date overtimeEndDate) {
		this.overtimeEndDate = overtimeEndDate;
	}

	public void resetOvertimes()
	{
		setOvertimes(null);
	}
	@SuppressWarnings("unchecked")
	public Vector<PartnerDriverOvertime> getOvertimes() {
		boolean research = true;
		if(overtimes == null || overtimes.size() == 0)
			research = true;
		else if(overtimes.size() > 0)
		{
			if(getDriver() != null)
			{
				if(overtimes.get(0).getDriver().getId().longValue() == getDriver().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			overtimes = null;
			if(getDriver() != null && getOvertimeStDate() != null && getOvertimeEndDate() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from PartnerDriverOvertime e where e.driver = :driver and (e.tranDate between :stdt and :enddt)");
				q.setParameter("driver", getDriver());
				q.setParameter("stdt", getOvertimeStDate());
				q.setParameter("enddt", getOvertimeEndDate());
				
				Object dpsObj = gDAO.search(q, 0);
				if(dpsObj != null)
				{
					overtimes = (Vector<PartnerDriverOvertime>)dpsObj;
				}
				gDAO.destroy();
			}
		}
		return overtimes;
	}

	public void setOvertimes(Vector<PartnerDriverOvertime> overtimes) {
		this.overtimes = overtimes;
	}

	@SuppressWarnings("unchecked")
	public PartnerDriverOvertimeRequest getOvertimeReq() {
		if(overtimeReq == null)
			overtimeReq = new PartnerDriverOvertimeRequest();
		if(overtimeReq.getAmountPerHour() <= 0) {
			GeneralDAO gDAO = new GeneralDAO();
			Query q2 = gDAO.createQuery("Select e from PartnerSetting e where e.partner.id=:partner_id");
			q2.setParameter("partner_id", dashBean.getUser().getPartner().getId());
			Object psObj = gDAO.search(q2, 0);
			if(psObj != null) {
				PartnerSetting sett = null;
				Vector<PartnerSetting> psList = (Vector<PartnerSetting>)psObj;
				for(PartnerSetting ps : psList)
					sett = ps;
				if(sett != null)
					overtimeReq.setAmountPerHour(sett.getOverTimeAmountPerHour());
			}
			gDAO.destroy();
		}
		return overtimeReq;
	}

	public void setOvertimeReq(PartnerDriverOvertimeRequest overtimeReq) {
		this.overtimeReq = overtimeReq;
	}

	public PartnerDriverOvertimeRequest getSelectedOvertimeReq() {
		return selectedOvertimeReq;
	}

	public void setSelectedOvertimeReq(
			PartnerDriverOvertimeRequest selectedOvertimeReq) {
		this.selectedOvertimeReq = selectedOvertimeReq;
	}

	@SuppressWarnings("unchecked")
	public Vector<PartnerDriverOvertimeRequest> getMyOvertimeReqs() {
		if(myOvertimeReqs == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Query q = gDAO.createQuery("Select e from PartnerDriverOvertimeRequest e where e.createdBy = :createdBy");
			q.setParameter("createdBy", dashBean.getUser());
			
			Object dpsObj = gDAO.search(q, 0);
			if(dpsObj != null)
			{
				myOvertimeReqs = (Vector<PartnerDriverOvertimeRequest>)dpsObj;
			}
			gDAO.destroy();
		}
		return myOvertimeReqs;
	}

	public void setMyOvertimeReqs(
			Vector<PartnerDriverOvertimeRequest> myOvertimeReqs) {
		this.myOvertimeReqs = myOvertimeReqs;
	}

	public void resetMyOvertimes()
	{
		setMyOvertimes(null);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<PartnerDriverOvertime> getMyOvertimes() {
		if(myOvertimes == null)
		{
			if(getOvertimeStDate() != null && getOvertimeEndDate() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from PartnerDriverOvertime e where e.driver.personel = :personel and (e.tranDate between :stdt and :enddt)");
				q.setParameter("personel", dashBean.getUser().getPersonel());
				q.setParameter("stdt", getOvertimeStDate());
				q.setParameter("enddt", getOvertimeEndDate());
				
				Object dpsObj = gDAO.search(q, 0);
				if(dpsObj != null)
				{
					myOvertimes = (Vector<PartnerDriverOvertime>)dpsObj;
				}
				gDAO.destroy();
			}
		}
		return myOvertimes;
	}

	public void setMyOvertimes(Vector<PartnerDriverOvertime> myOvertimes) {
		this.myOvertimes = myOvertimes;
	}

	@SuppressWarnings("unchecked")
	public Vector<PartnerDriverOvertimeRequest> getPendingOvertimeReqs() {
		boolean research = true;
		if(pendingOvertimeReqs == null || pendingOvertimeReqs.size() == 0)
			research = true;
		else if(pendingOvertimeReqs.size() > 0) {
			if(getPartner() != null) {
				if(pendingOvertimeReqs.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research) {
			pendingOvertimeReqs = null;
			if(getPartner() != null) {
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from PartnerDriverOvertimeRequest e where e.partner = :partner and e.approvalStatus = 'PENDING' and e.approvedBy=:approvedBy");
				q.setParameter("partner", getPartner());
				q.setParameter("approvedBy", dashBean.getUser());
				
				Object dpsObj = gDAO.search(q, 0);
				if(dpsObj != null) {
					pendingOvertimeReqs = (Vector<PartnerDriverOvertimeRequest>)dpsObj;
				}
				gDAO.destroy();
			}
		}
		return pendingOvertimeReqs;
	}

	public void setPendingOvertimeReqs(
			Vector<PartnerDriverOvertimeRequest> pendingOvertimeReqs) {
		this.pendingOvertimeReqs = pendingOvertimeReqs;
	}

	public PartnerDriverQuery getDvrQuery() {
		if(dvrQuery == null)
			dvrQuery = new PartnerDriverQuery();
		return dvrQuery;
	}

	public void setDvrQuery(PartnerDriverQuery dvrQuery) {
		this.dvrQuery = dvrQuery;
	}

	public PartnerDriverQuery getSelDvrQuery() {
		return selDvrQuery;
	}

	public void setSelDvrQuery(PartnerDriverQuery selDvrQuery) {
		this.selDvrQuery = selDvrQuery;
	}

	public Date getQueryStDate() {
		return queryStDate;
	}

	public void setQueryStDate(Date queryStDate) {
		this.queryStDate = queryStDate;
	}

	public Date getQueryEndDate() {
		return queryEndDate;
	}

	public void setQueryEndDate(Date queryEndDate) {
		this.queryEndDate = queryEndDate;
	}

	public String getQueryStatus() {
		return queryStatus;
	}

	public void setQueryStatus(String queryStatus) {
		this.queryStatus = queryStatus;
	}

	public String getFinalQueryRemarks() {
		return finalQueryRemarks;
	}

	public void setFinalQueryRemarks(String finalQueryRemarks) {
		this.finalQueryRemarks = finalQueryRemarks;
	}

	public boolean isPunishDriver() {
		return punishDriver;
	}

	public void setPunishDriver(boolean punishDriver) {
		this.punishDriver = punishDriver;
	}

	public void resetQueries() {
		setDvrQueries(null);
	}
	@SuppressWarnings("unchecked")
	public Vector<PartnerDriverQuery> getDvrQueries() {
		boolean research = true;
		if(dvrQueries == null || dvrQueries.size() == 0)
			research = true;
		else if(dvrQueries.size() > 0)
		{
			if(getDriver() != null)
			{
				if(dvrQueries.get(0).getDriver().getId().longValue() == getDriver().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			dvrQueries = null;
			if(getDriver() != null && getQueryStDate() != null && getQueryEndDate() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = null;
				if(getQueryStatus() != null && !getQueryStatus().isEmpty()) {
					q = gDAO.createQuery("Select e from PartnerDriverQuery e where e.driver = :driver and (e.tranDate between :stdt and :enddt) and e.status=:status");
					q.setParameter("driver", getDriver());
					q.setParameter("stdt", getQueryStDate());
					q.setParameter("enddt", getQueryEndDate());
					q.setParameter("status", getQueryStatus());
				} else {
					q = gDAO.createQuery("Select e from PartnerDriverQuery e where e.driver = :driver and (e.tranDate between :stdt and :enddt)");
					q.setParameter("driver", getDriver());
					q.setParameter("stdt", getQueryStDate());
					q.setParameter("enddt", getQueryEndDate());
				}
				
				Object dpsObj = gDAO.search(q, 0);
				if(dpsObj != null) {
					dvrQueries = (Vector<PartnerDriverQuery>)dpsObj;
				}
				gDAO.destroy();
			}
		}
		return dvrQueries;
	}

	public void setDvrQueries(Vector<PartnerDriverQuery> dvrQueries) {
		this.dvrQueries = dvrQueries;
	}

	@SuppressWarnings("unchecked")
	public Vector<PartnerDriverQuery> getMyPendingQueries() {
		if(dashBean.getDriver() != null && dashBean.getDriver().getId() != null) {
			if(myPendingQueries == null || myPendingQueries.size() == 0) {
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from PartnerDriverQuery e where e.driver = :driver and e.status=:status");
				q.setParameter("driver", dashBean.getDriver());
				q.setParameter("status", "PENDING");
				
				Object dpsObj = gDAO.search(q, 0);
				if(dpsObj != null)
					myPendingQueries = (Vector<PartnerDriverQuery>)dpsObj;
				else
					myPendingQueries = new Vector<PartnerDriverQuery>();
				gDAO.destroy();
			}
		} else
			myPendingQueries = new Vector<PartnerDriverQuery>();
		return myPendingQueries;
	}

	public void setMyPendingQueries(Vector<PartnerDriverQuery> myPendingQueries) {
		this.myPendingQueries = myPendingQueries;
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
				if(driverGrades.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
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
				if(mroles.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
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
						
						List<Report> mrReports = new ArrayList<Report>();
						params = new Hashtable<String, Object>();
						params.put("role", mr);
						Object mrrsObj = gDAO.search("MRoleReport", params);
						if(mrrsObj != null)
						{
							Vector<MRoleReport> mrrsList = (Vector<MRoleReport>)mrrsObj;
							for(MRoleReport mrf : mrrsList)
							{
								mrReports.add(mrf.getReport());
							}
						}
						mr.setReports(mrReports);
						
						List<MDash> mrDashs = new ArrayList<MDash>();
						params = new Hashtable<String, Object>();
						params.put("role", mr);
						Object mrdsObj = gDAO.search("MDashRole", params);
						if(mrdsObj != null)
						{
							Vector<MDashRole> mrrsList = (Vector<MDashRole>)mrdsObj;
							for(MDashRole mrf : mrrsList)
							{
								mrDashs.add(mrf.getDash());
							}
						}
						mr.setDashs(mrDashs);
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
					params.put("appTypeModule.appTypeVersion", getSub().getAppTypeVersion());
					Object mdsObj = gDAO.search("ApplicationTypeFunction", params);
					if(mdsObj != null)
					{
						Vector<ApplicationTypeFunction> mdsList = (Vector<ApplicationTypeFunction>)mdsObj;
						for(ApplicationTypeFunction e : mdsList)
						{
							partnerFunctions.add(e.getFunction());
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

	@SuppressWarnings("unchecked")
	public Vector<Report> getPartnerReports() {
		boolean research = false;
		if(partnerReports == null || partnerReports.size() == 0)
			research = true;
		
		if(research)
		{
			GeneralDAO gDAO = new GeneralDAO();
			partnerReports = new Vector<Report>();
			
			if(getPartner() != null && !getPartner().isSattrak())
			{
				if(getSub() != null)
				{
					// this is a subscription based loading of the functions
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("appTypeVersion", getSub().getAppTypeVersion());
					Object mdsObj = gDAO.search("ApplicationTypeReport", params);
					if(mdsObj != null)
					{
						Vector<ApplicationTypeReport> mdsList = (Vector<ApplicationTypeReport>)mdsObj;
						for(ApplicationTypeReport e : mdsList)
						{
							partnerReports.add(e.getReport());
						}
					}
				}
			}
			else if(getPartner() != null)// load all functions, this is a sattrak user
			{
				Object fsObj = gDAO.findAll("Report");
				if(fsObj != null)
				{
					partnerReports = (Vector<Report>)fsObj;
				}
			}
		}
		return partnerReports;
	}

	public void setPartnerReports(Vector<Report> partnerReports) {
		this.partnerReports = partnerReports;
	}

	@SuppressWarnings("unchecked")
	public Vector<MDash> getPartnerDashs() {
		boolean research = false;
		if(partnerDashs == null || partnerDashs.size() == 0)
			research = true;
		
		if(research)
		{
			GeneralDAO gDAO = new GeneralDAO();
			partnerDashs = new Vector<MDash>();
			
			if(getPartner() != null && !getPartner().isSattrak())
			{
				if(getSub() != null)
				{
					// this is a subscription based loading of the functions
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("appTypeVersion", getSub().getAppTypeVersion());
					Object mdsObj = gDAO.search("ApplicationTypeDash", params);
					if(mdsObj != null)
					{
						Vector<ApplicationTypeDash> mdsList = (Vector<ApplicationTypeDash>)mdsObj;
						for(ApplicationTypeDash e : mdsList)
						{
							partnerDashs.add(e.getDash());
						}
					}
				}
			}
			else if(getPartner() != null)// load all functions, this is a sattrak user
			{
				Object fsObj = gDAO.findAll("MDash");
				if(fsObj != null)
				{
					partnerDashs = (Vector<MDash>)fsObj;
				}
			}
		}
		return partnerDashs;
	}

	public void setPartnerDashs(Vector<MDash> partnerDashs) {
		this.partnerDashs = partnerDashs;
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
				if(users.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
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
	
	public Date getOvertimeSubmitMinDate() {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -7);
		return c.getTime();
	}
	
	public Date getOvertimeSubmitMaxDate() {
		Calendar c = Calendar.getInstance();
		return c.getTime();
	}
	
	public Long getVehicle_id() {
		return vehicle_id;
	}

	public void setVehicle_id(Long vehicle_id) {
		this.vehicle_id = vehicle_id;
	}

	public Date getAudit_st() {
		return audit_st;
	}

	public void setAudit_st(Date audit_st) {
		this.audit_st = audit_st;
	}

	public Date getAudit_end() {
		return audit_end;
	}

	public void setAudit_end(Date audit_end) {
		this.audit_end = audit_end;
	}

	public void resetAudits()
	{
		setAudits(null);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Audit> getAudits()
	{
		boolean research = true;
		if(audits == null || audits.size() == 0)
			research = true;
		else if(audits.size() > 0)
		{
			if(getPartner() != null)
			{
				if(audits.get(0).getUser().getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			audits = null;
			GeneralDAO gDAO = new GeneralDAO();
			Object auditsObj = null;
			if(getPartner() != null)
			{
				if(getAudit_end() != null && getAudit_st() != null)
				{
					String qry = "Select e from Audit e where e.user.partner=:partner and (e.action_dt between :st and :et) order by e.action_dt desc";
					if(getVehicle_id() != null && getVehicle_id() > 0)
						qry = "Select e from Audit e where e.vehicle.id=:v_id and e.user.partner=:partner and (e.action_dt between :st and :et) order by e.action_dt desc";
					Query q = gDAO.createQuery(qry);
					if(getVehicle_id() != null && getVehicle_id() > 0)
						q.setParameter("v_id", getVehicle_id());
					q.setParameter("partner", getPartner());
					q.setParameter("st", getAudit_st());
					q.setParameter("et", getAudit_end());
					auditsObj = gDAO.search(q, 0);
				}
				else
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("user.partner", getPartner());
					if(getVehicle_id() != null && getVehicle_id() > 0)
						params.put("vehicle.id", getVehicle_id());
					auditsObj = gDAO.search("Audit", params);
				}
				if(auditsObj != null)
				{
					audits = (Vector<Audit>)auditsObj;
				}
			}
		}
		return audits;
	}

	public void setAudits(Vector<Audit> audits) {
		this.audits = audits;
	}

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public String getApprovalComment() {
		return approvalComment;
	}

	public void setApprovalComment(String approvalComment) {
		this.approvalComment = approvalComment;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
