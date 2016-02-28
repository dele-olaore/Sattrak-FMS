package com.dexter.fms.jasper.datasources;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Vector;

public class UtilizationDataSource {
	public class Utilization {
		private String vehicleReg, status;
		private double distanceCovered, avgSpeed, maxSpeed, fuel_consumed, working_time, km_per_liter;
		private int tripsCount, no_of_passengers;
		
		public Utilization(){}

		public String getVehicleReg() {
			return vehicleReg;
		}

		public void setVehicleReg(String vehicleReg) {
			this.vehicleReg = vehicleReg;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public double getDistanceCovered() {
			return new BigDecimal(distanceCovered).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}

		public void setDistanceCovered(double distanceCovered) {
			this.distanceCovered = distanceCovered;
		}

		public double getAvgSpeed() {
			return new BigDecimal(avgSpeed).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}

		public void setAvgSpeed(double avgSpeed) {
			this.avgSpeed = avgSpeed;
		}

		public double getMaxSpeed() {
			return new BigDecimal(maxSpeed).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}

		public void setMaxSpeed(double maxSpeed) {
			this.maxSpeed = maxSpeed;
		}

		public int getTripsCount() {
			return tripsCount;
		}

		public void setTripsCount(int tripsCount) {
			this.tripsCount = tripsCount;
		}

		public double getFuel_consumed() {
			return new BigDecimal(fuel_consumed).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}

		public void setFuel_consumed(double fuel_consumed) {
			this.fuel_consumed = fuel_consumed;
		}

		public double getWorking_time() {
			return new BigDecimal(working_time).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}

		public void setWorking_time(double working_time) {
			this.working_time = working_time;
		}

		public double getKm_per_liter() {
			return new BigDecimal(km_per_liter).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}

		public void setKm_per_liter(double km_per_liter) {
			this.km_per_liter = km_per_liter;
		}

		public int getNo_of_passengers() {
			return no_of_passengers;
		}

		public void setNo_of_passengers(int no_of_passengers) {
			this.no_of_passengers = no_of_passengers;
		}
		
	}
	
	private static UtilizationDataSource me;
	public static UtilizationDataSource getInstance() {
		if(me == null)
			me = new UtilizationDataSource();
		return me;
	}
	private static Collection<Utilization> collection = new Vector<Utilization>();
	
	static {
		//collection.add(getInstance().new MaintCost("BDG805CE", "2016-01-18 08:22:30", "2016-01-19 00:00:00", 10000, "AdHoc"));
	}
	
	public static void setData(Vector<Utilization> data) {
		collection = data;
	}
	
	public static Object[] getCollectionArray() {
		return collection.toArray();
	}
	
	public static Collection<Utilization> getCollectionList() {
		return collection;
	}
}
