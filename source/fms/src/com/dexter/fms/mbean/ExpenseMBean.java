package com.dexter.fms.mbean;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.persistence.Query;
import javax.servlet.ServletContext;

import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import com.dexter.common.util.Emailer;
import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.Notification;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.app.Budget;
import com.dexter.fms.model.app.Expense;
import com.dexter.fms.model.app.ExpenseRequest;
import com.dexter.fms.model.app.ExpenseType;
import com.dexter.fms.model.app.Vehicle;

@ManagedBean(name = "expenseBean")
@SessionScoped
public class ExpenseMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-FleetMBean");
	
	private FacesMessage msg = null;
	
	private Long partner_id;
	private Partner partner;
	
	private ExpenseType expType;
	private Vector<ExpenseType> expTypes;
	
	private Budget budget;
	private Vector<Budget> setupbudgets, budgets;
	
	private Long vehicle_id;
	private Long staff_id;
	
	private Long expType_id;
	private Expense exp;
	private Vector<Expense> exps;
	private Date start_dt, end_dt;
	private StreamedContent expensesExcelTemplate;
	private UploadedFile expensesBatchExcel;
	
	private Long approvalUser_id;
	private ExpenseRequest expRequest;
	private Vector<ExpenseRequest> myExpRequests, mySubExpRequests, pendingExpRequests;
	private String approvalStatus; // PENDING, APPROVED, DENIED
	private String approvalComment; // comment for approval status
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public ExpenseMBean()
	{
		InputStream stream2 = ((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/resources/templates/fms_batchload_expenses.xls");  
		expensesExcelTemplate = new DefaultStreamedContent(stream2, "application/vnd.ms-excel", "fms_batchload_expenses.xls");
	}
	
	public void attendToExpenseRequest()
	{
		if(getPendingExpRequests() != null && getPendingExpRequests().size() > 0)
		{
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = false;
			for(ExpenseRequest expR : getPendingExpRequests())
			{
				if(expR.isSelected())
				{
					expR.setApprovalComment(getApprovalComment());
					expR.setApprovalStatus(getApprovalStatus());
					expR.setApproval_dt(new Date());
					
					ret = gDAO.update(expR);
					if(!ret)
						break;
					else
					{
						if(getApprovalStatus().equalsIgnoreCase("APPROVED"))
						{
							Expense exp = new Expense();
							exp.setAmount(expR.getAmount());
							exp.setCreatedBy(expR.getCreatedBy());
							exp.setCrt_dt(new Date());
							exp.setExpense_dt(expR.getExpense_dt());
							exp.setPartner(expR.getPartner());
							exp.setRemarks(expR.getRemarks());
							exp.setType(expR.getType());
							
							ret = gDAO.save(exp);
							if(!ret)
								break;
							else
							{
								expR.setExpense(exp);
								ret = gDAO.update(expR);
								if(!ret)
									break;
							}
						}
					}
				}
			}
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Expense request(s) attended to successfully!");
				
				setPendingExpRequests(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during save: " + gDAO.getMessage());
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No entry found in pending list!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	@SuppressWarnings("deprecation")
	public void saveBatchExpRequest()
	{
		if(getMyExpRequests() != null && getMyExpRequests().size() > 0)
		{
			ArrayList<String> emails = new ArrayList<String>(), usernames = new ArrayList<String>();
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = false;
			for(ExpenseRequest expR : getMyExpRequests())
			{
				ret = gDAO.save(expR);
				if(ret)
				{
					try
					{
						String to = expR.getApprovalUser().getPersonel().getEmail();
						if(to != null && to.trim().length() > 0 && !emails.contains(to))
							emails.add(to);
					} catch(Exception ex){}
					try
					{
						if(!usernames.contains(expR.getApprovalUser().getUsername()))
						{
							usernames.add(expR.getApprovalUser().getUsername());
							// Send notification and email to the approval user
							Notification n = new Notification();
							n.setCrt_dt(new Date());
							n.setUser(expR.getApprovalUser());
							n.setMessage("You have pending expense request from '" + dashBean.getUser().getPersonel().getFirstname() + " " + dashBean.getUser().getPersonel().getLastname() + "'. Submitted: " + new Date().toLocaleString());
							n.setNotified(false);
							n.setSubject("Expense Request");
							n.setPage_url("approve_expenses");
							
							ret = gDAO.save(n);
							if(!ret)
								break;
							
							
						}
					} catch(Exception ex){}
				}
				else
					break;
			}
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Expense request(s) save successfully!");
				
				if(emails.size() > 0)
				{
					final String[] to = new String[emails.size()];
					for(int i=0; i<to.length; i++)
						to[i] = emails.get(i);
					final String subject = "Expense Request - Batch";
					StringBuilder sb = new StringBuilder();
					sb.append("<html><body>");
					sb.append("<p><strong>Hello All</strong><br/></p>");
					sb.append("<p>Please be informed that User: '" + dashBean.getUser().getPersonel().getFirstname() + " " + dashBean.getUser().getPersonel().getLastname() + "' just submitted a batch expense request to " + to.length + " person(s), which you are one of, for approval.<br/></p>");
					sb.append("<p>Regards<br/>FMS Team</p>");
					sb.append("</body></html>");
					final String body = sb.toString();
					Thread t = new Thread()
					{
						public void run()
						{
							try
							{
								Emailer.sendEmail("fms@sattrakservices.com", to, subject, body);
							} catch(Exception ex){}
						}
					};
					t.start();
				}
				
				setMyExpRequests(null);
				setPendingExpRequests(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during save: " + gDAO.getMessage());
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No entry found in batch!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	public void addExpRequestToBatch()
	{
		if(getPartner() != null)
		{
			if(getExpRequest().getAmount() > 0 &&
					getExpRequest().getExpense_dt() != null && getExpType_id() != null && getExpType_id() > 0 &&
					getApprovalUser_id() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Object expType = gDAO.find(ExpenseType.class, getExpType_id());
				if(expType != null)
				{
					ExpenseType et = (ExpenseType)expType;
					if(!et.isSystemObj())
					{
						Object approvalUObj = gDAO.find(PartnerUser.class, getApprovalUser_id());
						if(approvalUObj != null)
						{
							PartnerUser approveUser = (PartnerUser)approvalUObj;
							Vehicle vehicle = null;
							if(getVehicle_id() != null)
							{
								Object vehicleObj = gDAO.find(Vehicle.class, getVehicle_id());
								if(vehicleObj != null) vehicle = (Vehicle)vehicleObj;
							}
							PartnerPersonel staff = null;
							if(getStaff_id() != null)
							{
								Object staffObj = gDAO.find(PartnerPersonel.class, getStaff_id());
								if(staffObj != null) staff = (PartnerPersonel)staffObj;
							}
							getExpRequest().setVehicle(vehicle);
							getExpRequest().setPersonel(staff);
							getExpRequest().setType(et);
							getExpRequest().setCreatedBy(dashBean.getUser());
							getExpRequest().setCrt_dt(new Date());
							getExpRequest().setPartner(getPartner());
							getExpRequest().setApprovalUser(approveUser);
							getExpRequest().setApprovalStatus("PENDING");
							getExpRequest().setRequest_dt(new Date());
							
							if(getMyExpRequests() == null)
								setMyExpRequests(new Vector<ExpenseRequest>());
							getMyExpRequests().add(getExpRequest());
							
							setExpRequest(null);
							
							msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Request added to batch successfully.");
						}
						else
						{
							msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Unknown approval user selected.");
						}
					}
					else
					{
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Can't manually insert expense/request data for a system expense type.");
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid expense type selected!");
				}
				gDAO.destroy();
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No partner found!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	public void removeExpRequestFromBatch(int pos)
	{
		if(getMyExpRequests() != null)
		{
			if(pos < 0 || pos >= getMyExpRequests().size())
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid position for request to remove!");
			}
			else
			{
				getMyExpRequests().remove(pos);
				getMyExpRequests().trimToSize();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Request removed from batch successfully!");
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No request in batch!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	@SuppressWarnings("deprecation")
	public void saveExpRequest()
	{
		if(getPartner() != null)
		{
			if(getExpRequest().getAmount() > 0 &&
					getExpRequest().getExpense_dt() != null && getExpType_id() != null && getExpType_id() > 0 &&
					getApprovalUser_id() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Object expType = gDAO.find(ExpenseType.class, getExpType_id());
				if(expType != null)
				{
					ExpenseType et = (ExpenseType)expType;
					if(!et.isSystemObj())
					{
						Object approvalUObj = gDAO.find(PartnerUser.class, getApprovalUser_id());
						if(approvalUObj != null)
						{
							PartnerUser approveUser = (PartnerUser)approvalUObj;
							
							Vehicle vehicle = null;
							if(getVehicle_id() != null)
							{
								Object vehicleObj = gDAO.find(Vehicle.class, getVehicle_id());
								if(vehicleObj != null) vehicle = (Vehicle)vehicleObj;
							}
							PartnerPersonel staff = null;
							if(getStaff_id() != null)
							{
								Object staffObj = gDAO.find(PartnerPersonel.class, getStaff_id());
								if(staffObj != null) staff = (PartnerPersonel)staffObj;
							}
							getExpRequest().setPersonel(staff);
							getExpRequest().setVehicle(vehicle);
							getExpRequest().setType(et);
							getExpRequest().setCreatedBy(dashBean.getUser());
							getExpRequest().setCrt_dt(new Date());
							getExpRequest().setPartner(getPartner());
							getExpRequest().setApprovalUser(approveUser);
							getExpRequest().setApprovalStatus("PENDING");
							getExpRequest().setRequest_dt(new Date());
							
							gDAO.startTransaction();
							boolean ret = gDAO.save(getExpRequest());
							if(ret)
							{
								// Send notification and email to the approval user
								Notification n = new Notification();
								n.setCrt_dt(new Date());
								n.setUser(approveUser);
								n.setMessage("You have pending expense request from '" + dashBean.getUser().getPersonel().getFirstname() + " " + dashBean.getUser().getPersonel().getLastname() + "'. Submitted: " + new Date().toLocaleString());
								n.setNotified(false);
								n.setSubject("Expense Request");
								n.setPage_url("approve_expenses");
								
								gDAO.save(n);
								
								gDAO.commit();
								
								if(approveUser.getPersonel().getEmail() != null && approveUser.getPersonel().getEmail().trim().length() > 0)
								{
									final String to = approveUser.getPersonel().getEmail();
									final String subject = "Expense Request";
									StringBuilder sb = new StringBuilder();
									sb.append("<html><body>");
									sb.append("<p><strong>Dear " + approveUser.getPersonel().getFirstname() + "</strong><br/></p>");
									sb.append("<p>Please be informed that User: '" + dashBean.getUser().getPersonel().getFirstname() + " " + dashBean.getUser().getPersonel().getLastname() + "' just submitted an expense request to you for approval.<br/></p>");
									sb.append("<p>Regards<br/>FMS Team</p>");
									sb.append("</body></html>");
									final String body = sb.toString();
									Thread t = new Thread()
									{
										public void run()
										{
											try
											{
												Emailer.sendEmail("fms@sattrakservices.com", new String[]{to}, subject, body);
											} catch(Exception ex){}
										}
									};
									t.start();
								}
								
								msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Expense request save successfully!");
								setExpRequest(null);
								setExpType_id(null);
								setMyExpRequests(null);
								setPendingExpRequests(null);
							}
							else
							{
								gDAO.rollback();
								msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during save: " + gDAO.getMessage());
							}
						}
						else
						{
							msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Unknown approval user selected.");
						}
					}
					else
					{
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Can't manually insert expense/request data for a system expense type.");
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid expense type selected!");
				}
				gDAO.destroy();
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No partner found!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void deleteBudget()
	{
		if(getBudget().getId() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = gDAO.remove(getBudget());
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Budget deleted successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setBudget(null);
				setBudgets(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to delete budget. " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid budget selected!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
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
	
	public void saveExp()
	{
		if(getPartner() != null)
		{
			if(getExp().getAmount() > 0 &&
					getExp().getExpense_dt() != null && getExpType_id() != null && getExpType_id() > 0)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Object expType = gDAO.find(ExpenseType.class, getExpType_id());
				if(expType != null)
				{
					if(getExp().getExpense_dt().after(new Date()))
					{
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Future date selected. Not allowed!");
					}
					else
					{
						ExpenseType et = (ExpenseType)expType;
						if(!et.isSystemObj())
						{
							Vehicle vehicle = null;
							if(getVehicle_id() != null)
							{
								Object vehicleObj = gDAO.find(Vehicle.class, getVehicle_id());
								if(vehicleObj != null) vehicle = (Vehicle)vehicleObj;
							}
							PartnerPersonel staff = null;
							if(getStaff_id() != null)
							{
								Object staffObj = gDAO.find(PartnerPersonel.class, getStaff_id());
								if(staffObj != null) staff = (PartnerPersonel)staffObj;
							}
							getExp().setVehicle(vehicle);
							getExp().setPersonel(staff);
							getExp().setType(et);
							getExp().setCreatedBy(dashBean.getUser());
							getExp().setCrt_dt(new Date());
							getExp().setPartner(getPartner());
							
							gDAO.startTransaction();
							boolean ret = gDAO.save(getExp());
							if(ret)
							{
								gDAO.commit();
								msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Expense save successfully!");
								setExp(null);
								setExpType_id(null);
								setExps(null);
								dashBean.resetExps();
							}
							else
							{
								gDAO.rollback();
								msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during save: " + gDAO.getMessage());
							}
						}
						else
						{
							msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Can't manually insert expense data for a system expense type.");
						}
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid expense type selected!");
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No partner found!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	@SuppressWarnings("unchecked")
	public void saveBudgetSetup()
	{
		if(getSetupbudgets() != null && getSetupbudgets().size() > 0 && 
				getBudget().getStart_dt() != null && getBudget().getEnd_dt() != null)
		{
			if(getBudget().getStart_dt().before(getBudget().getEnd_dt()))
			{
				GeneralDAO gDAO = new GeneralDAO();
				gDAO.startTransaction();
				boolean ret = false;
				int count = 0;
				for(Budget b : getSetupbudgets())
				{
					if(b.getAmount() != null && b.getAmount().doubleValue() > 0)
					{
						boolean exists = false;
						Query q = gDAO.createQuery("Select e from Budget e where e.type=:type and e.partner=:partner and e.active=:active");
						q.setParameter("type", b.getType());
						q.setParameter("partner", b.getPartner());
						q.setParameter("active", true);
						//q.setParameter("end_dt", new Date()); //getBudget().getEnd_dt()
						
						Vector<Budget> qretList = null;
						Object qret = gDAO.search(q, 0);
						if(qret != null)
						{
							qretList = (Vector<Budget>)qret;
							if(qretList.size() > 0)
							{
								for(Budget e : qretList)
								{
									if(e.getEnd_dt().after(new Date()))
									{
										exists = true;
										break;
									}
								}
							}
						}
						
						if(!exists)
						{
							count++;
							
							if(qretList != null && qretList.size() > 0)
							{
								for(Budget e : qretList)
								{
									e.setActive(false);
									gDAO.update(e);
								}
							}
							
							b.setCreatedBy(dashBean.getUser());
							b.setCrt_dt(new Date());
							b.setEnd_dt(getBudget().getEnd_dt());
							b.setStart_dt(getBudget().getStart_dt());
							b.setRemarks(getBudget().getRemarks());
							b.setActive(true);
							
							ret = gDAO.save(b);
							if(!ret)
								break;
						}
					}
				}
				
				if(count > 0)
				{
					if(ret)
					{
						gDAO.commit();
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Budget setup successfully.");
						
						setBudget(null);
						setSetupbudgets(null);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during setup: " + gDAO.getMessage());
					}
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No entry could be processed. Please input valid amounts for the expense types and ensure the budgets do not already exist.");
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid date values.");
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void saveExpType()
	{
		if(getExpType().getName() != null && getPartner() != null)
		{
			getExpType().setCreatedBy(dashBean.getUser());
			getExpType().setCrt_dt(new Date());
			getExpType().setPartner(getPartner());
			getExpType().setSystemObj(false);
			
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = gDAO.save(getExpType());
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Expense type created successfully.");
				
				setExpType(null);
				setExpTypes(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create expense type: " + gDAO.getMessage());
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "All fields with the '*' sign are required!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public Long getPartner_id() {
		return partner_id;
	}

	public void setPartner_id(Long partner_id) {
		this.partner_id = partner_id;
	}

	public Partner getPartner() {
		if(!dashBean.getUser().getPartner().isSattrak())
		{
			partner = dashBean.getUser().getPartner();
		}
		else
		{
			if(getPartner_id() != null)
			{
				partner = (Partner)new GeneralDAO().find(Partner.class, getPartner_id());
			}
		}
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public ExpenseType getExpType() {
		if(expType == null)
			expType = new ExpenseType();
		return expType;
	}

	public void setExpType(ExpenseType expType) {
		this.expType = expType;
	}

	public void resetExpTypes()
	{
		setExpTypes(null);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<ExpenseType> getExpTypes() {
		boolean research = true;
		if(expTypes == null || expTypes.size() == 0)
			research = true;
		else if(expTypes.size() > 0)
		{
			if(getPartner() != null)
			{
				if(expTypes.get(0).getPartner() != null && expTypes.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			expTypes = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
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
		}
		return expTypes;
	}

	public void setExpTypes(Vector<ExpenseType> expTypes) {
		this.expTypes = expTypes;
	}

	public Budget getBudget() {
		if(budget == null)
			budget = new Budget();
		return budget;
	}

	public void setBudget(Budget budget) {
		this.budget = budget;
	}
	
	public void resetSetupbudgets()
	{
		setSetupbudgets(null);
	}

	public Vector<Budget> getSetupbudgets() {
		boolean research = true;
		if(setupbudgets == null || setupbudgets.size() == 0)
			research = true;
		else if(setupbudgets.size() > 0)
		{
			if(getPartner() != null)
			{
				if(setupbudgets.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			setupbudgets = null;
			if(getPartner() != null)
			{
				setupbudgets = new Vector<Budget>();
				for(ExpenseType e : getExpTypes())
				{
					Budget b = new Budget();
					b.setPartner(getPartner());
					b.setType(e);
					setupbudgets.add(b);
				}
			}
		}
		return setupbudgets;
	}

	public void setSetupbudgets(Vector<Budget> setupbudgets) {
		this.setupbudgets = setupbudgets;
	}

	@SuppressWarnings("unchecked")
	public Vector<Budget> getBudgets() {
		boolean research = true;
		if(budgets == null || budgets.size() == 0)
			research = true;
		else if(budgets.size() > 0)
		{
			if(getPartner() != null)
			{
				if(budgets.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			budgets = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				params.put("active", true);
				
				Object drvs = gDAO.search("Budget", params);
				if(drvs != null)
				{
					budgets = (Vector<Budget>)drvs;
				}
			}
		}
		return budgets;
	}

	public void setBudgets(Vector<Budget> budgets) {
		this.budgets = budgets;
	}

	public Long getVehicle_id() {
		return vehicle_id;
	}

	public void setVehicle_id(Long vehicle_id) {
		this.vehicle_id = vehicle_id;
	}

	public Long getStaff_id() {
		return staff_id;
	}

	public void setStaff_id(Long staff_id) {
		this.staff_id = staff_id;
	}

	public Long getExpType_id() {
		return expType_id;
	}

	public void setExpType_id(Long expType_id) {
		this.expType_id = expType_id;
	}

	public Expense getExp() {
		if(exp == null)
			exp = new Expense();
		return exp;
	}

	public void setExp(Expense exp) {
		this.exp = exp;
	}
	
	public void resetExps()
	{
		setExps(null);
	}

	@SuppressWarnings("unchecked")
	public Vector<Expense> getExps() {
		boolean research = true;
		if(exps == null || exps.size() == 0)
			research = true;
		else if(exps.size() > 0)
		{
			if(getPartner() != null)
			{
				if(exps.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			exps = null;
			if(getPartner() != null)
			{
				if(getStart_dt() != null && getEnd_dt() != null)
				{
					GeneralDAO gDAO = new GeneralDAO();
					
					Query q = gDAO.createQuery("Select e from Expense e where e.partner=:partner and (e.expense_dt between :start_dt and :end_dt) order by e.expense_dt desc");
					q.setParameter("partner", getPartner());
					q.setParameter("start_dt", getStart_dt());
					q.setParameter("end_dt", getEnd_dt());
					Object drvs = gDAO.search(q, 0);
					if(drvs != null)
					{
						exps = (Vector<Expense>)drvs;
					}
				}
			}
		}
		return exps;
	}

	public void setExps(Vector<Expense> exps) {
		this.exps = exps;
	}

	public Date getStart_dt() {
		return start_dt;
	}

	public void setStart_dt(Date start_dt) {
		this.start_dt = start_dt;
	}

	public Date getEnd_dt() {
		return end_dt;
	}

	public void setEnd_dt(Date end_dt) {
		this.end_dt = end_dt;
	}

	public UploadedFile getExpensesBatchExcel() {
		return expensesBatchExcel;
	}

	public void setExpensesBatchExcel(UploadedFile expensesBatchExcel) {
		this.expensesBatchExcel = expensesBatchExcel;
	}

	public Long getApprovalUser_id() {
		return approvalUser_id;
	}

	public void setApprovalUser_id(Long approvalUser_id) {
		this.approvalUser_id = approvalUser_id;
	}

	public ExpenseRequest getExpRequest() {
		if(expRequest == null)
			expRequest = new ExpenseRequest();
		return expRequest;
	}

	public void setExpRequest(ExpenseRequest expRequest) {
		this.expRequest = expRequest;
	}

	public Vector<ExpenseRequest> getMyExpRequests() {
		if(myExpRequests == null)
			myExpRequests = new Vector<ExpenseRequest>();
		return myExpRequests;
	}

	public void setMyExpRequests(Vector<ExpenseRequest> myExpRequests) {
		this.myExpRequests = myExpRequests;
	}

	@SuppressWarnings("unchecked")
	public Vector<ExpenseRequest> getMySubExpRequests() {
		boolean research = false;
		if(mySubExpRequests == null || mySubExpRequests.size() == 0)
			research = true;
		if(research)
		{
			mySubExpRequests = null;
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("createdBy", dashBean.getUser());
			params.put("approvalStatus", "PENDING");
			GeneralDAO gDAO = new GeneralDAO();
			Object retObj = gDAO.search("ExpenseRequest", params);
			if(retObj != null)
			{
				mySubExpRequests = (Vector<ExpenseRequest>)retObj;
			}
		}
		return mySubExpRequests;
	}

	public void setMySubExpRequests(Vector<ExpenseRequest> mySubExpRequests) {
		this.mySubExpRequests = mySubExpRequests;
	}

	@SuppressWarnings("unchecked")
	public Vector<ExpenseRequest> getPendingExpRequests() {
		boolean research = false;
		if(pendingExpRequests == null || pendingExpRequests.size() == 0)
			research = true;
		if(research)
		{
			pendingExpRequests = null;
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("approvalUser", dashBean.getUser());
			params.put("approvalStatus", "PENDING");
			GeneralDAO gDAO = new GeneralDAO();
			Object retObj = gDAO.search("ExpenseRequest", params);
			if(retObj != null)
			{
				pendingExpRequests = (Vector<ExpenseRequest>)retObj;
			}
		}
		return pendingExpRequests;
	}

	public void setPendingExpRequests(Vector<ExpenseRequest> pendingExpRequests) {
		this.pendingExpRequests = pendingExpRequests;
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

	public StreamedContent getExpensesExcelTemplate() {
		return expensesExcelTemplate;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
