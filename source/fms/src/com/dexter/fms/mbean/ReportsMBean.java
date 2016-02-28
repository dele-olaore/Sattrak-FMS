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
import org.primefaces.model.chart.PieChartModel;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.dto.CorporateTripsSummary;
import com.dexter.fms.dto.FuelConsumption;
import com.dexter.fms.dto.SpeedSummary;
import com.dexter.fms.dto.VehicleBehaviorSummary;
import com.dexter.fms.dto.VehicleCheckInOut;
import com.dexter.fms.dto.VehicleCostEfficientSummary;
import com.dexter.fms.dto.VehicleUtilizationSummary;
import com.dexter.fms.jasper.datasources.AssetEfficiencyReportDS;
import com.dexter.fms.jasper.datasources.FleetCostDS;
import com.dexter.fms.jasper.datasources.FuelConsumptionDS;
import com.dexter.fms.jasper.datasources.MaintCostDataSource;
import com.dexter.fms.jasper.datasources.MaintCostDataSource.MaintCost;
import com.dexter.fms.jasper.datasources.MaintenanceCostDS;
import com.dexter.fms.jasper.datasources.MilleageDS;
import com.dexter.fms.jasper.datasources.SalesEfficiencyReportDS;
import com.dexter.fms.jasper.datasources.UtilizationDataSource;
import com.dexter.fms.jasper.datasources.UtilizationDataSource.Utilization;
import com.dexter.fms.jasper.datasources.UtilizationReportDS;
import com.dexter.fms.jasper.datasources.VehicleCostSummary;
import com.dexter.fms.jasper.datasources.VehicleCostSummary.Cost;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerDriver;
import com.dexter.fms.model.PartnerDriverQuery;
import com.dexter.fms.model.PartnerPersonel;
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
import com.dexter.fms.model.app.VehicleFuelData;
import com.dexter.fms.model.app.VehicleFueling;
import com.dexter.fms.model.app.VehicleLicense;
import com.dexter.fms.model.app.VehicleOdometerData;
import com.dexter.fms.model.app.VehicleParameters;
import com.dexter.fms.model.app.VehicleRoutineMaintenance;
import com.dexter.fms.model.app.VehicleSales;
import com.dexter.fms.model.app.VehicleSpeedData;
import com.dexter.fms.model.app.VehicleStatusEnum;
import com.dexter.fms.model.app.VehicleTrackerData;
import com.dexter.fms.model.app.VehicleTrackerEventData;
import com.dexter.fms.model.app.WorkOrderItem;
import com.dexter.fms.model.app.WorkOrderVehicle;
import com.dexter.fms.model.app.WorkOrderVendor;
import com.dexter.fms.model.ref.Department;
import com.dexter.fms.model.ref.Division;
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

