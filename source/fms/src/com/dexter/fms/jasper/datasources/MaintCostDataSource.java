package com.dexter.fms.jasper.datasources;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Vector;

public class MaintCostDataSource {
	public class MaintCost {
		// Reg Number | Start Date | End Date | Cost | Type(Routine and AdHoc, Routine, AdHoc)
		private String regNumber, startDate, endDate, type, costStr;
		private double cost;
		private int count;

		public MaintCost(String regNumber, String startDate, String endDate, double cost, String type) {
			this.regNumber = regNumber;
			this.startDate = startDate;
			this.endDate = endDate;
			setCost(cost);
			this.type = type;
		}
		
		public MaintCost(String regNumber, String startDate, String endDate, double cost, String type, int count) {
			this.regNumber = regNumber;
			this.startDate = startDate;
			this.endDate = endDate;
			setCost(cost);
			this.type = type;
			this.count = count;
		}
		
		public String getRegNumber() {
			return regNumber;
		}

		public void setRegNumber(String regNumber) {
			this.regNumber = regNumber;
		}

		public String getStartDate() {
			return startDate;
		}

		public void setStartDate(String startDate) {
			this.startDate = startDate;
		}

		public String getEndDate() {
			return endDate;
		}

		public void setEndDate(String endDate) {
			this.endDate = endDate;
		}

		public String getCostStr() {
			return costStr;
		}

		public void setCostStr(String costStr) {
			this.costStr = costStr;
		}

		public double getCost() {
			return new BigDecimal(cost).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}

		public void setCost(double cost) {
			this.cost = cost;
			NumberFormat nf = NumberFormat.getNumberInstance();
			costStr = "N " + nf.format(cost);
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}
	}
	
	private static MaintCostDataSource me;
	public static MaintCostDataSource getInstance() {
		if(me == null)
			me = new MaintCostDataSource();
		return me;
	}
	private static Collection<MaintCost> collection = new Vector<MaintCost>();
	
	static {
		collection.add(getInstance().new MaintCost("BDG805CE", "2016-01-18 08:22:30", "2016-01-19 00:00:00", 10000, "AdHoc"));
	}
	
	public static void setData(Vector<MaintCost> data) {
		collection = data;
	}
	
	public static Object[] getCollectionArray() {
		return collection.toArray();
	}
	
	public static Collection<MaintCost> getCollectionList() {
		return collection;
	}
}
