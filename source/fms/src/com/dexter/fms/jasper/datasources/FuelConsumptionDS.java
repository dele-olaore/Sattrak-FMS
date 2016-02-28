package com.dexter.fms.jasper.datasources;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Vector;

public class FuelConsumptionDS {
	private int noOfAssets;
	public class Entry {
		private String vehicleReg;
		private int tripsCount;
		private double fuelConsumed, distanceCovered, noOfVehicles, consumeRate, fuelCost, costPerKm;
		public Entry() {}
		public String getVehicleReg() {
			return vehicleReg;
		}
		public void setVehicleReg(String vehicleReg) {
			this.vehicleReg = vehicleReg;
		}
		public double getFuelConsumed() {
			return new BigDecimal(fuelConsumed).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setFuelConsumed(double fuelConsumed) {
			this.fuelConsumed = fuelConsumed;
		}
		public double getDistanceCovered() {
			return new BigDecimal(distanceCovered).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setDistanceCovered(double distanceCovered) {
			this.distanceCovered = distanceCovered;
		}
		public int getTripsCount() {
			return tripsCount;
		}
		public void setTripsCount(int tripsCount) {
			this.tripsCount = tripsCount;
		}
		public double getNoOfVehicles() {
			return noOfVehicles;
		}
		public void setNoOfVehicles(double noOfVehicles) {
			this.noOfVehicles = noOfVehicles;
		}
		public double getConsumeRate() {
			return new BigDecimal(consumeRate).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setConsumeRate(double consumeRate) {
			this.consumeRate = consumeRate;
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
	
	public FuelConsumptionDS(){}
	
	private static FuelConsumptionDS me;
	public static FuelConsumptionDS getInstance() {
		if(me == null)
			me = new FuelConsumptionDS();
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