@ManagedBean(name = "reportsBean")
@SessionScoped
public class ReportsMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-UserMBean");
	
	private FacesMessage msg = null;
	
	private Long partner_id;
	private Partner partner;
	
	private int yos;
	private long role_id;
	private long region_id, division_id;
	private long department_id, unit_id, staff_id;
	private Vector<Department> depts;
	private Vector<Region> regions;
	
	private long engineCapacity_id, brand_id, yearOfPurchase;
	
	private Date start_dt, end_dt;
	private int rgroup;
	private Long fleet_id;
	private Vector<Fleet> fleets;
	private Long vehicle_id;
	private String regNo, stype, reportType, mainttype;
	
	private Long driver_id;
	private String accidentStatus, action_taken;
	private Long vehicleModel_id;
	private int minYears;
	private int searchType;
	
	private double flimit;
	
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
	private Vector<FuelConsumption> fleetCost;
	
	private Vector<Vehicle> vehiclesDrivingInfoReport;
	
	private Vector<VehicleCheckInOut> vehiclesCheckInOut;
	
	private Vector<PartnerDriverQuery> driverQueries;
	
	private Vector<String[]> maintCostReport, maintCostByPartsReport, maintCostByBrandsReport, partsReplacementReport, partsServicingReport;
	private Vector<String[]> expenseSummaryReport, accidentsSummaryReport;
	
	private Vector<DriverLicense> driverLicenseRenewal;
	
	private VehicleUtilizationSummary utilizationSummary;
	private CorporateTripsSummary corporateTripsSummary;
	private VehicleCostEfficientSummary vehicleCostEfficientSummary;
	private SpeedSummary speedSummary;
	
	private UtilizationReportDS utilazationReport;
	private AssetEfficiencyReportDS assetEfficiencyReport;
	private SalesEfficiencyReportDS salesEfficiencyReport;
	
	private String report_title;
	private String report_start_dt;
	private String report_end_dt;
	private String report_page = "/faces/reports_home.xhtml";
	
	private String period, groupBy, filterType="byfleet";
	
	private PieChartModel pieModel;
	
	private long maxY;
	private CartesianChartModel barModel;
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	@ManagedProperty("#{fleetBean}")
	FleetMBean fleetBean;
	
	public ReportsMBean() {}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	private Vector<VehicleParameters> filterVehicles(GeneralDAO gDAO, boolean engineCap, boolean brand, boolean yop) {
		Vector<VehicleParameters> vpList = null;
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("vehicle.partner.id", getPartner().getId());
		if(region_id > 0)
			params.put("region.id", region_id);
		if(division_id > 0)
			params.put("dept.division.id", division_id);
		if(department_id > 0)
			params.put("dept.id", department_id);
		if(unit_id > 0)
			params.put("unit.id", unit_id);
		if(fleet_id > 0)
			params.put("vehicle.fleet.id", fleet_id);
		if(engineCapacity_id > 0 && engineCap)
			params.put("vehicle.engineCapacity.id", engineCapacity_id);
		if(brand_id > 0 && brand)
			params.put("vehicle.model.id", brand_id);
		Object vpObj = gDAO.search("VehicleParameters", params);
		if(vpObj != null) {
			vpList = (Vector<VehicleParameters>)vpObj;
			if(yearOfPurchase > 0 && yop) {
				Vector<VehicleParameters> newList = new Vector<VehicleParameters>();
				for(VehicleParameters vp : vpList) {
					Date dop = vp.getVehicle().getPurchaseDate();
					if(dop != null) {
						int v_year = 1900 + dop.getYear();
						if(yearOfPurchase == v_year) {
							newList.add(vp);
						}
					}
				}
				vpList = newList;
			}
			Vector<VehicleParameters> newList = new Vector<VehicleParameters>();
			for(VehicleParameters vp : vpList) {
				boolean exists = false;
				for(int i=0; i<newList.size(); i++) {
					VehicleParameters e = newList.get(i);
					if(e.getVehicle().getRegistrationNo().equalsIgnoreCase(vp.getVehicle().getRegistrationNo())) {
						newList.set(i, e);
						exists = true;
						break;
					}
				}
				if(!exists)
					newList.add(vp);
			}
			vpList = newList;
		}
		return vpList;
	}
	
	@SuppressWarnings("static-access")
	public void downloadSalesEfficiencyPDF() {
		try {
			SalesEfficiencyReportDS objDS = salesEfficiencyReport;
			double totalEngineHours = 0, totalCosts = 0;
			double totalDistanceCovered = 0, totalFuelConsumed = 0, totalRevenue = 0, totalCostPerHour = 0, totalCostPerKm = 0;
			double avgCostPerHour = 0, avgCostPerKm = 0, allCostPerHour = 0, allCostPerKm = 0;
			int noOfTrips = 0;
			for(SalesEfficiencyReportDS.Entry entry : objDS.getCollectionList()) {
				totalEngineHours += entry.getEngineHours();
				totalDistanceCovered+=entry.getDistanceCovered();
				totalFuelConsumed += entry.getFuelConsumed();
				totalRevenue += entry.getRevenue();
				noOfTrips += entry.getTripsCount();
				totalCosts += entry.getCost();
				allCostPerHour+= entry.getCostPerHour();
				allCostPerKm += entry.getCostPerKm();
			}
			try {
				totalCostPerHour = new BigDecimal(totalCosts).divide(new BigDecimal(totalEngineHours), 2, RoundingMode.HALF_UP).doubleValue();
			} catch(Exception ex){}
			try {
				totalCostPerKm = new BigDecimal(totalCosts).divide(new BigDecimal(totalDistanceCovered), 2, RoundingMode.HALF_UP).doubleValue();
			} catch(Exception ex){}
			try {
				avgCostPerHour = new BigDecimal(allCostPerHour).divide(new BigDecimal(objDS.getCollectionList().size()), 2, RoundingMode.HALF_UP).doubleValue();
			} catch(Exception ex){}
			try {
				avgCostPerKm = new BigDecimal(allCostPerKm).divide(new BigDecimal(objDS.getCollectionList().size()), 2, RoundingMode.HALF_UP).doubleValue();
			} catch(Exception ex){}
			GeneralDAO gDAO = new GeneralDAO();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
			Map<String, Object> parameters = new HashMap<String, Object>();
			if(filterType.equals("byregion")) {
				parameters.put("reportTitle", "Sales Efficiency Report");
				setGroupBy("Region");
			}
			else if(filterType.equals("byfleet")) {
				parameters.put("reportTitle", "Sales Efficiency Report");
				setGroupBy("Individual Vehicles");
			}
			else if(filterType.equals("byenginecap")) {
				parameters.put("reportTitle", "Sales Efficiency Report");
				setGroupBy("Engine Capacity");
			}
			else if(filterType.equals("bybrand")) {
				parameters.put("reportTitle", "Sales Efficiency Report");
				setGroupBy("Vehicle Brand");
			}
			parameters.put("reportDesc", "This is a summary report of sales efficiency.");
			parameters.put("preparedOn", sdf.format(new Date()));
			parameters.put("groupBy", getGroupBy());
			parameters.put("period", getPeriod());
			if(getRegion_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Region.class, getRegion_id());
				if(obj != null) {
					Region r = (Region)obj;
					parameters.put("region", r.getName());
				} else
					parameters.put("region", "N/A");
			} else
				parameters.put("region", "All");
			if(getDivision_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Division.class, getDivision_id());
				if(obj != null) {
					Division r = (Division)obj;
					parameters.put("branch", r.getName());
				} else
					parameters.put("branch", "N/A");
			} else
				parameters.put("branch", "All");
			if(getDepartment_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Department.class, getDepartment_id());
				if(obj != null) {
					Department r = (Department)obj;
					parameters.put("department", r.getName());
				} else
					parameters.put("department", "N/A");
			} else
				parameters.put("department", "All");
			
			gDAO.destroy();
			
			parameters.put("totalsLabel1", "Fuel consumed");
			parameters.put("totalsValue1", ""+totalFuelConsumed);
			parameters.put("totalsLabel2", "Distance");
			parameters.put("totalsValue2", ""+totalDistanceCovered);
			parameters.put("totalsLabel3", "Cost/Hr");
			parameters.put("totalsValue3", ""+totalCostPerHour);
			parameters.put("totalsLabel4", "Cost/Km");
			parameters.put("totalsValue4", ""+totalCostPerKm);
			parameters.put("totalsLabel5", "No of Trips");
			parameters.put("totalsValue5", ""+noOfTrips);
			parameters.put("totalsLabel6", "Revenue Achieved");
			parameters.put("totalsValue6", ""+totalRevenue);
			parameters.put("totalsLabel7", "Cost Incurred");
			parameters.put("totalsValue7", ""+totalCosts);
			
			parameters.put("summaryLabel1", "No. of Assets");
			parameters.put("summaryValue1", ""+objDS.getNoOfAssets());
			
			parameters.put("summaryLabel2", "Average Cost/Km");
			parameters.put("summaryValue2", ""+avgCostPerKm);
			
			parameters.put("summaryLabel3", "Average Cost/Hr");
			parameters.put("summaryValue3", ""+avgCostPerHour);
			
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(objDS.getCollectionList());
			
			downloadJasperPDF(parameters, "vehicle_utilization_report.pdf", "/resources/jasper/sales_efficiency_report.jasper", ds);
		} catch(Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation", "static-access" })
	public void searchSalesEfficiency() {
		salesEfficiencyReport = null;
		if(getStart_dt() != null && getEnd_dt() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner.id", getPartner().getId());
			if(division_id > 0)
				params.put("department.division.id", division_id);
			if(department_id > 0)
				params.put("department.id", department_id);
			if(unit_id > 0)
				params.put("unit.id", unit_id);
			if(staff_id > 0)
				params.put("id", staff_id);
			Vector<PartnerPersonel> vpList = null;
			Object vpObj = gDAO.search("PartnerPersonel", params);
			if(vpObj != null) {
				vpList = (Vector<PartnerPersonel>)vpObj;
			}
			if(vpList != null && vpList.size() > 0) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
					setPeriod(sdf.format(getStart_dt()) + " - " + sdf.format(getEnd_dt()));
				} catch(Exception ex){ ex.printStackTrace(); }
				salesEfficiencyReport = new SalesEfficiencyReportDS();
				Vector<SalesEfficiencyReportDS.Entry> groupList = new Vector<SalesEfficiencyReportDS.Entry>();
				double daysCount = 0;
				Calendar start_can = Calendar.getInstance(), end_can = Calendar.getInstance();
				while(start_can.before(end_can)) {
					int dow = start_can.get(Calendar.DAY_OF_WEEK);
					if(dow!=Calendar.SATURDAY &&
							dow!=Calendar.SUNDAY)
						daysCount += 1;
					start_can.add(Calendar.DATE, 1);
				}
				int noOfVehicles = vpList.size();
				salesEfficiencyReport.setNoOfAssets(noOfVehicles);
				for(PartnerPersonel vp : vpList) {
					int tripsCount = 0;
					double actualEngineHours = 0, distanceCovered = 0;
					double myfuel_consumption = 0, totalCost = 0, totalRevenue = 0;
					Query q = gDAO.createQuery("Select e from VehicleSales e where e.staff.id = :s_id and (e.salesDate between :st_dt and :ed_dt) order by e.salesDate");
					q.setParameter("s_id", vp.getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					Object listObj = gDAO.search(q, 0);
					if(listObj != null) {
						Vector<VehicleSales> list = (Vector<VehicleSales>)listObj;
						for(VehicleSales e : list) {
							totalRevenue += e.getSales();
						}
					}
					
					q = gDAO.createQuery("Select e from CorporateTrip e where e.staff.id=:s_id and (e.departureDateTime between :st_dt and :ed_dt) order by e.departureDateTime");
					q.setParameter("s_id", vp.getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					listObj = gDAO.search(q, 0);
					if(listObj != null) {
						Vector<CorporateTrip> list = (Vector<CorporateTrip>)listObj;
						for(CorporateTrip ct : list) {
							if(ct.getVehicle() == null)
								continue;
							q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
							q.setParameter("v_id", ct.getVehicle().getId());
							q.setParameter("st_dt", getStart_dt());
							q.setParameter("ed_dt", getEnd_dt());
							Object obj = gDAO.search(q, 0);
							if(obj != null) {
								double start_odometer = 0, end_odometer = 0, start_enginehours = 0, end_enginehours = 0;
								boolean start = true;
								Vector<VehicleOdometerData> list2 = (Vector<VehicleOdometerData>)obj;
								for(VehicleOdometerData e : list2) {
									end_odometer = e.getOdometer();
									end_enginehours = e.getEngineHours();
									if(start) {
										start = false;
										start_odometer = e.getOdometer();
										start_enginehours = e.getEngineHours();
									}
								}
								double mymileage_consumption = Math.abs(end_odometer-start_odometer);
								double myenginehours = Math.abs(end_enginehours-start_enginehours);
								distanceCovered += mymileage_consumption;
								actualEngineHours += myenginehours;
							}
							q = gDAO.createQuery("Select e from VehicleFuelData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
							q.setParameter("v_id", ct.getVehicle().getId());
							q.setParameter("st_dt", getStart_dt());
							q.setParameter("ed_dt", getEnd_dt());
							obj = gDAO.search(q, 0);
							if(obj != null) {
								double last_fuel_level = 0;
								Vector<VehicleFuelData> list2 = (Vector<VehicleFuelData>)obj;
								for(VehicleFuelData e : list2) {
									if(last_fuel_level > e.getFuelLevel()) {
										myfuel_consumption += (last_fuel_level - e.getFuelLevel());
									}
									last_fuel_level = e.getFuelLevel();
								}
							}
							q = gDAO.createQuery("Select e from Expense e where (e.expense_dt between :start_dt and :end_dt) and e.vehicle.id=:v_id");
							q.setParameter("v_id", ct.getVehicle().getId());
							q.setParameter("start_dt", getStart_dt());
							q.setParameter("end_dt", getEnd_dt());
							obj = gDAO.search(q, 0);
							if(obj != null) {
								Vector<Expense> objList = (Vector<Expense>)obj;
								for(Expense vrm : objList) {
									if(vrm.getAmount()>0) {
										totalCost += vrm.getAmount();
									}
								}
							}
						}
					}
					
					String key = null;
					key = vp.getFirstname() + " " + vp.getLastname();
					
					if(key != null) {
						SalesEfficiencyReportDS.Entry entry = salesEfficiencyReport.new Entry();
						entry.setVehicleReg(key);
						entry.setCost(new BigDecimal(totalCost).setScale(2, RoundingMode.HALF_UP).doubleValue());// new BigDecimal(maintCost).setScale(2, RoundingMode.HALF_UP).doubleValue()
						entry.setDistanceCovered(new BigDecimal(distanceCovered).setScale(2, RoundingMode.HALF_UP).doubleValue());
						entry.setFuelConsumed(new BigDecimal(myfuel_consumption).setScale(2, RoundingMode.HALF_UP).doubleValue());
						entry.setTripsCount(tripsCount);
						entry.setRevenue(new BigDecimal(totalRevenue).setScale(2, RoundingMode.HALF_UP).doubleValue());
						entry.setEngineHours(new BigDecimal(actualEngineHours).setScale(2, RoundingMode.HALF_UP).doubleValue());
						if(entry.getDistanceCovered() > 0) {
							try {
								double costPerHour = new BigDecimal(totalCost).divide(new BigDecimal(actualEngineHours), 2, RoundingMode.HALF_UP).doubleValue();
								entry.setCostPerHour(costPerHour);
							} catch(Exception ex){}
							try {
								double costPerKm = new BigDecimal(totalCost).divide(new BigDecimal(entry.getDistanceCovered()), 2, RoundingMode.HALF_UP).doubleValue();
								entry.setCostPerKm(costPerKm);
							} catch(Exception ex){}
						}
						groupList.add(entry);
					}
				}
				salesEfficiencyReport.setData(groupList);
			}
			gDAO.destroy();
			
			if(getSalesEfficiencyReport() != null && getSalesEfficiencyReport().getCollectionList().size() > 0) {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getSalesEfficiencyReport().getCollectionList().size() + " record(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "No record found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} else {
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Please supply date range!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("static-access")
	public void downloadAssetEfficiencyReportPDF() {
		try {
			AssetEfficiencyReportDS objDS = assetEfficiencyReport;
			double totalStandardWorktime = 0, totalActualWorktime = 0, totalCosts = 0;
			double totalDistanceCovered = 0, totalVehicleKmPerLiter = 0, totalVehicleMaintCostPerKm = 0, totalVehicleCostPerKm = 0;
			int noOfTrips = 0;
			double avgBrandKmPerLiter = 0, brandMaintCostPerKm = 0, avgCostPerKm = 0, totalPercentAvailability = 0, avgPercentAvailability = 0;
			for(AssetEfficiencyReportDS.Entry entry : objDS.getCollectionList()) {
				totalStandardWorktime += entry.getStandardWorktime();
				totalActualWorktime += entry.getActualWorkingTime();
				totalDistanceCovered+=entry.getDistanceCovered();
				totalVehicleKmPerLiter += entry.getVehicleKmPerLiter();
				totalVehicleMaintCostPerKm += entry.getVehicleMaintCostPerKm();
				totalVehicleCostPerKm += entry.getVehicleCostPerKm();
				noOfTrips += entry.getTripsCount();
				totalCosts += entry.getTotalCost();
				totalPercentAvailability += entry.getPercentAvailability();
			}
			try {
				avgBrandKmPerLiter = new BigDecimal(totalVehicleKmPerLiter).divide(new BigDecimal(objDS.getCollectionList().size()), 2, RoundingMode.HALF_UP).doubleValue();
			} catch(Exception ex){}
			try {
				brandMaintCostPerKm = new BigDecimal(totalVehicleMaintCostPerKm).divide(new BigDecimal(objDS.getCollectionList().size()), 2, RoundingMode.HALF_UP).doubleValue();
			} catch(Exception ex){}
			try {
				avgCostPerKm = new BigDecimal(totalVehicleCostPerKm).divide(new BigDecimal(objDS.getCollectionList().size()), 2, RoundingMode.HALF_UP).doubleValue();
			} catch(Exception ex){}
			try {
				avgPercentAvailability = new BigDecimal(totalPercentAvailability).divide(new BigDecimal(objDS.getCollectionList().size()), 2, RoundingMode.HALF_UP).doubleValue();
			} catch(Exception ex){}
			for(AssetEfficiencyReportDS.Entry entry : objDS.getCollectionList()) {
				entry.setAvgBrandKmPerLiter(avgBrandKmPerLiter);
				entry.setAvgCostPerKm(avgCostPerKm);
				entry.setBrandMaintCostPerKm(brandMaintCostPerKm);
			}
			
			GeneralDAO gDAO = new GeneralDAO();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
			Map<String, Object> parameters = new HashMap<String, Object>();
			if(filterType.equals("byregion")) {
				parameters.put("reportTitle", "Assets Efficiency Report");
				setGroupBy("Region");
			}
			else if(filterType.equals("byfleet")) {
				parameters.put("reportTitle", "Assets Efficiency Report");
				setGroupBy("Individual Vehicles");
			}
			else if(filterType.equals("byenginecap")) {
				parameters.put("reportTitle", "Assets Efficiency Report");
				setGroupBy("Engine Capacity");
			}
			else if(filterType.equals("bybrand")) {
				parameters.put("reportTitle", "Assets Efficiency Report");
				setGroupBy("Vehicle Brand");
			}
			parameters.put("reportDesc", "This is a summary report of vehicles efficiency.");
			parameters.put("preparedOn", sdf.format(new Date()));
			parameters.put("groupBy", getGroupBy());
			parameters.put("period", getPeriod());
			if(getRegion_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Region.class, getRegion_id());
				if(obj != null) {
					Region r = (Region)obj;
					parameters.put("region", r.getName());
				} else
					parameters.put("region", "N/A");
			} else
				parameters.put("region", "All");
			if(getDivision_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Division.class, getDivision_id());
				if(obj != null) {
					Division r = (Division)obj;
					parameters.put("branch", r.getName());
				} else
					parameters.put("branch", "N/A");
			} else
				parameters.put("branch", "All");
			if(getDepartment_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Department.class, getDepartment_id());
				if(obj != null) {
					Department r = (Department)obj;
					parameters.put("department", r.getName());
				} else
					parameters.put("department", "N/A");
			} else
				parameters.put("department", "All");
			
			gDAO.destroy();
			
			parameters.put("totalsLabel1", "Working time");
			parameters.put("totalsValue1", ""+totalStandardWorktime);
			parameters.put("totalsLabel2", "Distance");
			parameters.put("totalsValue2", ""+totalDistanceCovered);
			parameters.put("totalsLabel3", "Driving Time");
			parameters.put("totalsValue3", ""+totalActualWorktime);
			parameters.put("totalsLabel4", "Cost");
			parameters.put("totalsValue4", ""+totalCosts);
			parameters.put("totalsLabel5", "No of Trips");
			parameters.put("totalsValue5", ""+noOfTrips);
			parameters.put("totalsLabel6", "");
			parameters.put("totalsValue6", "");
			parameters.put("totalsLabel7", "");
			parameters.put("totalsValue7", "");
			
			parameters.put("summaryLabel1", "No. of Assets");
			parameters.put("summaryValue1", ""+objDS.getNoOfAssets());
			
			parameters.put("summaryLabel2", "% Fleet Availability");
			parameters.put("summaryValue2", ""+avgPercentAvailability);
			
			parameters.put("summaryLabel3", "");
			parameters.put("summaryValue3", "");
			
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(objDS.getCollectionList());
			
			downloadJasperPDF(parameters, "vehicle_utilization_report.pdf", "/resources/jasper/assets_efficiency_report.jasper", ds);
		} catch(Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation", "static-access" })
	public void searchAssetEfficiencyReport() {
		assetEfficiencyReport = null;
		if(getStart_dt() != null && getEnd_dt() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("vehicle.partner.id", getPartner().getId());
			if(region_id > 0 && filterType.equals("byregion"))
				params.put("region.id", region_id);
			if(division_id > 0 && !filterType.equals("byfleet"))
				params.put("dept.division.id", division_id);
			if(department_id > 0 && !filterType.equals("byfleet"))
				params.put("dept.id", department_id);
			if(unit_id > 0 && !filterType.equals("byfleet"))
				params.put("unit.id", unit_id);
			if(fleet_id > 0)
				params.put("vehicle.fleet.id", fleet_id);
			if(engineCapacity_id > 0 && filterType.equals("byenginecap"))
				params.put("vehicle.engineCapacity.id", engineCapacity_id);
			if(brand_id > 0 && filterType.equals("bybrand"))
				params.put("vehicle.model.id", brand_id);
			Vector<VehicleParameters> vpList = null;
			Object vpObj = gDAO.search("VehicleParameters", params);
			if(vpObj != null) {
				vpList = (Vector<VehicleParameters>)vpObj;
				if(yearOfPurchase > 0 && filterType.equals("byyearofp")) {
					Vector<VehicleParameters> newList = new Vector<VehicleParameters>();
					for(VehicleParameters vp : vpList) {
						Date dop = vp.getVehicle().getPurchaseDate();
						if(dop != null) {
							int v_year = 1900 + dop.getYear();
							if(yearOfPurchase == v_year) {
								newList.add(vp);
							}
						}
					}
					vpList = newList;
				}
				Vector<VehicleParameters> newList = new Vector<VehicleParameters>();
				for(VehicleParameters vp : vpList) {
					boolean exists = false;
					for(int i=0; i<newList.size(); i++) {
						VehicleParameters e = newList.get(i);
						if(e.getVehicle().getRegistrationNo().equalsIgnoreCase(vp.getVehicle().getRegistrationNo())) {
							newList.set(i, e);
							exists = true;
							break;
						}
					}
					if(!exists)
						newList.add(vp);
				}
				vpList = newList;
			}
			if(vpList != null && vpList.size() > 0) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
					setPeriod(sdf.format(getStart_dt()) + " - " + sdf.format(getEnd_dt()));
				} catch(Exception ex){ ex.printStackTrace(); }
				assetEfficiencyReport = new AssetEfficiencyReportDS();
				Vector<AssetEfficiencyReportDS.Entry> groupList = new Vector<AssetEfficiencyReportDS.Entry>();
				double daysCount = 0;
				Calendar start_can = Calendar.getInstance(), end_can = Calendar.getInstance();
				start_can.setTime(getStart_dt());
				end_can.setTime(getEnd_dt());
				while(start_can.before(end_can)) {
					int dow = start_can.get(Calendar.DAY_OF_WEEK);
					if(dow!=Calendar.SATURDAY &&
							dow!=Calendar.SUNDAY)
						daysCount += 1;
					start_can.add(Calendar.DATE, 1);
				}
				double standardWorktime = 12*daysCount, standardEngineHours = 12*daysCount;
				int noOfVehicles = vpList.size();
				assetEfficiencyReport.setNoOfAssets(noOfVehicles);
				for(VehicleParameters vp : vpList) {
					int tripsCount = 0, ageOfVehicle = 0;
					double actualWorkingTime = 0, actualEngineHours = 0, drivingTime = 0, distanceCovered = 0, costPerKm = 0;
					double myfuel_consumption = 0, maintCost = 0, totalCost = 0;
					Date dop = vp.getVehicle().getPurchaseDate();
					if(dop != null){
						Calendar c = Calendar.getInstance();
						int nowyear = c.get(Calendar.YEAR);
						c.setTime(dop);
						int dopyear = c.get(Calendar.YEAR);
						ageOfVehicle = nowyear-dopyear;
						if(ageOfVehicle == 0)
							ageOfVehicle = 1;
					}
					// vehicle % availability = standard worktime/actual worktime
					Query q = gDAO.createQuery("Select e from CorporateTrip e where e.vehicle.id = :vehicle_id and (e.departureDateTime between :st_dt and :ed_dt) and (e.tripStatus = 'ON_TRIP' or e.tripStatus = 'SHOULD_BE_COMPLETED' or e.tripStatus = 'COMPLETION_REQUEST' or e.tripStatus = 'COMPLETED')");
					q.setParameter("vehicle_id", vp.getVehicle().getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					Object obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<CorporateTrip> list = (Vector<CorporateTrip>)obj;
						tripsCount = list.size();
						for(CorporateTrip ct : list) {
							Date start = ct.getDepartureDateTime();
							Date end = (ct.getCompleteRequestDateTime() != null) ? ct.getCompleteRequestDateTime() : ct.getCompletedDateTime();
							if(end == null)
								end = new Date();
							long durationMilli = Math.abs(end.getTime() - start.getTime());
							long hour = 1000*60*60;
							int divide = 0;
							try {
								divide = Integer.parseInt(""+(durationMilli/hour));
							} catch(Exception ex){}
							if(divide <= 0)
								divide = 1;
							actualWorkingTime += divide;
						}
					}
					q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
					q.setParameter("v_id", vp.getVehicle().getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					Object listObj = gDAO.search(q, 0);
					if(listObj != null) {
						double start_odometer = 0, end_odometer = 0, start_enginehours = 0, end_enginehours = 0;
						boolean start = true;
						Vector<VehicleOdometerData> list = (Vector<VehicleOdometerData>)listObj;
						for(VehicleOdometerData e : list) {
							end_odometer = e.getOdometer();
							end_enginehours = e.getEngineHours();
							if(start) {
								start = false;
								start_odometer = e.getOdometer();
								start_enginehours = e.getEngineHours();
							}
						}
						double mymileage_consumption = Math.abs(end_odometer-start_odometer);
						double myenginehours = Math.abs(end_enginehours-start_enginehours);
						distanceCovered += mymileage_consumption;
						actualEngineHours += myenginehours;
					}
					q = gDAO.createQuery("Select e from VehicleFuelData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
					q.setParameter("v_id", vp.getVehicle().getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					listObj = gDAO.search(q, 0);
					if(listObj != null) {
						double last_fuel_level = 0;
						Vector<VehicleFuelData> list = (Vector<VehicleFuelData>)listObj;
						for(VehicleFuelData e : list) {
							if(last_fuel_level > e.getFuelLevel()) {
								myfuel_consumption += (last_fuel_level - e.getFuelLevel());
							}
							last_fuel_level = e.getFuelLevel();
						}
					}
					q = gDAO.createQuery("Select e from Expense e where (e.expense_dt between :start_dt and :end_dt) and e.vehicle.id=:v_id");
					q.setParameter("v_id", vp.getVehicle().getId());
					q.setParameter("start_dt", getStart_dt());
					q.setParameter("end_dt", getEnd_dt());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<Expense> objList = (Vector<Expense>)obj;
						BigDecimal cost = new BigDecimal(0);
						for(Expense vrm : objList) {
							if(vrm.getAmount()>0) {
								totalCost += vrm.getAmount();
								if(vrm.getType().getName().equalsIgnoreCase("Maintenance"))
									maintCost += cost.doubleValue();
							}
						}
					}
					
					String key = null;
					key = vp.getVehicle().getRegistrationNo();
					
					if(key != null) {
						AssetEfficiencyReportDS.Entry entry = assetEfficiencyReport.new Entry();
						entry.setVehicleReg(key);
						entry.setActualWorkingTime(new BigDecimal(actualWorkingTime).setScale(2, RoundingMode.HALF_UP).doubleValue());
						entry.setStandardWorktime(new BigDecimal(standardWorktime).setScale(2, RoundingMode.HALF_UP).doubleValue());
						entry.setAgeOfVehicle(ageOfVehicle);
						entry.setDistanceCovered(new BigDecimal(distanceCovered).setScale(2, RoundingMode.HALF_UP).doubleValue());
						entry.setFuelConsumed(new BigDecimal(myfuel_consumption).setScale(2, RoundingMode.HALF_UP).doubleValue());
						entry.setTripsCount(tripsCount);
						entry.setMaintCost(new BigDecimal(maintCost).setScale(2, RoundingMode.HALF_UP).doubleValue());
						entry.setTotalCost(new BigDecimal(totalCost).setScale(2, RoundingMode.HALF_UP).doubleValue());
						if(entry.getDistanceCovered() > 0) {
							try {
							double vehicleKmPerLiter = new BigDecimal(entry.getDistanceCovered()).divide(new BigDecimal(entry.getFuelConsumed()), 2, RoundingMode.HALF_UP).doubleValue();
							entry.setVehicleKmPerLiter(vehicleKmPerLiter);
							} catch(Exception ex){}
							try {
								double vehicleMaintCostPerKm = new BigDecimal(maintCost).divide(new BigDecimal(entry.getDistanceCovered()), 2, RoundingMode.HALF_UP).doubleValue();
								entry.setVehicleMaintCostPerKm(vehicleMaintCostPerKm);
							} catch(Exception ex){}
							try {
								double vehicleCostPerKm = new BigDecimal(totalCost).divide(new BigDecimal(entry.getDistanceCovered()), 2, RoundingMode.HALF_UP).doubleValue();
								entry.setVehicleCostPerKm(vehicleCostPerKm);
							} catch(Exception ex){}
						}
						if(entry.getActualWorkingTime() > 0) {
							double percentAvailability = new BigDecimal(entry.getStandardWorktime()).divide(new BigDecimal(entry.getActualWorkingTime()), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP).doubleValue();
							entry.setPercentAvailability(percentAvailability);
						}
						groupList.add(entry);
					}
				}
				assetEfficiencyReport.setData(groupList);
			}
			gDAO.destroy();
			
			if(getAssetEfficiencyReport() != null && getAssetEfficiencyReport().getCollectionList().size() > 0) {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getAssetEfficiencyReport().getCollectionList().size() + " record(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "No record found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} else {
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Please supply date range!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("static-access")
	public void downloadUtilizationReportPDF() {
		try {
			UtilizationReportDS utilReportDS = utilazationReport;
			double totalStandardWorktime = 0, totalActualWorktime = 0, totalPercentUtil = 0, avgPercentUtil=0;
			double totalDistanceCovered = 0, totalEngineHours = 0;
			int noOfTrips = 0;
			// vehicle % utilization= standard worktime/actual worktime*100
			for(UtilizationReportDS.Entry entry : utilReportDS.getCollectionList()) {
				totalStandardWorktime += entry.getStandardWorktime();
				totalActualWorktime += entry.getActualWorkingTime();
				totalDistanceCovered+=entry.getDistanceCovered();
				noOfTrips += entry.getTripsCount();
				totalEngineHours+=entry.getActualEngineHours();
			}
			try {
				totalPercentUtil = new BigDecimal(totalStandardWorktime).divide(new BigDecimal(totalActualWorktime), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP).doubleValue();
			} catch(Exception ex){}
			try {
				if(totalPercentUtil > 0)
					avgPercentUtil = new BigDecimal(totalPercentUtil).divide(new BigDecimal(utilReportDS.getCollectionList().size()), 2, RoundingMode.HALF_UP).doubleValue();
			} catch(Exception ex){}
			
			GeneralDAO gDAO = new GeneralDAO();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
			Map<String, Object> parameters = new HashMap<String, Object>();
			if(filterType.equals("byregion")) {
				parameters.put("reportTitle", "Vehicle Utilization Report");
				setGroupBy("Region");
			}
			else if(filterType.equals("byfleet")) {
				parameters.put("reportTitle", "Vehicle Utilization Report");
				setGroupBy("Individual Vehicles");
			}
			else if(filterType.equals("byenginecap")) {
				parameters.put("reportTitle", "Vehicle Utilization Report");
				setGroupBy("Engine Capacity");
			}
			else if(filterType.equals("bybrand")) {
				parameters.put("reportTitle", "Vehicle Utilization Report");
				setGroupBy("Vehicle Brand");
			}
			parameters.put("reportDesc", "This is a summary report of vehicles utilization.");
			parameters.put("preparedOn", sdf.format(new Date()));
			parameters.put("groupBy", getGroupBy());
			parameters.put("period", getPeriod());
			if(getRegion_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Region.class, getRegion_id());
				if(obj != null) {
					Region r = (Region)obj;
					parameters.put("region", r.getName());
				} else
					parameters.put("region", "N/A");
			} else
				parameters.put("region", "All");
			if(getDivision_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Division.class, getDivision_id());
				if(obj != null) {
					Division r = (Division)obj;
					parameters.put("branch", r.getName());
				} else
					parameters.put("branch", "N/A");
			} else
				parameters.put("branch", "All");
			if(getDepartment_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Department.class, getDepartment_id());
				if(obj != null) {
					Department r = (Department)obj;
					parameters.put("department", r.getName());
				} else
					parameters.put("department", "N/A");
			} else
				parameters.put("department", "All");
			
			gDAO.destroy();
			
			parameters.put("totalsLabel1", "Working time");
			parameters.put("totalsValue1", ""+totalActualWorktime);
			parameters.put("totalsLabel2", "Distance");
			parameters.put("totalsValue2", ""+totalDistanceCovered);
			parameters.put("totalsLabel3", "Engine hours");
			parameters.put("totalsValue3", ""+totalEngineHours);
			parameters.put("totalsLabel4", "No of Trips");
			parameters.put("totalsValue4", ""+noOfTrips);
			parameters.put("totalsLabel5", "");
			parameters.put("totalsValue5", "");
			parameters.put("totalsLabel6", "");
			parameters.put("totalsValue6", "");
			parameters.put("totalsLabel7", "");
			parameters.put("totalsValue7", "");
			
			parameters.put("summaryLabel1", "No. of Assets");
			parameters.put("summaryValue1", ""+utilReportDS.getNoOfAssets());
			
			parameters.put("summaryLabel2", "Total % Vehicle Utilization");
			parameters.put("summaryValue2", ""+totalPercentUtil);
			
			parameters.put("summaryLabel3", "Average % Vehicle Utilization");
			parameters.put("summaryValue3", ""+avgPercentUtil);
			
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(utilReportDS.getCollectionList());
			
			downloadJasperPDF(parameters, "vehicle_utilization_report.pdf", "/resources/jasper/vehicle_utilization_report.jasper", ds);
		} catch(Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation", "static-access" })
	public void searchUtilizationReport() {
		utilazationReport = null;
		if(getStart_dt() != null && getEnd_dt() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("vehicle.partner.id", getPartner().getId());
			if(region_id > 0 && filterType.equals("byregion"))
				params.put("region.id", region_id);
			if(division_id > 0 && !filterType.equals("byfleet"))
				params.put("dept.division.id", division_id);
			if(department_id > 0 && !filterType.equals("byfleet"))
				params.put("dept.id", department_id);
			if(unit_id > 0 && !filterType.equals("byfleet"))
				params.put("unit.id", unit_id);
			if(fleet_id > 0)
				params.put("vehicle.fleet.id", fleet_id);
			if(engineCapacity_id > 0 && filterType.equals("byenginecap"))
				params.put("vehicle.engineCapacity.id", engineCapacity_id);
			if(brand_id > 0 && filterType.equals("bybrand"))
				params.put("vehicle.model.id", brand_id);
			Vector<VehicleParameters> vpList = null;
			Object vpObj = gDAO.search("VehicleParameters", params);
			if(vpObj != null) {
				vpList = (Vector<VehicleParameters>)vpObj;
				if(yearOfPurchase > 0 && filterType.equals("byyearofp")) {
					Vector<VehicleParameters> newList = new Vector<VehicleParameters>();
					for(VehicleParameters vp : vpList) {
						Date dop = vp.getVehicle().getPurchaseDate();
						if(dop != null) {
							int v_year = 1900 + dop.getYear();
							if(yearOfPurchase == v_year) {
								newList.add(vp);
							}
						}
					}
					vpList = newList;
				}
				Vector<VehicleParameters> newList = new Vector<VehicleParameters>();
				for(VehicleParameters vp : vpList) {
					boolean exists = false;
					for(int i=0; i<newList.size(); i++) {
						VehicleParameters e = newList.get(i);
						if(e.getVehicle().getRegistrationNo().equalsIgnoreCase(vp.getVehicle().getRegistrationNo())) {
							newList.set(i, e);
							exists = true;
							break;
						}
					}
					if(!exists)
						newList.add(vp);
				}
				vpList = newList;
			}
			if(vpList != null && vpList.size() > 0) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
					setPeriod(sdf.format(getStart_dt()) + " - " + sdf.format(getEnd_dt()));
				} catch(Exception ex){ ex.printStackTrace(); }
				utilazationReport = new UtilizationReportDS();
				Vector<UtilizationReportDS.Entry> groupList = new Vector<UtilizationReportDS.Entry>();
				double daysCount = 0;
				Calendar start_can = Calendar.getInstance(), end_can = Calendar.getInstance();
				while(start_can.before(end_can)) {
					int dow = start_can.get(Calendar.DAY_OF_WEEK);
					if(dow!=Calendar.SATURDAY &&
							dow!=Calendar.SUNDAY)
						daysCount += 1;
					start_can.add(Calendar.DATE, 1);
				}
				double standardWorktime = 12*daysCount;
				int noOfVehicles = vpList.size();
				utilazationReport.setNoOfAssets(noOfVehicles);
				for(VehicleParameters vp : vpList) {
					int tripsCount = 0;
					double actualWorkingTime = 0, drivingTime = 0, distanceCovered = 0, costPerKm = 0;
					// vehicle % utilization= standard worktime/actual worktime*100
					Query q = gDAO.createQuery("Select e from CorporateTrip e where e.vehicle.id = :vehicle_id and (e.departureDateTime between :st_dt and :ed_dt) and (e.tripStatus = 'ON_TRIP' or e.tripStatus = 'SHOULD_BE_COMPLETED' or e.tripStatus = 'COMPLETION_REQUEST' or e.tripStatus = 'COMPLETED')");
					q.setParameter("vehicle_id", vp.getVehicle().getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					Object obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<CorporateTrip> list = (Vector<CorporateTrip>)obj;
						tripsCount = list.size();
						for(CorporateTrip ct : list) {
							Date start = ct.getDepartureDateTime();
							Date end = (ct.getCompleteRequestDateTime() != null) ? ct.getCompleteRequestDateTime() : ct.getCompletedDateTime();
							if(end == null)
								end = new Date();
							long durationMilli = Math.abs(end.getTime() - start.getTime());
							long hour = 1000*60*60;
							int divide = 0;
							try {
								divide = Integer.parseInt(""+(durationMilli/hour));
							} catch(Exception ex){}
							if(divide <= 0)
								divide = 1;
							actualWorkingTime += divide;
						}
					}
					q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
					q.setParameter("v_id", vp.getVehicle().getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					Object listObj = gDAO.search(q, 0);
					if(listObj != null) {
						double start_odometer = 0, end_odometer = 0;
						boolean start = true;
						Vector<VehicleOdometerData> list = (Vector<VehicleOdometerData>)listObj;
						for(VehicleOdometerData e : list) {
							end_odometer = e.getOdometer();
							if(start) {
								start = false;
								start_odometer = e.getOdometer();
							}
						}
						double mymileage_consumption = Math.abs(end_odometer-start_odometer);
						distanceCovered += mymileage_consumption;
					}
					double currentOdometer = 0;
					q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.id = :v_id");
					q.setParameter("v_id", vp.getVehicle().getId());
					listObj = gDAO.search(q, 0);
					if(listObj != null) {
						Vector<VehicleTrackerData> list = (Vector<VehicleTrackerData>)listObj;
						for(VehicleTrackerData e : list)
							currentOdometer = e.getOdometer();
					}
					
					double myfuel_consumption = 0;
					q = gDAO.createQuery("Select e from VehicleFuelData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
					q.setParameter("v_id", vp.getVehicle().getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					listObj = gDAO.search(q, 0);
					if(listObj != null) {
						double last_fuel_level = 0;
						Vector<VehicleFuelData> list = (Vector<VehicleFuelData>)listObj;
						for(VehicleFuelData e : list) {
							if(last_fuel_level > e.getFuelLevel()) {
								myfuel_consumption += last_fuel_level - e.getFuelLevel();
							}
							last_fuel_level = e.getFuelLevel();
						}
					}
					
					String key = null;
					key = vp.getVehicle().getRegistrationNo();
					/*if(filterType.equals("byregion"))
						try {
							key = vp.getRegion().getName();
						} catch(Exception ex){}
					else if(filterType.equals("byfleet"))
						key = vp.getVehicle().getRegistrationNo();
					else if(filterType.equals("byenginecap"))
						try {
							key = vp.getVehicle().getEngineCapacity().getName();
						} catch(Exception ex){}
					else if(filterType.equals("bybrand"))
						key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";*/
					
					if(key != null) {
						boolean exists = false;
						for(UtilizationReportDS.Entry e : groupList) {
							if(e.getVehicleReg().equals(key)) {
								e.setCurrentOdometer(currentOdometer);
								e.setFuelConsumption(myfuel_consumption);
								e.setActualWorkingTime(e.getActualWorkingTime()+actualWorkingTime);
								e.setCostPerKm(e.getCostPerKm()+costPerKm);
								e.setDistanceCovered(e.getDistanceCovered()+distanceCovered);
								e.setDrivingTime(e.getDrivingTime()+drivingTime);
								e.setTripsCount(e.getTripsCount()+tripsCount);
								e.setNoOfVehicles(e.getNoOfVehicles()+1);
								if(e.getActualWorkingTime() > 0) {
									try {
									double percentUtil = new BigDecimal(e.getStandardWorktime()).divide(new BigDecimal(e.getActualWorkingTime())).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP).doubleValue();
									e.setPercentUtil(percentUtil);
									} catch(Exception ex){}
								}
								exists = true;
								break;
							}
						}
						if(!exists) {
							UtilizationReportDS.Entry entry = utilazationReport.new Entry();
							entry.setVehicleReg(key);
							entry.setCurrentOdometer(currentOdometer);
							entry.setFuelConsumption(myfuel_consumption);
							entry.setStandardWorktime(standardWorktime);
							entry.setActualWorkingTime(actualWorkingTime);
							entry.setCostPerKm(costPerKm);
							entry.setDistanceCovered(distanceCovered);
							entry.setDrivingTime(drivingTime);
							entry.setTripsCount(tripsCount);
							if(entry.getActualWorkingTime() > 0) {
								try {
								double percentUtil = new BigDecimal(entry.getStandardWorktime()).divide(new BigDecimal(entry.getActualWorkingTime())).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP).doubleValue();
								entry.setPercentUtil(percentUtil);
								} catch(Exception ex){}
							}
							entry.setNoOfVehicles(1);
							groupList.add(entry);
						}
					}
				}
				utilazationReport.setData(groupList);
			}
			gDAO.destroy();
			
			if(getUtilazationReport() != null && getUtilazationReport().getCollectionList().size() > 0) {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getUtilazationReport().getCollectionList().size() + " record(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "No record found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} else {
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Please supply date range!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void searchFleetCost() {
		setFleetCost(null);
		if(getStart_dt() != null && getEnd_dt() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("vehicle.partner.id", getPartner().getId());
			if(region_id > 0 && filterType.equals("byregion"))
				params.put("region.id", region_id);
			if(division_id > 0 && !filterType.equals("byfleet"))
				params.put("dept.division.id", division_id);
			if(department_id > 0 && !filterType.equals("byfleet"))
				params.put("dept.id", department_id);
			if(unit_id > 0 && !filterType.equals("byfleet"))
				params.put("unit.id", unit_id);
			if(fleet_id > 0)
				params.put("vehicle.fleet.id", fleet_id);
			if(engineCapacity_id > 0 && filterType.equals("byenginecap"))
				params.put("vehicle.engineCapacity.id", engineCapacity_id);
			if(brand_id > 0 && filterType.equals("bybrand"))
				params.put("vehicle.model.id", brand_id);
			Vector<VehicleParameters> vpList = null;
			Object vpObj = gDAO.search("VehicleParameters", params);
			if(vpObj != null) {
				vpList = (Vector<VehicleParameters>)vpObj;
				if(yearOfPurchase > 0 && filterType.equals("byyearofp")) {
					Vector<VehicleParameters> newList = new Vector<VehicleParameters>();
					for(VehicleParameters vp : vpList) {
						Date dop = vp.getVehicle().getPurchaseDate();
						if(dop != null) {
							int v_year = 1900 + dop.getYear();
							if(yearOfPurchase == v_year) {
								newList.add(vp);
							}
						}
					}
					vpList = newList;
				}
				Vector<VehicleParameters> newList = new Vector<VehicleParameters>();
				for(VehicleParameters vp : vpList) {
					boolean exists = false;
					for(int i=0; i<newList.size(); i++) {
						VehicleParameters e = newList.get(i);
						if(e.getVehicle().getRegistrationNo().equalsIgnoreCase(vp.getVehicle().getRegistrationNo())) {
							newList.set(i, e);
							exists = true;
							break;
						}
					}
					if(!exists)
						newList.add(vp);
				}
				vpList = newList;
			}
			if(vpList != null) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
					setPeriod(sdf.format(getStart_dt()) + " - " + sdf.format(getEnd_dt()));
				} catch(Exception ex){ ex.printStackTrace(); }
				for(VehicleParameters vp : vpList) {
					int trips = 0;
					double distance = 0, totalCost=0, maintCost=0, fuelCost=0, driverCost=0, licenseCost=0, otherCost=0;
					Query q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :vehicle_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
					q.setParameter("vehicle_id", vp.getVehicle().getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					Object obj = gDAO.search(q, 0);
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
						BigDecimal distanceDeci = new BigDecimal(Math.abs(endOdometer - startOdometer));
						distanceDeci = distanceDeci.setScale(2, RoundingMode.HALF_UP);
						distance = distanceDeci.doubleValue();
					}
					
					q = gDAO.createQuery("Select e from CorporateTrip e where e.vehicle.id = :vehicle_id and e.tripStatus = 'COMPLETED' and (e.departureDateTime between :st_dt and :ed_dt)");
					q.setParameter("vehicle_id", vp.getVehicle().getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<CorporateTrip> objList = (Vector<CorporateTrip>)obj;
						if(objList != null && objList.size() > 0) {
							for(CorporateTrip vted : objList) {
								if(vted.getId() != null)
									trips += 1;
							}
						}
					}
					
					q = gDAO.createQuery("Select e from VehicleRoutineMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.id=:v_id");
					q.setParameter("v_id", vp.getVehicle().getId());
					q.setParameter("start_dt", getStart_dt());
					q.setParameter("end_dt", getEnd_dt());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<VehicleRoutineMaintenance> objList = (Vector<VehicleRoutineMaintenance>)obj;
						for(VehicleRoutineMaintenance vrm : objList) {
							BigDecimal cost = new BigDecimal(0);
							if(vrm.getClosed_amount() != null) {
								cost = cost.add(vrm.getClosed_amount());
								cost = cost.setScale(2);
							}
							maintCost += cost.doubleValue();
							totalCost += cost.doubleValue();
						}
					}
					q = gDAO.createQuery("Select e from VehicleAdHocMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.id=:v_id");
					q.setParameter("v_id", vp.getVehicle().getId());
					q.setParameter("start_dt", getStart_dt());
					q.setParameter("end_dt", getEnd_dt());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<VehicleAdHocMaintenance> objList = (Vector<VehicleAdHocMaintenance>)obj;
						for(VehicleAdHocMaintenance vrm : objList) {
							BigDecimal cost = new BigDecimal(0);
							if(vrm.getClosed_cost() != null) {
								cost = cost.add(vrm.getClosed_cost());
								cost = cost.setScale(2);
							}
							maintCost += cost.doubleValue();
							totalCost += cost.doubleValue();
						}
					}
					
					q = gDAO.createQuery("Select e from Expense e where (e.expense_dt between :start_dt and :end_dt) and e.vehicle.id=:v_id");
					q.setParameter("v_id", vp.getVehicle().getId());
					q.setParameter("start_dt", getStart_dt());
					q.setParameter("end_dt", getEnd_dt());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<Expense> objList = (Vector<Expense>)obj;
						for(Expense vrm : objList) {
							BigDecimal cost = new BigDecimal(vrm.getAmount());
							cost = cost.setScale(2, RoundingMode.HALF_UP);
							if(vrm.getType().getName().equalsIgnoreCase("Fueling")) {
								fuelCost += cost.doubleValue();
								totalCost += cost.doubleValue();
							} else if(vrm.getType().getName().equalsIgnoreCase("Driver")) {
								driverCost += cost.doubleValue();
								totalCost += cost.doubleValue();
							} else if(vrm.getType().getName().contains("License")) {
								licenseCost += cost.doubleValue();
								totalCost += cost.doubleValue();
							} else {
								otherCost += cost.doubleValue();
								totalCost += cost.doubleValue();
							}
						}
					}
					
					String key = null;
					if(filterType.equals("byregion"))
						try {
							key = vp.getRegion().getName();
						} catch(Exception ex){}
					else if(filterType.equals("byfleet"))
						key = vp.getVehicle().getRegistrationNo();
					else if(filterType.equals("byenginecap"))
						try {
							key = vp.getVehicle().getEngineCapacity().getName();
						} catch(Exception ex){}
					else if(filterType.equals("bybrand"))
						key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
					if(key != null) {
						FuelConsumption fc2 = new FuelConsumption();
						boolean exists = false;
						for(FuelConsumption e : getFleetCost()) {
							if(e.getRegNo().equals(key)) {
								e.setDistance(e.getDistance() + distance);
								e.setFuelCost(e.getFuelCost() + fuelCost);
								e.setDriverCost(e.getDriverCost() + driverCost);
								e.setMaintCost(e.getMaintCost() + maintCost);
								e.setLicenseCost(e.getLicenseCost() + licenseCost);
								e.setOtherCost(e.getOtherCost() + otherCost);
								e.setTripsCount(e.getTripsCount() + trips);
								e.setNoOfVehicles(e.getNoOfVehicles()+1);
								try {
									double costPerKm = (e.getFuelCost()+e.getDriverCost()+e.getMaintCost()+e.getLicenseCost()+e.getOtherCost())/e.getDistance();
									e.setCostPerKm(costPerKm);
								} catch(Exception ex){}
								exists = true;
								break;
							}
						}
						if(!exists) {
							fc2.setRegNo(key);
							fc2.setDistance(distance);
							fc2.setFuelCost(fuelCost);
							fc2.setDriverCost(driverCost);
							fc2.setMaintCost(maintCost);
							fc2.setLicenseCost(licenseCost);
							fc2.setOtherCost(otherCost);
							fc2.setTripsCount(trips);
							fc2.setNoOfVehicles(1);
							try {
								double costPerKm = totalCost/distance;
								fc2.setCostPerKm(costPerKm);
							} catch(Exception ex){}
							getFleetCost().add(fc2);
						}
					}
				}
			}
			
			gDAO.destroy();
			
			if(getFleetCost() != null && getFleetCost().size() > 0) {
				if(getPieModel() == null)
					createPieModel();
				
				for(FuelConsumption v : getFleetCost()) {
					double totalCost = v.getFuelCost() + v.getDriverCost() + v.getMaintCost() + v.getLicenseCost() + v.getOtherCost();
					getPieModel().set(v.getRegNo(), totalCost);
				}
				
				if(barModel == null)
					createBarModel();
				maxY = 0;
				ChartSeries distanceCoveredSeries = new ChartSeries();
				distanceCoveredSeries.setLabel("Distance covered");
				for(FuelConsumption v : getFleetCost()) {
					if(v.getDistance() > 0) {
						distanceCoveredSeries.set(v.getRegNo(), v.getDistance());
						if(v.getDistance() > maxY)
							maxY = new BigDecimal(v.getDistance()).longValue() + 5;
					} else
						distanceCoveredSeries.set(v.getRegNo(), 0);
				}
				barModel.addSeries(distanceCoveredSeries);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getFleetCost().size() + " record(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "No record found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} else {
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Please supply date range!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void downloadFleetCostPDF() {
		try {
			FleetCostDS fleetCostDS = FleetCostDS.getInstance();
			Vector<FleetCostDS.Entry> data = new Vector<FleetCostDS.Entry>();
			double distance = 0, trips = 0, avgDistance = 0, mainCost=0, driverCost=0, fuelCost=0;
			double totalAsstes = 0, totalCost=0, costPerKm = 0;
			for(FuelConsumption fc : getFleetCost()) {
				FleetCostDS.Entry entry = fleetCostDS. new Entry();
				// private String vehicleReg;
				//private double maintCost, distanceCovered, tripsCount, noOfVehicles, driverCost, fuelCost, costPerKm;
				entry.setMaintCost(fc.getMaintCost());
				mainCost+=entry.getMaintCost();
				totalCost+=entry.getMaintCost();
				entry.setDriverCost(fc.getDriverCost());
				driverCost+=entry.getDriverCost();
				totalCost+=entry.getDriverCost();
				entry.setFuelCost(fc.getFuelCost());
				fuelCost+=entry.getFuelCost();
				totalCost+=entry.getFuelCost();
				entry.setDistanceCovered(fc.getDistance());
				distance+=fc.getDistance();
				entry.setNoOfVehicles(fc.getNoOfVehicles());
				totalAsstes += entry.getNoOfVehicles();
				entry.setTripsCount(fc.getTripsCount());
				trips+=fc.getTripsCount();
				entry.setVehicleReg(fc.getRegNo());
				data.add(entry);
			}
			fleetCostDS.setData(data);
			try {
				avgDistance = new BigDecimal(distance).divide(new BigDecimal(getFleetCost().size()), RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP).doubleValue();
			} catch(Exception ex){}
			try {
				costPerKm = new BigDecimal(distance).divide(new BigDecimal(totalCost), RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP).doubleValue();
			} catch(Exception ex){}
			
			GeneralDAO gDAO = new GeneralDAO();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
			Map<String, Object> parameters = new HashMap<String, Object>();
			if(filterType.equals("byregion")) {
				parameters.put("reportTitle", "Fleet Cost Report, Regional Vehicles");
				setGroupBy("Region");
			}
			else if(filterType.equals("byfleet")) {
				parameters.put("reportTitle", "Fleet Cost Report, Individual Vehicle");
				setGroupBy("Individual Vehicles");
			}
			else if(filterType.equals("byenginecap")) {
				parameters.put("reportTitle", "Fleet Cost Report, Engine Capacity");
				setGroupBy("Engine Capacity");
			}
			else if(filterType.equals("bybrand")) {
				parameters.put("reportTitle", "Fleet Cost Report, Vehicle Brands");
				setGroupBy("Vehicle Brand");
			}
			parameters.put("reportDesc", "This is a summary report of fleet cost.");
			parameters.put("preparedOn", sdf.format(new Date()));
			parameters.put("groupBy", getGroupBy());
			parameters.put("period", getPeriod());
			if(getRegion_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Region.class, getRegion_id());
				if(obj != null) {
					Region r = (Region)obj;
					parameters.put("region", r.getName());
				} else
					parameters.put("region", "N/A");
			} else
				parameters.put("region", "All");
			if(getDivision_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Division.class, getDivision_id());
				if(obj != null) {
					Division r = (Division)obj;
					parameters.put("branch", r.getName());
				} else
					parameters.put("branch", "N/A");
			} else
				parameters.put("branch", "All");
			if(getDepartment_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Department.class, getDepartment_id());
				if(obj != null) {
					Department r = (Department)obj;
					parameters.put("department", r.getName());
				} else
					parameters.put("department", "N/A");
			} else
				parameters.put("department", "All");
			
			gDAO.destroy();
			
			parameters.put("totalsLabel1", "Maintenance cost");
			parameters.put("totalsValue1", ""+mainCost);
			parameters.put("totalsLabel2", "Distance");
			parameters.put("totalsValue2", ""+distance);
			parameters.put("totalsLabel3", "Driver Cost");
			parameters.put("totalsValue3", ""+driverCost);
			parameters.put("totalsLabel4", "Refueling Cost");
			parameters.put("totalsValue4", ""+fuelCost);
			parameters.put("totalsLabel5", "No of Trips");
			parameters.put("totalsValue5", ""+trips);
			parameters.put("totalsLabel6", "");
			parameters.put("totalsValue6", "");
			parameters.put("totalsLabel7", "");
			parameters.put("totalsValue7", "");
			
			parameters.put("summaryLabel1", "No of Assets");
			parameters.put("summaryValue1", ""+totalAsstes);
			
			parameters.put("summaryLabel2", "Total Fleet Cost");
			parameters.put("summaryValue2", ""+totalCost);
			
			parameters.put("summaryLabel3", "Average Fleet Costs/Km");
			parameters.put("summaryValue3", ""+costPerKm);
			
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(fleetCostDS.getCollectionList());
			
			downloadJasperPDF(parameters, "milleage_report.pdf", "/resources/jasper/fleetCost.jasper", ds);
		} catch(Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void coverageAreaSummary() {
		if(getStart_dt() != null && getEnd_dt() != null) {
			GeneralDAO gDAO = new GeneralDAO();
		
			
			
			gDAO.destroy();
		} else {
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed: ", "Please supply date range!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void speedSummaryReport() {
		speedSummary = null;
		if(getStart_dt() != null && getEnd_dt() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getPartner());
			if(getFleet_id() != null && getFleet_id() > 0) {
				params.put("fleet.id", getFleet_id());
			}
			if(getVehicle_id() != null && getVehicle_id() > 0) {
				params.put("id", getVehicle_id());
			}
			Object obj = gDAO.search("Vehicle", params);
			if(obj != null) {
				speedSummary = new SpeedSummary();
				Vector<Vehicle> vehicles = (Vector<Vehicle>)obj;
				Vector<VehicleSpeedData> thirtyKmList = new Vector<VehicleSpeedData>(), nintyKmList = new Vector<VehicleSpeedData>(), aboveKmList = new Vector<VehicleSpeedData>(), allKmList = new Vector<VehicleSpeedData>();
				speedSummary.setVehicles(vehicles);
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
					setPeriod(sdf.format(getStart_dt()) + " - " + sdf.format(getEnd_dt()));
				} catch(Exception ex){ ex.printStackTrace(); }
				for(Vehicle v : speedSummary.getVehicles()) {
					if(getFleet_id() != null && getFleet_id() > 0) {
						setGroupBy(v.getFleet().getName());
					} else {
						setGroupBy("All");
					}
					Query q = gDAO.createQuery("Select e from VehicleSpeedData e where e.speed > 0 and e.vehicle.id=:v_id and (e.captured_dt between :st_dt and :ed_dt)");
					q.setParameter("v_id", v.getId());
					q.setParameter("st_dt", getStart_dt());
					q.setParameter("ed_dt", getEnd_dt());
					Object obj2 = gDAO.search(q, 0);
					if(obj2 != null) {
						Vector<VehicleSpeedData> speedList = (Vector<VehicleSpeedData>)obj2;
						for(VehicleSpeedData vsd : speedList) {
							allKmList.add(vsd);
							if(vsd.getSpeed() <= 30)
								thirtyKmList.add(vsd);
							else if(vsd.getSpeed() > 30 && vsd.getSpeed() <= 90)
								nintyKmList.add(vsd);
							else
								aboveKmList.add(vsd);
						}
					}
				}
				speedSummary.setAllKmList(allKmList);
				speedSummary.setAboveKmCount(aboveKmList.size());
				speedSummary.setAboveKmList(aboveKmList);
				speedSummary.setNintyKmCount(nintyKmList.size());
				speedSummary.setNintyKmList(nintyKmList);
				speedSummary.setThirtyKmCount(thirtyKmList.size());
				speedSummary.setThirtyKmList(thirtyKmList);
			}
			gDAO.destroy();
			
			if(speedSummary != null && speedSummary.getVehicles() != null && speedSummary.getVehicles().size() > 0) {
				if(getPieModel() == null)
					createPieModel();
				
				getPieModel().set("0-30km/h", speedSummary.getThirtyKmCount());
				getPieModel().set("30-90kn/h", speedSummary.getNintyKmCount());
				getPieModel().set("Above 90km/h", speedSummary.getAboveKmCount());
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", speedSummary.getVehicles().size() + " vehicle(s) found!");
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
			if(getFleet_id() != null && getFleet_id() > 0) {
				params.put("fleet.id", getFleet_id());
			}
			if(getVehicle_id() != null && getVehicle_id() > 0) {
				params.put("id", getVehicle_id());
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
					if(getFleet_id() != null && getFleet_id() > 0) {
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
						BigDecimal distanceDeci = new BigDecimal(Math.abs(endOdometer - startOdometer));
						distanceDeci = distanceDeci.setScale(2, RoundingMode.HALF_UP);
						double distance = distanceDeci.doubleValue();
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
			if(getFleet_id() != null && getFleet_id() > 0) {
				params.put("fleet.id", getFleet_id());
			}
			if(getVehicle_id() != null && getVehicle_id() > 0) {
				params.put("id", getVehicle_id());
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
					if(getFleet_id() != null && getFleet_id() > 0) {
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
						long trips_duration = 0;
						for(CorporateTrip ct : list) {
							if(ct.getDriver() != null && !driverIds.contains(""+ct.getDriver().getId().longValue())) {
								driverIds.add(""+ct.getDriver().getId().longValue());
							}
							Date end_trip = new Date();
							if(ct.getCompletedDateTime() != null)
								end_trip = ct.getCompletedDateTime();
							trips_duration += Math.abs(end_trip.getTime() - ct.getDepartureDateTime().getTime());
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
						if(trips_duration > 0) {
							try {
								BigDecimal timemilli = new BigDecimal(trips_duration).divide(new BigDecimal(3600000));
								timemilli = timemilli.setScale(2);
								v.setWorking_time(timemilli.doubleValue());
								corporateTripsSummary.setWorking_time(corporateTripsSummary.getWorking_time() + v.getWorking_time());
							} catch(Exception ex) {}
						} else
							v.setWorking_time(0);
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
						avgSpeed = totalSpeed.divide(speedEntryCount, 2, RoundingMode.HALF_UP);
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
						BigDecimal distanceDeci = new BigDecimal(Math.abs(endOdometer - startOdometer));
						distanceDeci = distanceDeci.setScale(2, RoundingMode.HALF_UP);
						double distance = distanceDeci.doubleValue();
						v.setDistance(new BigDecimal(distance).setScale(2, RoundingMode.HALF_UP).doubleValue());
						corporateTripsSummary.setDistance(corporateTripsSummary.getDistance() + v.getDistance());
					}
					
					q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.id = :vehicle_id");
					q.setParameter("vehicle_id", v.getId());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<VehicleTrackerData> objList = (Vector<VehicleTrackerData>)obj;
						for(VehicleTrackerData vtd : objList) {
							// This is used to represent the vehicle's current odometer
							v.setMaint_odometer(new BigDecimal(vtd.getOdometer()));
						}
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
								BigDecimal oneLiter = new BigDecimal(v.getDistance()).divide(new BigDecimal(v.getFuel_consumed()), 2, RoundingMode.HALF_UP);
								oneLiter = oneLiter.setScale(2);
								v.setKm_per_liter(oneLiter.doubleValue());
								corporateTripsSummary.setKm_per_liter(corporateTripsSummary.getKm_per_liter() + v.getKm_per_liter());
							} catch(Exception ex) {}
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
			if(getFleet_id() != null && getFleet_id() > 0) {
				params.put("fleet.id", getFleet_id());
			}
			if(getVehicle_id() != null && getVehicle_id() > 0) {
				params.put("id", getVehicle_id());
			}
			Object obj = gDAO.search("Vehicle", params);
			if(obj != null) {
				utilizationSummary = new VehicleUtilizationSummary();
				Vector<Vehicle> vehicles = (Vector<Vehicle>)obj;
				if((getDivision_id() != null && getDivision_id() > 0) || 
						(getDepartment_id() != null && getDepartment_id() > 0) ||
						(getUnit_id() != null && getUnit_id() > 0)) {
					Vector<Vehicle> newList = new Vector<Vehicle>();
					for(Vehicle v : vehicles) {
						Hashtable<String, Object> params2 = new Hashtable<String, Object>();
						params2.put("vehicle.id", v.getId());
						if(getDivision_id() != null && getDivision_id() > 0)
							params2.put("dept.division.id", getDivision_id());
						if(getDepartment_id() != null && getDepartment_id() > 0)
							params2.put("dept.id", getDepartment_id());
						if(getUnit_id() != null && getUnit_id() > 0)
							params2.put("unit.id", getUnit_id());
						Object vpObj = gDAO.search("VehicleParameters", params2);
						if(vpObj != null) {
							Vector<VehicleParameters> vpList = (Vector<VehicleParameters>)vpObj;
							if(vpList.size() > 0)
								newList.add(v);
						}
					}
					vehicles = newList;
				}
				
				utilizationSummary.setTotalCount(vehicles.size());
				utilizationSummary.setVehicles(vehicles);
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
					setPeriod(sdf.format(getStart_dt()) + " - " + sdf.format(getEnd_dt()));
				} catch(Exception ex){ ex.printStackTrace(); }
				for(Vehicle v : utilizationSummary.getVehicles()) {
					if(getFleet_id() != null && getFleet_id() > 0) {
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
						avgSpeed = totalSpeed.divide(speedEntryCount, 2, RoundingMode.HALF_UP);
						v.setAverage_speed(avgSpeed.doubleValue());
						} catch(Exception ex){ ex.printStackTrace(); }
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
						BigDecimal distanceDeci = new BigDecimal(Math.abs(endOdometer - startOdometer));
						distanceDeci = distanceDeci.setScale(2, RoundingMode.HALF_UP);
						double distance = distanceDeci.doubleValue();
						v.setDistance_covered(new BigDecimal(distance).setScale(2, RoundingMode.HALF_UP).doubleValue());
					}
					
					q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.id = :vehicle_id");
					q.setParameter("vehicle_id", v.getId());
					obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<VehicleTrackerData> objList = (Vector<VehicleTrackerData>)obj;
						for(VehicleTrackerData vtd : objList) {
							v.setDistance(vtd.getOdometer());
						}
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
		//TODO: Put implementation
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
		
		if(getPartner() != null)
		{
			setDriversByRegion(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner.id", getPartner().getId());
			if(division_id > 0)
				params.put("personel.department.division.id", division_id);
			if(department_id > 0)
				params.put("personel.department.id", department_id);
			if(unit_id > 0)
				params.put("personel.unit.id", unit_id);
			if(region_id > 0)
				params.put("personel.region.id", region_id);
			
			Object drvs = gDAO.search("PartnerDriver", params);
			if(drvs != null)
			{
				setDriversByRegion((Vector<PartnerDriver>)drvs);
				if(yos > 0) {
					Vector<PartnerDriver> newList = new Vector<PartnerDriver>();
					for(PartnerDriver pd : getDriversByRegion()) {
						if(pd.getYearsOfService() == yos) 
							newList.add(pd);
					}
					setDriversByRegion(newList);
				}
				for(PartnerDriver pd : getDriversByRegion())
				{
					params = new Hashtable<String, Object>();
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
	public void search() {
		resetReportInfo();
		
		if(getPartner() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("partner", getPartner());
			if(getRegion_id() != null)
				params.put("personel.region.id", getRegion_id());
			if(getDivision_id() != null)
				params.put("personel.department.division.id", getDivision_id());
			if(getDepartment_id() != null)
				params.put("personel.department.id", getDepartment_id());
			if(getUnit_id() != null)
				params.put("personel.unit.id", getUnit_id());
			
			Object dpsObj = gDAO.search("PartnerUser", params);
			if(dpsObj != null) {
				allUsers = (Vector<PartnerUser>)dpsObj;
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", allUsers.size() + " user(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("All User Report");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No user found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
	}
	
	public void searchDriverLicenseRenewal() {
		resetReportInfo();
		
		if(getPartner() != null && getStart_dt() != null && getEnd_dt() != null) {
			setDriverLicenseRenewal(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String qry = "Select e from DriverLicense e where e.driver.partner.id=:partner_id and (e.lic_start_dt between :st_dt and :ed_dt)";
			if(division_id > 0)
				qry += " and e.driver.personel.department.division.id=:division_id";
			if(department_id > 0)
				qry += " and e.driver.personel.department.id=:department_id";
			if(unit_id > 0)
				qry += " and e.driver.personel.unit.id=:unit_id";
			if(region_id > 0)
				qry += " and e.driver.personel.region.id=:region_id";
			
			Query q = gDAO.createQuery(qry);
			q.setParameter("partner_id", getPartner().getId());
			q.setParameter("st_dt", getStart_dt());
			q.setParameter("ed_dt", getEnd_dt());
			if(division_id > 0)
				q.setParameter("division_id", division_id);
			if(department_id > 0)
				q.setParameter("department_id", department_id);
			if(unit_id > 0)
				q.setParameter("unit_id", unit_id);
			if(region_id > 0)
				q.setParameter("region_id", region_id);
			
			Object obj = gDAO.search(q, 0);
			if(obj != null) {
				setDriverLicenseRenewal((Vector<DriverLicense>)obj);
			}
			if(getDriverLicenseRenewal().size() > 0)
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getDriverLicenseRenewal().size() + " driver license registration/renewals found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Driver License Registration/Renewal History");
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No driver license registration/renewal found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
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
			
			Query q = gDAO.createQuery("Select e from DriverLicense e where e.driver.partner=:partner and e.expired=:expired and e.active=:active"); //  and (e.lic_end_dt > :start_dt and e.lic_end_dt < :end_date)
			q.setParameter("partner", getPartner());
			q.setParameter("expired", true);
			q.setParameter("active", true);
			/*q.setParameter("start_dt", c.getTime());
			q.setParameter("end_date", c2.getTime());*/
			
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
		Vector<VehicleAccident> list = new Vector<VehicleAccident>();
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null)
		{
			setVehicleAccidents(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			Vector<VehicleParameters> vpList = filterVehicles(gDAO, false, false, false);
			if(vpList != null) {
				String str = "Select e from VehicleAccident e where (e.accident_dt between :start_dt and :end_dt) and e.vehicle.id=:v_id";
				if(driver_id != null && driver_id > 0)
					str += " and e.accidentDriver.id=:d_id";
				if(action_taken != null && ((!action_taken.isEmpty() && !action_taken.equalsIgnoreCase("Any")) ||
						action_taken.isEmpty()))
					str += " and e.requiresRepairOrReplace=:action_taken";
				for(VehicleParameters vp : vpList) {
					Query q = gDAO.createQuery(str);
					q.setParameter("start_dt", getStart_dt());
					q.setParameter("end_dt", getEnd_dt());
					q.setParameter("v_id", vp.getVehicle().getId());
					if(driver_id != null && driver_id > 0)
						q.setParameter("d_id", driver_id);
					if(action_taken != null && ((!action_taken.isEmpty() && !action_taken.equalsIgnoreCase("Any")) ||
							action_taken.isEmpty()))
						q.setParameter("action_taken", action_taken);
					Object obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<VehicleAccident> objlist = (Vector<VehicleAccident>)obj;
						list.addAll(objlist);
					}
				}
			}
			gDAO.destroy();
			
			if(list.size() > 0) {
				setVehicleAccidents(list);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getVehicleAccidents().size() + " accident(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Accident Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			} else {
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
			
			if(getFleet_id() != null && getFleet_id() > 0) {
				Object fleetObj = gDAO.find(Fleet.class, getFleet_id());
				if(fleetObj != null) {
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
			GeneralDAO gDAO = new GeneralDAO();
			
			Vector<VehicleParameters> vpList = filterVehicles(gDAO, false, false, false);
			Vector<String[]> vlist = new Vector<String[]>();
			for(VehicleParameters vp : vpList) {
				String[] e = new String[2];
				e[0] = ""+vp.getVehicle().getId();
				e[1] = vp.getVehicle().getRegistrationNo();
				vlist.add(e);
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
				double totalDistanceCovered = 0;
				String[] date_tow = new String[]{"Total", "", "", "", "", ""};
				
				for(String[] e : vlist) {
					String[] r = new String[6];
					r[0] = e[1];
					r[1] = sdf.format(start_can.getTime());
					r[2] = sdf.format(search_end_can.getTime());
					int count = 0;
					BigDecimal cost = new BigDecimal(0);
					double distanceCovered = 0;
					Query q = gDAO.createQuery("Select e from VehicleAccidentRepair e where (e.accident.accident_dt between :start_dt and :end_dt) and e.accident.vehicle.id=:v_id order by e.accident.accident_dt");
					q.setParameter("v_id", Long.parseLong(e[0]));
					q.setParameter("start_dt", start_can.getTime());
					q.setParameter("end_dt", search_end_can.getTime());
					Object obj = gDAO.search(q, 0);
					if(obj != null) {
						Vector<VehicleAccidentRepair> objList = (Vector<VehicleAccidentRepair>)obj;
						double prevOdometer = 0;
						for(VehicleAccidentRepair var : objList) {
							if(var.getAccident().getOdometer() > 0) {
								distanceCovered += Math.abs(var.getAccident().getOdometer() - prevOdometer);
								prevOdometer = var.getAccident().getOdometer();
							}
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
						totalDistanceCovered += distanceCovered;
						r[3] = ""+count;
						r[4] = cost.toPlainString();
						r[5] = ""+distanceCovered;
						list.add(r);
					}
				}
				if(total_cost.doubleValue() > 0) {
					date_tow[4] = total_cost.toPlainString();
					date_tow[5] = ""+totalDistanceCovered;
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
				String[] date_tow = new String[]{"Total", "", "", "", "", "", "", ""};
				
				for(Item itm : items) {
					for(String[] e : vlist) {
						String[] r = new String[8];
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
							r[6] = "";
							r[7] = "";
							
							q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :vehicle_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
							q.setParameter("vehicle_id", Long.parseLong(e[0]));
							q.setParameter("st_dt", start_can.getTime());
							q.setParameter("ed_dt", search_end_can.getTime());
							obj = gDAO.search(q, 0);
							if(obj != null) {
								Vector<VehicleOdometerData> odolist = (Vector<VehicleOdometerData>)obj;
								double startOdometer = 0, endOdometer = 0;
								for(int i=0; i<odolist.size(); i++) {
									VehicleOdometerData vod = odolist.get(i);
									if(i == 0)
										startOdometer = vod.getOdometer();
									if(i == odolist.size() - 1)
										endOdometer = vod.getOdometer();
								}
								BigDecimal distanceDeci = new BigDecimal(Math.abs(endOdometer - startOdometer));
								distanceDeci = distanceDeci.setScale(2, RoundingMode.HALF_UP);
								double distance = distanceDeci.doubleValue();
								r[6] = ""+distance;
							}
							
							q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.id = :vehicle_id order by e.captured_dt");
							q.setParameter("vehicle_id", Long.parseLong(e[0]));
							obj = gDAO.search(q, 0);
							if(obj != null) {
								Vector<VehicleTrackerData> odolist = (Vector<VehicleTrackerData>)obj;
								for(VehicleTrackerData vtd : odolist)
									r[7] = ""+vtd.getOdometer();
							}
							
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
				String[] date_tow = new String[]{"Total", "", "", "", "", "", "", ""};
				
				for(Item itm : items) {
					for(String[] e : vlist) {
						String[] r = new String[8];
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
							r[6] = "";
							r[7] = "";
							
							q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :vehicle_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
							q.setParameter("vehicle_id", Long.parseLong(e[0]));
							q.setParameter("st_dt", start_can.getTime());
							q.setParameter("ed_dt", search_end_can.getTime());
							obj = gDAO.search(q, 0);
							if(obj != null) {
								Vector<VehicleOdometerData> odolist = (Vector<VehicleOdometerData>)obj;
								double startOdometer = 0, endOdometer = 0;
								for(int i=0; i<odolist.size(); i++) {
									VehicleOdometerData vod = odolist.get(i);
									if(i == 0)
										startOdometer = vod.getOdometer();
									if(i == odolist.size() - 1)
										endOdometer = vod.getOdometer();
								}
								BigDecimal distanceDeci = new BigDecimal(Math.abs(endOdometer - startOdometer));
								distanceDeci = distanceDeci.setScale(2, RoundingMode.HALF_UP);
								double distance = distanceDeci.doubleValue();
								r[6] = ""+distance;
							}
							
							q = gDAO.createQuery("Select e from VehicleTrackerData e where e.vehicle.id = :vehicle_id order by e.captured_dt");
							q.setParameter("vehicle_id", Long.parseLong(e[0]));
							obj = gDAO.search(q, 0);
							if(obj != null) {
								Vector<VehicleTrackerData> odolist = (Vector<VehicleTrackerData>)obj;
								for(VehicleTrackerData vtd : odolist)
									r[7] = ""+vtd.getOdometer();
							}
							
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
				if(barModel == null)
					createBarModel();
				maxY = 0;
				ChartSeries maintCostSeries = new ChartSeries();
				maintCostSeries.setLabel("Maintenance Cost");
				for(String[] e : getMaintCostByPartsReport()) {
					if(e[0] != null && !e[0].equalsIgnoreCase("Total") && e[3] != null && !e[3].isEmpty()) {
						try {
							maintCostSeries.set(e[0], Double.parseDouble(e[3]));
							if(Double.parseDouble(e[3]) > maxY)
								maxY = new BigDecimal(Double.parseDouble(e[3])).longValue() + 5;
						} catch(Exception ex){}
					}
				}
				barModel.addSeries(maintCostSeries);
				
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
	
	private MaintenanceCostDS maintenanceDS;
	
	@SuppressWarnings("unchecked")
	public void searchMaintCosts() {
		resetReportInfo();
		setMaintCostReport(null);
		maintenanceDS = null;
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("vehicle.partner.id", getPartner().getId());
			if(region_id > 0 && filterType.equals("byregion"))
				params.put("region.id", region_id);
			if(division_id > 0 && !filterType.equals("byfleet"))
				params.put("dept.division.id", division_id);
			if(department_id > 0 && !filterType.equals("byfleet"))
				params.put("dept.id", department_id);
			if(unit_id > 0 && !filterType.equals("byfleet"))
				params.put("unit.id", unit_id);
			if(fleet_id > 0)
				params.put("vehicle.fleet.id", fleet_id);
			if(engineCapacity_id > 0 && filterType.equals("byenginecap"))
				params.put("vehicle.engineCapacity.id", engineCapacity_id);
			if(brand_id > 0 && filterType.equals("bybrand"))
				params.put("vehicle.model.id", brand_id);
			Vector<VehicleParameters> vpList = null;
			Object vpObj = gDAO.search("VehicleParameters", params);
			if(vpObj != null) {
				vpList = (Vector<VehicleParameters>)vpObj;
				if(yearOfPurchase > 0 && filterType.equals("byyearofp")) {
					Vector<VehicleParameters> newList = new Vector<VehicleParameters>();
					for(VehicleParameters vp : vpList) {
						Date dop = vp.getVehicle().getPurchaseDate();
						if(dop != null) {
							int v_year = 1900 + dop.getYear();
							if(yearOfPurchase == v_year) {
								newList.add(vp);
							}
						}
					}
					vpList = newList;
				}
				Vector<VehicleParameters> newList = new Vector<VehicleParameters>();
				for(VehicleParameters vp : vpList) {
					boolean exists = false;
					for(int i=0; i<newList.size(); i++) {
						VehicleParameters e = newList.get(i);
						if(e.getVehicle().getRegistrationNo().equalsIgnoreCase(vp.getVehicle().getRegistrationNo())) {
							newList.set(i, e);
							exists = true;
							break;
						}
					}
					if(!exists)
						newList.add(vp);
				}
				vpList = newList;
			}
			
			Vector<MaintenanceCostDS.MaintCost> groupList = new Vector<MaintenanceCostDS.MaintCost>();
			
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
			if(vpList != null) {
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
					
					for(VehicleParameters vp : vpList) {
						String[] r = new String[5];
						r[0] = vp.getVehicle().getRegistrationNo();
						r[1] = sdf.format(start_can.getTime());
						r[2] = sdf.format(search_end_can.getTime());
						
						double adhocCost=0, routineCost=0, totalCost=0, partsReplacementCost=0, partsServicingCost=0;
						double mymileage_consumption=0;
						
						if(getMainttype().equalsIgnoreCase("Routine") || getMainttype().equalsIgnoreCase("Both")) {
							Query q = gDAO.createQuery("Select e from VehicleRoutineMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.id=:v_id");
							q.setParameter("v_id", vp.getVehicle().getId());
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
										Query u = gDAO.createQuery("Select e from WorkOrderItem e where e.workOrderVehicle.workOrder.id=:wrk_id");
										u.setParameter("wrk_id", vrm.getWorkOrder().getId());
										Object uobj = gDAO.search(u, 0);
										if(uobj != null) {
											Vector<WorkOrderItem> woiList = (Vector<WorkOrderItem>)uobj;
											for(WorkOrderItem woi : woiList) {
												if(woi.getAction().equalsIgnoreCase("REPAIR")) {
													partsServicingCost += woi.getInitEstAmount();
												} else if(woi.getAction().equalsIgnoreCase("REPLACE")) {
													partsReplacementCost += woi.getInitEstAmount();
												}
											}
										}
									}
									routineCost += cost.doubleValue();
									totalCost += cost.doubleValue();
								}
								Query q2 = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
								q2.setParameter("v_id", vp.getVehicle().getId());
								q2.setParameter("st_dt", start_can.getTime());
								q2.setParameter("ed_dt", search_end_can.getTime());
								Object listObj = gDAO.search(q2, 0);
								if(listObj != null) {
									double start_odometer = 0, end_odometer = 0;
									boolean start = true;
									Vector<VehicleOdometerData> vodList = (Vector<VehicleOdometerData>)listObj;
									for(VehicleOdometerData e : vodList) {
										end_odometer = e.getOdometer();
										if(start) {
											start = false;
											start_odometer = e.getOdometer();
										}
									}
									mymileage_consumption += Math.abs(end_odometer-start_odometer);
								}
								if(cost.doubleValue() > 0) {
									total_cost = total_cost.add(cost);
									total_cost = total_cost.setScale(2);
									r[3] = cost.toPlainString();
									r[4] = "Routine";
									list.add(r);
								}
							}
						} else if(getMainttype().equalsIgnoreCase("AdHoc") || getMainttype().equalsIgnoreCase("Both")) {
							Query q = gDAO.createQuery("Select e from VehicleAdHocMaintenance e where (e.start_dt between :start_dt and :end_dt) and e.vehicle.id=:v_id");
							q.setParameter("v_id", vp.getVehicle().getId());
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
										Query u = gDAO.createQuery("Select e from WorkOrderItem e where e.workOrderVehicle.workOrder.id=:wrk_id");
										u.setParameter("wrk_id", vam.getWorkOrder().getId());
										Object uobj = gDAO.search(u, 0);
										if(uobj != null) {
											Vector<WorkOrderItem> woiList = (Vector<WorkOrderItem>)uobj;
											for(WorkOrderItem woi : woiList) {
												if(woi.getAction().equalsIgnoreCase("REPAIR")) {
													partsServicingCost += woi.getInitEstAmount();
												} else if(woi.getAction().equalsIgnoreCase("REPLACE")) {
													partsReplacementCost += woi.getInitEstAmount();
												}
											}
										}
									}
									adhocCost += cost.doubleValue();
									totalCost += cost.doubleValue();
								}
								Query q2 = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
								q2.setParameter("v_id", vp.getVehicle().getId());
								q2.setParameter("st_dt", start_can.getTime());
								q2.setParameter("ed_dt", search_end_can.getTime());
								Object listObj = gDAO.search(q2, 0);
								if(listObj != null) {
									double start_odometer = 0, end_odometer = 0;
									boolean start = true;
									Vector<VehicleOdometerData> vodList = (Vector<VehicleOdometerData>)listObj;
									for(VehicleOdometerData e : vodList) {
										end_odometer = e.getOdometer();
										if(start) {
											start = false;
											start_odometer = e.getOdometer();
										}
									}
									mymileage_consumption += Math.abs(end_odometer-start_odometer);
								}
								if(cost.doubleValue() > 0) {
									total_cost = total_cost.add(cost);
									total_cost = total_cost.setScale(2);
									r[3] = cost.toPlainString();
									r[4] = "AdHoc";
									list.add(r);
								}
							}
						}
						
						String key = null;
						if(filterType.equals("byregion"))
							try {
								key = vp.getRegion().getName();
							} catch(Exception ex){}
						else if(filterType.equals("byfleet"))
							key = vp.getVehicle().getRegistrationNo();
						else if(filterType.equals("byenginecap"))
							try {
								key = vp.getVehicle().getEngineCapacity().getName();
							} catch(Exception ex){}
						else if(filterType.equals("bybrand"))
							key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
						
						if(key != null) {
							MaintenanceCostDS mds = new MaintenanceCostDS();
							MaintenanceCostDS.MaintCost mc = mds.new MaintCost();
							boolean exists = false;
							for(MaintenanceCostDS.MaintCost e : groupList) {
								if(e.getVehicleReg().equals(key)) {
									e.setDistance(e.getDistance() + mymileage_consumption);
									e.setAdhocCost(e.getAdhocCost() + adhocCost);
									e.setPartsReplacementCost(e.getPartsReplacementCost() + partsReplacementCost);
									e.setPartsServicingCost(e.getPartsServicingCost() + partsServicingCost);
									e.setRoutineCost(e.getRoutineCost() + routineCost);
									e.setTotalCost(e.getTotalCost() + totalCost);
									try {
									e.setCostPerKM(e.getDistance()/e.getTotalCost());
									} catch(Exception ex){}
									exists = true;
									break;
								}
							}
							if(!exists) {
								mc.setVehicleReg(key);
								mc.setDistance(mc.getDistance() + mymileage_consumption);
								mc.setAdhocCost(mc.getAdhocCost() + adhocCost);
								mc.setPartsReplacementCost(mc.getPartsReplacementCost() + partsReplacementCost);
								mc.setPartsServicingCost(mc.getPartsServicingCost() + partsServicingCost);
								mc.setRoutineCost(mc.getRoutineCost() + routineCost);
								mc.setTotalCost(mc.getTotalCost() + totalCost);
								try {
									mc.setCostPerKM(mc.getDistance()/mc.getTotalCost());
								} catch(Exception ex){}
								groupList.add(mc);
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
			}
			if(list.size() > 0) {
				setMaintCostReport(list);
				maintenanceDS = new MaintenanceCostDS();
				maintenanceDS.setNoOfAssets(vpList.size());
				maintenanceDS.setData(groupList);
				
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
	
	public void downloadMaintCostsPDF() {
		try {
			if(maintenanceDS != null) {
				GeneralDAO gDAO = new GeneralDAO();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
				Map<String, Object> parameters = new HashMap<String, Object>();
				if(filterType.equals("byregion")) {
					parameters.put("reportTitle", "Maintenance Cost Report, Regional Vehicles");
				}
				else if(filterType.equals("byfleet")) {
					parameters.put("reportTitle", "Maintenance Cost Report, Individual Vehicle");
				}
				else if(filterType.equals("byenginecap"))
					parameters.put("reportTitle", "Maintenance Cost Report, Engine Capacity");
				else if(filterType.equals("bybrand"))
					parameters.put("reportTitle", "Maintenance Cost Report, Vehicle Brands");
				parameters.put("reportDesc", "This is a summary report of vehicle maintenance costing.");
				parameters.put("preparedOn", sdf.format(new Date()));
				parameters.put("groupBy", getGroupBy());
				parameters.put("period", getPeriod());
				if(getRegion_id() > 0 && !filterType.equals("byfleet")) {
					Object obj = gDAO.find(Region.class, getRegion_id());
					if(obj != null) {
						Region r = (Region)obj;
						parameters.put("region", r.getName());
					} else
						parameters.put("region", "N/A");
				} else
					parameters.put("region", "All");
				if(getDivision_id() > 0 && !filterType.equals("byfleet")) {
					Object obj = gDAO.find(Division.class, getDivision_id());
					if(obj != null) {
						Division r = (Division)obj;
						parameters.put("branch", r.getName());
					} else
						parameters.put("branch", "N/A");
				} else
					parameters.put("branch", "All");
				if(getDepartment_id() > 0 && !filterType.equals("byfleet")) {
					Object obj = gDAO.find(Department.class, getDepartment_id());
					if(obj != null) {
						Department r = (Department)obj;
						parameters.put("department", r.getName());
					} else
						parameters.put("department", "N/A");
				} else
					parameters.put("department", "All");
				
				gDAO.destroy();
				
				double adhocCost=0, routineCost=0, totalCost=0, partsReplacementCost=0, partsServicingCost=0;
				double totalMileage=0, costPerKm=0, avgCostPerKm = 0;
				for(MaintenanceCostDS.MaintCost e : maintenanceDS.getCollectionList()) {
					adhocCost += e.getAdhocCost();
					routineCost += e.getRoutineCost();
					totalCost += e.getTotalCost();
					partsReplacementCost += e.getPartsReplacementCost();
					partsServicingCost += e.getPartsServicingCost();
					totalMileage += e.getDistance();
					costPerKm += e.getCostPerKM();
				}
				try {
					avgCostPerKm = costPerKm/maintenanceDS.getCollectionList().size();
				} catch(Exception ex){}
				
				parameters.put("totalsLabel1", "Ad-hoc cost");
				parameters.put("totalsValue1", ""+adhocCost);
				parameters.put("totalsLabel2", "Distance");
				parameters.put("totalsValue2", ""+totalMileage);
				parameters.put("totalsLabel3", "Routine Cost");
				parameters.put("totalsValue3", ""+routineCost);
				parameters.put("totalsLabel4", "Maintenance Cost");
				parameters.put("totalsValue4", ""+totalCost);
				parameters.put("totalsLabel5", "Servicing parts");
				parameters.put("totalsValue5", ""+partsServicingCost);
				parameters.put("totalsLabel6", "Replacing parts");
				parameters.put("totalsValue6", ""+partsReplacementCost);
				parameters.put("totalsLabel7", "");
				parameters.put("totalsValue7", "");
				
				parameters.put("summaryLabel1", "No. of Assets");
				parameters.put("summaryValue1", ""+maintenanceDS.getNoOfAssets());
				parameters.put("summaryLabel2", "Total Maintenance Cost");
				parameters.put("summaryValue2", ""+totalCost);
				parameters.put("summaryLabel3", "Average Maintenance Costs/Km");
				parameters.put("summaryValue3", ""+avgCostPerKm);
				
				JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(maintenanceDS.getCollectionList());
				
				downloadJasperPDF(parameters, "maintenance_costs.pdf", "/resources/jasper/maintenance_cost.jasper", ds);
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "Datasource is not available!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
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
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
			setPeriod(sdf.format(getStart_dt()) + " - " + sdf.format(getEnd_dt()));
		} catch(Exception ex){ ex.printStackTrace(); }
		
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm:ss");
		parameters.put("period", getPeriod()!=null ? getPeriod() : "");
		parameters.put("groupBy", getGroupBy()!=null ? getGroupBy() : "");
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
			if(getFleet_id() != null && getFleet_id() > 0) {
				try {
					f = (Fleet)gDAO.find(Fleet.class, getFleet_id());
				} catch(Exception ex){}
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
			if(getFleet_id() != null && getFleet_id() > 0) {
				try {
					f = (Fleet)gDAO.find(Fleet.class, getFleet_id());
				} catch(Exception ex){}
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
				for(VehicleAdHocMaintenance e : getAdhocmaints()) {
					try {
						q = gDAO.createQuery("Select e from WorkOrderVehicle e where e.workOrder.id=:wrk_id and e.vehicle.id=:v_id");
						q.setParameter("wrk_id", e.getWorkOrder().getId());
						q.setParameter("v_id", e.getVehicle().getId());
						Object obj = gDAO.search(q, 0);
						if(obj != null) {
							Vector<WorkOrderVehicle> objlist = (Vector<WorkOrderVehicle>)obj;
							for(WorkOrderVehicle wov : objlist)
								if(wov.getCurrentVehOdometer() != null)
									e.setOdometer(wov.getCurrentVehOdometer().doubleValue());
						}
						
						q = gDAO.createQuery("Select e from WorkOrderVendor e where e.workOrder.id=:wrk_id and e.vendor.id=:v_id");
						q.setParameter("wrk_id", e.getWorkOrder().getId());
						q.setParameter("v_id", e.getVendor().getId());
						obj = gDAO.search(q, 0);
						if(obj != null) {
							Vector<WorkOrderVendor> objlist = (Vector<WorkOrderVendor>)obj;
							for(WorkOrderVendor wov : objlist) {
								e.setFinalApproveBy(wov.getWorkOrder().getFinalApproveBy());
								e.setNegotiated_days_of_completion(wov.getNegotiated_days_of_completion());
							}
						}
					} catch(Exception ex){}
				}
				
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
			
			gDAO.destroy();
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
			if(getFleet_id() != null && getFleet_id() > 0) {
				Object fleetObj = gDAO.find(Fleet.class, getFleet_id());
				if(fleetObj != null) {
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
			if(getFleet_id() != null && getFleet_id() > 0) {
				Object fleetObj = gDAO.find(Fleet.class, getFleet_id());
				if(fleetObj != null) {
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
	
	public void searchDriverQueriesSummary() {
		// Name, No. of queries, years of service score, driver grade
		resetReportInfo();
		if(getPartner() != null && getStart_dt() != null && getEnd_dt() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Query q = gDAO.createQuery("Select e from PartnerDriver e where e.partner.id=:p_id and e.active=:active");
			q.setParameter("p_id", getPartner().getId());
			q.setParameter("active", true);
			// TODO: Load all drivers
			
			setDriverQueries(new Vector<PartnerDriverQuery>());
			
			String qry = "Select e from PartnerDriverQuery e where (e.tranDate between :stdt and :eddt) and e.driver.partner.id=:partner_id";
			if(getDriver_id() != null && getDriver_id() > 0)
				qry += " and e.driver.id = :driver_id";
			
			q = gDAO.createQuery(qry);
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
	
	public void searchDriverVehicleDriving() {
		resetReportInfo();
		
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null) {
			setCorTrips(null);
			
			GeneralDAO gDAO = new GeneralDAO();
			
			String str = "Select e from CorporateTrip e where (e.departureDateTime between :start_dt and :end_dt) and e.partner = :partner and e.driver.id > 0";
			
			Query q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			
			Object drvs = gDAO.search(q, 0);
			if(drvs != null) {
				setCorTrips((Vector<CorporateTrip>)drvs);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getCorTrips().size() + " trip(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Corporate Trip Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "No trip found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
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
			if(drvs != null) {
				setCorTrips((Vector<CorporateTrip>)drvs);
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", getCorTrips().size() + " trip(s) found!");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setReport_title("Corporate Trip Report");
				setReport_start_dt(getStart_dt().toLocaleString());
				setReport_end_dt(getEnd_dt().toLocaleString());
			} else {
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
			
			boolean fleet = false, veh = false, limit = false;
			if(getFleet_id() != null && getFleet_id() > 0) {
				str += " and e.vehicle.fleet.id = :fleet_id";
				fleet = true;
			}
			if(getVehicle_id() != null && getVehicle_id() > 0) {
				str += " and e.vehicle.id = :vehicle_id";
				veh = true;
			}
			if(flimit > 0)
			{
				str += " and e.litres >= :minlitres";
				limit = true;
			}
			
			Query q = gDAO.createQuery(str);
			q.setParameter("start_dt", getStart_dt());
			q.setParameter("end_dt", getEnd_dt());
			q.setParameter("partner", getPartner());
			if(fleet)
				q.setParameter("fleet_id", getFleet_id());
			if(veh)
				q.setParameter("vehicle_id", getVehicle_id());
			if(limit)
				q.setParameter("minlitres", flimit);
			
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
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null && getStype() != null) {
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("vehicle.partner.id", getPartner().getId());
			if(region_id > 0 && filterType.equals("byregion"))
				params.put("region.id", region_id);
			if(division_id > 0 && !filterType.equals("byfleet"))
				params.put("dept.division.id", division_id);
			if(department_id > 0 && !filterType.equals("byfleet"))
				params.put("dept.id", department_id);
			if(unit_id > 0 && !filterType.equals("byfleet"))
				params.put("unit.id", unit_id);
			if(fleet_id > 0)
				params.put("vehicle.fleet.id", fleet_id);
			if(engineCapacity_id > 0 && filterType.equals("byenginecap"))
				params.put("vehicle.engineCapacity.id", engineCapacity_id);
			if(brand_id > 0 && filterType.equals("bybrand"))
				params.put("vehicle.model.id", brand_id);
			Vector<VehicleParameters> vpList = null;
			Object vpObj = gDAO.search("VehicleParameters", params);
			if(vpObj != null) {
				vpList = (Vector<VehicleParameters>)vpObj;
				if(yearOfPurchase > 0 && filterType.equals("byyearofp")) {
					Vector<VehicleParameters> newList = new Vector<VehicleParameters>();
					for(VehicleParameters vp : vpList) {
						Date dop = vp.getVehicle().getPurchaseDate();
						if(dop != null) {
							int v_year = 1900 + dop.getYear();
							if(yearOfPurchase == v_year) {
								newList.add(vp);
							}
						}
					}
					vpList = newList;
				}
				Vector<VehicleParameters> newList = new Vector<VehicleParameters>();
				for(VehicleParameters vp : vpList) {
					boolean exists = false;
					for(int i=0; i<newList.size(); i++) {
						VehicleParameters e = newList.get(i);
						if(e.getVehicle().getRegistrationNo().equalsIgnoreCase(vp.getVehicle().getRegistrationNo())) {
							newList.set(i, e);
							exists = true;
							break;
						}
					}
					if(!exists)
						newList.add(vp);
				}
				vpList = newList;
			}
			
			Vector<FuelConsumption> groupList = new Vector<FuelConsumption>();
			Calendar start_can = Calendar.getInstance(), end_can = Calendar.getInstance();
			start_can.setTime(getStart_dt());
			end_can.setTime(getEnd_dt());
			int style = Calendar.LONG;
			Locale us = Locale.US;
			setFuelConsumptions(null);
			if(vpList != null) {
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
						
						for(VehicleParameters vp : vpList) {
							Query q = gDAO.createQuery("Select e from VehicleFuelData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
							q.setParameter("v_id", vp.getVehicle().getId());
							q.setParameter("st_dt", st_dt);
							q.setParameter("ed_dt", ed_dt);
							Object listObj = gDAO.search(q, 0);
							if(listObj != null) {
								double last_fuel_level = 0, myfuel_consumption = 0;
								Vector<VehicleFuelData> list = (Vector<VehicleFuelData>)listObj;
								for(VehicleFuelData e : list) {
									if(last_fuel_level > e.getFuelLevel()) {
										myfuel_consumption += last_fuel_level - e.getFuelLevel();
									}
									last_fuel_level = e.getFuelLevel();
								}
								if(myfuel_consumption > 0) {
									q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :vehicle_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
									q.setParameter("vehicle_id", vp.getVehicle().getId());
									q.setParameter("st_dt", getStart_dt());
									q.setParameter("ed_dt", getEnd_dt());
									Object obj = gDAO.search(q, 0);
									double distance = 0;
									if(obj != null) {
										Vector<VehicleOdometerData> vodlist = (Vector<VehicleOdometerData>)obj;
										double startOdometer = 0, endOdometer = 0;
										for(int i=0; i<vodlist.size(); i++) {
											VehicleOdometerData vod = vodlist.get(i);
											if(i == 0)
												startOdometer = vod.getOdometer();
											if(i == vodlist.size() - 1)
												endOdometer = vod.getOdometer();
										}
										BigDecimal distanceDeci = new BigDecimal(Math.abs(endOdometer - startOdometer));
										distanceDeci = distanceDeci.setScale(2, RoundingMode.HALF_UP);
										distance = distanceDeci.doubleValue();
									}
									String key = null;
									if(filterType.equals("byregion"))
										try {
											key = vp.getRegion().getName();
										} catch(Exception ex){}
									else if(filterType.equals("byfleet"))
										key = vp.getVehicle().getRegistrationNo();
									else if(filterType.equals("byenginecap"))
										try {
											key = vp.getVehicle().getEngineCapacity().getName();
										} catch(Exception ex){}
									else if(filterType.equals("bybrand"))
										key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
									
									if(key != null) {
										FuelConsumption fc2 = new FuelConsumption();
										boolean exists = false;
										// tripsCount, consumeRate, fuelCost, costPerKm
										for(FuelConsumption e : groupList) {
											if(e.getRegNo().equals(key) && e.getDate().equals(date)) {
												e.setDistance(e.getDistance() + distance);
												e.setLevel(e.getLevel() + myfuel_consumption);
												e.setNoOfVehicles(e.getNoOfVehicles()+1);
												exists = true;
												break;
											}
										}
										if(!exists) {
											fc2.setRegNo(key);
											fc2.setDate(date);
											fc2.setDistance(distance);
											fc2.setLevel(myfuel_consumption);
											fc2.setNoOfVehicles(1);
											groupList.add(fc2);
										}
									}
								}
							}
							q = gDAO.createQuery("Select e from VehicleTrackerEventData e where e.vehicle.id = :vehicle_id and (e.event_name = 'Ignition On' or e.event_name = 'Ignition Off') and (e.captured_dt between :st_dt and :ed_dt)");
							q.setParameter("vehicle_id", vp.getVehicle().getId());
							q.setParameter("st_dt", getStart_dt());
							q.setParameter("ed_dt", getEnd_dt());
							Object obj = gDAO.search(q, 0);
							if(obj != null) {
								int trips = 0;
								Vector<VehicleTrackerEventData> objList = (Vector<VehicleTrackerEventData>)obj;
								if(objList != null && objList.size() > 0) {
									for(VehicleTrackerEventData vted : objList) {
										if(vted.getEvent_name() != null && vted.getEvent_name().trim().equalsIgnoreCase("Ignition On"))
											trips += 1;
									}
									if(trips > 0) {
										String key = null;
										if(filterType.equals("byregion"))
											try {
												key = vp.getRegion().getName();
											} catch(Exception ex){}
										else if(filterType.equals("byfleet"))
											key = vp.getVehicle().getRegistrationNo();
										else if(filterType.equals("byenginecap"))
											try {
												key = vp.getVehicle().getEngineCapacity().getName();
											} catch(Exception ex){}
										else if(filterType.equals("bybrand"))
											key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
										if(key != null) {
											for(FuelConsumption e : groupList) {
												if(e.getRegNo().equals(key) && e.getDate().equals(date)) {
													e.setTripsCount(e.getTripsCount() + trips);
													break;
												}
											}
										}
									}
								}
							}
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
						
						for(VehicleParameters vp : vpList) {
							Query q = gDAO.createQuery("Select e from VehicleFuelData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
							q.setParameter("v_id", vp.getVehicle().getId());
							q.setParameter("st_dt", st_dt);
							q.setParameter("ed_dt", ed_dt);
							Object listObj = gDAO.search(q, 0);
							if(listObj != null) {
								double last_fuel_level = 0, myfuel_consumption = 0;
								Vector<VehicleFuelData> list = (Vector<VehicleFuelData>)listObj;
								for(VehicleFuelData e : list) {
									if(last_fuel_level > e.getFuelLevel()) {
										myfuel_consumption += last_fuel_level - e.getFuelLevel();
									}
									last_fuel_level = e.getFuelLevel();
								}
								if(myfuel_consumption > 0) {
									q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :vehicle_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
									q.setParameter("vehicle_id", vp.getVehicle().getId());
									q.setParameter("st_dt", getStart_dt());
									q.setParameter("ed_dt", getEnd_dt());
									Object obj = gDAO.search(q, 0);
									double distance = 0;
									if(obj != null) {
										Vector<VehicleOdometerData> vodlist = (Vector<VehicleOdometerData>)obj;
										double startOdometer = 0, endOdometer = 0;
										for(int i=0; i<vodlist.size(); i++) {
											VehicleOdometerData vod = vodlist.get(i);
											if(i == 0)
												startOdometer = vod.getOdometer();
											if(i == vodlist.size() - 1)
												endOdometer = vod.getOdometer();
										}
										BigDecimal distanceDeci = new BigDecimal(Math.abs(endOdometer - startOdometer));
										distanceDeci = distanceDeci.setScale(2, RoundingMode.HALF_UP);
										distance = distanceDeci.doubleValue();
									}
									String key = null;
									if(filterType.equals("byregion"))
										try {
											key = vp.getRegion().getName();
										} catch(Exception ex){}
									else if(filterType.equals("byfleet"))
										key = vp.getVehicle().getRegistrationNo();
									else if(filterType.equals("byenginecap"))
										try {
											key = vp.getVehicle().getEngineCapacity().getName();
										} catch(Exception ex){}
									else if(filterType.equals("bybrand"))
										key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
									
									if(key != null) {
										FuelConsumption fc2 = new FuelConsumption();
										boolean exists = false;
										for(FuelConsumption e : groupList) {
											if(e.getRegNo().equals(key) && e.getDate().equals(date)) {
												e.setDistance(e.getDistance() + distance);
												e.setLevel(e.getLevel() + myfuel_consumption);
												e.setNoOfVehicles(e.getNoOfVehicles()+1);
												exists = true;
												break;
											}
										}
										if(!exists) {
											fc2.setRegNo(key);
											fc2.setDate(date);
											fc2.setDistance(distance);
											fc2.setLevel(myfuel_consumption);
											fc2.setNoOfVehicles(1);
											groupList.add(fc2);
										}
									}
								}
							}
							q = gDAO.createQuery("Select e from VehicleTrackerEventData e where e.vehicle.id = :vehicle_id and (e.event_name = 'Ignition On' or e.event_name = 'Ignition Off') and (e.captured_dt between :st_dt and :ed_dt)");
							q.setParameter("vehicle_id", vp.getVehicle().getId());
							q.setParameter("st_dt", getStart_dt());
							q.setParameter("ed_dt", getEnd_dt());
							Object obj = gDAO.search(q, 0);
							if(obj != null) {
								int trips = 0;
								Vector<VehicleTrackerEventData> objList = (Vector<VehicleTrackerEventData>)obj;
								if(objList != null && objList.size() > 0) {
									for(VehicleTrackerEventData vted : objList) {
										if(vted.getEvent_name() != null && vted.getEvent_name().trim().equalsIgnoreCase("Ignition On"))
											trips += 1;
									}
									if(trips > 0) {
										String key = null;
										if(filterType.equals("byregion"))
											try {
												key = vp.getRegion().getName();
											} catch(Exception ex){}
										else if(filterType.equals("byfleet"))
											key = vp.getVehicle().getRegistrationNo();
										else if(filterType.equals("byenginecap"))
											try {
												key = vp.getVehicle().getEngineCapacity().getName();
											} catch(Exception ex){}
										else if(filterType.equals("bybrand"))
											key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
										if(key != null) {
											for(FuelConsumption e : groupList) {
												if(e.getRegNo().equals(key) && e.getDate().equals(date)) {
													e.setTripsCount(e.getTripsCount() + trips);
													break;
												}
											}
										}
									}
								}
							}
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
						
						for(VehicleParameters vp : vpList) {
							Query q = gDAO.createQuery("Select e from VehicleFuelData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
							q.setParameter("v_id", vp.getVehicle().getId());
							q.setParameter("st_dt", st_dt);
							q.setParameter("ed_dt", ed_dt);
							Object listObj = gDAO.search(q, 0);
							if(listObj != null) {
								double last_fuel_level = 0, myfuel_consumption = 0;
								Vector<VehicleFuelData> list = (Vector<VehicleFuelData>)listObj;
								for(VehicleFuelData e : list) {
									if(last_fuel_level > e.getFuelLevel()) {
										myfuel_consumption += last_fuel_level - e.getFuelLevel();
									}
									last_fuel_level = e.getFuelLevel();
								}
								if(myfuel_consumption > 0) {
									q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :vehicle_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
									q.setParameter("vehicle_id", vp.getVehicle().getId());
									q.setParameter("st_dt", getStart_dt());
									q.setParameter("ed_dt", getEnd_dt());
									Object obj = gDAO.search(q, 0);
									double distance = 0;
									if(obj != null) {
										Vector<VehicleOdometerData> vodlist = (Vector<VehicleOdometerData>)obj;
										double startOdometer = 0, endOdometer = 0;
										for(int i=0; i<vodlist.size(); i++) {
											VehicleOdometerData vod = vodlist.get(i);
											if(i == 0)
												startOdometer = vod.getOdometer();
											if(i == vodlist.size() - 1)
												endOdometer = vod.getOdometer();
										}
										BigDecimal distanceDeci = new BigDecimal(Math.abs(endOdometer - startOdometer));
										distanceDeci = distanceDeci.setScale(2, RoundingMode.HALF_UP);
										distance = distanceDeci.doubleValue();
									}
									String key = null;
									if(filterType.equals("byregion"))
										try {
											key = vp.getRegion().getName();
										} catch(Exception ex){}
									else if(filterType.equals("byfleet"))
										key = vp.getVehicle().getRegistrationNo();
									else if(filterType.equals("byenginecap"))
										try {
											key = vp.getVehicle().getEngineCapacity().getName();
										} catch(Exception ex){}
									else if(filterType.equals("bybrand"))
										key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
									
									if(key != null) {
										FuelConsumption fc2 = new FuelConsumption();
										boolean exists = false;
										for(FuelConsumption e : groupList) {
											if(e.getRegNo().equals(key) && e.getDate().equals(date)) {
												e.setDistance(e.getDistance() + distance);
												e.setLevel(e.getLevel() + myfuel_consumption);
												e.setNoOfVehicles(e.getNoOfVehicles()+1);
												exists = true;
												break;
											}
										}
										if(!exists) {
											fc2.setRegNo(key);
											fc2.setDate(date);
											fc2.setDistance(distance);
											fc2.setLevel(myfuel_consumption);
											fc2.setNoOfVehicles(1);
											groupList.add(fc2);
										}
									}
								}
							}
							q = gDAO.createQuery("Select e from VehicleTrackerEventData e where e.vehicle.id = :vehicle_id and (e.event_name = 'Ignition On' or e.event_name = 'Ignition Off') and (e.captured_dt between :st_dt and :ed_dt)");
							q.setParameter("vehicle_id", vp.getVehicle().getId());
							q.setParameter("st_dt", getStart_dt());
							q.setParameter("ed_dt", getEnd_dt());
							Object obj = gDAO.search(q, 0);
							if(obj != null) {
								int trips = 0;
								Vector<VehicleTrackerEventData> objList = (Vector<VehicleTrackerEventData>)obj;
								if(objList != null && objList.size() > 0) {
									for(VehicleTrackerEventData vted : objList) {
										if(vted.getEvent_name() != null && vted.getEvent_name().trim().equalsIgnoreCase("Ignition On"))
											trips += 1;
									}
									if(trips > 0) {
										String key = null;
										if(filterType.equals("byregion"))
											try {
												key = vp.getRegion().getName();
											} catch(Exception ex){}
										else if(filterType.equals("byfleet"))
											key = vp.getVehicle().getRegistrationNo();
										else if(filterType.equals("byenginecap"))
											try {
												key = vp.getVehicle().getEngineCapacity().getName();
											} catch(Exception ex){}
										else if(filterType.equals("bybrand"))
											key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
										if(key != null) {
											for(FuelConsumption e : groupList) {
												if(e.getRegNo().equals(key) && e.getDate().equals(date)) {
													e.setTripsCount(e.getTripsCount() + trips);
													break;
												}
											}
										}
									}
								}
							}
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
						
						for(VehicleParameters vp : vpList) {
							Query q = gDAO.createQuery("Select e from VehicleFuelData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
							q.setParameter("v_id", vp.getVehicle().getId());
							q.setParameter("st_dt", st_dt);
							q.setParameter("ed_dt", ed_dt);
							Object listObj = gDAO.search(q, 0);
							if(listObj != null) {
								double last_fuel_level = 0, myfuel_consumption = 0;
								Vector<VehicleFuelData> list = (Vector<VehicleFuelData>)listObj;
								for(VehicleFuelData e : list) {
									if(last_fuel_level > e.getFuelLevel()) {
										myfuel_consumption += last_fuel_level - e.getFuelLevel();
									}
									last_fuel_level = e.getFuelLevel();
								}
								if(myfuel_consumption > 0) {
									q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :vehicle_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
									q.setParameter("vehicle_id", vp.getVehicle().getId());
									q.setParameter("st_dt", getStart_dt());
									q.setParameter("ed_dt", getEnd_dt());
									Object obj = gDAO.search(q, 0);
									double distance = 0;
									if(obj != null) {
										Vector<VehicleOdometerData> vodlist = (Vector<VehicleOdometerData>)obj;
										double startOdometer = 0, endOdometer = 0;
										for(int i=0; i<vodlist.size(); i++) {
											VehicleOdometerData vod = vodlist.get(i);
											if(i == 0)
												startOdometer = vod.getOdometer();
											if(i == vodlist.size() - 1)
												endOdometer = vod.getOdometer();
										}
										BigDecimal distanceDeci = new BigDecimal(Math.abs(endOdometer - startOdometer));
										distanceDeci = distanceDeci.setScale(2, RoundingMode.HALF_UP);
										distance = distanceDeci.doubleValue();
									}
									String key = null;
									if(filterType.equals("byregion"))
										try {
											key = vp.getRegion().getName();
										} catch(Exception ex){}
									else if(filterType.equals("byfleet"))
										key = vp.getVehicle().getRegistrationNo();
									else if(filterType.equals("byenginecap"))
										try {
											key = vp.getVehicle().getEngineCapacity().getName();
										} catch(Exception ex){}
									else if(filterType.equals("bybrand"))
										key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
									
									if(key != null) {
										FuelConsumption fc2 = new FuelConsumption();
										boolean exists = false;
										for(FuelConsumption e : groupList) {
											if(e.getRegNo().equals(key) && e.getDate().equals(date)) {
												e.setDistance(e.getDistance() + distance);
												e.setLevel(e.getLevel() + myfuel_consumption);
												e.setNoOfVehicles(e.getNoOfVehicles()+1);
												exists = true;
												break;
											}
										}
										if(!exists) {
											fc2.setRegNo(key);
											fc2.setDate(date);
											fc2.setDistance(distance);
											fc2.setLevel(myfuel_consumption);
											fc2.setNoOfVehicles(1);
											groupList.add(fc2);
										}
									}
								}
							}
							q = gDAO.createQuery("Select e from VehicleTrackerEventData e where e.vehicle.id = :vehicle_id and (e.event_name = 'Ignition On' or e.event_name = 'Ignition Off') and (e.captured_dt between :st_dt and :ed_dt)");
							q.setParameter("vehicle_id", vp.getVehicle().getId());
							q.setParameter("st_dt", getStart_dt());
							q.setParameter("ed_dt", getEnd_dt());
							Object obj = gDAO.search(q, 0);
							if(obj != null) {
								int trips = 0;
								Vector<VehicleTrackerEventData> objList = (Vector<VehicleTrackerEventData>)obj;
								if(objList != null && objList.size() > 0) {
									for(VehicleTrackerEventData vted : objList) {
										if(vted.getEvent_name() != null && vted.getEvent_name().trim().equalsIgnoreCase("Ignition On"))
											trips += 1;
									}
									if(trips > 0) {
										String key = null;
										if(filterType.equals("byregion"))
											try {
												key = vp.getRegion().getName();
											} catch(Exception ex){}
										else if(filterType.equals("byfleet"))
											key = vp.getVehicle().getRegistrationNo();
										else if(filterType.equals("byenginecap"))
											try {
												key = vp.getVehicle().getEngineCapacity().getName();
											} catch(Exception ex){}
										else if(filterType.equals("bybrand"))
											key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
										if(key != null) {
											for(FuelConsumption e : groupList) {
												if(e.getRegNo().equals(key) && e.getDate().equals(date)) {
													e.setTripsCount(e.getTripsCount() + trips);
													break;
												}
											}
										}
									}
								}
							}
						}
					}
				}
				setFuelConsumptions(groupList);
			}
			gDAO.destroy();
			
			if(getFuelConsumptions().size() > 0) {
				if(getPieModel() == null)
					createPieModel();
				Hashtable<String, Number> pieData = new Hashtable<String, Number>();
				for(FuelConsumption fc : getFuelConsumptions()) {
					Number v = fc.getLevel();
					if(pieData.containsKey(fc.getRegNo()))
						v = v.doubleValue() + pieData.get(fc.getRegNo()).doubleValue();
					pieData.put(fc.getRegNo(), v);
				}
				getPieModel().setData(pieData);
				
				if(barModel == null)
					createBarModel();
				maxY = 0;
				ChartSeries distanceCoveredSeries = new ChartSeries(), fuelConsumedSeries = new ChartSeries();
				distanceCoveredSeries.setLabel("Distance covered");
				fuelConsumedSeries.setLabel("Fuel consumed");
				Hashtable<Object, Number> distanceCoveredData = new Hashtable<Object, Number>(), fuelConsumedDate = new Hashtable<Object, Number>();
				for(FuelConsumption fc : getFuelConsumptions()) {
					String key = fc.getRegNo();
					Number distance = fc.getDistance();
					Number fuel = fc.getLevel();
					if(distanceCoveredData.containsKey(key))
						distance = distance.doubleValue() + distanceCoveredData.get(key).doubleValue();
					if(fuelConsumedDate.containsKey(key))
						fuel = fuel.doubleValue() + fuelConsumedDate.get(key).doubleValue();
					distanceCoveredData.put(key, distance);
					fuelConsumedDate.put(key, fuel);
					
					if(distance.longValue() > maxY)
						maxY = distance.longValue() + 5;
					else if(fuel.longValue() > maxY)
						maxY = fuel.longValue() + 5;
				}
				distanceCoveredSeries.setData(distanceCoveredData);
				fuelConsumedSeries.setData(fuelConsumedDate);
				
				barModel.addSeries(distanceCoveredSeries);
				barModel.addSeries(fuelConsumedSeries);
			}
		}
	}
	
	public void downloadFuelConsumptionPDF() {
		try {
			FuelConsumptionDS fuelConsumptionDS = FuelConsumptionDS.getInstance();
			Vector<FuelConsumptionDS.Entry> data = new Vector<FuelConsumptionDS.Entry>();
			double distance = 0, trips = 0, avgFuelConsumed = 0, totalConsumed = 0, avgFuelCost=0, totalCost = 0;
			for(FuelConsumption fc : getFuelConsumptions()) {
				FuelConsumptionDS.Entry entry = fuelConsumptionDS. new Entry();
				entry.setDistanceCovered(fc.getDistance());
				distance+=fc.getDistance();
				entry.setNoOfVehicles(fc.getNoOfVehicles());
				entry.setTripsCount(fc.getTripsCount());
				trips+=fc.getTripsCount();
				entry.setVehicleReg(fc.getRegNo());
				entry.setFuelConsumed(fc.getLevel());
				totalConsumed += fc.getLevel();
				entry.setFuelCost(fc.getLevel() * 86.50);
				totalCost += entry.getFuelCost();
				try {
					entry.setConsumeRate(new BigDecimal(fc.getDistance()).divide(new BigDecimal(entry.getFuelConsumed()), RoundingMode.HALF_UP).doubleValue());
				} catch(Exception ex){}
				try {
					entry.setCostPerKm(new BigDecimal(fc.getDistance()).divide(new BigDecimal(fc.getFuelCost()), RoundingMode.HALF_UP).doubleValue());
				} catch(Exception ex){}
				data.add(entry);
			}
			fuelConsumptionDS.setData(data);
			try {
				avgFuelConsumed = new BigDecimal(distance).divide(new BigDecimal(totalConsumed), RoundingMode.HALF_UP).doubleValue();
			} catch(Exception ex){}
			try {
				
				avgFuelCost = new BigDecimal(distance).divide(new BigDecimal(totalCost), RoundingMode.HALF_UP).doubleValue();
			} catch(Exception ex){}
			
			GeneralDAO gDAO = new GeneralDAO();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
			Map<String, Object> parameters = new HashMap<String, Object>();
			if(filterType.equals("byregion")) {
				parameters.put("reportTitle", "Fuel Comsumption Report, Regional Vehicles");
				setGroupBy("Region");
			}
			else if(filterType.equals("byfleet")) {
				parameters.put("reportTitle", "Fuel Comsumption Report, Individual Vehicle");
				setGroupBy("Individual Vehicles");
			}
			else if(filterType.equals("byenginecap")) {
				parameters.put("reportTitle", "Fuel Comsumption Report, Engine Capacity");
				setGroupBy("Engine Capacity");
			}
			else if(filterType.equals("bybrand")) {
				parameters.put("reportTitle", "Fuel Comsumption Report, Vehicle Brands");
				setGroupBy("Vehicle Brand");
			}
			parameters.put("reportDesc", "This is a summary report of vehicle fuel consumption.");
			parameters.put("preparedOn", sdf.format(new Date()));
			parameters.put("groupBy", getGroupBy());
			parameters.put("period", getPeriod());
			if(getRegion_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Region.class, getRegion_id());
				if(obj != null) {
					Region r = (Region)obj;
					parameters.put("region", r.getName());
				} else
					parameters.put("region", "N/A");
			} else
				parameters.put("region", "All");
			if(getDivision_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Division.class, getDivision_id());
				if(obj != null) {
					Division r = (Division)obj;
					parameters.put("branch", r.getName());
				} else
					parameters.put("branch", "N/A");
			} else
				parameters.put("branch", "All");
			if(getDepartment_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Department.class, getDepartment_id());
				if(obj != null) {
					Department r = (Department)obj;
					parameters.put("department", r.getName());
				} else
					parameters.put("department", "N/A");
			} else
				parameters.put("department", "All");
			
			gDAO.destroy();
			
			parameters.put("totalsLabel1", "No of trips");
			parameters.put("totalsValue1", ""+trips);
			parameters.put("totalsLabel2", "Distance");
			parameters.put("totalsValue2", ""+distance);
			parameters.put("totalsLabel3", "Fuel Consumed");
			parameters.put("totalsValue3", ""+totalConsumed);
			parameters.put("totalsLabel4", "");
			parameters.put("totalsValue4", "");
			parameters.put("totalsLabel5", "");
			parameters.put("totalsValue5", "");
			parameters.put("totalsLabel6", "");
			parameters.put("totalsValue6", "");
			parameters.put("totalsLabel7", "");
			parameters.put("totalsValue7", "");
			
			parameters.put("summaryLabel1", "No of Assets");
			parameters.put("summaryValue1", ""+fuelConsumptionDS.getNoOfAssets());
			
			parameters.put("summaryLabel2", "Avg. Consumed");
			parameters.put("summaryValue2", ""+avgFuelConsumed);
			
			parameters.put("summaryLabel3", "Avg. Cost");
			parameters.put("summaryValue3", ""+avgFuelCost);
			
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(fuelConsumptionDS.getCollectionList());
			
			downloadJasperPDF(parameters, "fuel_consumption_report.pdf", "/resources/jasper/fuel_consumption.jasper", ds);
		} catch(Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void vehicleMileage() {
		if(getStart_dt() != null && getEnd_dt() != null && getPartner() != null && getStype() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("vehicle.partner.id", getPartner().getId());
			if(region_id > 0 && filterType.equals("byregion"))
				params.put("region.id", region_id);
			if(division_id > 0 && !filterType.equals("byfleet"))
				params.put("dept.division.id", division_id);
			if(department_id > 0 && !filterType.equals("byfleet"))
				params.put("dept.id", department_id);
			if(unit_id > 0 && !filterType.equals("byfleet"))
				params.put("unit.id", unit_id);
			if(fleet_id > 0)
				params.put("vehicle.fleet.id", fleet_id);
			if(engineCapacity_id > 0 && filterType.equals("byenginecap"))
				params.put("vehicle.engineCapacity.id", engineCapacity_id);
			if(brand_id > 0 && filterType.equals("bybrand"))
				params.put("vehicle.model.id", brand_id);
			Vector<VehicleParameters> vpList = null;
			Object vpObj = gDAO.search("VehicleParameters", params);
			if(vpObj != null) {
				vpList = (Vector<VehicleParameters>)vpObj;
				if(yearOfPurchase > 0 && filterType.equals("byyearofp")) {
					Vector<VehicleParameters> newList = new Vector<VehicleParameters>();
					for(VehicleParameters vp : vpList) {
						Date dop = vp.getVehicle().getPurchaseDate();
						if(dop != null) {
							int v_year = 1900 + dop.getYear();
							if(yearOfPurchase == v_year) {
								newList.add(vp);
							}
						}
					}
					vpList = newList;
				}
				Vector<VehicleParameters> newList = new Vector<VehicleParameters>();
				for(VehicleParameters vp : vpList) {
					boolean exists = false;
					for(int i=0; i<newList.size(); i++) {
						VehicleParameters e = newList.get(i);
						if(e.getVehicle().getRegistrationNo().equalsIgnoreCase(vp.getVehicle().getRegistrationNo())) {
							newList.set(i, e);
							exists = true;
							break;
						}
					}
					if(!exists)
						newList.add(vp);
				}
				vpList = newList;
			}
			
			Vector<FuelConsumption> groupList = new Vector<FuelConsumption>();
			
			Calendar start_can = Calendar.getInstance(), end_can = Calendar.getInstance();
			start_can.setTime(getStart_dt());
			end_can.setTime(getEnd_dt());
			int style = Calendar.LONG;
			Locale us = Locale.US;
			setMileageConsumptions(null);
			if(vpList != null) {
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
						
						for(VehicleParameters vp : vpList) {
							Query q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt) order by e.captured_dt");
							q.setParameter("v_id", vp.getVehicle().getId());
							q.setParameter("st_dt", st_dt);
							q.setParameter("ed_dt", ed_dt);
							Object listObj = gDAO.search(q, 0);
							if(listObj != null) {
								double start_odometer = 0, end_odometer = 0;
								boolean start = true;
								Vector<VehicleOdometerData> list = (Vector<VehicleOdometerData>)listObj;
								for(VehicleOdometerData e : list) {
									end_odometer = e.getOdometer();
									if(start) {
										start = false;
										start_odometer = e.getOdometer();
									}
								}
								double mymileage_consumption = Math.abs(end_odometer-start_odometer);
								if(mymileage_consumption > 0) {
									String key = null;
									if(filterType.equals("byregion"))
										try {
											key = vp.getRegion().getName();
										} catch(Exception ex){}
									else if(filterType.equals("byfleet"))
										key = vp.getVehicle().getRegistrationNo();
									else if(filterType.equals("byenginecap"))
										try {
											key = vp.getVehicle().getEngineCapacity().getName();
										} catch(Exception ex){}
									else if(filterType.equals("bybrand"))
										key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
									
									if(key != null) {
										FuelConsumption fc2 = new FuelConsumption();
										boolean exists = false;
										for(FuelConsumption e : groupList) {
											if(e.getRegNo().equals(key) && e.getDate().equals(date)) {
												e.setDistance(e.getDistance() + mymileage_consumption);
												e.setLevel(e.getLevel() + mymileage_consumption);
												e.setNoOfVehicles(e.getNoOfVehicles()+1);
												exists = true;
												break;
											}
										}
										if(!exists) {
											fc2.setRegNo(key);
											fc2.setDate(date);
											fc2.setDistance(mymileage_consumption);
											fc2.setLevel(mymileage_consumption);
											fc2.setNoOfVehicles(1);
											groupList.add(fc2);
										}
									}
								}
							}
							q = gDAO.createQuery("Select e from VehicleTrackerEventData e where e.vehicle.id = :vehicle_id and (e.event_name = 'Ignition On' or e.event_name = 'Ignition Off') and (e.captured_dt between :st_dt and :ed_dt)");
							q.setParameter("vehicle_id", vp.getVehicle().getId());
							q.setParameter("st_dt", getStart_dt());
							q.setParameter("ed_dt", getEnd_dt());
							Object obj = gDAO.search(q, 0);
							if(obj != null) {
								int trips = 0;
								Vector<VehicleTrackerEventData> objList = (Vector<VehicleTrackerEventData>)obj;
								if(objList != null && objList.size() > 0) {
									for(VehicleTrackerEventData vted : objList) {
										if(vted.getEvent_name() != null && vted.getEvent_name().trim().equalsIgnoreCase("Ignition On"))
											trips += 1;
									}
									if(trips > 0) {
										String key = null;
										if(filterType.equals("byregion"))
											try {
												key = vp.getRegion().getName();
											} catch(Exception ex){}
										else if(filterType.equals("byfleet"))
											key = vp.getVehicle().getRegistrationNo();
										else if(filterType.equals("byenginecap"))
											try {
												key = vp.getVehicle().getEngineCapacity().getName();
											} catch(Exception ex){}
										else if(filterType.equals("bybrand"))
											key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
										if(key != null) {
											for(FuelConsumption e : groupList) {
												if(e.getRegNo().equals(key) && e.getDate().equals(date)) {
													e.setTripsCount(e.getTripsCount() + trips);
													break;
												}
											}
										}
									}
								}
							}
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
						
						for(VehicleParameters vp : vpList) {
							Query q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
							q.setParameter("v_id", vp.getVehicle().getId());
							q.setParameter("st_dt", st_dt);
							q.setParameter("ed_dt", ed_dt);
							Object listObj = gDAO.search(q, 0);
							if(listObj != null) {
								double start_odometer = 0, end_odometer = 0;
								boolean start = true;
								Vector<VehicleOdometerData> list = (Vector<VehicleOdometerData>)listObj;
								for(VehicleOdometerData e : list) {
									end_odometer = e.getOdometer();
									if(start) {
										start = false;
										start_odometer = e.getOdometer();
									}
								}
								double mymileage_consumption = Math.abs(end_odometer-start_odometer);
								if(mymileage_consumption > 0) {
									String key = null;
									if(filterType.equals("byregion"))
										try {
											key = vp.getRegion().getName();
										} catch(Exception ex){}
									else if(filterType.equals("byfleet"))
										key = vp.getVehicle().getRegistrationNo();
									else if(filterType.equals("byenginecap"))
										try {
											key = vp.getVehicle().getEngineCapacity().getName();
										} catch(Exception ex){}
									else if(filterType.equals("bybrand"))
										key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
									
									if(key != null) {
										FuelConsumption fc2 = new FuelConsumption();
										boolean exists = false;
										for(FuelConsumption e : groupList) {
											if(e.getRegNo().equals(key) && e.getDate().equals(date)) {
												e.setDistance(e.getDistance() + mymileage_consumption);
												e.setLevel(e.getLevel() + mymileage_consumption);
												e.setNoOfVehicles(e.getNoOfVehicles()+1);
												exists = true;
												break;
											}
										}
										if(!exists) {
											fc2.setRegNo(key);
											fc2.setDate(date);
											fc2.setDistance(mymileage_consumption);
											fc2.setLevel(mymileage_consumption);
											fc2.setNoOfVehicles(1);
											groupList.add(fc2);
										}
									}
								}
							}
							q = gDAO.createQuery("Select e from VehicleTrackerEventData e where e.vehicle.id = :vehicle_id and (e.event_name = 'Ignition On' or e.event_name = 'Ignition Off') and (e.captured_dt between :st_dt and :ed_dt)");
							q.setParameter("vehicle_id", vp.getVehicle().getId());
							q.setParameter("st_dt", getStart_dt());
							q.setParameter("ed_dt", getEnd_dt());
							Object obj = gDAO.search(q, 0);
							if(obj != null) {
								int trips = 0;
								Vector<VehicleTrackerEventData> objList = (Vector<VehicleTrackerEventData>)obj;
								if(objList != null && objList.size() > 0) {
									for(VehicleTrackerEventData vted : objList) {
										if(vted.getEvent_name() != null && vted.getEvent_name().trim().equalsIgnoreCase("Ignition On"))
											trips += 1;
									}
									if(trips > 0) {
										String key = null;
										if(filterType.equals("byregion"))
											try {
												key = vp.getRegion().getName();
											} catch(Exception ex){}
										else if(filterType.equals("byfleet"))
											key = vp.getVehicle().getRegistrationNo();
										else if(filterType.equals("byenginecap"))
											try {
												key = vp.getVehicle().getEngineCapacity().getName();
											} catch(Exception ex){}
										else if(filterType.equals("bybrand"))
											key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
										if(key != null) {
											for(FuelConsumption e : groupList) {
												if(e.getRegNo().equals(key) && e.getDate().equals(date)) {
													e.setTripsCount(e.getTripsCount() + trips);
													break;
												}
											}
										}
									}
								}
							}
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
						
						for(VehicleParameters vp : vpList) {
							Query q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
							q.setParameter("v_id", vp.getVehicle().getId());
							q.setParameter("st_dt", st_dt);
							q.setParameter("ed_dt", ed_dt);
							Object listObj = gDAO.search(q, 0);
							if(listObj != null) {
								double start_odometer = 0, end_odometer = 0;
								boolean start = true;
								Vector<VehicleOdometerData> list = (Vector<VehicleOdometerData>)listObj;
								for(VehicleOdometerData e : list) {
									end_odometer = e.getOdometer();
									if(start) {
										start = false;
										start_odometer = e.getOdometer();
									}
								}
								double mymileage_consumption = Math.abs(end_odometer-start_odometer);
								if(mymileage_consumption > 0) {
									String key = null;
									if(filterType.equals("byregion"))
										try {
											key = vp.getRegion().getName();
										} catch(Exception ex){}
									else if(filterType.equals("byfleet"))
										key = vp.getVehicle().getRegistrationNo();
									else if(filterType.equals("byenginecap"))
										try {
											key = vp.getVehicle().getEngineCapacity().getName();
										} catch(Exception ex){}
									else if(filterType.equals("bybrand"))
										key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
									
									if(key != null) {
										FuelConsumption fc2 = new FuelConsumption();
										boolean exists = false;
										for(FuelConsumption e : groupList) {
											if(e.getRegNo().equals(key) && e.getDate().equals(date)) {
												e.setDistance(e.getDistance() + mymileage_consumption);
												e.setLevel(e.getLevel() + mymileage_consumption);
												e.setNoOfVehicles(e.getNoOfVehicles()+1);
												exists = true;
												break;
											}
										}
										if(!exists) {
											fc2.setRegNo(key);
											fc2.setDate(date);
											fc2.setDistance(mymileage_consumption);
											fc2.setLevel(mymileage_consumption);
											fc2.setNoOfVehicles(1);
											groupList.add(fc2);
										}
									}
								}
							}
							q = gDAO.createQuery("Select e from VehicleTrackerEventData e where e.vehicle.id = :vehicle_id and (e.event_name = 'Ignition On' or e.event_name = 'Ignition Off') and (e.captured_dt between :st_dt and :ed_dt)");
							q.setParameter("vehicle_id", vp.getVehicle().getId());
							q.setParameter("st_dt", getStart_dt());
							q.setParameter("ed_dt", getEnd_dt());
							Object obj = gDAO.search(q, 0);
							if(obj != null) {
								int trips = 0;
								Vector<VehicleTrackerEventData> objList = (Vector<VehicleTrackerEventData>)obj;
								if(objList != null && objList.size() > 0) {
									for(VehicleTrackerEventData vted : objList) {
										if(vted.getEvent_name() != null && vted.getEvent_name().trim().equalsIgnoreCase("Ignition On"))
											trips += 1;
									}
									if(trips > 0) {
										String key = null;
										if(filterType.equals("byregion"))
											try {
												key = vp.getRegion().getName();
											} catch(Exception ex){}
										else if(filterType.equals("byfleet"))
											key = vp.getVehicle().getRegistrationNo();
										else if(filterType.equals("byenginecap"))
											try {
												key = vp.getVehicle().getEngineCapacity().getName();
											} catch(Exception ex){}
										else if(filterType.equals("bybrand"))
											key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
										if(key != null) {
											for(FuelConsumption e : groupList) {
												if(e.getRegNo().equals(key) && e.getDate().equals(date)) {  
													e.setTripsCount(e.getTripsCount() + trips);
													break;
												}
											}
										}
									}
								}
							}
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
						
						for(VehicleParameters vp : vpList) {
							Query q = gDAO.createQuery("Select e from VehicleOdometerData e where e.vehicle.id = :v_id and (e.captured_dt between :st_dt and :ed_dt)");
							q.setParameter("v_id", vp.getVehicle().getId());
							q.setParameter("st_dt", st_dt);
							q.setParameter("ed_dt", ed_dt);
							Object listObj = gDAO.search(q, 0);
							if(listObj != null) {
								double start_odometer = 0, end_odometer = 0;
								boolean start = true;
								Vector<VehicleOdometerData> list = (Vector<VehicleOdometerData>)listObj;
								for(VehicleOdometerData e : list) {
									end_odometer = e.getOdometer();
									if(start) {
										start = false;
										start_odometer = e.getOdometer();
									}
								}
								double mymileage_consumption = Math.abs(end_odometer-start_odometer);
								if(mymileage_consumption > 0) {
									String key = null;
									if(filterType.equals("byregion"))
										try {
											key = vp.getRegion().getName();
										} catch(Exception ex){}
									else if(filterType.equals("byfleet"))
										key = vp.getVehicle().getRegistrationNo();
									else if(filterType.equals("byenginecap"))
										try {
											key = vp.getVehicle().getEngineCapacity().getName();
										} catch(Exception ex){}
									else if(filterType.equals("bybrand"))
										key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
									
									if(key != null) {
										FuelConsumption fc2 = new FuelConsumption();
										boolean exists = false;
										for(FuelConsumption e : groupList) {
											if(e.getRegNo().equals(key) && e.getDate().equals(date)) {
												e.setDistance(e.getDistance() + mymileage_consumption);
												e.setLevel(e.getLevel() + mymileage_consumption);
												e.setNoOfVehicles(e.getNoOfVehicles()+1);
												exists = true;
												break;
											}
										}
										if(!exists) {
											fc2.setRegNo(key);
											fc2.setDate(date);
											fc2.setDistance(mymileage_consumption);
											fc2.setLevel(mymileage_consumption);
											fc2.setNoOfVehicles(1);
											groupList.add(fc2);
										}
									}
								}
							}
							q = gDAO.createQuery("Select e from VehicleTrackerEventData e where e.vehicle.id = :vehicle_id and (e.event_name = 'Ignition On' or e.event_name = 'Ignition Off') and (e.captured_dt between :st_dt and :ed_dt)");
							q.setParameter("vehicle_id", vp.getVehicle().getId());
							q.setParameter("st_dt", getStart_dt());
							q.setParameter("ed_dt", getEnd_dt());
							Object obj = gDAO.search(q, 0);
							if(obj != null) {
								int trips = 0;
								Vector<VehicleTrackerEventData> objList = (Vector<VehicleTrackerEventData>)obj;
								if(objList != null && objList.size() > 0) {
									for(VehicleTrackerEventData vted : objList) {
										if(vted.getEvent_name() != null && vted.getEvent_name().trim().equalsIgnoreCase("Ignition On"))
											trips += 1;
									}
									if(trips > 0) {
										String key = null;
										if(filterType.equals("byregion"))
											try {
												key = vp.getRegion().getName();
											} catch(Exception ex){}
										else if(filterType.equals("byfleet"))
											key = vp.getVehicle().getRegistrationNo();
										else if(filterType.equals("byenginecap"))
											try {
												key = vp.getVehicle().getEngineCapacity().getName();
											} catch(Exception ex){}
										else if(filterType.equals("bybrand"))
											key = vp.getVehicle().getModel().getName()+"("+vp.getVehicle().getModel().getYear()+")";
										if(key != null) {
											for(FuelConsumption e : groupList) {
												if(e.getRegNo().equals(key) && e.getDate().equals(date)) {
													e.setTripsCount(e.getTripsCount() + trips);
													break;
												}
											}
										}
									}
								}
							}
						}
					}
				}
				setMileageConsumptions(groupList);
			}
			gDAO.destroy();
			
			if(getMileageConsumptions().size() > 0) {
				if(getPieModel() == null)
					createPieModel();
				Hashtable<String, Number> pieData = new Hashtable<String, Number>();
				for(FuelConsumption fc : getMileageConsumptions()) {
					Number v = fc.getLevel();
					if(pieData.containsKey(fc.getRegNo()))
						v = v.doubleValue() + pieData.get(fc.getRegNo()).doubleValue();
					pieData.put(fc.getRegNo(), v);
				}
				getPieModel().setData(pieData);
			}
		}
	}
	
	public void downloadVehicleMileagePDF() {
		try {
			MilleageDS milleageDS = MilleageDS.getInstance();
			Vector<MilleageDS.Entry> data = new Vector<MilleageDS.Entry>();
			double distance = 0, trips = 0, avgDistance = 0;
			double totalAsstes = 0;
			for(FuelConsumption fc : getMileageConsumptions()) {
				MilleageDS.Entry entry = milleageDS. new Entry();
				entry.setDistanceCovered(fc.getDistance());
				distance+=fc.getDistance();
				entry.setNoOfVehicles(fc.getNoOfVehicles());
				totalAsstes += entry.getNoOfVehicles();
				entry.setTripsCount(fc.getTripsCount());
				trips+=fc.getTripsCount();
				entry.setVehicleReg(fc.getRegNo());
				entry.setWorking_time(0);
				data.add(entry);
			}
			milleageDS.setData(data);
			try {
				avgDistance = new BigDecimal(distance).divide(new BigDecimal(getMileageConsumptions().size()), RoundingMode.HALF_UP).doubleValue();
			} catch(Exception ex){}
			
			GeneralDAO gDAO = new GeneralDAO();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
			Map<String, Object> parameters = new HashMap<String, Object>();
			if(filterType.equals("byregion")) {
				parameters.put("reportTitle", "Mileage Report, Regional Vehicles");
				setGroupBy("Region");
			}
			else if(filterType.equals("byfleet")) {
				parameters.put("reportTitle", "Mileage Report, Individual Vehicle");
				setGroupBy("Individual Vehicles");
			}
			else if(filterType.equals("byenginecap")) {
				parameters.put("reportTitle", "Mileage Report, Engine Capacity");
				setGroupBy("Engine Capacity");
			}
			else if(filterType.equals("bybrand")) {
				parameters.put("reportTitle", "Mileage Report, Vehicle Brands");
				setGroupBy("Vehicle Brand");
			}
			parameters.put("reportDesc", "This is a summary report of vehicle mileage.");
			parameters.put("preparedOn", sdf.format(new Date()));
			parameters.put("groupBy", getGroupBy());
			parameters.put("period", getPeriod());
			if(getRegion_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Region.class, getRegion_id());
				if(obj != null) {
					Region r = (Region)obj;
					parameters.put("region", r.getName());
				} else
					parameters.put("region", "N/A");
			} else
				parameters.put("region", "All");
			if(getDivision_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Division.class, getDivision_id());
				if(obj != null) {
					Division r = (Division)obj;
					parameters.put("branch", r.getName());
				} else
					parameters.put("branch", "N/A");
			} else
				parameters.put("branch", "All");
			if(getDepartment_id() > 0 && !filterType.equals("byfleet")) {
				Object obj = gDAO.find(Department.class, getDepartment_id());
				if(obj != null) {
					Department r = (Department)obj;
					parameters.put("department", r.getName());
				} else
					parameters.put("department", "N/A");
			} else
				parameters.put("department", "All");
			
			gDAO.destroy();
			
			parameters.put("totalsLabel1", "No of trips");
			parameters.put("totalsValue1", ""+trips);
			parameters.put("totalsLabel2", "Distance");
			parameters.put("totalsValue2", ""+distance);
			parameters.put("totalsLabel3", "");
			parameters.put("totalsValue3", "");
			parameters.put("totalsLabel4", "");
			parameters.put("totalsValue4", "");
			parameters.put("totalsLabel5", "");
			parameters.put("totalsValue5", "");
			parameters.put("totalsLabel6", "");
			parameters.put("totalsValue6", "");
			parameters.put("totalsLabel7", "");
			parameters.put("totalsValue7", "");
			
			parameters.put("summaryLabel1", "No of Assets");
			parameters.put("summaryValue1", ""+totalAsstes);
			
			parameters.put("summaryLabel2", "Average Distance");
			parameters.put("summaryValue2", ""+avgDistance);
			
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(milleageDS.getCollectionList());
			
			downloadJasperPDF(parameters, "milleage_report.pdf", "/resources/jasper/mileage.jasper", ds);
		} catch(Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: ", "The pdf generation failed!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void vehicleMileage_old() {
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
				GeneralDAO gDAO = new GeneralDAO();
				try {
					partner = (Partner)gDAO.find(Partner.class, getPartner_id());
				} catch(Exception ex){}
				gDAO.destroy();
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

	public int getYos() {
		return yos;
	}

	public void setYos(int yos) {
		this.yos = yos;
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

	public long getStaff_id() {
		return staff_id;
	}

	public void setStaff_id(long staff_id) {
		this.staff_id = staff_id;
	}

	public Long getEngineCapacity_id() {
		return engineCapacity_id;
	}

	public void setEngineCapacity_id(Long engineCapacity_id) {
		this.engineCapacity_id = engineCapacity_id;
	}

	public Long getBrand_id() {
		return brand_id;
	}

	public void setBrand_id(Long brand_id) {
		this.brand_id = brand_id;
	}

	public long getYearOfPurchase() {
		return yearOfPurchase;
	}

	public void setYearOfPurchase(long yearOfPurchase) {
		this.yearOfPurchase = yearOfPurchase;
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

	public String getAction_taken() {
		return action_taken;
	}

	public void setAction_taken(String action_taken) {
		this.action_taken = action_taken;
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

	public double getFlimit() {
		return flimit;
	}

	public void setFlimit(double flimit) {
		this.flimit = flimit;
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

	public Vector<FuelConsumption> getFleetCost() {
		if(fleetCost == null)
			fleetCost = new Vector<FuelConsumption>();
		return fleetCost;
	}

	public void setFleetCost(Vector<FuelConsumption> fleetCost) {
		this.fleetCost = fleetCost;
	}

	@SuppressWarnings("unchecked")
	public Vector<Vehicle> getVehiclesDrivingInfoReport() {
		if(vehiclesDrivingInfoReport == null)
		{
			vehiclesDrivingInfoReport = new Vector<Vehicle>();
			
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("vehicle.partner.id", getPartner().getId());
				if(region_id > 0)
					params.put("region.id", region_id);
				if(division_id > 0)
					params.put("dept.division.id", division_id);
				if(department_id > 0)
					params.put("dept.id", department_id);
				if(unit_id > 0)
					params.put("unit.id", unit_id);
				if(fleet_id > 0)
					params.put("vehicle.fleet.id", fleet_id);
				
				Object obj = gDAO.search("VehicleParameters", params);
				if(obj != null) {
					Vector<VehicleParameters> vpList = (Vector<VehicleParameters>)obj;
					Vector<Vehicle> vList = new Vector<Vehicle>();
					for(VehicleParameters vp : vpList) {
						boolean exists = false;
						for(Vehicle v : vList) {
							if(v.getId().longValue() == vp.getVehicle().getId().longValue()) {
								exists = true;
								break;
							}
						}
						if(!exists)
							vList.add(vp.getVehicle());
					}
					vehiclesDrivingInfoReport = vList;
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
				if(getFleet_id() != null && getFleet_id() > 0)
					qry += " and e.vehicle.fleet.id=:fleet_id";
				if(getVehicle_id() != null && getVehicle_id() > 0)
					qry += " and e.vehicle.id=:vehicle_id";
				
				Query q = gDAO.createQuery(qry);
				q.setParameter("partner", getPartner());
				q.setParameter("stdt", getStart_dt());
				q.setParameter("eddt", getEnd_dt());
				
				if(getFleet_id() != null && getFleet_id() > 0)
					q.setParameter("fleet_id", getFleet_id());
				if(getVehicle_id() != null && getVehicle_id() > 0)
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
				if(getFleet_id() != null && getFleet_id() > 0)
					qry += " and e.vehicle.fleet.id=:fleet_id";
				if(getVehicle_id() != null && getVehicle_id() > 0)
					qry += " and e.vehicle.id=:vehicle_id";
				
				q = gDAO.createQuery(qry);
				q.setParameter("partner", getPartner());
				q.setParameter("stdt", getStart_dt());
				q.setParameter("eddt", getEnd_dt());
				
				if(getFleet_id() != null && getFleet_id() > 0)
					q.setParameter("fleet_id", getFleet_id());
				if(getVehicle_id() != null && getVehicle_id() > 0)
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
				if(getFleet_id() != null && getFleet_id() > 0)
					qry += " and e.vehicle.fleet.id=:fleet_id";
				if(getVehicle_id() != null && getVehicle_id() > 0)
					qry += " and e.vehicle.id=:vehicle_id";
				
				q = gDAO.createQuery(qry);
				q.setParameter("partner", getPartner());
				q.setParameter("stdt", getStart_dt());
				q.setParameter("eddt", getEnd_dt());
				
				if(getFleet_id() != null && getFleet_id() > 0)
					q.setParameter("fleet_id", getFleet_id());
				if(getVehicle_id() != null && getVehicle_id() > 0)
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

	public Vector<DriverLicense> getDriverLicenseRenewal() {
		return driverLicenseRenewal;
	}

	public void setDriverLicenseRenewal(Vector<DriverLicense> driverLicenseRenewal) {
		this.driverLicenseRenewal = driverLicenseRenewal;
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

	public SpeedSummary getSpeedSummary() {
		return speedSummary;
	}

	public void setSpeedSummary(SpeedSummary speedSummary) {
		this.speedSummary = speedSummary;
	}

	public UtilizationReportDS getUtilazationReport() {
		return utilazationReport;
	}

	public void setUtilazationReport(UtilizationReportDS utilazationReport) {
		this.utilazationReport = utilazationReport;
	}

	public AssetEfficiencyReportDS getAssetEfficiencyReport() {
		return assetEfficiencyReport;
	}

	public void setAssetEfficiencyReport(
			AssetEfficiencyReportDS assetEfficiencyReport) {
		this.assetEfficiencyReport = assetEfficiencyReport;
	}

	public SalesEfficiencyReportDS getSalesEfficiencyReport() {
		return salesEfficiencyReport;
	}

	public void setSalesEfficiencyReport(
			SalesEfficiencyReportDS salesEfficiencyReport) {
		this.salesEfficiencyReport = salesEfficiencyReport;
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
	
	public String getFilterType() {
		return filterType;
	}

	public void setFilterType(String filterType) {
		this.filterType = filterType;
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

	public MaintenanceCostDS getMaintenanceDS() {
		return maintenanceDS;
	}

	public void setMaintenanceDS(MaintenanceCostDS maintenanceDS) {
		this.maintenanceDS = maintenanceDS;
	}
	
}
