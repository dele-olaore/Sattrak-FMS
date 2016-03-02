package com.dexter.fms.jersey.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.persistence.Query;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.dexter.common.util.Hasher;
import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.dao.ReportDAO;
import com.dexter.fms.jersey.response.FMSRestListResponse;
import com.dexter.fms.jersey.response.FMSRestLoginResponse;
import com.dexter.fms.jersey.response.FMSRestResponse;
import com.dexter.fms.jersey.response.FMSRestListResponse.Entity;
import com.dexter.fms.model.Audit;
import com.dexter.fms.model.PartnerSubscription;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.PartnerUserRole;
import com.dexter.fms.model.app.Approver;
import com.dexter.fms.model.app.CorporateTrip;
import com.dexter.fms.model.app.ExpenseRequest;
import com.dexter.fms.model.app.VehicleAccident;
import com.dexter.fms.model.app.VehicleFuelingRequest;
import com.dexter.fms.model.app.VehicleMaintenanceRequest;
import com.dexter.fms.model.app.WorkOrder;

@Path("/")
public class FMSRestService
{
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:MM:SS");
	
	public FMSRestService() {}
	
	private void saveAudit(String narration, PartnerUser user)
	{
		Audit audit = new Audit();
		audit.setAction_dt(new Date());
		audit.setUser(user);
		audit.setNarration(narration);
		
		GeneralDAO gDAO = new GeneralDAO();
		gDAO.startTransaction();
		if(gDAO.save(audit))
			gDAO.commit();
		else
			gDAO.rollback();
		gDAO.destroy();
	}
	
