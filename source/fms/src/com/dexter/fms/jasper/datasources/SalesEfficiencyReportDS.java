package com.dexter.fms.jasper.datasources;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Vector;

public class SalesEfficiencyReportDS {
	private int noOfAssets;
	private double avgCostPerKm, avgCostPerHour;
	public class Entry {
		private String vehicleReg;
		private int tripsCount;
		private double distanceCovered, fuelConsumed, revenue, cost, costPerHour, costPerKm, engineHours;
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
		public double getRevenue() {
			return new BigDecimal(revenue).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setRevenue(double revenue) {
			this.revenue = revenue;
		}
		public double getCost() {
			return new BigDecimal(cost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setCost(double cost) {
			this.cost = cost;
		}
		public double getCostPerHour() {
			return new BigDecimal(costPerHour).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setCostPerHour(double costPerHour) {
			this.costPerHour = costPerHour;
		}
		public double getCostPerKm() {
			return new BigDecimal(costPerKm).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setCostPerKm(double costPerKm) {
			this.costPerKm = costPerKm;
		}
		public double getEngineHours() {
			return new BigDecimal(engineHours).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setEngineHours(double engineHours) {
			this.engineHours = engineHours;
		}
	}
	
	public SalesEfficiencyReportDS(){}
	
	private static SalesEfficiencyReportDS me;
	public static SalesEfficiencyReportDS getInstance() {
		if(me == null)
			me = new SalesEfficiencyReportDS();
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

	public double getAvgCostPerKm() {
		return new BigDecimal(avgCostPerKm).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	public void setAvgCostPerKm(double avgCostPerKm) {
		this.avgCostPerKm = avgCostPerKm;
	}

	public double getAvgCostPerHour() {
		return new BigDecimal(avgCostPerHour).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	public void setAvgCostPerHour(double avgCostPerHour) {
		this.avgCostPerHour = avgCostPerHour;
	}
}
