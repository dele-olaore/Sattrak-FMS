package com.dexter.fms.mbean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
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

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LineChartSeries;
import org.primefaces.model.chart.PieChartModel;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.dto.CorporateTripsSummary;
import com.dexter.fms.dto.FuelConsumption;
import com.dexter.fms.dto.VehicleBehaviorSummary;
import com.dexter.fms.dto.VehicleCheckInOut;
import com.dexter.fms.dto.VehicleCostEfficientSummary;
import com.dexter.fms.dto.VehicleUtilizationSummary;
import com.dexter.fms.jasper.datasources.MaintCostDataSource;
import com.dexter.fms.jasper.datasources.MaintCostDataSource.MaintCost;
import com.dexter.fms.jasper.datasources.UtilizationDataSource;
import com.dexter.fms.jasper.datasources.UtilizationDataSource.Utilization;
import com.dexter.fms.jasper.datasources.VehicleCostSummary;
import com.dexter.fms.jasper.datasources.VehicleCostSummary.Cost;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerDriverQuery;
import com.dexter.fms.model.PartnerSetting;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.app.CorporateTrip;
import com.dexter.fms.model.app.CorporateTripPassenger;
import com.dexter.fms.model.app.DriverLicense;
import com.dexter.fms.model.app.Expense;
import com.dexter.fms.model.app.ExpenseType;
import com.dexter.fms.model.app.Fleet;
import com.dexter.fms.model.app.Item;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleAccident;
import com.dexter.fms.model.app.VehicleAccidentRepair;
import com.dexter.fms.model.app.VehicleAdHocMaintenance;
import com.dexter.fms.model.app.VehicleBehaviour;
import com.dexter.fms.model.app.VehicleDriver;
import com.dexter.fms.model.app.VehicleFueling;
import com.dexter.fms.model.app.VehicleLicense;
import com.dexter.fms.model.app.VehicleOdometerData;
import com.dexter.fms.model.app.VehicleRoutineMaintenance;
import com.dexter.fms.model.app.VehicleSpeedData;
import com.dexter.fms.model.app.VehicleStatusEnum;
import com.dexter.fms.model.app.VehicleTrackerData;
import com.dexter.fms.model.app.VehicleTrackerEventData;
import com.dexter.fms.model.app.WorkOrderItem;
import com.dexter.fms.model.ref.Department;
import com.dexter.fms.model.ref.Region;
import com.dexter.fms.model.ref.VehicleModel;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

