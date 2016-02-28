package com.dexter.fms.jasper.datasources;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Vector;

public class FleetCostDS {
	private int noOfAssets;
	public class Entry {
		private String vehicleReg;
		private double maintCost, distanceCovered, tripsCount, noOfVehicles, driverCost, fuelCost, costPerKm;
		public Entry() {}
		public String getVehicleReg() {
			return vehicleReg;
		}
		public void setVehicleReg(String vehicleReg) {
			this.vehicleReg = vehicleReg;
		}
		public double getMaintCost() {
			return new BigDecimal(maintCost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setMaintCost(double maintCost) {
			this.maintCost = maintCost;
		}
		public double getDriverCost() {
			return new BigDecimal(driverCost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setDriverCost(double driverCost) {
			this.driverCost = driverCost;
		}
		public double getDistanceCovered() {
			return new BigDecimal(distanceCovered).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setDistanceCovered(double distanceCovered) {
			this.distanceCovered = distanceCovered;
		}
		public double getTripsCount() {
			return tripsCount;
		}
		public void setTripsCount(double tripsCount) {
			this.tripsCount = tripsCount;
		}
		public double getNoOfVehicles() {
			return noOfVehicles;
		}
		public void setNoOfVehicles(double noOfVehicles) {
			this.noOfVehicles = noOfVehicles;
		}
		public double getFuelCost() {
			return new BigDecimal(fuelCost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setFuelCost(double fuelCost) {
			this.fuelCost = fuelCost;
		}
		public double getCostPerKm() {
			return new BigDecimal(costPerKm).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setCostPerKm(double costPerKm) {
			this.costPerKm = costPerKm;
		}
	}
	
	public FleetCostDS(){}
	
	private static FleetCostDS me;
	public static FleetCostDS getInstance() {
		if(me == null)
			me = new FleetCostDS();
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
}
