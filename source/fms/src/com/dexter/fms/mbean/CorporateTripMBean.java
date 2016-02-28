package com.dexter.fms.mbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
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
import javax.persistence.Query;

import org.primefaces.model.DualListModel;

import com.dexter.common.util.Emailer;
import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.Notification;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.PartnerSetting;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.app.CorporateTrip;
import com.dexter.fms.model.app.CorporateTripPassenger;
import com.dexter.fms.model.app.Fleet;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleDriver;
import com.dexter.fms.model.app.VehicleStatusEnum;
import com.dexter.fms.model.app.VehicleTrackerData;
import com.dexter.fms.model.ref.LGA;
import com.dexter.fms.model.ref.State;
import com.dexter.fms.util.SMSGateway;
import com.greatnowhere.models.GooglePlaceEx;

@ManagedBean(name = "ctripBean")
@SessionScoped
public class CorporateTripMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-CorporateTripMBean");
	
	private FacesMessage msg = null;
	
	private Long partner_id;
	private Partner partner;
	
	private DualListModel<PartnerPersonel> personelsList;
	private Vector<PartnerPersonel> personels, selectedPersonels;
	private ArrayList<PartnerPersonel> tripManifest;
	private CorporateTrip trip;
	private Date tripSubmitMinDate;
	private Vector<CorporateTrip> pendingTrips, pending2Trips, ongoingTrips, completedTrips, myOngoingTrips, myAwaitingTrips, completionTripRequests;
	
	private Long division_id, department_id, unit_id, approver_id;
	private Long fleet_id;
	private Long vehicle_id, driver_id;
	private String regNo;
	private Vector<Fleet> fleets;
	
	private String vlocation, tripvlocation, vassignedDriver;
	private double vmileage, vfuellevel, tripvmileage, tripvfuellevel;
	
	private Long state_id, lga_id;
	private Vector<State> states;
	private Vector<LGA> lgas;
	
	private GooglePlaceEx place;
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public CorporateTripMBean()
	{}
	
	public void uncompleteTrip()
	{
		if(getTrip().getId() != null)
		{
			String naration = "Deny trip completion for Trip: " + getTrip().getVehicle().getRegistrationNo() + " (" + getTrip().getDepartureDateTime() + ")";
			GeneralDAO gDAO = new GeneralDAO();
			
			getTrip().setTripStatus("SHOULD_BE_COMPLETED");
			
			gDAO.startTransaction();
			boolean ret = gDAO.update(getTrip());
			if(ret) {
				if(getTrip().getVehicle() != null) {
					getTrip().getVehicle().setActiveStatus(VehicleStatusEnum.ON_TRIP.getStatus());
					gDAO.update(getTrip().getVehicle());
				}
				
				Notification notif = new Notification();
				notif.setCrt_dt(new Date());
				notif.setNotified(false);
				notif.setPage_url("trip_request");
				notif.setSubject("Trip Completion Request - DENIED");
				notif.setUser(getTrip().getCreatedBy());
				notif.setMessage("Your trip completion request is DENIED");
				gDAO.save(notif);
				
				gDAO.commit();
				
				final StringBuilder body = new StringBuilder("<html><body>");
				body.append("<p><strong>Dear ").append(getTrip().getStaff().getFirstname()).append("</strong></p>");
				body.append("<p>Your trip completion request for Vehicle: <strong>").append(getTrip().getVehicle().getRegistrationNo()).append("</strong> has been <strong><font color=red>DENIED</font></strong>.</p>");
				body.append("<p>Denied by: ").append(getTrip().getApproveUser2().getPersonel().getFirstname()).append(" ").append(getTrip().getApproveUser2().getPersonel().getLastname()).append("</p>");
				body.append("<p>Regards</br>Sattrak FMS Portal</p>");
				body.append("</body></html>");
				
				final String email = getTrip().getStaff().getEmail();
				final String phone = getTrip().getStaff().getPhone();
				Thread notifyThread = new Thread() {
					public void run() {
						try {
							if(email != null)
								Emailer.sendEmail("fms@sattrakservices.com", new String[]{email}, "FMS - Trip Completion Request - DENIED", body.toString());
						} catch(Exception ex) {
							ex.printStackTrace();
						}
						if(phone != null)
							try {
								SMSGateway.sendSMS("FMS", phone, "Your trip completion request for Vehicle: " + getTrip().getVehicle().getRegistrationNo() + " has been DENIED.");
							} catch(Exception ex) {
								ex.printStackTrace();
							}
					}
				};
				notifyThread.start();
				
				naration += ", Status: Success";
				dashBean.saveAudit(naration, "", getTrip().getVehicle());
				
				setTrip(null);
				setCompletionTripRequests(null);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Trip un-completed successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Error occurred during save: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				naration += ", Status: Failed: " + gDAO.getMessage();
				dashBean.saveAudit(naration, "", getTrip().getVehicle());
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid trip selected!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void completeTrip()
	{
		if(getTrip().getId() != null)
		{
			String naration = "Approve trip completion request for trip: " + getTrip().getVehicle().getRegistrationNo() + " (" + getTrip().getDepartureDateTime() + ")";
			GeneralDAO gDAO = new GeneralDAO();
			
			getTrip().setCheckInSubmitDateTime(new Date());
			getTrip().setTripStatus("COMPLETED");
			
			gDAO.startTransaction();
			boolean ret = gDAO.update(getTrip());
			if(ret)
			{
				if(getTrip().getVehicle() != null) {
					getTrip().getVehicle().setActiveStatus(VehicleStatusEnum.ACTIVE.getStatus());
					gDAO.update(getTrip().getVehicle());
				}
				
				gDAO.commit();
				
				naration += ", Status: Success";
				dashBean.saveAudit(naration, "", getTrip().getVehicle());
				
				setTrip(null);
				setOngoingTrips(null);
				setCompletedTrips(null);
				setCompletionTripRequests(null);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Trip completed successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Error occurred during save: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				naration += ", Status: Failed: " + gDAO.getMessage();
				dashBean.saveAudit(naration, "", getTrip().getVehicle());
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid trip selected!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void cancelMyTrip()
	{
		if(getTrip().getId() != null)
		{
			String naration = "Cancel trip: " + getTrip().getDepartureDateTime();
			GeneralDAO gDAO = new GeneralDAO();
			
			getTrip().setTripStatus("CANCELED");
			
			gDAO.startTransaction();
			boolean ret = gDAO.update(getTrip());
			if(ret) {
				if(getTrip().getVehicle() != null) {
					getTrip().getVehicle().setActiveStatus(VehicleStatusEnum.ACTIVE.getStatus());
					gDAO.update(getTrip().getVehicle());
				}
				
				gDAO.commit();
				
				naration += ", Status: Success";
				
				setTrip(null);
				setMyAwaitingTrips(null);
				setMyOngoingTrips(null);
				setPending2Trips(null);
				setPendingTrips(null);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Trip canceled successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Error occurred during save: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				naration += ", Status: Failed: " + gDAO.getMessage();
			}
			gDAO.destroy();
			
			dashBean.saveAudit(naration, "", null);
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid trip selected!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void requestCompleteTrip()
	{
		if(getTrip().getId() != null)
		{
			String naration = "Request for trip completion: " + getTrip().getVehicle().getRegistrationNo() + " (" + getTrip().getDepartureDateTime() + ")";
			GeneralDAO gDAO = new GeneralDAO();
			
			getTrip().setCompleteRequestDateTime(new Date());
			getTrip().setTripStatus("COMPLETION_REQUEST");
			
			gDAO.startTransaction();
			boolean ret = gDAO.update(getTrip());
			if(ret)
			{
				Notification notif = new Notification();
				notif.setCrt_dt(new Date());
				notif.setNotified(false);
				notif.setPage_url("attend_trips");
				notif.setSubject("Trip Completion Request");
				notif.setUser(getTrip().getApproveUser2());
				notif.setMessage("A trip completion request has just been submitted for one of the trips you approved");
				
				gDAO.save(notif);
				
				gDAO.commit();
				
				final StringBuilder body = new StringBuilder("<html><body>");
				body.append("<p><strong>Dear ").append(getTrip().getApproveUser2().getPersonel().getFirstname()).append("</strong></p>");
				body.append("<p>A trip completion request has just been submitted for one of the trips you approved. Details below: -</p>");
				body.append("<p>Staff: ").append(getTrip().getStaff().getFirstname()).append(" ").append(getTrip().getStaff().getLastname()).append("</br>");
				body.append("Vehicle: ").append(getTrip().getVehicle().getRegistrationNo()).append("</br>");
				body.append("Departure Date/Time: ").append(getTrip().getDepartureDateTime()).append("</br>");
				body.append("Est. Arrival Date/Time: ").append(getTrip().getEstimatedArrivalDateTime()).append("</br></p>");
				body.append("<p>Regards</br>Sattrak FMS Portal</p>");
				body.append("</body></html>");
				
				final String email = getTrip().getApproveUser2().getPersonel().getEmail();
				final String phone = getTrip().getApproveUser2().getPersonel().getPhone();
				Thread notifyThread = new Thread()
				{
					public void run()
					{
						try {
							if(email != null)
								Emailer.sendEmail("fms@sattrakservices.com", new String[]{email}, "FMS - Trip Completion Request", body.toString());
						} catch(Exception ex) {
							ex.printStackTrace();
						}
						if(phone != null)
							try {
								SMSGateway.sendSMS("FMS", phone, "A trip completion request has just been submitted for one of the trips you approved.");
							} catch(Exception ex) {
								ex.printStackTrace();
							}
					}
				};
				notifyThread.start();
				
				setTrip(null);
				setMyOngoingTrips(null);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Trip completion request submitted successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				naration += ", Status: Success";
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Error occurred during save: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				naration += ", Status: Failed: " + gDAO.getMessage();
			}
			gDAO.destroy();
			
			dashBean.saveAudit(naration, "", null);
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
			String naration = "Attend to trip request: " + getTrip().getStaff().getFirstname() + " " + getTrip().getStaff().getLastname() + ": " + getTrip().getDepartureDateTime() + ", " + getTrip().getArrivalLocation();
			GeneralDAO gDAO = new GeneralDAO();
			
			if(!dashBean.getUser().getPersonel().isFleetManager()) {
				PartnerUser userToNotif = null;
				
				getTrip().setAttendedDate(new Date());
				getTrip().setApproveUser(dashBean.getUser());
				
				naration += ": Approval Status: " + getTrip().getApprovalStatus();
				
				Notification notif = new Notification(), notif2 = null;
				notif.setCrt_dt(new Date());
				notif.setNotified(false);
				notif.setUser(getTrip().getCreatedBy());
				notif.setPage_url("trip_request");
				notif.setSubject("Trip Request - " + getTrip().getApprovalStatus() + " - By Unit Head");
				notif.setMessage("Your trip request with Departure Time: " + getTrip().getDepartureDateTime() + " and Destignation: " + getTrip().getArrivalLocation() + " has been: " + getTrip().getApprovalStatus() + " by your unit head. WAIT FOR FINAL NOTIFICATION FROM FLEET MANAGER!");
				final StringBuilder body = new StringBuilder("<html><body>");
				body.append("<p><strong>Dear ").append(getTrip().getStaff().getFirstname()).append("</strong></p>");
				body.append("<p>Your trip request with Departure Time: <strong>").append(getTrip().getDepartureDateTime()).append("</strong> and Destignation: <strong>").append(getTrip().getArrivalLocation()).append("</strong> has been: <strong>").append((getTrip().getApprovalStatus().equals("APPROVED")) ? "<font color=green>" : "<font color=red>").append(getTrip().getApprovalStatus()).append("</font></strong> by your unit head.</p>");
				body.append("<p>Reasons: <strong>").append(getTrip().getApprovalReason()).append("</strong></p>");
				if(getTrip().getApprovalStatus().equals("APPROVED")) {
					getTrip().setApprovalStatus2("PENDING");
					body.append("<p>Your request now needs to be approved by the Fleet Manager.</p>");
					
					Query q = gDAO.createQuery("Select e from PartnerUser e where e.partner=:partner and e.active=:active and e.personel.fleetManager=:fleetManager");
					q.setParameter("partner", dashBean.getUser().getPartner());
					q.setParameter("active", true);
					q.setParameter("fleetManager", true);
					
					Object objs = gDAO.search(q, 0);
					if(objs != null) {
						Vector<PartnerUser> usrs = (Vector<PartnerUser>)objs;
						for(PartnerUser e : usrs)
							userToNotif = e;
					}
					if(userToNotif != null) {
						notif2 = new Notification();
						notif2.setCrt_dt(new Date());
						notif2.setNotified(false);
						notif2.setUser(userToNotif);
						notif2.setPage_url("attend_trips");
						notif2.setSubject("Trip Request - " + getTrip().getApprovalStatus() + " - By Unit Head");
						notif2.setMessage("You have a new trip request with Departure Time: " + getTrip().getDepartureDateTime() + " and Destignation: " + getTrip().getArrivalLocation() + " which has been: " + getTrip().getApprovalStatus() + " by the unit head.");
					} else {
						gDAO.destroy();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Could not detect the fleet manager user account!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
						return;
					}
				}
				body.append("<p>Your request was attended to by: -</p>");
				body.append("<p>").append(getTrip().getApproveUser().getPersonel().getFirstname()).append(" ").append(getTrip().getApproveUser().getPersonel().getLastname()).append("</p>");
				body.append("<p>Regards</br>Sattrak FMS Portal</p>");
				body.append("</body></html>");
				
				gDAO.startTransaction();
				boolean ret = gDAO.update(getTrip());
				if(ret) {
					gDAO.save(notif);
					if(notif2 != null)
						gDAO.save(notif2);
					gDAO.commit();
					
					naration += ", Status: Success";
					
					final String email = getTrip().getStaff().getEmail();
					final String apStatus = getTrip().getApprovalStatus();
					final String phone = getTrip().getStaff().getPhone();
					
					final PartnerUser userToContact = userToNotif;
					StringBuilder ebody = new StringBuilder("<html><body>");
					if(userToContact != null) {
						ebody.append("<p><strong>Dear ").append(userToContact.getPersonel().getFirstname()).append("</strong></p>");
						ebody.append("<p>You have a new trip request as below: -</p>");
						ebody.append("<p><ul><li>Request By: ").append(getTrip().getStaff().getFirstname() + " " + getTrip().getStaff().getLastname()).append("</li>");
						ebody.append("<li>Departure Time: ").append(getTrip().getDepartureDateTime()).append("</li>");
						ebody.append("<li>Destination: ").append(getTrip().getArrivalLocation()).append("</li></ul></p>");
						ebody.append("<p>This trip has been approved by the unit head.</p>");
						ebody.append("<p>Regards</br>Sattrak FMS Portal</p>");
					}
					ebody.append("</body></html>");
					final String emailBody = ebody.toString();
					
					setTrip(null);
					setPendingTrips(null);
					setOngoingTrips(null);
					
					Thread notifyThread = new Thread() {
						public void run() {
							if(email != null)
								try {
									Emailer.sendEmail("fms@sattrakservices.com", new String[]{email}, "FMS - Trip Request - " + apStatus + " - By Unit Head", body.toString());
								} catch(Exception ex) {
									ex.printStackTrace();
								}
								
							if(phone != null)
								try {
									SMSGateway.sendSMS("FMS", phone, "Your trip request has been " + apStatus + " by your unit head.");
								} catch(Exception ex) {
									ex.printStackTrace();
								}
							
							if(userToContact != null) {
								if(userToContact.getPersonel().getEmail() != null) {
									try {
										Emailer.sendEmail("fms@sattrakservices.com", new String[]{userToContact.getPersonel().getEmail()}, "FMS - Trip Request", emailBody);
									} catch(Exception ex) {
										ex.printStackTrace();
									}
								}
								if(userToContact.getPersonel().getPhone() != null) {
									try {
										String message = "Hello, you have a pending trip request from " + getTrip().getStaff().getFirstname() + " " + getTrip().getStaff().getLastname() + ". Please log on to the FMS portal to attend to it. Thank you.";
										SMSGateway.sendSMS("FMS", userToContact.getPersonel().getPhone(), message);
									} catch(Exception ex) {
										ex.printStackTrace();
									}
								}
							}
						}
					};
					notifyThread.start();
					
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Trip attended to successfully!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				} else {
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Error occurred during save: " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
					
					naration += ", Status: Failed: " + gDAO.getMessage();
				}
				
				gDAO.destroy();
			} else if(dashBean.getUser().getPersonel().isFleetManager()) {
				getTrip().setAttendedDate2(new Date());
				getTrip().setApproveUser2(dashBean.getUser());
				
				naration += ": Approval Status: " + getTrip().getApprovalStatus();
				
				boolean valid = true;
				Vehicle v = null;
				
				Notification notif = new Notification();
				notif.setCrt_dt(new Date());
				notif.setNotified(false);
				notif.setUser(getTrip().getCreatedBy());
				notif.setPage_url("trip_request");
				notif.setSubject("Trip Request - " + getTrip().getApprovalStatus2() + " - By Fleet Manager");
				notif.setMessage("Your trip request with Departure Time: " + getTrip().getDepartureDateTime() + " and Destignation: " + getTrip().getArrivalLocation() + " has been: " + getTrip().getApprovalStatus2() + " by the fleet manager.");
				final StringBuilder body = new StringBuilder("<html><body>");
				body.append("<p><strong>Dear ").append(getTrip().getStaff().getFirstname()).append("</strong></p>");
				body.append("<p>Your trip request with Departure Time: <strong>").append(getTrip().getDepartureDateTime()).append("</strong> and Destignation: <strong>").append(getTrip().getArrivalLocation()).append("</strong> has been: <strong>").append((getTrip().getApprovalStatus2().equals("APPROVED")) ? "<font color=green>" : "<font color=red>").append(getTrip().getApprovalStatus2()).append("</font></strong></p>");
				body.append("<p>Reasons: <strong>").append(getTrip().getApprovalReason2()).append("</strong></p>");
				if(getTrip().getApprovalStatus2().equals("APPROVED")) {
					getTrip().setTripStatus("AWAITING");
					if(getVehicle_id() != null) {
						Object vObj = gDAO.find(Vehicle.class, getVehicle_id());
						if(vObj != null) {
							v = (Vehicle)vObj;
							if(v.isActive() && v.getActiveStatus().equals(VehicleStatusEnum.ACTIVE.getStatus())) {
								v.setActiveStatus(VehicleStatusEnum.BOOKED_FOR_TRIP.getStatus());
								getTrip().setVehicle(v);
								
								body.append("<p>Resources for your trip: -</p>");
								body.append("<p>Vehicle: ").append(v.getRegistrationNo()).append("<br/>");
								
								PartnerDriver pd = null;
								Object dObj = gDAO.find(PartnerDriver.class, getDriver_id());
								if(dObj != null)
									pd = (PartnerDriver)dObj;
								
								if(pd != null) {
									getTrip().setDriver(pd);
									body.append("Driver: ").append(getTrip().getDriver().getPersonel().getFirstname()).append(" ").append(getTrip().getDriver().getPersonel().getLastname()).append("</br>");
									body.append("Driver Phone: ").append(pd.getPersonel().getPhone()!=null ? pd.getPersonel().getPhone() : "N/A").append("</p>");
									valid = true;
								} else {
									valid = false;
									msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Please select a valid driver for the trip!");
									FacesContext.getCurrentInstance().addMessage(null, msg);
								}
							} else {
								valid = false;
								msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "In-active vehicle selected!");
								FacesContext.getCurrentInstance().addMessage(null, msg);
							}
						} else {
							// invalid vehicle selected
							valid = false;
							msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Unknown vehicle selected!");
							FacesContext.getCurrentInstance().addMessage(null, msg);
						}
					} else {
						// must select a vehicle after approval for the trip
						valid = false;
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Vehicle must be selected for a trip approval!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				body.append("<p>Your request was attended to by: -</p>");
				body.append("<p>").append(getTrip().getApproveUser2().getPersonel().getFirstname()).append(" ").append(getTrip().getApproveUser2().getPersonel().getLastname()).append("</p>");
				body.append("<p>Regards</br>Sattrak FMS Portal</p>");
				body.append("</body></html>");
				
				if(valid) {
					gDAO.startTransaction();
					boolean ret = gDAO.update(getTrip());
					if(ret) {
						gDAO.save(notif);
						if(v != null)
							gDAO.update(v);
						gDAO.commit();
						
						naration += ", Status: Success";
						
						final String email = getTrip().getStaff().getEmail();
						final String apStatus = getTrip().getApprovalStatus2();
						
						final String driverPhone = getTrip().getDriver() != null ? getTrip().getDriver().getPersonel().getPhone() : null;
						final String departTime = getTrip().getDepartureDateTime().toLocaleString();
						final String desti = getTrip().getArrivalLocation();
						final String forName = getTrip().getStaff().getFirstname() + " " + getTrip().getStaff().getLastname();
						
						setTrip(null);
						setPendingTrips(null);
						setOngoingTrips(null);
						
						Thread notifyThread = new Thread() {
							public void run() {
								try {
									if(email != null)
										Emailer.sendEmail("fms@sattrakservices.com", new String[]{email}, "FMS - Trip Request - " + apStatus + " - Fleet Manager", body.toString());
								} catch(Exception ex) {
									ex.printStackTrace();
								}
								
								if(driverPhone != null) {
									SMSGateway.sendSMS("FMS", driverPhone, "You have being assigned for a trip to depart at: " + departTime + " to destination: " + desti + ". The trip is for " + forName);
								}
							}
						};
						notifyThread.start();
						
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Trip attended to successfully!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					} else {
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Error occurred during save: " + gDAO.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, msg);
						
						naration += ", Status: Failed: " + gDAO.getMessage();
					}
				}
				
				gDAO.destroy();
				
				dashBean.saveAudit(naration, "", null);
			}
		} else {
			// invalid trip selected
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid trip selected!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void saveTripRequest()
	{
		if(getPartner() != null && getTrip().getDepartureDateTime() != null && getTrip().getDepartureLocation() != null &&
				getTrip().getEstimatedArrivalDateTime() != null && getTrip().getPurpose() != null &&
				getTrip().getArrivalLocation() != null && getApprover_id() != null && getApprover_id() > 0)
		{
			String naration = "Request for trip: " + getTrip().getDepartureDateTime() + ": " + getTrip().getArrivalLocation();
			if(getTrip().getDepartureDateTime().after(new Date())) {
				if(getTrip().getDepartureDateTime().before(getTrip().getEstimatedArrivalDateTime())) {
					GeneralDAO gDAO = new GeneralDAO();
					PartnerUser userToNotif = null;
					Object objs = null;
					
					State arrivalState = null;
					LGA arrivalLga = null;
					if(getState_id() != null && getState_id().longValue() > 0) {
						Object arrivalStateObj = gDAO.find(State.class, getState_id());
						if(arrivalStateObj!=null) {
							arrivalState = (State)arrivalStateObj;
							getTrip().setArrivalState(arrivalState);
						}
					}
					if(getLga_id() != null && getLga_id().longValue() > 0) {
						Object arrivalLgaObj = gDAO.find(LGA.class, getLga_id());
						if(arrivalLgaObj!=null) {
							arrivalLga = (LGA)arrivalLgaObj;
							getTrip().setArrivalLga(arrivalLga);
						}
					}
					
					getTrip().setPartner(getPartner());
					getTrip().setCreatedBy(dashBean.getUser());
					getTrip().setCrt_dt(new Date());
					getTrip().setStaff(dashBean.getUser().getPersonel());
					getTrip().setApprovalStatus("PENDING");
					getTrip().setApprovalStatus2("PENDING");
					getTrip().setTripStatus("PENDING");
					
					PartnerUser approver = null;
					try {
						Query q = gDAO.createQuery("Select e from PartnerUser e where e.personel.id=:p_id");
						q.setParameter("p_id", getApprover_id());
						Object obj = gDAO.search(q, 0);
						if(obj != null) {
							Vector<PartnerUser> list = (Vector<PartnerUser>)obj;
							for(PartnerUser e : list)
								approver = e;
						}
					} catch(Exception ex){}
					
					if(approver != null) {
						if(approver.getPersonel().isFleetManager()) {
							gDAO.destroy();
							msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "You can not submit a trip request directly to the fleet manager.");
							FacesContext.getCurrentInstance().addMessage(null, msg);
							return;
						}
						getTrip().setApproveUser(approver);
						
						// Get the unit head, so the person can be copied in the notification email
						Query q = gDAO.createQuery("Select e from PartnerUser e where e.partner=:partner and e.active=:active and e.personel.unit=:unit and e.personel.unitHead=:unitHead");
						q.setParameter("partner", dashBean.getUser().getPartner());
						q.setParameter("active", true);
						q.setParameter("unit", dashBean.getUser().getPersonel().getUnit());
						q.setParameter("unitHead", true);
						
						objs = gDAO.search(q, 0);
						if(objs != null) {
							Vector<PartnerUser> usrs = (Vector<PartnerUser>)objs;
							for(PartnerUser e : usrs)
								userToNotif = e;
						}
					}
					
					gDAO.startTransaction();
					
					if(approver != null)
					{
						Notification notif = new Notification();
						notif.setCrt_dt(new Date());
						notif.setNotified(false);
						notif.setUser(approver);
						notif.setPage_url("attend_trips");
						notif.setSubject("Trip Request By " + getTrip().getStaff().getFirstname() + " " + getTrip().getStaff().getLastname());
						notif.setMessage("You have a new trip request with Departure Time: " + getTrip().getDepartureDateTime() + " and Destination: " + getTrip().getArrivalLocation());
						gDAO.save(notif);
					
						if(gDAO.save(getTrip())) {
							System.out.println("Source Personel Size: " + getPersonelsList().getSource().size());
							if(getTripManifest() != null && getTripManifest().size() > 0) {
								for(PartnerPersonel pp : getTripManifest()) {
									CorporateTripPassenger ctp = new CorporateTripPassenger();
									ctp.setPassenger(pp);
									ctp.setTrip(getTrip());
									gDAO.save(ctp);
								}
							} else if(getPersonelsList().getTarget() != null && getPersonelsList().getTarget().size() > 0) {
								for(PartnerPersonel pp : getPersonelsList().getTarget()) {
									CorporateTripPassenger ctp = new CorporateTripPassenger();
									ctp.setPassenger(pp);
									ctp.setTrip(getTrip());
									gDAO.save(ctp);
								}
							}
							
							if(gDAO.commit()) {
								naration += ", Status: Success";
								
								final PartnerUser approverToContact = approver, userToContact = userToNotif;
								StringBuilder body = new StringBuilder("<html><body>");
								if(approverToContact != null) {
									body.append("<p><strong>Dear ").append(approverToContact.getPersonel().getFirstname()).append("</strong></p>");
									body.append("<p>You have a new trip request as below: -</p>");
									body.append("<p><ul><li>Request By: ").append(getTrip().getStaff().getFirstname() + " " + getTrip().getStaff().getLastname()).append("</li>");
									body.append("<li>Departure Time: ").append(getTrip().getDepartureDateTime()).append("</li>");
									body.append("<li>Purpose: ").append(getTrip().getPurpose()).append("</li>");
									body.append("<li>Departure Time: ").append(getTrip().getDepartureDateTime()).append("</li></ul></p>");
									body.append("<p>Regards</br></br>Sattrak FMS Portal</p>");
								}
								body.append("</body></html>");
								final String emailBody = body.toString();
								Thread notifyThread = new Thread() {
									public void run() {
										if(approverToContact != null) {
											if(approverToContact.getPersonel().getEmail() != null) {
												String[] email = null;
												if(userToContact != null)
													email = new String[] {approverToContact.getPersonel().getEmail(), userToContact.getPersonel().getEmail()};
												else
													email = new String[]{approverToContact.getPersonel().getEmail()};
												try {
													Emailer.sendEmail("fms@sattrakservices.com", email, "FMS - Trip Request", emailBody);
												} catch(Exception ex) {
													ex.printStackTrace();
												}
											}
											if(approverToContact.getPersonel().getPhone() != null) {
												try {
													String message = "Hello, you have a pending trip request from " + getTrip().getStaff().getFirstname() + " " + getTrip().getStaff().getLastname() + ". Please log on to the FMS portal to attend to it. Thank you.";
													SMSGateway.sendSMS("FMS", approverToContact.getPersonel().getPhone(), message);
												} catch(Exception ex) {
													ex.printStackTrace();
												}
											}
										}
									}
								};
								notifyThread.start();
								
								setTrip(null);
								setPendingTrips(null);
								setPersonels(null);
								setPersonelsList(null);
								setSelectedPersonels(null);
								setState_id(null);setLga_id(null);
								
								msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Trip request submitted successfully!");
								FacesContext.getCurrentInstance().addMessage(null, msg);
							} else {
								gDAO.rollback();
								msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Error occurred during save: " + gDAO.getMessage());
								FacesContext.getCurrentInstance().addMessage(null, msg);
								
								naration += ", Status: Failed: " + gDAO.getMessage();
							}
						}
						else
						{
							gDAO.rollback();
							msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Error occurred during save: " + gDAO.getMessage());
							FacesContext.getCurrentInstance().addMessage(null, msg);
							
							naration += ", Status: Failed: " + gDAO.getMessage();
						}
					} else {
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Approver not found!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
						
						naration += ", Status: Failed: Approver not found!";
					}
					gDAO.destroy();
					
					dashBean.saveAudit(naration, "", null);
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
		if(personelsList == null) {
			personelsList = new DualListModel<PartnerPersonel>(getPersonels(), getTripManifest());
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
				gDAO.destroy();
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

	public ArrayList<PartnerPersonel> getTripManifest() {
		if(tripManifest == null)
			tripManifest = new ArrayList<PartnerPersonel>();
		return tripManifest;
	}

	public void setTripManifest(ArrayList<PartnerPersonel> tripManifest) {
		this.tripManifest = tripManifest;
	}

	public CorporateTrip getTrip() {
		if(trip == null)
			trip = new CorporateTrip();
		return trip;
	}

	public void setTrip(CorporateTrip trip) {
		this.trip = trip;
	}

	public Date getTripSubmitMinDate() {
		PartnerSetting setting = dashBean.getSetting();
		if(setting != null && setting.getMaxMinutesToBookTrip() > 0) {
			Calendar can = Calendar.getInstance();
			can.add(Calendar.MINUTE, setting.getMaxMinutesToBookTrip());
			tripSubmitMinDate = can.getTime();
		}
		if(tripSubmitMinDate == null)
			tripSubmitMinDate = new Date();
		return tripSubmitMinDate;
	}

	public void setTripSubmitMinDate(Date tripSubmitMinDate) {
		this.tripSubmitMinDate = tripSubmitMinDate;
	}

	@SuppressWarnings("unchecked")
	public Vector<CorporateTrip> getPendingTrips() {
		if(dashBean.getUser().getPersonel().isUnitHead() || dashBean.getUser().getPersonel().isFleetManager())
		{
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
			if(research) {
				pendingTrips = null;
				if(getPartner() != null) {
					GeneralDAO gDAO = new GeneralDAO();
					Object tripsObj = null;
					if(!dashBean.getUser().getPersonel().isFleetManager()) {
						Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and e.approveUser.id=:approveUser and e.approvalStatus=:approvalStatus and e.departureDateTime > :nowDateTime");
						q.setParameter("partner", getPartner());
						q.setParameter("approveUser", dashBean.getUser().getId());
						q.setParameter("approvalStatus", "PENDING");
						q.setParameter("nowDateTime", new Date());
						
						tripsObj = gDAO.search(q, 0);
					} else if(dashBean.getUser().getPersonel().isFleetManager()) {
						Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and e.approvalStatus2=:approvalStatus2 and e.approvalStatus=:approvalStatus and e.departureDateTime > :nowDateTime");
						q.setParameter("partner", getPartner());
						q.setParameter("approvalStatus", "APPROVED");
						q.setParameter("approvalStatus2", "PENDING");
						q.setParameter("nowDateTime", new Date());
						
						tripsObj = gDAO.search(q, 0);
					}
					
					if(tripsObj != null) {
						pendingTrips = (Vector<CorporateTrip>)tripsObj;
					}
					gDAO.destroy();
				}
			}
		}
		return pendingTrips;
	}

	public void setPendingTrips(Vector<CorporateTrip> pendingTrips) {
		this.pendingTrips = pendingTrips;
	}

	@SuppressWarnings("unchecked")
	public Vector<CorporateTrip> getPending2Trips() {
		if(dashBean.getUser().getPersonel().isUnitHead() || dashBean.getUser().getPersonel().isFleetManager()) {
			boolean research = true;
			if(pending2Trips == null || pending2Trips.size() == 0)
				research = true;
			else if(pending2Trips.size() > 0) {
				if(getPartner() != null) {
					if(pending2Trips.get(0).getPartner().getId() == getPartner().getId())
						research = false;
				}
			}
			if(research) {
				pending2Trips = null;
				if(getPartner() != null) {
					GeneralDAO gDAO = new GeneralDAO();
					Object tripsObj = null;
					if(!dashBean.getUser().getPersonel().isFleetManager()) {
						Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and e.approveUser.id=:approveUser and e.approvalStatus=:approvalStatus and e.departureDateTime > :nowDateTime and e.tripStatus=:tripStatus");
						q.setParameter("partner", getPartner());
						q.setParameter("approveUser", dashBean.getUser().getId());
						q.setParameter("approvalStatus", "APPROVED");
						q.setParameter("nowDateTime", new Date());
						q.setParameter("tripStatus", "AWAITING");
						
						tripsObj = gDAO.search(q, 0);
					} else if(dashBean.getUser().getPersonel().isFleetManager()) {
						Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and e.approvalStatus2=:approvalStatus2 and e.approvalStatus=:approvalStatus and e.departureDateTime > :nowDateTime and e.tripStatus=:tripStatus");
						q.setParameter("partner", getPartner());
						q.setParameter("approvalStatus", "APPROVED");
						q.setParameter("approvalStatus2", "APPROVED");
						q.setParameter("nowDateTime", new Date());
						q.setParameter("tripStatus", "AWAITING");
						
						tripsObj = gDAO.search(q, 0);
					}
					
					if(tripsObj != null) {
						pending2Trips = (Vector<CorporateTrip>)tripsObj;
					}
					gDAO.destroy();
				}
			}
		}
		return pending2Trips;
	}

	public void setPending2Trips(Vector<CorporateTrip> pending2Trips) {
		this.pending2Trips = pending2Trips;
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
				
				Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and e.approvalStatus=:approvalStatus and (e.tripStatus=:tripStatus or e.tripStatus=:tripStatus2)");
				q.setParameter("partner", getPartner());
				q.setParameter("approvalStatus", "APPROVED");
				q.setParameter("tripStatus", "ON_TRIP");
				q.setParameter("tripStatus2", "SHOULD_BE_COMPLETED");
				
				Object tripsObj = gDAO.search(q, 0);
				if(tripsObj != null)
				{
					ongoingTrips = (Vector<CorporateTrip>)tripsObj;
				}
				gDAO.destroy();
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
				
				Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and e.approvalStatus=:approvalStatus and (e.tripStatus=:tripStatus or e.tripStatus=:tripStatus2)");
				q.setParameter("partner", getPartner());
				q.setParameter("approvalStatus", "APPROVED");
				q.setParameter("tripStatus", "COMPLETED");
				q.setParameter("tripStatus2", "SHOULD_BE_COMPLETED");
				
				Object tripsObj = gDAO.search(q, 0);
				if(tripsObj != null)
				{
					completedTrips = (Vector<CorporateTrip>)tripsObj;
				}
				gDAO.destroy();
			}
		}
		return completedTrips;
	}

	public void setCompletedTrips(Vector<CorporateTrip> completedTrips) {
		this.completedTrips = completedTrips;
	}

	@SuppressWarnings("unchecked")
	public Vector<CorporateTrip> getMyOngoingTrips() {
		boolean research = true;
		if(myOngoingTrips == null || myOngoingTrips.size() == 0)
			research = true;
		else if(myOngoingTrips.size() > 0)
		{
			if(getPartner() != null)
			{
				if(myOngoingTrips.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			myOngoingTrips = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and e.approvalStatus=:approvalStatus and (e.tripStatus=:tripStatus or e.tripStatus=:tripStatus2) and e.createdBy = :createdBy");
				q.setParameter("partner", getPartner());
				q.setParameter("approvalStatus", "APPROVED");
				q.setParameter("tripStatus", "ON_TRIP");
				q.setParameter("tripStatus2", "SHOULD_BE_COMPLETED");
				q.setParameter("createdBy", dashBean.getUser());
				
				Object tripsObj = gDAO.search(q, 0);
				if(tripsObj != null)
				{
					myOngoingTrips = (Vector<CorporateTrip>)tripsObj;
				}
				gDAO.destroy();
			}
		}
		return myOngoingTrips;
	}

	public void setMyOngoingTrips(Vector<CorporateTrip> myOngoingTrips) {
		this.myOngoingTrips = myOngoingTrips;
	}

	@SuppressWarnings("unchecked")
	public Vector<CorporateTrip> getMyAwaitingTrips() {
		boolean research = true;
		if(myAwaitingTrips == null || myAwaitingTrips.size() == 0)
			research = true;
		else if(myAwaitingTrips.size() > 0)
		{
			if(getPartner() != null)
			{
				if(myAwaitingTrips.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			myAwaitingTrips = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and (e.approvalStatus=:approvalStatus or e.approvalStatus=:approvalStatus2) and (e.tripStatus=:tripStatus or e.tripStatus=:tripStatus2) and e.createdBy = :createdBy");
				q.setParameter("partner", getPartner());
				q.setParameter("approvalStatus", "APPROVED");
				q.setParameter("approvalStatus2", "PENDING");
				q.setParameter("tripStatus", "AWAITING");
				q.setParameter("tripStatus2", "PENDING");
				q.setParameter("createdBy", dashBean.getUser());
				
				Object tripsObj = gDAO.search(q, 0);
				if(tripsObj != null) {
					myAwaitingTrips = (Vector<CorporateTrip>)tripsObj;
				}
				gDAO.destroy();
			}
		}
		return myAwaitingTrips;
	}

	public void setMyAwaitingTrips(Vector<CorporateTrip> myAwaitingTrips) {
		this.myAwaitingTrips = myAwaitingTrips;
	}

	@SuppressWarnings("unchecked")
	public Vector<CorporateTrip> getCompletionTripRequests() {
		boolean research = true;
		if(completionTripRequests == null || completionTripRequests.size() == 0)
			research = true;
		else if(completionTripRequests.size() > 0)
		{
			if(getPartner() != null)
			{
				if(completionTripRequests.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			completionTripRequests = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from CorporateTrip e where e.partner=:partner and e.approvalStatus=:approvalStatus and e.tripStatus=:tripStatus and e.approveUser=:approveUser");
				q.setParameter("partner", getPartner());
				q.setParameter("approvalStatus", "APPROVED");
				q.setParameter("tripStatus", "COMPLETION_REQUEST");
				q.setParameter("approveUser", dashBean.getUser());
				
				Object tripsObj = gDAO.search(q, 0);
				if(tripsObj != null)
				{
					completionTripRequests = (Vector<CorporateTrip>)tripsObj;
				}
				gDAO.destroy();
			}
		}
		return completionTripRequests;
	}

	public void setCompletionTripRequests(
			Vector<CorporateTrip> completionTripRequests) {
		this.completionTripRequests = completionTripRequests;
	}

	public Long getDivision_id() {
		return division_id;
	}

	public void setDivision_id(Long division_id) {
		this.division_id = division_id;
	}

	public Long getDepartment_id() {
		return department_id;
	}

	public void setDepartment_id(Long department_id) {
		this.department_id = department_id;
	}

	public Long getUnit_id() {
		return unit_id;
	}

	public void setUnit_id(Long unit_id) {
		this.unit_id = unit_id;
	}

	public Long getApprover_id() {
		return approver_id;
	}

	public void setApprover_id(Long approver_id) {
		this.approver_id = approver_id;
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

	public Long getDriver_id() {
		return driver_id;
	}

	public void setDriver_id(Long driver_id) {
		this.driver_id = driver_id;
	}

	public String getRegNo() {
		return regNo;
	}

	public void setRegNo(String regNo) {
		this.regNo = regNo;
	}

	@SuppressWarnings("unchecked")
	public Vector<Fleet> getFleets() {
		boolean research = true;
		if(fleets == null || fleets.size() == 0)
			research = true;
		
		if(research)
		{
			fleets = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				Object dpsObj = gDAO.search("Fleet", params);
				if(dpsObj != null)
				{
					fleets = (Vector<Fleet>)dpsObj;
				}
				gDAO.destroy();
			}
		}
		return fleets;
	}

	public void setFleets(Vector<Fleet> fleets) {
		this.fleets = fleets;
	}

	@SuppressWarnings("unchecked")
	public String getVlocation() {
		vlocation = "";
		if(getVehicle_id() != null && getVehicle_id().intValue() > 0) {
			GeneralDAO gDAO = new GeneralDAO();
			Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.id = :v_id");
			q.setParameter("v_id", getVehicle_id());
			Object vtdObj = gDAO.search(q, 0);
			if(vtdObj != null) {
				List<VehicleTrackerData> vtdList = (List<VehicleTrackerData>)vtdObj;
				for(VehicleTrackerData e : vtdList) {
					vlocation = e.getAddress();
				}
			}
			gDAO.destroy();
		}
		return vlocation;
	}

	public void setVlocation(String vlocation) {
		this.vlocation = vlocation;
	}

	@SuppressWarnings("unchecked")
	public String getTripvlocation() {
		tripvlocation = "";
		if(getTrip() != null && getTrip().getVehicle() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.id = :v_id");
			q.setParameter("v_id", getTrip().getVehicle().getId());
			Object vtdObj = gDAO.search(q, 0);
			if(vtdObj != null) {
				List<VehicleTrackerData> vtdList = (List<VehicleTrackerData>)vtdObj;
				for(VehicleTrackerData e : vtdList) {
					tripvlocation = e.getAddress();
				}
			}
			gDAO.destroy();
		}
		return tripvlocation;
	}

	public void setTripvlocation(String tripvlocation) {
		this.tripvlocation = tripvlocation;
	}

	@SuppressWarnings("unchecked")
	public String getVassignedDriver() {
		vassignedDriver = "N/A";
		if(getVehicle_id() != null && getVehicle_id().intValue() > 0) {
			GeneralDAO gDAO = new GeneralDAO();
			Query q = gDAO.createQuery("Select e from VehicleDriver e where e.vehicle.id = :v_id and e.active=:active");
			q.setParameter("v_id", getVehicle_id());
			q.setParameter("active", true);
			Object vtdObj = gDAO.search(q, 0);
			if(vtdObj != null) {
				List<VehicleDriver> vtdList = (List<VehicleDriver>)vtdObj;
				for(VehicleDriver e : vtdList) {
					vassignedDriver = e.getDriver().getPersonel().getFirstname() + " " + e.getDriver().getPersonel().getLastname();
				}
			}
			gDAO.destroy();
		}
		return vassignedDriver;
	}

	public void setVassignedDriver(String vassignedDriver) {
		this.vassignedDriver = vassignedDriver;
	}

	@SuppressWarnings("unchecked")
	public double getVmileage() {
		vmileage = 0;
		if(getVehicle_id() != null && getVehicle_id().intValue() > 0) {
			GeneralDAO gDAO = new GeneralDAO();
			Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.id = :v_id");
			q.setParameter("v_id", getVehicle_id());
			Object vtdObj = gDAO.search(q, 0);
			if(vtdObj != null) {
				List<VehicleTrackerData> vtdList = (List<VehicleTrackerData>)vtdObj;
				for(VehicleTrackerData e : vtdList) {
					vmileage = e.getOdometer();
				}
			}
			gDAO.destroy();
		}
		return vmileage;
	}

	public void setVmileage(double vmileage) {
		this.vmileage = vmileage;
	}

	@SuppressWarnings("unchecked")
	public double getVfuellevel() {
		vfuellevel = 0;
		if(getVehicle_id() != null && getVehicle_id().intValue() > 0) {
			GeneralDAO gDAO = new GeneralDAO();
			Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.id = :v_id");
			q.setParameter("v_id", getVehicle_id());
			Object vtdObj = gDAO.search(q, 0);
			if(vtdObj != null) {
				List<VehicleTrackerData> vtdList = (List<VehicleTrackerData>)vtdObj;
				for(VehicleTrackerData e : vtdList) {
					vfuellevel = e.getFuelLevel();
				}
			}
			gDAO.destroy();
		}
		return vfuellevel;
	}

	public void setVfuellevel(double vfuellevel) {
		this.vfuellevel = vfuellevel;
	}

	@SuppressWarnings("unchecked")
	public double getTripvmileage() {
		tripvmileage = 0;
		if(getTrip() != null && getTrip().getVehicle() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.id = :v_id");
			q.setParameter("v_id", getTrip().getVehicle().getId());
			Object vtdObj = gDAO.search(q, 0);
			if(vtdObj != null) {
				List<VehicleTrackerData> vtdList = (List<VehicleTrackerData>)vtdObj;
				for(VehicleTrackerData e : vtdList) {
					tripvmileage = e.getOdometer();
				}
			}
			gDAO.destroy();
		}
		return tripvmileage;
	}

	public void setTripvmileage(double tripvmileage) {
		this.tripvmileage = tripvmileage;
	}

	@SuppressWarnings("unchecked")
	public double getTripvfuellevel() {
		tripvfuellevel = 0;
		if(getTrip() != null && getTrip().getVehicle() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.id = :v_id");
			q.setParameter("v_id", getTrip().getVehicle().getId());
			Object vtdObj = gDAO.search(q, 0);
			if(vtdObj != null) {
				List<VehicleTrackerData> vtdList = (List<VehicleTrackerData>)vtdObj;
				for(VehicleTrackerData e : vtdList) {
					tripvfuellevel = e.getFuelLevel();
				}
			}
			gDAO.destroy();
		}
		return tripvfuellevel;
	}

	public void setTripvfuellevel(double tripvfuellevel) {
		this.tripvfuellevel = tripvfuellevel;
	}

	public Long getState_id() {
		return state_id;
	}

	public void setState_id(Long state_id) {
		this.state_id = state_id;
	}

	public Long getLga_id() {
		return lga_id;
	}

	public void setLga_id(Long lga_id) {
		this.lga_id = lga_id;
	}

	@SuppressWarnings("unchecked")
	public Vector<State> getStates() {
		if(states == null || states.size() == 0) {
			GeneralDAO gDAO = new GeneralDAO();
			Object listObj = gDAO.findAll("State");
			if(listObj != null)
				states = (Vector<State>)listObj;
			gDAO.destroy();
		}
		return states;
	}

	public void setStates(Vector<State> states) {
		this.states = states;
	}

	@SuppressWarnings("unchecked")
	public Vector<LGA> getLgas() {
		if(getState_id() != null && getState_id().longValue() > 0) {
			GeneralDAO gDAO = new GeneralDAO();
			Query q = gDAO.createQuery("Select e from LGA e where e.state.id=:state_id");
			q.setParameter("state_id", getState_id());
			Object listObj = gDAO.search(q, 0);
			if(listObj != null)
				lgas = (Vector<LGA>)listObj;
			gDAO.destroy();
		}
		return lgas;
	}

	public void setLgas(Vector<LGA> lgas) {
		this.lgas = lgas;
	}

	public GooglePlaceEx getPlace() {
		return place;
	}

	public void setPlace(GooglePlaceEx place) {
		this.place = place;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
