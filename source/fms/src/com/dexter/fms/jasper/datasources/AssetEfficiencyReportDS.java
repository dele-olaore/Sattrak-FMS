package com.dexter.fms.jasper.datasources;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Vector;

public class AssetEfficiencyReportDS {
	private int noOfAssets, totalPercentAvailability;
	public class Entry {
		private String vehicleReg;
		private int tripsCount, ageOfVehicle;
		private double distanceCovered, fuelConsumed, avgBrandKmPerLiter, vehicleKmPerLiter, brandMaintCostPerKm, vehicleMaintCostPerKm;
		private double avgCostPerKm, vehicleCostPerKm, percentAvailability, standardWorktime, actualWorkingTime, maintCost, totalCost;
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
		public int getAgeOfVehicle() {
			return ageOfVehicle;
		}
		public void setAgeOfVehicle(int ageOfVehicle) {
			this.ageOfVehicle = ageOfVehicle;
		}
		public double getDistanceCovered() {
			return new BigDecimal(distanceCovered).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setDistanceCovered(double distanceCovered) {
			this.distanceCovered = distanceCovered;
		}
		public double getFuelConsumed() {
			return new BigDecimal(fuelConsumed).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setFuelConsumed(double fuelConsumed) {
			this.fuelConsumed = fuelConsumed;
		}
		public double getAvgBrandKmPerLiter() {
			return new BigDecimal(avgBrandKmPerLiter).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setAvgBrandKmPerLiter(double avgBrandKmPerLiter) {
			this.avgBrandKmPerLiter = avgBrandKmPerLiter;
		}
		public double getVehicleKmPerLiter() {
			return new BigDecimal(vehicleKmPerLiter).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setVehicleKmPerLiter(double vehicleKmPerLiter) {
			this.vehicleKmPerLiter = vehicleKmPerLiter;
		}
		public double getBrandMaintCostPerKm() {
			return new BigDecimal(brandMaintCostPerKm).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setBrandMaintCostPerKm(double brandMaintCostPerKm) {
			this.brandMaintCostPerKm = brandMaintCostPerKm;
		}
		public double getVehicleMaintCostPerKm() {
			return new BigDecimal(vehicleMaintCostPerKm).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setVehicleMaintCostPerKm(double vehicleMaintCostPerKm) {
			this.vehicleMaintCostPerKm = vehicleMaintCostPerKm;
		}
		public double getAvgCostPerKm() {
			return new BigDecimal(avgCostPerKm).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setAvgCostPerKm(double avgCostPerKm) {
			this.avgCostPerKm = avgCostPerKm;
		}
		public double getVehicleCostPerKm() {
			return new BigDecimal(vehicleCostPerKm).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setVehicleCostPerKm(double vehicleCostPerKm) {
			this.vehicleCostPerKm = vehicleCostPerKm;
		}
		public double getPercentAvailability() {
			return new BigDecimal(percentAvailability).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setPercentAvailability(double percentAvailability) {
			this.percentAvailability = percentAvailability;
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
		public double getMaintCost() {
			return new BigDecimal(maintCost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setMaintCost(double maintCost) {
			this.maintCost = maintCost;
		}
		public double getTotalCost() {
			return new BigDecimal(totalCost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setTotalCost(double totalCost) {
			this.totalCost = totalCost;
		}
	}
	
	public AssetEfficiencyReportDS(){}
	
	private static AssetEfficiencyReportDS me;
	public static AssetEfficiencyReportDS getInstance() {
		if(me == null)
			me = new AssetEfficiencyReportDS();
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

	public int getTotalPercentAvailability() {
		return totalPercentAvailability;
	}

	public void setTotalPercentAvailability(int totalPercentAvailability) {
		this.totalPercentAvailability = totalPercentAvailability;
	}
}
