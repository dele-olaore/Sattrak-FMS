package com.dexter.fms.mbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
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

import org.primefaces.model.DualListModel;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.app.CorporateTrip;
import com.dexter.fms.model.app.CorporateTripPassenger;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleDriver;

@ManagedBean(name = "ctripBean")
@SessionScoped
public class CorporateTripMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-VendorMBean");
	
	private FacesMessage msg = null;
	
	private Long partner_id;
	private Partner partner;
	
	private DualListModel<PartnerPersonel> personelsList;
	private Vector<PartnerPersonel> personels, selectedPersonels;
	private CorporateTrip trip;
	private Vector<CorporateTrip> pendingTrips, ongoingTrips, completedTrips;
	
	private Long vehicle_id;
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public CorporateTripMBean()
	{}
	
	public void completeTrip()
	{
		if(getTrip().getId() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			getTrip().setCompletedDateTime(new Date());
			getTrip().setTripStatus("COMPLETED");
			
			gDAO.startTransaction();
			boolean ret = gDAO.update(getTrip());
			if(ret)
			{
				gDAO.commit();
				
				setTrip(null);
				setOngoingTrips(null);
				setCompletedTrips(null);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Trip completed successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Error occurred during save: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid trip selected!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void attendToTrip()
	{
		if(getTrip().getId() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			getTrip().setAttendedDate(new Date());
			getTrip().setApproveUser(dashBean.getUser());
			boolean valid = true;
			if(getTrip().getApprovalStatus().equals("APPROVED"))
			{
				if(getVehicle_id() != null)
				{
					Object vObj = gDAO.find(Vehicle.class, getVehicle_id());
					if(vObj != null)
					{
						Vehicle v = (Vehicle)vObj;
						if(v.isActive())
						{
							Hashtable<String, Object> params = new Hashtable<String, Object>();
							params.put("vehicle", v);
							params.put("active", true);
							Object vdsObj = gDAO.search("VehicleDriver", params);
							if(vdsObj != null)
							{
								Vector<VehicleDriver> vdsList = (Vector<VehicleDriver>)vdsObj;
								if(vdsList.size() > 0)
								{
									PartnerDriver pd = vdsList.get(0).getDriver();
									getTrip().setDriver(pd);
									valid = true;
								}
								else
								{
									// 
									valid = false;
									msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "The selected vehicle is not assigned to a driver. Only assigned vehicles can be used for trips!");
									FacesContext.getCurrentInstance().addMessage(null, msg);
								}
							}
							else
							{
								// 
								valid = false;
								msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "The selected vehicle is not assigned to a driver. Only assigned vehicles can be used for trips!");
								FacesContext.getCurrentInstance().addMessage(null, msg);
							}
						}
						else
						{
							valid = false;
							msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "In-active vehicle selected!");
							FacesContext.getCurrentInstance().addMessage(null, msg);
						}
					}
					else
					{
						// invalid vehicle selected
						valid = false;
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Unknown vehicle selected!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				else
				{
					// must select a vehicle after approval for the trip
					valid = false;
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Vehicle must be selected for a trip approval!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			
			if(valid)
			{
				gDAO.startTransaction();
				boolean ret = gDAO.update(getTrip());
				if(ret)
				{
					gDAO.commit();
					
					setTrip(null);
					setPendingTrips(null);
					setOngoingTrips(null);
					
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Trip attended to successfully!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Error occurred during save: " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			gDAO.destroy();
		}
		else
		{
			// invalid trip selected
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid trip selected!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void saveTripRequest()
	{
		if(getPartner() != null && getTrip().getDepartureDateTime() != null && getTrip().getDepartureLocation() != null &&
				getTrip().getEstimatedArrivalDateTime() != null && getTrip().getPurpose() != null &&
				getTrip().getArrivalLocation() != null)
		{
			if(getTrip().getDepartureDateTime().after(new Date()))
			{
				if(getTrip().getDepartureDateTime().before(getTrip().getEstimatedArrivalDateTime()))
				{
					getTrip().setCreatedBy(dashBean.getUser());
					getTrip().setCrt_dt(new Date());
					getTrip().setStaff(dashBean.getUser().getPersonel());
					getTrip().setApprovalStatus("PENDING");
					
					GeneralDAO gDAO = new GeneralDAO();
					gDAO.startTransaction();
					if(gDAO.save(getTrip()))
					{
						System.out.println("Source Personel Size: " + getPersonelsList().getSource().size());
						if(getPersonelsList().getTarget() != null && getPersonelsList().getTarget().size() > 0)
						{
							for(PartnerPersonel pp : getPersonelsList().getTarget())
							{
								CorporateTripPassenger ctp = new CorporateTripPassenger();
								ctp.setPassenger(pp);
								ctp.setTrip(getTrip());
								gDAO.save(ctp);
							}
						}
						gDAO.commit();
						
						setTrip(null);
						setPendingTrips(null);
						setPersonels(null);
						setPersonelsList(null);
						setSelectedPersonels(null);
						
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Trip request submitted successfully!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Error occurred during save: " + gDAO.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					gDAO.destroy();
				}
				else
				{
					// arrival date can not be before departure date
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Departure can not be before estimated arrival date!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				// can't request for trip in the past
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Can't set a past date as departure date!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public Partner getPartner() {
		partner = dashBean.getUser().getPartner();
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}
	
	public Long getPartner_id() {
		return partner_id;
	}

	public void setPartner_id(Long partner_id) {
		this.partner_id = partner_id;
	}

	public DualListModel<PartnerPersonel> getPersonelsList() {
		if(personelsList == null)
		{
			personelsList = new DualListModel<PartnerPersonel>(getPersonels(), new ArrayList<PartnerPersonel>());
		}
		return personelsList;
	}

	public void setPersonelsList(DualListModel<PartnerPersonel> personelsList) {
		this.personelsList = personelsList;
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
				Object psObj = gDAO.search("PartnerPersonel", params);
				if(psObj != null)
				{
					personels = (Vector<PartnerPersonel>)psObj;
				}
			}
		}
		return personels;
	}

	public void setPersonels(Vector<PartnerPersonel> personels) {
		this.personels = personels;
	}

	public Vector<PartnerPersonel> getSelectedPersonels() {
		if(selectedPersonels == null)
			selectedPersonels = new Vector<PartnerPersonel>();
		return selectedPersonels;
	}

	public void setSelectedPersonels(Vector<PartnerPersonel> selectedPersonels) {
		this.selectedPersonels = selectedPersonels;
	}

	public CorporateTrip getTrip() {
		if(trip == null)
			trip = new CorporateTrip();
		return trip;
	}

	public void setTrip(CorporateTrip trip) {
		this.trip = trip;
	}

	@SuppressWarnings("unchecked")
	public Vector<CorporateTrip> getPendingTrips() {
		boolean research = true;
		if(pendingTrips == null || pendingTrips.size() == 0)
			research = true;
		else if(pendingTrips.size() > 0)
		{
			if(getPartner() != null)
			{
				if(pendingTrips.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			pendingTrips = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and e.approvalStatus=:approvalStatus and e.departureDateTime > :nowDateTime");
				q.setParameter("partner", getPartner());
				q.setParameter("approvalStatus", "PENDING");
				q.setParameter("nowDateTime", new Date());
				
				Object tripsObj = gDAO.search(q, 0);
				if(tripsObj != null)
				{
					pendingTrips = (Vector<CorporateTrip>)tripsObj;
				}
			}
		}
		return pendingTrips;
	}

	public void setPendingTrips(Vector<CorporateTrip> pendingTrips) {
		this.pendingTrips = pendingTrips;
	}

	@SuppressWarnings("unchecked")
	public Vector<CorporateTrip> getOngoingTrips() {
		boolean research = true;
		if(ongoingTrips == null || ongoingTrips.size() == 0)
			research = true;
		else if(ongoingTrips.size() > 0)
		{
			if(getPartner() != null)
			{
				if(ongoingTrips.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			ongoingTrips = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DAY_OF_MONTH, -7);
				
				Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and e.approvalStatus=:approvalStatus and (e.tripStatus=:tripStatus or e.tripStatus=:tripStatus2) and e.departureDateTime > :maxwaitDateTime");
				q.setParameter("partner", getPartner());
				q.setParameter("approvalStatus", "APPROVED");
				q.setParameter("tripStatus", "ON_TRIP");
				q.setParameter("tripStatus2", "SHOULD_BE_COMPLETED");
				q.setParameter("maxwaitDateTime", c.getTime());
				
				Object tripsObj = gDAO.search(q, 0);
				if(tripsObj != null)
				{
					ongoingTrips = (Vector<CorporateTrip>)tripsObj;
				}
			}
		}
		return ongoingTrips;
	}

	public void setOngoingTrips(Vector<CorporateTrip> ongoingTrips) {
		this.ongoingTrips = ongoingTrips;
	}

	@SuppressWarnings("unchecked")
	public Vector<CorporateTrip> getCompletedTrips() {
		boolean research = true;
		if(completedTrips == null || completedTrips.size() == 0)
			research = true;
		else if(completedTrips.size() > 0)
		{
			if(getPartner() != null)
			{
				if(completedTrips.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			completedTrips = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DAY_OF_MONTH, -7);
				
				Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and e.approvalStatus=:approvalStatus and (e.tripStatus=:tripStatus or (e.tripStatus=:tripStatus2 and e.departureDateTime < :maxwaitDateTime))");
				q.setParameter("partner", getPartner());
				q.setParameter("approvalStatus", "APPROVED");
				q.setParameter("tripStatus", "COMPLETED");
				q.setParameter("tripStatus2", "SHOULD_BE_COMPLETED");
				q.setParameter("maxwaitDateTime", c.getTime());
				
				Object tripsObj = gDAO.search(q, 0);
				if(tripsObj != null)
				{
					completedTrips = (Vector<CorporateTrip>)tripsObj;
				}
			}
		}
		return completedTrips;
	}

	public void setCompletedTrips(Vector<CorporateTrip> completedTrips) {
		this.completedTrips = completedTrips;
	}

	public Long getVehicle_id() {
		return vehicle_id;
	}

	public void setVehicle_id(Long vehicle_id) {
		this.vehicle_id = vehicle_id;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