	@GET
    @Produces("application/json")
	@Path("available")
	public FMSRestResponse available() {
		FMSRestResponse resp = new FMSRestResponse();
		resp.setSuccess(true);
		resp.setMessage("Yes");
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Produces("application/json")
	@Path("login")
	public FMSRestLoginResponse login(@FormParam("username")String username, @FormParam("password")String password, 
			@FormParam("partnerCode")String partnerCode) {
		FMSRestLoginResponse resp = new FMSRestLoginResponse();
		
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("username", username);
		params.put("partner_code", password);
		
		Object foundUsers = gDAO.search("PartnerUser", params);
		if(foundUsers != null) {
			Vector<PartnerUser> list = (Vector<PartnerUser>)foundUsers;
			if(list.size()>0) {
				PartnerUser foundUser = list.get(0);
				if(foundUser.getPassword().equals(Hasher.getHashValue(password))) {
					if(foundUser.isActivated()) {
						boolean proceed = true;
						if(!foundUser.getPartner().isSattrak()) {
							params = new Hashtable<String, Object>();
							params.put("partner", foundUser.getPartner());
							params.put("active", true);
							params.put("expired", false);
							Object foundSubs = gDAO.search("PartnerSubscription", params);
							if(foundSubs != null) {
								Vector<PartnerSubscription> subs = (Vector<PartnerSubscription>)foundSubs;
								if(subs.size() > 0) {
									proceed = true;
								} else {
									proceed = false;
									resp.setSuccess(false);
									resp.setMessage("Authentication successful but no active subscription!");
									saveAudit("MOBILE-LOGIN: Authentication successful for user: " + username + " but no active subscription!", foundUser);
								}
							} else {
								proceed = false;
								resp.setSuccess(false);
								resp.setMessage("Authentication successful but no active subscription!");
								saveAudit("MOBILE-LOGIN: Authentication successful for user: " + username + " but no active subscription!", foundUser);
							}
						}
						if(proceed) {
							Hashtable<String, Object> params2 = new Hashtable<String, Object>();
							params2.put("user", foundUser);
							Object foundRoles = gDAO.search("PartnerUserRole", params2);
							if(foundRoles != null) {
								Vector<PartnerUserRole> urolesList = (Vector<PartnerUserRole>)foundRoles;
								for(PartnerUserRole pur : urolesList)
									resp.setRoleName(pur.getRole().getName());
							}
							
							resp.setSuccess(true);
							resp.setMessage("Authentication successful!");
							resp.setFirstname(foundUser.getPersonel().getFirstname());
							resp.setLastname(foundUser.getPersonel().getLastname());
							resp.setPartnerName(foundUser.getPartner().getName());
							resp.setUserId(foundUser.getId());
							resp.setPartnerId(foundUser.getPartner().getId());
							saveAudit("MOBILE-LOGIN: Authentication successful for user: " + username + ", Partner: " + foundUser.getPartner().getName(), foundUser);
						}
					} else {
						resp.setSuccess(false);
						resp.setMessage("Authentication successful! However you need to reset your initial password before you can continue. Please go to the web portal to reset your initial password.");
						saveAudit("MOBILE-LOGIN: Authentication successful for user: " + username + ", Partner: " + foundUser.getPartner().getName() + ". But needs to activate account.", foundUser);
					}
				} else {
					resp.setSuccess(false);
					resp.setMessage("Authentication failed!");
					saveAudit("MOBILE-LOGIN: Authentication failed for user: " + username, null);
				}
			} else {
				resp.setSuccess(false);
				resp.setMessage("User does not exist. No record found.");
				saveAudit("MOBILE-LOGIN: User does not exist: " + username, null);
			}
		} else {
			resp.setSuccess(false);
			resp.setMessage("Authentication failed!");
			saveAudit("MOBILE-LOGIN: User does not exist: " + username, null);
		}
		gDAO.destroy();
		
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	@GET
    @Produces("application/json")
	@Path("pendingMaintenanceRequests")
	public FMSRestResponse pendingMaintRequests(@QueryParam("userId") String userId) {
		FMSRestResponse resp = new FMSRestResponse();
		
		long count = 0;
		GeneralDAO gDAO = new GeneralDAO();
		Query q = gDAO.createQuery("Select e from Approver e where e.entityName='VehicleMaintenanceRequest' and e.approvalUser.id=:userId and e.approvalStatus='PENDING' order by e.crt_dt");
		try {
			q.setParameter("userId", Long.parseLong(userId));
		} catch(Exception ex) {
			ex.printStackTrace();
			resp.setSuccess(false);
			resp.setMessage("Invalid User ID format supplied!");
			saveAudit("MOBILE-PENDING-MAINTENANCE-REQUESTS: Invalid User ID format supplied!: " + userId, null);
			
			gDAO.destroy();
			return resp;
		}
		Object obj = gDAO.search(q, 0);
		if(obj != null) {
			Vector<Approver> apList = (Vector<Approver>)obj;
			if(apList.size() > 0) {
				for(Approver ap : apList) {
					Object mobj = gDAO.find(VehicleMaintenanceRequest.class, ap.getEntityId());
					if(mobj != null) {
						count += 1;
					}
				}
			}
		}
		gDAO.destroy();
		
		resp.setSuccess(true);
		resp.setMessage(""+count);
		
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	@GET
    @Produces("application/json")
	@Path("pendingMaintRequestsList")
	public FMSRestListResponse pendingMaintRequestsList(@QueryParam("userId") String userId) {
		FMSRestListResponse resp = new FMSRestListResponse();
		List<Entity> list = null;
		
		GeneralDAO gDAO = new GeneralDAO();
		Query q = gDAO.createQuery("Select e from Approver e where e.entityName='VehicleMaintenanceRequest' and e.approvalUser.id=:userId and e.approvalStatus='PENDING' order by e.crt_dt");
		try {
			q.setParameter("userId", Long.parseLong(userId));
		} catch(Exception ex) {
			ex.printStackTrace();
			resp.setSuccess(false);
			resp.setMessage("Invalid User ID format supplied!");
			saveAudit("MOBILE-PENDING-MAINTENANCE-REQUESTS-LIST: Invalid User ID format supplied!: " + userId, null);
			
			gDAO.destroy();
			return resp;
		}
		Object obj = gDAO.search(q, 0);
		if(obj != null) {
			Vector<Approver> apList = (Vector<Approver>)obj;
			if(apList.size() > 0) {
				list = new ArrayList<Entity>();
				for(Approver ap : apList) {
					Object mobj = gDAO.find(VehicleMaintenanceRequest.class, ap.getEntityId());
					if(mobj != null) {
						VehicleMaintenanceRequest e = (VehicleMaintenanceRequest)mobj;
						Entity entity = resp.new Entity();
						entity.setDate(sdf.format(e.getCrt_dt()));
						entity.setDescription(e.getDescription());
						entity.setEntity(e.getVehicle().getRegistrationNo());
						entity.setRequestBy(e.getCreatedBy().getPersonel().getFirstname() + " " + e.getCreatedBy().getPersonel().getLastname());
						entity.setType(e.getMaintenanceType());
						list.add(entity);
					}
				}
			}
		}
		gDAO.destroy();
		
		if(list != null) {
			resp.setSuccess(true);
			resp.setList(list);
			resp.setMessage(list.size() + " record(s) found!");
		} else {
			resp.setSuccess(false);
			resp.setMessage("No record found!");
		}
		
		return resp;
	}
	
	@GET
    @Produces("application/json")
	@Path("pendingAccidentRepairRequests")
	public FMSRestResponse pendingAccidentRepairRequests(@QueryParam("partnerId") String partnerId) {
		FMSRestResponse resp = new FMSRestResponse();
		long count = 0;
		try {
			count = new ReportDAO().getPendingAccidentRepairRequestCount(Long.parseLong(partnerId));
		} catch(Exception ex) {
			ex.printStackTrace();
			resp.setSuccess(false);
			resp.setMessage("Invalid Partner ID format supplied!");
			saveAudit("MOBILE-PENDING-ACCIDENT-REPAIR-REQUESTS: Invalid Partner ID format supplied!: " + partnerId, null);
			return resp;
		}
		resp.setSuccess(true);
		resp.setMessage(""+count);
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	@GET
    @Produces("application/json")
	@Path("pendingAccidentRepairRequestsList")
	public FMSRestListResponse pendingAccidentRepairRequestsList(@QueryParam("partnerId") String partnerId) {
		FMSRestListResponse resp = new FMSRestListResponse();
		List<Entity> list = null;
		
		GeneralDAO gDAO = new GeneralDAO();
		Query q = gDAO.createQuery("Select e from VehicleAccident e where e.repairApprovedDesc='PENDING' and e.active=true and e.vehicle.partner.id=:p_id");
		try {
			q.setParameter("p_id", Long.parseLong(partnerId));
		} catch(Exception ex) {
			ex.printStackTrace();
			resp.setSuccess(false);
			resp.setMessage("Invalid Partner ID format supplied!");
			saveAudit("MOBILE-PENDING-ACCIDENT-REPAIR-REQUESTS-LIST: Invalid Partner ID format supplied!: " + partnerId, null);
			
			gDAO.destroy();
			return resp;
		}
		Object obj = gDAO.search(q, 0);
		if(obj != null) {
			list = new ArrayList<Entity>();
			Vector<VehicleAccident> apList = (Vector<VehicleAccident>)obj;
			for(VehicleAccident e : apList) {
				Entity entity = resp.new Entity();
				entity.setDate(sdf.format(e.getAccident_dt()));
				entity.setDescription(e.getAccidentDescription());
				entity.setEntity(e.getVehicle().getRegistrationNo());
				entity.setRequestBy(e.getCreatedBy().getPersonel().getFirstname() + " " + e.getCreatedBy().getPersonel().getLastname());
				entity.setType("Accident");
				list.add(entity);
			}
		}
		gDAO.destroy();
		
		if(list != null) {
			resp.setSuccess(true);
			resp.setList(list);
			resp.setMessage(list.size() + " record(s) found!");
		} else {
			resp.setSuccess(false);
			resp.setMessage("No record found!");
		}
		
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	@GET
    @Produces("application/json")
	@Path("pendingWorkorderForApprovalRequests")
	public FMSRestResponse pendingWorkorderForApprovalRequests(@QueryParam("userId") String userId) {
		FMSRestResponse resp = new FMSRestResponse();
		
		long count = 0;
		GeneralDAO gDAO = new GeneralDAO();
		
		String str = "Select e from WorkOrder e where e.status=:status and e.approveBy.id=:userId";
		Query q = gDAO.createQuery(str);
		q.setParameter("status", "REQUEST");
		try {
			q.setParameter("userId", Long.parseLong(userId));
		} catch(Exception ex) {
			ex.printStackTrace();
			resp.setSuccess(false);
			resp.setMessage("Invalid User ID format supplied!");
			saveAudit("MOBILE-PENDING-WORKORDER-REQUESTS: Invalid User ID format supplied!: " + userId, null);
			
			gDAO.destroy();
			return resp;
		}
		Object obj = gDAO.search(q, 0);
		if(obj != null) {
			Vector<WorkOrder> apList = (Vector<WorkOrder>)obj;
			count = apList.size();
		}
		gDAO.destroy();
		
		resp.setSuccess(true);
		resp.setMessage(""+count);
		
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	@GET
    @Produces("application/json")
	@Path("pendingWorkorderForApprovalRequestsList")
	public FMSRestListResponse pendingWorkorderForApprovalRequestsList(@QueryParam("userId") String userId) {
		FMSRestListResponse resp = new FMSRestListResponse();
		List<Entity> list = null;
		GeneralDAO gDAO = new GeneralDAO();
		
		String str = "Select e from WorkOrder e where e.status=:status and e.approveBy.id=:userId";
		Query q = gDAO.createQuery(str);
		q.setParameter("status", "REQUEST");
		try {
			q.setParameter("userId", Long.parseLong(userId));
		} catch(Exception ex) {
			ex.printStackTrace();
			resp.setSuccess(false);
			resp.setMessage("Invalid User ID format supplied!");
			saveAudit("MOBILE-PENDING-WORKORDER-REQUESTS-LIST: Invalid User ID format supplied!: " + userId, null);
			
			gDAO.destroy();
			return resp;
		}
		Object obj = gDAO.search(q, 0);
		if(obj != null) {
			list = new ArrayList<Entity>();
			Vector<WorkOrder> apList = (Vector<WorkOrder>)obj;
			for(WorkOrder e : apList) {
				Entity entity = resp.new Entity();
				entity.setDate(sdf.format(e.getCrt_dt()));
				entity.setDescription(e.getSummaryDetailsOfWorkOrder());
				entity.setEntity(e.getWorkOrderNumber());
				entity.setRequestBy(e.getCreatedBy().getPersonel().getFirstname() + " " + e.getCreatedBy().getPersonel().getLastname());
				entity.setType(e.getWorkOrderType());
				list.add(entity);
			}
		}
		gDAO.destroy();
		
		if(list != null) {
			resp.setSuccess(true);
			resp.setList(list);
			resp.setMessage(list.size() + " record(s) found!");
		} else {
			resp.setSuccess(false);
			resp.setMessage("No record found!");
		}
		
		return resp;
	}
	
	@GET
    @Produces("application/json")
	@Path("pendingExpenseRequests")
	public FMSRestResponse pendingExpenseRequests(@QueryParam("userId") String userId) {
		FMSRestResponse resp = new FMSRestResponse();
		long count = 0;
		try {
			count = new ReportDAO().getPendingExpenseRequestCount(Long.parseLong(userId));
		} catch(Exception ex) {
			ex.printStackTrace();
			resp.setSuccess(false);
			resp.setMessage("Invalid User ID format supplied!");
			saveAudit("MOBILE-PENDING-EXPENSE-REQUESTS: Invalid User ID format supplied!: " + userId, null);
			return resp;
		}
		resp.setSuccess(true);
		resp.setMessage(""+count);
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	@GET
    @Produces("application/json")
	@Path("pendingExpenseRequestsList")
	public FMSRestListResponse pendingExpenseRequestsList(@QueryParam("userId") String userId) {
		FMSRestListResponse resp = new FMSRestListResponse();
		List<Entity> list = null;
		GeneralDAO gDAO = new GeneralDAO();
		Query q = gDAO.createQuery("Select e from ExpenseRequest e where e.approvalStatus='PENDING' and e.approvalUser.id=:userId");
		try {
			q.setParameter("userId", Long.parseLong(userId));
		} catch(Exception ex) {
			ex.printStackTrace();
			resp.setSuccess(false);
			resp.setMessage("Invalid User ID format supplied!");
			saveAudit("MOBILE-PENDING-EXPENSE-REQUESTS-LIST: Invalid User ID format supplied!: " + userId, null);
			
			gDAO.destroy();
			return resp;
		}
		Object obj = gDAO.search(q, 0);
		if(obj != null) {
			list = new ArrayList<Entity>();
			Vector<ExpenseRequest> apList = (Vector<ExpenseRequest>)obj;
			for(ExpenseRequest e : apList) {
				Entity entity = resp.new Entity();
				entity.setDate(sdf.format(e.getRequest_dt()));
				entity.setDescription(e.getRemarks());
				entity.setEntity((e.getVehicle() != null) ? e.getVehicle().getRegistrationNo() : "");
				entity.setRequestBy(e.getCreatedBy().getPersonel().getFirstname() + " " + e.getCreatedBy().getPersonel().getLastname());
				entity.setType(e.getType().getName());
				list.add(entity);
			}
		}
		gDAO.destroy();
		
		if(list != null) {
			resp.setSuccess(true);
			resp.setList(list);
			resp.setMessage(list.size() + " record(s) found!");
		} else {
			resp.setSuccess(false);
			resp.setMessage("No record found!");
		}
		
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	@GET
    @Produces("application/json")
	@Path("pendingTripsRequests")
	public FMSRestResponse pendingTripsRequests(@QueryParam("userId") String userId) {
		FMSRestResponse resp = new FMSRestResponse();
		
		long count = 0;
		GeneralDAO gDAO = new GeneralDAO();
		
		Query q = gDAO.createQuery("Select e from CorporateTrip e where ((e.approveUser.id=:userId and e.approvalStatus='PENDING') or (e.approveUser2.id=:userId and e.approvalStatus2='PENDING' and e.approvalStatus='APPROVED')) and e.departureDateTime > :nowDateTime");
		try {
			q.setParameter("userId", Long.parseLong(userId));
		} catch(Exception ex) {
			ex.printStackTrace();
			resp.setSuccess(false);
			resp.setMessage("Invalid User ID format supplied!");
			saveAudit("MOBILE-PENDING-TRIPS-REQUESTS: Invalid User ID format supplied!: " + userId, null);
			
			gDAO.destroy();
			return resp;
		}
		q.setParameter("nowDateTime", new Date());
		Object tripsObj = gDAO.search(q, 0);
		if(tripsObj != null) {
			Vector<CorporateTrip> trips = (Vector<CorporateTrip>)tripsObj;
			count = trips.size();
		}
		gDAO.destroy();
		
		resp.setSuccess(true);
		resp.setMessage(""+count);
		
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	@GET
    @Produces("application/json")
	@Path("pendingTripsRequestsList")
	public FMSRestListResponse pendingTripsRequestsList(@QueryParam("userId") String userId) {
		FMSRestListResponse resp = new FMSRestListResponse();
		List<Entity> list = null;
		GeneralDAO gDAO = new GeneralDAO();
		Query q = gDAO.createQuery("Select e from CorporateTrip e where ((e.approveUser.id=:userId and e.approvalStatus='PENDING') or (e.approveUser2.id=:userId and e.approvalStatus2='PENDING' and e.approvalStatus='APPROVED')) and e.departureDateTime > :nowDateTime");
		try {
			q.setParameter("userId", Long.parseLong(userId));
		} catch(Exception ex) {
			ex.printStackTrace();
			resp.setSuccess(false);
			resp.setMessage("Invalid User ID format supplied!");
			saveAudit("MOBILE-PENDING-TRIPS-REQUESTS-LIST: Invalid User ID format supplied!: " + userId, null);
			
			gDAO.destroy();
			return resp;
		}
		q.setParameter("nowDateTime", new Date());
		Object tripsObj = gDAO.search(q, 0);
		if(tripsObj != null) {
			list = new ArrayList<Entity>();
			Vector<CorporateTrip> trips = (Vector<CorporateTrip>)tripsObj;
			for(CorporateTrip e : trips) {
				Entity entity = resp.new Entity();
				entity.setDate(sdf.format(e.getCrt_dt()));
				entity.setDescription(e.getPurpose());
				entity.setEntity((e.getVehicle() != null) ? e.getVehicle().getRegistrationNo() : "");
				entity.setRequestBy(e.getCreatedBy().getPersonel().getFirstname() + " " + e.getCreatedBy().getPersonel().getLastname());
				entity.setType("Trip");
				list.add(entity);
			}
		}
		gDAO.destroy();
		
		if(list != null) {
			resp.setSuccess(true);
			resp.setList(list);
			resp.setMessage(list.size() + " record(s) found!");
		} else {
			resp.setSuccess(false);
			resp.setMessage("No record found!");
		}
		return resp;
	}
	
	@GET
    @Produces("application/json")
	@Path("pendingFuelingRequests")
	public FMSRestResponse pendingFuelingRequests(@QueryParam("userId") String userId) {
		FMSRestResponse resp = new FMSRestResponse();
		long count = 0;
		try {
			count = new ReportDAO().getPendingFuelingRequestCount(Long.parseLong(userId));
		} catch(Exception ex) {
			ex.printStackTrace();
			resp.setSuccess(false);
			resp.setMessage("Invalid User ID format supplied!");
			saveAudit("MOBILE-PENDING-FUELING-REQUESTS: Invalid User ID format supplied!: " + userId, null);
			return resp;
		}
		resp.setSuccess(true);
		resp.setMessage(""+count);
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	@GET
    @Produces("application/json")
	@Path("pendingFuelingRequestsList")
	public FMSRestListResponse pendingFuelingRequestsList(@QueryParam("userId") String userId) {
		FMSRestListResponse resp = new FMSRestListResponse();
		List<Entity> list = null;
		GeneralDAO gDAO = new GeneralDAO();
		Query q = gDAO.createQuery("Select e from Approver e where e.entityName='Fueling' and e.approvalUser.id=:userId and e.approvalStatus='PENDING' order by e.crt_dt");
		try {
			q.setParameter("userId", Long.parseLong(userId));
		} catch(Exception ex) {
			ex.printStackTrace();
			resp.setSuccess(false);
			resp.setMessage("Invalid User ID format supplied!");
			saveAudit("MOBILE-PENDING-MAINTENANCE-REQUESTS-LIST: Invalid User ID format supplied!: " + userId, null);
			
			gDAO.destroy();
			return resp;
		}
		Object obj = gDAO.search(q, 0);
		if(obj != null) {
			Vector<Approver> apList = (Vector<Approver>)obj;
			if(apList.size() > 0) {
				list = new ArrayList<Entity>();
				for(Approver ap : apList) {
					Object mobj = gDAO.find(VehicleFuelingRequest.class, ap.getEntityId());
					if(mobj != null) {
						VehicleFuelingRequest e = (VehicleFuelingRequest)mobj;
						Entity entity = resp.new Entity();
						entity.setDate(sdf.format(e.getCrt_dt()));
						entity.setDescription(e.getLocation());
						entity.setEntity(e.getVehicle().getRegistrationNo());
						entity.setRequestBy(e.getCreatedBy().getPersonel().getFirstname() + " " + e.getCreatedBy().getPersonel().getLastname());
						entity.setType("Fueling");
						list.add(entity);
					}
				}
			}
		}
		gDAO.destroy();
		
		if(list != null) {
			resp.setSuccess(true);
			resp.setList(list);
			resp.setMessage(list.size() + " record(s) found!");
		} else {
			resp.setSuccess(false);
			resp.setMessage("No record found!");
		}
		
		return resp;
	}
}
