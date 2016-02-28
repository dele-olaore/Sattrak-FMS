package com.dexter.fms.jasper.datasources;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Vector;

public class MilleageDS {
	private int noOfAssets;
	public class Entry {
		private String vehicleReg;
		private int tripsCount;
		private double distanceCovered, working_time, noOfVehicles;
		public Entry() {}
		public String getVehicleReg() {
			return vehicleReg;
		}
		public void setVehicleReg(String vehicleReg) {
			this.vehicleReg = vehicleReg;
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
		public double getWorking_time() {
			return new BigDecimal(working_time).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		public void setWorking_time(double working_time) {
			this.working_time = working_time;
		}
		public double getNoOfVehicles() {
			return noOfVehicles;
		}
		public void setNoOfVehicles(double noOfVehicles) {
			this.noOfVehicles = noOfVehicles;
		}
	}
	
	public MilleageDS(){}
	
	private static MilleageDS me;
	public static MilleageDS getInstance() {
		if(me == null)
			me = new MilleageDS();
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