@ManagedBean(name = "reportsBean2")
@SessionScoped
public class ReportsMBean2 implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-UserMBean");
	
	private FacesMessage msg = null;
	
	private Long partner_id;
	private Partner partner;
	
	private Long role_id;
	private Long region_id;
	private Long department_id;
	private Vector<Department> depts;
	private Vector<Region> regions;
	
	private Date start_dt, end_dt;
	private int rgroup;
	private Long fleet_id;
	private Vector<Fleet> fleets;
	private Long vehicle_id;
	private String regNo, stype, reportType, mainttype;
	
	private Long driver_id;
	private String accidentStatus;
	private Long vehicleModel_id;
	private int minYears;
	private int searchType;
	
	private Vector<PartnerUser> allUsers;
	private Vector<DriverLicense> dueDriversLic;
	private Vector<VehicleAccident> vehicleAccidents, driverAccidents, statusAccidents, brandAccidents;
	private Vector<Vehicle> vehiclesByBrand, vehicles;
	private Vector<VehicleRoutineMaintenance> rmaints;
	private Vector<VehicleAdHocMaintenance> adhocmaints;
	private Vector<VehicleAccident> activeAccidents;
	private Vector<Vehicle> accidentedVehicles;
	private Vector<VehicleLicense> dueVehicleLicenses;
	private Vector<Vehicle> vehiclesAges;
	
	private Vector<CorporateTrip> corTrips;
	private Vector<Expense> expenses;
	
	private Vector<VehicleFueling> fuelings;
	
	// latest report based on Banjo's document
	private Vector<PartnerDriver> driversByYears, driversByRegion;
	private Vector<Fleet> partnerFleets;
	private Vector<FuelConsumption> fuelConsumptions, mileageConsumptions;
	
	private Vector<Vehicle> vehiclesDrivingInfoReport;
	
	private Vector<VehicleCheckInOut> vehiclesCheckInOut;
	
	private Vector<PartnerDriverQuery> driverQueries;
	
	private Vector<String[]> maintCostReport, maintCostByPartsReport, maintCostByBrandsReport, partsReplacementReport, partsServicingReport;
	private Vector<String[]> expenseSummaryReport, accidentsSummaryReport;
	
	private VehicleUtilizationSummary utilizationSummary;
	private CorporateTripsSummary corporateTripsSummary;
	private VehicleCostEfficientSummary vehicleCostEfficientSummary;
	
	private String report_title;
	private String report_start_dt;
	private String report_end_dt;
	private String report_page = "/faces/reports_home.xhtml";
	
	private String period, groupBy;
	
	private PieChartModel pieModel;
	
	private long maxY;
	private CartesianChartModel barModel;
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	@ManagedProperty("#{fleetBean}")
	FleetMBean fleetBean;
	
	public ReportsMBean2() {}
	
	public void downloadVehicleCostEfficientPDF() {
		try {
			// distance_covered, maint_cost, fuel_cost, other_cost;
			VehicleCostSummary utilDS = VehicleCostSummary.getInstance();
			Vector<Cost> data = new Vector<Cost>();
			for(Vehicle e : vehicleCostEfficientSummary.getVehicles()) {
				VehicleCostSummary.Cost mc = utilDS.new Cost();
				mc.setDistance(e.getDistance_covered());
				mc.setFuelCost(e.getFuel_cost());
				mc.setMaintCost(e.getMaint_cost());
				mc.setOtherCost(e.getOther_cost());
				mc.setVehicleReg(e.getRegistrationNo());
				mc.setTotalCost(mc.getFuelCost()+mc.getMaintCost()+mc.getOtherCost());
				data.add(mc);
			}
			utilDS.setData(data);
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("reportTitle", "Vehicle Utilization Report");
			parameters.put("reportDesc", "Summary report of vehicles utilization.");
			parameters.put("totalsLabel1", "Distance Covered");
			parameters.put("totalsLabel2", "Fueling Cost");
			parameters.put("totalsLabel3", "Maintenance Cost");
			parameters.put("totalsLabel4", "Others Cost");
			parameters.put("totalsLabel5", "");
			parameters.put("totalsLabel6", "");
			parameters.put("totalsLabel7", "");
			parameters.put("summaryLabel1", "Total Cost");
			parameters.put("summaryLabel2", "No of Vehicles");
			
			parameters.put("totalsValue1", ""+vehicleCostEfficientSummary.getDistance_covered());
			parameters.put("totalsValue2", ""+vehicleCostEfficientSummary.getFuel_cost());
			parameters.put("totalsValue3", ""+vehicleCostEfficientSummary.getMaint_cost());
			parameters.put("totalsValue4", ""+vehicleCostEfficientSummary.getOther_cost());
			parameters.put("totalsValue5", "");
			parameters.put("totalsValue6", "");
			parameters.put("totalsValue7", "");
			parameters.put("summaryValue1", ""+vehicleCostEfficientSummary.getFuel_cost()+vehicleCostEfficientSummary.getMaint_cost()+vehicleCostEfficientSummary.getOther_cost());
			parameters.put("summaryValue2", ""+vehicleCostEfficientSummary.getVehicles().size());
			
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(utilDS.getCollectionList());
			
			downloadJasperPDF(parameters, "cost_efficient_report.pdf", "/resources/jasper/cost_efficient.jasper", ds);
		} catch(Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchVehicleCostEfficient() {
		// distance_covered, maint_cost, fuel_cost, other_cost;
		vehicleCostEfficientSummary = null;
		if(getStart_dt() != null && getEnd_dt() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			// distance_covered, trips, max_speed, average_speed
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getPartner());
			if(getFleet_id() != null) {
				params.put("fleet.id", getFleet_id());
			}
			Object obj = gDAO.search("Vehicle", params);
			if(obj != null) {
				vehicleCostEfficientSummary = new VehicleCostEfficientSummary();
				Vector<Vehicle> vehicles = (Vector<Vehicle>)obj;
				vehicleCostEfficientSummary.setVehicles(vehicles);
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
					setPeriod(sdf.format(getStart_dt()) + " - " + sdf.format(getEnd_dt()));
				} catch(Exception ex){ ex.printStackTrace(); }
				for(Vehicle v : vehicleCostEfficientSummary.getVehicles()) {
					if(getFleet_id() != null) {
						setGroupBy(v.getFleet().getName());
					} else {
						setGroupBy("All");
					}
					Query q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :vehicle_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
					q.setParameter("vehicle_id", v.getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<VehicleOdometerData> list = (Vector<VehicleOdometerData>)obj;
						double startOdometer = 0, endOdometer = 0;
						for(int i=0; i<list.size(); i++) {
							VehicleOdometerData vod = list.get(i);
							if(i == 0)
								startOdometer = vod.getOdometer();
							if(i == list.size() - 1)
								endOdometer = vod.getOdometer();
						}
						double distance = Math.abs(endOdometer - startOdometer);
						v.setDistance_covered(new BigDecimal(distance).setScale(2, RoundingMode.HALF_UP).doubleValue());
						vehicleCostEfficientSummary.setDistance_covered(vehicleCostEfficientSummary.getDistance_covered() + v.getDistance_covered());
					}
					
					q = gDAO.createQuery("Select e from Expense e where e.vehicle.id = :vehicle_id and (e.expense_dt between :st_dt and :ed_dt) order by e.expense_dt");
					q.setParameter("vehicle_id", v.getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<Expense> list = (Vector<Expense>)obj;
						double fuel_cost = 0, maint_cost = 0, others_cost = 0;
						for(Expense ex : list) {
							if(ex.getType().getName().equalsIgnoreCase("Fueling"))
								fuel_cost += ex.getAmount();
							else if(ex.getType().getName().equalsIgnoreCase("Maintenance"))
								maint_cost += ex.getAmount();
							else
								others_cost = ex.getAmount();
						}
						v.setFuel_cost(fuel_cost);
						v.setMaint_cost(maint_cost);
						v.setOther_cost(others_cost);
						v.setTotal_cost(v.getFuel_cost() + v.getMaint_cost() + v.getOther_cost());
						vehicleCostEfficientSummary.setFuel_cost(vehicleCostEfficientSummary.getFuel_cost() + v.getFuel_cost());
						vehicleCostEfficientSummary.setMaint_cost(vehicleCostEfficientSummary.getMaint_cost() + v.getMaint_cost());
						vehicleCostEfficientSummary.setOther_cost(vehicleCostEfficientSummary.getOther_cost() + v.getOther_cost());
					}
				}
			}
			gDAO.destroy();
			
			if(vehicleCostEfficientSummary != null && vehicleCostEfficientSummary.getVehicles() != null && vehicleCostEfficientSummary.getVehicles().size() > 0) {
				if(getPieModel() == null)
					createPieModel();
				
				for(Vehicle v : vehicleCostEfficientSummary.getVehicles()) {
					if(v.getTotal_cost() > 0)
						getPieModel().set(v.getRegistrationNo(), v.getTotal_cost());
					else
						getPieModel().set(v.getRegistrationNo(), 0);
				}
				
				if(barModel == null)
					createBarModel();
				maxY = 0;
				ChartSeries distanceCoveredSeries = new ChartSeries();
				distanceCoveredSeries.setLabel("Distance covered");
				for(Vehicle v : vehicleCostEfficientSummary.getVehicles()) {
					if(v.getDistance_covered() > 0) {
						distanceCoveredSeries.set(v.getRegistrationNo(), v.getDistance_covered());
						if(v.getDistance_covered() > maxY)
							maxY = new BigDecimal(v.getDistance_covered()).longValue() + 5;
					} else
						distanceCoveredSeries.set(v.getRegistrationNo(), 0);
				}
				barModel.addSeries(distanceCoveredSeries);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", vehicleCostEfficientSummary.getVehicles().size() + " vehicle(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "No vehicle found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} else {
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Please supply date range!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void downloadCorporateTripsPDF() {
		try {
			UtilizationDataSource utilDS = UtilizationDataSource.getInstance();
			Vector<Utilization> data = new Vector<Utilization>();
			double totalDistance = 0, totalWorkTime = 0, totalFuel = 0, kmPerL = 0, avgKmPerL = 0;
			int totalTrips = 0, totalPassengers = 0, totalDrivers = 0;
			for(Vehicle e : corporateTripsSummary.getVehicles()) {
				UtilizationDataSource.Utilization mc = utilDS.new Utilization();
				mc.setAvgSpeed(e.getAverage_speed());
				mc.setDistanceCovered(e.getDistance());
				totalDistance += mc.getDistanceCovered();
				mc.setMaxSpeed(e.getMax_speed());
				mc.setNo_of_passengers(e.getNo_of_passengers());
				totalPassengers += mc.getNo_of_passengers();
				mc.setFuel_consumed(e.getFuel_consumed());
				totalFuel += mc.getFuel_consumed();
				mc.setWorking_time(e.getWorking_time());
				totalWorkTime += mc.getWorking_time();
				mc.setTripsCount(e.getNo_of_trips());
				totalTrips += mc.getTripsCount();
				mc.setVehicleReg(e.getRegistrationNo());
				mc.setKm_per_liter(e.getKm_per_liter());
				kmPerL += mc.getKm_per_liter();
				totalDrivers += e.getNo_of_drivers();
				data.add(mc);
			}
			try {
				avgKmPerL = kmPerL/corporateTripsSummary.getVehicles().size();
			} catch(Exception ex){}
			
			utilDS.setData(data);
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("reportTitle", "Vehicle Trips Report");
			parameters.put("reportDesc", "Summary report of vehicle trips.");
			parameters.put("totalsLabel1", "No. of Trips");
			parameters.put("totalsLabel2", "Distance");
			parameters.put("totalsLabel3", "Working Time");
			parameters.put("totalsLabel4", "No. of Passenger");
			parameters.put("totalsLabel5", "Fuel Consumed");
			parameters.put("totalsLabel6", "Fuel Cons. Rate");
			parameters.put("totalsLabel7", "");
			parameters.put("summaryLabel1", "No. of Vehicles");
			parameters.put("summaryLabel2", "No. of Drivers");
			
			parameters.put("totalsValue1", ""+totalTrips);
			parameters.put("totalsValue2", ""+totalDistance);
			parameters.put("totalsValue3", ""+totalWorkTime);
			parameters.put("totalsValue4", ""+totalPassengers);
			parameters.put("totalsValue5", ""+totalFuel);
			parameters.put("totalsValue6", ""+avgKmPerL);
			parameters.put("totalsValue7", "");
			parameters.put("summaryValue1", ""+corporateTripsSummary.getVehicles().size());
			parameters.put("summaryValue2", ""+totalDrivers);
			
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(utilDS.getCollectionList());
			
			downloadJasperPDF(parameters, "corporate_trips_utilization.pdf", "/resources/jasper/corpTrips.jasper", ds);
		} catch(Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchCorporateTrips() {
		//no_of_trips, distance, no_of_passengers, fuel_consumed(use vehiclefueling table), working_time, km_per_liter()
		corporateTripsSummary = null;
		if(getStart_dt() != null && getEnd_dt() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			// distance_covered, trips, max_speed, average_speed
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getPartner());
			if(getFleet_id() != null) {
				params.put("fleet.id", getFleet_id());
			}
			Object obj = gDAO.search("Vehicle", params);
			if(obj != null) {
				corporateTripsSummary = new CorporateTripsSummary();
				Vector<Vehicle> vehicles = (Vector<Vehicle>)obj;
				corporateTripsSummary.setVehicles(vehicles);
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
					setPeriod(sdf.format(getStart_dt()) + " - " + sdf.format(getEnd_dt()));
				} catch(Exception ex){ ex.printStackTrace(); }
				for(Vehicle v : corporateTripsSummary.getVehicles()) {
					if(getFleet_id() != null) {
						setGroupBy(v.getFleet().getName());
					} else {
						setGroupBy("All");
					}
					Query q = gDAO.createQuery("Select e from CorporateTrip e where e.vehicle.id = :vehicle_id and (e.departureDateTime between :st_dt and :ed_dt) and (e.tripStatus = 'ON_TRIP' or e.tripStatus = 'SHOULD_BE_COMPLETED' or e.tripStatus = 'COMPLETION_REQUEST' or e.tripStatus = 'COMPLETED')");
					q.setParameter("vehicle_id", v.getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<CorporateTrip> list = (Vector<CorporateTrip>)obj;
						v.setNo_of_trips(list.size());
						corporateTripsSummary.setNo_of_trips(corporateTripsSummary.getNo_of_trips() + v.getNo_of_trips());
						Vector<String> driverIds = new Vector<String>();
						for(CorporateTrip ct : list) {
							if(ct.getDriver() != null && !driverIds.contains(""+ct.getDriver().getId().longValue())) {
								driverIds.add(""+ct.getDriver().getId().longValue());
							}
							q = gDAO.createQuery("Select e from CorporateTripPassenger e where e.trip.id = :trip_id");
							q.setParameter("trip_id", ct.getId());
							Object ctpObj = gDAO.search(q, 0);
							if(ctpObj != null) {
								Vector<CorporateTripPassenger> ctpList = (Vector<CorporateTripPassenger>)ctpObj;
								v.setNo_of_passengers(v.getNo_of_passengers() + ctpList.size());
								corporateTripsSummary.setNo_of_passengers(corporateTripsSummary.getNo_of_passengers() + v.getNo_of_passengers());
							}
						}
						v.setNo_of_drivers(driverIds.size());
						corporateTripsSummary.setNo_of_drivers(corporateTripsSummary.getNo_of_drivers() + v.getNo_of_drivers());
					}
					
					BigDecimal topSpeed = new BigDecimal(0), totalSpeed = new BigDecimal(0), speedEntryCount = new BigDecimal(0), avgSpeed = new BigDecimal(0);
					q = gDAO.createQuery("Select e from VehicleSpeedData e where e.vehicle.id = :vehicle_id and (e.captured_dt between :st_dt and :ed_dt)");
					q.setParameter("vehicle_id", v.getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<VehicleSpeedData> list = (Vector<VehicleSpeedData>)obj;
						for(VehicleSpeedData vsd : list) {
							if(vsd.getSpeed() > 0)
								speedEntryCount = speedEntryCount.add(new BigDecimal(1));
							totalSpeed = totalSpeed.add(new BigDecimal(vsd.getSpeed()));
							if(vsd.getSpeed() > topSpeed.doubleValue())
								topSpeed = new BigDecimal(vsd.getSpeed());
						}
						try {
						avgSpeed = totalSpeed.divide(speedEntryCount);
						v.setAverage_speed(avgSpeed.doubleValue());
						} catch(Exception ex){}
						v.setMax_speed(topSpeed.doubleValue());
					}
					
					q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :vehicle_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
					q.setParameter("vehicle_id", v.getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<VehicleOdometerData> list = (Vector<VehicleOdometerData>)obj;
						double startOdometer = 0, endOdometer = 0;
						for(int i=0; i<list.size(); i++) {
							VehicleOdometerData vod = list.get(i);
							if(i == 0)
								startOdometer = vod.getOdometer();
							if(i == list.size() - 1)
								endOdometer = vod.getOdometer();
						}
						double distance = Math.abs(endOdometer - startOdometer);
						v.setDistance(new BigDecimal(distance).setScale(2, RoundingMode.HALF_UP).doubleValue());
						corporateTripsSummary.setDistance(corporateTripsSummary.getDistance() + v.getDistance());
					}
					
					q = gDAO.createQuery("Select e from VehicleFueling e where e.vehicle.id = :vehicle_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
					q.setParameter("vehicle_id", v.getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<VehicleFueling> list = (Vector<VehicleFueling>)obj;
						double startFuelLevel = 0, totalConsumed = 0;
						for(int i=0; i<list.size(); i++) {
							VehicleFueling vf = list.get(i);
							if(i > 0)
								totalConsumed += Math.abs(startFuelLevel - (vf.getFuelLevel() - vf.getLitres()));
							startFuelLevel = vf.getFuelLevel();
						}
						v.setFuel_consumed(totalConsumed);
						corporateTripsSummary.setFuel_consumed(corporateTripsSummary.getFuel_consumed() + v.getFuel_consumed());
						
						if(v.getDistance() > 0 && v.getFuel_consumed() > 0) {
							try {
								BigDecimal oneLiter = new BigDecimal(v.getDistance()).divide(new BigDecimal(v.getFuel_consumed()));
								oneLiter = oneLiter.setScale(2);
								v.setKm_per_liter(oneLiter.doubleValue());
								corporateTripsSummary.setKm_per_liter(corporateTripsSummary.getKm_per_liter() + v.getKm_per_liter());
							} catch(Exception ex) {}
						}
					}
					
					q = gDAO.createQuery("Select e from VehicleTrackerEventData e where e.vehicle.id = :vehicle_id and (e.event_name = 'Ignition On' or e.event_name = 'Ignition Off') and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
					q.setParameter("vehicle_id", v.getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					obj = gDAO.search(q, 0);
					if(obj != null) { // 
						Vector<VehicleTrackerEventData> objList = (Vector<VehicleTrackerEventData>)obj;
						if(objList != null && objList.size() > 0) {
							long timeInMilli = 0;
							Date switchOnTime = null, switchOffTime = null;
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
							for(VehicleTrackerEventData vted : objList) {
								if(vted.getEvent_name() != null && vted.getEvent_name().trim().equalsIgnoreCase("Ignition On")) {
									try {
										switchOnTime = sdf.parse(vted.getEvent_time());
										switchOffTime = null;
									} catch(Exception ex) {}
								} else if(vted.getEvent_name() != null && vted.getEvent_name().trim().equalsIgnoreCase("Ignition Off")) {
									try {
										switchOffTime = sdf.parse(vted.getEvent_time());
										if(switchOnTime != null) {
											timeInMilli += Math.abs(switchOffTime.getTime() - switchOnTime.getTime());
										}
									} catch(Exception ex) {}
								}
							}
							if(timeInMilli > 0) {
								try {
									BigDecimal timemilli = new BigDecimal(timeInMilli).divide(new BigDecimal(3600000));
									timemilli = timemilli.setScale(3);
									v.setWorking_time(timemilli.doubleValue());
									corporateTripsSummary.setWorking_time(corporateTripsSummary.getWorking_time() + v.getWorking_time());
								} catch(Exception ex) {}
							} else
								v.setWorking_time(0);
						}
					}
				}
			}
			gDAO.destroy();
			
			if(corporateTripsSummary != null && corporateTripsSummary.getVehicles() != null && corporateTripsSummary.getVehicles().size() > 0) {
				if(getPieModel() == null)
					createPieModel();
				
				for(Vehicle v : corporateTripsSummary.getVehicles()) {
					getPieModel().set(v.getRegistrationNo(), v.getNo_of_trips());
				}
				
				if(barModel == null)
					createBarModel();
				maxY = 0;
				ChartSeries distanceCoveredSeries = new ChartSeries();
				distanceCoveredSeries.setLabel("Distance covered");
				for(Vehicle v : corporateTripsSummary.getVehicles()) {
					distanceCoveredSeries.set(v.getRegistrationNo(), v.getDistance());
					if(v.getDistance() > maxY)
						maxY = new BigDecimal(v.getDistance()).longValue() + 5;
				}
				barModel.addSeries(distanceCoveredSeries);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", corporateTripsSummary.getVehicles().size() + " vehicle(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "No vehicle found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} else {
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Please supply date range!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void downloadVehiclesUtilizationPDF() {
		try {
			// distance_covered, trips, max_speed, average_speed
			UtilizationDataSource utilDS = UtilizationDataSource.getInstance();
			Vector<Utilization> data = new Vector<Utilization>();
			for(Vehicle e : utilizationSummary.getVehicles()) {
				UtilizationDataSource.Utilization mc = utilDS.new Utilization();
				mc.setAvgSpeed(e.getAverage_speed());
				mc.setDistanceCovered(e.getDistance_covered());
				mc.setMaxSpeed(e.getMax_speed());
				mc.setTripsCount(e.getTrips());
				mc.setVehicleReg(e.getRegistrationNo());
				mc.setStatus(e.getUtilizationStatus());
				data.add(mc);
			}
			utilDS.setData(data);
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("reportTitle", "Vehicle Utilization Report");
			parameters.put("reportDesc", "Summary report of vehicles utilization.");
			parameters.put("totalsLabel1", "Vehicles in use");
			parameters.put("totalsLabel2", "Accidented");
			parameters.put("totalsLabel3", "Not in use");
			parameters.put("totalsLabel4", "In Workshop");
			parameters.put("totalsLabel5", "");
			parameters.put("totalsLabel6", "");
			parameters.put("totalsLabel7", "");
			parameters.put("summaryLabel1", "Total Vehicles");
			parameters.put("summaryLabel2", "");
			
			parameters.put("totalsValue1", ""+utilizationSummary.getInuse());
			parameters.put("totalsValue2", ""+utilizationSummary.getTotalAccidented());
			parameters.put("totalsValue3", ""+utilizationSummary.getNotinuse());
			parameters.put("totalsValue4", ""+utilizationSummary.getTotalInWorkshop());
			parameters.put("totalsValue5", "");
			parameters.put("totalsValue6", "");
			parameters.put("totalsValue7", "");
			parameters.put("summaryValue1", ""+utilizationSummary.getVehicles().size());
			parameters.put("summaryValue2", "");
			
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(utilDS.getCollectionList());
			
			downloadJasperPDF(parameters, "utilization_report.pdf", "/resources/jasper/utilization.jasper", ds);
		} catch(Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchVehiclesUtilization() {
		utilizationSummary = null;
		Vector<Vehicle> listInUse = new Vector<Vehicle>(), listNotInUse = new Vector<Vehicle>(), accidentedList = new Vector<Vehicle>(), inworkshopList = new Vector<Vehicle>();
		if(getStart_dt() != null && getEnd_dt() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			// distance_covered, trips, max_speed, average_speed
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getPartner());
			if(getFleet_id() != null) {
				params.put("fleet.id", getFleet_id());
			}
			Object obj = gDAO.search("Vehicle", params);
			if(obj != null) {
				utilizationSummary = new VehicleUtilizationSummary();
				Vector<Vehicle> vehicles = (Vector<Vehicle>)obj;
				utilizationSummary.setTotalCount(vehicles.size());
				utilizationSummary.setVehicles(vehicles);
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
					setPeriod(sdf.format(getStart_dt()) + " - " + sdf.format(getEnd_dt()));
				} catch(Exception ex){ ex.printStackTrace(); }
				for(Vehicle v : utilizationSummary.getVehicles()) {
					if(getFleet_id() != null) {
						setGroupBy(v.getFleet().getName());
					} else {
						setGroupBy("All");
					}
					int trips = 0;
					BigDecimal topSpeed = new BigDecimal(0), totalSpeed = new BigDecimal(0), speedEntryCount = new BigDecimal(0), avgSpeed = new BigDecimal(0);
					
					Query q = gDAO.createQuery("Select e from VehicleSpeedData e where e.vehicle.id = :vehicle_id and (e.captured_dt between :st_dt and :ed_dt)");
					q.setParameter("vehicle_id", v.getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<VehicleSpeedData> list = (Vector<VehicleSpeedData>)obj;
						for(VehicleSpeedData vsd : list) {
							if(vsd.getSpeed() > 0)
								speedEntryCount = speedEntryCount.add(new BigDecimal(1));
							totalSpeed = totalSpeed.add(new BigDecimal(vsd.getSpeed()));
							if(vsd.getSpeed() > topSpeed.doubleValue())
								topSpeed = new BigDecimal(vsd.getSpeed());
						}
						try {
						avgSpeed = totalSpeed.divide(speedEntryCount);
						v.setAverage_speed(avgSpeed.doubleValue());
						} catch(Exception ex){}
						v.setMax_speed(topSpeed.doubleValue());
					}
					
					q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :vehicle_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
					q.setParameter("vehicle_id", v.getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<VehicleOdometerData> list = (Vector<VehicleOdometerData>)obj;
						double startOdometer = 0, endOdometer = 0;
						for(int i=0; i<list.size(); i++) {
							VehicleOdometerData vod = list.get(i);
							if(i == 0)
								startOdometer = vod.getOdometer();
							if(i == list.size() - 1)
								endOdometer = vod.getOdometer();
						}
						double distance = Math.abs(endOdometer - startOdometer);
						v.setDistance_covered(new BigDecimal(distance).setScale(2, RoundingMode.HALF_UP).doubleValue());
					}
					
					q = gDAO.createQuery("Select e from VehicleTrackerEventData e where e.vehicle.id = :vehicle_id and (e.event_name = 'Ignition On' or e.event_name = 'Ignition Off') and (e.captured_dt between :st_dt and :ed_dt)");
					q.setParameter("vehicle_id", v.getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<VehicleTrackerEventData> objList = (Vector<VehicleTrackerEventData>)obj;
						if(objList != null && objList.size() > 0) {
							for(VehicleTrackerEventData vted : objList) {
								if(vted.getEvent_name() != null && vted.getEvent_name().trim().equalsIgnoreCase("Ignition On"))
									trips += 1;
							}
							v.setTrips(trips);
							if(v.getActiveStatus() != null && v.getActiveStatus().equalsIgnoreCase(VehicleStatusEnum.ACCIDENTED.getStatus())) {
								v.setUtilizationStatus("Accidented");
								accidentedList.add(v);
							} else if(v.getActiveStatus() != null && v.getActiveStatus().equalsIgnoreCase(VehicleStatusEnum.UNDER_MAINTENANCE.getStatus())) {
								v.setUtilizationStatus("In workshop");
								inworkshopList.add(v);
							} else {
								v.setUtilizationStatus("In use");
								listInUse.add(objList.get(0).getVehicle());
							}
						} else {
							if(v.getActiveStatus() != null && v.getActiveStatus().equalsIgnoreCase(VehicleStatusEnum.ACCIDENTED.getStatus())) {
								v.setUtilizationStatus("Accidented");
								accidentedList.add(v);
							} else if(v.getActiveStatus() != null && v.getActiveStatus().equalsIgnoreCase(VehicleStatusEnum.UNDER_MAINTENANCE.getStatus())) {
								v.setUtilizationStatus("In workshop");
								inworkshopList.add(v);
							} else {
								v.setUtilizationStatus("Not in use");
								listNotInUse.add(v);
							}
						}
					} else {
						if(v.getActiveStatus() != null && v.getActiveStatus().equalsIgnoreCase(VehicleStatusEnum.ACCIDENTED.getStatus())) {
							v.setUtilizationStatus("Accidented");
							accidentedList.add(v);
						} else if(v.getActiveStatus() != null && v.getActiveStatus().equalsIgnoreCase(VehicleStatusEnum.UNDER_MAINTENANCE.getStatus())) {
							v.setUtilizationStatus("In workshop");
							inworkshopList.add(v);
						} else {
							v.setUtilizationStatus("Not in use");
							listNotInUse.add(v);
						}
					}
				}
				utilizationSummary.setAccidentedList(accidentedList);
				utilizationSummary.setTotalAccidented(accidentedList.size());
				utilizationSummary.setInuseList(listInUse);
				utilizationSummary.setInuse(listInUse.size());
				utilizationSummary.setInworkshopList(inworkshopList);
				utilizationSummary.setTotalInWorkshop(inworkshopList.size());
				utilizationSummary.setNotinuseList(listNotInUse);
				utilizationSummary.setNotinuse(listNotInUse.size());
			}
			
			gDAO.destroy();
			
			if(utilizationSummary != null && utilizationSummary.getVehicles() != null && utilizationSummary.getVehicles().size() > 0) {
				if(getPieModel() == null)
					createPieModel();
				
				getPieModel().set("Accidented", utilizationSummary.getTotalAccidented());
				getPieModel().set("In use", utilizationSummary.getInuse());
				getPieModel().set("Not in use", utilizationSummary.getNotinuse());
				getPieModel().set("In workshop", utilizationSummary.getTotalInWorkshop());
				
				if(barModel == null)
					createBarModel();
				maxY = 0;
				ChartSeries distanceCoveredSeries = new ChartSeries();
				distanceCoveredSeries.setLabel("Distance covered");
				for(Vehicle v : utilizationSummary.getVehicles()) {
					if(v.getDistance_covered() > 0) {
						distanceCoveredSeries.set(v.getRegistrationNo(), v.getDistance_covered());
						if(v.getDistance_covered() > maxY)
							maxY = new BigDecimal(v.getDistance_covered()).longValue() + 5;
					} else
						distanceCoveredSeries.set(v.getRegistrationNo(), 0);
				}
				barModel.addSeries(distanceCoveredSeries);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", utilizationSummary.getVehicles().size() + " vehicle(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "No vehicle found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} else {
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Please supply date range!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void allVehicles() {
		setVehicles(null);
		GeneralDAO gDAO = new GeneralDAO();
		
		Vector<Fleet> fleets = null;
		Query q = gDAO.createQuery("Select e from Fleet e where e.partner=:partner");
		q.setParameter("partner", getPartner());
		Object obj = gDAO.search(q, 0);
		if(obj != null)
			fleets = (Vector<Fleet>)obj;
		
		String str = "Select e from Vehicle e where e.active=:active and e.partner=:partner";
		
		q = gDAO.createQuery(str);
		q.setParameter("active", true);
		q.setParameter("partner", getPartner());
		
		obj = gDAO.search(q, 0);
		if(obj != null)
		{
			setVehicles((Vector<Vehicle>)obj);
			
			if(fleets != null && getVehicles() != null) {
				for(Fleet e : fleets) {
					double value = 0;
					for(Vehicle v : getVehicles()) {
						if(v != null && v.getFleet() != null &&
								v.getFleet().getId().longValue() == e.getId().longValue()) {
							value += 1;
						}
					}
					if(value > 0) {
						if(getPieModel() == null)
							createPieModel();
						
						getPieModel().set(e.getName(), value);
					}
				}
			}
			
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getVehicles().size() + " vehicle(s) found!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			
			setReport_title("All Vehicles by Fleet");
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No vehicle found!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void drivingEvents() {
		resetReportInfo();
		if(getPartner() != null) {
			if(getStart_dt() != null && getEnd_dt() != null) {
				setVehiclesDrivingInfoReport(null);
				setReport_title("Driving Behavior Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
				if(getReportType().equalsIgnoreCase("Summary")) {
					
				} else if(getReportType().equalsIgnoreCase("Detail")) {
					
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchFleetCosts()
	{
		resetReportInfo();
		
		if(getPartner() != null)
		{
			setPartnerFleets(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from Fleet e where e.partner=:partner";
			Query q = gDAO.createQuery(str);
			q.setParameter("partner", getPartner());
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				Vector<Fleet> flts = (Vector<Fleet>)drvs;
				for(Fleet f : flts)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("fleet", f);
					params.put("active", true);
					Object vObj = gDAO.search("Vehicle", params);
					if(vObj != null)
					{
						f.setVehicles((Vector<Vehicle>)vObj);
						BigDecimal sum = new BigDecimal(0);
						for(Vehicle v : f.getVehicles())
						{
							if(v.getPurchaseAmt() != null)
								sum = sum.add(v.getPurchaseAmt());
						}
						f.setFleetCost(sum);
					}
				}
				setPartnerFleets(flts);
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getPartnerFleets().size() + " fleet(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Fleet Cost Analysis Report");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No fleet found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchDriversByRegion()
	{
		resetReportInfo();
		
		if(getPartner() != null && getRegion_id() != null)
		{
			setDriversByRegion(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from PartnerDriver e where e.partner=:partner and e.personel.region.id=:region_id";
			
			Query q = gDAO.createQuery(str);
			q.setParameter("partner", getPartner());
			q.setParameter("region_id", getRegion_id());
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setDriversByRegion((Vector<PartnerDriver>)drvs);
				for(PartnerDriver pd : getDriversByRegion())
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("driver", pd);
					params.put("active", true);
					Object pdvObj = gDAO.search("VehicleDriver", params);
					if(pdvObj != null)
					{
						Vector<VehicleDriver> vdList = (Vector<VehicleDriver>)pdvObj;
						for(VehicleDriver vd : vdList)
						{
							pd.setVehicle(vd.getVehicle());
						}
					}
				}
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getDriversByRegion().size() + " driver(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Drivers by Region Report");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No driver found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchDriversByYears()
	{
		resetReportInfo();
		
		if(getPartner() != null)
		{
			setDriversByYears(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from PartnerDriver e where e.partner=:partner order by e.personel.hiredDate";
			
			Query q = gDAO.createQuery(str);
			q.setParameter("partner", getPartner());
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				Vector<PartnerDriver> partnerDrivers = (Vector<PartnerDriver>)drvs;
				setDriversByYears(new Vector<PartnerDriver>());
				Calendar c = Calendar.getInstance();
				for(PartnerDriver pd : partnerDrivers)
				{
					Hashtable<String, Object> params = new Hashtable<String, Object>();
					params.put("driver", pd);
					params.put("active", true);
					Object pdvObj = gDAO.search("VehicleDriver", params);
					if(pdvObj != null)
					{
						Vector<VehicleDriver> vdList = (Vector<VehicleDriver>)pdvObj;
						for(VehicleDriver vd : vdList)
						{
							pd.setVehicle(vd.getVehicle());
						}
					}
					
					if(pd.getPersonel().getHiredDate() != null)
					{
						Calendar pdc = Calendar.getInstance();
						pdc.setTime(pd.getPersonel().getHiredDate());
						pd.setYearsOfService(c.get(Calendar.YEAR)-pdc.get(Calendar.YEAR));
					}
					getDriversByYears().add(pd);
				}
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getDriversByYears().size() + " driver(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Drivers by Years of Service Report");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No driver found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void search()
	{
		resetReportInfo();
		
		if(getPartner() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getPartner());
			
			if(getRegion_id() != null)
			{
				Object regionObj = gDAO.find(Region.class, getRegion_id());
				if(regionObj != null)
				{
					params.put("personel.region", (Region)regionObj);
				}
			}
			if(getDepartment_id() != null)
			{
				Object deptObj = gDAO.find(Department.class, getDepartment_id());
				if(deptObj != null)
				{
					params.put("personel.department", (Department)deptObj);
				}
			}
			
			Object dpsObj = gDAO.search("PartnerUser", params);
			if(dpsObj != null)
			{
				allUsers = (Vector<PartnerUser>)dpsObj;
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", allUsers.size() + " user(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("All User Report");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No user found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchDueDrvLicense()
	{
		resetReportInfo();
		
		if(getPartner() != null)
		{
			setDueDriversLic(new Vector<DriverLicense>());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Calendar c = Calendar.getInstance();
			Calendar c2 = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, -30);
			
			Query q = gDAO.createQuery("Select e from DriverLicense e where e.driver.partner=:partner and e.expired=:expired and e.active=:active and (e.lic_end_dt > :start_dt and e.lic_end_dt < :end_date)");
			q.setParameter("partner", getPartner());
			q.setParameter("expired", true);
			q.setParameter("active", true);
			q.setParameter("start_dt", c.getTime());
			q.setParameter("end_date", c2.getTime());
			
			Object licsObj = gDAO.search(q, 0);
			if(licsObj != null)
			{
				Vector<DriverLicense> lics = (Vector<DriverLicense>)licsObj;
				getDueDriversLic().addAll(lics);
			}
			
			c = Calendar.getInstance();
			c2 = Calendar.getInstance();
			c2.add(Calendar.DAY_OF_MONTH, 30);
			
			q = gDAO.createQuery("Select e from DriverLicense e where e.driver.partner=:partner and e.expired=:expired and e.active=:active and (e.lic_end_dt > :start_dt and e.lic_end_dt < :end_date)");
			q.setParameter("partner", getPartner());
			q.setParameter("expired", false);
			q.setParameter("active", true);
			q.setParameter("start_dt", c.getTime());
			q.setParameter("end_date", c2.getTime());
			
			licsObj = gDAO.search(q, 0);
			if(licsObj != null)
			{
				Vector<DriverLicense> lics = (Vector<DriverLicense>)licsObj;
				getDueDriversLic().addAll(lics);
			}
			
			if(getDueDriversLic().size() > 0)
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getDueDriversLic().size() + " recent/up-coming expired driver's license(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Driver License Due");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No recent/up-coming expired driver's license found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchAccidentsByBrand()
	{
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && fleetBean != null && fleetBean.getPartner() != null)
		{
			setPartner(fleetBean.getPartner());
			setPieModel(null);
			setBrandAccidents(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Vector<VehicleModel> models = null;
			Query q = gDAO.createQuery("Select e from VehicleModel e where e.partner=:partner");
			q.setParameter("partner", getPartner());
			Object obj = gDAO.search(q, 0);
			if(obj != null)
				models = (Vector<VehicleModel>)obj;
			
			String str = "Select e from VehicleAccident e where (e.accident_dt between :start_dt and :end_dt) and e.vehicle.partner=:partner";
			
			VehicleModel vm = null;
			if(getVehicleModel_id() != null && getVehicleModel_id() > 0) {
				Object vmObj = gDAO.find(VehicleModel.class, getVehicleModel_id());
				if(vmObj != null)
					vm = (VehicleModel)vmObj;
			}
			
			if(getVehicleModel_id() != null && getVehicleModel_id() > 0)
				str += " and e.vehicle.model.id = :model_id";
			
			q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			if(getVehicleModel_id() != null && getVehicleModel_id() > 0)
				q.setParameter("model_id", getVehicleModel_id());
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setBrandAccidents((Vector<VehicleAccident>)drvs);
				
				if(models != null && getBrandAccidents() != null) {
					for(VehicleModel e : models) {
						double value = 0;
						for(VehicleAccident va : getBrandAccidents()) {
							if(va.getVehicle() != null && va.getVehicle().getModel() != null &&
									va.getVehicle().getModel().getId() != null && 
									va.getVehicle().getModel().getId().longValue() == e.getId().longValue()) {
								value += 1;
							}
						}
						if(value > 0) {
							if(getPieModel() == null)
								createPieModel();
							
							getPieModel().set(e.getName() + "-" + e.getYear(), value);
						}
					}
				}
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getBrandAccidents().size() + " accident(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Accident Report by Brand: " + ((vm != null) ? vm.getName() : "All"));
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No accident found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} else {
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Could not retrieve partner!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void searchAccidentsByStatus()
	{
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null && getAccidentStatus() != null)
		{
			setStatusAccidents(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from VehicleAccident e where (e.accident_dt between :start_dt and :end_dt) and e.vehicle.partner=:partner";
			
			if(getAccidentStatus() != null && getAccidentStatus().trim().length() > 0)
				str += " and e.repairApprovedDesc = :repairApprovedDesc";
			
			Query q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			if(getAccidentStatus() != null && getAccidentStatus().trim().length() > 0)
				q.setParameter("repairApprovedDesc", getAccidentStatus());
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setStatusAccidents((Vector<VehicleAccident>)drvs);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getStatusAccidents().size() + " accident(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Accident Report by Status: " + getAccidentStatus());
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No accident found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void searchAccidentsByDriver()
	{
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null)
		{
			setPieModel(null);
			setDriverAccidents(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Vector<PartnerDriver> drivers = null;
			Query q = gDAO.createQuery("Select e from PartnerDriver e where e.partner=:partner");
			q.setParameter("partner", getPartner());
			Object obj = gDAO.search(q, 0);
			if(obj != null)
				drivers = (Vector<PartnerDriver>)obj;
			
			String str = "Select e from VehicleAccident e where (e.accident_dt between :start_dt and :end_dt) and e.vehicle.partner=:partner";
			PartnerDriver d = null;
			try {
			if(getDriver_id() != null && getDriver_id() > 0)
				d = (PartnerDriver) gDAO.find(PartnerDriver.class, getDriver_id());
			} catch(Exception ex){}
			
			if(getDriver_id() != null && getDriver_id() > 0)
				str += " and e.assignedDriver = :assignedDriver";
			
			q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			if(getDriver_id() != null && getDriver_id() > 0)
				q.setParameter("assignedDriver", d);
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setDriverAccidents((Vector<VehicleAccident>)drvs);
				
				if(drivers != null && getDriverAccidents() != null) {
					for(PartnerDriver e : drivers) {
						double value = 0;
						for(VehicleAccident va : getDriverAccidents()) {
							if(va.getAssignedDriver() != null &&
									va.getAssignedDriver().getId().longValue() == e.getId().longValue()) {
								value += 1;
							}
						}
						if(value > 0) {
							if(getPieModel() == null)
								createPieModel();
							
							getPieModel().set(e.getPersonel().getFirstname() + "-" + e.getPersonel().getLastname(), value);
						}
					}
				}
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getDriverAccidents().size() + " accident(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Accident Report by Driver: " + ((d != null) ? d.getPersonel().getFirstname() + " " + d.getPersonel().getLastname() : "All"));
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No accident found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void searchAccidents()
	{
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null)
		{
			setVehicleAccidents(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from VehicleAccident e where (e.accident_dt between :start_dt and :end_dt) and e.vehicle.partner=:partner";
			Vehicle v = null;
			if(getVehicle_id() != null && getVehicle_id() > 0)
			{
				try
				{
					v = (Vehicle)gDAO.find(Vehicle.class, getVehicle_id());
				}
				catch(Exception ex){}
			}
			if(getVehicle_id() != null && getVehicle_id() > 0)
				str += " and e.vehicle = :vehicle";
			
			Query q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			if(getVehicle_id() != null && getVehicle_id() > 0)
				q.setParameter("vehicle", v);
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setVehicleAccidents((Vector<VehicleAccident>)drvs);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getVehicleAccidents().size() + " accident(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Accident Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No accident found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchVBrands()
	{
		resetReportInfo();
		
		if(getPartner() != null)
		{
			setVehiclesByBrand(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Vector<VehicleModel> models = null;
			Query q = gDAO.createQuery("Select e from VehicleModel e where e.partner=:partner");
			q.setParameter("partner", getPartner());
			Object obj = gDAO.search(q, 0);
			if(obj != null)
				models = (Vector<VehicleModel>)obj;
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getPartner());
			params.put("active", true);
			
			if(getFleet_id() != null)
			{
				Object fleetObj = gDAO.find(Fleet.class, getFleet_id());
				if(fleetObj != null)
				{
					params.put("fleet", (Fleet)fleetObj);
				}
			}
			
			Object vehicles = gDAO.search("Vehicle", params);
			if(vehicles != null)
			{
				setVehiclesByBrand((Vector<Vehicle>)vehicles);
				
				if(models != null && getVehiclesByBrand() != null) {
					for(VehicleModel e : models) {
						double value = 0;
						for(Vehicle v : getVehiclesByBrand()) {
							if(v != null && v.getModel().getId().longValue() == e.getId().longValue()) {
								value += 1;
							}
						}
						if(value > 0) {
							if(getPieModel() == null)
								createPieModel();
							
							getPieModel().set(e.getName() + "-" + e.getYear(), value);
						}
					}
				}
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getVehiclesByBrand().size() + " vehicle(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Vehicle Brands Report");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No vehicles found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchAccidentsSummary() {
		resetReportInfo();
		setAccidentsSummaryReport(null);
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null) {
			if(getFleet_id() != null && getFleet_id() > 0) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Vector<String[]> vlist = new Vector<String[]>();
			if(getVehicle_id() != null && getVehicle_id() > 0) {
				Object obj = gDAO.find(Vehicle.class, getVehicle_id());
				if(obj != null) {
					Vehicle v = (Vehicle)obj;
					String[] e = new String[2];
					e[0] = ""+v.getId();
					e[1] = v.getRegistrationNo();
					vlist.add(e);
				}
			} else {
				Query q = gDAO.createQuery("Select e from Vehicle e where e.fleet.id=:fleet_id");
				q.setParameter("fleet_id", getFleet_id());
				Object obj = gDAO.search(q, 0);
				if(obj != null) {
					Vector<Vehicle> list = (Vector<Vehicle>)obj;
					for(Vehicle v : list) {
						String[] e = new String[2];
						e[0] = ""+v.getId();
						e[1] = v.getRegistrationNo();
						vlist.add(e);
					}
				}
			}
			
			Calendar start_can = Calendar.getInstance(), end_can = Calendar.getInstance();
			start_can.setTime(getStart_dt());
			start_can.set(Calendar.HOUR_OF_DAY, 0);
			start_can.set(Calendar.MINUTE, 0);
			start_can.set(Calendar.SECOND, 0);
			start_can.set(Calendar.MILLISECOND, 0);
			end_can.setTime(getEnd_dt());
			end_can.set(Calendar.HOUR_OF_DAY, end_can.getMaximum(Calendar.HOUR_OF_DAY));
			end_can.set(Calendar.MINUTE, end_can.getMaximum(Calendar.MINUTE));
			end_can.set(Calendar.SECOND, end_can.getMaximum(Calendar.SECOND));
			end_can.set(Calendar.MILLISECOND, end_can.getMaximum(Calendar.MILLISECOND));
			if(getRgroup() == 1) { //daily
				
			} else if(getRgroup() == 2) { //weekly
				start_can.set(Calendar.DAY_OF_WEEK, start_can.getFirstDayOfWeek());
				end_can.set(Calendar.DAY_OF_WEEK, end_can.getMaximum(Calendar.DAY_OF_WEEK));
			} else if(getRgroup() == 3) { //monthly
				start_can.set(Calendar.DAY_OF_MONTH, 1);
				end_can.set(Calendar.DAY_OF_MONTH, end_can.getMaximum(Calendar.DAY_OF_MONTH));
			}
			
			Vector<String[]> list = new Vector<String[]>(); // Vehicle | Start Date | End Date | No of Time | Cost
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
			while(start_can.before(end_can)) {
				Calendar search_end_can = Calendar.getInstance();
				search_end_can.setTime(start_can.getTime());
				if(getRgroup() == 1) { //daily
					
				} else if(getRgroup() == 2) { //weekly
					search_end_can.set(Calendar.DAY_OF_WEEK, search_end_can.getMaximum(Calendar.DAY_OF_WEEK));
				} else if(getRgroup() == 3) { //monthly
					search_end_can.set(Calendar.DAY_OF_MONTH, search_end_can.getMaximum(Calendar.DAY_OF_MONTH));
				}
				search_end_can.set(Calendar.HOUR_OF_DAY, search_end_can.getMaximum(Calendar.HOUR_OF_DAY));
				search_end_can.set(Calendar.MINUTE, search_end_can.getMaximum(Calendar.MINUTE));
				search_end_can.set(Calendar.SECOND, search_end_can.getMaximum(Calendar.SECOND));
				search_end_can.set(Calendar.MILLISECOND, search_end_can.getMaximum(Calendar.MILLISECOND));
				
				BigDecimal total_cost = new BigDecimal(0);
				String[] date_tow = new String[]{"Total", "", "", "", ""};
				
				for(String[] e : vlist) {
					String[] r = new String[5];
					r[0] = e[1];
					r[1] = sdf.format(start_can.getTime());
					r[2] = sdf.format(search_end_can.getTime());
					int count = 0;
					BigDecimal cost = new BigDecimal(0);
					Query q = gDAO.createQuery("Select e from VehicleAccidentRepair e where (e.accident.accident_dt between :start_dt and :end_dt) and e.accident.vehicle.id=:v_id");
					q.setParameter("v_id", Long.parseLong(e[0]));
					q.setParameter("start_dt", start_can.getTime());
					q.setParameter("end_dt", search_end_can.getTime());
					Object obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<VehicleAccidentRepair> objList = (Vector<VehicleAccidentRepair>)obj;
						
						for(VehicleAccidentRepair var : objList) {
							if(var.getRepairAmt() > 0) {
								count += 1;
								cost = cost.add(new BigDecimal(var.getRepairAmt()));
								cost.setScale(2);
							}
						}
					}
					if(count > 0) {
						total_cost = total_cost.add(cost);
						total_cost = total_cost.setScale(2);
						r[3] = ""+count;
						r[4] = cost.toPlainString();
						list.add(r);
					}
				}
				if(total_cost.doubleValue() > 0) {
					date_tow[4] = total_cost.toPlainString();
					list.add(date_tow);
				}
				
				if(getRgroup() == 1) { //daily
					start_can.add(Calendar.DATE, 1);
				} else if(getRgroup() == 2) { //weekly
					start_can.add(Calendar.WEEK_OF_YEAR, 1);
				} else if(getRgroup() == 3) { //monthly
					start_can.add(Calendar.MONTH, 1);
				}
			}
			
			if(list.size() > 0) {
				setAccidentsSummaryReport(list);
				
				if(vlist != null && getAccidentsSummaryReport() != null) {
					for(String[] e : vlist) {
						double value = 0;
						for(String[] va : getAccidentsSummaryReport()) {
							if(va[0].equals(e[1])) {
								try {
									value += Double.parseDouble(va[4]);
								} catch(Exception ex) { ex.printStackTrace(); }
							}
						}
						if(value > 0) {
							if(getPieModel() == null)
								createPieModel();
							
							getPieModel().set(e[1], value);
						}
					}
				}
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getAccidentsSummaryReport().size() + " record(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Accidents Summary Report");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No record found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			
			gDAO.destroy();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchExpenseSummary() {
		resetReportInfo();
		setExpenseSummaryReport(null);
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			Vector<String[]> vlist = new Vector<String[]>(), plist = new Vector<String[]>();
			
			Query q = gDAO.createQuery("Select e from Vehicle e where e.partner.id=:partner_id");
			q.setParameter("partner_id", getPartner().getId());
			Object obj = gDAO.search(q, 0);
			if(obj != null) {
				Vector<Vehicle> list = (Vector<Vehicle>)obj;
				for(Vehicle v : list) {
					String[] e = new String[2];
					e[0] = ""+v.getId();
					e[1] = v.getRegistrationNo();
					vlist.add(e);
				}
			}
			/*q = gDAO.createQuery("Select e from PartnerPersonel e where e.partner.id=:partner_id");
			q.setParameter("partner_id", getPartner().getId());
			obj = gDAO.search(q, 0);
			if(obj != null) {
				Vector<PartnerPersonel> list = (Vector<PartnerPersonel>)obj;
				for(PartnerPersonel p : list) {
					String[] e = new String[2];
					e[0] = ""+p.getId();
					e[1] = p.getFirstname() + " " + p.getLastname();
					plist.add(e);
				}
			}*/
			
			Vector<ExpenseType> etlist = new Vector<ExpenseType>();
			q = gDAO.createQuery("Select e from ExpenseType e where e.partner.id=:partner_id");
			q.setParameter("partner_id", getPartner().getId());
			obj = gDAO.search(q, 0);
			if(obj != null)
				etlist = (Vector<ExpenseType>)obj;
			
			Calendar start_can = Calendar.getInstance(), end_can = Calendar.getInstance();
			start_can.setTime(getStart_dt());
			start_can.set(Calendar.HOUR_OF_DAY, 0);
			start_can.set(Calendar.MINUTE, 0);
			start_can.set(Calendar.SECOND, 0);
			start_can.set(Calendar.MILLISECOND, 0);
			end_can.setTime(getEnd_dt());
			end_can.set(Calendar.HOUR_OF_DAY, end_can.getMaximum(Calendar.HOUR_OF_DAY));
			end_can.set(Calendar.MINUTE, end_can.getMaximum(Calendar.MINUTE));
			end_can.set(Calendar.SECOND, end_can.getMaximum(Calendar.SECOND));
			end_can.set(Calendar.MILLISECOND, end_can.getMaximum(Calendar.MILLISECOND));
			if(getRgroup() == 1) { //daily
				
			} else if(getRgroup() == 2) { //weekly
				start_can.set(Calendar.DAY_OF_WEEK, start_can.getFirstDayOfWeek());
				end_can.set(Calendar.DAY_OF_WEEK, end_can.getMaximum(Calendar.DAY_OF_WEEK));
			} else if(getRgroup() == 3) { //monthly
				start_can.set(Calendar.DAY_OF_MONTH, 1);
				end_can.set(Calendar.DAY_OF_MONTH, end_can.getMaximum(Calendar.DAY_OF_MONTH));
			}
			
			Vector<String[]> list = new Vector<String[]>(); // Expense Type	| Start Date | End Date | Beneficiary | Cost
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
			while(start_can.before(end_can)) {
				Calendar search_end_can = Calendar.getInstance();
				search_end_can.setTime(start_can.getTime());
				if(getRgroup() == 1) { //daily
					
				} else if(getRgroup() == 2) { //weekly
					search_end_can.set(Calendar.DAY_OF_WEEK, search_end_can.getMaximum(Calendar.DAY_OF_WEEK));
				} else if(getRgroup() == 3) { //monthly
					search_end_can.set(Calendar.DAY_OF_MONTH, search_end_can.getMaximum(Calendar.DAY_OF_MONTH));
				}
				search_end_can.set(Calendar.HOUR_OF_DAY, search_end_can.getMaximum(Calendar.HOUR_OF_DAY));
				search_end_can.set(Calendar.MINUTE, search_end_can.getMaximum(Calendar.MINUTE));
				search_end_can.set(Calendar.SECOND, search_end_can.getMaximum(Calendar.SECOND));
				search_end_can.set(Calendar.MILLISECOND, search_end_can.getMaximum(Calendar.MILLISECOND));
				
				BigDecimal total_cost = new BigDecimal(0);
				String[] date_tow = new String[]{"Total", "", "", "", ""};
				
				for(ExpenseType et : etlist) {
					for(String[] e : vlist) {
						String[] r = new String[5];
						r[0] = et.getName();
						r[1] = sdf.format(start_can.getTime());
						r[2] = sdf.format(search_end_can.getTime());
						r[3] = e[1];
						BigDecimal cost = new BigDecimal(0);
						int count = 0;
						q = gDAO.createQuery("Select e from Expense e where (e.expense_dt between :start_dt and :end_dt) and e.type.id=:type_id and e.vehicle.id=:v_id");
						q.setParameter("type_id", et.getId());
						q.setParameter("v_id", Long.parseLong(e[0]));
						q.setParameter("start_dt", start_can.getTime());
						q.setParameter("end_dt", search_end_can.getTime());
						obj = gDAO.search(q, 0);
						if(obj != null) {
							Vector<Expense> objList = (Vector<Expense>)obj;
							for(Expense ex : objList) {
								if(ex.getAmount() > 0) {
									count += 1;
									cost = cost.add(new BigDecimal(ex.getAmount()));
									cost = cost.setScale(2);
								}
							}
						}
						if(count > 0) {
							r[4] = cost.toPlainString();
							list.add(r);
						}
					}
					for(String[] e : plist) {
						String[] r = new String[5];
						r[0] = et.getName();
						r[1] = sdf.format(start_can.getTime());
						r[2] = sdf.format(search_end_can.getTime());
						r[3] = e[1];
						BigDecimal cost = new BigDecimal(0);
						int count = 0;
						q = gDAO.createQuery("Select e from Expense e where (e.expense_dt between :start_dt and :end_dt) and e.type.id=:type_id and e.personel.id=:p_id");
						q.setParameter("type_id", et.getId());
						q.setParameter("p_id", Long.parseLong(e[0]));
						q.setParameter("start_dt", start_can.getTime());
						q.setParameter("end_dt", search_end_can.getTime());
						obj = gDAO.search(q, 0);
						if(obj != null) {
							Vector<Expense> objList = (Vector<Expense>)obj;
							for(Expense ex : objList) {
								if(ex.getAmount() > 0) {
									count += 1;
									cost = cost.add(new BigDecimal(ex.getAmount()));
									cost = cost.setScale(2);
								}
							}
						}
						if(count > 0) {
							total_cost = total_cost.add(cost);
							total_cost = total_cost.setScale(2);
							r[4] = cost.toPlainString();
							list.add(r);
						}
					}
				}
				if(total_cost.doubleValue() > 0) {
					date_tow[4] = total_cost.toPlainString();
					list.add(date_tow);
				}
				
				if(getRgroup() == 1) { //daily
					start_can.add(Calendar.DATE, 1);
				} else if(getRgroup() == 2) { //weekly
					start_can.add(Calendar.WEEK_OF_YEAR, 1);
				} else if(getRgroup() == 3) { //monthly
					start_can.add(Calendar.MONTH, 1);
				}
			}
			
			if(list.size() > 0) {
				setExpenseSummaryReport(list);
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getExpenseSummaryReport().size() + " record(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Expense Summary Report");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No record found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			
			gDAO.destroy();
		}
	}
	
	public void downloadExpenseSummaryPDF() {
		try {
			MaintCostDataSource maintCostDS = MaintCostDataSource.getInstance();
			Vector<MaintCost> data = new Vector<MaintCost>();
			for(String[] e : getExpenseSummaryReport()) {
				if(e[0] != null && !e[0].equalsIgnoreCase("Total")) {
					MaintCostDataSource.MaintCost mc = maintCostDS.new MaintCost(e[0], e[1], e[2], Double.parseDouble(e[4]), e[3]);
					data.add(mc);
				}
			}
			maintCostDS.setData(data);
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("mainHeader", "FMS - Expense Report");
			parameters.put("subHeader", "Expense Summary Report");
			parameters.put("reportDesc", "This is a summary report of expenses.");
			parameters.put("keyHeader", "Expense Type");
			parameters.put("col2Header", "Start Date");
			parameters.put("col3Header", "End Date");
			parameters.put("col4Header", "Cost");
			parameters.put("col5Header", "Beneficiary");
			parameters.put("totalLabel", "Total Cost: ");
			parameters.put("pieChartDesc", "Pie Chart showing the various distribution of expense cost per expense type for the selected period.");
			
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(maintCostDS.getCollectionList());
			
			downloadJasperPDF(parameters, "expense_summary_report.pdf", "/resources/jasper/generic5Col.jasper", ds);
		} catch(Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchPartsServicing() {
		resetReportInfo();
		setPartsServicingReport(null);
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null) {
			if(getFleet_id() != null && getFleet_id() > 0) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Vector<String[]> vlist = new Vector<String[]>();
			if(getVehicle_id() != null && getVehicle_id() > 0) {
				Object obj = gDAO.find(Vehicle.class, getVehicle_id());
				if(obj != null) {
					Vehicle v = (Vehicle)obj;
					String[] e = new String[2];
					e[0] = ""+v.getId();
					e[1] = v.getRegistrationNo();
					vlist.add(e);
				}
			} else {
				Query q = gDAO.createQuery("Select e from Vehicle e where e.fleet.id=:fleet_id");
				q.setParameter("fleet_id", getFleet_id());
				Object obj = gDAO.search(q, 0);
				if(obj != null) {
					Vector<Vehicle> list = (Vector<Vehicle>)obj;
					for(Vehicle v : list) {
						String[] e = new String[2];
						e[0] = ""+v.getId();
						e[1] = v.getRegistrationNo();
						vlist.add(e);
					}
				}
			}
			
			Calendar start_can = Calendar.getInstance(), end_can = Calendar.getInstance();
			start_can.setTime(getStart_dt());
			start_can.set(Calendar.HOUR_OF_DAY, 0);
			start_can.set(Calendar.MINUTE, 0);
			start_can.set(Calendar.SECOND, 0);
			start_can.set(Calendar.MILLISECOND, 0);
			end_can.setTime(getEnd_dt());
			end_can.set(Calendar.HOUR_OF_DAY, end_can.getMaximum(Calendar.HOUR_OF_DAY));
			end_can.set(Calendar.MINUTE, end_can.getMaximum(Calendar.MINUTE));
			end_can.set(Calendar.SECOND, end_can.getMaximum(Calendar.SECOND));
			end_can.set(Calendar.MILLISECOND, end_can.getMaximum(Calendar.MILLISECOND));
			if(getRgroup() == 1) { //daily
				
			} else if(getRgroup() == 2) { //weekly
				start_can.set(Calendar.DAY_OF_WEEK, start_can.getFirstDayOfWeek());
				end_can.set(Calendar.DAY_OF_WEEK, end_can.getMaximum(Calendar.DAY_OF_WEEK));
			} else if(getRgroup() == 3) { //monthly
				start_can.set(Calendar.DAY_OF_MONTH, 1);
				end_can.set(Calendar.DAY_OF_MONTH, end_can.getMaximum(Calendar.DAY_OF_MONTH));
			}
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
				setPeriod(sdf.format(getStart_dt()) + " - " + sdf.format(getEnd_dt()));
			} catch(Exception ex){ ex.printStackTrace(); }
			
			Vector<Item> items = new Vector<Item>();
			Query q = gDAO.createQuery("Select e from Item e where e.partner.id=:partner_id");
			q.setParameter("partner_id", getPartner().getId());
			Object obj = gDAO.search(q, 0);
			if(obj != null)
				items = (Vector<Item>)obj;
			
			Vector<String[]> list = new Vector<String[]>(); // Vehicle | Start Date | End Date | Part |	No of time | Cost
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
			while(start_can.before(end_can)) {
				Calendar search_end_can = Calendar.getInstance();
				search_end_can.setTime(start_can.getTime());
				if(getRgroup() == 1) { //daily
					
				} else if(getRgroup() == 2) { //weekly
					search_end_can.set(Calendar.DAY_OF_WEEK, search_end_can.getMaximum(Calendar.DAY_OF_WEEK));
				} else if(getRgroup() == 3) { //monthly
					search_end_can.set(Calendar.DAY_OF_MONTH, search_end_can.getMaximum(Calendar.DAY_OF_MONTH));
				}
				search_end_can.set(Calendar.HOUR_OF_DAY, search_end_can.getMaximum(Calendar.HOUR_OF_DAY));
				search_end_can.set(Calendar.MINUTE, search_end_can.getMaximum(Calendar.MINUTE));
				search_end_can.set(Calendar.SECOND, search_end_can.getMaximum(Calendar.SECOND));
				search_end_can.set(Calendar.MILLISECOND, search_end_can.getMaximum(Calendar.MILLISECOND));
				
				BigDecimal total_cost = new BigDecimal(0);
				String[] date_tow = new String[]{"Total", "", "", "", "", ""};
				
				for(Item itm : items) {
					for(String[] e : vlist) {
						String[] r = new String[6];
						r[0] = e[1];
						r[1] = sdf.format(start_can.getTime());
						r[2] = sdf.format(search_end_can.getTime());
						r[3] = itm.getName();
						BigDecimal cost = new BigDecimal(0);
						int count = 0;
						q = gDAO.createQuery("Select e from WorkOrderItem e where (e.workOrderVehicle.workOrder.approve_dt between :start_dt and :end_dt) and e.item.id=:itm_id and e.workOrderVehicle.vehicle.id=:v_id and e.workOrderVehicle.workOrder.workOrderType=:wrk_type and e.action=:action");
						q.setParameter("itm_id", itm.getId());
						q.setParameter("wrk_type", "Adhoc");
						q.setParameter("v_id", Long.parseLong(e[0]));
						q.setParameter("start_dt", start_can.getTime());
						q.setParameter("end_dt", search_end_can.getTime());
						q.setParameter("action", "REPAIR");
						obj = gDAO.search(q, 0);
						if(obj != null) {
							Vector<WorkOrderItem> objList = (Vector<WorkOrderItem>)obj;
							for(WorkOrderItem woi : objList) {
								if(woi.getInitEstAmount() > 0) {
									count += 1;
									cost = cost.add(new BigDecimal(woi.getInitEstAmount()));
									cost = cost.setScale(2);
								}
							}
						}
						if(count > 0) {
							total_cost = total_cost.add(cost);
							total_cost = total_cost.setScale(2);
							r[4] = ""+count;
							r[5] = cost.toPlainString();
							list.add(r);
						}
					}
				}
				if(total_cost.doubleValue() > 0) {
					date_tow[5] = total_cost.toPlainString();
					list.add(date_tow);
				}
				
				if(getRgroup() == 1) { //daily
					start_can.add(Calendar.DATE, 1);
				} else if(getRgroup() == 2) { //weekly
					start_can.add(Calendar.WEEK_OF_YEAR, 1);
				} else if(getRgroup() == 3) { //monthly
					start_can.add(Calendar.MONTH, 1);
				}
			}
			
			if(list.size() > 0) {
				setPartsServicingReport(list);
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getPartsServicingReport().size() + " record(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Vehicle Parts Servicing Report");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No record found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			
			gDAO.destroy();
			}
		}
	}
	
	public void downloadPartsServicingPDF() {
		try {
			MaintCostDataSource maintCostDS = MaintCostDataSource.getInstance();
			Vector<MaintCost> data = new Vector<MaintCost>();
			for(String[] e : getPartsServicingReport()) {
				if(e[0] != null && !e[0].equalsIgnoreCase("Total")) {
					MaintCostDataSource.MaintCost mc = maintCostDS.new MaintCost(e[0], e[1], e[2], Double.parseDouble(e[5]), e[3], Integer.parseInt(e[4]));
					data.add(mc);
				}
			}
			maintCostDS.setData(data);
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("reportDesc", "Parts Servicing Report");
			
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(maintCostDS.getCollectionList());
			
			downloadJasperPDF(parameters, "parts_servicing.pdf", "/resources/jasper/MaintParts.jasper", ds);
		} catch(Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchPartsReplacements() {
		resetReportInfo();
		setPartsReplacementReport(null);
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null) {
			if(getFleet_id() != null && getFleet_id() > 0) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Vector<String[]> vlist = new Vector<String[]>();
			if(getVehicle_id() != null && getVehicle_id() > 0) {
				Object obj = gDAO.find(Vehicle.class, getVehicle_id());
				if(obj != null) {
					Vehicle v = (Vehicle)obj;
					String[] e = new String[2];
					e[0] = ""+v.getId();
					e[1] = v.getRegistrationNo();
					vlist.add(e);
				}
			} else {
				Query q = gDAO.createQuery("Select e from Vehicle e where e.fleet.id=:fleet_id");
				q.setParameter("fleet_id", getFleet_id());
				Object obj = gDAO.search(q, 0);
				if(obj != null) {
					Vector<Vehicle> list = (Vector<Vehicle>)obj;
					for(Vehicle v : list) {
						String[] e = new String[2];
						e[0] = ""+v.getId();
						e[1] = v.getRegistrationNo();
						vlist.add(e);
					}
				}
			}
			
			Calendar start_can = Calendar.getInstance(), end_can = Calendar.getInstance();
			start_can.setTime(getStart_dt());
			start_can.set(Calendar.HOUR_OF_DAY, 0);
			start_can.set(Calendar.MINUTE, 0);
			start_can.set(Calendar.SECOND, 0);
			start_can.set(Calendar.MILLISECOND, 0);
			end_can.setTime(getEnd_dt());
			end_can.set(Calendar.HOUR_OF_DAY, end_can.getMaximum(Calendar.HOUR_OF_DAY));
			end_can.set(Calendar.MINUTE, end_can.getMaximum(Calendar.MINUTE));
			end_can.set(Calendar.SECOND, end_can.getMaximum(Calendar.SECOND));
			end_can.set(Calendar.MILLISECOND, end_can.getMaximum(Calendar.MILLISECOND));
			if(getRgroup() == 1) { //daily
				setGroupBy("Daily");
			} else if(getRgroup() == 2) { //weekly
				start_can.set(Calendar.DAY_OF_WEEK, start_can.getFirstDayOfWeek());
				end_can.set(Calendar.DAY_OF_WEEK, end_can.getMaximum(Calendar.DAY_OF_WEEK));
			} else if(getRgroup() == 3) { //monthly
				start_can.set(Calendar.DAY_OF_MONTH, 1);
				end_can.set(Calendar.DAY_OF_MONTH, end_can.getMaximum(Calendar.DAY_OF_MONTH));
			}
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
				setPeriod(sdf.format(getStart_dt()) + " - " + sdf.format(getEnd_dt()));
			} catch(Exception ex){ ex.printStackTrace(); }
			
			Vector<Item> items = new Vector<Item>();
			Query q = gDAO.createQuery("Select e from Item e where e.partner.id=:partner_id");
			q.setParameter("partner_id", getPartner().getId());
			Object obj = gDAO.search(q, 0);
			if(obj != null)
				items = (Vector<Item>)obj;
			
			Vector<String[]> list = new Vector<String[]>(); // Vehicle | Start Date | End Date | Part |	No of time | Cost
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
			while(start_can.before(end_can)) {
				Calendar search_end_can = Calendar.getInstance();
				search_end_can.setTime(start_can.getTime());
				if(getRgroup() == 1) { //daily
					
				} else if(getRgroup() == 2) { //weekly
					search_end_can.set(Calendar.DAY_OF_WEEK, search_end_can.getMaximum(Calendar.DAY_OF_WEEK));
				} else if(getRgroup() == 3) { //monthly
					search_end_can.set(Calendar.DAY_OF_MONTH, search_end_can.getMaximum(Calendar.DAY_OF_MONTH));
				}
				search_end_can.set(Calendar.HOUR_OF_DAY, search_end_can.getMaximum(Calendar.HOUR_OF_DAY));
				search_end_can.set(Calendar.MINUTE, search_end_can.getMaximum(Calendar.MINUTE));
				search_end_can.set(Calendar.SECOND, search_end_can.getMaximum(Calendar.SECOND));
				search_end_can.set(Calendar.MILLISECOND, search_end_can.getMaximum(Calendar.MILLISECOND));
				
				BigDecimal total_cost = new BigDecimal(0);
				String[] date_tow = new String[]{"Total", "", "", "", "", ""};
				
				for(Item itm : items) {
					for(String[] e : vlist) {
						String[] r = new String[6];
						r[0] = e[1];
						r[1] = sdf.format(start_can.getTime());
						r[2] = sdf.format(search_end_can.getTime());
						r[3] = itm.getName();
						BigDecimal cost = new BigDecimal(0);
						int count = 0;
						q = gDAO.createQuery("Select e from WorkOrderItem e where (e.workOrderVehicle.workOrder.approve_dt between :start_dt and :end_dt) and e.item.id=:itm_id and e.workOrderVehicle.vehicle.id=:v_id and e.workOrderVehicle.workOrder.workOrderType=:wrk_type and e.action=:action");
						q.setParameter("itm_id", itm.getId());
						q.setParameter("wrk_type", "Adhoc");
						q.setParameter("v_id", Long.parseLong(e[0]));
						q.setParameter("start_dt", start_can.getTime());
						q.setParameter("end_dt", search_end_can.getTime());
						q.setParameter("action", "REPLACE");
						obj = gDAO.search(q, 0);
						if(obj != null) {
							Vector<WorkOrderItem> objList = (Vector<WorkOrderItem>)obj;
							for(WorkOrderItem woi : objList) {
								if(woi.getInitEstAmount() > 0) {
									count += 1;
									cost = cost.add(new BigDecimal(woi.getInitEstAmount()));
									cost = cost.setScale(2);
								}
							}
						}
						if(count > 0) {
							total_cost = total_cost.add(cost);
							total_cost = total_cost.setScale(2);
							r[4] = ""+count;
							r[5] = cost.toPlainString();
							list.add(r);
						}
					}
				}
				if(total_cost.doubleValue() > 0) {
					date_tow[5] = total_cost.toPlainString();
					list.add(date_tow);
				}
				if(getRgroup() == 1) { //daily
					start_can.add(Calendar.DATE, 1);
				} else if(getRgroup() == 2) { //weekly
					start_can.add(Calendar.WEEK_OF_YEAR, 1);
				} else if(getRgroup() == 3) { //monthly
					start_can.add(Calendar.MONTH, 1);
				}
			}
			
			if(list.size() > 0) {
				setPartsReplacementReport(list);
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getPartsReplacementReport().size() + " record(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Vehicle Parts Replacement Report");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No record found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			
			gDAO.destroy();
			}
		}
	}
	
	public void downloadPartsReplacementsPDF() {
		try {
			MaintCostDataSource maintCostDS = MaintCostDataSource.getInstance();
			Vector<MaintCost> data = new Vector<MaintCost>();
			for(String[] e : getPartsReplacementReport()) {
				if(e[0] != null && !e[0].equalsIgnoreCase("Total")) {
					MaintCostDataSource.MaintCost mc = maintCostDS.new MaintCost(e[0], e[1], e[2], Double.parseDouble(e[5]), e[3], Integer.parseInt(e[4]));
					data.add(mc);
				}
			}
			maintCostDS.setData(data);
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("reportDesc", "Parts Replacement Reports");
			
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(maintCostDS.getCollectionList());
			
			downloadJasperPDF(parameters, "parts_replacement.pdf", "/resources/jasper/MaintParts.jasper", ds);
		} catch(Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchMaintCostsByBrands() {
		resetReportInfo();
		setMaintCostByBrandsReport(null);
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			Vector<String> brands = new Vector<String>();
			Vector<String[]> blist = new Vector<String[]>();
			Query q = null;
			if(getFleet_id() != null && getFleet_id() > 0) {
				q = gDAO.createQuery("Select e from Vehicle e where e.fleet.id=:fleet_id");
				q.setParameter("fleet_id", getFleet_id());
			} else {
				q = gDAO.createQuery("Select e from Vehicle e where e.partner.id=:partner_id");
				q.setParameter("partner_id", getPartner().getId());
			}
			Object obj = gDAO.search(q, 0);
			if(obj != null) {
				Vector<Vehicle> list = (Vector<Vehicle>)obj;
				for(Vehicle v : list) {
					if(v.getModel() != null && v.getModel().getMaker() != null && 
							v.getModel().getMaker().getName() != null) {
						String[] e = new String[2];
						e[0] = ""+v.getModel().getMaker().getId();
						e[1] = v.getModel().getMaker().getName();
						if(!brands.contains(e[1])) {
							brands.add(e[1]);
							blist.add(e);
						}
					}
				}
			}
			
			Calendar start_can = Calendar.getInstance(), end_can = Calendar.getInstance();
			start_can.setTime(getStart_dt());
			start_can.set(Calendar.HOUR_OF_DAY, 0);
			start_can.set(Calendar.MINUTE, 0);
			start_can.set(Calendar.SECOND, 0);
			start_can.set(Calendar.MILLISECOND, 0);
			end_can.setTime(getEnd_dt());
			end_can.set(Calendar.HOUR_OF_DAY, end_can.getMaximum(Calendar.HOUR_OF_DAY));
			end_can.set(Calendar.MINUTE, end_can.getMaximum(Calendar.MINUTE));
			end_can.set(Calendar.SECOND, end_can.getMaximum(Calendar.SECOND));
			end_can.set(Calendar.MILLISECOND, end_can.getMaximum(Calendar.MILLISECOND));
			if(getRgroup() == 1) { //daily
				
			} else if(getRgroup() == 2) { //weekly
				start_can.set(Calendar.DAY_OF_WEEK, start_can.getFirstDayOfWeek());
				end_can.set(Calendar.DAY_OF_WEEK, end_can.getMaximum(Calendar.DAY_OF_WEEK));
			} else if(getRgroup() == 3) { //monthly
				start_can.set(Calendar.DAY_OF_MONTH, 1);
				end_can.set(Calendar.DAY_OF_MONTH, end_can.getMaximum(Calendar.DAY_OF_MONTH));
			}
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
				setPeriod(sdf.format(getStart_dt()) + " - " + sdf.format(getEnd_dt()));
			} catch(Exception ex){ ex.printStackTrace(); }
			
			Vector<String[]> list = new Vector<String[]>(); // Reg Number | Start Date | End Date | Cost | Type(Routine and AdHoc, Routine, AdHoc)
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
			while(start_can.before(end_can)) {
				Calendar search_end_can = Calendar.getInstance();
				search_end_can.setTime(start_can.getTime());
				if(getRgroup() == 1) { //daily
					
				} else if(getRgroup() == 2) { //weekly
					search_end_can.set(Calendar.DAY_OF_WEEK, search_end_can.getMaximum(Calendar.DAY_OF_WEEK));
				} else if(getRgroup() == 3) { //monthly
					search_end_can.set(Calendar.DAY_OF_MONTH, search_end_can.getMaximum(Calendar.DAY_OF_MONTH));
				}
				search_end_can.set(Calendar.HOUR_OF_DAY, search_end_can.getMaximum(Calendar.HOUR_OF_DAY));
				search_end_can.set(Calendar.MINUTE, search_end_can.getMaximum(Calendar.MINUTE));
				search_end_can.set(Calendar.SECOND, search_end_can.getMaximum(Calendar.SECOND));
				search_end_can.set(Calendar.MILLISECOND, search_end_can.getMaximum(Calendar.MILLISECOND));
				
				BigDecimal total_cost = new BigDecimal(0);
				String[] date_tow = new String[]{"Total", "", "", "", getMainttype()};
				
				for(String[] e : blist) {
					String[] r = new String[5];
					r[0] = e[1];
					r[1] = sdf.format(start_can.getTime());
					r[2] = sdf.format(search_end_can.getTime());
					if(getMainttype().equalsIgnoreCase("Routine")) {
						q = gDAO.createQuery("Select e from VehicleRoutineMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.model.maker.id=:m_id");
						q.setParameter("m_id", Long.parseLong(e[0]));
						q.setParameter("start_dt", start_can.getTime());
						q.setParameter("end_dt", search_end_can.getTime());
						obj = gDAO.search(q, 0);
						if(obj != null) {
							Vector<VehicleRoutineMaintenance> objList = (Vector<VehicleRoutineMaintenance>)obj;
							BigDecimal cost = new BigDecimal(0);
							for(VehicleRoutineMaintenance vrm : objList) {
								if(vrm.getClosed_amount() != null) {
									cost = cost.add(vrm.getClosed_amount());
									cost.setScale(2);
								}
							}
							if(cost.doubleValue() > 0) {
								total_cost = total_cost.add(cost);
								total_cost = total_cost.setScale(2);
								r[3] = cost.toPlainString();
								r[4] = "Routine";
								list.add(r);
							}
						}
					} else if(getMainttype().equalsIgnoreCase("AdHoc")) {
						q = gDAO.createQuery("Select e from VehicleAdHocMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.model.maker.id=:m_id");
						q.setParameter("m_id", Long.parseLong(e[0]));
						q.setParameter("start_dt", start_can.getTime());
						q.setParameter("end_dt", search_end_can.getTime());
						obj = gDAO.search(q, 0);
						if(obj != null) {
							Vector<VehicleAdHocMaintenance> objList = (Vector<VehicleAdHocMaintenance>)obj;
							BigDecimal cost = new BigDecimal(0);
							for(VehicleAdHocMaintenance vam : objList) {
								if(vam.getClosed_cost() != null) {
									cost = cost.add(vam.getClosed_cost());
									cost.setScale(2);
								}
							}
							if(cost.doubleValue() > 0) {
								total_cost = total_cost.add(cost);
								total_cost = total_cost.setScale(2);
								r[3] = cost.toPlainString();
								r[4] = "AdHoc";
								list.add(r);
							}
						}
					} else if(getMainttype().equalsIgnoreCase("Both")) {
						BigDecimal cost = new BigDecimal(0);
						q = gDAO.createQuery("Select e from VehicleRoutineMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.model.maker.id=:m_id");
						q.setParameter("m_id", Long.parseLong(e[0]));
						q.setParameter("start_dt", start_can.getTime());
						q.setParameter("end_dt", search_end_can.getTime());
						obj = gDAO.search(q, 0);
						if(obj != null) {
							Vector<VehicleRoutineMaintenance> objList = (Vector<VehicleRoutineMaintenance>)obj;
							for(VehicleRoutineMaintenance vrm : objList) {
								if(vrm.getClosed_amount() != null) {
									cost = cost.add(vrm.getClosed_amount());
									cost.setScale(2);
								}
							}
						}
						q = gDAO.createQuery("Select e from VehicleAdHocMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.model.maker.id=:m_id");
						q.setParameter("m_id", Long.parseLong(e[0]));
						q.setParameter("start_dt", start_can.getTime());
						q.setParameter("end_dt", search_end_can.getTime());
						obj = gDAO.search(q, 0);
						if(obj != null) {
							Vector<VehicleAdHocMaintenance> objList = (Vector<VehicleAdHocMaintenance>)obj;
							for(VehicleAdHocMaintenance vam : objList) {
								if(vam.getClosed_cost() != null) {
									cost = cost.add(vam.getClosed_cost());
									cost.setScale(2);
								}
							}
						}
						if(cost.doubleValue() > 0) {
							total_cost = total_cost.add(cost);
							total_cost = total_cost.setScale(2);
							r[3] = cost.toPlainString();
							r[4] = "Routine and AdHoc";
							list.add(r);
						}
					}
				}
				if(total_cost.doubleValue() > 0) {
					date_tow[3] = total_cost.toPlainString();
					list.add(date_tow);
				}
				
				if(getRgroup() == 1) { //daily
					start_can.add(Calendar.DATE, 1);
				} else if(getRgroup() == 2) { //weekly
					start_can.add(Calendar.WEEK_OF_YEAR, 1);
				} else if(getRgroup() == 3) { //monthly
					start_can.add(Calendar.MONTH, 1);
				}
			}
			
			if(list.size() > 0) {
				setMaintCostByBrandsReport(list);
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getMaintCostByBrandsReport().size() + " record(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Maintenance Cost by Brands Report");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No record found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			
			gDAO.destroy();
		}
	}
	
	public void downloadMaintCostsByBrandsPDF() {
		try {
			MaintCostDataSource maintCostDS = MaintCostDataSource.getInstance();
			Vector<MaintCost> data = new Vector<MaintCost>();
			for(String[] e : getMaintCostByBrandsReport()) {
				if(e[0] != null && !e[0].equalsIgnoreCase("Total")) {
					MaintCostDataSource.MaintCost mc = maintCostDS.new MaintCost(e[0], e[1], e[2], Double.parseDouble(e[3]), e[4]);
					data.add(mc);
				}
			}
			maintCostDS.setData(data);
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("reportDesc", "This is a summary report of vehicle brands maintenance costing.");
			parameters.put("keyHeader", "Vehicle Brand");
			
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(maintCostDS.getCollectionList());
			
			downloadJasperPDF(parameters, "brands_maintenance_costs.pdf", "/resources/jasper/MaintCost.jasper", ds);
		} catch(Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchMaintCostsByParts() {
		resetReportInfo();
		setMaintCostByPartsReport(null);
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null) {
			if(getFleet_id() != null && getFleet_id() > 0) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Vector<String[]> vlist = new Vector<String[]>();
			if(getVehicle_id() != null && getVehicle_id() > 0) {
				Object obj = gDAO.find(Vehicle.class, getVehicle_id());
				if(obj != null) {
					Vehicle v = (Vehicle)obj;
					String[] e = new String[2];
					e[0] = ""+v.getId();
					e[1] = v.getRegistrationNo();
					vlist.add(e);
				}
			} else {
				Query q = gDAO.createQuery("Select e from Vehicle e where e.fleet.id=:fleet_id");
				q.setParameter("fleet_id", getFleet_id());
				Object obj = gDAO.search(q, 0);
				if(obj != null) {
					Vector<Vehicle> list = (Vector<Vehicle>)obj;
					for(Vehicle v : list) {
						String[] e = new String[2];
						e[0] = ""+v.getId();
						e[1] = v.getRegistrationNo();
						vlist.add(e);
					}
				}
			}
			
			Calendar start_can = Calendar.getInstance(), end_can = Calendar.getInstance();
			start_can.setTime(getStart_dt());
			start_can.set(Calendar.HOUR_OF_DAY, 0);
			start_can.set(Calendar.MINUTE, 0);
			start_can.set(Calendar.SECOND, 0);
			start_can.set(Calendar.MILLISECOND, 0);
			end_can.setTime(getEnd_dt());
			end_can.set(Calendar.HOUR_OF_DAY, end_can.getMaximum(Calendar.HOUR_OF_DAY));
			end_can.set(Calendar.MINUTE, end_can.getMaximum(Calendar.MINUTE));
			end_can.set(Calendar.SECOND, end_can.getMaximum(Calendar.SECOND));
			end_can.set(Calendar.MILLISECOND, end_can.getMaximum(Calendar.MILLISECOND));
			if(getRgroup() == 1) { //daily
				
			} else if(getRgroup() == 2) { //weekly
				start_can.set(Calendar.DAY_OF_WEEK, start_can.getFirstDayOfWeek());
				end_can.set(Calendar.DAY_OF_WEEK, end_can.getMaximum(Calendar.DAY_OF_WEEK));
			} else if(getRgroup() == 3) { //monthly
				start_can.set(Calendar.DAY_OF_MONTH, 1);
				end_can.set(Calendar.DAY_OF_MONTH, end_can.getMaximum(Calendar.DAY_OF_MONTH));
			}
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
				setPeriod(sdf.format(getStart_dt()) + " - " + sdf.format(getEnd_dt()));
			} catch(Exception ex){ ex.printStackTrace(); }
			
			Vector<Item> items = new Vector<Item>();
			Object itemsObj = gDAO.findAll("Item");
			if(itemsObj != null) 
				items = (Vector<Item>)itemsObj;
			
			Vector<String[]> list = new Vector<String[]>(); // Vehicle Parts | Start Date | End Date | Cost | Type(Routine and AdHoc, Routine, AdHoc)
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
			while(start_can.before(end_can)) {
				Calendar search_end_can = Calendar.getInstance();
				search_end_can.setTime(start_can.getTime());
				if(getRgroup() == 1) { //daily
					
				} else if(getRgroup() == 2) { //weekly
					search_end_can.set(Calendar.DAY_OF_WEEK, search_end_can.getMaximum(Calendar.DAY_OF_WEEK));
				} else if(getRgroup() == 3) { //monthly
					search_end_can.set(Calendar.DAY_OF_MONTH, search_end_can.getMaximum(Calendar.DAY_OF_MONTH));
				}
				search_end_can.set(Calendar.HOUR_OF_DAY, search_end_can.getMaximum(Calendar.HOUR_OF_DAY));
				search_end_can.set(Calendar.MINUTE, search_end_can.getMaximum(Calendar.MINUTE));
				search_end_can.set(Calendar.SECOND, search_end_can.getMaximum(Calendar.SECOND));
				search_end_can.set(Calendar.MILLISECOND, search_end_can.getMaximum(Calendar.MILLISECOND));
				BigDecimal total_cost = new BigDecimal(0);
				String[] date_tow = new String[]{"Total", "", "", "", getMainttype()};
				for(Item itm : items) {
					String[] r = new String[5];
					r[0] = itm.getName();
					r[1] = sdf.format(start_can.getTime());
					r[2] = sdf.format(search_end_can.getTime());
					BigDecimal cost = new BigDecimal(0);
					for(String[] e : vlist) {
						if(getMainttype().equalsIgnoreCase("Routine")) {
							Query q = gDAO.createQuery("Select e from WorkOrderItem e where (e.workOrderVehicle.workOrder.approve_dt between :start_dt and :end_dt) and e.item.id=:itm_id and e.workOrderVehicle.vehicle.id=:v_id and e.workOrderVehicle.workOrder.workOrderType=:wrk_type");
							q.setParameter("itm_id", itm.getId());
							q.setParameter("wrk_type", "Routine");
							q.setParameter("v_id", Long.parseLong(e[0]));
							q.setParameter("start_dt", start_can.getTime());
							q.setParameter("end_dt", search_end_can.getTime());
							Object obj = gDAO.search(q, 0);
							if(obj != null) {
								Vector<WorkOrderItem> objList = (Vector<WorkOrderItem>)obj;
								for(WorkOrderItem woi : objList) {
									if(woi.getInitEstAmount() > 0) {
										cost = cost.add(new BigDecimal(woi.getInitEstAmount()));
										cost = cost.setScale(2);
									}
								}
							}
							r[4] = "Routine";
						} else if(getMainttype().equalsIgnoreCase("AdHoc")) {
							Query q = gDAO.createQuery("Select e from WorkOrderItem e where (e.workOrderVehicle.workOrder.approve_dt between :start_dt and :end_dt) and e.item.id=:itm_id and e.workOrderVehicle.vehicle.id=:v_id and e.workOrderVehicle.workOrder.workOrderType=:wrk_type");
							q.setParameter("itm_id", itm.getId());
							q.setParameter("wrk_type", "Adhoc");
							q.setParameter("v_id", Long.parseLong(e[0]));
							q.setParameter("start_dt", start_can.getTime());
							q.setParameter("end_dt", search_end_can.getTime());
							Object obj = gDAO.search(q, 0);
							if(obj != null) {
								Vector<WorkOrderItem> objList = (Vector<WorkOrderItem>)obj;
								for(WorkOrderItem woi : objList) {
									if(woi.getInitEstAmount() > 0) {
										cost = cost.add(new BigDecimal(woi.getInitEstAmount()));
										cost = cost.setScale(2);
									}
								}
							}
							r[4] = "AdHoc";
						} else if(getMainttype().equalsIgnoreCase("Both")) {
							Query q = gDAO.createQuery("Select e from WorkOrderItem e where (e.workOrderVehicle.workOrder.approve_dt between :start_dt and :end_dt) and e.item.id=:itm_id and e.workOrderVehicle.vehicle.id=:v_id and e.workOrderVehicle.workOrder.workOrderType=:wrk_type");
							q.setParameter("itm_id", itm.getId());
							q.setParameter("wrk_type", "Routine");
							q.setParameter("v_id", Long.parseLong(e[0]));
							q.setParameter("start_dt", start_can.getTime());
							q.setParameter("end_dt", search_end_can.getTime());
							Object obj = gDAO.search(q, 0);
							if(obj != null) {
								Vector<WorkOrderItem> objList = (Vector<WorkOrderItem>)obj;
								for(WorkOrderItem woi : objList) {
									if(woi.getInitEstAmount() > 0) {
										cost = cost.add(new BigDecimal(woi.getInitEstAmount()));
										cost = cost.setScale(2);
									}
								}
							}
							q = gDAO.createQuery("Select e from WorkOrderItem e where (e.workOrderVehicle.workOrder.approve_dt between :start_dt and :end_dt) and e.item.id=:itm_id and e.workOrderVehicle.vehicle.id=:v_id and e.workOrderVehicle.workOrder.workOrderType=:wrk_type");
							q.setParameter("itm_id", itm.getId());
							q.setParameter("wrk_type", "Adhoc");
							q.setParameter("v_id", Long.parseLong(e[0]));
							q.setParameter("start_dt", start_can.getTime());
							q.setParameter("end_dt", search_end_can.getTime());
							obj = gDAO.search(q, 0);
							if(obj != null) {
								Vector<WorkOrderItem> objList = (Vector<WorkOrderItem>)obj;
								for(WorkOrderItem woi : objList) {
									if(woi.getInitEstAmount() > 0) {
										cost = cost.add(new BigDecimal(woi.getInitEstAmount()));
										cost = cost.setScale(2);
									}
								}
							}
							r[4] = "Routine and AdHoc";
						}
					}
					if(cost.doubleValue() > 0) {
						total_cost = total_cost.add(cost);
						total_cost = total_cost.setScale(2);
						r[3] = cost.toPlainString();
						list.add(r);
					}
				}
				if(total_cost.doubleValue() > 0) {
					date_tow[3] = total_cost.toPlainString();
					list.add(date_tow);
				}
				if(getRgroup() == 1) { //daily
					start_can.add(Calendar.DATE, 1);
				} else if(getRgroup() == 2) { //weekly
					start_can.add(Calendar.WEEK_OF_YEAR, 1);
				} else if(getRgroup() == 3) { //monthly
					start_can.add(Calendar.MONTH, 1);
				}
			}
			
			if(list.size() > 0) {
				setMaintCostByPartsReport(list);
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getMaintCostByPartsReport().size() + " record(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Maintenance Cost by Parts Report");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No record found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			
			gDAO.destroy();
			}
		}
	}
	
	public void downloadMaintCostsByPartsPDF() {
		try {
			MaintCostDataSource maintCostDS = MaintCostDataSource.getInstance();
			Vector<MaintCost> data = new Vector<MaintCost>();
			for(String[] e : getMaintCostByPartsReport()) {
				if(e[0] != null && !e[0].equalsIgnoreCase("Total")) {
					MaintCostDataSource.MaintCost mc = maintCostDS.new MaintCost(e[0], e[1], e[2], Double.parseDouble(e[3]), e[4]);
					data.add(mc);
				}
			}
			maintCostDS.setData(data);
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("reportDesc", "This is a summary report of vehicle parts maintenance costing.");
			parameters.put("keyHeader", "Vehicle Parts");
			
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(maintCostDS.getCollectionList());
			
			downloadJasperPDF(parameters, "parts_maintenance_costs.pdf", "/resources/jasper/MaintCost.jasper", ds);
		} catch(Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchMaintCosts() {
		resetReportInfo();
		setMaintCostReport(null);
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null) {
			if(getFleet_id() != null && getFleet_id() > 0) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Vector<String[]> vlist = new Vector<String[]>();
			if(getVehicle_id() != null && getVehicle_id() > 0) {
				Object obj = gDAO.find(Vehicle.class, getVehicle_id());
				if(obj != null) {
					Vehicle v = (Vehicle)obj;
					String[] e = new String[2];
					e[0] = ""+v.getId();
					e[1] = v.getRegistrationNo();
					vlist.add(e);
				}
			} else {
				Query q = gDAO.createQuery("Select e from Vehicle e where e.fleet.id=:fleet_id");
				q.setParameter("fleet_id", getFleet_id());
				Object obj = gDAO.search(q, 0);
				if(obj != null) {
					Vector<Vehicle> list = (Vector<Vehicle>)obj;
					for(Vehicle v : list) {
						String[] e = new String[2];
						e[0] = ""+v.getId();
						e[1] = v.getRegistrationNo();
						vlist.add(e);
					}
				}
			}
			
			Calendar start_can = Calendar.getInstance(), end_can = Calendar.getInstance();
			start_can.setTime(getStart_dt());
			start_can.set(Calendar.HOUR_OF_DAY, 0);
			start_can.set(Calendar.MINUTE, 0);
			start_can.set(Calendar.SECOND, 0);
			start_can.set(Calendar.MILLISECOND, 0);
			end_can.setTime(getEnd_dt());
			end_can.set(Calendar.HOUR_OF_DAY, end_can.getMaximum(Calendar.HOUR_OF_DAY));
			end_can.set(Calendar.MINUTE, end_can.getMaximum(Calendar.MINUTE));
			end_can.set(Calendar.SECOND, end_can.getMaximum(Calendar.SECOND));
			end_can.set(Calendar.MILLISECOND, end_can.getMaximum(Calendar.MILLISECOND));
			if(getRgroup() == 1) { //daily
				
			} else if(getRgroup() == 2) { //weekly
				start_can.set(Calendar.DAY_OF_WEEK, start_can.getFirstDayOfWeek());
				end_can.set(Calendar.DAY_OF_WEEK, end_can.getMaximum(Calendar.DAY_OF_WEEK));
			} else if(getRgroup() == 3) { //monthly
				start_can.set(Calendar.DAY_OF_MONTH, 1);
				end_can.set(Calendar.DAY_OF_MONTH, end_can.getMaximum(Calendar.DAY_OF_MONTH));
			}
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
				setPeriod(sdf.format(getStart_dt()) + " - " + sdf.format(getEnd_dt()));
			} catch(Exception ex){ ex.printStackTrace(); }
			
			Vector<String[]> list = new Vector<String[]>(); // Reg Number | Start Date | End Date | Cost | Type(Routine and AdHoc, Routine, AdHoc)
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
			while(start_can.before(end_can)) {
				Calendar search_end_can = Calendar.getInstance();
				search_end_can.setTime(start_can.getTime());
				if(getRgroup() == 1) { //daily
					
				} else if(getRgroup() == 2) { //weekly
					search_end_can.set(Calendar.DAY_OF_WEEK, search_end_can.getMaximum(Calendar.DAY_OF_WEEK));
				} else if(getRgroup() == 3) { //monthly
					search_end_can.set(Calendar.DAY_OF_MONTH, search_end_can.getMaximum(Calendar.DAY_OF_MONTH));
				}
				search_end_can.set(Calendar.HOUR_OF_DAY, search_end_can.getMaximum(Calendar.HOUR_OF_DAY));
				search_end_can.set(Calendar.MINUTE, search_end_can.getMaximum(Calendar.MINUTE));
				search_end_can.set(Calendar.SECOND, search_end_can.getMaximum(Calendar.SECOND));
				search_end_can.set(Calendar.MILLISECOND, search_end_can.getMaximum(Calendar.MILLISECOND));
				
				BigDecimal total_cost = new BigDecimal(0);
				String[] date_tow = new String[]{"Total", "", "", "", getMainttype()};
				
				for(String[] e : vlist) {
					String[] r = new String[5];
					r[0] = e[1];
					r[1] = sdf.format(start_can.getTime());
					r[2] = sdf.format(search_end_can.getTime());
					if(getMainttype().equalsIgnoreCase("Routine")) {
						Query q = gDAO.createQuery("Select e from VehicleRoutineMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.id=:v_id");
						q.setParameter("v_id", Long.parseLong(e[0]));
						q.setParameter("start_dt", start_can.getTime());
						q.setParameter("end_dt", search_end_can.getTime());
						Object obj = gDAO.search(q, 0);
						if(obj != null) {
							Vector<VehicleRoutineMaintenance> objList = (Vector<VehicleRoutineMaintenance>)obj;
							BigDecimal cost = new BigDecimal(0);
							for(VehicleRoutineMaintenance vrm : objList) {
								if(vrm.getClosed_amount() != null) {
									cost = cost.add(vrm.getClosed_amount());
									cost.setScale(2);
								}
							}
							if(cost.doubleValue() > 0) {
								total_cost = total_cost.add(cost);
								total_cost = total_cost.setScale(2);
								r[3] = cost.toPlainString();
								r[4] = "Routine";
								list.add(r);
							}
						}
					} else if(getMainttype().equalsIgnoreCase("AdHoc")) {
						Query q = gDAO.createQuery("Select e from VehicleAdHocMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.id=:v_id");
						q.setParameter("v_id", Long.parseLong(e[0]));
						q.setParameter("start_dt", start_can.getTime());
						q.setParameter("end_dt", search_end_can.getTime());
						Object obj = gDAO.search(q, 0);
						if(obj != null) {
							Vector<VehicleAdHocMaintenance> objList = (Vector<VehicleAdHocMaintenance>)obj;
							BigDecimal cost = new BigDecimal(0);
							for(VehicleAdHocMaintenance vam : objList) {
								if(vam.getClosed_cost() != null) {
									cost = cost.add(vam.getClosed_cost());
									cost.setScale(2);
								}
							}
							if(cost.doubleValue() > 0) {
								total_cost = total_cost.add(cost);
								total_cost = total_cost.setScale(2);
								r[3] = cost.toPlainString();
								r[4] = "AdHoc";
								list.add(r);
							}
						}
					} else if(getMainttype().equalsIgnoreCase("Both")) {
						BigDecimal cost = new BigDecimal(0);
						Query q = gDAO.createQuery("Select e from VehicleRoutineMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.id=:v_id");
						q.setParameter("v_id", Long.parseLong(e[0]));
						q.setParameter("start_dt", start_can.getTime());
						q.setParameter("end_dt", search_end_can.getTime());
						Object obj = gDAO.search(q, 0);
						if(obj != null) {
							Vector<VehicleRoutineMaintenance> objList = (Vector<VehicleRoutineMaintenance>)obj;
							for(VehicleRoutineMaintenance vrm : objList) {
								if(vrm.getClosed_amount() != null) {
									cost = cost.add(vrm.getClosed_amount());
									cost.setScale(2);
								}
							}
						}
						q = gDAO.createQuery("Select e from VehicleAdHocMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.id=:v_id");
						q.setParameter("v_id", Long.parseLong(e[0]));
						q.setParameter("start_dt", start_can.getTime());
						q.setParameter("end_dt", search_end_can.getTime());
						obj = gDAO.search(q, 0);
						if(obj != null) {
							Vector<VehicleAdHocMaintenance> objList = (Vector<VehicleAdHocMaintenance>)obj;
							for(VehicleAdHocMaintenance vam : objList) {
								if(vam.getClosed_cost() != null) {
									cost = cost.add(vam.getClosed_cost());
									cost.setScale(2);
								}
							}
						}
						if(cost.doubleValue() > 0) {
							total_cost = total_cost.add(cost);
							total_cost = total_cost.setScale(2);
							r[3] = cost.toPlainString();
							r[4] = "Routine and AdHoc";
							list.add(r);
						}
					}
				}
				if(total_cost.doubleValue() > 0) {
					date_tow[3] = total_cost.toPlainString();
					list.add(date_tow);
				}
				
				if(getRgroup() == 1) { //daily
					start_can.add(Calendar.DATE, 1);
				} else if(getRgroup() == 2) { //weekly
					start_can.add(Calendar.WEEK_OF_YEAR, 1);
				} else if(getRgroup() == 3) { //monthly
					start_can.add(Calendar.MONTH, 1);
				}
			}
			
			if(list.size() > 0) {
				setMaintCostReport(list);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getMaintCostReport().size() + " record(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Maintenance Cost Report");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No record found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			
			gDAO.destroy();
			}
		}
	}
	
	public void downloadMaintCostsPDF() {
		try {
			MaintCostDataSource maintCostDS = MaintCostDataSource.getInstance();
			Vector<MaintCost> data = new Vector<MaintCost>();
			for(String[] e : getMaintCostReport()) {
				if(e[0] != null && !e[0].equalsIgnoreCase("Total")) {
					MaintCostDataSource.MaintCost mc = maintCostDS.new MaintCost(e[0], e[1], e[2], Double.parseDouble(e[3]), e[4]);
					data.add(mc);
				}
			}
			maintCostDS.setData(data);
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("reportDesc", "This is a summary report of vehicle maintenance costing.");
			parameters.put("keyHeader", "Vehicle");
			
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(maintCostDS.getCollectionList());
			
			downloadJasperPDF(parameters, "maintenance_costs.pdf", "/resources/jasper/MaintCost.jasper", ds);
		} catch(Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void downloadJasperPDF(Map<String, Object> parameters, String filename, String jasper_filepath, 
			JRBeanCollectionDataSource ds) {
		FacesContext context = FacesContext.getCurrentInstance();
		
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm:ss");
		parameters.put("period", getPeriod());
		parameters.put("groupBy", getGroupBy());
		parameters.put("preparedBy", dashBean.getUser().getPersonel().getFirstname() + " " + dashBean.getUser().getPersonel().getLastname());
		parameters.put("partnerName", getPartner().getName());
		
		PartnerSetting setting = null;
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("partner", getPartner());
		GeneralDAO gDAO = new GeneralDAO();
		Object pSettingsObj = gDAO.search("PartnerSetting", params);
		if(pSettingsObj != null)
		{
			Vector<PartnerSetting> pSettingsList = (Vector<PartnerSetting>)pSettingsObj;
			for(PartnerSetting e : pSettingsList)
			{
				setting = e;
			}
		}
		gDAO.destroy();
		
		if(setting != null) {
			// /fms/imageservlet/1010453-partner--1167796541
			String logoURL = "http://sattrakservices.com/fms/imageservlet/" + setting.getId().longValue() + "-partner-123";
			// #{request.contextPath}/imageservlet/#{partnerBean.setting.id}-partner-#{appBean.randomNumber}
			System.out.println("logoURL: " + logoURL);
			parameters.put("partnerLogoURL", logoURL);
		} else {
			// put a default logo here
		}
		
		try {
			parameters.put("preparedOn", sdf.format(new Date()));
		} catch(Exception ex){}
		
		try {
			JasperPrint jp = JasperFillManager.fillReport(((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getRealPath(jasper_filepath), parameters, ds);
			if(jp != null) {
				byte[] pdf = JasperExportManager.exportReportToPdf(jp);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        baos.write(pdf);
				writeFileToResponse(context.getExternalContext(), baos, filename, "application/pdf");
				context.responseComplete();
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void searchRMaints()
	{
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null)
		{
			setRmaints(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from VehicleRoutineMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.partner=:partner";
			
			Fleet f = null;
			if(getFleet_id() != null && getFleet_id() > 0)
			{
				try
				{
					f = (Fleet)gDAO.find(Fleet.class, getFleet_id());
				}
				catch(Exception ex){}
			}
			if(getFleet_id() != null && getFleet_id() > 0)
				str += " and e.vehicle.fleet = :fleet";
			
			Vehicle v = null;
			if(getVehicle_id() != null && getVehicle_id() > 0)
			{
				try
				{
					v = (Vehicle)gDAO.find(Vehicle.class, getVehicle_id());
				}
				catch(Exception ex){}
			}
			if(getVehicle_id() != null && getVehicle_id() > 0)
				str += " and e.vehicle = :vehicle";
			
			Query q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			if(getFleet_id() != null && getFleet_id() > 0)
				q.setParameter("fleet", f);
			if(getVehicle_id() != null && getVehicle_id() > 0)
				q.setParameter("vehicle", v);
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setRmaints((Vector<VehicleRoutineMaintenance>)drvs);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getRmaints().size() + " routine maintenance(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Routine Maintenance Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No routine maintenance found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void searchAdHocMaints()
	{
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null)
		{
			setAdhocmaints(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from VehicleAdHocMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.partner=:partner";
			
			Fleet f = null;
			if(getFleet_id() != null && getFleet_id() > 0)
			{
				try
				{
					f = (Fleet)gDAO.find(Fleet.class, getFleet_id());
				}
				catch(Exception ex){}
			}
			if(getFleet_id() != null && getFleet_id() > 0)
				str += " and e.vehicle.fleet = :fleet";
			
			Vehicle v = null;
			if(getVehicle_id() != null && getVehicle_id() > 0)
			{
				try
				{
					v = (Vehicle)gDAO.find(Vehicle.class, getVehicle_id());
				}
				catch(Exception ex){}
			}
			if(getVehicle_id() != null && getVehicle_id() > 0)
				str += " and e.vehicle = :vehicle";
			
			Query q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			if(getFleet_id() != null && getFleet_id() > 0)
				q.setParameter("fleet", f);
			if(getVehicle_id() != null && getVehicle_id() > 0)
				q.setParameter("vehicle", v);
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setAdhocmaints((Vector<VehicleAdHocMaintenance>)drvs);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getAdhocmaints().size() + " ad-hoc maintenance(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Adhoc Maintenance Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No ad-hoc maintenance found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchAcciVehicles()
	{
		resetReportInfo();
		
		if(getPartner() != null)
		{
			setActiveAccidents(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("vehicle.partner", getPartner());
			params.put("active", true);
			if(getFleet_id() != null)
			{
				Object fleetObj = gDAO.find(Fleet.class, getFleet_id());
				if(fleetObj != null)
				{
					params.put("vehicle.fleet", (Fleet)fleetObj);
				}
			}
			
			Object vehiclesAccidents = gDAO.search("VehicleAccident", params);
			if(vehiclesAccidents != null)
			{
				setActiveAccidents((Vector<VehicleAccident>)vehiclesAccidents);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getActiveAccidents().size() + " vehicle accident(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Current Active Vehicle Accidents Report");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No vehicle accident found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchVehicleAges()
	{
		resetReportInfo();
		
		if(getPartner() != null)
		{
			setActiveAccidents(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getPartner());
			params.put("active", true);
			if(getFleet_id() != null)
			{
				Object fleetObj = gDAO.find(Fleet.class, getFleet_id());
				if(fleetObj != null)
				{
					params.put("fleet", (Fleet)fleetObj);
				}
			}
			
			Object vehicles = gDAO.search("Vehicle", params);
			if(vehicles != null)
			{
				setVehiclesAges((Vector<Vehicle>)vehicles);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getVehiclesAges().size() + " vehicle(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Ages of Vehicles Report");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No vehicle found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchDriverQueries() {
		resetReportInfo();
		if(getPartner() != null && getStart_dt() != null && getEnd_dt() != null) {
			setDriverQueries(new Vector<PartnerDriverQuery>());
			GeneralDAO gDAO = new GeneralDAO();
			String qry = "Select e from PartnerDriverQuery e where (e.tranDate between :stdt and :eddt) and e.driver.partner.id=:partner_id";
			if(getDriver_id() != null && getDriver_id() > 0)
				qry += " and e.driver.id = :driver_id";
			
			Query q = gDAO.createQuery(qry);
			q.setParameter("stdt", getStart_dt());
			q.setParameter("eddt", getEnd_dt());
			q.setParameter("partner_id", getPartner().getId());
			if(getDriver_id() != null && getDriver_id() > 0)
				q.setParameter("driver_id", getDriver_id());
			
			Object listObj = gDAO.search(q, 0);
			if(listObj != null)
				setDriverQueries((Vector<PartnerDriverQuery>)listObj);
			
			gDAO.destroy();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchLicDue()
	{
		resetReportInfo();
		
		if(getPartner() != null)
		{
			setDueVehicleLicenses(new Vector<VehicleLicense>());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Calendar c = Calendar.getInstance();
			Calendar c2 = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, -30);
			
			Query q = gDAO.createQuery("Select e from VehicleLicense e where e.vehicle.partner=:partner and e.expired=:expired and e.active=:active and (e.lic_end_dt > :start_dt and e.lic_end_dt < :end_date)");
			q.setParameter("partner", getPartner());
			q.setParameter("expired", true);
			q.setParameter("active", true);
			q.setParameter("start_dt", c.getTime());
			q.setParameter("end_date", c2.getTime());
			
			Object licsObj = gDAO.search(q, 0);
			if(licsObj != null)
			{
				Vector<VehicleLicense> lics = (Vector<VehicleLicense>)licsObj;
				getDueVehicleLicenses().addAll(lics);
			}
			
			c = Calendar.getInstance();
			c2 = Calendar.getInstance();
			c2.add(Calendar.DAY_OF_MONTH, 30);
			
			q = gDAO.createQuery("Select e from VehicleLicense e where e.vehicle.partner=:partner and e.expired=:expired and e.active=:active and (e.lic_end_dt > :start_dt and e.lic_end_dt < :end_date)");
			q.setParameter("partner", getPartner());
			q.setParameter("expired", false);
			q.setParameter("active", true);
			q.setParameter("start_dt", c.getTime());
			q.setParameter("end_date", c2.getTime());
			
			licsObj = gDAO.search(q, 0);
			if(licsObj != null)
			{
				Vector<VehicleLicense> lics = (Vector<VehicleLicense>)licsObj;
				getDueVehicleLicenses().addAll(lics);
			}
			
			if(getDueVehicleLicenses().size() > 0)
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getDueVehicleLicenses().size() + " recent/up-coming expired vehicle license(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Vehicle License Due");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No recent/up-coming expired vehicle license found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void searchCorTrips()
	{
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null)
		{
			setCorTrips(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from CorporateTrip e where (e.departureDateTime between :start_dt and :end_dt) and e.partner = :partner";
			
			Query q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setCorTrips((Vector<CorporateTrip>)drvs);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getCorTrips().size() + " trip(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Corporate Trip Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No trip found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void searchExpenses()
	{
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null)
		{
			setExpenses(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from Expense e where (e.expense_dt between :start_dt and :end_dt) and e.partner = :partner";
			
			Query q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setExpenses((Vector<Expense>)drvs);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getExpenses().size() + " expense(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Expenses Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No expense found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	public void downloadExpensesPDF() {
		MaintCostDataSource maintCostDS = MaintCostDataSource.getInstance();
		Vector<MaintCost> data = new Vector<MaintCost>();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
		for(Expense e : getExpenses()) {
			MaintCostDataSource.MaintCost mc = maintCostDS.new MaintCost(e.getType().getName(), 
					sdf.format(e.getExpense_dt()), sdf.format(e.getExpense_dt()), e.getAmount(), 
					(e.getVehicle() != null) ? e.getVehicle().getRegistrationNo() : (e.getPersonel() != null ? e.getPersonel().getFirstname() + " " + e.getPersonel().getLastname() : "N/A"));
			data.add(mc);
		}
		maintCostDS.setData(data);
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("mainHeader", "FMS - Expense Report");
		parameters.put("subHeader", "Expense Detail Report");
		parameters.put("reportDesc", "This is a detail report of expenses.");
		parameters.put("keyHeader", "Expense Type");
		parameters.put("col2Header", "Start Date");
		parameters.put("col3Header", "End Date");
		parameters.put("col4Header", "Cost");
		parameters.put("col5Header", "Beneficiary");
		parameters.put("totalLabel", "Total Cost: ");
		parameters.put("pieChartDesc", "Pie Chart showing the various distribution of expense cost per expense type for the selected period.");
		
		JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(maintCostDS.getCollectionList());
		
		downloadJasperPDF(parameters, "expense_detail_report.pdf", "/resources/jasper/generic5Col.jasper", ds);
	}
	
	@SuppressWarnings("unchecked")
	public void searchFuelings()
	{
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null)
		{
			setFuelings(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from VehicleFueling e where (e.captured_dt between :start_dt and :end_dt) and e.vehicle.partner = :partner";
			
			boolean fleet = false, veh = false;
			if(getFleet_id() != null)
			{
				str += " and e.vehicle.fleet.id = :fleet_id";
				fleet = true;
			}
			
			if(getVehicle_id() != null)
			{
				str += " and e.vehicle.id = :vehicle_id";
				veh = true;
			}
			
			Query q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			if(fleet)
				q.setParameter("fleet_id", getFleet_id());
			if(veh)
				q.setParameter("vehicle_id", getVehicle_id());
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null)
			{
				setFuelings((Vector<VehicleFueling>)drvs);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getFuelings().size() + " fueling(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Fueling Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No fueling found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			
			gDAO.destroy();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchFuelConsumptions() {
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null && getStype() != null && getVehicle_id() != null)
		{
			Calendar start_can = Calendar.getInstance(), end_can = Calendar.getInstance();
			start_can.setTime(getStart_dt());
			end_can.setTime(getEnd_dt());
			int style = Calendar.LONG;
			Locale us = Locale.US;
			setFuelConsumptions(null);
			GeneralDAO gDAO = new GeneralDAO();
			if(getStype().equalsIgnoreCase("Month")) {
				start_can.set(Calendar.HOUR_OF_DAY, start_can.getMinimum(Calendar.HOUR_OF_DAY));
				start_can.set(Calendar.MINUTE, start_can.getMinimum(Calendar.MINUTE));
				start_can.set(Calendar.SECOND, start_can.getMinimum(Calendar.SECOND));
				start_can.set(Calendar.MILLISECOND, start_can.getMinimum(Calendar.MILLISECOND));
				while(end_can.after(start_can)) {
					String date = start_can.get(Calendar.YEAR) + "-" + start_can.getDisplayName(Calendar.MONTH, style, us);
					Date st_dt = start_can.getTime();
					start_can.add(Calendar.MONTH, 1);
					Date ed_dt = start_can.getTime();
					
					Query q = gDAO.createQuery("Select e from VehicleFueling e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
					q.setParameter("v_id", getVehicle_id());
					q.setParameter("st_dt", st_dt);
					q.setParameter("ed_dt", ed_dt);
					Object listObj = gDAO.search(q, 0);
					if(listObj != null) {
						String regNo = "N/A";
						double last_fuel_level = 0, total_fuel_consumption = 0;
						boolean start = true;
						Vector<VehicleFueling> list = (Vector<VehicleFueling>)listObj;
						for(VehicleFueling e : list) {
							double this_fuel_level_b4_purchase = e.getFuelLevel()-e.getLitres();
							if(start) {
								regNo = e.getVehicle().getRegistrationNo();
								start = false;
								last_fuel_level = e.getFuelLevel();
							} else {
								total_fuel_consumption += (last_fuel_level - this_fuel_level_b4_purchase);
								last_fuel_level = e.getFuelLevel();
							}
						}
						FuelConsumption fc = new FuelConsumption();
						fc.setDate(date);
						fc.setRegNo(regNo);
						fc.setLevel(total_fuel_consumption);
						getFuelConsumptions().add(fc);
					}
				}
			} else if(getStype().equalsIgnoreCase("Week")) {
				start_can.set(Calendar.HOUR_OF_DAY, start_can.getMinimum(Calendar.HOUR_OF_DAY));
				start_can.set(Calendar.MINUTE, start_can.getMinimum(Calendar.MINUTE));
				start_can.set(Calendar.SECOND, start_can.getMinimum(Calendar.SECOND));
				start_can.set(Calendar.MILLISECOND, start_can.getMinimum(Calendar.MILLISECOND));
				while(end_can.after(start_can)) {
					String date = start_can.get(Calendar.YEAR) + "-" + start_can.getDisplayName(Calendar.MONTH, style, us) + " WK:" + start_can.get(Calendar.WEEK_OF_MONTH);
					Date st_dt = start_can.getTime();
					start_can.add(Calendar.WEEK_OF_YEAR, 1);
					Date ed_dt = start_can.getTime();
					
					Query q = gDAO.createQuery("Select e from VehicleFueling e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
					q.setParameter("v_id", getVehicle_id());
					q.setParameter("st_dt", st_dt);
					q.setParameter("ed_dt", ed_dt);
					Object listObj = gDAO.search(q, 0);
					if(listObj != null) {
						String regNo = "N/A";
						double last_fuel_level = 0, total_fuel_consumption = 0;
						boolean start = true;
						Vector<VehicleFueling> list = (Vector<VehicleFueling>)listObj;
						for(VehicleFueling e : list) {
							double this_fuel_level_b4_purchase = e.getFuelLevel()-e.getLitres();
							if(start) {
								regNo = e.getVehicle().getRegistrationNo();
								start = false;
								last_fuel_level = e.getFuelLevel();
							} else {
								total_fuel_consumption += (last_fuel_level - this_fuel_level_b4_purchase);
								last_fuel_level = e.getFuelLevel();
							}
						}
						FuelConsumption fc = new FuelConsumption();
						fc.setDate(date);
						fc.setRegNo(regNo);
						fc.setLevel(total_fuel_consumption);
						getFuelConsumptions().add(fc);
					}
				}
			} else if(getStype().equalsIgnoreCase("Day")) {
				start_can.set(Calendar.HOUR_OF_DAY, start_can.getMinimum(Calendar.HOUR_OF_DAY));
				start_can.set(Calendar.MINUTE, start_can.getMinimum(Calendar.MINUTE));
				start_can.set(Calendar.SECOND, start_can.getMinimum(Calendar.SECOND));
				start_can.set(Calendar.MILLISECOND, start_can.getMinimum(Calendar.MILLISECOND));
				while(end_can.after(start_can)) {
					String date = start_can.get(Calendar.YEAR) + "-" + start_can.getDisplayName(Calendar.MONTH, style, us) + "-" + start_can.get(Calendar.DATE);
					Date st_dt = start_can.getTime();
					start_can.add(Calendar.DATE, 1);
					Date ed_dt = start_can.getTime();
					
					Query q = gDAO.createQuery("Select e from VehicleFueling e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
					q.setParameter("v_id", getVehicle_id());
					q.setParameter("st_dt", st_dt);
					q.setParameter("ed_dt", ed_dt);
					Object listObj = gDAO.search(q, 0);
					if(listObj != null) {
						String regNo = "N/A";
						double last_fuel_level = 0, total_fuel_consumption = 0;
						boolean start = true;
						Vector<VehicleFueling> list = (Vector<VehicleFueling>)listObj;
						for(VehicleFueling e : list) {
							double this_fuel_level_b4_purchase = e.getFuelLevel()-e.getLitres();
							if(start) {
								regNo = e.getVehicle().getRegistrationNo();
								start = false;
								last_fuel_level = e.getFuelLevel();
							} else {
								total_fuel_consumption += (last_fuel_level - this_fuel_level_b4_purchase);
								last_fuel_level = e.getFuelLevel();
							}
						}
						FuelConsumption fc = new FuelConsumption();
						fc.setDate(date);
						fc.setRegNo(regNo);
						fc.setLevel(total_fuel_consumption);
						getFuelConsumptions().add(fc);
					}
				}
			} else if(getStype().equalsIgnoreCase("Hour")) {
				start_can.set(Calendar.HOUR_OF_DAY, start_can.getMinimum(Calendar.HOUR_OF_DAY));
				start_can.set(Calendar.MINUTE, start_can.getMinimum(Calendar.MINUTE));
				start_can.set(Calendar.SECOND, start_can.getMinimum(Calendar.SECOND));
				start_can.set(Calendar.MILLISECOND, start_can.getMinimum(Calendar.MILLISECOND));
				while(end_can.after(start_can)) {
					String date = start_can.get(Calendar.YEAR) + "-" + start_can.getDisplayName(Calendar.MONTH, style, us) + "-" + start_can.get(Calendar.DATE) + " " + start_can.get(Calendar.HOUR_OF_DAY);
					Date st_dt = start_can.getTime();
					start_can.add(Calendar.HOUR_OF_DAY, 1);
					Date ed_dt = start_can.getTime();
					
					Query q = gDAO.createQuery("Select e from VehicleFueling e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
					q.setParameter("v_id", getVehicle_id());
					q.setParameter("st_dt", st_dt);
					q.setParameter("ed_dt", ed_dt);
					Object listObj = gDAO.search(q, 0);
					if(listObj != null) {
						String regNo = "N/A";
						double last_fuel_level = 0, total_fuel_consumption = 0;
						boolean start = true;
						Vector<VehicleFueling> list = (Vector<VehicleFueling>)listObj;
						for(VehicleFueling e : list) {
							double this_fuel_level_b4_purchase = e.getFuelLevel()-e.getLitres();
							if(start) {
								regNo = e.getVehicle().getRegistrationNo();
								start = false;
								last_fuel_level = e.getFuelLevel();
							} else {
								total_fuel_consumption += (last_fuel_level - this_fuel_level_b4_purchase);
								last_fuel_level = e.getFuelLevel();
							}
						}
						FuelConsumption fc = new FuelConsumption();
						fc.setDate(date);
						fc.setRegNo(regNo);
						fc.setLevel(total_fuel_consumption);
						getFuelConsumptions().add(fc);
					}
				}
			}
			gDAO.destroy();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void vehicleMileage() {
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null && getStype() != null && getVehicle_id() != null)
		{
			Calendar start_can = Calendar.getInstance(), end_can = Calendar.getInstance();
			start_can.setTime(getStart_dt());
			end_can.setTime(getEnd_dt());
			int style = Calendar.LONG;
			Locale us = Locale.US;
			setMileageConsumptions(null);
			GeneralDAO gDAO = new GeneralDAO();
			if(getStype().equalsIgnoreCase("Month")) {
				start_can.set(Calendar.HOUR_OF_DAY, start_can.getMinimum(Calendar.HOUR_OF_DAY));
				start_can.set(Calendar.MINUTE, start_can.getMinimum(Calendar.MINUTE));
				start_can.set(Calendar.SECOND, start_can.getMinimum(Calendar.SECOND));
				start_can.set(Calendar.MILLISECOND, start_can.getMinimum(Calendar.MILLISECOND));
				while(end_can.after(start_can)) {
					String date = start_can.get(Calendar.YEAR) + "-" + start_can.getDisplayName(Calendar.MONTH, style, us);
					Date st_dt = start_can.getTime();
					start_can.add(Calendar.MONTH, 1);
					Date ed_dt = start_can.getTime();
					
					Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
					q.setParameter("v_id", getVehicle_id());
					q.setParameter("st_dt", st_dt);
					q.setParameter("ed_dt", ed_dt);
					Object listObj = gDAO.search(q, 0);
					if(listObj != null) {
						String regNo = "N/A";
						double start_odometer = 0, end_odometer = 0;
						boolean start = true;
						Vector<VehicleTrackerData> list = (Vector<VehicleTrackerData>)listObj;
						for(VehicleTrackerData e : list) {
							end_odometer = e.getOdometer();
							if(start) {
								regNo = e.getVehicle().getRegistrationNo();
								start = false;
								start_odometer = e.getOdometer();
							}
						}
						FuelConsumption fc = new FuelConsumption();
						fc.setDate(date);
						fc.setRegNo(regNo);
						fc.setLevel(Math.abs(end_odometer-start_odometer));
						getMileageConsumptions().add(fc);
					}
				}
			} else if(getStype().equalsIgnoreCase("Week")) {
				start_can.set(Calendar.HOUR_OF_DAY, start_can.getMinimum(Calendar.HOUR_OF_DAY));
				start_can.set(Calendar.MINUTE, start_can.getMinimum(Calendar.MINUTE));
				start_can.set(Calendar.SECOND, start_can.getMinimum(Calendar.SECOND));
				start_can.set(Calendar.MILLISECOND, start_can.getMinimum(Calendar.MILLISECOND));
				while(end_can.after(start_can)) {
					String date = start_can.get(Calendar.YEAR) + "-" + start_can.getDisplayName(Calendar.MONTH, style, us) + " WK:" + start_can.get(Calendar.WEEK_OF_MONTH);
					Date st_dt = start_can.getTime();
					start_can.add(Calendar.WEEK_OF_YEAR, 1);
					Date ed_dt = start_can.getTime();
					
					Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
					q.setParameter("v_id", getVehicle_id());
					q.setParameter("st_dt", st_dt);
					q.setParameter("ed_dt", ed_dt);
					Object listObj = gDAO.search(q, 0);
					if(listObj != null) {
						String regNo = "N/A";
						double start_odometer = 0, end_odometer = 0;
						boolean start = true;
						Vector<VehicleTrackerData> list = (Vector<VehicleTrackerData>)listObj;
						for(VehicleTrackerData e : list) {
							end_odometer = e.getOdometer();
							if(start) {
								regNo = e.getVehicle().getRegistrationNo();
								start = false;
								start_odometer = e.getOdometer();
							}
						}
						FuelConsumption fc = new FuelConsumption();
						fc.setDate(date);
						fc.setRegNo(regNo);
						fc.setLevel(Math.abs(end_odometer-start_odometer));
						getMileageConsumptions().add(fc);
					}
				}
			} else if(getStype().equalsIgnoreCase("Day")) {
				start_can.set(Calendar.HOUR_OF_DAY, start_can.getMinimum(Calendar.HOUR_OF_DAY));
				start_can.set(Calendar.MINUTE, start_can.getMinimum(Calendar.MINUTE));
				start_can.set(Calendar.SECOND, start_can.getMinimum(Calendar.SECOND));
				start_can.set(Calendar.MILLISECOND, start_can.getMinimum(Calendar.MILLISECOND));
				while(end_can.after(start_can)) {
					String date = start_can.get(Calendar.YEAR) + "-" + start_can.getDisplayName(Calendar.MONTH, style, us) + "-" + start_can.get(Calendar.DATE);
					Date st_dt = start_can.getTime();
					start_can.add(Calendar.DATE, 1);
					Date ed_dt = start_can.getTime();
					
					Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
					q.setParameter("v_id", getVehicle_id());
					q.setParameter("st_dt", st_dt);
					q.setParameter("ed_dt", ed_dt);
					Object listObj = gDAO.search(q, 0);
					if(listObj != null) {
						String regNo = "N/A";
						double start_odometer = 0, end_odometer = 0;
						boolean start = true;
						Vector<VehicleTrackerData> list = (Vector<VehicleTrackerData>)listObj;
						for(VehicleTrackerData e : list) {
							end_odometer = e.getOdometer();
							if(start) {
								regNo = e.getVehicle().getRegistrationNo();
								start = false;
								start_odometer = e.getOdometer();
							}
						}
						FuelConsumption fc = new FuelConsumption();
						fc.setDate(date);
						fc.setRegNo(regNo);
						fc.setLevel(Math.abs(end_odometer-start_odometer));
						getMileageConsumptions().add(fc);
					}
				}
			} else if(getStype().equalsIgnoreCase("Hour")) {
				start_can.set(Calendar.HOUR_OF_DAY, start_can.getMinimum(Calendar.HOUR_OF_DAY));
				start_can.set(Calendar.MINUTE, start_can.getMinimum(Calendar.MINUTE));
				start_can.set(Calendar.SECOND, start_can.getMinimum(Calendar.SECOND));
				start_can.set(Calendar.MILLISECOND, start_can.getMinimum(Calendar.MILLISECOND));
				while(end_can.after(start_can)) {
					String date = start_can.get(Calendar.YEAR) + "-" + start_can.getDisplayName(Calendar.MONTH, style, us) + "-" + start_can.get(Calendar.DATE) + " " + start_can.get(Calendar.HOUR_OF_DAY);
					Date st_dt = start_can.getTime();
					start_can.add(Calendar.HOUR_OF_DAY, 1);
					Date ed_dt = start_can.getTime();
					
					Query q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
					q.setParameter("v_id", getVehicle_id());
					q.setParameter("st_dt", st_dt);
					q.setParameter("ed_dt", ed_dt);
					Object listObj = gDAO.search(q, 0);
					if(listObj != null) {
						String regNo = "N/A";
						double start_odometer = 0, end_odometer = 0;
						boolean start = true;
						Vector<VehicleTrackerData> list = (Vector<VehicleTrackerData>)listObj;
						for(VehicleTrackerData e : list) {
							end_odometer = e.getOdometer();
							if(start) {
								regNo = e.getVehicle().getRegistrationNo();
								start = false;
								start_odometer = e.getOdometer();
							}
						}
						FuelConsumption fc = new FuelConsumption();
						fc.setDate(date);
						fc.setRegNo(regNo);
						fc.setLevel(Math.abs(end_odometer-start_odometer));
						getMileageConsumptions().add(fc);
					}
				}
			}
			gDAO.destroy();
		}
	}
	
	private void resetReportInfo()
	{
		setReport_title(null);
		setReport_end_dt(null);
		setReport_start_dt(null);
	}
	
	public String gotoReportPage(String page, boolean subFunction)
	{
		setReport_page("/faces/"+page+".xhtml");
		if(!subFunction)
			dashBean.setFunction_page(page);
		
		setPieModel(null);
		setBarModel(null);
		
		return "reports?faces-redirect=true";
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

	public Long getPartner_id() {
		return partner_id;
	}

	public void setPartner_id(Long partner_id) {
		this.partner_id = partner_id;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public Long getRole_id() {
		return role_id;
	}

	public void setRole_id(Long role_id) {
		this.role_id = role_id;
	}

	public Long getRegion_id() {
		return region_id;
	}

	public void setRegion_id(Long region_id) {
		this.region_id = region_id;
	}

	public Long getDepartment_id() {
		return department_id;
	}

	public void setDepartment_id(Long department_id) {
		this.department_id = department_id;
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

	public int getRgroup() {
		return rgroup;
	}

	public void setRgroup(int rgroup) {
		this.rgroup = rgroup;
		if(rgroup == 1) //daily
			setGroupBy("Daily");
		else if(rgroup == 2) //weekly
			setGroupBy("Weekly");
		else if(rgroup == 3)
			setGroupBy("Monthly");
	}

	public Long getFleet_id() {
		return fleet_id;
	}

	public void setFleet_id(Long fleet_id) {
		this.fleet_id = fleet_id;
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
				if(fleets.get(0).getPartner().getId() == getPartner().getId())
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
				}
			}
		}
		return fleets;
	}
	
	public void createWord(int type, String filename)
	{
		try
		{
			FacesContext context = FacesContext.getCurrentInstance();
			XWPFDocument document = new XWPFDocument();
			
			XWPFParagraph paragraphOne = document.createParagraph();
	        paragraphOne.setAlignment(ParagraphAlignment.CENTER);
	        paragraphOne.setBorderBottom(Borders.SINGLE);
	        paragraphOne.setBorderTop(Borders.SINGLE);
	        paragraphOne.setBorderRight(Borders.SINGLE);
	        paragraphOne.setBorderLeft(Borders.SINGLE);
	        paragraphOne.setBorderBetween(Borders.SINGLE);
	        
	        XWPFRun paragraphOneRunOne = paragraphOne.createRun();
	        paragraphOneRunOne.setBold(true);
	        paragraphOneRunOne.setItalic(true);
	        paragraphOneRunOne.setText(getReport_title());
	        paragraphOneRunOne.addBreak();
	        
	        exportWordTable(type, document);
	        
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        document.write(baos);
	        
	        String fileName = filename + ".docx";
			
			writeFileToResponse(context.getExternalContext(), baos, fileName, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
			
			context.responseComplete();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
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
	
	private void exportWordTable(int type, XWPFDocument document)
	{
		XWPFTable table = document.createTable();
		switch(type)
		{
			case 1: // all users report
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "User name", true);
		        
				addHeaderCell(tableRowOne, "Full name", false);
				addHeaderCell(tableRowOne, "Email", false);
				addHeaderCell(tableRowOne, "Department", false);
				addHeaderCell(tableRowOne, "Region", false);
				addHeaderCell(tableRowOne, "Phone", false);
				addHeaderCell(tableRowOne, "Position", false);
				
				if(getAllUsers() != null)
				{
					for(PartnerUser e : getAllUsers())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getUsername());
						tableRow.getCell(1).setText(e.getPersonel().getFirstname() + " " + e.getPersonel().getLastname());
						tableRow.getCell(2).setText(e.getPersonel().getEmail());
						tableRow.getCell(3).setText((e.getPersonel().getDepartment()!=null) ? e.getPersonel().getDepartment().getName() : "N/A");
						tableRow.getCell(4).setText((e.getPersonel().getRegion()!=null) ? e.getPersonel().getRegion().getName() : "N/A");
						tableRow.getCell(5).setText(e.getPersonel().getPhone());
						tableRow.getCell(6).setText(e.getPersonel().getPosition());
					}
				}
				break;
			}
			case 2: // due driver's license
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Full Name", true);
		        
				addHeaderCell(tableRowOne, "Dept", false);
				addHeaderCell(tableRowOne, "Region", false);
				addHeaderCell(tableRowOne, "Phone", false);
				addHeaderCell(tableRowOne, "Position", false);
				addHeaderCell(tableRowOne, "License Due Date", false);
				
				if(getDueDriversLic() != null)
				{
					for(DriverLicense e : getDueDriversLic())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getDriver().getPersonel().getFirstname() + " " + e.getDriver().getPersonel().getLastname());
						tableRow.getCell(1).setText(e.getDriver().getPersonel().getDepartment()!=null ?e.getDriver().getPersonel().getDepartment().getName() : "N/A");
						tableRow.getCell(2).setText(e.getDriver().getPersonel().getRegion()!=null ?e.getDriver().getPersonel().getRegion().getName() : "N/A");
						tableRow.getCell(3).setText(e.getDriver().getPersonel().getPhone());
						tableRow.getCell(4).setText(e.getDriver().getPersonel().getPosition());
						tableRow.getCell(5).setText(e.getLic_end_dt().toLocaleString());
					}
				}
				break;
			}
			case 3: // accidents
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Reg No.", true);
		        
				addHeaderCell(tableRowOne, "Accident Date", false);
				addHeaderCell(tableRowOne, "Description", false);
				addHeaderCell(tableRowOne, "Driver", false);
				addHeaderCell(tableRowOne, "Driver Comment", false);
				addHeaderCell(tableRowOne, "Police Officer", false);
				addHeaderCell(tableRowOne, "Police Station", false);
				addHeaderCell(tableRowOne, "Police Comment", false);
				
				if(getVehicleAccidents() != null)
				{
					for(VehicleAccident e : getVehicleAccidents())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getVehicle().getRegistrationNo());
						tableRow.getCell(1).setText(e.getAccident_dt().toLocaleString());
						tableRow.getCell(2).setText(e.getAccidentDescription());
						tableRow.getCell(3).setText(e.getDriver_name());
						tableRow.getCell(4).setText(e.getDriverComment());
						tableRow.getCell(5).setText(e.getPoliceOfficer());
						tableRow.getCell(6).setText(e.getPoliceStation());
						tableRow.getCell(7).setText(e.getPoliceComment());
					}
				}
				break;
			}
			case 4: // vehicles by brand
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Make", true);
		        
				addHeaderCell(tableRowOne, "Model", false);
				addHeaderCell(tableRowOne, "Year", false);
				addHeaderCell(tableRowOne, "Reg No.", false);
				
				if(getVehiclesByBrand() != null)
				{
					for(Vehicle e : getVehiclesByBrand())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getModel().getMaker().getName());
						tableRow.getCell(1).setText(e.getModel().getName());
						tableRow.getCell(2).setText(e.getModel().getYear());
						tableRow.getCell(3).setText(e.getRegistrationNo());
					}
				}
				break;
			}
			case 5: // routine maintenance
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Reg No.", true);
		        
				addHeaderCell(tableRowOne, "Requester", false);
				addHeaderCell(tableRowOne, "Description", false);
				addHeaderCell(tableRowOne, "Start Date", false);
				addHeaderCell(tableRowOne, "Initial Cost", false);
				addHeaderCell(tableRowOne, "Status", false);
				addHeaderCell(tableRowOne, "Close Date", false);
				addHeaderCell(tableRowOne, "Final Cost", false);
				
				if(getRmaints() != null)
				{
					for(VehicleRoutineMaintenance e : getRmaints())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getVehicle().getRegistrationNo());
						tableRow.getCell(1).setText(e.getCreatedBy().getPersonel().getFirstname() + " " + e.getCreatedBy().getPersonel().getLastname());
						tableRow.getCell(2).setText(e.getDescription());
						tableRow.getCell(3).setText(e.getStart_dt().toLocaleString());
						tableRow.getCell(4).setText(e.getInitial_amount().toPlainString());
						tableRow.getCell(5).setText(e.getStatus());
						tableRow.getCell(6).setText(e.getClose_dt().toLocaleString());
						tableRow.getCell(7).setText(e.getClosed_amount().toPlainString());
					}
				}
				break;
			}
			case 6: // ad hoc maintenance
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Reg No.", true);
		        
				addHeaderCell(tableRowOne, "Requester", false);
				addHeaderCell(tableRowOne, "Description", false);
				addHeaderCell(tableRowOne, "Start Date", false);
				addHeaderCell(tableRowOne, "Initial Cost", false);
				addHeaderCell(tableRowOne, "Status", false);
				addHeaderCell(tableRowOne, "Close Date", false);
				addHeaderCell(tableRowOne, "Final Cost", false);
				
				if(getAdhocmaints() != null)
				{
					for(VehicleAdHocMaintenance e : getAdhocmaints())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getVehicle().getRegistrationNo());
						tableRow.getCell(1).setText(e.getCreatedBy().getPersonel().getFirstname() + " " + e.getCreatedBy().getPersonel().getLastname());
						tableRow.getCell(2).setText(e.getDescription());
						tableRow.getCell(3).setText(e.getStart_dt().toLocaleString());
						tableRow.getCell(4).setText(e.getInitial_cost().toPlainString());
						tableRow.getCell(5).setText(e.getStatus());
						tableRow.getCell(6).setText(e.getClose_dt().toLocaleString());
						tableRow.getCell(7).setText(e.getClosed_cost().toPlainString());
					}
				}
				break;
			}
			case 7: // active accidents
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Reg No.", true);
		        
				addHeaderCell(tableRowOne, "Accident Date", false);
				addHeaderCell(tableRowOne, "Description", false);
				addHeaderCell(tableRowOne, "Driver Name", false);
				addHeaderCell(tableRowOne, "Driver Comment", false);
				addHeaderCell(tableRowOne, "Police Name", false);
				addHeaderCell(tableRowOne, "Station Name", false);
				addHeaderCell(tableRowOne, "Police Comment", false);
				
				if(getActiveAccidents() != null)
				{
					for(VehicleAccident e : getActiveAccidents())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getVehicle().getRegistrationNo());
						tableRow.getCell(1).setText(e.getAccident_dt().toLocaleString());
						tableRow.getCell(2).setText(e.getAccidentDescription());
						tableRow.getCell(3).setText(e.getDriver_name());
						tableRow.getCell(4).setText(e.getDriverComment());
						tableRow.getCell(5).setText(e.getPoliceOfficer());
						tableRow.getCell(6).setText(e.getPoliceStation());
						tableRow.getCell(7).setText(e.getPoliceComment());
					}
				}
				break;
			}
			case 8: // coporate trips
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Requester", true);
		        
				addHeaderCell(tableRowOne, "Department", false);
				addHeaderCell(tableRowOne, "Reg No.", false);
				addHeaderCell(tableRowOne, "Departure Location", false);
				addHeaderCell(tableRowOne, "Arrival Location", false);
				addHeaderCell(tableRowOne, "Departure Date and Time", false);
				addHeaderCell(tableRowOne, "Est. Arrival Date and Time", false);
				addHeaderCell(tableRowOne, "Act. Arrival Date and Time", false);
				addHeaderCell(tableRowOne, "Status", false);
				
				if(getCorTrips() != null)
				{
					for(CorporateTrip e : getCorTrips())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getStaff().getFirstname() + " " + e.getStaff().getLastname());
						tableRow.getCell(1).setText((e.getStaff().getDepartment() != null) ? e.getStaff().getDepartment().getName() : "N/A");
						tableRow.getCell(2).setText(e.getVehicle()!=null ? e.getVehicle().getRegistrationNo() : "N/A");
						tableRow.getCell(3).setText(e.getDepartureLocation());
						tableRow.getCell(4).setText(e.getArrivalLocation());
						tableRow.getCell(5).setText(e.getDepartureDateTime().toLocaleString());
						tableRow.getCell(6).setText(e.getEstimatedArrivalDateTime().toLocaleString());
						tableRow.getCell(7).setText(e.getCompleteRequestDateTime()!=null ? e.getCompleteRequestDateTime().toLocaleString() : "N/A");
						tableRow.getCell(8).setText(e.getTripStatus());
					}
				}
				break;
			}
			case 9: // expense report
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Type", true);
		        
				addHeaderCell(tableRowOne, "Date", false);
				addHeaderCell(tableRowOne, "Amount", false);
				addHeaderCell(tableRowOne, "Detail", false);
				
				if(getExpenses() != null)
				{
					for(Expense e : getExpenses())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getType().getName());
						tableRow.getCell(0).setText(e.getExpense_dt().toLocaleString());
						tableRow.getCell(0).setText(""+e.getAmount());
						tableRow.getCell(0).setText(e.getRemarks());
					}
				}
				break;
			}
			case 10: // vehicle ages
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Make", true);
		        
				addHeaderCell(tableRowOne, "Model", false);
				addHeaderCell(tableRowOne, "Year", false);
				addHeaderCell(tableRowOne, "Reg No.", false);
				addHeaderCell(tableRowOne, "Date of Purchase", false);
				addHeaderCell(tableRowOne, "Age (in years)", false);
				
				if(getVehiclesAges() != null)
				{
					for(Vehicle e : getVehiclesAges())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getModel().getMaker() != null ? e.getModel().getMaker().getName() : "N/A");
						tableRow.getCell(1).setText(e.getModel() != null ? e.getModel().getName() : "N/A");
						tableRow.getCell(2).setText(e.getModel() != null ? e.getModel().getYear() : "N/A");
						tableRow.getCell(3).setText(e.getRegistrationNo());
						tableRow.getCell(4).setText(e.getPurchaseDate() != null ? e.getPurchaseDate().toLocaleString() : "N/A");
						tableRow.getCell(5).setText(""+e.getAge());
					}
				}
				break;
			}
			case 11: // vehicle fuelings
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Date", true);
		        
				addHeaderCell(tableRowOne, "Reg No.", false);
				addHeaderCell(tableRowOne, "Litres", false);
				addHeaderCell(tableRowOne, "Amount", false);
				addHeaderCell(tableRowOne, "Source", false);
				
				if(getFuelings() != null)
				{
					for(VehicleFueling e : getFuelings())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getCaptured_dt().toLocaleString());
						tableRow.getCell(1).setText(e.getVehicle().getRegistrationNo());
						tableRow.getCell(2).setText(""+e.getLitres());
						tableRow.getCell(3).setText(""+e.getAmt());
						tableRow.getCell(4).setText(e.getSource());
					}
				}
				break;
			}
			case 12: // due vehicle licenses
			{
				XWPFTableRow tableRowOne = table.getRow(0);
				
				addHeaderCell(tableRowOne, "Reg No.", true);
		        
				addHeaderCell(tableRowOne, "Fleet", false);
				addHeaderCell(tableRowOne, "Make", false);
				addHeaderCell(tableRowOne, "Model", false);
				addHeaderCell(tableRowOne, "License Due Date", false);
				
				if(getDueVehicleLicenses() != null)
				{
					for(VehicleLicense e : getDueVehicleLicenses())
					{
						XWPFTableRow tableRow = table.createRow();
						tableRow.getCell(0).setText(e.getVehicle().getRegistrationNo());
						tableRow.getCell(1).setText(e.getVehicle().getFleet().getName());
						tableRow.getCell(2).setText(e.getVehicle().getModel()!=null ?e.getVehicle().getModel().getMaker().getName() : "N/A");
						tableRow.getCell(3).setText(e.getVehicle().getModel()!=null ?e.getVehicle().getModel().getName() : "N/A");
						tableRow.getCell(4).setText(e.getLic_end_dt().toLocaleString());
					}
				}
				break;
			}
		}
	}
	
	static class HeaderFooter extends PdfPageEventHelper
	{
		public void onEndPage(PdfWriter writer, Document document)
		{
			Rectangle rect = writer.getBoxSize("footer");
			BaseFont bf_times;
			try
			{
				bf_times = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1252", false);
				Font font = new Font(bf_times, 9);
				ColumnText.showTextAligned(writer.getDirectContent(),
					    Element.ALIGN_RIGHT, new Phrase(String.format("%d", writer.getPageNumber()), font),  rect.getRight(),
					    rect.getBottom() - 18, 0);
				ColumnText.showTextAligned(writer.getDirectContent(),
						Element.ALIGN_LEFT, new Phrase("Copyright " + Calendar.getInstance().get(Calendar.YEAR), font), 
						rect.getLeft(), rect.getBottom() - 18, 0);
			}
			catch (DocumentException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void createPDF(int type, String filename, int pageType)
	{
		try
		{
			FacesContext context = FacesContext.getCurrentInstance();
			Document document = new Document();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter writer = PdfWriter.getInstance(document, baos);
			writer.setPageEvent(new HeaderFooter());
			writer.setBoxSize("footer", new Rectangle(36, 54, 559, 788));
			if (!document.isOpen())
			{
				document.open();
			}
			
			switch(pageType)
			{
			case 1:
				document.setPageSize(PageSize.A4);
				break;
			case 2:
				document.setPageSize(PageSize.A4.rotate());
				break;
			}
			document.addAuthor("FMS");
			document.addCreationDate();
			document.addCreator("FMS");
			document.addSubject("Report");
			document.addTitle(getReport_title());
			
			PdfPTable headerTable = new PdfPTable(1);
			
			ServletContext servletContext = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
			String logo = servletContext.getRealPath("") + File.separator + "resources" + File.separator + "images" + File.separator + "satraklogo.jpg";
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", dashBean.getUser().getPartner());
			GeneralDAO gDAO = new GeneralDAO();
			Object pSettingsObj = gDAO.search("PartnerSetting", params);
			PartnerSetting setting = null;
			if(pSettingsObj != null)
			{
				Vector<PartnerSetting> pSettingsList = (Vector<PartnerSetting>)pSettingsObj;
				for(PartnerSetting e : pSettingsList)
				{
					setting = e;
				}
			}
			gDAO.destroy();
			
			PdfPCell c = null;
			if(setting != null && setting.getLogo() != null)
			{
				Image logoImg = Image.getInstance(setting.getLogo());
				logoImg.scaleToFit(212, 51);
				c = new PdfPCell(logoImg);
			}
			else
				c = new PdfPCell(Image.getInstance(logo));
			c.setBorder(0);
			c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			headerTable.addCell(c);
			
			Paragraph stars = new Paragraph(20);
			stars.add(Chunk.NEWLINE);
	        stars.setSpacingAfter(20);
			
			c = new PdfPCell(stars);
			c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			c.setBorder(0);
			headerTable.addCell(c);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{
				//font exception
			}
			Font font = new Font(helvetica, 16, Font.NORMAL|Font.BOLD);
			
			c = new PdfPCell(new Paragraph(getReport_title(), font));
			c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			c.setBorder(0);
			headerTable.addCell(c);
			
			if(getReport_start_dt() != null && getReport_end_dt() != null)
			{
				stars = new Paragraph(20);
				stars.add(Chunk.NEWLINE);
		        stars.setSpacingAfter(10);
				
				c = new PdfPCell(stars);
				c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				c.setBorder(0);
				headerTable.addCell(c);
				
				new Font(helvetica, 12, Font.NORMAL);
				Paragraph ch = new Paragraph("From " + getReport_start_dt() + " to " + getReport_end_dt(), font);
				c = new PdfPCell(ch);
				c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				headerTable.addCell(c);
			}
			stars = new Paragraph(20);
			stars.add(Chunk.NEWLINE);
	        stars.setSpacingAfter(20);
			
			c = new PdfPCell(stars);
			c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			c.setBorder(0);
			headerTable.addCell(c);
			document.add(headerTable);
			
			PdfPTable pdfTable = exportPDFTable(type);
			if(pdfTable != null)
				document.add(pdfTable);
			
			//Keep modifying your pdf file (add pages and more)
			
			document.close();
			String fileName = filename + ".pdf";
			
			writeFileToResponse(context.getExternalContext(), baos, fileName, "application/pdf");
			
			context.responseComplete();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	private PdfPTable exportPDFTable(int type)
	{
		int numberOfColumns = 0;
		PdfPTable pdfTable = null;
		switch(type)
		{
		case 1: // all users report
		{
			numberOfColumns = 7;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("User name", font));
			pdfTable.addCell(new Paragraph("Full name", font));
			pdfTable.addCell(new Paragraph("Email", font));
			pdfTable.addCell(new Paragraph("Department", font));
			pdfTable.addCell(new Paragraph("Region", font));
			pdfTable.addCell(new Paragraph("Phone", font));
			pdfTable.addCell(new Paragraph("Position", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getAllUsers() != null)
			{
				for(PartnerUser e : getAllUsers())
				{
					pdfTable.addCell(new Paragraph(e.getUsername(), font));
					pdfTable.addCell(new Paragraph(e.getPersonel().getFirstname() + " " + e.getPersonel().getLastname(), font));
					pdfTable.addCell(new Paragraph(e.getPersonel().getEmail(), font));
					pdfTable.addCell(new Paragraph((e.getPersonel().getDepartment()!=null) ? e.getPersonel().getDepartment().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph((e.getPersonel().getRegion()!=null) ? e.getPersonel().getRegion().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getPersonel().getPhone(), font));
					pdfTable.addCell(new Paragraph(e.getPersonel().getPosition(), font));
				}
			}
			break;
		}
		case 2: // due driver's license
		{
			numberOfColumns = 6;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Full Name", font));
			pdfTable.addCell(new Paragraph("Dept", font));
			pdfTable.addCell(new Paragraph("Region", font));
			pdfTable.addCell(new Paragraph("Phone", font));
			pdfTable.addCell(new Paragraph("Position", font));
			pdfTable.addCell(new Paragraph("License Due Date", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getDueDriversLic() != null)
			{
				for(DriverLicense e : getDueDriversLic())
				{
					pdfTable.addCell(new Paragraph(e.getDriver().getPersonel().getFirstname() + " " + e.getDriver().getPersonel().getLastname(), font));
					pdfTable.addCell(new Paragraph(e.getDriver().getPersonel().getDepartment()!=null ?e.getDriver().getPersonel().getDepartment().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getDriver().getPersonel().getRegion()!=null ?e.getDriver().getPersonel().getRegion().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getDriver().getPersonel().getPhone(), font));
					pdfTable.addCell(new Paragraph(e.getDriver().getPersonel().getPosition(), font));
					pdfTable.addCell(new Paragraph(e.getLic_end_dt().toLocaleString(), font));
				}
			}
			break;
		}
		case 3: // accidents
		{
			numberOfColumns = 8;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Accident Date", font));
			pdfTable.addCell(new Paragraph("Description", font));
			pdfTable.addCell(new Paragraph("Driver", font));
			pdfTable.addCell(new Paragraph("Driver Comment", font));
			pdfTable.addCell(new Paragraph("Police Officer", font));
			pdfTable.addCell(new Paragraph("Police Station", font));
			pdfTable.addCell(new Paragraph("Police Comment", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getVehicleAccidents() != null)
			{
				for(VehicleAccident e : getVehicleAccidents())
				{
					pdfTable.addCell(new Paragraph(e.getVehicle().getRegistrationNo(), font));
					pdfTable.addCell(new Paragraph(e.getAccident_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getAccidentDescription(), font));
					pdfTable.addCell(new Paragraph(e.getDriver_name(), font));
					pdfTable.addCell(new Paragraph(e.getDriverComment(), font));
					pdfTable.addCell(new Paragraph(e.getPoliceOfficer(), font));
					pdfTable.addCell(new Paragraph(e.getPoliceStation(), font));
					pdfTable.addCell(new Paragraph(e.getPoliceComment(), font));
				}
			}
			break;
		}
		case 4: // vehicles by brand
		{
			numberOfColumns = 4;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Make", font));
			pdfTable.addCell(new Paragraph("Model", font));
			pdfTable.addCell(new Paragraph("Year", font));
			pdfTable.addCell(new Paragraph("Reg No.", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getVehiclesByBrand() != null)
			{
				for(Vehicle e : getVehiclesByBrand())
				{
					pdfTable.addCell(new Paragraph(e.getModel().getMaker().getName(), font));
					pdfTable.addCell(new Paragraph(e.getModel().getName(), font));
					pdfTable.addCell(new Paragraph(e.getModel().getYear(), font));
					pdfTable.addCell(new Paragraph(e.getRegistrationNo(), font));
				}
			}
			break;
		}
		case 5: // routine maintenance
		{
			numberOfColumns = 8;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Requester", font));
			pdfTable.addCell(new Paragraph("Description", font));
			pdfTable.addCell(new Paragraph("Start Date", font));
			pdfTable.addCell(new Paragraph("Initial Cost", font));
			pdfTable.addCell(new Paragraph("Status", font));
			pdfTable.addCell(new Paragraph("Close Date", font));
			pdfTable.addCell(new Paragraph("Final Cost", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getRmaints() != null)
			{
				for(VehicleRoutineMaintenance e : getRmaints())
				{
					pdfTable.addCell(new Paragraph(e.getVehicle().getRegistrationNo(), font));
					pdfTable.addCell(new Paragraph(e.getCreatedBy().getPersonel().getFirstname() + " " + e.getCreatedBy().getPersonel().getLastname(), font));
					pdfTable.addCell(new Paragraph(e.getDescription(), font));
					pdfTable.addCell(new Paragraph(e.getStart_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getInitial_amount().toPlainString(), font));
					pdfTable.addCell(new Paragraph(e.getStatus(), font));
					pdfTable.addCell(new Paragraph(e.getClose_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getClosed_amount().toPlainString(), font));
				}
			}
			break;
		}
		case 6: // ad hoc maintenance
		{
			numberOfColumns = 8;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Requester", font));
			pdfTable.addCell(new Paragraph("Description", font));
			pdfTable.addCell(new Paragraph("Start Date", font));
			pdfTable.addCell(new Paragraph("Initial Cost", font));
			pdfTable.addCell(new Paragraph("Status", font));
			pdfTable.addCell(new Paragraph("Close Date", font));
			pdfTable.addCell(new Paragraph("Final Cost", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getAdhocmaints() != null)
			{
				for(VehicleAdHocMaintenance e : getAdhocmaints())
				{
					pdfTable.addCell(new Paragraph(e.getVehicle().getRegistrationNo(), font));
					pdfTable.addCell(new Paragraph(e.getCreatedBy().getPersonel().getFirstname() + " " + e.getCreatedBy().getPersonel().getLastname(), font));
					pdfTable.addCell(new Paragraph(e.getDescription(), font));
					pdfTable.addCell(new Paragraph(e.getStart_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getInitial_cost().toPlainString(), font));
					pdfTable.addCell(new Paragraph(e.getStatus(), font));
					pdfTable.addCell(new Paragraph(e.getClose_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getClosed_cost().toPlainString(), font));
				}
			}
			break;
		}
		case 7: // active accidents
		{
			numberOfColumns = 8;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Accident Date", font));
			pdfTable.addCell(new Paragraph("Description", font));
			pdfTable.addCell(new Paragraph("Driver Name", font));
			pdfTable.addCell(new Paragraph("Driver Comment", font));
			pdfTable.addCell(new Paragraph("Police Name", font));
			pdfTable.addCell(new Paragraph("Station Name", font));
			pdfTable.addCell(new Paragraph("Police Comment", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getActiveAccidents() != null)
			{
				for(VehicleAccident e : getActiveAccidents())
				{
					pdfTable.addCell(new Paragraph(e.getVehicle().getRegistrationNo(), font));
					pdfTable.addCell(new Paragraph(e.getAccident_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getAccidentDescription(), font));
					pdfTable.addCell(new Paragraph(e.getDriver_name(), font));
					pdfTable.addCell(new Paragraph(e.getDriverComment(), font));
					pdfTable.addCell(new Paragraph(e.getPoliceOfficer(), font));
					pdfTable.addCell(new Paragraph(e.getPoliceStation(), font));
					pdfTable.addCell(new Paragraph(e.getPoliceComment(), font));
				}
			}
			break;
		}
		case 8: // coporate trips
		{
			numberOfColumns = 9;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 6, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Requester", font));
			pdfTable.addCell(new Paragraph("Department", font));
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Departure Location", font));
			pdfTable.addCell(new Paragraph("Arrival Location", font));
			pdfTable.addCell(new Paragraph("Departure Date and Time", font));
			pdfTable.addCell(new Paragraph("Est. Arrival Date and Time", font));
			pdfTable.addCell(new Paragraph("Act. Arrival Date and Time", font));
			pdfTable.addCell(new Paragraph("Status", font));
			
			font = new Font(helvetica, 6, Font.NORMAL);
			
			if(getCorTrips() != null)
			{
				for(CorporateTrip e : getCorTrips())
				{
					pdfTable.addCell(new Paragraph(e.getStaff().getFirstname() + " " + e.getStaff().getLastname(), font));
					pdfTable.addCell(new Paragraph((e.getStaff().getDepartment() != null) ? e.getStaff().getDepartment().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getVehicle()!=null ? e.getVehicle().getRegistrationNo() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getDepartureLocation(), font));
					pdfTable.addCell(new Paragraph(e.getArrivalLocation(), font));
					pdfTable.addCell(new Paragraph(e.getDepartureDateTime().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getEstimatedArrivalDateTime().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getCompleteRequestDateTime()!=null ? e.getCompleteRequestDateTime().toLocaleString() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getTripStatus(), font));
				}
			}
			break;
		}
		case 9: // expense report
		{
			numberOfColumns = 4;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Type", font));
			pdfTable.addCell(new Paragraph("Date", font));
			pdfTable.addCell(new Paragraph("Amount", font));
			pdfTable.addCell(new Paragraph("Detail", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getExpenses() != null)
			{
				for(Expense e : getExpenses())
				{
					pdfTable.addCell(new Paragraph(e.getType().getName(), font));
					pdfTable.addCell(new Paragraph(e.getExpense_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(""+e.getAmount(), font));
					pdfTable.addCell(new Paragraph(e.getRemarks(), font));
				}
			}
			break;
		}
		case 10: // vehicle ages
		{
			numberOfColumns = 6;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Make", font));
			pdfTable.addCell(new Paragraph("Model", font));
			pdfTable.addCell(new Paragraph("Year", font));
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Date of Purchase", font));
			pdfTable.addCell(new Paragraph("Age (in years)", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getVehiclesAges() != null)
			{
				for(Vehicle e : getVehiclesAges())
				{
					pdfTable.addCell(new Paragraph(e.getModel().getMaker() != null ? e.getModel().getMaker().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getModel() != null ? e.getModel().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getModel() != null ? e.getModel().getYear() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getRegistrationNo(), font));
					pdfTable.addCell(new Paragraph(e.getPurchaseDate() != null ? e.getPurchaseDate().toLocaleString() : "N/A", font));
					pdfTable.addCell(new Paragraph(""+e.getAge(), font));
				}
			}
			break;
		}
		case 11: // vehicle fuelings
		{
			numberOfColumns = 5;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Date", font));
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Litres", font));
			pdfTable.addCell(new Paragraph("Amount", font));
			pdfTable.addCell(new Paragraph("Source", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getFuelings() != null)
			{
				for(VehicleFueling e : getFuelings())
				{
					pdfTable.addCell(new Paragraph(e.getCaptured_dt().toLocaleString(), font));
					pdfTable.addCell(new Paragraph(e.getVehicle().getRegistrationNo(), font));
					pdfTable.addCell(new Paragraph(""+e.getLitres(), font));
					pdfTable.addCell(new Paragraph(""+e.getAmt(), font));
					pdfTable.addCell(new Paragraph(e.getSource(), font));
				}
			}
			break;
		}
		case 12: // due vehicle licenses
		{
			numberOfColumns = 5;
			pdfTable = new PdfPTable(numberOfColumns);
			
			BaseFont helvetica = null;
			try
			{
				helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
			}
			catch (Exception e)
			{}
			Font font = new Font(helvetica, 8, Font.BOLDITALIC);
			
			pdfTable.addCell(new Paragraph("Reg No.", font));
			pdfTable.addCell(new Paragraph("Fleet", font));
			pdfTable.addCell(new Paragraph("Make", font));
			pdfTable.addCell(new Paragraph("Model", font));
			pdfTable.addCell(new Paragraph("License Due Date", font));
			
			font = new Font(helvetica, 8, Font.NORMAL);
			
			if(getDueVehicleLicenses() != null)
			{
				for(VehicleLicense e : getDueVehicleLicenses())
				{
					pdfTable.addCell(new Paragraph(e.getVehicle().getRegistrationNo(), font));
					pdfTable.addCell(new Paragraph(e.getVehicle().getFleet().getName(), font));
					pdfTable.addCell(new Paragraph(e.getVehicle().getModel()!=null ?e.getVehicle().getModel().getMaker().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getVehicle().getModel()!=null ?e.getVehicle().getModel().getName() : "N/A", font));
					pdfTable.addCell(new Paragraph(e.getLic_end_dt().toLocaleString(), font));
				}
			}
			break;
		}
		}
		
		pdfTable.setWidthPercentage(100);
		
		return pdfTable;
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

	public void setFleets(Vector<Fleet> fleets) {
		this.fleets = fleets;
	}

	public String getRegNo() {
		return regNo;
	}

	public void setRegNo(String regNo) {
		this.regNo = regNo;
	}

	public String getStype() {
		return stype;
	}

	public void setStype(String stype) {
		this.stype = stype;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String getMainttype() {
		return mainttype;
	}

	public void setMainttype(String mainttype) {
		this.mainttype = mainttype;
	}

	public Long getDriver_id() {
		return driver_id;
	}

	public void setDriver_id(Long driver_id) {
		this.driver_id = driver_id;
	}

	public String getAccidentStatus() {
		return accidentStatus;
	}

	public void setAccidentStatus(String accidentStatus) {
		this.accidentStatus = accidentStatus;
	}

	public Long getVehicleModel_id() {
		return vehicleModel_id;
	}

	public void setVehicleModel_id(Long vehicleModel_id) {
		this.vehicleModel_id = vehicleModel_id;
	}

	public int getMinYears() {
		return minYears;
	}

	public void setMinYears(int minYears) {
		this.minYears = minYears;
	}

	public int getSearchType() {
		return searchType;
	}

	public void setSearchType(int searchType) {
		this.searchType = searchType;
	}

	public Long getVehicle_id() {
		return vehicle_id;
	}

	public void setVehicle_id(Long vehicle_id) {
		this.vehicle_id = vehicle_id;
	}

	public Vector<PartnerUser> getAllUsers() {
		return allUsers;
	}

	public void setAllUsers(Vector<PartnerUser> allUsers) {
		this.allUsers = allUsers;
	}

	public Vector<DriverLicense> getDueDriversLic() {
		return dueDriversLic;
	}

	public void setDueDriversLic(Vector<DriverLicense> dueDriversLic) {
		this.dueDriversLic = dueDriversLic;
	}

	public Vector<VehicleAccident> getVehicleAccidents() {
		return vehicleAccidents;
	}

	public void setVehicleAccidents(Vector<VehicleAccident> vehicleAccidents) {
		this.vehicleAccidents = vehicleAccidents;
	}

	public Vector<VehicleAccident> getDriverAccidents() {
		return driverAccidents;
	}

	public void setDriverAccidents(Vector<VehicleAccident> driverAccidents) {
		this.driverAccidents = driverAccidents;
	}

	public Vector<VehicleAccident> getStatusAccidents() {
		return statusAccidents;
	}

	public void setStatusAccidents(Vector<VehicleAccident> statusAccidents) {
		this.statusAccidents = statusAccidents;
	}

	public Vector<VehicleAccident> getBrandAccidents() {
		return brandAccidents;
	}

	public void setBrandAccidents(Vector<VehicleAccident> brandAccidents) {
		this.brandAccidents = brandAccidents;
	}

	public Vector<Vehicle> getVehiclesByBrand() {
		return vehiclesByBrand;
	}

	public void setVehiclesByBrand(Vector<Vehicle> vehiclesByBrand) {
		this.vehiclesByBrand = vehiclesByBrand;
	}

	public Vector<Vehicle> getVehicles() {
		return vehicles;
	}

	public void setVehicles(Vector<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public Vector<VehicleRoutineMaintenance> getRmaints() {
		return rmaints;
	}

	public void setRmaints(Vector<VehicleRoutineMaintenance> rmaints) {
		this.rmaints = rmaints;
	}

	public Vector<VehicleAdHocMaintenance> getAdhocmaints() {
		return adhocmaints;
	}

	public void setAdhocmaints(Vector<VehicleAdHocMaintenance> adhocmaints) {
		this.adhocmaints = adhocmaints;
	}

	public Vector<VehicleAccident> getActiveAccidents() {
		return activeAccidents;
	}

	public void setActiveAccidents(Vector<VehicleAccident> activeAccidents) {
		this.activeAccidents = activeAccidents;
	}

	public Vector<Vehicle> getAccidentedVehicles() {
		return accidentedVehicles;
	}

	public void setAccidentedVehicles(Vector<Vehicle> accidentedVehicles) {
		this.accidentedVehicles = accidentedVehicles;
	}

	public Vector<VehicleLicense> getDueVehicleLicenses() {
		return dueVehicleLicenses;
	}

	public void setDueVehicleLicenses(Vector<VehicleLicense> dueVehicleLicenses) {
		this.dueVehicleLicenses = dueVehicleLicenses;
	}

	public Vector<Vehicle> getVehiclesAges() {
		return vehiclesAges;
	}

	public void setVehiclesAges(Vector<Vehicle> vehiclesAges) {
		this.vehiclesAges = vehiclesAges;
	}

	public Vector<PartnerDriver> getDriversByYears() {
		return driversByYears;
	}

	public void setDriversByYears(Vector<PartnerDriver> driversByYears) {
		this.driversByYears = driversByYears;
	}

	public Vector<PartnerDriver> getDriversByRegion() {
		return driversByRegion;
	}

	public void setDriversByRegion(Vector<PartnerDriver> driversByRegion) {
		this.driversByRegion = driversByRegion;
	}

	public Vector<Fleet> getPartnerFleets() {
		return partnerFleets;
	}

	public void setPartnerFleets(Vector<Fleet> partnerFleets) {
		this.partnerFleets = partnerFleets;
	}

	public Vector<FuelConsumption> getFuelConsumptions() {
		if(fuelConsumptions == null)
			fuelConsumptions = new Vector<FuelConsumption>();
		return fuelConsumptions;
	}

	public void setFuelConsumptions(Vector<FuelConsumption> fuelConsumptions) {
		this.fuelConsumptions = fuelConsumptions;
	}

	public Vector<FuelConsumption> getMileageConsumptions() {
		if(mileageConsumptions == null)
			mileageConsumptions = new Vector<FuelConsumption>();
		return mileageConsumptions;
	}

	public void setMileageConsumptions(Vector<FuelConsumption> mileageConsumptions) {
		this.mileageConsumptions = mileageConsumptions;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vehicle> getVehiclesDrivingInfoReport() {
		if(vehiclesDrivingInfoReport == null)
		{
			vehiclesDrivingInfoReport = new Vector<Vehicle>();
			
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
						vehiclesDrivingInfoReport = new Vector<Vehicle>();
						vehiclesDrivingInfoReport.add((Vehicle)vobj);
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
						vehiclesDrivingInfoReport = (Vector<Vehicle>)vhsObj;
					}
				}
				
				if(vehiclesDrivingInfoReport != null && vehiclesDrivingInfoReport.size() > 0)
				{
					for(Vehicle v : vehiclesDrivingInfoReport)
					{
						Query q = gDAO.createQuery("Select e from VehicleBehaviour e where e.vehicle = :vehicle and (e.eventDate between :stdt and :eddt) order by e.eventDate desc");
						
						q.setParameter("vehicle", v);
						q.setParameter("stdt", getStart_dt());
						q.setParameter("eddt", getEnd_dt());
						
						Object retObj = gDAO.search(q, 0);
						if(retObj != null) {
							v.setVehicleBehaviorData((Vector<VehicleBehaviour>)retObj);
							v.setVehicleBehaviorSummary(new Vector<VehicleBehaviorSummary>());
							for(VehicleBehaviour vb : v.getVehicleBehaviorData()) {
								boolean exists = false;
								for(VehicleBehaviorSummary tes2 : v.getVehicleBehaviorSummary()) {
									if(tes2.getWarningName().equals(vb.getWarning().getName())) {
										tes2.getVehicleWarnings().add(vb);
										exists = true;
										break;
									}
								}
								if(!exists) {
									VehicleBehaviorSummary tes = new VehicleBehaviorSummary();
									tes.setWarningName(vb.getWarning().getName());
									tes.getVehicleWarnings().add(vb);
									v.getVehicleBehaviorSummary().add(tes);
								}
							}
						}
					}
				}
				gDAO.destroy();
			}
		}
		return vehiclesDrivingInfoReport;
	}

	public void setVehiclesDrivingInfoReport(
			Vector<Vehicle> vehiclesDrivingInfoReport) {
		this.vehiclesDrivingInfoReport = vehiclesDrivingInfoReport;
	}

	public void resetVehiclesCheckInOut() {
		setVehiclesCheckInOut(null);
	}
	@SuppressWarnings("unchecked")
	public Vector<VehicleCheckInOut> getVehiclesCheckInOut() {
		if(vehiclesCheckInOut == null || vehiclesCheckInOut.size() == 0) {
			if(getPartner() != null) {
				vehiclesCheckInOut = new Vector<VehicleCheckInOut>();
				GeneralDAO gDAO = new GeneralDAO();
				
				String qry = "Select e from CorporateTrip e where e.partner=:partner and (e.departureDateTime between :stdt and :eddt)";
				if(getFleet_id() != null)
					qry += " and e.vehicle.fleet.id=:fleet_id";
				if(getVehicle_id() != null)
					qry += " and e.vehicle.id=:vehicle_id";
				
				Query q = gDAO.createQuery(qry);
				q.setParameter("partner", getPartner());
				q.setParameter("stdt", getStart_dt());
				q.setParameter("eddt", getEnd_dt());
				
				if(getFleet_id() != null)
					q.setParameter("fleet_id", getFleet_id());
				if(getVehicle_id() != null)
					q.setParameter("vehicle_id", getVehicle_id());
				
				Object obj = gDAO.search(q, 0);
				if(obj != null) {
					Vector<CorporateTrip> list = (Vector<CorporateTrip>)obj;
					for(CorporateTrip rec : list) {
						VehicleCheckInOut e = new VehicleCheckInOut();
						e.setVehicleRegNumber(rec.getVehicle().getRegistrationNo());
						e.setOperation("TRIP");
						e.setOperationDesc(rec.getArrivalLocation());
						e.setCheckOut(rec.getDepartureDateTime());
						e.setCheckIn(rec.getCompleteRequestDateTime());
						vehiclesCheckInOut.add(e);
					}
				}
				
				qry = "Select e from VehicleAdHocMaintenance e where e.vehicle.partner=:partner and (e.start_dt between :stdt and :eddt)";
				if(getFleet_id() != null)
					qry += " and e.vehicle.fleet.id=:fleet_id";
				if(getVehicle_id() != null)
					qry += " and e.vehicle.id=:vehicle_id";
				
				q = gDAO.createQuery(qry);
				q.setParameter("partner", getPartner());
				q.setParameter("stdt", getStart_dt());
				q.setParameter("eddt", getEnd_dt());
				
				if(getFleet_id() != null)
					q.setParameter("fleet_id", getFleet_id());
				if(getVehicle_id() != null)
					q.setParameter("vehicle_id", getVehicle_id());
				
				obj = gDAO.search(q, 0);
				if(obj != null) {
					Vector<VehicleAdHocMaintenance> list = (Vector<VehicleAdHocMaintenance>)obj;
					for(VehicleAdHocMaintenance rec : list) {
						VehicleCheckInOut e = new VehicleCheckInOut();
						e.setVehicleRegNumber(rec.getVehicle().getRegistrationNo());
						e.setOperation("ADHOC-MAINT");
						e.setOperationDesc(rec.getDescription());
						e.setCheckOut(rec.getStart_dt());
						e.setCheckIn(rec.getClose_dt());
						vehiclesCheckInOut.add(e);
					}
				}
				
				qry = "Select e from VehicleRoutineMaintenance e where e.vehicle.partner=:partner and (e.start_dt between :stdt and :eddt)";
				if(getFleet_id() != null)
					qry += " and e.vehicle.fleet.id=:fleet_id";
				if(getVehicle_id() != null)
					qry += " and e.vehicle.id=:vehicle_id";
				
				q = gDAO.createQuery(qry);
				q.setParameter("partner", getPartner());
				q.setParameter("stdt", getStart_dt());
				q.setParameter("eddt", getEnd_dt());
				
				if(getFleet_id() != null)
					q.setParameter("fleet_id", getFleet_id());
				if(getVehicle_id() != null)
					q.setParameter("vehicle_id", getVehicle_id());
				
				obj = gDAO.search(q, 0);
				if(obj != null) {
					Vector<VehicleRoutineMaintenance> list = (Vector<VehicleRoutineMaintenance>)obj;
					for(VehicleRoutineMaintenance rec : list) {
						VehicleCheckInOut e = new VehicleCheckInOut();
						e.setVehicleRegNumber(rec.getVehicle().getRegistrationNo());
						e.setOperation("ROUTINE-MAINT");
						e.setOperationDesc(rec.getDescription());
						e.setCheckOut(rec.getStart_dt());
						e.setCheckIn(rec.getClose_dt());
						vehiclesCheckInOut.add(e);
					}
				}
				
				gDAO.destroy();
			}
		}
		return vehiclesCheckInOut;
	}

	public void setVehiclesCheckInOut(Vector<VehicleCheckInOut> vehiclesCheckInOut) {
		this.vehiclesCheckInOut = vehiclesCheckInOut;
	}

	public Vector<PartnerDriverQuery> getDriverQueries() {
		
		return driverQueries;
	}

	public void setDriverQueries(Vector<PartnerDriverQuery> driverQueries) {
		this.driverQueries = driverQueries;
	}

	public Vector<String[]> getMaintCostReport() {
		return maintCostReport;
	}

	public void setMaintCostReport(Vector<String[]> maintCostReport) {
		this.maintCostReport = maintCostReport;
	}

	public Vector<String[]> getMaintCostByPartsReport() {
		return maintCostByPartsReport;
	}

	public void setMaintCostByPartsReport(Vector<String[]> maintCostByPartsReport) {
		this.maintCostByPartsReport = maintCostByPartsReport;
	}

	public Vector<String[]> getMaintCostByBrandsReport() {
		return maintCostByBrandsReport;
	}

	public void setMaintCostByBrandsReport(Vector<String[]> maintCostByBrandsReport) {
		this.maintCostByBrandsReport = maintCostByBrandsReport;
	}

	public Vector<String[]> getPartsReplacementReport() {
		return partsReplacementReport;
	}

	public void setPartsReplacementReport(Vector<String[]> partsReplacementReport) {
		this.partsReplacementReport = partsReplacementReport;
	}

	public Vector<String[]> getPartsServicingReport() {
		return partsServicingReport;
	}

	public void setPartsServicingReport(Vector<String[]> partsServicingReport) {
		this.partsServicingReport = partsServicingReport;
	}

	public Vector<String[]> getExpenseSummaryReport() {
		return expenseSummaryReport;
	}

	public void setExpenseSummaryReport(Vector<String[]> expenseSummaryReport) {
		this.expenseSummaryReport = expenseSummaryReport;
	}

	public Vector<String[]> getAccidentsSummaryReport() {
		return accidentsSummaryReport;
	}

	public void setAccidentsSummaryReport(Vector<String[]> accidentsSummaryReport) {
		this.accidentsSummaryReport = accidentsSummaryReport;
	}

	public VehicleUtilizationSummary getUtilizationSummary() {
		return utilizationSummary;
	}

	public void setUtilizationSummary(VehicleUtilizationSummary utilizationSummary) {
		this.utilizationSummary = utilizationSummary;
	}

	public CorporateTripsSummary getCorporateTripsSummary() {
		return corporateTripsSummary;
	}

	public void setCorporateTripsSummary(CorporateTripsSummary corporateTripsSummary) {
		this.corporateTripsSummary = corporateTripsSummary;
	}

	public VehicleCostEfficientSummary getVehicleCostEfficientSummary() {
		return vehicleCostEfficientSummary;
	}

	public void setVehicleCostEfficientSummary(
			VehicleCostEfficientSummary vehicleCostEfficientSummary) {
		this.vehicleCostEfficientSummary = vehicleCostEfficientSummary;
	}

	public Vector<CorporateTrip> getCorTrips() {
		return corTrips;
	}

	public void setCorTrips(Vector<CorporateTrip> corTrips) {
		this.corTrips = corTrips;
	}

	public Vector<Expense> getExpenses() {
		return expenses;
	}

	public void setExpenses(Vector<Expense> expenses) {
		this.expenses = expenses;
	}

	public Vector<VehicleFueling> getFuelings() {
		return fuelings;
	}

	public void setFuelings(Vector<VehicleFueling> fuelings) {
		this.fuelings = fuelings;
	}

	public String getReport_title() {
		return report_title;
	}

	public void setReport_title(String report_title) {
		this.report_title = report_title;
	}

	public String getReport_start_dt() {
		return report_start_dt;
	}

	public void setReport_start_dt(String report_start_dt) {
		this.report_start_dt = report_start_dt;
	}

	public String getReport_end_dt() {
		return report_end_dt;
	}

	public void setReport_end_dt(String report_end_dt) {
		this.report_end_dt = report_end_dt;
	}

	public String getReport_page() {
		return report_page;
	}

	public void setReport_page(String report_page) {
		this.report_page = report_page;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}
	
	private void createPieModel() {
		pieModel = new PieChartModel();
	}

	public PieChartModel getPieModel() {
		return pieModel;
	}

	public void setPieModel(PieChartModel pieModel) {
		this.pieModel = pieModel;
	}

	public long getMaxY() {
		return maxY;
	}

	public void setMaxY(long maxY) {
		this.maxY = maxY;
	}

	private void createBarModel() {
		if(barModel == null)
			barModel = new CartesianChartModel();
	}
	
	public CartesianChartModel getBarModel() {
		return barModel;
	}

	public void setBarModel(CartesianChartModel barModel) {
		this.barModel = barModel;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}

	public FleetMBean getFleetBean() {
		return fleetBean;
	}

	public void setFleetBean(FleetMBean fleetBean) {
		this.fleetBean = fleetBean;
	}
	
}
