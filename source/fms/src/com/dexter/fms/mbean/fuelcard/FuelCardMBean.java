package com.dexter.fms.mbean.fuelcard;

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

import org.primefaces.event.RowEditEvent;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.dao.fuelcard.BankRecordDAO;
import com.dexter.fms.dao.fuelcard.ReportDAO;
import com.dexter.fms.mbean.DashboardMBean;
import com.dexter.fms.model.Audit;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.app.Fleet;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleParameters;
import com.dexter.fms.model.fuelcard.BankRecord;
import com.dexter.fms.model.fuelcard.Card;
import com.dexter.fms.model.fuelcard.CardBalanceNotification;
import com.dexter.fms.model.ref.Region;

@ManagedBean(name = "fuelcardBean")
@SessionScoped
public class FuelCardMBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-FuelCardMBean");
	
	private Vector<CardBalanceNotification> settings;
	private CardBalanceNotification cardBal, selCardBal;
	private long region_id;
	
	private Date tranDate, tranDate2;
	private int tranType;
	
	private long vehicle_id, department_id;
	private int month;
	private String region_nm, department_nm;
	private Vehicle vehicle;
	private Vector<Vehicle> vehicles;
	
	private Vector<BankRecord> bankTranList;
	private ArrayList<String[]> records, records2, records22, recordsHC, recordsHP, recordsLD, recordsBE, recordsEX;
	private Boolean[] recordsFields, records2Fields, recordsEXFields;
	private boolean showTracker;
	
	private Vector<Card> cards;
	private Card card;
	
	private Partner partner;
	private Long partner_id;
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public FuelCardMBean() {}
	
	public Partner getPartner() {
		if(!dashBean.getUser().getPartner().isSattrak()) {
			partner = dashBean.getUser().getPartner();
		} else {
			if(getPartner_id() != null) {
				GeneralDAO gDAO = new GeneralDAO();
				partner = (Partner)gDAO.find(Partner.class, getPartner_id());
				gDAO.destroy();
			}
		}
		return partner;
	}
	
	public void onEdit(RowEditEvent event) {
		Object eventSource = event.getObject();
		
		if(eventSource instanceof CardBalanceNotification) {
			setSelCardBal((CardBalanceNotification)eventSource);
			update();
		} else {
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			gDAO.update(eventSource);
			try {
				gDAO.commit();
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Update successful!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				if(eventSource instanceof Card) {
					setCards(null);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
				gDAO.rollback();
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Error: ", "Update failed: " + ex.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
	}
	
	public void onCancel(RowEditEvent event) {
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Update canceled!");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void createCard() {
		if(getCard().getCardPan() != null && getCard().getCardname() != null) {
			getCard().setCrt_dt(new Date());
			getCard().setPartner(dashBean.getUser().getPartner());
			getCard().setStatus("ACTIVE");
			
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			gDAO.save(getCard());
			try {
				gDAO.commit();
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Card created successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				setCard(null);
				setCards(null);
			} catch(Exception ex) {
				gDAO.rollback();
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Error: ", ex.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
				ex.printStackTrace();
			}
			gDAO.destroy();
		}
	}
	
	public void update()
	{
		Audit audit = new Audit();
		
		if(getSelCardBal() != null && getSelCardBal().getThresholdAlertEmail() != null && getSelCardBal().getMinbalance() != null && 
				getSelCardBal().getNew_region_id() > 0L && getSelCardBal().getId() != null) {
			audit.setAction_dt(new java.util.Date());
			audit.setNarration("Updating notification settings...");
			//audit.setEntity("CardBalanceNotification");
			audit.setUser(getActiveUser());
			
			FacesContext curContext = FacesContext.getCurrentInstance();
			
			GeneralDAO gDAO = new GeneralDAO();
			Object regionObj = gDAO.find(Region.class, getSelCardBal().getNew_region_id());
			if(regionObj != null)
				getSelCardBal().setRegion((Region)regionObj);
			
			gDAO.startTransaction();
			gDAO.update(getSelCardBal());
			gDAO.save(audit);
			try {
				if(gDAO.commit()) {
					curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Notification settings updated successfully!"));
				} else {
					gDAO.rollback();
					curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Notification settings update failed!"));
				}
			} catch(Exception ex) {
				ex.printStackTrace();
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Notification settings update failed!"));
			}
			
			setSelCardBal(null);
			setSettings(null);
		}
	}
	
	public void save() {
		Audit audit = new Audit();
		
		if(getCardBal().getThresholdAlertEmail() != null && getCardBal().getMinbalance() != null && getRegion_id() > 0L) {
			audit.setAction_dt(new java.util.Date());
			audit.setNarration("Setting the notification settings...");
			//audit.setEntity("CardBalanceNotification");
			audit.setUser(getActiveUser());
			
			FacesContext curContext = FacesContext.getCurrentInstance();
			
			GeneralDAO gDAO = new GeneralDAO();
			Object regionObj = gDAO.find(Region.class, getRegion_id());
			if(regionObj != null)
				getCardBal().setRegion((Region)regionObj);
			
			gDAO.startTransaction();
			gDAO.save(getCardBal());
			gDAO.save(audit);
			try {
				if(gDAO.commit()) {
					curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Notification settings saved successfully!"));
				} else {
					gDAO.rollback();
					curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Notification settings save failed!"));
				}
			} catch(Exception ex) {
				ex.printStackTrace();
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Notification settings save failed!"));
			}
			
			setCardBal(null);
			setSettings(null);
		}
	}
	
	public void searchBankTrans()
	{
		Audit audit = new Audit();
		
		setRecords(null);
		if(getTranDate() != null && getTranDate2() != null)
		{
			audit.setAction_dt(new java.util.Date());
			audit.setNarration("Searching for transaction notifications...");
			//audit.setEntity("TRANSACTIONS");
			audit.setUser(getActiveUser());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			if(getRegion_id() > 0) {
				if(getVehicle_id() > 0) {
					Object obj = gDAO.find(Vehicle.class, getVehicle_id());
					if(obj != null) {
						setVehicle((Vehicle)obj);
						setBankTranList(new BankRecordDAO().search(getTranDate(), getTranDate2(), getVehicle()));
					}
				} else {
					Object obj = gDAO.find(Region.class, getRegion_id());
					setBankTranList(new BankRecordDAO().searchByRegion(getTranDate(), getTranDate2(), (Region)obj));
				}
			} else if(getVehicle_id() > 0) {
				Object obj = gDAO.find(Vehicle.class, getVehicle_id());
				if(obj != null) {
					setVehicle((Vehicle)obj);
					setBankTranList(new BankRecordDAO().search(getTranDate(), getTranDate2(), getVehicle()));
				}
			} else {
				setBankTranList(new BankRecordDAO().search(getTranDate(), getTranDate2(), null));
			}
			
			FacesContext curContext = FacesContext.getCurrentInstance();
			audit.setNarration(audit.getNarration() + ". " + getBankTranList().size() + " record(s) found.");
			if(getBankTranList().size() > 0) {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Search successful " + getBankTranList().size() + "!"));
				//curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, Utils.getBundleMessage("search.success", new Object[] {}), null));
			} else {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Search failed!"));
			}
			
			try {
				gDAO.startTransaction();
				gDAO.save(audit);
				gDAO.commit();
			} catch(Exception ex) {}
			gDAO.destroy();
		}
	}
	
	public void searchDailyTransactions()
	{
		Audit audit = new Audit();
		
		setRecords(null);
		if(getTranDate() != null && getTranDate2() != null) {
			audit.setAction_dt(new java.util.Date());
			audit.setNarration("Searching for daily log sheet transactions...");
			audit.setUser(getActiveUser());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			if(getRegion_id() > 0) {
				if(getVehicle_id() > 0) {
					Object obj = gDAO.find(Vehicle.class, getVehicle_id());
					if(obj != null) {
						setVehicle((Vehicle)obj);
						setRecords(new ReportDAO().searchDailyLogSheet(getTranDate(), getTranDate2(), getVehicle()));
					}
				} else {
					Object obj = gDAO.find(Region.class, getRegion_id());
					setRecords(new ReportDAO().searchDailyLogSheetRegion(getTranDate(), getTranDate2(), (Region)obj));
				}
			} else if(getVehicle_id() > 0) {
				Object obj = gDAO.find(Vehicle.class, getVehicle_id());
				if(obj != null) {
					setVehicle((Vehicle)obj);
					setRecords(new ReportDAO().searchDailyLogSheet(getTranDate(), getTranDate2(), getVehicle()));
				}
			} else {
				setRecords(new ReportDAO().searchDailyLogSheetRegion(getTranDate(), getTranDate2(), null));
			}
			
			FacesContext curContext = FacesContext.getCurrentInstance();
			audit.setNarration(audit.getNarration() + ". " + getRecords().size() + " record(s) found.");
			if(getRecords().size() > 0) {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Search successful " + getRecords().size() + "!"));
			} else {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Search failed!"));
			}
			
			try {
				gDAO.startTransaction();
				gDAO.save(audit);
				gDAO.commit();
			} catch(Exception ex) {}
			gDAO.destroy();
		}
	}
	
	public void searchFuelPurchaseTransactionsSummary()
	{
		Audit audit = new Audit();
		setRecords22(null);
		if(getTranDate() != null && getTranDate2() != null) {
			audit.setAction_dt(new java.util.Date());
			audit.setNarration("Searching for summary fuel purchase transactions...");
			audit.setUser(getActiveUser());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			if(getRegion_id() > 0) {
				if(getVehicle_id() > 0) {
					setRecords22(new ReportDAO().searchFuelPurchaseReportSummary(getTranDate(), getTranDate2(), getVehicle_id()));
				} else {
					Object obj = gDAO.find(Region.class, getRegion_id());
					setRecords22(new ReportDAO().searchFuelPurchaseReportSummary(getTranDate(), getTranDate2(), (Region)obj));
				}
			} else if(getVehicle_id() > 0) {
				setRecords22(new ReportDAO().searchFuelPurchaseReportSummary(getTranDate(), getTranDate2(), getVehicle_id()));
			} else {
				setRecords22(new ReportDAO().searchFuelPurchaseReportSummary(getTranDate(), getTranDate2(), null));
			}
			
			FacesContext curContext = FacesContext.getCurrentInstance();
			if(getRecords22().size() > 0) {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Search successful " + getRecords22().size() + "!"));
			} else {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Search failed!"));
			}
			audit.setNarration(audit.getNarration() + ". " + getRecords22().size() + " record(s) found.");
			
			try {
				gDAO.startTransaction();
				gDAO.save(audit);
				gDAO.commit();
			} catch(Exception ex) {}
			gDAO.destroy();
		}
	}
	
	public void searchFuelPurchaseTransactions()
	{
		Audit audit = new Audit();
		setRecords2(null);
		if(getTranDate() != null && getTranDate2() != null)
		{
			audit.setAction_dt(new java.util.Date());
			audit.setNarration("Searching for daily fuel purchase transactions...");
			audit.setUser(getActiveUser());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			if(getRegion_id() > 0) {
				if(getVehicle_id() > 0) {
					setRecords2(new ReportDAO().searchFuelPurchaseReport(getTranDate(), getTranDate2(), getVehicle_id()));
				} else {
					Object obj = gDAO.find(Region.class, getRegion_id());
					setRecords2(new ReportDAO().searchFuelPurchaseReport(getTranDate(), getTranDate2(), (Region)obj));
				}
			} else if(getVehicle_id() > 0) {
				setRecords2(new ReportDAO().searchFuelPurchaseReport(getTranDate(), getTranDate2(), getVehicle_id()));
			} else {
				setRecords2(new ReportDAO().searchFuelPurchaseReport(getTranDate(), getTranDate2(), null));
			}
			
			FacesContext curContext = FacesContext.getCurrentInstance();
			if(getRecords2().size() > 0) {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Search successful " + getRecords2().size() + "!"));
			} else {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Search failed!"));
			}
			audit.setNarration(audit.getNarration() + ". " + getRecords2().size() + " record(s) found.");
			
			try {
				gDAO.startTransaction();
				gDAO.save(audit);
				gDAO.commit();
			} catch(Exception ex) {}
			gDAO.destroy();
		}
	}
	
	public void searchExceptionTransactions()
	{
		Audit audit = new Audit();
		setRecordsEX(null);
		if(getTranDate() != null && getTranDate2() != null)
		{
			audit.setAction_dt(new java.util.Date());
			audit.setNarration("Searching for exception transactions...");
			audit.setUser(getActiveUser());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			FacesContext curContext = FacesContext.getCurrentInstance();
			
			if(getRegion_id() > 0) {
				if(getVehicle_id() > 0) {
					Object obj = gDAO.find(Vehicle.class, getVehicle_id());
					if(obj != null) {
						setVehicle((Vehicle)obj);
						setRecordsEX(new ReportDAO().exceptionTransactions(getTranDate(), getTranDate2(), getVehicle()));
					}
				} else {
					Object obj = gDAO.find(Region.class, getRegion_id());
					setRecordsEX(new ReportDAO().exceptionTransactionsRegion(getTranDate(), getTranDate2(), (Region)obj));
				}
			} else if(getVehicle_id() > 0) {
				Object obj = gDAO.find(Vehicle.class, getVehicle_id());
				if(obj != null) {
					setVehicle((Vehicle)obj);
					setRecordsEX(new ReportDAO().exceptionTransactions(getTranDate(), getTranDate2(), getVehicle()));
				}
			} else {
				setRecordsEX(new ReportDAO().exceptionTransactionsRegion(getTranDate(), getTranDate2(), null));
			}
			
			if(getRecordsEX().size() > 0) {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Search successful " + getRecordsEX().size() + "!"));
			} else {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Search failed!"));
			}
			
			audit.setNarration(audit.getNarration() + ". " + getRecordsEX().size() + " record(s) found.");
			
			try {
				gDAO.startTransaction();
				gDAO.save(audit);
				gDAO.commit();
			} catch(Exception ex) {}
			gDAO.destroy();
		}
	}
	
	public void searchHighestFuelConsumption()
	{
		Audit audit = new Audit();
		setRecordsHC(null);
		if(getTranDate() != null && getTranDate2() != null) {
			audit.setAction_dt(new java.util.Date());
			audit.setNarration("Searching for highest fuel consuming vehicles transactions...");
			audit.setUser(getActiveUser());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			FacesContext curContext = FacesContext.getCurrentInstance();
			
			setRecordsHC(new ReportDAO().highestFuelConsumption(getTranDate(), getTranDate2()));
			if(getRecordsHC().size() > 0) {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Search successful " + getRecordsHC().size() + "!"));
			} else {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Search failed!"));
			}
			
			audit.setNarration(audit.getNarration() + ". " + getRecordsHC().size() + " record(s) found.");
			
			try {
				gDAO.startTransaction();
				gDAO.save(audit);
				gDAO.commit();
			} catch(Exception ex) {}
			gDAO.destroy();
		}
	}
	
	public void searchHighestFuelPurchase()
	{
		Audit audit = new Audit();
		setRecordsHP(null);
		if(getTranDate() != null && getTranDate2() != null) {
			audit.setAction_dt(new java.util.Date());
			audit.setNarration("Searching for highest fuel purchase vehicles transactions...");
			audit.setUser(getActiveUser());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			FacesContext curContext = FacesContext.getCurrentInstance();
			
			setRecordsHP(new ReportDAO().highestFuelPurchase(getTranDate(), getTranDate2()));
			if(getRecordsHP().size() > 0) {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Search successful " + getRecordsHP().size() + "!"));
			} else {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Search failed!"));
			}
			
			audit.setNarration(audit.getNarration() + ". " + getRecordsHP().size() + " record(s) found.");
			
			try {
				gDAO.startTransaction();
				gDAO.save(audit);
				gDAO.commit();
			} catch(Exception ex) {}
			gDAO.destroy();
		}
	}
	
	public void searchLongestDistance()
	{
		Audit audit = new Audit();
		setRecordsLD(null);
		if(getTranDate() != null && getTranDate2() != null)
		{
			audit.setAction_dt(new java.util.Date());
			audit.setNarration("Searching for longest distance vehicles transactions...");
			audit.setUser(getActiveUser());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			FacesContext curContext = FacesContext.getCurrentInstance();
			
			setRecordsLD(new ReportDAO().longestDistance(getTranDate(), getTranDate2()));
			if(getRecordsLD().size() > 0) {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Search successful " + getRecordsLD().size() + "!"));
			} else {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Search failed!"));
			}
			
			audit.setNarration(audit.getNarration() + ". " + getRecordsLD().size() + " record(s) found.");
			
			try {
				gDAO.startTransaction();
				gDAO.save(audit);
				gDAO.commit();
			} catch(Exception ex) {}
			gDAO.destroy();
		}
	}
	
	public void searchBestEfficiency()
	{
		Audit audit = new Audit();
		setRecordsBE(null);
		if(getTranDate() != null && getTranDate2() != null) {
			audit.setAction_dt(new java.util.Date());
			audit.setNarration("Searching for best fuel efficient vehicles transactions...");
			audit.setUser(getActiveUser());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			FacesContext curContext = FacesContext.getCurrentInstance();
			
			setRecordsBE(new ReportDAO().bestEfficiency(getTranDate(), getTranDate2()));
			if(getRecordsBE().size() > 0) {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Search successful " + getRecordsBE().size() + "!"));
			} else {
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Search failed!"));
			}
			
			audit.setNarration(audit.getNarration() + ". " + getRecordsBE().size() + " record(s) found.");
			
			try {
				gDAO.startTransaction();
				gDAO.save(audit);
				gDAO.commit();
			} catch(Exception ex) {}
			gDAO.destroy();
		}
	}
	
	public void searchTransactions()
	{
		FacesContext curContext = FacesContext.getCurrentInstance();
		
		setRecords(new ArrayList<String[]>());
		setRecords2(new ArrayList<String[]>());
		
		if(getTranType() == 1) {
			setRecords(new ReportDAO().searchReport(getTranDate()));
			
			if(getRecords().size() > 0)
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Search successful " + getRecords().size() + "!"));
			else
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Search failed!"));
		} else if(getTranType() == 2) {
			setRecords2(new ReportDAO().searchReport2(getTranDate()));
			
			if(getRecords2().size() > 0)
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Search successful " + getRecords2().size() + "!"));
			else
				curContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Search failed!"));
		}
	}

	@SuppressWarnings("unchecked")
	public Vector<CardBalanceNotification> getSettings() {
		if(settings == null || settings.size() == 0) {
			GeneralDAO gDAO = new GeneralDAO();
			Object objs = gDAO.findAll("CardBalanceNotification");
			if(objs != null)
				settings = (Vector<CardBalanceNotification>)objs;
			gDAO.destroy();
		}
		return settings;
	}

	public void setSettings(Vector<CardBalanceNotification> settings) {
		this.settings = settings;
	}

	public CardBalanceNotification getCardBal() {
		if(cardBal == null)
			cardBal = new CardBalanceNotification();
		return cardBal;
	}

	public void setCardBal(CardBalanceNotification cardBal) {
		this.cardBal = cardBal;
	}
	
	public CardBalanceNotification getSelCardBal() {
		return selCardBal;
	}

	public void setSelCardBal(CardBalanceNotification selCardBal) {
		this.selCardBal = selCardBal;
	}

	public long getRegion_id() {
		return region_id;
	}

	public void setRegion_id(long region_id) {
		this.region_id = region_id;
	}
	
	public Date getTranDate() {
		return tranDate;
	}

	public void setTranDate(Date tranDate) {
		this.tranDate = tranDate;
	}

	public int getTranType() {
		return tranType;
	}

	public void setTranType(int tranType) {
		this.tranType = tranType;
	}

	public Date getTranDate2() {
		return tranDate2;
	}

	public void setTranDate2(Date tranDate2) {
		this.tranDate2 = tranDate2;
	}

	public long getVehicle_id() {
		return vehicle_id;
	}

	public void setVehicle_id(long vehicle_id) {
		this.vehicle_id = vehicle_id;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public long getDepartment_id() {
		return department_id;
	}

	public void setDepartment_id(long department_id) {
		this.department_id = department_id;
	}

	public String getRegion_nm() {
		return region_nm;
	}

	public void setRegion_nm(String region_nm) {
		this.region_nm = region_nm;
	}

	public String getDepartment_nm() {
		return department_nm;
	}

	public void setDepartment_nm(String department_nm) {
		this.department_nm = department_nm;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vehicle> getVehicles() {
		if(vehicles == null || vehicles.size() == 0)
		{
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				String qry = "Select e from Vehicle e where e.partner = :partner order by e.registrationNo";
				Query q = gDAO.createQuery(qry);
				q.setParameter("partner", getPartner());
				Object vhsObj = gDAO.search(q, 0);
				if(vhsObj != null) {
					Vector<Vehicle> vehiclesList = (Vector<Vehicle>) vhsObj;
					if(getRegion_id() > 0) {
						vehicles = new Vector<Vehicle>();
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						for(Vehicle v : vehiclesList) {
							params = new Hashtable<String, Object>();
							params.put("vehicle", v);
							Object vParams = gDAO.search("VehicleParameters", params);
							if(vParams != null) {
								Vector<VehicleParameters> vParamsList = (Vector<VehicleParameters>)vParams;
								v.setParams(vParamsList);
							}
							if(v.getParams() != null && v.getParams().get(v.getParams().size()-1).getRegion().getId().longValue() == getRegion_id()) {
								vehicles.add(v);
							}
						}
					} else {
						vehicles = vehiclesList;
					}
				}
				gDAO.destroy();
			}
		}
		return vehicles;
	}

	public void setVehicles(Vector<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public Vector<BankRecord> getBankTranList() {
		if(bankTranList == null)
			bankTranList = new Vector<BankRecord>();
		return bankTranList;
	}

	public void setBankTranList(Vector<BankRecord> bankTranList) {
		this.bankTranList = bankTranList;
	}

	public ArrayList<String[]> getRecords() {
		if(records == null)
			records = new ArrayList<String[]>();
		return records;
	}

	public void setRecords(ArrayList<String[]> records) {
		this.records = records;
	}

	public ArrayList<String[]> getRecords2() {
		if(records2 == null)
			records2 = new ArrayList<String[]>();
		return records2;
	}

	public void setRecords2(ArrayList<String[]> records2) {
		this.records2 = records2;
	}

	public ArrayList<String[]> getRecords22() {
		if(records22 == null)
			records22 = new ArrayList<String[]>();
		return records22;
	}

	public void setRecords22(ArrayList<String[]> records22) {
		this.records22 = records22;
	}

	public ArrayList<String[]> getRecordsHC() {
		if(recordsHC == null)
			recordsHC = new ArrayList<String[]>();
		return recordsHC;
	}

	public void setRecordsHC(ArrayList<String[]> recordsHC) {
		this.recordsHC = recordsHC;
	}

	public ArrayList<String[]> getRecordsHP() {
		if(recordsHP == null)
			recordsHP = new ArrayList<String[]>();
		return recordsHP;
	}

	public void setRecordsHP(ArrayList<String[]> recordsHP) {
		this.recordsHP = recordsHP;
	}

	public ArrayList<String[]> getRecordsLD() {
		if(recordsLD == null)
			recordsLD = new ArrayList<String[]>();
		return recordsLD;
	}

	public void setRecordsLD(ArrayList<String[]> recordsLD) {
		this.recordsLD = recordsLD;
	}

	public ArrayList<String[]> getRecordsBE() {
		if(recordsBE == null)
			recordsBE = new ArrayList<String[]>();
		return recordsBE;
	}

	public void setRecordsBE(ArrayList<String[]> recordsBE) {
		this.recordsBE = recordsBE;
	}

	public ArrayList<String[]> getRecordsEX() {
		if(recordsEX == null)
			recordsEX = new ArrayList<String[]>();
		return recordsEX;
	}

	public void setRecordsEX(ArrayList<String[]> recordsEX) {
		this.recordsEX = recordsEX;
	}

	public Boolean[] getRecordsFields() {
		if(recordsFields == null)
		{
			recordsFields = new Boolean[11];
			for(int i=0; i<recordsFields.length; i++)
				recordsFields[i] = true;
		}
		return recordsFields;
	}

	public void setRecordsFields(Boolean[] recordsFields) {
		this.recordsFields = recordsFields;
	}

	public Boolean[] getRecords2Fields() {
		if(records2Fields == null)
		{
			records2Fields = new Boolean[14];
			for(int i=0; i<records2Fields.length; i++)
				records2Fields[i] = true;
		}
		return records2Fields;
	}

	public void setRecords2Fields(Boolean[] records2Fields) {
		this.records2Fields = records2Fields;
	}

	public Boolean[] getRecordsEXFields() {
		if(recordsEXFields == null)
		{
			recordsEXFields = new Boolean[14];
			for(int i=0; i<recordsEXFields.length; i++)
				recordsEXFields[i] = true;
		}
		return recordsEXFields;
	}

	public void setRecordsEXFields(Boolean[] recordsEXFields) {
		this.recordsEXFields = recordsEXFields;
	}

	public boolean isShowTracker() {
		return showTracker;
	}

	public void setShowTracker(boolean showTracker) {
		this.showTracker = showTracker;
	}

	@SuppressWarnings("unchecked")
	public Vector<Card> getCards() {
		if(cards == null || cards.size() == 0) {
			GeneralDAO gDAO = new GeneralDAO();
			Query q = gDAO.createQuery("Select e from Card e where e.partner=:partner");
			q.setParameter("partner", dashBean.getUser().getPartner());
			Object obj = gDAO.search(q, 0);
			if(obj != null)
				cards = (Vector<Card>)obj;
			gDAO.destroy();
		}
		return cards;
	}

	public void setCards(Vector<Card> cards) {
		this.cards = cards;
	}

	public Card getCard() {
		if(card == null)
			card = new Card();
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	private PartnerUser getActiveUser()
	{
		FacesContext curContext = FacesContext.getCurrentInstance();
		return ((DashboardMBean)curContext.getExternalContext().getSessionMap().get("dashboardBean")).getUser();
	}

	public Long getPartner_id() {
		return partner_id;
	}

	public void setPartner_id(Long partner_id) {
		this.partner_id = partner_id;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
