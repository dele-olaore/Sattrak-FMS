package com.dexter.fms.mbean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.Query;
import javax.servlet.ServletContext;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

import com.dexter.common.util.Emailer;
import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.Notification;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerLicense;
import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.app.DashboardVehicle;
import com.dexter.fms.model.app.DriverLicense;
import com.dexter.fms.model.app.Expense;
import com.dexter.fms.model.app.ExpenseType;
import com.dexter.fms.model.app.Fleet;
import com.dexter.fms.model.app.Item;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleAccident;
import com.dexter.fms.model.app.VehicleAccidentRepair;
import com.dexter.fms.model.app.VehicleAdHocMaintenance;
import com.dexter.fms.model.app.VehicleAdHocMaintenanceRequest;
import com.dexter.fms.model.app.VehicleBehaviour;
import com.dexter.fms.model.app.VehicleDisposal;
import com.dexter.fms.model.app.VehicleDocument;
import com.dexter.fms.model.app.VehicleDriver;
import com.dexter.fms.model.app.VehicleFuelData;
import com.dexter.fms.model.app.VehicleFueling;
import com.dexter.fms.model.app.VehicleFuelingRequest;
import com.dexter.fms.model.app.VehicleLicense;
import com.dexter.fms.model.app.VehicleLocationData;
import com.dexter.fms.model.app.VehicleOdometerData;
import com.dexter.fms.model.app.VehicleParameters;
import com.dexter.fms.model.app.VehicleRoutineMaintenance;
import com.dexter.fms.model.app.VehicleRoutineMaintenanceSetup;
import com.dexter.fms.model.app.VehicleStatusEnum;
import com.dexter.fms.model.app.VehicleTrackerData;
import com.dexter.fms.model.app.WorkOrder;
import com.dexter.fms.model.app.WorkOrderItem;
import com.dexter.fms.model.app.WorkOrderVehicle;
import com.dexter.fms.model.ref.Department;
import com.dexter.fms.model.ref.DocumentType;
import com.dexter.fms.model.ref.FuelType;
import com.dexter.fms.model.ref.LicenseType;
import com.dexter.fms.model.ref.Region;
import com.dexter.fms.model.ref.ServiceType;
import com.dexter.fms.model.ref.TransactionType;
import com.dexter.fms.model.ref.VehicleMake;
import com.dexter.fms.model.ref.VehicleModel;
import com.dexter.fms.model.ref.VehicleStandardRM;
import com.dexter.fms.model.ref.VehicleType;
import com.dexter.fms.model.ref.VehicleWarning;
import com.dexter.fms.model.ref.Vendor;
import com.dexter.fms.model.ref.VendorServices;

