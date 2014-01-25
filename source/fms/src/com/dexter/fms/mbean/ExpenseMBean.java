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
import javax.persistence.Query;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.app.Budget;
import com.dexter.fms.model.app.Expense;
import com.dexter.fms.model.app.ExpenseType;

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
	
	private Long expType_id;
	private Expense exp;
	private Vector<Expense> exps;
	private Date start_dt, end_dt;
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public ExpenseMBean()
	{}
	
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
						getExp().setType((ExpenseType)expType);
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
						Query q = gDAO.createQuery("Select e from Budget e where e.type=:type and e.partner=:partner and e.end_dt >= :end_dt");
						q.setParameter("type", b.getType());
						q.setParameter("partner", b.getPartner());
						q.setParameter("end_dt", getBudget().getEnd_dt());
						
						Object qret = gDAO.search(q, 0);
						if(qret != null)
						{
							Vector<Budget> qretList = (Vector<Budget>)qret;
							if(qretList.size() > 0)
							{
								exists = true;
							}
						}
						
						if(!exists)
						{
							count++;
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

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
