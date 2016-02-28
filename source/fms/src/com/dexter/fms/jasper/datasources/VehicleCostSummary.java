package com.dexter.fms.jasper.datasources;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Vector;

public class VehicleCostSummary {
	public class Cost {
		private String vehicleReg;
		private double distance, maintCost, fuelCost, otherCost, totalCost;
		
		public Cost(){}

		public String getVehicleReg() {
			return vehicleReg;
		}

		public void setVehicleReg(String vehicleReg) {
			this.vehicleReg = vehicleReg;
		}

		public double getDistance() {
			return new BigDecimal(distance).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}

		public void setDistance(double distance) {
			this.distance = distance;
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

		public double getTotalCost() {
			return new BigDecimal(totalCost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}

		public void setTotalCost(double totalCost) {
			this.totalCost = totalCost;
		}
		
	}
	private static VehicleCostSummary me;
	public static VehicleCostSummary getInstance() {
		if(me == null)
			me = new VehicleCostSummary();
		return me;
	}
	private static Collection<Cost> collection = new Vector<Cost>();
	
	static {
		//collection.add(getInstance().new MaintCost("BDG805CE", "2016-01-18 08:22:30", "2016-01-19 00:00:00", 10000, "AdHoc"));
	}
	
	public static void setData(Vector<Cost> data) {
		collection = data;
	}
	
	public static Object[] getCollectionArray() {
		return collection.toArray();
	}
	
	public static Collection<Cost> getCollectionList() {
		return collection;
	}
}