@ManagedBean(name = "fleetBean")
@SessionScoped
public class FleetMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-FleetMBean");
	
	private FacesMessage msg = null;
	
	private Long partner_id;
	private Partner partner;
	
	private Long vehicleType_id;
	private Long vehicleMake_id;
	private VehicleType vehicleType;
	private Vector<VehicleType> vehicleTypes;
	private VehicleMake vehicleMake;
	private Vector<VehicleMake> vehicleMakes;
	private VehicleModel vmodel;
	private Vector<VehicleModel> vmodels, allvmodels;
	private StreamedContent vmodelsExcelTemplate;
	private UploadedFile modelsBatchExcel;
	private boolean autoCreate;
	
	private Fleet fleet;
	private Vector<Fleet> fleets;
	
	private Long fleet_id;
	private Long vehicleModel_id;
	private Long vendor_id;
	private Long department_id;
	private Long region_id;
	private Long fuelType_id, assignee_id;
	private Vector<Department> depts;
	private Vector<Region> regions;
	private Vector<FuelType> fuelTypes;
	private Vector<PartnerPersonel> personels;
	
	private StreamedContent vehiclesExcelTemplate;
	private UploadedFile vehiclesBatchExcel;
	private UploadedFile vehiclePhoto;
	private Vector<Vendor> vsalesVendors, vserviceVendors, vrepairVendors, vinsuranceVendors, vlicensesVendors;
	private Vehicle vehicle, selectedVehicle;
	private VehicleParameters vehicleParam;
	private Vector<Vehicle> vehicles, tvehicles, tvehicles2;
	
	// for various documents that comes along with a vehicle
	private Long documentType_id;
	private UploadedFile documentContent;
	private VehicleDocument vehicleDocument;
	private Vector<VehicleDocument> vehicleDocuments, selectedVehicleDocuments;
	
	private Long driver_id;
	private Vector<PartnerDriver> partnerDrivers;
	
	private Long vehicle_id;
	private String regNo;
	private Date stdt, eddt;
	
	private VehicleStandardRM vsrm;
	private Vector<VehicleStandardRM> vsrmList;
	private StreamedContent vmodelsVSRMExcelTemplate;
	private UploadedFile modelsVSRMBatchExcel;
	
	private WorkOrder workOrder, selectedWorkOrder, selectedPendingRoutineWorkOrder, selectedInprogressRoutineWorkOrder, selectedPendingAdhocWorkOrder, selectedInprogressAdhocWorkOrder;
	private Vector<WorkOrder> rountineWorkOrders, pendingRoutineWorkOrders, inprogressRoutineWorkOrders, adhocWorkOrders, pendingAdhocWorkOrders, inprogressAdhocWorkOrders;
	private String vehSummaryDetailsOfWorkOrder, item_action;
	private double initVehEstAmount, itmInitEstAmount;
	private BigDecimal initVehOdometer;
	private long item_id, workOrder_id, inprgWorkOrder_id;
	private int itmCount;
	private Vector<Item> items;
	private UploadedFile workOrderVendorDoc;
	
	private boolean nextRMSetup;
	private VehicleRoutineMaintenanceSetup routineSetup;
	private VehicleRoutineMaintenance routine;
	private Vector<VehicleRoutineMaintenance> routines;
	
	private long approved_adhoc_req_id;
	private VehicleAdHocMaintenanceRequest adhocRequest;
	private Vector<VehicleAdHocMaintenanceRequest> adHocRequests;
	private VehicleAdHocMaintenance adhocMain;
	private Vector<VehicleAdHocMaintenance> adhocMains;
	
	private Long insuranceComp_id, repairComp_id;
	private UploadedFile accidentPhoto, repairedPhoto, accidentDocument, accidentDocument2, accidentDocument3, repairAttachment;
	private boolean accidentStatus;
	private VehicleAccident accident, selectedAccident;
	private Vector<VehicleAccident> accidents, pendingAccidents, reviewedAccidents, deniedAccidents, approvedAccidents;
	private VehicleAccidentRepair repair;
	
	private MapModel vtrackingModel;
	private Marker marker;
	private Vector<VehicleLocationData> vehiclesLocs;
	private String defaultCenterCoor = "6.427887,3.4287645";
	private String centerCoor;
	private int vtrackpollinterval = 120;
	private VehicleTrackerData markerTrackerData = null;
	
	private StreamedContent fuelingExcelTemplate;
	private UploadedFile fuelingBatchExcel;
	private VehicleFueling fueling;
	private Vector<VehicleFueling> fuelings;
	
	private Long approvalUser_id;
	private VehicleFuelingRequest fuelingRequest;
	private Vector<VehicleFuelingRequest> myFuelingRequests, mySubFuelingRequest, pendingFuelingRequests;
	private String approvalStatus; // PENDING, APPROVED, DENIED
	private String approvalComment; // comment for approval status
	
	private Long licenseType_id;
	private Long transactionType_id;
	
	private LicenseType licType;
	private Vector<LicenseType> licTypes;
	
	private DocumentType docType;
	private Vector<DocumentType> docTypes;
	
	private UploadedFile licenseDocument;
	private VehicleLicense license;
	private Vector<VehicleLicense> licenses;
	
	private UploadedFile driverLicenseDocument;
	private DriverLicense driverLicense;
	private Vector<DriverLicense> driverLicenses;
	
	private Vector<VehicleDriver> vehicleDrivers;
	
	private Long warning_id;
	private VehicleWarning warnType;
	private Vector<VehicleWarning> warnTypes;
	
	private VehicleBehaviour vehicleBehaviour;
	private Vector<VehicleBehaviour> vehicleBehaviours;
	
	private VehicleDisposal vdisposal;
	private Vector<VehicleDisposal> myDisposalRequests, mySubDisposalRequest, pendingDisposalRequests;
	
	private boolean finished;
	private String workOrderStatus;
	private Date start_dt, end_dt;
	
	private boolean selectAll;
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public FleetMBean()
	{
		vtrackpollinterval = 120;
        defaultCenterCoor = "6.427887,3.4287645";
        centerCoor = defaultCenterCoor;
        
        InputStream stream = ((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/resources/templates/fms_batchload_vehiclemodels.xls");  
        vmodelsExcelTemplate = new DefaultStreamedContent(stream, "application/vnd.ms-excel", "fms_batchload_vehiclemodels.xls");
        
        InputStream stream2 = ((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/resources/templates/fms_batchload_vehicles.xls");  
        vehiclesExcelTemplate = new DefaultStreamedContent(stream2, "application/vnd.ms-excel", "fms_batchload_vehicles.xls");
        
        InputStream stream3 = ((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/resources/templates/fms_batchload_fueling.xls");  
        fuelingExcelTemplate = new DefaultStreamedContent(stream3, "application/vnd.ms-excel", "fms_batchload_fueling.xls");
        
        InputStream stream4 = ((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/resources/templates/fms_batchload_vsrm.xls");  
        vmodelsVSRMExcelTemplate = new DefaultStreamedContent(stream4, "application/vnd.ms-excel", "fms_batchload_vsrm.xls");
	}
	
	public void removeItmFromVeh()
	{
		if(getWorkOrder().getSelectedVehicle().getSelectedItem() != null)
		{
			int size = getWorkOrder().getSelectedVehicle().getItems().size();
			for(int i=0; i<size; i++)
			{
				WorkOrderItem e = getWorkOrder().getSelectedVehicle().getItems().get(i);
				if(e.getItem().getId().longValue() == getWorkOrder().getSelectedVehicle().getSelectedItem().getItem().getId().longValue())
				{
					getWorkOrder().getSelectedVehicle().getItems().remove(i);
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Item removed successfully!");
					break;
				}
			}
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void addItmToVeh()
	{
		GeneralDAO gDAO = new GeneralDAO();
		
		if(getItem_id() > 0L && getItem_action() != null && getItmCount() > 0)
		{
			boolean exist = false;
			int size = getWorkOrder().getSelectedVehicle().getItems().size();
			for(int i=0; i<size; i++)
			{
				WorkOrderItem e = getWorkOrder().getSelectedVehicle().getItems().get(i);
				if(e.getItem().getId().longValue() == getItem_id())
				{
					exist = true;
					break;
				}
			}
			
			if(!exist)
			{
				WorkOrderItem wrkItm = new WorkOrderItem();
				Object iobj = gDAO.find(Item.class, getItem_id());
				if(iobj != null)
				{
					wrkItm.setItem((Item)iobj);
					wrkItm.setAction(getItem_action());
					wrkItm.setCount(getItmCount());
					wrkItm.setCreatedBy(dashBean.getUser());
					wrkItm.setCrt_dt(new Date());
					wrkItm.setInitEstAmount(getItmInitEstAmount());
					
					getWorkOrder().getSelectedVehicle().getItems().add(wrkItm);
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Item added successfully!");
				}
				else
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Item not found!");
			}
			else
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Item exists in the list already!");
		}
		else
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Please select an item and enter the action required and count!");
		
		FacesContext.getCurrentInstance().addMessage(null, msg);
		
		gDAO.destroy();
	}
	
	public void removeVehFromWorkOrd()
	{
		if(getWorkOrder().getSelectedVehicle() != null)
		{
			int size = getWorkOrder().getVehicles().size();
			for(int i=0; i<size; i++)
			{
				WorkOrderVehicle e = getWorkOrder().getVehicles().get(i);
				if(e.getVehicle().getId().longValue() == getWorkOrder().getSelectedVehicle().getVehicle().getId().longValue())
				{
					getWorkOrder().getVehicles().remove(i);
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Vehicle removed successfully!");
					break;
				}
			}
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void addVehToWorkOrd()
	{
		GeneralDAO gDAO = new GeneralDAO();
		
		if(getVehicle_id() != null && getVehicle_id().longValue() > 0L && getVehSummaryDetailsOfWorkOrder() != null && 
				getVehSummaryDetailsOfWorkOrder().trim().length() > 0 && getInitVehOdometer() != null)
		{
			boolean exist = false;
			int size = getWorkOrder().getVehicles().size();
			for(int i=0; i<size; i++)
			{
				WorkOrderVehicle e = getWorkOrder().getVehicles().get(i);
				if(e.getVehicle().getId().longValue() == getVehicle_id().longValue())
				{
					exist = true;
					break;
				}
			}
			
			if(!exist)
			{
				WorkOrderVehicle vWorkOrd = new WorkOrderVehicle();
				Object vobj = gDAO.find(Vehicle.class, getVehicle_id());
				if(vobj != null)
				{
					vWorkOrd.setVehicle((Vehicle)vobj);
					vWorkOrd.setCreatedBy(dashBean.getUser());
					vWorkOrd.setCrt_dt(new Date());
					vWorkOrd.setDetailsOfWork(getVehSummaryDetailsOfWorkOrder());
					vWorkOrd.setInitEstAmount(getInitVehEstAmount());
					vWorkOrd.setCurrentVehOdometer(getInitVehOdometer());
					
					getWorkOrder().getVehicles().add(vWorkOrd);
					
					setVehSummaryDetailsOfWorkOrder(null);
					setInitVehOdometer(null);
					setInitVehEstAmount(0);
					
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Vehicle added successfully!");
				}
				else
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Vehicle not found!");
			}
			else
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Vehicle exists in the list already!");
		}
		else
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Please select a vehicle and enter the work description!");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		
		gDAO.destroy();
	}
	
	private void addHeaderCell(XWPFTableRow row, String text, boolean first)
	{
		XWPFParagraph tp = null;
		if(first)
			tp = row.getCell(0).getParagraphs().get(0);
		else
		{
			tp = row.addNewTableCell().getParagraphs().get(0);
		}
		XWPFRun tpr = tp.createRun();
		tpr.setBold(true);
		tpr.setItalic(true);
		tpr.setText(text);
	}
	private void writeFileToResponse(ExternalContext externalContext, ByteArrayOutputStream baos, String fileName, String mimeType)
	{
		try
		{
			externalContext.responseReset();
			externalContext.setResponseContentType(mimeType);
			externalContext.setResponseHeader("Expires", "0");
			externalContext.setResponseHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
			externalContext.setResponseHeader("Pragma", "public");
			externalContext.setResponseHeader("Content-disposition", "attachment;filename=" + fileName);
			externalContext.setResponseContentLength(baos.size());
			OutputStream out = externalContext.getResponseOutputStream();
			baos.writeTo(out);
			externalContext.responseFlushBuffer();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void generateWorkOrderWordDoc(long id)
	{
		try
		{
			setSelectedWorkOrder(null);
			for(WorkOrder w : getRountineWorkOrders())
			{
				if(w.getId().longValue() == id)
				{
					setSelectedWorkOrder(w);
					break;
				}
			}
			
			if(getSelectedWorkOrder() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				FacesContext context = FacesContext.getCurrentInstance();
				XWPFDocument document = new XWPFDocument();
				
				XWPFParagraph paragraphOne = document.createParagraph();
		        paragraphOne.setAlignment(ParagraphAlignment.LEFT);
		        /*paragraphOne.setBorderBottom(Borders.SINGLE);
		        paragraphOne.setBorderTop(Borders.SINGLE);
		        paragraphOne.setBorderRight(Borders.SINGLE);
		        paragraphOne.setBorderLeft(Borders.SINGLE);
		        paragraphOne.setBorderBetween(Borders.SINGLE);
		        */
		        XWPFRun paragraphOneRunOne = paragraphOne.createRun();
		        paragraphOneRunOne.setBold(true);
		        paragraphOneRunOne.setItalic(true);
		        paragraphOneRunOne.setText("Work Order No. - " + getSelectedWorkOrder().getWorkOrderNumber());
		        paragraphOneRunOne.addBreak();
		        
		        paragraphOneRunOne = paragraphOne.createRun();
		        paragraphOneRunOne.setBold(true);
		        paragraphOneRunOne.setItalic(true);
		        paragraphOneRunOne.setText("Type - " + getSelectedWorkOrder().getWorkOrderType());
		        paragraphOneRunOne.addBreak();
		        
		        paragraphOneRunOne = paragraphOne.createRun();
		        paragraphOneRunOne.setBold(true);
		        paragraphOneRunOne.setItalic(true);
		        paragraphOneRunOne.setText("Prepared by - " + getSelectedWorkOrder().getCreatedBy().getPersonel().getFirstname() + " " + getSelectedWorkOrder().getCreatedBy().getPersonel().getLastname());
		        paragraphOneRunOne.addBreak();
		        
		        paragraphOneRunOne = paragraphOne.createRun();
		        paragraphOneRunOne.setBold(true);
		        paragraphOneRunOne.setItalic(true);
		        paragraphOneRunOne.setText("Prepared on - " + getSelectedWorkOrder().getCrt_dt());
		        paragraphOneRunOne.addBreak();
		        
		        paragraphOneRunOne = paragraphOne.createRun();
		        paragraphOneRunOne.setBold(true);
		        paragraphOneRunOne.setItalic(true);
		        paragraphOneRunOne.setText("Description - " + getSelectedWorkOrder().getSummaryDetailsOfWorkOrder());
		        paragraphOneRunOne.addBreak();
		        
		        paragraphOneRunOne = paragraphOne.createRun();
		        paragraphOneRunOne.setBold(true);
		        paragraphOneRunOne.setItalic(true);
		        paragraphOneRunOne.setText("Status - " + getSelectedWorkOrder().getStatus());
		        paragraphOneRunOne.addBreak();
		        
		        if((getSelectedWorkOrder().getStatus().equalsIgnoreCase("IN-PROGRESS") || getSelectedWorkOrder().getStatus().equalsIgnoreCase("COMPLETED")))
		        {
		        	paragraphOneRunOne = paragraphOne.createRun();
			        paragraphOneRunOne.setBold(true);
			        paragraphOneRunOne.setItalic(true);
			        if(getSelectedWorkOrder().getVendor()!=null)
			        	paragraphOneRunOne.setText("Vendor - " + getSelectedWorkOrder().getVendor().getName());
			        else
			        	paragraphOneRunOne.setText("Vendor - N/A");
			        paragraphOneRunOne.addBreak();
		        }
		        
		        XWPFParagraph paragraph2 = document.createParagraph();
		        paragraph2.setAlignment(ParagraphAlignment.CENTER);
		        XWPFRun paragraph2Run = paragraph2.createRun();
		        paragraph2Run.setBold(true);
		        paragraph2Run.setItalic(true);
		        paragraph2Run.setUnderline(UnderlinePatterns.DOUBLE);
		        paragraph2Run.setText("Vehicles");
		        paragraph2Run.addBreak();
		        
		        double totalCost = 0;
		        for(WorkOrderVehicle v : getSelectedWorkOrder().getVehicles())
		        {
		        	VehicleRoutineMaintenance vrm = null;
		        	if(getSelectedWorkOrder().getStatus().equalsIgnoreCase("IN-PROGRESS") || getSelectedWorkOrder().getStatus().equalsIgnoreCase("COMPLETED"))
			        {
			        	Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("vehicle", v.getVehicle());
						params.put("workOrder", getSelectedWorkOrder());
						
						Object vrmObj = gDAO.search("VehicleRoutineMaintenance", params);
						if(vrmObj != null)
						{
							Vector<VehicleRoutineMaintenance> list = (Vector<VehicleRoutineMaintenance>)vrmObj;
							vrm = list.get(0);
						}
			        }
		        	
		        	XWPFParagraph paragraph = document.createParagraph();
		        	paragraph.setAlignment(ParagraphAlignment.LEFT);
			        XWPFRun paragraphRun = paragraph.createRun();
			        paragraphRun.setBold(true);
			        paragraphRun.setItalic(true);
			        paragraphRun.setText("Registration Number: " + v.getVehicle().getRegistrationNo());
			        paragraphRun.addBreak();
			        
			        paragraphRun = paragraph.createRun();
			        paragraphRun.setBold(true);
			        paragraphRun.setItalic(true);
			        paragraphRun.setText("Model: " + v.getVehicle().getModel().getName() + "[" + v.getVehicle().getModel().getYear() + "]");
			        paragraphRun.addBreak();
			        
			        paragraphRun = paragraph.createRun();
			        paragraphRun.setBold(true);
			        paragraphRun.setItalic(true);
			        paragraphRun.setText("Work Required: " + v.getDetailsOfWork());
			        paragraphRun.addBreak();
			        
			        if(getSelectedWorkOrder().getStatus().equalsIgnoreCase("IN-PROGRESS") && vrm != null)
			        {
			        	paragraphRun = paragraph.createRun();
				        paragraphRun.setBold(true);
				        paragraphRun.setItalic(true);
				        paragraphRun.setText("Start Date: " + vrm.getStart_dt());
				        paragraphRun.addBreak();
			        }
			        else if(getSelectedWorkOrder().getStatus().equalsIgnoreCase("COMPLETED") && vrm != null)
			        {
			        	paragraphRun = paragraph.createRun();
				        paragraphRun.setBold(true);
				        paragraphRun.setItalic(true);
				        paragraphRun.setText("Start Date: " + vrm.getStart_dt());
				        paragraphRun.addBreak();
				        
				        paragraphRun = paragraph.createRun();
				        paragraphRun.setBold(true);
				        paragraphRun.setItalic(true);
				        paragraphRun.setText("Close Date: " + vrm.getClose_dt());
				        paragraphRun.addBreak();
			        }
			        
			        paragraphRun = paragraph.createRun();
			        paragraphRun.setBold(true);
			        paragraphRun.setItalic(true);
			        if(getSelectedWorkOrder().getStatus().equalsIgnoreCase("NEW"))
			        	paragraphRun.setText("Vendor Cost: ");
			        else if(getSelectedWorkOrder().getStatus().equalsIgnoreCase("IN-PROGRESS"))
			        {
			        	if(vrm != null)
			        	{
			        		paragraphRun.setText("Vendor Cost: " + NumberFormat.getInstance().format(vrm.getInitial_amount().doubleValue()));
			        		totalCost += vrm.getInitial_amount().doubleValue();
			        	}
						else
							paragraphRun.setText("Vendor Cost: ");
			        }
			        else if(getSelectedWorkOrder().getStatus().equalsIgnoreCase("COMPLETED"))
			        {
			        	if(vrm != null)
			        	{
			        		paragraphRun.setText("Vendor Cost: " + NumberFormat.getInstance().format(vrm.getClosed_amount().doubleValue()));
			        		totalCost += vrm.getClosed_amount().doubleValue();
			        	}
						else
							paragraphRun.setText("Vendor Cost: ");
			        }
			        paragraphRun.addBreak();
			        
			        XWPFTable table = paragraph.getDocument().createTable();
			        XWPFTableRow tableRowOne = table.getRow(0);
					addHeaderCell(tableRowOne, "Item", true);
					addHeaderCell(tableRowOne, "Type", false);
					addHeaderCell(tableRowOne, "Action", false);
					addHeaderCell(tableRowOne, "Count", false);
					
					if(getSelectedWorkOrder().getStatus().equalsIgnoreCase("NEW"))
					{
						addHeaderCell(tableRowOne, "Vendor Cost", false);
						addHeaderCell(tableRowOne, "Comment", false);
					}
					
					for(WorkOrderItem itm : v.getItems())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(itm.getItem().getName());
						tableRow.getCell(1).setText(itm.getItem().getType().getName());
						tableRow.getCell(2).setText(itm.getAction());
						tableRow.getCell(3).setText(String.valueOf(itm.getCount()));
						if(getSelectedWorkOrder().getStatus().equalsIgnoreCase("NEW"))
						{
							tableRow.getCell(4).setText("");
							tableRow.getCell(5).setText("");
						}
					}
		        }
		        gDAO.destroy();
		        
		        if(getSelectedWorkOrder().getStatus().equalsIgnoreCase("IN-PROGRESS") || getSelectedWorkOrder().getStatus().equalsIgnoreCase("COMPLETED"))
		        {
			        XWPFParagraph paragraph = document.createParagraph();
		        	paragraph.setAlignment(ParagraphAlignment.LEFT);
			        XWPFRun paragraphRun = paragraph.createRun();
			        paragraphRun.setBold(true);
			        paragraphRun.setItalic(true);
			        paragraphRun.setText("Total Cost: " + NumberFormat.getInstance().format(totalCost));
		        }
		        
		        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        document.write(baos);
		        
		        String fileName = getSelectedWorkOrder().getWorkOrderNumber() + ".docx";
		        
		        writeFileToResponse(context.getExternalContext(), baos, fileName, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
				
				context.responseComplete();
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No work order selected!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", ex.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void downloadWorkOrderVendorDoc(long id)
	{
		try
		{
			setSelectedWorkOrder(null);
			for(WorkOrder w : getRountineWorkOrders())
			{
				if(w.getId().longValue() == id)
				{
					setSelectedWorkOrder(w);
					break;
				}
			}
			
			if(getSelectedWorkOrder() != null && getSelectedWorkOrder().getVendorDocument() != null)
			{
				FacesContext context = FacesContext.getCurrentInstance();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        baos.write(getSelectedWorkOrder().getVendorDocument());
		        
		        String fileName = getSelectedWorkOrder().getWorkOrderNumber() + ".pdf";
		        
		        writeFileToResponse(context.getExternalContext(), baos, fileName, "application/pdf");
				
				context.responseComplete();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", ex.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void createWorkOrd(String type)
	{
		GeneralDAO gDAO = new GeneralDAO();
		String workOrderNumber = type + "-";
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("partner", getPartner());
		params.put("workOrderType", type);
		Object objs = gDAO.search("WorkOrder", params);
		if(objs != null)
		{
			Vector<WorkOrder> ords = (Vector<WorkOrder>)objs;
			int size = ords.size();
			String seq = String.valueOf(size);
			for(int i=0; i<5; i++)
			{
				if(seq.length() == 5)
					break;
				else
					seq = "0"+seq;
			}
			workOrderNumber = workOrderNumber + seq;
		}
		
		if(type.equalsIgnoreCase("Adhoc") && getApproved_adhoc_req_id() > 0L)
		{
			VehicleAdHocMaintenanceRequest adhocMainRequest = null;
			
			Object obj = gDAO.find(VehicleAdHocMaintenanceRequest.class, getApproved_adhoc_req_id());
			if(obj != null)
				adhocMainRequest = (VehicleAdHocMaintenanceRequest)obj;
			
			getWorkOrder().setAdhocMainRequest(adhocMainRequest);
		}
		
		getWorkOrder().setCreatedBy(dashBean.getUser());
		getWorkOrder().setCrt_dt(new Date());
		getWorkOrder().setPartner(getPartner());
		getWorkOrder().setWorkOrderType(type);
		getWorkOrder().setFinished(false);
		getWorkOrder().setStatus("NEW");
		
		Vector<WorkOrderVehicle> wrkVehs = getWorkOrder().getVehicles();
		
		getWorkOrder().setWorkOrderNumber(workOrderNumber);
		
		gDAO.startTransaction();
		if(gDAO.save(getWorkOrder()))
		{
			for(WorkOrderVehicle v : wrkVehs)
			{
				Vector<WorkOrderItem> wrkItms = v.getItems();
				v.setWorkOrder(getWorkOrder());
				if(gDAO.save(v))
				{
					for(WorkOrderItem itm : wrkItms)
					{
						itm.setWorkOrderVehicle(v);
						gDAO.save(itm);
					}
				}
			}
		}
		
		try
		{
			gDAO.commit();
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Work order '" + workOrderNumber + "' created successfully!");
			setWorkOrder(null);
		}
		catch(Exception ex)
		{
			gDAO.rollback();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during save: " + gDAO.getMessage());
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
		
		gDAO.destroy();
	}
	
	public void attendToDisposalRequest()
	{
		if(getPendingDisposalRequests() != null && getPendingDisposalRequests().size() > 0)
		{
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = false;
			for(VehicleDisposal expR : getPendingDisposalRequests())
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
							expR.getVehicle().setActive(false);
							expR.getVehicle().setActiveStatus(VehicleStatusEnum.SOLD_DISPOSED.getStatus());
							
							ret = gDAO.update(expR.getVehicle());
							if(!ret)
								break;
						}
					}
				}
			}
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Vehicle disposal request(s) attended to successfully!");
				
				setPendingDisposalRequests(null);
				setApprovalComment(null);
				setApprovalStatus(null);
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
	public void saveBatchDisRequest()
	{
		if(getMyDisposalRequests() != null && getMyDisposalRequests().size() > 0)
		{
			ArrayList<String> emails = new ArrayList<String>(), usernames = new ArrayList<String>();
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = false;
			for(VehicleDisposal expR : getMyDisposalRequests())
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
							n.setMessage("You have pending disposal request from '" + dashBean.getUser().getPersonel().getFirstname() + " " + dashBean.getUser().getPersonel().getLastname() + "'. Submitted: " + new Date().toLocaleString());
							n.setNotified(false);
							n.setSubject("Disposal Request");
							n.setPage_url("approve_vdisposal");
							
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
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Vehicle disposal request(s) save successfully!");
				
				if(emails.size() > 0)
				{
					final String[] to = new String[emails.size()];
					for(int i=0; i<to.length; i++)
						to[i] = emails.get(i);
					final String subject = "Vehicle Disposal Request - Batch";
					StringBuilder sb = new StringBuilder();
					sb.append("<html><body>");
					sb.append("<p><strong>Hello All</strong><br/></p>");
					sb.append("<p>Please be informed that User: '" + dashBean.getUser().getPersonel().getFirstname() + " " + dashBean.getUser().getPersonel().getLastname() + "' just submitted a batch disposal request to " + to.length + " person(s), which you are one of, for approval.<br/></p>");
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
				
				setMyDisposalRequests(null);
				setPendingDisposalRequests(null);
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
	
	public void addDisRequestToBatch()
	{
		if(getVehicle_id() != null && getVdisposal().getNegoVal() > 0 && getVdisposal().getNetbookVal() > 0 &&
				getVdisposal().getSoldTo() != null && getVdisposal().getSoldTo().trim().length() > 0 && 
				getApprovalUser_id() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object vObj = gDAO.find(Vehicle.class, getVehicle_id());
			if(vObj != null)
			{
				getVdisposal().setVehicle((Vehicle)vObj);
				
				Object uObj = gDAO.find(PartnerUser.class, getApprovalUser_id());
				if(uObj != null)
				{
					PartnerUser approveUser = (PartnerUser)uObj;
					getVdisposal().setApprovalUser(approveUser);
					
					getVdisposal().setApprovalStatus("PENDING");
					getVdisposal().setCreatedBy(dashBean.getUser());
					getVdisposal().setCrt_dt(new Date());
					getVdisposal().setRequest_dt(new Date());
					
					if(getMyDisposalRequests() == null)
						setMyDisposalRequests(new Vector<VehicleDisposal>());
					getMyDisposalRequests().add(getVdisposal());
					
					setVdisposal(null);
					
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Request added to batch successfully.");
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid approval user selected!");
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid vehicle selected!");
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Also ensure you selected a vehicle!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	public void removeDisRequestFromBatch(int pos)
	{
		if(getMyDisposalRequests() != null)
		{
			if(pos < 0 || pos >= getMyDisposalRequests().size())
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid position for request to remove!");
			}
			else
			{
				getMyDisposalRequests().remove(pos);
				getMyDisposalRequests().trimToSize();
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
	public void saveVehicleDisposeRequest()
	{
		if(getSelectedVehicle() != null && getSelectedVehicle().getId() != null)
		{
			if(getVdisposal().getNegoVal() > 0 && getVdisposal().getNetbookVal() > 0 &&
					getVdisposal().getSoldTo() != null && getVdisposal().getSoldTo().trim().length() > 0)
			{
				getVdisposal().setCreatedBy(dashBean.getUser());
				getVdisposal().setCrt_dt(new Date());
				getVdisposal().setRequest_dt(new Date());
				getVdisposal().setVehicle(getSelectedVehicle());
				getVdisposal().setApprovalStatus("PENDING");
				
				GeneralDAO gDAO = new GeneralDAO();
				
				Object uObj = gDAO.find(PartnerUser.class, getApprovalUser_id());
				if(uObj != null)
				{
					PartnerUser approveUser = (PartnerUser)uObj;
					getVdisposal().setApprovalUser(approveUser);
				}
				
				gDAO.startTransaction();
				boolean ret = gDAO.save(getVdisposal());
				if(ret)
				{
					String email = null;
					try
					{
						email = getVdisposal().getApprovalUser().getPersonel().getEmail();
					} catch(Exception ex){}
					try
					{
						if(getVdisposal().getApprovalUser().getUsername() != null)
						{
							// Send notification and email to the approval user
							Notification n = new Notification();
							n.setCrt_dt(new Date());
							n.setUser(getVdisposal().getApprovalUser());
							n.setMessage("You have pending disposal request from '" + dashBean.getUser().getPersonel().getFirstname() + " " + dashBean.getUser().getPersonel().getLastname() + "'. Submitted: " + new Date().toLocaleString());
							n.setNotified(false);
							n.setSubject("Disposal Request");
							n.setPage_url("approve_vdisposal");
							
							ret = gDAO.save(n);
						}
					} catch(Exception ex){}
					if(email != null)
					{
						final String[] to = new String[]{email};
						final String subject = "Vehicle Disposal Request - Single";
						StringBuilder sb = new StringBuilder();
						sb.append("<html><body>");
						sb.append("<p><strong>Hello</strong><br/></p>");
						sb.append("<p>Please be informed that User: '" + dashBean.getUser().getPersonel().getFirstname() + " " + dashBean.getUser().getPersonel().getLastname() + "' just submitted a single disposal request to you for approval.<br/></p>");
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
					
					gDAO.commit();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Disposal request submitted successfully!");
					
					setVdisposal(null);
					setSelectedVehicle(null);
					
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", gDAO.getMessage());
				}
			}
			else
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
		}
		else
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No vehicle selected!");
		
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void commitDocumentBatch()
	{
		if(getVehicleDocuments() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			gDAO.startTransaction();
			boolean ret = false;
			for(VehicleDocument vd : getVehicleDocuments())
			{
				ret = gDAO.save(vd);
				if(!ret)
					break;
			}
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Documents saved successfully.");
				
				setVehicleDocuments(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Save failed. Message: " + gDAO.getMessage());
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Nothing to commit!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void saveVehicleDocument()
	{
		if(getDocumentType_id() != null && getDocumentContent() != null && getSelectedVehicle() != null)
		{
			if(getDocumentContent().getContentType() != null && getDocumentContent().getContentType().toLowerCase().indexOf("pdf") >= 0)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Object dtObj = gDAO.find(DocumentType.class, getDocumentType_id());
				if(dtObj != null)
				{
					DocumentType dt = (DocumentType)dtObj;
					getVehicleDocument().setDocumentType(dt);
					getVehicleDocument().setCreatedBy(dashBean.getUser());
					getVehicleDocument().setCrt_dt(new Date());
					getVehicleDocument().setDocumentData(getDocumentContent().getContents());
					getVehicleDocument().setVehicle(getSelectedVehicle());
					
					gDAO.startTransaction();
					boolean ret = gDAO.save(getVehicleDocument());
					if(ret)
					{
						gDAO.commit();
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Document saved successfully.");
						
						setVehicleDocument(null);
						setSelectedVehicleDocuments(null);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Save failed. Message: " + gDAO.getMessage());
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid document type selected!");
				}
				gDAO.destroy();
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid document file uploaded!");
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void addVehicleDocument()
	{
		if(getDocumentType_id() != null && getDocumentContent() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object dtObj = gDAO.find(DocumentType.class, getDocumentType_id());
			if(dtObj != null)
			{
				DocumentType dt = (DocumentType)dtObj;
				getVehicleDocument().setDocumentType(dt);
				getVehicleDocument().setCreatedBy(dashBean.getUser());
				getVehicleDocument().setCrt_dt(new Date());
				getVehicleDocument().setDocumentData(getDocumentContent().getContents());
				getVehicleDocument().setVehicle(getSelectedVehicle());
				
				if(getVehicleDocuments() == null)
					setVehicleDocuments(new Vector<VehicleDocument>());
				
				getVehicleDocuments().add(getVehicleDocument());
				setVehicleDocument(null);
				setDocumentContent(null);
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Document added to batch successfully!");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid document type selected!");
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void saveWarning()
	{
		if(getVehicle_id() != null && getWarning_id() != null && getVehicleBehaviour().getEventDate() != null && getDriver_id() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Object vObj = gDAO.find(Vehicle.class, getVehicle_id());
			if(vObj != null)
			{
				Vehicle v = (Vehicle)vObj;
				getVehicleBehaviour().setVehicle(v);
				
				Object wObj = gDAO.find(VehicleWarning.class, getWarning_id());
				if(wObj != null)
				{
					VehicleWarning vw = (VehicleWarning)wObj;
					getVehicleBehaviour().setWarning(vw);
					
					Object pdObj = gDAO.find(PartnerDriver.class, getDriver_id());
					if(pdObj != null)
					{
						PartnerDriver pd = (PartnerDriver)pdObj;
						getVehicleBehaviour().setDriver(pd);
					}
					
					/*Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("vehicle", v);
					Object vDrvs = gDAO.search("VehicleDriver", params);
					if(vDrvs != null)
					{
						Vector<VehicleDriver> vdList = (Vector<VehicleDriver>)vDrvs;
						for(VehicleDriver vd : vdList)
						{
							if(vd.isActive())
							{
								getVehicleBehaviour().setDriver(vd.getDriver());
								break;
							}
						}
					}*/
					
					gDAO.startTransaction();
					boolean ret = gDAO.save(getVehicleBehaviour());
					if(ret)
					{
						gDAO.commit();
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Warning saved successfully.");
						
						setVehicleBehaviour(null);
						setVehicleBehaviours(null);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Save failed. Message: " + gDAO.getMessage());
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid warning selected!");
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid vehicle selected!");
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void searchVehicleDrivers()
	{
		if(getStdt() != null && getEddt() != null && getVehicle_id() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Query q = gDAO.createQuery("Select e from VehicleDriver e where e.vehicle.id=:v_id and e.active=:active");
			q.setParameter("v_id", getVehicle_id());
			q.setParameter("active", true);
			
			
			q = gDAO.createQuery("Select e from VehicleDriver e where e.vehicle.id=:v_id and e.active=:active and (e.end_dt between :stdt and :eddt)");
			
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
	
	public void saveWarnType()
	{
		if(getWarnType().getName() != null)
		{
			getWarnType().setCrt_dt(new Date());
			getWarnType().setCreatedBy(dashBean.getUser());
			
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = false;
			if(getWarnType().getId() != null)
				ret = gDAO.update(getWarnType());
			else
				ret = gDAO.save(getWarnType());
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Driving warning type saved successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setWarnType(null);
				setWarnTypes(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Error occured during save: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Driving warning type name is required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void saveDocType()
	{
		if(getDocType().getName() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = false;
			if(getDocType().getId() != null)
				ret = gDAO.update(getDocType());
			else
				ret = gDAO.save(getDocType());
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Document type saved successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setDocType(null);
				setDocTypes(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Error occured during save: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Document type name is required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void saveLicType()
	{
		if(getLicType().getName() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = false;
			if(getLicType().getId() != null)
				ret = gDAO.update(getLicType());
			else
				ret = gDAO.save(getLicType());
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "License type saved successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setLicType(null);
				setLicTypes(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Error occured during save: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "License type name is required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void saveDrvLicense()
	{
		if(getDriver_id() != null && getDriverLicense().getTran_dt() != null && getDriverLicense().getLic_start_dt() != null &&
				getTransactionType_id() != null && getTransactionType_id() > 0)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object drvObj = gDAO.find(PartnerDriver.class, getDriver_id());
			if(drvObj != null)
			{
				getDriverLicense().setDriver((PartnerDriver)drvObj);
				
				Object ttObj = gDAO.find(TransactionType.class, getTransactionType_id());
				if(ttObj != null)
					getDriverLicense().setTranType((TransactionType)ttObj);
				
				Object vendorObj = gDAO.find(Vendor.class, getVendor_id());
				if(vendorObj != null)
					getDriverLicense().setVendor((Vendor)vendorObj);
				
				if(getDriverLicenseDocument() != null)
					getDriverLicense().setDocument(getDriverLicenseDocument().getContents());
				
				Calendar c = Calendar.getInstance();
				c.setTime(getDriverLicense().getLic_start_dt());
				c.add(Calendar.YEAR, 1);
				getDriverLicense().setLic_end_dt(c.getTime());
				
				// Below commented to allow the ApplicationMBean to activate it
				if(getDriverLicense().getLic_end_dt().after(new Date()))
				{
					getDriverLicense().setActive(true);
					getDriverLicense().setExpired(false);
				}
				else
				{
					getDriverLicense().setActive(false);
					getDriverLicense().setExpired(true);
				}
				
				gDAO.startTransaction();
				boolean ret = false;
				if(getDriverLicense().getId() != null)
				{
					getDriverLicense().setCreatedBy(dashBean.getUser());
					ret = gDAO.update(getDriverLicense());
				}
				else
				{
					getDriverLicense().setCrt_dt(new Date());
					getDriverLicense().setCreatedBy(dashBean.getUser());
					ret = gDAO.save(getDriverLicense());
				}
				
				if(ret)
				{
					gDAO.commit();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Drivers' License captured/updated successfully!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					
					setDriverLicense(null);
					setDriverLicenses(null);
					setDriver_id(null);
					setTransactionType_id(null);
					setDriverLicenseDocument(null);
					setVendor_id(null);
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Error occured during save: " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				gDAO.destroy();
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid driver selected!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Also ensure you selected a driver!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void saveLicense()
	{
		if(getVehicle_id() != null && getLicense().getTran_dt() != null && getLicense().getLic_start_dt() != null &&
				getLicenseType_id() != null && getLicenseType_id() > 0 && getTransactionType_id() != null && 
				getTransactionType_id() > 0)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object vObj = gDAO.find(Vehicle.class, getVehicle_id());
			if(vObj != null)
			{
				getLicense().setVehicle((Vehicle)vObj);
				
				Object ltObj = gDAO.find(LicenseType.class, getLicenseType_id());
				if(ltObj != null)
					getLicense().setLicType((LicenseType)ltObj);
				
				Object ttObj = gDAO.find(TransactionType.class, getTransactionType_id());
				if(ttObj != null)
					getLicense().setTranType((TransactionType)ttObj);
				
				Object vendorObj = gDAO.find(Vendor.class, getVendor_id());
				if(vendorObj != null)
					getLicense().setVendor((Vendor)vendorObj);
				
				if(getLicenseDocument() != null)
					getLicense().setDocument(getLicenseDocument().getContents());
				
				Calendar c = Calendar.getInstance();
				c.setTime(getLicense().getLic_start_dt());
				c.add(Calendar.YEAR, 1);
				getLicense().setLic_end_dt(c.getTime());
				
				if(getLicense().getLic_end_dt().after(new Date()))
				{
					getLicense().setActive(true);
					getLicense().setExpired(false);
				}
				else
				{
					getLicense().setActive(false);
					getLicense().setExpired(true);
				}
				
				gDAO.startTransaction();
				boolean ret = false;
				if(getLicense().getId() != null)
				{
					getLicense().setCreatedBy(dashBean.getUser());
					ret = gDAO.update(getLicense());
				}
				else
				{
					getLicense().setCrt_dt(new Date());
					getLicense().setCreatedBy(dashBean.getUser());
					ret = gDAO.save(getLicense());
				}
				
				if(ret)
				{
					gDAO.commit();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "License captured/updated successfully!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					
					setLicense(null);
					setLicenses(null);
					setVehicle_id(null);
					setLicenseType_id(null);
					setTransactionType_id(null);
					setLicenseDocument(null);
					setVendor_id(null);
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Error occured during save: " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				gDAO.destroy();
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid vehicle selected!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Also ensure you selected a vehicle!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void attendToFuelingRequest()
	{
		if(getPendingFuelingRequests() != null && getPendingFuelingRequests().size() > 0)
		{
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = false;
			for(VehicleFuelingRequest expR : getPendingFuelingRequests())
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
							Hashtable<String, Object> params = new Hashtable<String, Object>();
							params.put("name", "Fueling");
							params.put("systemObj", true);
							Object expTypesObj = gDAO.search("ExpenseType", params);
							if(expTypesObj != null)
							{
								Vector<ExpenseType> expTypes = (Vector<ExpenseType>)expTypesObj;
								ExpenseType et = null;
								if(expTypes.size() > 0)
									et = expTypes.get(0);
									
									Expense exp = new Expense();
									exp.setAmount(expR.getAmt());
									exp.setCreatedBy(expR.getCreatedBy());
									exp.setCrt_dt(new Date());
									exp.setExpense_dt(expR.getFueling_dt());
									exp.setPartner(expR.getCreatedBy().getPartner());
									exp.setRemarks("Fueling for vehicle: " + expR.getVehicle().getRegistrationNo() + " on " + getFueling().getCaptured_dt());
									
									exp.setType(et);
									
									ret = gDAO.save(exp);
									if(!ret)
										break;
							}
							
							VehicleFueling vf = new VehicleFueling();
							vf.setAmt(expR.getAmt());
							vf.setCaptured_dt(expR.getFueling_dt());
							vf.setCreatedBy(expR.getCreatedBy());
							vf.setCrt_dt(new Date());
							vf.setFuelLevel(expR.getFuelLevel());
							vf.setLitres(expR.getLitres());
							vf.setLocation(expR.getLocation());
							vf.setOdometer(expR.getOdometer());
							vf.setSource("Cash");
							vf.setVehicle(expR.getVehicle());
							ret = gDAO.save(vf);
							if(!ret)
								break;
							else
							{
								if(vf.getOdometer() > 0)
								{
									VehicleOdometerData vod = new VehicleOdometerData();
									vod.setCaptured_dt(vf.getCaptured_dt());
									vod.setCrt_dt(new Date());
									vod.setOdometer(vf.getOdometer());
									vod.setSource("Fueling");
									vod.setVehicle(vf.getVehicle());
									
									gDAO.save(vod);
								}
								
								if(vf.getFuelLevel() > 0)
								{
									VehicleFuelData vfd = new VehicleFuelData();
									vfd.setCaptured_dt(vf.getCaptured_dt());
									vfd.setCrt_dt(new Date());
									vfd.setFuelLevel(vf.getFuelLevel());
									vfd.setSource("Fueling");
									vfd.setVehicle(vf.getVehicle());
									
									gDAO.save(vfd);
								}
								
								expR.setVehicleFueling(vf);
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
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Fueling request(s) attended to successfully!");
				
				setPendingFuelingRequests(null);
				setApprovalComment(null);
				setApprovalStatus(null);
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
	public void saveBatchFuelingRequest()
	{
		if(getMyFuelingRequests() != null && getMyFuelingRequests().size() > 0)
		{
			ArrayList<String> emails = new ArrayList<String>(), usernames = new ArrayList<String>();
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = false;
			for(VehicleFuelingRequest expR : getMyFuelingRequests())
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
							n.setMessage("You have pending fueling request from '" + dashBean.getUser().getPersonel().getFirstname() + " " + dashBean.getUser().getPersonel().getLastname() + "'. Submitted: " + new Date().toLocaleString());
							n.setNotified(false);
							n.setSubject("Fueling Request");
							n.setPage_url("approve_fueling");
							
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
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Fueling request(s) save successfully!");
				
				if(emails.size() > 0)
				{
					final String[] to = new String[emails.size()];
					for(int i=0; i<to.length; i++)
						to[i] = emails.get(i);
					final String subject = "Fueling Request - Batch";
					StringBuilder sb = new StringBuilder();
					sb.append("<html><body>");
					sb.append("<p><strong>Hello All</strong><br/></p>");
					sb.append("<p>Please be informed that User: '" + dashBean.getUser().getPersonel().getFirstname() + " " + dashBean.getUser().getPersonel().getLastname() + "' just submitted a batch fueling request to " + to.length + " person(s), which you are one of, for approval.<br/></p>");
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
				
				setMyFuelingRequests(null);
				setPendingFuelingRequests(null);
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
	
	public void addFuelingRequestToBatch()
	{
		if(getVehicle_id() != null && getFuelingRequest().getFueling_dt() != null && getFuelingRequest().getAmt() > 0 &&
				getFuelingRequest().getLitres() > 0 && getApprovalUser_id() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object vObj = gDAO.find(Vehicle.class, getVehicle_id());
			if(vObj != null)
			{
				getFuelingRequest().setVehicle((Vehicle)vObj);
				
				Object uObj = gDAO.find(PartnerUser.class, getApprovalUser_id());
				if(uObj != null)
				{
					PartnerUser approveUser = (PartnerUser)uObj;
					getFuelingRequest().setApprovalUser(approveUser);
					
					getFuelingRequest().setApprovalStatus("PENDING");
					getFuelingRequest().setCreatedBy(dashBean.getUser());
					getFuelingRequest().setCrt_dt(new Date());
					getFuelingRequest().setRequest_dt(new Date());
					
					if(getMyFuelingRequests() == null)
						setMyFuelingRequests(new Vector<VehicleFuelingRequest>());
					getMyFuelingRequests().add(getFuelingRequest());
					
					setFuelingRequest(null);
					
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Request added to batch successfully.");
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid approval user selected!");
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid vehicle selected!");
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Also ensure you selected a vehicle!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	public void removeFuelingRequestFromBatch(int pos)
	{
		if(getMyFuelingRequests() != null)
		{
			if(pos < 0 || pos >= getMyFuelingRequests().size())
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid position for request to remove!");
			}
			else
			{
				getMyFuelingRequests().remove(pos);
				getMyFuelingRequests().trimToSize();
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
	public void saveFuelingRequest()
	{
		if(getVehicle_id() != null && getFuelingRequest().getFueling_dt() != null && getFuelingRequest().getAmt() > 0 &&
					getFuelingRequest().getLitres() > 0 && getApprovalUser_id() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object vObj = gDAO.find(Vehicle.class, getVehicle_id());
			if(vObj != null)
			{
				getFuelingRequest().setVehicle((Vehicle)vObj);
				
				Object uObj = gDAO.find(PartnerUser.class, getApprovalUser_id());
				if(uObj != null)
				{
					PartnerUser approveUser = (PartnerUser)uObj;
					getFuelingRequest().setApprovalUser(approveUser);
					
					getFuelingRequest().setApprovalStatus("PENDING");
					getFuelingRequest().setCreatedBy(dashBean.getUser());
					getFuelingRequest().setCrt_dt(new Date());
					getFuelingRequest().setRequest_dt(new Date());
					
					gDAO.startTransaction();
					boolean ret = false;
					ret = gDAO.save(getFuelingRequest());
					if(ret)
					{
						// Send notification and email to the approval user
						Notification n = new Notification();
						n.setCrt_dt(new Date());
						n.setUser(approveUser);
						n.setMessage("You have pending fueling request from '" + dashBean.getUser().getPersonel().getFirstname() + " " + dashBean.getUser().getPersonel().getLastname() + "'. Submitted: " + new Date().toLocaleString());
						n.setNotified(false);
						n.setSubject("Fueling Request");
						n.setPage_url("approve_fueling");
						
						gDAO.save(n);
						
						gDAO.commit();
						
						if(approveUser.getPersonel().getEmail() != null && approveUser.getPersonel().getEmail().trim().length() > 0)
						{
							final String to = approveUser.getPersonel().getEmail();
							final String subject = "Fueling Request";
							StringBuilder sb = new StringBuilder();
							sb.append("<html><body>");
							sb.append("<p><strong>Dear " + approveUser.getPersonel().getFirstname() + "</strong><br/></p>");
							sb.append("<p>Please be informed that User: '" + dashBean.getUser().getPersonel().getFirstname() + " " + dashBean.getUser().getPersonel().getLastname() + "' just submitted a fueling request to you for approval.<br/></p>");
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
						
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Fueling request saved successfully!");
						setFuelingRequest(null);
						setMyFuelingRequests(null);
						setPendingFuelingRequests(null);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during save: " + gDAO.getMessage());
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid approval user selected!");
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid vehicle selected!");
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Also ensure you selected a vehicle!");
		}
		
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	@SuppressWarnings("unchecked")
	public void saveFueling()
	{
		if(getVehicle_id() != null && getFueling().getCaptured_dt() != null && getFueling().getAmt() > 0 &&
				getFueling().getLitres() > 0)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object vObj = gDAO.find(Vehicle.class, getVehicle_id());
			if(vObj != null)
			{
				getFueling().setVehicle((Vehicle)vObj);
			
				gDAO.startTransaction();
				boolean ret = false;
				if(getFueling().getId() != null)
				{
					getFueling().setCreatedBy(dashBean.getUser());
					ret = gDAO.update(getFueling());
				}
				else
				{
					getFueling().setCrt_dt(new Date());
					getFueling().setCreatedBy(dashBean.getUser());
					ret = gDAO.save(getFueling());
				}
				
				if(ret)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("name", "Fueling");
					params.put("systemObj", true);
					Object expTypesObj = gDAO.search("ExpenseType", params);
					if(expTypesObj != null)
					{
						Vector<ExpenseType> expTypes = (Vector<ExpenseType>)expTypesObj;
						if(expTypes.size() > 0)
						{
							// insert an expense for this fueling transaction
							Expense exp = new Expense();
							exp.setAmount(getFueling().getAmt());
							exp.setCreatedBy(dashBean.getUser());
							exp.setCrt_dt(new Date());
							exp.setExpense_dt(getFueling().getCaptured_dt());
							exp.setPartner(getPartner());
							exp.setRemarks("Fueling for vehicle: " + getFueling().getVehicle().getRegistrationNo() + " on " + getFueling().getCaptured_dt());
							
							exp.setType(expTypes.get(0));
							
							gDAO.save(exp);
						}
					}
					
					if(getFueling().getOdometer() > 0)
					{
						VehicleOdometerData vod = new VehicleOdometerData();
						vod.setCaptured_dt(getFueling().getCaptured_dt());
						vod.setCrt_dt(new Date());
						vod.setOdometer(getFueling().getOdometer());
						vod.setSource("Fueling");
						vod.setVehicle(getFueling().getVehicle());
						
						gDAO.save(vod);
					}
					
					if(getFueling().getFuelLevel() > 0)
					{
						VehicleFuelData vfd = new VehicleFuelData();
						vfd.setCaptured_dt(getFueling().getCaptured_dt());
						vfd.setCrt_dt(new Date());
						vfd.setFuelLevel(getFueling().getFuelLevel());
						vfd.setSource("Fueling");
						vfd.setVehicle(getFueling().getVehicle());
						
						gDAO.save(vfd);
					}
					
					gDAO.commit();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Fueling captured/updated successfully!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					
					setFueling(null);
					setFuelings(null);
					setVehicle_id(null);
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Error occured during save: " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				gDAO.destroy();
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid vehicle selected!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Also ensure you selected a vehicle!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void repairAccident()
	{
		if(getSelectedAccident() != null && getSelectedAccident().getId() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			getRepair().setAccident(getSelectedAccident());
			getRepair().setCreatedBy(dashBean.getUser());
			getRepair().setCrt_dt(new Date());
			
			if(getRepairComp_id() != null)
			{
				Object repairVendor = gDAO.find(Vendor.class, getRepairComp_id());
				if(repairVendor != null)
					getRepair().setRepairComp((Vendor)repairVendor);
			}
			if(getInsuranceComp_id() != null)
			{
				Object vendor = gDAO.find(Vendor.class, getInsuranceComp_id());
				if(vendor != null)
					getRepair().setInsuranceComp((Vendor)vendor);
			}
			if(getRepairedPhoto() != null)
			{
				getRepair().setAfterRepairPhoto(getRepairedPhoto().getContents());
			}
			if(getRepairAttachment() != null)
			{
				getRepair().setAttachment(getRepairAttachment().getContents());
			}
			
			boolean ret = false;
			
			gDAO.startTransaction();
			ret = gDAO.save(getRepair());
			if(ret)
			{
				if(getRepair().getRepairerType().equals("REPAIR"))
				{
					if(getRepair().isRequiresAdHocRepair())
					{
						VehicleAdHocMaintenance maint = new VehicleAdHocMaintenance();
						
						maint.setCreatedBy(dashBean.getUser());
						maint.setCrt_dt(new Date());
						maint.setVehicle(getRepair().getAccident().getVehicle());
						maint.setActive(true);
						maint.setStatus("Started");
						
						ret = gDAO.save(maint);
						if(ret)
						{
							maint.getVehicle().setActive(false);
							maint.getVehicle().setActiveStatus(VehicleStatusEnum.UNDER_MAINTENANCE.getStatus());
							gDAO.update(maint.getVehicle());
						}
					}
					else
					{
						getRepair().getAccident().getVehicle().setActive(true);
						getRepair().getAccident().getVehicle().setActiveStatus(VehicleStatusEnum.ACTIVE.getStatus());
						gDAO.update(getRepair().getAccident().getVehicle());
					}
				}
				else if(getRepair().getRepairerType().equals("REPLACE"))
				{
					getRepair().getAccident().getVehicle().setActive(false);
					getRepair().getAccident().getVehicle().setActiveStatus(VehicleStatusEnum.REPLACED.getStatus());
					gDAO.update(getRepair().getAccident().getVehicle());
				}
				else if(getRepair().getRepairerType().equals("GROUNDED"))
				{
					getRepair().getAccident().getVehicle().setActive(false);
					getRepair().getAccident().getVehicle().setActiveStatus(VehicleStatusEnum.GROUNDED.getStatus());
					gDAO.update(getRepair().getAccident().getVehicle());
				}
				
				getRepair().getAccident().setActive(false);
				getRepair().getAccident().setRepairApprovedDesc("DONE");
				gDAO.update(getRepair().getAccident());
				
				gDAO.commit();
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Accident repaired successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setApprovedAccidents(null);
				setSelectedAccident(null);
				setRepair(null);
			}
			else
			{
				gDAO.rollback();
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Error occured during save: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			
			gDAO.destroy();
		}
	}
	
	public void attendToAccident()
	{
		if(getAccident().getId() != null)
		{
			if(getAccident().getRepairApprovedDesc() != null && getAccident().getApprovalComment() != null)
			{
				getAccident().setApprovalUser(dashBean.getUser());
				
				GeneralDAO gDAO = new GeneralDAO();
				gDAO.startTransaction();
				boolean ret = gDAO.update(getAccident());
				if(ret)
				{
					gDAO.commit();
					
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Accident attended to successfully!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					
					setPendingAccidents(null);
					setReviewedAccidents(null);
					setAccident(null);
				}
				else
				{
					gDAO.rollback();
					
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Action failed: " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				gDAO.destroy();
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Also ensure you selected a vehicle!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid accident repair request selected!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void cancelAccident()
	{
		if(getSelectedAccident() != null && getSelectedAccident().getId() != null)
		{
			getSelectedAccident().setActive(false);
			getSelectedAccident().setRepairApprovedDesc("CANCELED");
			
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = gDAO.update(getSelectedAccident());
			if(ret)
			{
				getSelectedAccident().getVehicle().setActive(true);
				getSelectedAccident().getVehicle().setActiveStatus(VehicleStatusEnum.ACTIVE.getStatus());
				
				gDAO.update(getSelectedAccident().getVehicle());
				
				gDAO.commit();
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Accident canceled successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setAccidents(null);
				setApprovedAccidents(null);
				setDeniedAccidents(null);
				setSelectedAccident(null);
			}
			else
			{
				gDAO.rollback();
				
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Action failed: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void saveAccident()
	{
		if(getVehicle_id() != null && getAccident().getAccident_dt() != null && getAccident().getAccidentDescription() != null &&
				getAccident().getDriverComment() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object vObj = gDAO.find(Vehicle.class, getVehicle_id());
			if(vObj != null)
			{
				getAccident().setVehicle((Vehicle)vObj);
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle", getAccident().getVehicle());
				params.put("active", true);
				
				Object drvObj = gDAO.search("VehicleDriver", params);
				if(drvObj != null)
				{
					Vector<VehicleDriver> drvList = (Vector<VehicleDriver>)drvObj;
					if(drvList != null && drvList.size() > 0)
					{
						getAccident().setAssignedDriver(drvList.get(0).getDriver());
					}
				}
			}
			/*if(getRepairComp_id() != null)
			{
				Object repairVendor = gDAO.find(Vendor.class, getRepairComp_id());
				if(repairVendor != null)
					getAccident().setRepairComp((Vendor)repairVendor);
			}
			if(getInsuranceComp_id() != null)
			{
				Object vendor = gDAO.find(Vendor.class, getInsuranceComp_id());
				if(vendor != null)
					getAccident().setInsuranceComp((Vendor)vendor);
			}*/
			if(getAccidentPhoto() != null)
			{
				getAccident().setAccidentPhoto(getAccidentPhoto().getContents());
			}
			/*if(getRepairedPhoto() != null)
			{
				getAccident().setAfterRepairPhoto(getRepairedPhoto().getContents());
			}*/
			if(getAccidentDocument() != null)
			{
				getAccident().setDocument(getAccidentDocument().getContents());
			}
			if(getAccidentDocument2() != null)
			{
				getAccident().setDocument2(getAccidentDocument2().getContents());
			}
			if(getAccidentDocument3() != null)
			{
				getAccident().setDocument3(getAccidentDocument3().getContents());
			}
			
			if(getAccident().getRequiresRepairOrReplace().trim().length() == 0)
			{
				getAccident().setActive(false);
				getAccident().setRepairApprovedDesc("NOT NEEDED");
			}
			else
			{
				getAccident().setActive(true);
				getAccident().setRepairApprovedDesc("PENDING");
			}
			
			gDAO.startTransaction();
			boolean ret = false;
			if(getAccident().getId() != null)
			{
				getAccident().setCreatedBy(dashBean.getUser());
				ret = gDAO.update(getAccident());
			}
			else
			{
				getAccident().setCrt_dt(new Date());
				getAccident().setCreatedBy(dashBean.getUser());
				ret = gDAO.save(getAccident());
			}
			
			if(ret)
			{
				if(getAccident().getRequiresRepairOrReplace().trim().length() > 0)
				{
					getAccident().getVehicle().setActive(false);
					getAccident().getVehicle().setActiveStatus(VehicleStatusEnum.ACCIDENTED.getStatus());
					
					gDAO.update(getAccident().getVehicle());
				}
				
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Accident captured/updated successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setAccidentStatus(false);
				setRepairComp_id(null);
				setInsuranceComp_id(null);
				setAccidentPhoto(null);
				setRepairedPhoto(null);
				setAccidentDocument(null);
				setAccident(null);
				setAccidents(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Error occured during save: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Also ensure you selected a vehicle!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void finishAdHocMaintenance()
	{
		if(getAdhocMain().getId() != null && getAdhocMain().getClose_dt() != null &&
				getAdhocMain().getClosed_cost() != null && getAdhocMain().getClosed_cost().doubleValue() > 0)
		{
			getAdhocMain().setActive(false);
			getAdhocMain().setStatus("Finished");
			
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = gDAO.update(getAdhocMain());
			if(ret)
			{
				getAdhocMain().getVehicle().setActive(true);
				getAdhocMain().getVehicle().setActiveStatus(VehicleStatusEnum.ACTIVE.getStatus());
				gDAO.update(getAdhocMain().getVehicle());
				
				gDAO.commit();
				setAdhocMain(null);
				setAdhocMains(null);
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Ad-Hoc maintenance started for successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during maintenance completion: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Also ensure you selected a started Ad-Hoc maintenance!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void completeAdhocWorkOrder()
	{
		if(getSelectedInprogressAdhocWorkOrder() != null && getSelectedInprogressAdhocWorkOrder().getVehicles().size() > 0)
		{
			boolean ret = false;
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			for(WorkOrderVehicle wrdv : getSelectedInprogressAdhocWorkOrder().getVehicles())
			{
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle", wrdv.getVehicle());
				params.put("workOrder", getSelectedInprogressAdhocWorkOrder());
				
				Object vrmObj = gDAO.search("VehicleAdHocMaintenance", params);
				if(vrmObj != null)
				{
					Vector<VehicleAdHocMaintenance> list = (Vector<VehicleAdHocMaintenance>)vrmObj;
					VehicleAdHocMaintenance vrm = list.get(0);
					
					vrm.setClose_dt(wrdv.getClose_dt());
					vrm.setClosed_cost(wrdv.getClosed_amount());
					vrm.setActive(false);
					vrm.setStatus("Finished");
					ret = gDAO.update(vrm);
					if(ret)
					{
						// vehicle is no longer under maintenance
						vrm.getVehicle().setActive(true);
						vrm.getVehicle().setActiveStatus(VehicleStatusEnum.ACTIVE.getStatus());
						ret = gDAO.update(vrm.getVehicle());
					}
				}
			}
			
			if(ret)
			{
				getSelectedInprogressAdhocWorkOrder().setFinished(true);
				getSelectedInprogressAdhocWorkOrder().setStatus("COMPLETED");
				if(getWorkOrderVendorDoc() != null && getWorkOrderVendorDoc().getSize() > 0L)
				{
					if(getWorkOrderVendorDoc().getFileName().toLowerCase().indexOf(".pdf")>=0)
					{
						if(getWorkOrderVendorDoc().getSize() <= 500000L)
							getSelectedInprogressAdhocWorkOrder().setVendorDocument(getWorkOrderVendorDoc().getContents());
						else
						{
							msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Upload Error: ", "Document not uploaded because its too large!");
							FacesContext.getCurrentInstance().addMessage(null, msg);
						}
					}
					else
					{
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Upload Error: ", "Document not uploaded. Only PDF format is allowed!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				gDAO.update(getSelectedInprogressAdhocWorkOrder());
				
				gDAO.commit();
				setSelectedInprogressAdhocWorkOrder(null);
				setInprgWorkOrder_id(0L);
				resetInprogressAdhocWorkOrderVehicles();
				setInprogressAdhocWorkOrders(null);
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Adhoc maintenance work order finished successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during maintenance finish: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No In-Progress work order selected!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void startAdHocMaintenanceWK()
	{
		if(getSelectedPendingAdhocWorkOrder() != null && getSelectedPendingAdhocWorkOrder().getVehicles().size() > 0 && 
				getAdhocMain().getStart_dt() != null)
		{
			if(getSelectedPendingAdhocWorkOrder().getVehicles() != null && getSelectedPendingAdhocWorkOrder().getVehicles().size() > 0)
			{
				int count = 0;
				boolean ret = false;
				GeneralDAO gDAO = new GeneralDAO();
				Vendor vendor = null;
				if(getVendor_id() != null && getVendor_id() > 0)
				{
					Object vobj = gDAO.find(Vendor.class, getVendor_id());
					if(vobj != null)
						vendor = (Vendor)vobj;
				}
				gDAO.startTransaction();
				for(WorkOrderVehicle wrdv : getSelectedPendingAdhocWorkOrder().getVehicles())
				{
					Vehicle v = wrdv.getVehicle();
					
					VehicleAdHocMaintenance vr = new VehicleAdHocMaintenance();
					vr.setDescription(wrdv.getDetailsOfWork());
					vr.setInitial_cost(wrdv.getInitial_amount());
					vr.setStart_dt(getAdhocMain().getStart_dt());
					vr.setWorkOrder(getSelectedPendingAdhocWorkOrder());
					vr.setCreatedBy(dashBean.getUser());
					vr.setCrt_dt(new Date());
					vr.setVehicle(v);
					vr.setActive(true);
					vr.setStatus("Start");
					vr.setVendor(vendor);
					
					ret = gDAO.save(vr);
					if(!ret)
						break;
					else if(vr.getStatus().equalsIgnoreCase("Start"))
					{
						// vehicle is under maintenance
						v.setActive(false);
						v.setActiveStatus(VehicleStatusEnum.UNDER_MAINTENANCE.getStatus());
						ret = gDAO.update(v);
						if(!ret)
							break;
					}
					count++;
				}
				
				if(count > 0)
				{
					if(ret)
					{
						getSelectedPendingAdhocWorkOrder().setStatus("IN-PROGRESS");
						getSelectedPendingAdhocWorkOrder().setVendor(vendor);
						gDAO.update(getSelectedPendingAdhocWorkOrder());
						
						getSelectedPendingAdhocWorkOrder().getAdhocMainRequest().setActive(false);
						getSelectedPendingAdhocWorkOrder().getAdhocMainRequest().setStatus("Attended");
						gDAO.update(getSelectedPendingAdhocWorkOrder().getAdhocMainRequest());
						
						gDAO.commit();
						setAdhocMain(null);
						setVendor_id(null);
						setSelectedPendingAdhocWorkOrder(null);
						setWorkOrder_id(0L);
						setPendingAdhocWorkOrders(null);
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Adhoc maintenance started for " + count + " vehicle(s) successfully!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during maintenance start: " + gDAO.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No vehicle selected!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No vehicle found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void startAdHocMaintenance()
	{
		if(getAdhocRequest().getId() != null && getAdhocMain().getStart_dt() != null &&
				getAdhocMain().getDescription() != null && getAdhocMain().getInitial_cost() != null && 
				getAdhocMain().getInitial_cost().doubleValue() > 0)
		{
			getAdhocMain().setCreatedBy(dashBean.getUser());
			getAdhocMain().setCrt_dt(new Date());
			getAdhocMain().setVehicle(getAdhocRequest().getVehicle());
			getAdhocMain().setActive(true);
			getAdhocMain().setStatus("Started");
			
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = gDAO.save(getAdhocMain());
			if(ret)
			{
				getAdhocMain().getVehicle().setActive(false);
				getAdhocMain().getVehicle().setActiveStatus(VehicleStatusEnum.UNDER_MAINTENANCE.getStatus());
				gDAO.update(getAdhocMain().getVehicle());
				
				getAdhocRequest().setActive(false);
				getAdhocRequest().setStatus("Attended");
				gDAO.update(getAdhocRequest());
				
				gDAO.commit();
				setAdhocMain(null);
				setAdhocMains(null);
				setAdhocRequest(null);
				setAdHocRequests(null);
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Ad-Hoc maintenance started for successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during maintenance start: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Also ensure you selected an existing Ad-Hoc Request!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void requestAdHocMaintenance()
	{
		if(getAdhocRequest().getDescription() != null && getAdhocRequest().getMaintenance_dt() != null)
		{
			if(getVehicles() != null && getVehicles().size() > 0)
			{
				int count = 0;
				boolean ret = false;
				GeneralDAO gDAO = new GeneralDAO();
				gDAO.startTransaction();
				for(Vehicle v : getVehicles())
				{
					if(v.isSelected())
					{
						count++;
						getAdhocRequest().setActive(true);
						getAdhocRequest().setCreatedBy(dashBean.getUser());
						getAdhocRequest().setCrt_dt(new Date());
						getAdhocRequest().setStatus("Pending");
						getAdhocRequest().setVehicle(v);
						
						ret = gDAO.save(getAdHocRequests());
						if(!ret)
							break;
					}
				}
				if(count > 0)
				{
					if(ret)
					{
						gDAO.commit();
						setAdhocRequest(null);
						resetVehicles();
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Ad-Hoc maintenance request submitted for " + count + " vehicle(s) successfully!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during maintenance request: " + gDAO.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No vehicle selected!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No vehicle found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void finishRMaintenance()
	{
		if(getRoutine().getOdometer() != null && getRoutine().getOdometer().doubleValue() > 0 && 
				getRoutine().getDescription() != null && getRoutine().getInitial_amount() != null &&
				getRoutine().getInitial_amount().doubleValue() > 0 && getRoutine().getStart_dt() != null &&
				getRoutine().getClose_dt() != null && getRoutine().getClosed_amount() != null &&
				getRoutine().getClosed_amount().doubleValue() > 0)
		{
			getRoutine().setFinished(true);
			getRoutine().setStatus("Finished");
			boolean ret = false;
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			ret = gDAO.update(getRoutine());
			if(ret)
			{
				// vehicle is no longer under maintenance
				getRoutine().getVehicle().setActive(true);
				getRoutine().getVehicle().setActiveStatus(VehicleStatusEnum.ACTIVE.getStatus());
				ret = gDAO.update(getRoutine().getVehicle());
			}
			
			if(ret)
			{
				if(isNextRMSetup())
				{
					VehicleRoutineMaintenanceSetup vr = new VehicleRoutineMaintenanceSetup();
					vr.setLast_m_odometer(getRoutine().getOdometer());
					vr.setAlert_interval_odometer(getRoutineSetup().getAlert_interval_odometer());
					vr.setInterval_odometer(getRoutineSetup().getInterval_odometer());
					
					vr.setAlert_odometer(vr.getLast_m_odometer().add(vr.getAlert_interval_odometer()));
					vr.setOdometer(vr.getLast_m_odometer().add(vr.getInterval_odometer()));
					
					vr.setActive(true);
					vr.setCreatedBy(dashBean.getUser());
					vr.setCrt_dt(new Date());
					vr.setVehicle(getRoutine().getVehicle());
					ret = gDAO.save(vr);
					if(ret)
					{
						setNextRMSetup(false);
						setRoutineSetup(null);
					}
				}
				if(ret)
				{
					gDAO.commit();
					setRoutine(null);
					setVendor_id(null);
					resetRoutines();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Routine maintenance finished for vehicle successfully!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during maintenance finish: " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during maintenance finish: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void completeRoutineWorkOrder()
	{
		if(getSelectedInprogressRoutineWorkOrder() != null && getSelectedInprogressRoutineWorkOrder().getVehicles().size() > 0)
		{
			boolean ret = false;
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			for(WorkOrderVehicle wrdv : getSelectedInprogressRoutineWorkOrder().getVehicles())
			{
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle", wrdv.getVehicle());
				params.put("workOrder", getSelectedInprogressRoutineWorkOrder());
				
				Object vrmObj = gDAO.search("VehicleRoutineMaintenance", params);
				if(vrmObj != null)
				{
					Vector<VehicleRoutineMaintenance> list = (Vector<VehicleRoutineMaintenance>)vrmObj;
					VehicleRoutineMaintenance vrm = list.get(0);
					
					vrm.setClose_dt(wrdv.getClose_dt());
					vrm.setClosed_amount(wrdv.getClosed_amount());
					vrm.setFinished(true);
					vrm.setStatus("Finished");
					ret = gDAO.update(vrm);
					if(ret)
					{
						// vehicle is no longer under maintenance
						vrm.getVehicle().setActive(true);
						vrm.getVehicle().setActiveStatus(VehicleStatusEnum.ACTIVE.getStatus());
						ret = gDAO.update(vrm.getVehicle());
						
						if(wrdv.isNextRMSetup())
						{
							VehicleRoutineMaintenanceSetup vr = new VehicleRoutineMaintenanceSetup();
							vr.setLast_m_odometer(vrm.getOdometer());
							vr.setAlert_interval_odometer(wrdv.getAlert_interval_odometer());
							vr.setInterval_odometer(wrdv.getInterval_odometer());
							
							vr.setAlert_odometer(vr.getLast_m_odometer().add(vr.getAlert_interval_odometer()));
							vr.setOdometer(vr.getLast_m_odometer().add(vr.getInterval_odometer()));
							
							vr.setActive(true);
							vr.setCreatedBy(dashBean.getUser());
							vr.setCrt_dt(new Date());
							vr.setVehicle(vrm.getVehicle());
							ret = gDAO.save(vr);
						}
					}
				}
			}
			
			if(ret)
			{
				getSelectedInprogressRoutineWorkOrder().setFinished(true);
				getSelectedInprogressRoutineWorkOrder().setStatus("COMPLETED");
				if(getWorkOrderVendorDoc() != null && getWorkOrderVendorDoc().getSize() > 0L)
				{
					if(getWorkOrderVendorDoc().getFileName().toLowerCase().indexOf(".pdf")>=0)
					{
						if(getWorkOrderVendorDoc().getSize() <= 500000L)
							getSelectedInprogressRoutineWorkOrder().setVendorDocument(getWorkOrderVendorDoc().getContents());
						else
						{
							msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Upload Error: ", "Document not uploaded because its too large!");
							FacesContext.getCurrentInstance().addMessage(null, msg);
						}
					}
					else
					{
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Upload Error: ", "Document not uploaded. Only PDF format is allowed!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				gDAO.update(getSelectedInprogressRoutineWorkOrder());
				
				gDAO.commit();
				setSelectedInprogressRoutineWorkOrder(null);
				setInprgWorkOrder_id(0L);
				resetInprogressRoutineWorkOrderVehicles();
				setInprogressRoutineWorkOrders(null);
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Routine maintenance work order finished successfully!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during maintenance finish: " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No In-Progress work order selected!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void startRMaintenance()
	{
		if(getSelectedPendingRoutineWorkOrder() != null && getSelectedPendingRoutineWorkOrder().getVehicles().size() > 0 && 
				getRoutine().getStart_dt() != null)
		{
			if(getSelectedPendingRoutineWorkOrder().getVehicles() != null && getSelectedPendingRoutineWorkOrder().getVehicles().size() > 0)
			{
				int count = 0;
				boolean ret = false;
				GeneralDAO gDAO = new GeneralDAO();
				Vendor vendor = null;
				if(getVendor_id() != null && getVendor_id() > 0)
				{
					Object vobj = gDAO.find(Vendor.class, getVendor_id());
					if(vobj != null)
						vendor = (Vendor)vobj;
				}
				gDAO.startTransaction();
				for(WorkOrderVehicle wrdv : getSelectedPendingRoutineWorkOrder().getVehicles())
				{
					Vehicle v = wrdv.getVehicle();
					
					VehicleRoutineMaintenance vr = new VehicleRoutineMaintenance();
					count++;
					vr.setWorkOrder(getSelectedPendingRoutineWorkOrder());
					vr.setOdometer(wrdv.getOdometer());
					vr.setDescription(wrdv.getDetailsOfWork());
					vr.setFinished(false);
					vr.setInitial_amount(new BigDecimal(wrdv.getInitEstAmount()));
					vr.setStart_dt(getRoutine().getStart_dt());
					vr.setStatus("Start");
					vr.setCreatedBy(dashBean.getUser());
					vr.setCrt_dt(new Date());
					vr.setVehicle(v);
					vr.setVendor(vendor);
					
					ret = gDAO.save(vr);
					if(!ret)
						break;
					else if(vr.getStatus().equalsIgnoreCase("Start"))
					{
						// vehicle is under maintenance
						v.setActive(false);
						v.setActiveStatus(VehicleStatusEnum.UNDER_MAINTENANCE.getStatus());
						ret = gDAO.update(v);
						if(!ret)
							break;
					}
					
					if(ret)
					{
						// reset all the active maintenance setup for this vehicle to in-active
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("vehicle", v);
						params.put("active", true);
						
						Object vrmsObj = gDAO.search("VehicleRoutineMaintenanceSetup", params);
						if(vrmsObj != null)
						{
							Vector<VehicleRoutineMaintenanceSetup> vrmsList = (Vector<VehicleRoutineMaintenanceSetup>)vrmsObj;
							if(vrmsList.size()>0)
							{
								for(VehicleRoutineMaintenanceSetup vrm : vrmsList)
								{
									if(vrm.getOdometer().doubleValue() <= getRoutine().getOdometer().doubleValue())
									{
										vrm.setActive(false);
										gDAO.update(vrm);
									}
								}
							}
						}
					}
				}
				if(count > 0)
				{
					if(ret)
					{
						getSelectedPendingRoutineWorkOrder().setStatus("IN-PROGRESS");
						getSelectedPendingRoutineWorkOrder().setVendor(vendor);
						gDAO.update(getSelectedPendingRoutineWorkOrder());
						
						gDAO.commit();
						setRoutine(null);
						setVendor_id(null);
						setSelectedPendingRoutineWorkOrder(null);
						setWorkOrder_id(0L);
						setPendingRoutineWorkOrders(null);
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Routine maintenance started for " + count + " vehicle(s) successfully!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during maintenance start: " + gDAO.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No vehicle selected!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No vehicle found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void setupRMaintenance()
	{
		if(getVehicles() != null && getVehicles().size() > 0)
		{
			int count = 0;
			boolean ret = false;
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			for(Vehicle v : getVehicles())
			{
				if(v.isSelected())
				{
					if(v.getRout_setup().getLast_m_odometer() != null && v.getRout_setup().getLast_m_odometer().doubleValue() > 0 && 
						v.getRout_setup().getInterval_odometer() != null && v.getRout_setup().getInterval_odometer().doubleValue() > 0 &&
						v.getRout_setup().getAlert_interval_odometer() != null && v.getRout_setup().getAlert_interval_odometer().doubleValue() > 0)
					{
						VehicleRoutineMaintenanceSetup vr = v.getRout_setup();
						count++;
						
						vr.setAlert_odometer(vr.getAlert_interval_odometer().add(vr.getLast_m_odometer()));
						vr.setOdometer(vr.getLast_m_odometer().add(vr.getInterval_odometer()));
						vr.setActive(true);
						vr.setCreatedBy(dashBean.getUser());
						vr.setCrt_dt(new Date());
						vr.setVehicle(v);
						ret = gDAO.save(vr);
						if(!ret)
							break;
					}
					else
					{
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required! Vehicle: " + v.getRegistrationNo());
						FacesContext.getCurrentInstance().addMessage(null, msg);
						ret = false;
						break;
					}
				}
			}
			if(count > 0)
			{
				if(ret)
				{
					gDAO.commit();
					setRoutineSetup(null);
					resetVehicles();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Routine maintenance setup for " + count + " vehicle(s) successfully!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Error occured during setup: " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No vehicle selected!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "No vehicle found!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void searchTrackerInfo()
	{
		if(getPartner() != null && getFleet_id() != null && getFleet_id() > 0 && getStdt() != null && getEddt() != null)
		{
			setTvehicles(null);
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void searchTrackerInfo2()
	{
		if(getPartner() != null && getFleet_id() != null && getFleet_id() > 0)
		{
			setTvehicles2(null);
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Vehicle> getTvehicles() {
		if(tvehicles == null)
		{
			tvehicles = new Vector<Vehicle>();
			
			if(getPartner() != null && getFleet_id() != null && getFleet_id() > 0)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				
				if(getFleet_id() != null && getFleet_id() > 0)
				{
					Object fobj = gDAO.find(Fleet.class, getFleet_id());
					if(fobj != null)
						params.put("fleet", (Fleet)fobj);
				}
				
				boolean found_one = false;
				if(getVehicle_id() != null && getVehicle_id() > 0)
				{
					Object vobj = gDAO.find(Vehicle.class, getVehicle_id());
					if(vobj != null)
					{
						tvehicles = new Vector<Vehicle>();
						tvehicles.add((Vehicle)vobj);
					}
					found_one = true;
				}
				else if(getRegNo() != null && getRegNo().trim().length() > 0)
				{
					params.put("registrationNo", getRegNo());
				}
				if(!found_one)
				{
					Object vhsObj = gDAO.search("Vehicle", params);
					if(vhsObj != null)
					{
						tvehicles = (Vector<Vehicle>)vhsObj;
					}
				}
				
				if(tvehicles != null && tvehicles.size() > 0)
				{
					for(Vehicle v : tvehicles)
					{
						Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle = :vehicle and (e.captured_dt between :stdt and :eddt) order by e.captured_dt desc");
						
						q.setParameter("vehicle", v);
						q.setParameter("stdt", getStdt());
						q.setParameter("eddt", getEddt());
						
						Object retObj = gDAO.search(q, 0);
						if(retObj != null)
							v.setTrackerData((Vector<VehicleTrackerData>)retObj);
					}
				}
			}
		}
		return tvehicles;
	}

	public void setTvehicles(Vector<Vehicle> tvehicles) {
		this.tvehicles = tvehicles;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vehicle> getTvehicles2() {
		if(tvehicles2 == null)
		{
			tvehicles2 = new Vector<Vehicle>();
			
			if(getPartner() != null && getFleet_id() != null && getFleet_id() > 0)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				
				if(getFleet_id() != null && getFleet_id() > 0)
				{
					Object fobj = gDAO.find(Fleet.class, getFleet_id());
					if(fobj != null)
						params.put("fleet", (Fleet)fobj);
				}
				
				boolean found_one = false;
				if(getVehicle_id() != null && getVehicle_id() > 0)
				{
					Object vobj = gDAO.find(Vehicle.class, getVehicle_id());
					if(vobj != null)
					{
						tvehicles2 = new Vector<Vehicle>();
						tvehicles2.add((Vehicle)vobj);
					}
					found_one = true;
				}
				else if(getRegNo() != null && getRegNo().trim().length() > 0)
				{
					params.put("registrationNo", getRegNo());
				}
				if(!found_one)
				{
					Object vhsObj = gDAO.search("Vehicle", params);
					if(vhsObj != null)
					{
						tvehicles2 = (Vector<Vehicle>)vhsObj;
					}
				}
				
				if(tvehicles2 != null && tvehicles2.size() > 0)
				{
					for(Vehicle v : tvehicles2)
					{
						Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle = :vehicle order by e.captured_dt desc");
						
						q.setParameter("vehicle", v);
						
						try
						{
							Object retObj = gDAO.search(q, 0);
							if(retObj != null)
								v.setTrackerData((Vector<VehicleTrackerData>)retObj);
						}
						catch(Exception ex){}
					}
				}
			}
		}
		return tvehicles2;
	}

	public void setTvehicles2(Vector<Vehicle> tvehicles2) {
		this.tvehicles2 = tvehicles2;
	}

	public Long getDocumentType_id() {
		return documentType_id;
	}

	public void setDocumentType_id(Long documentType_id) {
		this.documentType_id = documentType_id;
	}

	public UploadedFile getDocumentContent() {
		return documentContent;
	}

	public void setDocumentContent(UploadedFile documentContent) {
		this.documentContent = documentContent;
	}

	public VehicleDocument getVehicleDocument() {
		if(vehicleDocument == null)
			vehicleDocument = new VehicleDocument();
		return vehicleDocument;
	}

	public void setVehicleDocument(VehicleDocument vehicleDocument) {
		this.vehicleDocument = vehicleDocument;
	}

	public Vector<VehicleDocument> getVehicleDocuments() {
		return vehicleDocuments;
	}

	public void setVehicleDocuments(Vector<VehicleDocument> vehicleDocuments) {
		this.vehicleDocuments = vehicleDocuments;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleDocument> getSelectedVehicleDocuments() {
		boolean research = true;
		if(selectedVehicleDocuments == null || selectedVehicleDocuments.size() == 0)
			research = true;
		else if(selectedVehicleDocuments.size() > 0)
		{
			if(getSelectedVehicle() != null && getSelectedVehicle().getId() != null)
			{
				if(selectedVehicleDocuments.get(0).getVehicle().getId() == getSelectedVehicle().getId())
					research = false;
			}
		}
		if(research)
		{
			selectedVehicleDocuments = null;
			if(getSelectedVehicle() != null && getSelectedVehicle().getId() != null)
			{
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle", getSelectedVehicle());
				
				GeneralDAO gDAO = new GeneralDAO();
				Object sObj = gDAO.search("VehicleDocument", params);
				if(sObj != null)
				{
					selectedVehicleDocuments = (Vector<VehicleDocument>)sObj;
				}
			}
		}
		return selectedVehicleDocuments;
	}

	public void setSelectedVehicleDocuments(
			Vector<VehicleDocument> selectedVehicleDocuments) {
		this.selectedVehicleDocuments = selectedVehicleDocuments;
	}

	@SuppressWarnings("unchecked")
	public void addVehicleTrack()
	{
		if(getSelectedVehicle().getId() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			boolean exists = false;
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("vehicle", getSelectedVehicle());
			params.put("user", dashBean.getUser());
			Object dvObj = gDAO.search("DashboardVehicle", params);
			if(dvObj != null)
			{
				Vector<DashboardVehicle> dvList = (Vector<DashboardVehicle>)dvObj;
				if(dvList != null && dvList.size() > 0)
				{
					exists = true;
				}
			}
			
			if(!exists)
			{
				DashboardVehicle dv = new DashboardVehicle();
				dv.setCrt_dt(new Date());
				dv.setUser(dashBean.getUser());
				dv.setVehicle(getSelectedVehicle());
				gDAO.startTransaction();
				boolean ret = gDAO.save(dv);
				if(ret)
				{
					gDAO.commit();
					
					dashBean.setDashVehicles(null);
					
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Vehicle added to dashboard successfully.");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to add vehicle. " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Vehicle already tracked on dashboard.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void assignVehicleDriver()
	{
		if(getDriver_id() != null)
		{
			if(getSelectedVehicle().getId() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Object drvObj = gDAO.find(PartnerDriver.class, getDriver_id());
				if(drvObj != null)
				{
					PartnerDriver drv = (PartnerDriver)drvObj;
					
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("driver", drv);
					params.put("active", true);
					
					Object drvVs = gDAO.search("VehicleDriver", params);
					boolean wasPreviousAssigned = false;
					gDAO.startTransaction();
					if(drvVs != null)
					{
						Vector<VehicleDriver> drvVsList = (Vector<VehicleDriver>)drvVs;
						for(VehicleDriver vd : drvVsList)
						{
							wasPreviousAssigned = true;
							vd.setActive(false);
							vd.setEnd_dt(new Date());
							gDAO.update(vd);
						}
					}
					
					VehicleDriver vd = new VehicleDriver();
					vd.setActive(true);
					vd.setCreatedBy(dashBean.getUser());
					vd.setCrt_dt(new Date());
					vd.setDriver(drv);
					vd.setStart_dt(new Date());
					vd.setVehicle(getSelectedVehicle());
					boolean ret = gDAO.save(vd);
					if(ret)
					{
						gDAO.commit();
						setSelectedVehicle(null);
						setDriver_id(null);
						setPartnerDrivers(null);
						setVehicles(null);
						
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Driver assigned successfully." + ((wasPreviousAssigned) ? " NOTE: This driver was un-assigned from his/her current vehicle." : ""));
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else
					{
						gDAO.rollback();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create vehicle. " + gDAO.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No selected driver!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No selected vehicle!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void BatchLoadVehicles()
	{
		if(getVehiclesBatchExcel() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			try
			{
				ByteArrayInputStream byteIn = new ByteArrayInputStream(getVehiclesBatchExcel().getContents());
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
					if(!isLicenseAvailable())
					{
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "You have used up your license. Please purchase more license to add more vehicles!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
						break;
					}
					
					Row row = rowIterator.next();
					String fleet_nm = "", vehicleType = "", vehicleMaker = "", vehicleModel="", modelYr="", trackerID="", regNo="", engineNo="", chassisNo="";
					String department="", region="";
					String purchase_date="", purchased_amount="", purchased_from="", fuel_type="", tyre_size="", tank_capacity="";
					String calibrated_capacity="", color="", sim_no="", unit_of_measurement="", card_no="";
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
								fleet_nm = val;
								break;
							case 1:
								vehicleType = val;
								break;
							case 2:
								vehicleMaker = val;
								break;
							case 3:
								vehicleModel = val;
								break;
							case 4:
								modelYr = val;
								break;
							case 5:
								trackerID = val;
								break;
							case 6:
								regNo = val;
								break;
							case 7:
								engineNo = val;
								break;
							case 8:
								chassisNo = val;
								break;
							case 9:
								department = val;
								break;
							case 10:
								region = val;
								break;
							case 11:
								purchase_date = val;
								break;
							case 12:
								purchased_amount = val;
								break;
							case 13:
								purchased_from = val;
								break;
							case 14:
								fuel_type = val;
								break;
							case 15:
								tyre_size = val;
								break;
							case 16:
								tank_capacity = val;
								break;
							case 17:
								calibrated_capacity = val;
								break;
							case 18:
								color = val;
								break;
							case 19:
								sim_no = val;
								break;
							case 20:
								unit_of_measurement = val;
								break;
							case 21:
								card_no = val;
								break;
							}
						}
						
						boolean createModel = false;
						VehicleType vt = null;
						VehicleMake vm = null;
						VehicleModel vmd = null;
						
						Fleet fleet = null;
						Department deptObj = null;
						Region regionObj = null;
						
						boolean conti = true;
						
						if(regNo != null && regNo.trim().length() > 0)
						{
							Vehicle v = null;
							Query q = gDAO.createQuery("Select e from Vehicle e where e.registrationNo = :registrationNo");
							q.setParameter("registrationNo", regNo);
							Object vObj = gDAO.search(q, 0);
							if(vObj != null)
							{
								Vector<Vehicle> vList = (Vector<Vehicle>)vObj;
								for(Vehicle e : vList)
									v = e;
							}
							if(v != null)
								conti = false;
						}
						if(conti)
						{
						if(fleet_nm != null && fleet_nm.trim().length() > 0)
						{
							Query q = gDAO.createQuery("Select e from Fleet e where e.partner = :partner and e.name = :name");
							q.setParameter("partner", getPartner());
							q.setParameter("name", fleet_nm);
							Object objs = gDAO.search(q, 0);
							if(objs != null)
							{
								Vector<Fleet> objsList = (Vector<Fleet>)objs;
								for(Fleet e : objsList)
									fleet = e;
							}
							if(fleet == null && isAutoCreate())
							{
								fleet = new Fleet();
								fleet.setCreatedBy(dashBean.getUser());
								fleet.setCrt_dt(new Date());
								fleet.setName(fleet_nm);
								fleet.setPartner(getPartner());
								fleet.setDefaultFleet(false);
								ret = gDAO.save(fleet);
								if(!ret)
									break;
							}
							else if(fleet == null && !isAutoCreate())
							{
								ret = false;
								gDAO.setMessage("Fleet: '" + fleet_nm + "' does not exist for vehicle: " + regNo);
								break;
							}
						}
						
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
									deptObj = e;
							}
							if(deptObj == null && isAutoCreate())
							{
								deptObj = new Department();
								deptObj.setCreatedBy(dashBean.getUser());
								deptObj.setCrt_dt(new Date());
								deptObj.setName(department);
								deptObj.setPartner(getPartner());
								ret = gDAO.save(deptObj);
								if(!ret)
									break;
							}
							else if(deptObj == null && !isAutoCreate())
							{
								ret = false;
								gDAO.setMessage("Department: '" + department + "' does not exist for vehicle: " + regNo);
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
									regionObj = e;
							}
							if(regionObj == null && isAutoCreate())
							{
								regionObj = new Region();
								regionObj.setCreatedBy(dashBean.getUser());
								regionObj.setCrt_dt(new Date());
								regionObj.setName(region);
								regionObj.setPartner(getPartner());
								ret = gDAO.save(regionObj);
								if(!ret)
									break;
							}
							else if(regionObj == null && !isAutoCreate())
							{
								ret = false;
								gDAO.setMessage("Region: '" + region + "' does not exist for vehicle: " + regNo);
								break;
							}
						}
						
						if(vehicleType != null && vehicleType.trim().length() > 0)
						{
							// search for existing vehicle type and maker
							Query q = gDAO.createQuery("Select e from VehicleType e where e.name = :name");
							q.setParameter("name", vehicleType.trim());
							Object qObj = gDAO.search(q, 0);
							if(qObj != null)
							{
								Vector<VehicleType> vtList = (Vector<VehicleType>)qObj;
								for(VehicleType e : vtList)
									vt = e;
							}
							if(vt == null && isAutoCreate())
							{
								vt = new VehicleType();
								vt.setCreatedBy(dashBean.getUser());
								vt.setCrt_dt(new Date());
								vt.setName(vehicleType.trim());
								vt.setPartner(getPartner());
								ret = gDAO.save(vt);
								if(!ret)
									break;
								createModel = true;
							}
							else if(vt == null && !isAutoCreate())
							{
								ret = false;
								gDAO.setMessage("Vehicle Type: '" + vehicleType + "' does not exist for vehicle: " + regNo);
								break;
							}
						}
						
						if(vehicleMaker != null && vehicleMaker.trim().length() > 0)
						{
							Query q = gDAO.createQuery("Select e from VehicleMake e where e.name = :name");
							q.setParameter("name", vehicleMaker.trim());
							Object qObj = gDAO.search(q, 0);
							if(qObj != null)
							{
								Vector<VehicleMake> vmList = (Vector<VehicleMake>)qObj;
								for(VehicleMake e : vmList)
									vm = e;
							}
							if(vm == null && isAutoCreate())
							{
								vm = new VehicleMake();
								vm.setCreatedBy(dashBean.getUser());
								vm.setCrt_dt(new Date());
								vm.setName(vehicleMaker.trim());
								vm.setPartner(getPartner());
								ret = gDAO.save(vm);
								if(!ret)
									break;
								createModel = true;
							}
							else if(vm == null && !isAutoCreate())
							{
								ret = false;
								gDAO.setMessage("Vehicle Make: '" + vehicleMaker + "' does not exist for vehicle: " + regNo);
								break;
							}
						}
						
						if(vehicleModel != null && vehicleModel.trim().length() > 0)
						{
							if(createModel)
							{
								vmd = new VehicleModel();
								vmd.setCreatedBy(dashBean.getUser());
								vmd.setCrt_dt(new Date());
								vmd.setMaker(vm);
								vmd.setType(vt);
								vmd.setName(vehicleModel);
								vmd.setYear(modelYr);
								vmd.setPartner(getPartner());
								ret = gDAO.save(vmd);
								if(!ret)
									break;
							}
							else
							{
								Query q = gDAO.createQuery("Select e from VehicleModel e where e.name = :name and e.year=:year and e.maker=:maker and e.type=:type");
								q.setParameter("name", vehicleModel.trim());
								q.setParameter("year", modelYr);
								q.setParameter("maker", vm);
								q.setParameter("type", vt);
								Object qObj = gDAO.search(q, 0);
								if(qObj != null)
								{
									Vector<VehicleModel> vmList = (Vector<VehicleModel>)qObj;
									for(VehicleModel e : vmList)
										vmd = e;
								}
								if(vmd == null && isAutoCreate())
								{
									vmd = new VehicleModel();
									vmd.setCreatedBy(dashBean.getUser());
									vmd.setCrt_dt(new Date());
									vmd.setMaker(vm);
									vmd.setType(vt);
									vmd.setName(vehicleModel);
									vmd.setYear(modelYr);
									vmd.setPartner(getPartner());
									ret = gDAO.save(vmd);
									if(!ret)
										break;
								}
								else if(vmd == null && !isAutoCreate())
								{
									ret = false;
									gDAO.setMessage("Vehicle Model: '" + vehicleModel + "' does not exist for vehicle: " + regNo);
									break;
								}
							}
						}
						
						Vendor ven = null;
						if(purchased_from != null && purchased_from.trim().length() > 0)
						{
							Query q = gDAO.createQuery("Select e from Vendor e where e.name = :name and e.partner=:partner");
							q.setParameter("name", vehicleModel.trim());
							q.setParameter("partner", getPartner());
							Object qObj = gDAO.search(q, 0);
							if(qObj != null)
							{
								Vector<Vendor> vmList = (Vector<Vendor>)qObj;
								for(Vendor e : vmList)
									ven = e;
							}
							if(ven == null && isAutoCreate())
							{
								ven = new Vendor();
								ven.setName(purchased_from);
								ven.setPartner(getPartner());
								ven.setCreatedBy(dashBean.getUser());
								ven.setCrt_dt(new Date());
								ret = gDAO.save(ven);
								if(!ret)
									break;
								else
								{
									ServiceType st = null;
									q = gDAO.createQuery("Select e from ServiceType e where e.name = :name");
									q.setParameter("name", "Vehicle Sales");
									qObj = gDAO.search(q, 0);
									if(qObj != null)
									{
										Vector<ServiceType> vmList = (Vector<ServiceType>)qObj;
										for(ServiceType e : vmList)
											st = e;
									}
									if(st != null)
									{
										VendorServices vs = new VendorServices();
										vs.setCreatedBy(dashBean.getUser());
										vs.setCrt_dt(new Date());
										vs.setServiceType(st);
										vs.setVendor(ven);
										ret = gDAO.save(vs);
										if(!ret)
											break;
									}
								}
							}
						}
						
						if(fleet != null && vt != null && vm != null && vmd != null)
						{
							Vehicle v = new Vehicle();
							v.setFleet(fleet);
							v.setActive(true);
							v.setActiveStatus(VehicleStatusEnum.ACTIVE.getStatus());
							v.setChasisNo(chassisNo);
							v.setCreatedBy(dashBean.getUser());
							v.setCrt_dt(new Date());
							v.setEngineNo(engineNo);
							v.setModel(vmd);
							v.setPartner(getPartner());
							v.setRegistrationNo(regNo);
							try
							{
								v.setZonControlId(Integer.parseInt(trackerID));
							} catch(Exception ex){}
							try
							{
								v.setPurchaseAmt(new BigDecimal(Double.parseDouble(purchased_amount)));
							} catch(Exception ex){}
							try
							{
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
								v.setPurchaseDate(sdf.parse(purchase_date));
							} catch(Exception ex){}
							v.setVendor(ven);
							ret = gDAO.save(v);
							if(!ret)
								break;
							else if(regionObj != null || deptObj != null)
							{
								VehicleParameters vpm = new VehicleParameters();
								vpm.setRegion(regionObj);
								vpm.setDept(deptObj);
								vpm.setCalibratedcapacity(calibrated_capacity);
								vpm.setCardno(card_no);
								vpm.setColor(color);
								vpm.setSimno(sim_no);
								vpm.setTankcapacity(tank_capacity);
								vpm.setTyresize(tyre_size);
								vpm.setUnitofmeasure(unit_of_measurement);
								
								if(fuel_type != null && fuel_type.trim().length() > 0)
								{
									Query q = gDAO.createQuery("Select e from FuelType e where e.name = :name");
									q.setParameter("name", fuel_type);
									Object qObj = gDAO.search(q, 0);
									if(qObj != null)
									{
										Vector<FuelType> vmList = (Vector<FuelType>)qObj;
										for(FuelType e : vmList)
											vpm.setFuelType(e);
									}
								}
								vpm.setCreatedBy(dashBean.getUser());
								vpm.setCrt_dt(new Date());
								vpm.setVehicle(v);
								ret = gDAO.save(vpm);
								if(!ret)
									break;
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
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "All vehicles created successfully.");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					
					setVehicleMakes(null);
					setVehicleTypes(null);
					setVmodels(null);
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create all models. " + gDAO.getMessage());
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
			}
		}
	}
	
	public void newVehicleParameter()
	{
		if(getSelectedVehicle() != null && getSelectedVehicle().getId() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			getVehicleParam().setCreatedBy(dashBean.getUser());
			getVehicleParam().setCrt_dt(new Date());
			getVehicleParam().setVehicle(getSelectedVehicle());
			
			if(getDepartment_id() != null)
			{
				Object dpt = gDAO.find(Department.class, getDepartment_id());
				if(dpt != null)
					getVehicleParam().setDept((Department)dpt);
			}
			if(getRegion_id() != null)
			{
				Object rg = gDAO.find(Region.class, getRegion_id());
				if(rg != null)
					getVehicleParam().setRegion((Region)rg);
			}
			if(getFuelType_id() != null)
			{
				Object rg = gDAO.find(FuelType.class, getFuelType_id());
				if(rg != null)
					getVehicleParam().setFuelType((FuelType)rg);
			}
			if(getVehiclePhoto() != null)
			{
				getVehicleParam().setPhoto(getVehiclePhoto().getContents());
			}
			
			gDAO.startTransaction();
			if(gDAO.save(getVehicleParam()))
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Vehicle created successfully.");
				
				setVehicleParam(null);
				setSelectedVehicle(null);
				setVehicles(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create new vehicle parameter. " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void saveVehicle()
	{
		if(getVehicle().getRegistrationNo() != null
				&& getVehicleModel_id() != null && getFleet_id() != null)
		{
			if(!isLicenseAvailable())
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "You have used up your license. Please purchase more license to add more vehicles!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Object modelObj = gDAO.find(VehicleModel.class, getVehicleModel_id());
			if(modelObj != null)
				getVehicle().setModel((VehicleModel)modelObj);
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid model selected!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			
			if(getAssignee_id() != null && getAssignee_id() > 0L)
			{
				Object assigneeObj = gDAO.find(PartnerPersonel.class, getAssignee_id());
				if(assigneeObj != null)
					getVehicle().setAssignee((PartnerPersonel)assigneeObj);
			}
			
			Object fleetObj = gDAO.find(Fleet.class, getFleet_id());
			if(fleetObj != null)
			{
				getVehicle().setFleet((Fleet)fleetObj);
				if(getVehicle().getFleet().getVehicleMake() != null)
				{
					// check the selected make to ensure its allowed in this fleet
					if(getVehicle().getModel().getMaker().getId() == getVehicle().getFleet().getVehicleMake().getId());
					else
					{
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Vehicles of maker '" + getVehicle().getModel().getMaker().getName() + "' is not allowed in the selected fleet!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
						return;
					}
				}
				
				if(getVehicle().getFleet().getVehicleType() != null)
				{
					// check the selected make to ensure its allowed in this fleet
					if(getVehicle().getModel().getType().getId() == getVehicle().getFleet().getVehicleType().getId());
					else
					{
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Vehicles of type '" + getVehicle().getModel().getType().getName() + "' is not allowed in the selected fleet!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
						return;
					}
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Invalid fleet selected!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			
			if(getVendor_id() != null)
			{
				Object vendorObj = gDAO.find(Vendor.class, getVendor_id());
				if(vendorObj != null)
					getVehicle().setVendor((Vendor)vendorObj);
			}
			
			getVehicle().setActive(true);
			getVehicle().setActiveStatus(VehicleStatusEnum.ACTIVE.getStatus());
			getVehicle().setCreatedBy(dashBean.getUser());
			getVehicle().setCrt_dt(new Date());
			getVehicle().setPartner(getPartner());
			
			gDAO.startTransaction();
			boolean ret = gDAO.save(getVehicle());
			if(ret)
			{
				getVehicleParam().setCreatedBy(dashBean.getUser());
				getVehicleParam().setCrt_dt(new Date());
				getVehicleParam().setVehicle(getVehicle());
				
				if(getDepartment_id() != null)
				{
					Object dpt = gDAO.find(Department.class, getDepartment_id());
					if(dpt != null)
						getVehicleParam().setDept((Department)dpt);
				}
				if(getRegion_id() != null)
				{
					Object rg = gDAO.find(Region.class, getRegion_id());
					if(rg != null)
						getVehicleParam().setRegion((Region)rg);
				}
				if(getFuelType_id() != null)
				{
					Object rg = gDAO.find(FuelType.class, getFuelType_id());
					if(rg != null)
						getVehicleParam().setFuelType((FuelType)rg);
				}
				if(getVehiclePhoto() != null)
				{
					getVehicleParam().setPhoto(getVehiclePhoto().getContents());
				}
				
				gDAO.save(getVehicleParam());
				
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Vehicle created successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setVehiclePhoto(null);
				setFuelType_id(null);
				setDepartment_id(null);
				setRegion_id(null);
				setVehicleParam(null);
				setVehicle(null);
				setVehicles(null);
				setFleets(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create vehicle. " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void BatchLoadVModels()
	{
		if(getModelsBatchExcel() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			try
			{
				ByteArrayInputStream byteIn = new ByteArrayInputStream(getModelsBatchExcel().getContents());
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
					String model_nm = "", vehicleType = "", vehicleMaker = "";
					String model_yr = "";
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
							if(cell.getColumnIndex() == 0)
								model_nm = val;
							else if(cell.getColumnIndex() == 1)
								model_yr = val;
							else if(cell.getColumnIndex() == 2)
								vehicleType = val;
							else if(cell.getColumnIndex() == 3)
								vehicleMaker = val;
						}
						
						VehicleType vt = null;
						VehicleMake vm = null;
						VehicleModel vmd = new VehicleModel();
						
						// search for existing vehicle type and maker
						Query q = gDAO.createQuery("Select e from VehicleType e where e.name = :name");
						q.setParameter("name", vehicleType.trim());
						Object qObj = gDAO.search(q, 0);
						if(qObj != null)
						{
							Vector<VehicleType> vtList = (Vector<VehicleType>)qObj;
							for(VehicleType e : vtList)
								vt = e;
						}
						if(vt == null && isAutoCreate())
						{
							vt = new VehicleType();
							vt.setCreatedBy(dashBean.getUser());
							vt.setCrt_dt(new Date());
							vt.setName(vehicleType.trim());
						}
						q = gDAO.createQuery("Select e from VehicleMake e where e.name = :name");
						q.setParameter("name", vehicleMaker.trim());
						qObj = gDAO.search(q, 0);
						if(qObj != null)
						{
							Vector<VehicleMake> vmList = (Vector<VehicleMake>)qObj;
							for(VehicleMake e : vmList)
								vm = e;
						}
						if(vm == null && isAutoCreate())
						{
							vm = new VehicleMake();
							vm.setCreatedBy(dashBean.getUser());
							vm.setCrt_dt(new Date());
							vm.setName(vehicleMaker.trim());
						}
						
						vmd.setType(vt);
						vmd.setMaker(vm);
						vmd.setCreatedBy(dashBean.getUser());
						vmd.setCrt_dt(new Date());
						vmd.setName(model_nm);
						vmd.setYear(model_yr);
						ret = gDAO.save(vmd);
						if(!ret)
							break;
					}
					else
						pos += 1;
				}
				if(ret)
				{
					gDAO.commit();
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "All models created successfully.");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					
					setVehicleMakes(null);
					setVehicleTypes(null);
					setVmodels(null);
				}
				else
				{
					gDAO.rollback();
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create all models. " + gDAO.getMessage());
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
			}
		}
	}
	
	public void save(int i)
	{
		GeneralDAO gDAO = new GeneralDAO();
		boolean ret = false;
		boolean validated = false;
		switch(i)
		{
			case 1: // vehicle type
			{
				if(getVehicleType().getName() != null && getPartner() != null)
				{
					getVehicleType().setPartner(getPartner());
					getVehicleType().setCreatedBy(dashBean.getUser());
					getVehicleType().setCrt_dt(new Date());
					gDAO.startTransaction();
					ret = gDAO.save(getVehicleType());
					if(ret)
					{
						setVehicleType(null);
						setVehicleTypes(null);
					}
					validated = true;
				}
				break;
			}
			case 2: // vehicle make
			{
				if(getVehicleMake().getName() != null && getPartner() != null)
				{
					getVehicleMake().setPartner(getPartner());
					getVehicleMake().setCreatedBy(dashBean.getUser());
					getVehicleMake().setCrt_dt(new Date());
					gDAO.startTransaction();
					ret = gDAO.save(getVehicleMake());
					if(ret)
					{
						setVehicleMake(null);
						setVehicleMakes(null);
					}
					validated = true;
				}
				break;
			}
			case 3: // vehicle models
			{
				if(getVmodel().getName() != null && getVehicleType_id() != null && getVehicleMake_id() != null && getPartner() != null)
				{
					getVmodel().setPartner(getPartner());
					getVmodel().setCreatedBy(dashBean.getUser());
					getVmodel().setCrt_dt(new Date());
					
					if(getVehicleMake_id() > 0)
					{
						Object obj = gDAO.find(VehicleMake.class, getVehicleMake_id());
						if(obj != null)
						{
							getVmodel().setMaker((VehicleMake)obj);
						}
					}
					if(getVehicleType_id() > 0)
					{
						Object obj = gDAO.find(VehicleType.class, getVehicleType_id());
						if(obj != null)
						{
							getVmodel().setType((VehicleType)obj);
						}
					}
					gDAO.startTransaction();
					ret = gDAO.save(getVmodel());
					if(ret)
					{
						setVmodel(null);
						setVmodels(null);
					}
					validated = true;
				}
				break;
			}
			case 4: // vehicle standard routine maintenance
			{
				if(getVsrm().getDescription() != null && getVsrm().getOdometer() != null && getVehicleModel_id() != null)
				{
					getVsrm().setCreatedBy(dashBean.getUser());
					getVsrm().setCrt_dt(new Date());
					if(getVehicleModel_id() > 0)
					{
						Object obj = gDAO.find(VehicleModel.class, getVehicleModel_id());
						if(obj != null)
						{
							getVsrm().setModel((VehicleModel)obj);
						}
					}
					gDAO.startTransaction();
					ret = gDAO.save(getVsrm());
					if(ret)
					{
						setVsrm(null);
						setVsrmList(null);
					}
					validated = true;
				}
				
				break;
			}
			case 5: // batch load VSRM
			{
				if(getAllvmodels() != null && getAllvmodels().size() > 0)
				{
					int count = 0;
					Vector<VehicleModel> selModels = new Vector<VehicleModel>();
					for(VehicleModel e : getAllvmodels())
					{
						if(e.isSelected())
						{
							count++;
							selModels.add(e);
						}
					}
					if(count == 0)
					{
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No vehicle model selected!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else
					{
						// Now read the content from excel
						if(getModelsVSRMBatchExcel() != null)
						{
							try
							{
								ByteArrayInputStream byteIn = new ByteArrayInputStream(getModelsVSRMBatchExcel().getContents());
								HSSFWorkbook workbook = new HSSFWorkbook(byteIn);
								HSSFSheet sheet = workbook.getSheetAt(0);
								
								//Get iterator to all the rows in current sheet starting from row 2
								Iterator<Row> rowIterator = sheet.iterator();
								int pos = 1;
								
								gDAO.startTransaction();
								while(rowIterator.hasNext())
								{
									Row row = rowIterator.next();
									String odometer = "", maintenance = "";
									if(pos > 1)
									{
										validated = true;
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
											if(cell.getColumnIndex() == 0)
												odometer = val;
											else if(cell.getColumnIndex() == 1)
												maintenance = val;
										}
										
										for(VehicleModel e : selModels)
										{
											VehicleStandardRM vsrm = new VehicleStandardRM();
											vsrm.setCreatedBy(dashBean.getUser());
											vsrm.setCrt_dt(new Date());
											vsrm.setDescription(maintenance);
											vsrm.setModel(e);
											vsrm.setOdometer(new BigDecimal(Long.parseLong(odometer)));
											ret = gDAO.save(vsrm);
										}
									}
								}
							}
							catch(Exception ex)
							{
								ex.printStackTrace();
								msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Severe error occured. " + ex.getMessage());
								FacesContext.getCurrentInstance().addMessage(null, msg);
							}
						}
					}
				}
				else
				{
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No vehicle model available!");
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
	
	public void saveFleet()
	{
		if(getFleet().getName() != null)
		{
			getFleet().setDefaultFleet(false);
			getFleet().setCreatedBy(dashBean.getUser());
			getFleet().setCrt_dt(new Date());
			getFleet().setPartner(getPartner());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			if(getVehicleMake_id() > 0)
			{
				Object obj = gDAO.find(VehicleMake.class, getVehicleMake_id());
				if(obj != null)
				{
					getFleet().setVehicleMake((VehicleMake)obj);
				}
			}
			if(getVehicleType_id() > 0)
			{
				Object obj = gDAO.find(VehicleType.class, getVehicleType_id());
				if(obj != null)
				{
					getFleet().setVehicleType((VehicleType)obj);
				}
			}
			
			gDAO.startTransaction();
			boolean ret = gDAO.save(getFleet());
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Fleet created successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setFleet(null);
				setFleets(null);
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
	
	public Long getPartner_id() {
		return partner_id;
	}

	public void setPartner_id(Long partner_id) {
		this.partner_id = partner_id;
	}

	public Long getVehicleType_id() {
		return vehicleType_id;
	}

	public void setVehicleType_id(Long vehicleType_id) {
		this.vehicleType_id = vehicleType_id;
	}

	public Long getVehicleMake_id() {
		return vehicleMake_id;
	}

	public void setVehicleMake_id(Long vehicleMake_id) {
		this.vehicleMake_id = vehicleMake_id;
	}

	public VehicleType getVehicleType() {
		if(vehicleType == null)
			vehicleType = new VehicleType();
		return vehicleType;
	}

	public void setVehicleType(VehicleType vehicleType) {
		this.vehicleType = vehicleType;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleType> getVehicleTypes() {
		boolean research = true;
		if(vehicleTypes == null || vehicleTypes.size() == 0)
			research = true;
		else if(vehicleTypes.size() > 0)
		{
			if(vehicleTypes.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
				research = false;
		}
		if(research && getPartner() != null)
		{
			vehicleTypes = null;
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getPartner());
			Object dpsObj = gDAO.search("VehicleType", params);
			if(dpsObj != null)
			{
				vehicleTypes = (Vector<VehicleType>)dpsObj;
				for(VehicleType e : vehicleTypes)
				{
					params = new Hashtable<String, Object>();
					params.put("model.type", e);
					Object objs = gDAO.search("Vehicle", params);
					if(objs != null)
						e.setVehicles((Vector<Vehicle>)objs);
				}
			}
		}
		return vehicleTypes;
	}

	public void setVehicleTypes(Vector<VehicleType> vehicleTypes) {
		this.vehicleTypes = vehicleTypes;
	}

	public VehicleMake getVehicleMake() {
		if(vehicleMake == null)
			vehicleMake = new VehicleMake();
		return vehicleMake;
	}

	public void setVehicleMake(VehicleMake vehicleMake) {
		this.vehicleMake = vehicleMake;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleMake> getVehicleMakes() {
		boolean research = true;
		if(vehicleMakes == null || vehicleMakes.size() == 0)
			research = true;
		else if(vehicleMakes.size() > 0)
		{
			if(vehicleMakes.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
				research = false;
		}
		if(research && getPartner() != null)
		{
			vehicleMakes = null;
			GeneralDAO gDAO = new GeneralDAO();
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getPartner());
			Object dpsObj = gDAO.search("VehicleMake", params);
			if(dpsObj != null)
			{
				vehicleMakes = (Vector<VehicleMake>)dpsObj;
				for(VehicleMake e : vehicleMakes)
				{
					params = new Hashtable<String, Object>();
					params.put("model.maker", e);
					Object objs = gDAO.search("Vehicle", params);
					if(objs != null)
						e.setVehicles((Vector<Vehicle>)objs);
				}
			}
		}
		return vehicleMakes;
	}

	public void setVehicleMakes(Vector<VehicleMake> vehicleMakes) {
		this.vehicleMakes = vehicleMakes;
	}

	public VehicleModel getVmodel() {
		if(vmodel == null)
			vmodel = new VehicleModel();
		return vmodel;
	}

	public void setVmodel(VehicleModel vmodel) {
		this.vmodel = vmodel;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleModel> getVmodels() {
		boolean research = true;
		if(vmodels == null || vmodels.size() == 0)
			research = true;
		else if(vmodels.size() > 0)
		{
			if(vmodels.get(0).getMaker().getId() == getVehicleMake_id() && vmodels.get(0).getType().getId() == getVehicleType_id())
				research = false;
		}
		if(research && getPartner() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			if(getVehicleMake_id() != null)
			{
				Object obj = gDAO.find(VehicleMake.class, getVehicleMake_id());
				if(obj != null)
					params.put("maker", (VehicleMake)obj);
			}
			if(getVehicleType_id() != null)
			{
				Object obj = gDAO.find(VehicleType.class, getVehicleType_id());
				if(obj != null)
					params.put("type", (VehicleType)obj);
			}
			params.put("partner", getPartner());
			Object foundObjs = gDAO.search("VehicleModel", params);
			if(foundObjs != null)
			{
				vmodels = (Vector<VehicleModel>)foundObjs;
				for(VehicleModel e : vmodels)
				{
					params = new Hashtable<String, Object>();
					params.put("model", e);
					Object objs = gDAO.search("Vehicle", params);
					if(objs != null)
						e.setVehicles((Vector<Vehicle>)objs);
				}
			}
		}
		return vmodels;
	}

	public void setVmodels(Vector<VehicleModel> vmodels) {
		this.vmodels = vmodels;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleModel> getAllvmodels() {
		boolean research = true;
		if(allvmodels == null || allvmodels.size() == 0)
			research = true;
		else if(allvmodels.size() > 0)
		{
			if(allvmodels.get(0).getMaker().getId() == getVehicleMake_id() && allvmodels.get(0).getType().getId() == getVehicleType_id())
				research = false;
		}
		if(research)
		{
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				Object foundObjs = gDAO.search("VehicleModel", params);
				if(foundObjs != null)
				{
					allvmodels = (Vector<VehicleModel>)foundObjs;
					for(VehicleModel e : allvmodels)
					{
						params = new Hashtable<String, Object>();
						params.put("model", e);
						Object objs = gDAO.search("Vehicle", params);
						if(objs != null)
							e.setVehicles((Vector<Vehicle>)objs);
					}
				}
			}
		}
		return allvmodels;
	}

	public void setAllvmodels(Vector<VehicleModel> allvmodels) {
		this.allvmodels = allvmodels;
	}

	public StreamedContent getVmodelsExcelTemplate() {
		return vmodelsExcelTemplate;
	}

	public UploadedFile getModelsBatchExcel() {
		return modelsBatchExcel;
	}

	public void setModelsBatchExcel(UploadedFile modelsBatchExcel) {
		this.modelsBatchExcel = modelsBatchExcel;
	}

	public boolean isAutoCreate() {
		return autoCreate;
	}

	public void setAutoCreate(boolean autoCreate) {
		this.autoCreate = autoCreate;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}

	public Fleet getFleet() {
		if(fleet == null)
			fleet = new Fleet();
		return fleet;
	}

	public void setFleet(Fleet fleet) {
		this.fleet = fleet;
	}
	
	public void resetFleets()
	{
		setFleets(null);
	}

	@SuppressWarnings("unchecked")
	public Vector<Fleet> getFleets() {
		boolean research = true;
		if(fleets == null || fleets.size() == 0)
			research = true;
		else if(fleets.size() > 0)
		{
			if(getPartner() != null)
			{
				if(fleets.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
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
					for(Fleet f : fleets)
					{
						params = new Hashtable<String, Object>();
						params.put("fleet", f);
						Object vhsObj = gDAO.search("Vehicle", params);
						if(vhsObj != null)
						{
							f.setVehicles((Vector<Vehicle>)vhsObj);
						}
					}
				}
			}
		}
		return fleets;
	}

	public void setFleets(Vector<Fleet> fleets) {
		this.fleets = fleets;
	}

	public Long getFleet_id() {
		return fleet_id;
	}

	public void setFleet_id(Long fleet_id) {
		this.fleet_id = fleet_id;
	}

	public Long getVehicleModel_id() {
		return vehicleModel_id;
	}

	public void setVehicleModel_id(Long vehicleModel_id) {
		this.vehicleModel_id = vehicleModel_id;
	}

	public Long getVendor_id() {
		return vendor_id;
	}

	public void setVendor_id(Long vendor_id) {
		this.vendor_id = vendor_id;
	}

	public Long getDepartment_id() {
		return department_id;
	}

	public void setDepartment_id(Long department_id) {
		this.department_id = department_id;
	}

	public Long getRegion_id() {
		return region_id;
	}

	public void setRegion_id(Long region_id) {
		this.region_id = region_id;
	}

	public Long getFuelType_id() {
		return fuelType_id;
	}

	public void setFuelType_id(Long fuelType_id) {
		this.fuelType_id = fuelType_id;
	}

	public Long getAssignee_id() {
		return assignee_id;
	}

	public void setAssignee_id(Long assignee_id) {
		this.assignee_id = assignee_id;
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

	@SuppressWarnings("unchecked")
	public Vector<FuelType> getFuelTypes() {
		if(fuelTypes == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Object fts = gDAO.findAll("FuelType");
			if(fts != null)
				fuelTypes = (Vector<FuelType>)fts;
		}
		return fuelTypes;
	}

	public void setFuelTypes(Vector<FuelType> fuelTypes) {
		this.fuelTypes = fuelTypes;
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

	public UploadedFile getVehiclesBatchExcel() {
		return vehiclesBatchExcel;
	}

	public void setVehiclesBatchExcel(UploadedFile vehiclesBatchExcel) {
		this.vehiclesBatchExcel = vehiclesBatchExcel;
	}

	public StreamedContent getVehiclesExcelTemplate() {
		return vehiclesExcelTemplate;
	}

	public UploadedFile getVehiclePhoto() {
		return vehiclePhoto;
	}

	public void setVehiclePhoto(UploadedFile vehiclePhoto) {
		this.vehiclePhoto = vehiclePhoto;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vendor> getVsalesVendors() {
		boolean research = true;
		if(vsalesVendors == null || vsalesVendors.size() == 0)
			research = true;
		else if(vsalesVendors.size() > 0)
		{
			if(getPartner() != null)
			{
				if(vsalesVendors.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			vsalesVendors = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Object stypes = gDAO.findAll("ServiceType");
				if(stypes != null)
				{
					Vector<ServiceType> stypesList = (Vector<ServiceType>)stypes;
					ServiceType st = null;
					for(ServiceType e : stypesList)
					{
						if(e.getName().trim().equalsIgnoreCase("vehicle sales"))
						{
							st = e;
							break;
						}
					}
					if(st != null)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("vendor.partner", getPartner());
						params.put("serviceType", st);
						Object dpsObj = gDAO.search("VendorServices", params);
						if(dpsObj != null)
						{
							Vector<VendorServices> vss = (Vector<VendorServices>)dpsObj;
							vsalesVendors = new Vector<Vendor>();
							for(VendorServices vs : vss)
							{
								vsalesVendors.add(vs.getVendor());
							}
						}
					}
				}
			}
		}
		return vsalesVendors;
	}

	public void setVsalesVendors(Vector<Vendor> vsalesVendors) {
		this.vsalesVendors = vsalesVendors;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vendor> getVserviceVendors() {
		boolean research = true;
		if(vserviceVendors == null || vserviceVendors.size() == 0)
			research = true;
		else if(vserviceVendors.size() > 0)
		{
			if(getPartner() != null)
			{
				if(vserviceVendors.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			vserviceVendors = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Object stypes = gDAO.findAll("ServiceType");
				if(stypes != null)
				{
					Vector<ServiceType> stypesList = (Vector<ServiceType>)stypes;
					ServiceType st = null;
					for(ServiceType e : stypesList)
					{
						if(e.getName().trim().equalsIgnoreCase("vehicle servicing"))
						{
							st = e;
							break;
						}
					}
					if(st != null)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("vendor.partner", getPartner());
						params.put("serviceType", st);
						Object dpsObj = gDAO.search("VendorServices", params);
						if(dpsObj != null)
						{
							Vector<VendorServices> vss = (Vector<VendorServices>)dpsObj;
							vserviceVendors = new Vector<Vendor>();
							for(VendorServices vs : vss)
							{
								vserviceVendors.add(vs.getVendor());
							}
						}
					}
				}
			}
		}
		return vserviceVendors;
	}

	public void setVserviceVendors(Vector<Vendor> vserviceVendors) {
		this.vserviceVendors = vserviceVendors;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vendor> getVrepairVendors() {
		boolean research = true;
		if(vrepairVendors == null || vrepairVendors.size() == 0)
			research = true;
		else if(vrepairVendors.size() > 0)
		{
			if(getPartner() != null)
			{
				if(vrepairVendors.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			vrepairVendors = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Object stypes = gDAO.findAll("ServiceType");
				if(stypes != null)
				{
					Vector<ServiceType> stypesList = (Vector<ServiceType>)stypes;
					ServiceType st = null;
					for(ServiceType e : stypesList)
					{
						if(e.getName().trim().equalsIgnoreCase("vehicle repair"))
						{
							st = e;
							break;
						}
					}
					if(st != null)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("vendor.partner", getPartner());
						params.put("serviceType", st);
						Object dpsObj = gDAO.search("VendorServices", params);
						if(dpsObj != null)
						{
							Vector<VendorServices> vss = (Vector<VendorServices>)dpsObj;
							vrepairVendors = new Vector<Vendor>();
							for(VendorServices vs : vss)
							{
								vrepairVendors.add(vs.getVendor());
							}
						}
					}
				}
			}
		}
		return vrepairVendors;
	}

	public void setVrepairVendors(Vector<Vendor> vrepairVendors) {
		this.vrepairVendors = vrepairVendors;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vendor> getVinsuranceVendors() {
		boolean research = true;
		if(vinsuranceVendors == null || vinsuranceVendors.size() == 0)
			research = true;
		else if(vinsuranceVendors.size() > 0)
		{
			if(getPartner() != null)
			{
				if(vinsuranceVendors.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			vinsuranceVendors = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Object stypes = gDAO.findAll("ServiceType");
				if(stypes != null)
				{
					Vector<ServiceType> stypesList = (Vector<ServiceType>)stypes;
					ServiceType st = null;
					for(ServiceType e : stypesList)
					{
						if(e.getName().trim().equalsIgnoreCase("insurance"))
						{
							st = e;
							break;
						}
					}
					if(st != null)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("vendor.partner", getPartner());
						params.put("serviceType", st);
						Object dpsObj = gDAO.search("VendorServices", params);
						if(dpsObj != null)
						{
							Vector<VendorServices> vss = (Vector<VendorServices>)dpsObj;
							vinsuranceVendors = new Vector<Vendor>();
							for(VendorServices vs : vss)
							{
								vinsuranceVendors.add(vs.getVendor());
							}
						}
					}
				}
			}
		}
		return vinsuranceVendors;
	}

	public void setVinsuranceVendors(Vector<Vendor> vinsuranceVendors) {
		this.vinsuranceVendors = vinsuranceVendors;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vendor> getVlicensesVendors() {
		boolean research = true;
		if(vlicensesVendors == null || vlicensesVendors.size() == 0)
			research = true;
		else if(vlicensesVendors.size() > 0)
		{
			if(getPartner() != null)
			{
				if(vlicensesVendors.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			vlicensesVendors = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Object stypes = gDAO.findAll("ServiceType");
				if(stypes != null)
				{
					Vector<ServiceType> stypesList = (Vector<ServiceType>)stypes;
					ServiceType st = null, stvl = null;
					for(ServiceType e : stypesList)
					{
						if(e.getName().trim().equalsIgnoreCase("insurance"))
						{
							st = e;
							if(stvl != null)
								break;
						}
						else if(e.getName().trim().equalsIgnoreCase("vehicle license"))
						{
							stvl = e;
							if(st != null)
								break;
						}
					}
					if(st != null && stvl != null)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("vendor.partner", getPartner());
						params.put("serviceType", st);
						Object dpsObj = gDAO.search("VendorServices", params);
						if(dpsObj != null)
						{
							Vector<VendorServices> vss = (Vector<VendorServices>)dpsObj;
							vlicensesVendors = new Vector<Vendor>();
							for(VendorServices vs : vss)
							{
								vlicensesVendors.add(vs.getVendor());
							}
						}
						
						params = new Hashtable<String, Object>();
						params.put("vendor.partner", getPartner());
						params.put("serviceType", stvl);
						dpsObj = gDAO.search("VendorServices", params);
						if(dpsObj != null)
						{
							Vector<VendorServices> vss = (Vector<VendorServices>)dpsObj;
							if(vlicensesVendors == null)
								vlicensesVendors = new Vector<Vendor>();
							for(VendorServices vs : vss)
							{
								Vendor vd = vs.getVendor();
								boolean exists = false;
								for(Vendor v : vlicensesVendors)
								{
									if(v.getId() == vd.getId())
									{
										exists = true;
										break;
									}
								}
								if(!exists)
									vlicensesVendors.add(vd);
							}
						}
					}
				}
			}
		}
		return vlicensesVendors;
	}

	public void setVlicensesVendors(Vector<Vendor> vlicensesVendors) {
		this.vlicensesVendors = vlicensesVendors;
	}

	public Vehicle getVehicle() {
		if(vehicle == null)
			vehicle = new Vehicle();
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public Vehicle getSelectedVehicle() {
		if(selectedVehicle == null)
			selectedVehicle = new Vehicle();
		return selectedVehicle;
	}

	public void setSelectedVehicle(Vehicle selectedVehicle) {
		this.selectedVehicle = selectedVehicle;
	}

	public VehicleParameters getVehicleParam() {
		if(vehicleParam == null)
			vehicleParam = new VehicleParameters();
		return vehicleParam;
	}

	public void setVehicleParam(VehicleParameters vehicleParam) {
		this.vehicleParam = vehicleParam;
	}

	public void resetVehicles()
	{
		setVehicles(null);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Vehicle> getVehicles() {
		if(vehicles == null)
		{
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				String qry = "Select e from Vehicle e where e.partner = :partner";
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				//params.put("partner", getPartner());
				
				Fleet f = null;
				if(getFleet_id() != null)
				{
					Object fobj = gDAO.find(Fleet.class, getFleet_id());
					if(fobj != null)
					{
						f = (Fleet)fobj;
						//params.put("fleet", (Fleet)fobj);
						qry += " and e.fleet=:fleet";
					}
				}
				
				qry += " order by e.registrationNo";
				Query q = gDAO.createQuery(qry);
				q.setParameter("partner", getPartner());
				if(f != null)
					q.setParameter("fleet", f);
				
				Object vhsObj = gDAO.search(q, 0); //.search("Vehicle", params);
				if(vhsObj != null)
				{
					vehicles = (Vector<Vehicle>)vhsObj;
					for(Vehicle v : vehicles)
					{
						params = new Hashtable<String, Object>();
						params.put("vehicle", v);
						Object vDrvs = gDAO.search("VehicleDriver", params);
						if(vDrvs != null)
						{
							v.setDrivers((Vector<VehicleDriver>)vDrvs);
							for(VehicleDriver vd : v.getDrivers())
							{
								if(vd.isActive())
								{
									v.setCurrentDriver(vd);
									break;
								}
							}
						}
						params = new Hashtable<String, Object>();
						params.put("vehicle", v);
						Object vParams = gDAO.search("VehicleParameters", params);
						if(vParams != null)
						{
							Vector<VehicleParameters> vParamsList = (Vector<VehicleParameters>)vParams;
							v.setParams(vParamsList);
						}
						params = new Hashtable<String, Object>();
						params.put("vehicle", v);
						Object vLicParams = gDAO.search("VehicleLicense", params);
						if(vLicParams != null)
						{
							Vector<VehicleLicense> vLicParamsList = (Vector<VehicleLicense>)vLicParams;
							for(VehicleLicense vlic : vLicParamsList)
							{
								if(vlic.getLicType().getName().equalsIgnoreCase("Vehicle License"))
									v.setLast_lic(vlic);
								else if(vlic.getLicType().getName().indexOf("Insurance")>=0)
									v.setLast_insur(vlic);
							}
						}
						params = new Hashtable<String, Object>();
						params.put("vehicle", v);
						Object vRMSParams = gDAO.search("VehicleRoutineMaintenanceSetup", params);
						if(vRMSParams != null)
						{
							Vector<VehicleRoutineMaintenanceSetup> vLicParamsList = (Vector<VehicleRoutineMaintenanceSetup>)vRMSParams;
							for(VehicleRoutineMaintenanceSetup vlic : vLicParamsList)
							{
								v.setMaint_odometer(vlic.getOdometer());
							}
						}
						params = new Hashtable<String, Object>();
						params.put("vehicle", v);
						Object vRMParams = gDAO.search("VehicleRoutineMaintenance", params);
						if(vRMParams != null)
						{
							Vector<VehicleRoutineMaintenance> vLicParamsList = (Vector<VehicleRoutineMaintenance>)vRMParams;
							for(VehicleRoutineMaintenance vlic : vLicParamsList)
							{
								v.setLast_rout_maint(vlic);
							}
						}
					}
				}
			}
		}
		return vehicles;
	}

	public void setVehicles(Vector<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public Long getDriver_id() {
		return driver_id;
	}

	public void setDriver_id(Long driver_id) {
		this.driver_id = driver_id;
	}

	@SuppressWarnings("unchecked")
	public Vector<PartnerDriver> getPartnerDrivers() {
		boolean research = true;
		if(partnerDrivers == null || partnerDrivers.size() == 0)
			research = true;
		else if(partnerDrivers.size() > 0)
		{
			if(getPartner() != null)
			{
				if(partnerDrivers.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			partnerDrivers = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				params.put("active", true);
				
				Object drvs = gDAO.search("PartnerDriver", params);
				if(drvs != null)
				{
					partnerDrivers = (Vector<PartnerDriver>)drvs;
				}
			}
		}
		return partnerDrivers;
	}

	public void setPartnerDrivers(Vector<PartnerDriver> partnerDrivers) {
		this.partnerDrivers = partnerDrivers;
	}

	public Long getVehicle_id() {
		return vehicle_id;
	}

	public void setVehicle_id(Long vehicle_id) {
		this.vehicle_id = vehicle_id;
	}

	public String getRegNo() {
		return regNo;
	}

	public void setRegNo(String regNo) {
		this.regNo = regNo;
	}

	public Date getStdt() {
		return stdt;
	}

	public void setStdt(Date stdt) {
		this.stdt = stdt;
	}

	public Date getEddt() {
		return eddt;
	}

	public void setEddt(Date eddt) {
		this.eddt = eddt;
	}

	@SuppressWarnings("unchecked")
	public String getApproximateSRM()
	{
		String ret = "";
		
		if(getInitVehOdometer() != null && getInitVehOdometer().doubleValue() > 0 && getVehicle_id() != null && getVehicle_id().longValue() > 0L)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Object vObj = gDAO.find(Vehicle.class, getVehicle_id());
			if(vObj != null)
			{
				Vehicle v = (Vehicle)vObj;
				Query q = gDAO.createQuery("Select e from VehicleStandardRM e where e.model = :model and ABS(e.odometer-:odometer) <= 2000");
				q.setParameter("model", v.getModel());
				q.setParameter("odometer", getInitVehOdometer());
				Object vsrmObj = gDAO.search(q, 0);
				if(vsrmObj != null)
				{
					Vector<VehicleStandardRM> vsrmList = (Vector<VehicleStandardRM>)vsrmObj;
					if(vsrmList.size() > 0)
						ret = vsrmList.get(0).getDescription();
				}
			}
			gDAO.destroy();
		}
		
		return ret;
	}
	
	public VehicleStandardRM getVsrm() {
		if(vsrm == null)
			vsrm = new VehicleStandardRM();
		return vsrm;
	}

	public void setVsrm(VehicleStandardRM vsrm) {
		this.vsrm = vsrm;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleStandardRM> getVsrmList() {
		if(vsrmList == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Query q = gDAO.createQuery("Select e from VehicleStandardRM e where e.createdBy.partner = :partner");
			q.setParameter("partner", getPartner());
			Object vsrmObj = gDAO.search(q, 0);
			if(vsrmObj != null)
				vsrmList = (Vector<VehicleStandardRM>)vsrmObj;
		}
		return vsrmList;
	}

	public void setVsrmList(Vector<VehicleStandardRM> vsrmList) {
		this.vsrmList = vsrmList;
	}

	public StreamedContent getVmodelsVSRMExcelTemplate() {
		return vmodelsVSRMExcelTemplate;
	}

	public UploadedFile getModelsVSRMBatchExcel() {
		return modelsVSRMBatchExcel;
	}

	public void setModelsVSRMBatchExcel(UploadedFile modelsVSRMBatchExcel) {
		this.modelsVSRMBatchExcel = modelsVSRMBatchExcel;
	}

	public WorkOrder getWorkOrder() {
		if(workOrder == null)
			workOrder = new WorkOrder();
		return workOrder;
	}

	public void setWorkOrder(WorkOrder workOrder) {
		this.workOrder = workOrder;
	}

	public WorkOrder getSelectedWorkOrder() {
		return selectedWorkOrder;
	}

	public void setSelectedWorkOrder(WorkOrder selectedWorkOrder) {
		this.selectedWorkOrder = selectedWorkOrder;
	}
	
	public void resetRountineWorkOrders()
	{
		setRountineWorkOrders(null);
	}
	@SuppressWarnings("unchecked")
	public Vector<WorkOrder> getRountineWorkOrders() {
		boolean research = true;
		if(rountineWorkOrders == null || rountineWorkOrders.size() == 0)
			research = true;
		else if(rountineWorkOrders.size() > 0)
		{
			if(getPartner() != null)
			{
				if(rountineWorkOrders.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			rountineWorkOrders = null;
			if(getPartner() != null && getStart_dt() != null && getEnd_dt() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				String str = "Select e from WorkOrder e where (e.crt_dt between :start_dt and :end_dt) and e.workOrderType=:workOrderType and e.status=:status and e.finished=:finished and e.partner=:partner";
				Query q = gDAO.createQuery(str);
				q.setParameter("partner", getPartner());
				q.setParameter("finished", isFinished());
				q.setParameter("status", getWorkOrderStatus());
				q.setParameter("workOrderType", "Routine");
				q.setParameter("start_dt", getStart_dt());
				q.setParameter("end_dt", getEnd_dt());
				Object objs = gDAO.search(q, 0);
				if(objs != null)
				{
					rountineWorkOrders = (Vector<WorkOrder>)objs;
					
					for(WorkOrder w : rountineWorkOrders)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("workOrder", w);
						
						Object wkv = gDAO.search("WorkOrderVehicle", params);
						if(wkv != null)
						{
							Vector<WorkOrderVehicle> vehicles = (Vector<WorkOrderVehicle>)wkv;
							for(WorkOrderVehicle v : vehicles)
							{
								Hashtable<String, Object> params2 = new Hashtable<String, Object>();
								params2.put("workOrderVehicle", v);
								
								Object wkit = gDAO.search("WorkOrderItem", params2);
								if(wkit != null)
								{
									v.setItems((Vector<WorkOrderItem>)wkit);
								}
							}
							w.setVehicles(vehicles);
						}
					}
				}
				gDAO.destroy();
			}
		}
		return rountineWorkOrders;
	}

	public void setRountineWorkOrders(Vector<WorkOrder> rountineWorkOrders) {
		this.rountineWorkOrders = rountineWorkOrders;
	}
	
	public WorkOrder getSelectedPendingRoutineWorkOrder() {
		return selectedPendingRoutineWorkOrder;
	}

	public void setSelectedPendingRoutineWorkOrder(
			WorkOrder selectedPendingRoutineWorkOrder) {
		this.selectedPendingRoutineWorkOrder = selectedPendingRoutineWorkOrder;
	}

	public WorkOrder getSelectedInprogressRoutineWorkOrder() {
		return selectedInprogressRoutineWorkOrder;
	}

	public void setSelectedInprogressRoutineWorkOrder(
			WorkOrder selectedInprogressRoutineWorkOrder) {
		this.selectedInprogressRoutineWorkOrder = selectedInprogressRoutineWorkOrder;
	}

	public void resetPendingRoutineWorkOrderVehicles()
	{
		setSelectedPendingRoutineWorkOrder(null);
		if(getWorkOrder_id() > 0L)
		{
			for(WorkOrder e : getPendingRoutineWorkOrders())
			{
				if(e.getId().longValue() == getWorkOrder_id())
				{
					setSelectedPendingRoutineWorkOrder(e);
					break;
				}
			}
		}
		if(getSelectedPendingRoutineWorkOrder() != null)
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Work-Order: " + getSelectedPendingRoutineWorkOrder().getWorkOrderNumber() + " loaded successfully!");
		
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	@SuppressWarnings("unchecked")
	public Vector<WorkOrder> getPendingRoutineWorkOrders() {
		boolean research = true;
		if(pendingRoutineWorkOrders == null || pendingRoutineWorkOrders.size() == 0)
			research = true;
		else if(pendingRoutineWorkOrders.size() > 0)
		{
			if(getPartner() != null)
			{
				if(pendingRoutineWorkOrders.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			pendingRoutineWorkOrders = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				String str = "Select e from WorkOrder e where e.workOrderType=:workOrderType and e.status=:status and e.finished=:finished and e.partner=:partner";
				Query q = gDAO.createQuery(str);
				q.setParameter("partner", getPartner());
				q.setParameter("finished", false);
				q.setParameter("status", "NEW");
				q.setParameter("workOrderType", "Routine");
				Object objs = gDAO.search(q, 0);
				if(objs != null)
				{
					pendingRoutineWorkOrders = (Vector<WorkOrder>)objs;
					
					for(WorkOrder w : pendingRoutineWorkOrders)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("workOrder", w);
						
						Object wkv = gDAO.search("WorkOrderVehicle", params);
						if(wkv != null)
						{
							Vector<WorkOrderVehicle> vehicles = (Vector<WorkOrderVehicle>)wkv;
							for(WorkOrderVehicle v : vehicles)
							{
								Hashtable<String, Object> params2 = new Hashtable<String, Object>();
								params2.put("workOrderVehicle", v);
								
								Object wkit = gDAO.search("WorkOrderItem", params2);
								if(wkit != null)
								{
									v.setItems((Vector<WorkOrderItem>)wkit);
								}
							}
							w.setVehicles(vehicles);
						}
					}
				}
				gDAO.destroy();
			}
		}
		return pendingRoutineWorkOrders;
	}

	public void setPendingRoutineWorkOrders(
			Vector<WorkOrder> pendingRoutineWorkOrders) {
		this.pendingRoutineWorkOrders = pendingRoutineWorkOrders;
	}
	
	public void resetInprogressRoutineWorkOrderVehicles()
	{
		setSelectedInprogressRoutineWorkOrder(null);
		if(getInprgWorkOrder_id() > 0L)
		{
			for(WorkOrder e : getInprogressRoutineWorkOrders())
			{
				if(e.getId().longValue() == getInprgWorkOrder_id())
				{
					setSelectedInprogressRoutineWorkOrder(e);
					break;
				}
			}
		}
		if(getSelectedInprogressRoutineWorkOrder() != null)
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Work-Order: " + getSelectedInprogressRoutineWorkOrder().getWorkOrderNumber() + " loaded successfully!");
		
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	@SuppressWarnings("unchecked")
	public Vector<WorkOrder> getInprogressRoutineWorkOrders() {
		boolean research = true;
		if(inprogressRoutineWorkOrders == null || inprogressRoutineWorkOrders.size() == 0)
			research = true;
		else if(inprogressRoutineWorkOrders.size() > 0)
		{
			if(getPartner() != null)
			{
				if(inprogressRoutineWorkOrders.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			inprogressRoutineWorkOrders = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				String str = "Select e from WorkOrder e where e.workOrderType=:workOrderType and e.status=:status and e.finished=:finished and e.partner=:partner";
				Query q = gDAO.createQuery(str);
				q.setParameter("partner", getPartner());
				q.setParameter("finished", false);
				q.setParameter("status", "IN-PROGRESS");
				q.setParameter("workOrderType", "Routine");
				Object objs = gDAO.search(q, 0);
				if(objs != null)
				{
					inprogressRoutineWorkOrders = (Vector<WorkOrder>)objs;
					
					for(WorkOrder w : inprogressRoutineWorkOrders)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("workOrder", w);
						
						Object wkv = gDAO.search("WorkOrderVehicle", params);
						if(wkv != null)
						{
							Vector<WorkOrderVehicle> vehicles = (Vector<WorkOrderVehicle>)wkv;
							for(WorkOrderVehicle v : vehicles)
							{
								Hashtable<String, Object> params2 = new Hashtable<String, Object>();
								params2.put("workOrderVehicle", v);
								
								Object wkit = gDAO.search("WorkOrderItem", params2);
								if(wkit != null)
								{
									v.setItems((Vector<WorkOrderItem>)wkit);
								}
							}
							w.setVehicles(vehicles);
						}
					}
				}
				gDAO.destroy();
			}
		}
		return inprogressRoutineWorkOrders;
	}

	public void setInprogressRoutineWorkOrders(
			Vector<WorkOrder> inprogressRoutineWorkOrders) {
		this.inprogressRoutineWorkOrders = inprogressRoutineWorkOrders;
	}
	
	// ============================Adhoc=============================== //
	
	public void resetAdhocWorkOrders()
	{
		setAdhocWorkOrders(null);
	}
	@SuppressWarnings("unchecked")
	public Vector<WorkOrder> getAdhocWorkOrders() {
		boolean research = true;
		if(adhocWorkOrders == null || adhocWorkOrders.size() == 0)
			research = true;
		else if(adhocWorkOrders.size() > 0)
		{
			if(getPartner() != null)
			{
				if(adhocWorkOrders.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			adhocWorkOrders = null;
			if(getPartner() != null && getStart_dt() != null && getEnd_dt() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				String str = "Select e from WorkOrder e where (e.crt_dt between :start_dt and :end_dt) and e.workOrderType=:workOrderType and e.status=:status and e.finished=:finished and e.partner=:partner";
				Query q = gDAO.createQuery(str);
				q.setParameter("partner", getPartner());
				q.setParameter("finished", isFinished());
				q.setParameter("status", getWorkOrderStatus());
				q.setParameter("workOrderType", "Adhoc");
				q.setParameter("start_dt", getStart_dt());
				q.setParameter("end_dt", getEnd_dt());
				Object objs = gDAO.search(q, 0);
				if(objs != null)
				{
					adhocWorkOrders = (Vector<WorkOrder>)objs;
					
					for(WorkOrder w : adhocWorkOrders)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("workOrder", w);
						
						Object wkv = gDAO.search("WorkOrderVehicle", params);
						if(wkv != null)
						{
							Vector<WorkOrderVehicle> vehicles = (Vector<WorkOrderVehicle>)wkv;
							for(WorkOrderVehicle v : vehicles)
							{
								Hashtable<String, Object> params2 = new Hashtable<String, Object>();
								params2.put("workOrderVehicle", v);
								
								Object wkit = gDAO.search("WorkOrderItem", params2);
								if(wkit != null)
								{
									v.setItems((Vector<WorkOrderItem>)wkit);
								}
							}
							w.setVehicles(vehicles);
						}
					}
				}
				gDAO.destroy();
			}
		}
		return adhocWorkOrders;
	}

	public void setAdhocWorkOrders(Vector<WorkOrder> adhocWorkOrders) {
		this.adhocWorkOrders = adhocWorkOrders;
	}
	
	public WorkOrder getSelectedPendingAdhocWorkOrder() {
		return selectedPendingAdhocWorkOrder;
	}

	public void setSelectedPendingAdhocWorkOrder(
			WorkOrder selectedPendingAdhocWorkOrder) {
		this.selectedPendingAdhocWorkOrder = selectedPendingAdhocWorkOrder;
	}

	public WorkOrder getSelectedInprogressAdhocWorkOrder() {
		return selectedInprogressAdhocWorkOrder;
	}

	public void setSelectedInprogressAdhocWorkOrder(
			WorkOrder selectedInprogressAdhocWorkOrder) {
		this.selectedInprogressAdhocWorkOrder = selectedInprogressAdhocWorkOrder;
	}

	public void resetPendingAdhocWorkOrderVehicles()
	{
		setSelectedPendingAdhocWorkOrder(null);
		if(getWorkOrder_id() > 0L)
		{
			for(WorkOrder e : getPendingAdhocWorkOrders())
			{
				if(e.getId().longValue() == getWorkOrder_id())
				{
					setSelectedPendingAdhocWorkOrder(e);
					break;
				}
			}
		}
		if(getSelectedPendingAdhocWorkOrder() != null)
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Work-Order: " + getSelectedPendingAdhocWorkOrder().getWorkOrderNumber() + " loaded successfully!");
		
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	@SuppressWarnings("unchecked")
	public Vector<WorkOrder> getPendingAdhocWorkOrders() {
		boolean research = true;
		if(pendingAdhocWorkOrders == null || pendingAdhocWorkOrders.size() == 0)
			research = true;
		else if(pendingAdhocWorkOrders.size() > 0)
		{
			if(getPartner() != null)
			{
				if(pendingAdhocWorkOrders.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			pendingAdhocWorkOrders = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				String str = "Select e from WorkOrder e where e.workOrderType=:workOrderType and e.status=:status and e.finished=:finished and e.partner=:partner";
				Query q = gDAO.createQuery(str);
				q.setParameter("partner", getPartner());
				q.setParameter("finished", false);
				q.setParameter("status", "NEW");
				q.setParameter("workOrderType", "Adhoc");
				Object objs = gDAO.search(q, 0);
				if(objs != null)
				{
					pendingAdhocWorkOrders = (Vector<WorkOrder>)objs;
					
					for(WorkOrder w : pendingAdhocWorkOrders)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("workOrder", w);
						
						Object wkv = gDAO.search("WorkOrderVehicle", params);
						if(wkv != null)
						{
							Vector<WorkOrderVehicle> vehicles = (Vector<WorkOrderVehicle>)wkv;
							for(WorkOrderVehicle v : vehicles)
							{
								Hashtable<String, Object> params2 = new Hashtable<String, Object>();
								params2.put("workOrderVehicle", v);
								
								Object wkit = gDAO.search("WorkOrderItem", params2);
								if(wkit != null)
								{
									v.setItems((Vector<WorkOrderItem>)wkit);
								}
							}
							w.setVehicles(vehicles);
						}
					}
				}
				gDAO.destroy();
			}
		}
		return pendingAdhocWorkOrders;
	}

	public void setPendingAdhocWorkOrders(
			Vector<WorkOrder> pendingAdhocWorkOrders) {
		this.pendingAdhocWorkOrders = pendingAdhocWorkOrders;
	}
	
	public void resetInprogressAdhocWorkOrderVehicles()
	{
		setSelectedInprogressAdhocWorkOrder(null);
		if(getInprgWorkOrder_id() > 0L)
		{
			for(WorkOrder e : getInprogressAdhocWorkOrders())
			{
				if(e.getId().longValue() == getInprgWorkOrder_id())
				{
					setSelectedInprogressAdhocWorkOrder(e);
					break;
				}
			}
		}
		if(getSelectedInprogressAdhocWorkOrder() != null)
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Work-Order: " + getSelectedInprogressAdhocWorkOrder().getWorkOrderNumber() + " loaded successfully!");
		
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	@SuppressWarnings("unchecked")
	public Vector<WorkOrder> getInprogressAdhocWorkOrders() {
		boolean research = true;
		if(inprogressAdhocWorkOrders == null || inprogressAdhocWorkOrders.size() == 0)
			research = true;
		else if(inprogressAdhocWorkOrders.size() > 0)
		{
			if(getPartner() != null)
			{
				if(inprogressAdhocWorkOrders.get(0).getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			inprogressAdhocWorkOrders = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				String str = "Select e from WorkOrder e where e.workOrderType=:workOrderType and e.status=:status and e.finished=:finished and e.partner=:partner";
				Query q = gDAO.createQuery(str);
				q.setParameter("partner", getPartner());
				q.setParameter("finished", false);
				q.setParameter("status", "IN-PROGRESS");
				q.setParameter("workOrderType", "Adhoc");
				Object objs = gDAO.search(q, 0);
				if(objs != null)
				{
					inprogressAdhocWorkOrders = (Vector<WorkOrder>)objs;
					
					for(WorkOrder w : inprogressAdhocWorkOrders)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("workOrder", w);
						
						Object wkv = gDAO.search("WorkOrderVehicle", params);
						if(wkv != null)
						{
							Vector<WorkOrderVehicle> vehicles = (Vector<WorkOrderVehicle>)wkv;
							for(WorkOrderVehicle v : vehicles)
							{
								Hashtable<String, Object> params2 = new Hashtable<String, Object>();
								params2.put("workOrderVehicle", v);
								
								Object wkit = gDAO.search("WorkOrderItem", params2);
								if(wkit != null)
								{
									v.setItems((Vector<WorkOrderItem>)wkit);
								}
							}
							w.setVehicles(vehicles);
						}
					}
				}
				gDAO.destroy();
			}
		}
		return inprogressAdhocWorkOrders;
	}

	public void setInprogressAdhocWorkOrders(
			Vector<WorkOrder> inprogressAdhocWorkOrders) {
		this.inprogressAdhocWorkOrders = inprogressAdhocWorkOrders;
	}
	
	// ================================================================ //

	public String getVehSummaryDetailsOfWorkOrder() {
		return vehSummaryDetailsOfWorkOrder;
	}

	public void setVehSummaryDetailsOfWorkOrder(String vehSummaryDetailsOfWorkOrder) {
		this.vehSummaryDetailsOfWorkOrder = vehSummaryDetailsOfWorkOrder;
	}

	public String getItem_action() {
		return item_action;
	}

	public void setItem_action(String item_action) {
		this.item_action = item_action;
	}

	public double getInitVehEstAmount() {
		return initVehEstAmount;
	}

	public void setInitVehEstAmount(double initVehEstAmount) {
		this.initVehEstAmount = initVehEstAmount;
	}

	public double getItmInitEstAmount() {
		return itmInitEstAmount;
	}

	public void setItmInitEstAmount(double itmInitEstAmount) {
		this.itmInitEstAmount = itmInitEstAmount;
	}

	public BigDecimal getInitVehOdometer() {
		return initVehOdometer;
	}

	public void setInitVehOdometer(BigDecimal initVehOdometer) {
		this.initVehOdometer = initVehOdometer;
	}

	public long getItem_id() {
		return item_id;
	}

	public void setItem_id(long item_id) {
		this.item_id = item_id;
	}

	public long getWorkOrder_id() {
		return workOrder_id;
	}

	public void setWorkOrder_id(long workOrder_id) {
		this.workOrder_id = workOrder_id;
	}

	public long getInprgWorkOrder_id() {
		return inprgWorkOrder_id;
	}

	public void setInprgWorkOrder_id(long inprgWorkOrder_id) {
		this.inprgWorkOrder_id = inprgWorkOrder_id;
	}

	public int getItmCount() {
		return itmCount;
	}

	public void setItmCount(int itmCount) {
		this.itmCount = itmCount;
	}

	@SuppressWarnings("unchecked")
	public Vector<Item> getItems() {
		boolean research = true;
		if(items != null && items.size() > 0)
		{
			if(items.get(0).getPartner().getId().longValue() == getWorkOrder().getSelectedVehicle().getVehicle().getPartner().getId().longValue())
				research = false;
		}
		if(research)
		{
			items = new Vector<Item>();
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getWorkOrder().getSelectedVehicle().getVehicle().getPartner());
			Object objs = gDAO.search("Item", params);
			if(objs != null)
				items = (Vector<Item>)objs;
		}
		return items;
	}

	public void setItems(Vector<Item> items) {
		this.items = items;
	}

	public UploadedFile getWorkOrderVendorDoc() {
		return workOrderVendorDoc;
	}

	public void setWorkOrderVendorDoc(UploadedFile workOrderVendorDoc) {
		this.workOrderVendorDoc = workOrderVendorDoc;
	}

	public boolean isNextRMSetup() {
		return nextRMSetup;
	}

	public void setNextRMSetup(boolean nextRMSetup) {
		this.nextRMSetup = nextRMSetup;
	}

	public VehicleRoutineMaintenanceSetup getRoutineSetup() {
		if(routineSetup == null)
			routineSetup = new VehicleRoutineMaintenanceSetup();
		return routineSetup;
	}

	public void setRoutineSetup(VehicleRoutineMaintenanceSetup routineSetup) {
		this.routineSetup = routineSetup;
	}

	public VehicleRoutineMaintenance getRoutine() {
		if(routine == null)
			routine = new VehicleRoutineMaintenance();
		return routine;
	}

	public void setRoutine(VehicleRoutineMaintenance routine) {
		this.routine = routine;
	}
	
	public void resetRoutines()
	{
		setRoutines(null);
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleRoutineMaintenance> getRoutines() {
		boolean research = true;
		if(routines == null || routines.size() == 0)
			research = true;
		else if(routines.size() > 0)
		{
			if(getPartner() != null)
			{
				if(routines.get(0).getVehicle().getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			routines = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle.partner", getPartner());
				params.put("finished", false);
				
				Object drvs = gDAO.search("VehicleRoutineMaintenance", params);
				if(drvs != null)
				{
					routines = (Vector<VehicleRoutineMaintenance>)drvs;
				}
			}
		}
		return routines;
	}

	public void setRoutines(Vector<VehicleRoutineMaintenance> routines) {
		this.routines = routines;
	}

	public VehicleAdHocMaintenanceRequest getAdhocRequest() {
		if(adhocRequest == null)
			adhocRequest = new VehicleAdHocMaintenanceRequest();
		return adhocRequest;
	}

	public void setAdhocRequest(VehicleAdHocMaintenanceRequest adhocRequest) {
		this.adhocRequest = adhocRequest;
	}
	
	public long getApproved_adhoc_req_id() {
		return approved_adhoc_req_id;
	}

	public void setApproved_adhoc_req_id(long approved_adhoc_req_id) {
		this.approved_adhoc_req_id = approved_adhoc_req_id;
	}

	public void resetAdHocRequests()
	{
		setAdHocRequests(null);
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleAdHocMaintenanceRequest> getAdHocRequests() {
		boolean research = true;
		if(adHocRequests == null || adHocRequests.size() == 0)
			research = true;
		else if(adHocRequests.size() > 0)
		{
			if(getPartner() != null)
			{
				if(adHocRequests.get(0).getVehicle().getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			adHocRequests = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle.partner", getPartner());
				params.put("active", true);
				
				Object drvs = gDAO.search("VehicleAdHocMaintenanceRequest", params);
				if(drvs != null)
				{
					adHocRequests = (Vector<VehicleAdHocMaintenanceRequest>)drvs;
				}
			}
		}
		return adHocRequests;
	}

	public void setAdHocRequests(
			Vector<VehicleAdHocMaintenanceRequest> adHocRequests) {
		this.adHocRequests = adHocRequests;
	}

	public VehicleAdHocMaintenance getAdhocMain() {
		if(adhocMain == null)
			adhocMain = new VehicleAdHocMaintenance();
		return adhocMain;
	}

	public void setAdhocMain(VehicleAdHocMaintenance adhocMain) {
		this.adhocMain = adhocMain;
	}
	
	public void resetAdHocMains()
	{
		setAdhocMains(null);
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleAdHocMaintenance> getAdhocMains() {
		boolean research = true;
		if(adhocMains == null || adhocMains.size() == 0)
			research = true;
		else if(adhocMains.size() > 0)
		{
			if(getPartner() != null)
			{
				if(adhocMains.get(0).getVehicle().getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			adhocMains = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle.partner", getPartner());
				params.put("active", true);
				
				Object drvs = gDAO.search("VehicleAdHocMaintenance", params);
				if(drvs != null)
				{
					adhocMains = (Vector<VehicleAdHocMaintenance>)drvs;
				}
			}
		}
		return adhocMains;
	}

	public void setAdhocMains(Vector<VehicleAdHocMaintenance> adhocMains) {
		this.adhocMains = adhocMains;
	}

	public Long getInsuranceComp_id() {
		return insuranceComp_id;
	}

	public void setInsuranceComp_id(Long insuranceComp_id) {
		this.insuranceComp_id = insuranceComp_id;
	}

	public Long getRepairComp_id() {
		return repairComp_id;
	}

	public void setRepairComp_id(Long repairComp_id) {
		this.repairComp_id = repairComp_id;
	}

	public UploadedFile getAccidentPhoto() {
		return accidentPhoto;
	}

	public void setAccidentPhoto(UploadedFile accidentPhoto) {
		this.accidentPhoto = accidentPhoto;
	}

	public UploadedFile getRepairedPhoto() {
		return repairedPhoto;
	}

	public void setRepairedPhoto(UploadedFile repairedPhoto) {
		this.repairedPhoto = repairedPhoto;
	}

	public UploadedFile getAccidentDocument() {
		return accidentDocument;
	}

	public void setAccidentDocument(UploadedFile accidentDocument) {
		this.accidentDocument = accidentDocument;
	}

	public UploadedFile getAccidentDocument2() {
		return accidentDocument2;
	}

	public void setAccidentDocument2(UploadedFile accidentDocument2) {
		this.accidentDocument2 = accidentDocument2;
	}

	public UploadedFile getAccidentDocument3() {
		return accidentDocument3;
	}

	public void setAccidentDocument3(UploadedFile accidentDocument3) {
		this.accidentDocument3 = accidentDocument3;
	}

	public UploadedFile getRepairAttachment() {
		return repairAttachment;
	}

	public void setRepairAttachment(UploadedFile repairAttachment) {
		this.repairAttachment = repairAttachment;
	}

	public boolean isAccidentStatus() {
		return accidentStatus;
	}

	public void setAccidentStatus(boolean accidentStatus) {
		this.accidentStatus = accidentStatus;
	}

	public VehicleAccident getAccident() {
		if(accident == null)
			accident = new VehicleAccident();
		return accident;
	}

	public void setAccident(VehicleAccident accident) {
		this.accident = accident;
	}

	public VehicleAccident getSelectedAccident() {
		return selectedAccident;
	}

	public void setSelectedAccident(VehicleAccident selectedAccident) {
		this.selectedAccident = selectedAccident;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleAccident> getAccidents() {
		boolean research = true;
		if(accidents == null || accidents.size() == 0)
			research = true;
		else if(accidents.size() > 0)
		{
			if(getPartner() != null)
			{
				if(accidents.get(0).getVehicle().getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			accidents = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle.partner", getPartner());
				params.put("createdBy", dashBean.getUser());
				params.put("active", true);
				params.put("repairApprovedDesc", "PENDING");
				
				Object drvs = gDAO.search("VehicleAccident", params);
				if(drvs != null)
				{
					accidents = (Vector<VehicleAccident>)drvs;
				}
			}
		}
		return accidents;
	}

	public void setAccidents(Vector<VehicleAccident> accidents) {
		this.accidents = accidents;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleAccident> getPendingAccidents() {
		boolean research = true;
		if(pendingAccidents == null || pendingAccidents.size() == 0)
			research = true;
		else if(pendingAccidents.size() > 0)
		{
			if(getPartner() != null)
			{
				if(pendingAccidents.get(0).getVehicle().getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			pendingAccidents = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle.partner", getPartner());
				params.put("active", true);
				params.put("repairApprovedDesc", "PENDING");
				
				Object drvs = gDAO.search("VehicleAccident", params);
				if(drvs != null)
				{
					pendingAccidents = (Vector<VehicleAccident>)drvs;
				}
			}
		}
		return pendingAccidents;
	}

	public void setPendingAccidents(Vector<VehicleAccident> pendingAccidents) {
		this.pendingAccidents = pendingAccidents;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleAccident> getReviewedAccidents() {
		boolean research = true;
		if(reviewedAccidents == null || reviewedAccidents.size() == 0)
			research = true;
		else if(reviewedAccidents.size() > 0)
		{
			if(getPartner() != null)
			{
				if(reviewedAccidents.get(0).getVehicle().getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			reviewedAccidents = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle.partner", getPartner());
				params.put("approvalUser", dashBean.getUser());
				params.put("active", true);
				params.put("repairApprovedDesc", "PENDING");
				
				Object drvs = gDAO.search("VehicleAccident", params);
				if(drvs != null)
				{
					reviewedAccidents = (Vector<VehicleAccident>)drvs;
				}
			}
		}
		return reviewedAccidents;
	}

	public void setReviewedAccidents(Vector<VehicleAccident> reviewedAccidents) {
		this.reviewedAccidents = reviewedAccidents;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleAccident> getDeniedAccidents() {
		boolean research = true;
		if(deniedAccidents == null || deniedAccidents.size() == 0)
			research = true;
		else if(deniedAccidents.size() > 0)
		{
			if(getPartner() != null)
			{
				if(deniedAccidents.get(0).getVehicle().getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			deniedAccidents = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle.partner", getPartner());
				params.put("createdBy", dashBean.getUser());
				params.put("active", true);
				params.put("repairApprovedDesc", "DENIED");
				
				Object drvs = gDAO.search("VehicleAccident", params);
				if(drvs != null)
				{
					deniedAccidents = (Vector<VehicleAccident>)drvs;
				}
			}
		}
		return deniedAccidents;
	}

	public void setDeniedAccidents(Vector<VehicleAccident> deniedAccidents) {
		this.deniedAccidents = deniedAccidents;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleAccident> getApprovedAccidents() {
		boolean research = true;
		if(approvedAccidents == null || approvedAccidents.size() == 0)
			research = true;
		else if(approvedAccidents.size() > 0)
		{
			if(getPartner() != null)
			{
				if(approvedAccidents.get(0).getVehicle().getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			approvedAccidents = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle.partner", getPartner());
				params.put("createdBy", dashBean.getUser());
				params.put("active", true);
				params.put("repairApprovedDesc", "APPROVED");
				
				Object drvs = gDAO.search("VehicleAccident", params);
				if(drvs != null)
				{
					approvedAccidents = (Vector<VehicleAccident>)drvs;
				}
			}
		}
		return approvedAccidents;
	}

	public void setApprovedAccidents(Vector<VehicleAccident> approvedAccidents) {
		this.approvedAccidents = approvedAccidents;
	}

	public VehicleAccidentRepair getRepair() {
		if(repair == null)
			repair = new VehicleAccidentRepair();
		return repair;
	}

	public void setRepair(VehicleAccidentRepair repair) {
		this.repair = repair;
	}

	public void setVTrackIntV()
	{
		if(getVtrackpollinterval() < 60)
		{
			setVtrackpollinterval(60);
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Interval set to minimum of 60 seconds.");
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Interval set successfully.");
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void onLocSelect()
	{
		try
		{
			Vehicle v = getSelectedVehicle();
			if(v != null)
			{
				for(VehicleLocationData vld : getVehiclesLocs())
				{
					if(vld.getVehicle().getId() == v.getId())
					{
						setCenterCoor(vld.getLat() + "," + vld.getLon());
						break;
					}
				}
			}
		}
		catch(Exception ex)
		{
			setCenterCoor(defaultCenterCoor);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void onMarkerSelect(OverlaySelectEvent event)
	{
		try
		{
			marker = (Marker) event.getOverlay();
			markerTrackerData = null;
			if(marker != null)
			{
				String regNum = marker.getTitle();
				GeneralDAO gDAO = new GeneralDAO();
				Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.registrationNo = :regNum");
				q.setParameter("regNum", regNum);
				Object listObj = gDAO.search(q, 0);
				if(listObj != null)
				{
					List<VehicleTrackerData> list = (List<VehicleTrackerData>)listObj;
					for(VehicleTrackerData e : list)
						markerTrackerData = e;
				}
				gDAO.destroy();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void updateMarkers()
	{
		int center_index = 0;
		for(VehicleLocationData vld : getVehiclesLocs())
		{
			boolean center = false;
			if(getSelectedVehicle() != null && vld.getVehicle() != null && vld.getVehicle().getId().longValue() == getSelectedVehicle().getId().longValue())
			{
				center = true;
			}
			boolean exists = false;
			if(getVtrackingModel().getMarkers() != null)
			{
				for(int i = 0; i < getVtrackingModel().getMarkers().size(); i++)
				{
					if(getVtrackingModel().getMarkers().get(i).getTitle().equalsIgnoreCase(vld.getVehicle().getRegistrationNo()))
					{
						exists = true;
						getVtrackingModel().getMarkers().get(i).setLatlng(new LatLng(vld.getLat(), vld.getLon()));
						// this is the selected vehicle, try to make the map follow it to where ever it's going
						if(center)
						{
							setCenterCoor(vld.getLat() + "," + vld.getLon());
							center_index = i;
						}
						break;
					}
				}
			}
			if(!exists)
			{
				try
				{
					LatLng coord1 = new LatLng(vld.getLat(), vld.getLon());
					//Basic marker
					Marker marker = new Marker(coord1, vld.getVehicle().getRegistrationNo());
					getVtrackingModel().addOverlay(marker);
				}
				catch(Exception ex)
				{}
			}
		}
		
		if(getVtrackingModel().getMarkers() != null)
		{
			for(int i = 0; i < getVtrackingModel().getMarkers().size(); i++)
			{
				RequestContext.getCurrentInstance().addCallbackParam("marker" + i, getVtrackingModel().getMarkers().get(i));
				RequestContext.getCurrentInstance().addCallbackParam("position" + i, getVtrackingModel().getMarkers().get(i).getLatlng());
				
				if(i == center_index)
				{
					RequestContext.getCurrentInstance().addCallbackParam("centerposition" + i, getVtrackingModel().getMarkers().get(i).getLatlng());
				}
			}
		}
	}
	
	public void reloadVTrack()
	{
		setVehiclesLocs(null);
		updateMarkers();
	}
	
	public MapModel getVtrackingModel() {
		if(vtrackingModel == null)
		{
			vtrackingModel = new DefaultMapModel();
			updateMarkers();
		}
		
		/*vtrackingModel = new DefaultMapModel();
		
		for(VehicleLocationData vld : getVehiclesLocs())
		{
			try
			{
				LatLng coord1 = new LatLng(vld.getLat(), vld.getLon());
				//Basic marker
				vtrackingModel.addOverlay(new Marker(coord1, vld.getVehicle().getRegistrationNo()));
			}
			catch(Exception ex)
			{}
		}*/
		return vtrackingModel;
	}

	public void setVtrackingModel(MapModel vtrackingModel) {
		this.vtrackingModel = vtrackingModel;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}
	
	public void resetVehicleLocs(int i)
	{
		setVehiclesLocs(null);
		if(i == 1)
			resetVehicles();
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleLocationData> getVehiclesLocs() {
		if(vehiclesLocs == null)
		{
			vehiclesLocs = new Vector<VehicleLocationData>();
			if(getVehicles() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Vector<Vehicle> dvsList = getVehicles();
				if(dvsList != null)
				for(Vehicle v : dvsList)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("active", true);
					params.put("vehicle", v);
					Object vlocObj = gDAO.search("VehicleLocationData", params);
					if(vlocObj != null)
					{
						Vector<VehicleLocationData> vloc = (Vector<VehicleLocationData>)vlocObj;
						if(vloc.size() > 0)
						{
							vehiclesLocs.add(vloc.get(0));
						}
					}
					
				}
			}
		}
		
		if(vehiclesLocs.size() > 0)
			setCenterCoor(vehiclesLocs.get(0).getLat() + "," + vehiclesLocs.get(0).getLon());
		else
			setCenterCoor(defaultCenterCoor);
		return vehiclesLocs;
	}

	public void setVehiclesLocs(Vector<VehicleLocationData> vehiclesLocs) {
		this.vehiclesLocs = vehiclesLocs;
	}

	public String getCenterCoor() {
		return centerCoor;
	}

	public void setCenterCoor(String centerCoor) {
		this.centerCoor = centerCoor;
	}

	public int getVtrackpollinterval() {
		return vtrackpollinterval;
	}

	public void setVtrackpollinterval(int vtrackpollinterval) {
		this.vtrackpollinterval = vtrackpollinterval;
	}

	public VehicleTrackerData getMarkerTrackerData() {
		return markerTrackerData;
	}

	public void setMarkerTrackerData(VehicleTrackerData markerTrackerData) {
		this.markerTrackerData = markerTrackerData;
	}

	public UploadedFile getFuelingBatchExcel() {
		return fuelingBatchExcel;
	}

	public void setFuelingBatchExcel(UploadedFile fuelingBatchExcel) {
		this.fuelingBatchExcel = fuelingBatchExcel;
	}

	public StreamedContent getFuelingExcelTemplate() {
		return fuelingExcelTemplate;
	}

	public VehicleFueling getFueling() {
		if(fueling == null)
			fueling = new VehicleFueling();
		return fueling;
	}

	public void setFueling(VehicleFueling fueling) {
		this.fueling = fueling;
	}

	public void resetFuelings()
	{
		setFuelings(null);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<VehicleFueling> getFuelings() {
		boolean research = true;
		if(fuelings == null || fuelings.size() == 0)
			research = true;
		else if(fuelings.size() > 0)
		{
			if(getPartner() != null)
			{
				if(fuelings.get(0).getVehicle().getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			fuelings = null;
			if(getPartner() != null && getStdt() != null && getEddt() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from VehicleFueling e where e.vehicle.partner=:partner and e.captured_dt between :stdt and :eddt");
				q.setParameter("partner", getPartner());
				q.setParameter("stdt", getStdt());
				q.setParameter("eddt", getEddt());
				
				Object drvs = gDAO.search(q, 0);
				if(drvs != null)
				{
					fuelings = (Vector<VehicleFueling>)drvs;
				}
			}
		}
		return fuelings;
	}

	public void setFuelings(Vector<VehicleFueling> fuelings) {
		this.fuelings = fuelings;
	}

	public Long getApprovalUser_id() {
		return approvalUser_id;
	}

	public void setApprovalUser_id(Long approvalUser_id) {
		this.approvalUser_id = approvalUser_id;
	}

	public VehicleFuelingRequest getFuelingRequest() {
		if(fuelingRequest == null)
			fuelingRequest = new VehicleFuelingRequest();
		return fuelingRequest;
	}

	public void setFuelingRequest(VehicleFuelingRequest fuelingRequest) {
		this.fuelingRequest = fuelingRequest;
	}

	public Vector<VehicleFuelingRequest> getMyFuelingRequests() {
		if(myFuelingRequests == null)
			myFuelingRequests = new Vector<VehicleFuelingRequest>();
		return myFuelingRequests;
	}

	public void setMyFuelingRequests(Vector<VehicleFuelingRequest> myFuelingRequests) {
		this.myFuelingRequests = myFuelingRequests;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleFuelingRequest> getMySubFuelingRequest() {
		boolean research = false;
		if(mySubFuelingRequest == null || mySubFuelingRequest.size() == 0)
			research = true;
		if(research)
		{
			mySubFuelingRequest = null;
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("createdBy", dashBean.getUser());
			params.put("approvalStatus", "PENDING");
			GeneralDAO gDAO = new GeneralDAO();
			Object retObj = gDAO.search("VehicleFuelingRequest", params);
			if(retObj != null)
			{
				mySubFuelingRequest = (Vector<VehicleFuelingRequest>)retObj;
			}
		}
		return mySubFuelingRequest;
	}

	public void setMySubFuelingRequest(
			Vector<VehicleFuelingRequest> mySubFuelingRequest) {
		this.mySubFuelingRequest = mySubFuelingRequest;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleFuelingRequest> getPendingFuelingRequests() {
		boolean research = false;
		if(pendingFuelingRequests == null || pendingFuelingRequests.size() == 0)
			research = true;
		if(research)
		{
			pendingFuelingRequests = null;
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("approvalUser", dashBean.getUser());
			params.put("approvalStatus", "PENDING");
			GeneralDAO gDAO = new GeneralDAO();
			Object retObj = gDAO.search("VehicleFuelingRequest", params);
			if(retObj != null)
			{
				pendingFuelingRequests = (Vector<VehicleFuelingRequest>)retObj;
			}
		}
		return pendingFuelingRequests;
	}

	public void setPendingFuelingRequests(
			Vector<VehicleFuelingRequest> pendingFuelingRequests) {
		this.pendingFuelingRequests = pendingFuelingRequests;
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

	public Long getLicenseType_id() {
		return licenseType_id;
	}

	public void setLicenseType_id(Long licenseType_id) {
		this.licenseType_id = licenseType_id;
	}

	public Long getTransactionType_id() {
		return transactionType_id;
	}

	public void setTransactionType_id(Long transactionType_id) {
		this.transactionType_id = transactionType_id;
	}

	public LicenseType getLicType() {
		if(licType == null)
			licType = new LicenseType();
		return licType;
	}

	public void setLicType(LicenseType licType) {
		this.licType = licType;
	}

	@SuppressWarnings("unchecked")
	public Vector<LicenseType> getLicTypes() {
		if(licTypes == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Object licTypesObj = gDAO.findAll("LicenseType");
			if(licTypesObj != null)
				licTypes = (Vector<LicenseType>)licTypesObj;
			gDAO.destroy();
		}
		return licTypes;
	}

	public void setLicTypes(Vector<LicenseType> licTypes) {
		this.licTypes = licTypes;
	}

	public DocumentType getDocType() {
		if(docType == null)
			docType = new DocumentType();
		return docType;
	}

	public void setDocType(DocumentType docType) {
		this.docType = docType;
	}

	@SuppressWarnings("unchecked")
	public Vector<DocumentType> getDocTypes() {
		if(docTypes == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Object licTypesObj = gDAO.findAll("DocumentType");
			if(licTypesObj != null)
				docTypes = (Vector<DocumentType>)licTypesObj;
			gDAO.destroy();
		}
		return docTypes;
	}

	public void setDocTypes(Vector<DocumentType> docTypes) {
		this.docTypes = docTypes;
	}

	public UploadedFile getLicenseDocument() {
		return licenseDocument;
	}

	public void setLicenseDocument(UploadedFile licenseDocument) {
		this.licenseDocument = licenseDocument;
	}

	public VehicleLicense getLicense() {
		if(license == null)
			license = new VehicleLicense();
		return license;
	}

	public void setLicense(VehicleLicense license) {
		this.license = license;
	}

	public void resetLicenses()
	{
		setLicenses(null);
	}
	
	@SuppressWarnings("unchecked")
	private boolean isLicenseAvailable()
	{
		boolean ret = true;
		
		GeneralDAO gDAO = new GeneralDAO();
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("partner", getPartner());
		Object vehObj = gDAO.search("Vehicle", params);
		if(vehObj != null)
		{
			Vector<Vehicle> vehicles = (Vector<Vehicle>)vehObj;
			params = new Hashtable<String, Object>();
			params.put("partner", getPartner());
			Object plicsObj = gDAO.search("PartnerLicense", params);
			if(plicsObj != null)
			{
				PartnerLicense lic = null;
				Vector<PartnerLicense> plics = (Vector<PartnerLicense>)plicsObj;
				for(PartnerLicense e : plics)
					lic = e;
				
				if(lic != null)
				{
					if(vehicles.size() < lic.getFinalLicenseCount())
					{
						ret = true;
					}
					else
					{
						ret = false;
					}
				}
				else if(!getPartner().isSattrak())
				{
					ret = false;
				}
			}
		}
		gDAO.destroy();
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<VehicleLicense> getLicenses() {
		boolean research = true;
		if(licenses == null || licenses.size() == 0)
			research = true;
		else if(licenses.size() > 0)
		{
			if(getPartner() != null)
			{
				if(licenses.get(0).getVehicle().getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			licenses = null;
			if(getPartner() != null && getStdt() != null && getEddt() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from VehicleLicense e where e.vehicle.partner=:partner and e.tran_dt between :stdt and :eddt");
				q.setParameter("partner", getPartner());
				q.setParameter("stdt", getStdt());
				q.setParameter("eddt", getEddt());
				
				Object drvs = gDAO.search(q, 0);
				if(drvs != null)
				{
					licenses = (Vector<VehicleLicense>)drvs;
				}
			}
		}
		return licenses;
	}

	public void setLicenses(Vector<VehicleLicense> licenses) {
		this.licenses = licenses;
	}

	public UploadedFile getDriverLicenseDocument() {
		return driverLicenseDocument;
	}

	public void setDriverLicenseDocument(UploadedFile driverLicenseDocument) {
		this.driverLicenseDocument = driverLicenseDocument;
	}

	public DriverLicense getDriverLicense() {
		if(driverLicense == null)
			driverLicense = new DriverLicense();
		return driverLicense;
	}

	public void setDriverLicense(DriverLicense driverLicense) {
		this.driverLicense = driverLicense;
	}
	
	public void resetDrvLicenses()
	{
		setDriverLicenses(null);
	}

	@SuppressWarnings("unchecked")
	public Vector<DriverLicense> getDriverLicenses() {
		boolean research = true;
		if(driverLicenses == null || driverLicenses.size() == 0)
			research = true;
		else if(driverLicenses.size() > 0)
		{
			if(getPartner() != null)
			{
				if(driverLicenses.get(0).getDriver().getPartner().getId().longValue() == getPartner().getId().longValue())
					research = false;
			}
		}
		if(research)
		{
			driverLicenses = null;
			if(getPartner() != null && getStdt() != null && getEddt() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from DriverLicense e where e.driver.partner=:partner and e.tran_dt between :stdt and :eddt");
				q.setParameter("partner", getPartner());
				q.setParameter("stdt", getStdt());
				q.setParameter("eddt", getEddt());
				
				Object drvs = gDAO.search(q, 0);
				if(drvs != null)
				{
					driverLicenses = (Vector<DriverLicense>)drvs;
				}
			}
		}
		return driverLicenses;
	}

	public void setDriverLicenses(Vector<DriverLicense> driverLicenses) {
		this.driverLicenses = driverLicenses;
	}

	public Vector<VehicleDriver> getVehicleDrivers() {
		return vehicleDrivers;
	}

	public void setVehicleDrivers(Vector<VehicleDriver> vehicleDrivers) {
		this.vehicleDrivers = vehicleDrivers;
	}
	
	public Long getWarning_id() {
		return warning_id;
	}

	public void setWarning_id(Long warning_id) {
		this.warning_id = warning_id;
	}

	public VehicleWarning getWarnType() {
		if(warnType == null)
			warnType = new VehicleWarning();
		return warnType;
	}

	public void setWarnType(VehicleWarning warnType) {
		this.warnType = warnType;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleWarning> getWarnTypes() {
		if(warnTypes == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Object warnTypesObj = gDAO.findAll("VehicleWarning");
			if(warnTypesObj != null)
				warnTypes = (Vector<VehicleWarning>)warnTypesObj;
			gDAO.destroy();
		}
		return warnTypes;
	}

	public void setWarnTypes(Vector<VehicleWarning> warnTypes) {
		this.warnTypes = warnTypes;
	}

	public VehicleBehaviour getVehicleBehaviour() {
		if(vehicleBehaviour == null)
			vehicleBehaviour = new VehicleBehaviour();
		return vehicleBehaviour;
	}

	public void setVehicleBehaviour(VehicleBehaviour vehicleBehaviour) {
		this.vehicleBehaviour = vehicleBehaviour;
	}

	public void resetVehicleBehaviours()
	{
		setVehicleBehaviours(null);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<VehicleBehaviour> getVehicleBehaviours() {
		boolean research = true;
		if(vehicleBehaviours == null || vehicleBehaviours.size() == 0)
			research = true;
		else if(vehicleBehaviours.size() > 0)
		{
			if(vehicleBehaviours.get(0).getVehicle().getId() == getVehicle_id())
				research = false;
		}
		if(research)
		{
			vehicleBehaviours = null;
			if(getVehicle_id() != null && getStdt() != null && getEddt() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from VehicleBehaviour e where e.vehicle.id=:v_id and e.eventDate between :stdt and :eddt");
				q.setParameter("v_id", getVehicle_id());
				q.setParameter("stdt", getStdt());
				q.setParameter("eddt", getEddt());
				
				Object drvs = gDAO.search(q, 0);
				if(drvs != null)
				{
					vehicleBehaviours = (Vector<VehicleBehaviour>)drvs;
				}
			}
		}
		return vehicleBehaviours;
	}

	public void setVehicleBehaviours(Vector<VehicleBehaviour> vehicleBehaviours) {
		this.vehicleBehaviours = vehicleBehaviours;
	}

	public VehicleDisposal getVdisposal() {
		if(vdisposal == null)
			vdisposal = new VehicleDisposal();
		return vdisposal;
	}

	public void setVdisposal(VehicleDisposal vdisposal) {
		this.vdisposal = vdisposal;
	}

	public Vector<VehicleDisposal> getMyDisposalRequests() {
		if(myDisposalRequests == null)
			myDisposalRequests = new Vector<VehicleDisposal>();
		return myDisposalRequests;
	}

	public void setMyDisposalRequests(Vector<VehicleDisposal> myDisposalRequests) {
		this.myDisposalRequests = myDisposalRequests;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleDisposal> getMySubDisposalRequest() {
		boolean research = false;
		if(mySubDisposalRequest == null || mySubDisposalRequest.size() == 0)
			research = true;
		if(research)
		{
			mySubDisposalRequest = null;
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("createdBy", dashBean.getUser());
			params.put("approvalStatus", "PENDING");
			GeneralDAO gDAO = new GeneralDAO();
			Object retObj = gDAO.search("VehicleDisposal", params);
			if(retObj != null)
			{
				mySubDisposalRequest = (Vector<VehicleDisposal>)retObj;
			}
		}
		return mySubDisposalRequest;
	}

	public void setMySubDisposalRequest(Vector<VehicleDisposal> mySubDisposalRequest) {
		this.mySubDisposalRequest = mySubDisposalRequest;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleDisposal> getPendingDisposalRequests() {
		boolean research = false;
		if(pendingDisposalRequests == null || pendingDisposalRequests.size() == 0)
			research = true;
		if(research)
		{
			pendingDisposalRequests = null;
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("approvalUser", dashBean.getUser());
			params.put("approvalStatus", "PENDING");
			GeneralDAO gDAO = new GeneralDAO();
			Object retObj = gDAO.search("VehicleDisposal", params);
			if(retObj != null)
			{
				pendingDisposalRequests = (Vector<VehicleDisposal>)retObj;
			}
		}
		return pendingDisposalRequests;
	}

	public void setPendingDisposalRequests(
			Vector<VehicleDisposal> pendingDisposalRequests) {
		this.pendingDisposalRequests = pendingDisposalRequests;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public String getWorkOrderStatus() {
		return workOrderStatus;
	}

	public void setWorkOrderStatus(String workOrderStatus) {
		this.workOrderStatus = workOrderStatus;
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

	public boolean isSelectAll() {
		return selectAll;
	}

	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}
	
}
