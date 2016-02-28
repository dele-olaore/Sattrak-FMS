package com.dexter.fms.jasper.datasources;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Vector;

public class UtilizationReportDS {
	private int noOfAssets, totalPercentUtil, avgPercentUtil;
	public class Entry {
		private String vehicleReg;
		private int tripsCount;
		private double standardWorktime, actualWorkingTime, standardEngineHours, actualEngineHours, drivingTime, distanceCovered, noOfVehicles, costPerKm, percentUtil;
		private double currentOdometer;
		public Entry() {}
		public String getVehicleReg() {
			return vehicleReg;
		}
		public void setVehicleReg(String vehicleReg) {
			this.vehicleReg = vehicleReg;
		}
		public int getTripsCount() {
			return tripsCount;
		}
		public void setTripsCount(int tripsCount) {
			this.tripsCount = tripsCount;
		}
		public double getStandardWorktime() {
			return new BigDecimal(standardWorktime).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setStandardWorktime(double standardWorktime) {
			this.standardWorktime = standardWorktime;
		}
		public double getActualWorkingTime() {
			return new BigDecimal(actualWorkingTime).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setActualWorkingTime(double actualWorkingTime) {
			this.actualWorkingTime = actualWorkingTime;
		}
		public double getStandardEngineHours() {
			return new BigDecimal(standardEngineHours).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setStandardEngineHours(double standardEngineHours) {
			this.standardEngineHours = standardEngineHours;
		}
		public double getActualEngineHours() {
			return new BigDecimal(actualEngineHours).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setActualEngineHours(double actualEngineHours) {
			this.actualEngineHours = actualEngineHours;
		}
		public double getDrivingTime() {
			return new BigDecimal(drivingTime).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setDrivingTime(double drivingTime) {
			this.drivingTime = drivingTime;
		}
		public double getDistanceCovered() {
			return new BigDecimal(distanceCovered).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setDistanceCovered(double distanceCovered) {
			this.distanceCovered = distanceCovered;
		}
		public double getNoOfVehicles() {
			return noOfVehicles;
		}
		public void setNoOfVehicles(double noOfVehicles) {
			this.noOfVehicles = noOfVehicles;
		}
		public double getCostPerKm() {
			return new BigDecimal(costPerKm).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setCostPerKm(double costPerKm) {
			this.costPerKm = costPerKm;
		}
		public double getPercentUtil() {
			return new BigDecimal(percentUtil).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setPercentUtil(double percentUtil) {
			this.percentUtil = percentUtil;
		}
		public double getCurrentOdometer() {
			return new BigDecimal(currentOdometer).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setCurrentOdometer(double currentOdometer) {
			this.currentOdometer = currentOdometer;
		}
	}
	
	public UtilizationReportDS(){}
	
	private static UtilizationReportDS me;
	public static UtilizationReportDS getInstance() {
		if(me == null)
			me = new UtilizationReportDS();
		return me;
	}
	private static Collection<Entry> collection = new Vector<Entry>();
	
	public static void setData(Vector<Entry> data) {
		collection = data;
	}
	
	public static Object[] getCollectionArray() {
		return collection.toArray();
	}
	
	public static Collection<Entry> getCollectionList() {
		return collection;
	}

	public Collection<Entry> getCollection() {
		return collection;
	}

	public void setCollection(Collection<Entry> collection) {
		this.collection = collection;
	}

	public int getNoOfAssets() {
		return noOfAssets;
	}

	public void setNoOfAssets(int noOfAssets) {
		this.noOfAssets = noOfAssets;
	}

	public int getTotalPercentUtil() {
		return totalPercentUtil;
	}

	public void setTotalPercentUtil(int totalPercentUtil) {
		this.totalPercentUtil = totalPercentUtil;
	}

	public int getAvgPercentUtil() {
		return avgPercentUtil;
	}

	public void setAvgPercentUtil(int avgPercentUtil) {
		this.avgPercentUtil = avgPercentUtil;
	}
	
}
