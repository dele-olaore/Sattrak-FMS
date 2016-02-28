package com.dexter.fms.jasper.datasources;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Vector;

public class MaintenanceCostDS {
	private int noOfAssets;
	public class MaintCost {
		private String vehicleReg, start_dt, end_dt;
		private double adhocCost, routineCost, totalCost, partsReplacementCost, partsServicingCost, distance, costPerKM, maintCost, fuelCost, otherCost;
		public MaintCost() {}
		
		public String getVehicleReg() {
			return vehicleReg;
		}
		public void setVehicleReg(String vehicleReg) {
			this.vehicleReg = vehicleReg;
		}
		public String getStart_dt() {
			return start_dt;
		}
		public void setStart_dt(String start_dt) {
			this.start_dt = start_dt;
		}
		public String getEnd_dt() {
			return end_dt;
		}
		public void setEnd_dt(String end_dt) {
			this.end_dt = end_dt;
		}
		public double getAdhocCost() {
			return new BigDecimal(adhocCost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setAdhocCost(double adhocCost) {
			this.adhocCost = adhocCost;
		}
		public double getRoutineCost() {
			return new BigDecimal(routineCost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setRoutineCost(double routineCost) {
			this.routineCost = routineCost;
		}
		public double getTotalCost() {
			return new BigDecimal(totalCost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setTotalCost(double totalCost) {
			this.totalCost = totalCost;
		}
		public double getPartsReplacementCost() {
			return new BigDecimal(partsReplacementCost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setPartsReplacementCost(double partsReplacementCost) {
			this.partsReplacementCost = partsReplacementCost;
		}
		public double getPartsServicingCost() {
			return new BigDecimal(partsServicingCost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setPartsServicingCost(double partsServicingCost) {
			this.partsServicingCost = partsServicingCost;
		}
		public double getDistance() {
			return new BigDecimal(distance).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setDistance(double distance) {
			this.distance = distance;
		}
		public double getCostPerKM() {
			return new BigDecimal(costPerKM).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setCostPerKM(double costPerKM) {
			this.costPerKM = costPerKM;
		}
		public double getMaintCost() {
			return new BigDecimal(maintCost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setMaintCost(double maintCost) {
			this.maintCost = maintCost;
		}
		public double getFuelCost() {
			return new BigDecimal(fuelCost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setFuelCost(double fuelCost) {
			this.fuelCost = fuelCost;
		}
		public double getOtherCost() {
			return new BigDecimal(otherCost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setOtherCost(double otherCost) {
			this.otherCost = otherCost;
		}
	}
	
	public MaintenanceCostDS(){}
	
	private static MaintenanceCostDS me;
	public static MaintenanceCostDS getInstance() {
		if(me == null)
			me = new MaintenanceCostDS();
		return me;
	}
	private static Collection<MaintCost> collection = new Vector<MaintCost>();
	
	public static void setData(Vector<MaintCost> data) {
		collection = data;
	}
	
	public static Object[] getCollectionArray() {
		return collection.toArray();
	}
	
	public static Collection<MaintCost> getCollectionList() {
		return collection;
	}

	public Collection<MaintCost> getCollection() {
		return collection;
	}

	public void setCollection(Collection<MaintCost> collection) {
		this.collection = collection;
	}

	public int getNoOfAssets() {
		return noOfAssets;
	}

	public void setNoOfAssets(int noOfAssets) {
		this.noOfAssets = noOfAssets;
	}
	
}
