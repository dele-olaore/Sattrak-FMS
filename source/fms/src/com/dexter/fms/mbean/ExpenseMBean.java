package com.dexter.fms.mbean;

import java.io.InputStream;
import java.io.Serializable;
import java.text.NumberFormat;
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
import com.dexter.fms.model.app.ExpenseHeader;
import com.dexter.fms.model.app.ExpenseRequest;
import com.dexter.fms.model.app.ExpenseType;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.ref.Department;
import com.dexter.fms.model.ref.Unit;

@ManagedBean(name = "expenseBean")
@SessionScoped
public class ExpenseMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-FleetMBean");
	
	private FacesMessage msg = null;
	
	private Long partner_id;
	private Partner partner;
	
	private ExpenseHeader expHeader;
	private Vector<ExpenseHeader> expHeaders;
	
	private ExpenseType expType;
	private Vector<ExpenseType> expTypes;
	
	private Budget budget;
	private Vector<Budget> setupbudgets, budgets;
	
	private Long fleet_id, vehicle_id;
	private Long division_id, division_id2, department_id, department_id2, unit_id, unit_id2, staff_id;
	
	private Long expHeader_id, editExpHeader_id, expType_id;
	private Expense exp;
	private Vector<Expense> exps;
	private Date start_dt, end_dt;
	private StreamedContent expensesExcelTemplate;
	private UploadedFile expensesBatchExcel;
	
	private Long approvalUser_id, expRequest_id;
	private ExpenseRequest expRequest;
	private Vector<ExpenseRequest> myExpRequests, mySubExpRequests, myAllSubExpRequests, pendingExpRequests, myAttendedExpRequests;
	private Vector<ExpenseRequest> approvedRequests;
	private String approvalStatus; // PENDING, APPROVED, DENIED
	private String approvalComment; // comment for approval status
	
	private Vector<PartnerPersonel> staffs;
	private Vector<Vehicle> vehicles;
	
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
			String naration = "Attend to expense request";
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = false;
			for(ExpenseRequest expR : getPendingExpRequests())
			{
				if(expR.isSelected())
				{
					naration += ", " + expR.getType().getName() + "(" + expR.getAmount() + ") :" + expR.getApprovalStatus();
					expR.setApprovalComment(getApprovalComment());
					expR.setApprovalStatus(getApprovalStatus());
					expR.setApproval_dt(new Date());
					
					ret = gDAO.update(expR);
					if(!ret)
						break;
					else
					{
						if(getApprovalStatus().equalsIgnoreCase("APPROVED")) {
							String email_message = "<html><body><p><strong>Dear " + expR.getCreatedBy().getPersonel().getFirstname() + ", </strong></p>";
							email_message += "<p>Your expense request for \"" + expR.getType().getName() + ": " + expR.getRemarks() + "\" has been approved! You can now make the expense and capture it on the platform.</p>";
							email_message += "<p>Regards<br/>FMS</p></body></html>";
							
							try {
								Emailer.sendEmail("fms@sattrakservices.com", new String[]{expR.getCreatedBy().getPersonel().getEmail()}, "Expense Approved", email_message);
							} catch(Exception ex){}
							
							/*Expense exp = new Expense();
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
							}*/
						} else if(getApprovalStatus().equalsIgnoreCase("DENIED")) {
							String email_message = "<html><body><p><strong>Dear " + expR.getCreatedBy().getPersonel().getFirstname() + ", </strong></p>";
							email_message += "<p>Your expense request for \"" + expR.getType().getName() + ": " + expR.getRemarks() + "\" has been <font color=red>denied!</font></p>";
							email_message += "<p>Regards<br/>FMS</p></body></html>";
							
							try {
								Emailer.sendEmail("fms@sattrakservices.com", new String[]{expR.getCreatedBy().getPersonel().getEmail()}, "Expense Denied", email_message);
							} catch(Exception ex){}
						}
					}
				}
			}
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Expense request(s) attended to successfully!");
				
				naration += ", Status: Success";
				
				setPendingExpRequests(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during save: " + gDAO.getMessage());
				naration += ", Status: Failed: " + gDAO.getMessage();
			}
			gDAO.destroy();
			
			dashBean.saveAudit(naration, "", null);
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
		//HttpServletRequest origRequest = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		
		if(getMyExpRequests() != null && getMyExpRequests().size() > 0) {
			String naration = "Saving batch expense requests:";
			ArrayList<String> emails = new ArrayList<String>(), usernames = new ArrayList<String>();
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = false;
			for(ExpenseRequest expR : getMyExpRequests())
			{
				naration += expR.getType().getName() + "(" + expR.getAmount() + "), ";
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
				
				naration += " Status: Success";
				
				if(usernames.size() > 0)
				{
					for(String username : usernames)
					{
						String firstname = "", table = "<table><tr><th>Type</th><th>Description</th><th>Amount</th><th>Expense Date</th><th>Request Date</th><th>Action</th></tr>";
						String email = "";
						for(ExpenseRequest expR : getMyExpRequests())
						{
							if(username.equals(expR.getApprovalUser().getUsername()))
							{
								firstname = expR.getApprovalUser().getPersonel().getFirstname();
								email = expR.getApprovalUser().getPersonel().getEmail();
								table += "<tr><td>" + expR.getType().getName() + "</td><td>" + expR.getRemarks() + "</td>";
								table += "<td>" + NumberFormat.getNumberInstance().format(expR.getAmount()) + "</td>";
								table += "<td>" + expR.getExpense_dt().toLocaleString() + "</td>";
								table += "<td>" + expR.getRequest_dt().toLocaleString() + "</td>";
								table += "<td><a href=\"http://sattrakservices.com/fms/approvalservlet?expId=" + expR.getId().longValue() + "&usrId=" + expR.getApprovalUser().getId().longValue() + "&apv=1\">Approve</a> | <a href=\"http://sattrakservices.com/fms/approvalservlet?expId=" + expR.getId().longValue() + "&usrId=" + expR.getApprovalUser().getId().longValue() + "&apv=0\">Deny</a></td></tr>";
							}
						}
						table += "</table>";
						
						String html = "<html><body>";
						
						html += "<p><strong>Dear " + firstname + "</strong>";
						html += "<p>Please be informed that personel: '" + dashBean.getUser().getPersonel().getFirstname() + " " + dashBean.getUser().getPersonel().getLastname() + "' just submitted a batch expense request which requires your approval. You can approve or deny these requests below or log in to the FMS platform to provide more comments along with your decision.</p>";
						html += "<p>" + table + "</p>";
						html += "<p>Regards<br/>FMS Team</p>";
						
						html += "</body></html>";
						final String[] to = new String[1];
						to[0] = email;
						final String subject = "Expense Request - Batch";
						final String body = html;
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
				}
				
				/*if(emails.size() > 0)
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
				}*/
				
				setMyExpRequests(null);
				setPendingExpRequests(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during save: " + gDAO.getMessage());
				
				naration += " Status: Failed: " + gDAO.getMessage();
			}
			gDAO.destroy();
			
			dashBean.saveAudit(naration, "", null);
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No entry found in batch!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	@SuppressWarnings("unchecked")
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
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("personel.id", getApprovalUser_id());
						Object approvalUObj = gDAO.search("PartnerUser", params);
						if(approvalUObj != null)
						{
							PartnerUser approveUser = null;
							Vector<PartnerUser> puList = (Vector<PartnerUser>)approvalUObj;
							for(PartnerUser pu : puList)
								approveUser = pu;
							if(approveUser != null) {
								Vehicle vehicle = null;
								if(getVehicle_id() != null) {
									Object vehicleObj = gDAO.find(Vehicle.class, getVehicle_id());
									if(vehicleObj != null) vehicle = (Vehicle)vehicleObj;
								}
								PartnerPersonel staff = null;
								if(getStaff_id() != null) {
									Object staffObj = gDAO.find(PartnerPersonel.class, getStaff_id());
									if(staffObj != null) staff = (PartnerPersonel)staffObj;
								}
								if(getDepartment_id2() != null && getDepartment_id2() > 0) {
									Object obj = gDAO.find(Department.class, getDepartment_id2());
									if(obj != null) getExpRequest().setMisDepartment((Department)obj);
								}
								if(getUnit_id2() != null && getUnit_id2() > 0) {
									Object obj = gDAO.find(Unit.class, getUnit_id2());
									if(obj != null) getExpRequest().setMisUnit((Unit)obj);
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
							} else {
								msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Unknown approval user selected.");
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
				String naration = "Create expense request for ";
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
								naration += " Vehicle: " + vehicle.getRegistrationNo();
							}
							PartnerPersonel staff = null;
							if(getStaff_id() != null)
							{
								Object staffObj = gDAO.find(PartnerPersonel.class, getStaff_id());
								if(staffObj != null) staff = (PartnerPersonel)staffObj;
								naration += " Staff: " + staff.getFirstname() + " " + staff.getLastname();
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
							
							naration += " Item: " + getExpRequest().getType().getName() + ", Amount: " + getExpRequest().getAmount();
							
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
								
								naration += ", Status: Success";
								
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
								
								naration += ", Status: Failed: " + gDAO.getMessage();
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
				
				dashBean.saveAudit(naration, "", null);
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
			String naration = "Delete budget: " + getBudget().getType().getName() + " " + getBudget().getStart_dt() + "-" + getBudget().getEnd_dt();
			GeneralDAO gDAO = new GeneralDAO();
			Budget b = null;
			Object bobj = gDAO.find(Budget.class, getBudget().getId());
			if(bobj != null) {
				b = (Budget)bobj;
			}
			if(b != null) {
				gDAO.startTransaction();
				boolean ret = gDAO.remove(b);
				if(ret) {
					gDAO.commit();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Budget deleted successfully.");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					
					naration += ", Status: Success";
					
					setBudget(null);
					setBudgets(null);
				} else {
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to delete budget. " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
					
					naration += ", Status: Failed: " + gDAO.getMessage();
				}
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Budget not found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				naration += ", Status: Failed: Budget not found!";
			}
			gDAO.destroy();
			
			dashBean.saveAudit(naration, "", null);
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
		
		if(eventSource instanceof ExpenseType) {
			ExpenseType et = (ExpenseType)eventSource;
			if(getEditExpHeader_id() != null && getEditExpHeader_id().longValue() > 0) {
				Object obj = gDAO.find(ExpenseHeader.class, getEditExpHeader_id());
				if(obj != null)
					et.setExpenseHeader((ExpenseHeader)obj);
			}
			eventSource = et;
		}
		
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
		if(getPartner() != null) {
			if(getExp().getAmount() > 0 &&
					getExp().getExpense_dt() != null && getExp().getType() != null) {
				String naration = "Create expense ";
				GeneralDAO gDAO = new GeneralDAO();
				if(getExp().getExpense_dt().after(new Date())) {
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Future date selected. Not allowed!");
				} else {
					ExpenseType et = getExp().getType();
					if(!et.isSystemObj())
					{
						naration += " Type: " + et.getName();
						
						getExp().setCreatedBy(dashBean.getUser());
						getExp().setCrt_dt(new Date());
						
						naration += " Amount: " + getExp().getAmount();
						
						gDAO.startTransaction();
						boolean ret = gDAO.save(getExp());
						if(ret) {
							if(getExp().getExpR() != null) {
								getExp().getExpR().setApprovalStatus("APPROVED and PROCESSED");
								gDAO.update(getExp().getExpR());
							}
							
							gDAO.commit();
							
							naration += ", Status: Success";
							
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
							
							naration += ", Status: Failed: " + gDAO.getMessage();
						}
						gDAO.destroy();
						
						dashBean.saveAudit(naration, "", null);
					}
					else
					{
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Can't manually insert expense data for a system expense type.");
					}
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
				String naration = "Setup budget";
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
							
							naration += ", Type:" + b.getType().getName() + " Amount: " + b.getAmount() + " Start: " + b.getStart_dt() + " End: " + b.getEnd_dt();
							
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
						
						naration += ", Status: Success";
						
						setBudget(null);
						setSetupbudgets(null);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during setup: " + gDAO.getMessage());
						
						naration += ", Status: Failed: " + gDAO.getMessage();
					}
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No entry could be processed. Please input valid amounts for the expense types and ensure the budgets do not already exist.");
					naration += ", Status: Failed: No entry could be processed. Please input valid amounts for the expense types and ensure the budgets do not already exist.";
				}
				gDAO.destroy();
				
				dashBean.saveAudit(naration, "", null);
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
	
	@SuppressWarnings("unchecked")
	public void saveExpType()
	{
		if(getExpType().getName() != null && getPartner() != null && getExpHeader_id() != null)
		{
			String naration = "Create expense type: " + getExpType().getName();
			getExpType().setCreatedBy(dashBean.getUser());
			getExpType().setCrt_dt(new Date());
			getExpType().setPartner(getPartner());
			getExpType().setSystemObj(false);
			
			GeneralDAO gDAO = new GeneralDAO();
			Object expHeader_obj = gDAO.find(ExpenseHeader.class, getExpHeader_id());
			if(expHeader_obj != null)
				getExpType().setExpenseHeader((ExpenseHeader)expHeader_obj);
			
			boolean exists = false;
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getPartner());
			params.put("name", getExpType().getName());
			Object exObj = gDAO.search("ExpenseType", params);
			if(exObj != null) {
				Vector<ExpenseType> exList = (Vector<ExpenseType>)exObj;
				if(exList.size() > 0)
					exists = true;
			}
			if(!exists) {
				gDAO.startTransaction();
				boolean ret = gDAO.save(getExpType());
				if(ret) {
					gDAO.commit();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Expense type created successfully.");
					
					naration += ", Status: Success";
					
					setExpType(null);
					setExpTypes(null);
				} else {
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create expense type: " + gDAO.getMessage());
					
					naration += ", Status: Failed: " + gDAO.getMessage();
				}
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Expense type with same name already exists!");
				naration += ", Status: Failed: Expense type with same name already exists!";
			}
			gDAO.destroy();
			
			dashBean.saveAudit(naration, "", null);
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "All fields with the '*' sign are required!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void saveExpHeader()
	{
		if(getExpHeader().getName() != null && getPartner() != null)
		{
			String naration = "Create expense header: " + getExpHeader().getName();
			getExpHeader().setCreatedBy(dashBean.getUser());
			getExpHeader().setCrt_dt(new Date());
			getExpHeader().setPartner(getPartner());
			getExpHeader().setSystemObj(false);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			gDAO.startTransaction();
			boolean ret = gDAO.save(getExpHeader());
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Expense header created successfully.");
				
				naration += ", Status: Success";
				
				setExpHeader(null);
				setExpHeaders(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create expense header: " + gDAO.getMessage());
				
				naration += ", Status: Failed: " + gDAO.getMessage();
			}
			gDAO.destroy();
			dashBean.saveAudit(naration, "", null);
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

	public ExpenseHeader getExpHeader() {
		if(expHeader == null)
			expHeader = new ExpenseHeader();
		return expHeader;
	}

	public void setExpHeader(ExpenseHeader expHeader) {
		this.expHeader = expHeader;
	}

	@SuppressWarnings("unchecked")
	public Vector<ExpenseHeader> getExpHeaders() {
		boolean research = true;
		if(expHeaders == null || expHeaders.size() == 0)
			research = true;
		else if(expHeaders.size() > 0)
		{
			if(getPartner() != null)
			{
				if(expHeaders.get(0).getPartner() != null && expHeaders.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			expHeaders = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				params.put("systemObj", false);
				
				Object drvs = gDAO.search("ExpenseHeader", params);
				if(drvs != null)
				{
					expHeaders = (Vector<ExpenseHeader>)drvs;
				}
				
				params = new Hashtable<String, Object>();
				params.put("systemObj", true);
				drvs = gDAO.search("ExpenseHeader", params);
				if(drvs != null)
				{
					if(expHeaders == null)
						expHeaders = new Vector<ExpenseHeader>();
					Vector<ExpenseHeader> drvsList = (Vector<ExpenseHeader>)drvs;
					for(ExpenseHeader e : drvsList)
					{
						expHeaders.add(e);
					}
				}
				gDAO.destroy();
			}
		}
		return expHeaders;
	}

	public void setExpHeaders(Vector<ExpenseHeader> expHeaders) {
		this.expHeaders = expHeaders;
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
		setExpHeaders(null);
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
				if(expTypes.get(0).getPartner() != null && expTypes.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
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
				gDAO.destroy();
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
				gDAO.destroy();
			}
		}
		return budgets;
	}

	public void setBudgets(Vector<Budget> budgets) {
		this.budgets = budgets;
	}

	public Long getFleet_id() {
		return fleet_id;
	}

	public void setFleet_id(Long fleet_id) {
		this.fleet_id = fleet_id;
	}

	public Long getVehicle_id() {
		return vehicle_id;
	}

	public void setVehicle_id(Long vehicle_id) {
		this.vehicle_id = vehicle_id;
	}

	public Long getDivision_id() {
		return division_id;
	}

	public void setDivision_id(Long division_id) {
		this.division_id = division_id;
	}

	public Long getDivision_id2() {
		return division_id2;
	}

	public void setDivision_id2(Long division_id2) {
		this.division_id2 = division_id2;
	}

	public Long getDepartment_id() {
		return department_id;
	}

	public void setDepartment_id(Long department_id) {
		this.department_id = department_id;
	}

	public Long getDepartment_id2() {
		return department_id2;
	}

	public void setDepartment_id2(Long department_id2) {
		this.department_id2 = department_id2;
	}

	public Long getUnit_id() {
		return unit_id;
	}

	public void setUnit_id(Long unit_id) {
		this.unit_id = unit_id;
	}

	public Long getUnit_id2() {
		return unit_id2;
	}

	public void setUnit_id2(Long unit_id2) {
		this.unit_id2 = unit_id2;
	}

	public Long getStaff_id() {
		return staff_id;
	}

	public void setStaff_id(Long staff_id) {
		this.staff_id = staff_id;
	}

	public Long getExpHeader_id() {
		return expHeader_id;
	}

	public void setExpHeader_id(Long expHeader_id) {
		this.expHeader_id = expHeader_id;
	}

	public Long getEditExpHeader_id() {
		return editExpHeader_id;
	}

	public void setEditExpHeader_id(Long editExpHeader_id) {
		this.editExpHeader_id = editExpHeader_id;
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
					gDAO.destroy();
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

	public Long getExpRequest_id() {
		return expRequest_id;
	}

	public void setExpRequest_id(Long expRequest_id) {
		this.expRequest_id = expRequest_id;
		if(expRequest_id != null) {
			for(ExpenseRequest e : approvedRequests) {
				if(e.getId().longValue() == expRequest_id.longValue()) {
					getExp().setExpR(e);
					getExp().setType(e.getType());
					getExp().setAmount(e.getAmount());
					getExp().setMisDepartment(e.getMisDepartment());
					getExp().setMisUnit(e.getMisUnit());
					getExp().setPartner(e.getPartner());
					getExp().setPersonel(e.getPersonel());
					getExp().setVehicle(e.getVehicle());
					
					if(e.getType() != null)
						setExpType_id(e.getType().getId());
					if(getExp().getExpense_dt() == null)
						getExp().setExpense_dt(e.getExpense_dt());
					if(getExp().getRemarks() == null)
						getExp().setRemarks(e.getRemarks());
					
					break;
				}
			}
		}
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
			gDAO.destroy();
		}
		return mySubExpRequests;
	}

	public void setMySubExpRequests(Vector<ExpenseRequest> mySubExpRequests) {
		this.mySubExpRequests = mySubExpRequests;
	}

	public void resetMyAllSubExpRequests() {
		setMyAllSubExpRequests(null);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<ExpenseRequest> getMyAllSubExpRequests() {
		if(myAllSubExpRequests == null) {
			if(getStart_dt() != null && getEnd_dt() != null) {
				GeneralDAO gDAO = new GeneralDAO();
				Query q = gDAO.createQuery("Select e from ExpenseRequest e where e.createdBy=:createdBy and e.request_dt between :st and :ed order by e.request_dt desc");
				q.setParameter("createdBy", dashBean.getUser());
				q.setParameter("st", getStart_dt());
				q.setParameter("ed", getEnd_dt());
				Object retObj = gDAO.search(q, 0);
				if(retObj != null)
				{
					myAllSubExpRequests = (Vector<ExpenseRequest>)retObj;
				}
				gDAO.destroy();
			}
		}
		return myAllSubExpRequests;
	}

	public void setMyAllSubExpRequests(Vector<ExpenseRequest> myAllSubExpRequests) {
		this.myAllSubExpRequests = myAllSubExpRequests;
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
			gDAO.destroy();
		}
		return pendingExpRequests;
	}

	public void setPendingExpRequests(Vector<ExpenseRequest> pendingExpRequests) {
		this.pendingExpRequests = pendingExpRequests;
	}

	@SuppressWarnings("unchecked")
	public Vector<ExpenseRequest> getMyAttendedExpRequests() {
		myAttendedExpRequests = null;
		GeneralDAO gDAO = new GeneralDAO();
		Query q = gDAO.createQuery("Select e from ExpenseRequest e where e.approvalUser=:approvalUser and not e.approvalStatus = 'PENDING' order by e.request_dt desc");
		q.setParameter("approvalUser", dashBean.getUser());
		Object retObj = gDAO.search(q, 0);
		if(retObj != null)
		{
			myAttendedExpRequests = (Vector<ExpenseRequest>)retObj;
		}
		gDAO.destroy();
		return myAttendedExpRequests;
	}

	public void setMyAttendedExpRequests(
			Vector<ExpenseRequest> myAttendedExpRequests) {
		this.myAttendedExpRequests = myAttendedExpRequests;
	}

	@SuppressWarnings("unchecked")
	public Vector<ExpenseRequest> getApprovedRequests() {
		GeneralDAO gDAO = new GeneralDAO();
		Query q = gDAO.createQuery("Select e from ExpenseRequest e where e.createdBy=:createdBy and e.approvalStatus = 'APPROVED' order by e.request_dt desc");
		q.setParameter("createdBy", dashBean.getUser());
		Object retObj = gDAO.search(q, 0);
		if(retObj != null) {
			approvedRequests = (Vector<ExpenseRequest>)retObj;
		}
		gDAO.destroy();
		return approvedRequests;
	}

	public void setApprovedRequests(Vector<ExpenseRequest> approvedRequests) {
		this.approvedRequests = approvedRequests;
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

	@SuppressWarnings("unchecked")
	public Vector<PartnerPersonel> getStaffs() {
		if(staffs == null)
		{
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				
				Object vhsObj = gDAO.search("PartnerPersonel", params);
				if(vhsObj != null)
					staffs = (Vector<PartnerPersonel>)vhsObj;
				
				gDAO.destroy();
			}
		}
		return staffs;
	}

	public void setStaffs(Vector<PartnerPersonel> staffs) {
		this.staffs = staffs;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vehicle> getVehicles() {
		if(vehicles == null)
		{
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				
				Object vhsObj = gDAO.search("Vehicle", params);
				if(vhsObj != null)
					vehicles = (Vector<Vehicle>)vhsObj;
				
				gDAO.destroy();
			}
		}
		return vehicles;
	}

	public void setVehicles(Vector<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
