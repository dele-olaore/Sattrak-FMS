package com.dexter.fms.dto;

public class FuelConsumption {
	private String date;
	private String regNo; // this should be the vehicle reg or the region name or fleet name or engine capacity, as required
	private int noOfVehicles, tripsCount;
	private double level, distance, consumeRate, fuelCost, costPerKm;
	
	private double maintCost, driverCost, licenseCost, otherCost;
	
	private double kmPerLitre, stdKmPerLitre, totalStdKmPerLitre;
	private long working_time;
	
	public FuelConsumption() {}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getRegNo() {
		return regNo;
	}

	public void setRegNo(String regNo) {
		this.regNo = regNo;
	}

	public int getNoOfVehicles() {
		return noOfVehicles;
	}

	public void setNoOfVehicles(int noOfVehicles) {
		this.noOfVehicles = noOfVehicles;
	}

	public int getTripsCount() {
		return tripsCount;
	}

	public void setTripsCount(int tripsCount) {
		this.tripsCount = tripsCount;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getLevel() {
		return level;
	}

	public void setLevel(double level) {
		this.level = level;
	}

	public double getConsumeRate() {
		return consumeRate;
	}

	public void setConsumeRate(double consumeRate) {
		this.consumeRate = consumeRate;
	}

	public double getFuelCost() {
		return fuelCost;
	}

	public void setFuelCost(double fuelCost) {
		this.fuelCost = fuelCost;
	}

	public double getCostPerKm() {
		return costPerKm;
	}

	public void setCostPerKm(double costPerKm) {
		this.costPerKm = costPerKm;
	}

	public double getMaintCost() {
		return maintCost;
	}

	public void setMaintCost(double maintCost) {
		this.maintCost = maintCost;
	}

	public double getDriverCost() {
		return driverCost;
	}

	public void setDriverCost(double driverCost) {
		this.driverCost = driverCost;
	}

	public double getLicenseCost() {
		return licenseCost;
	}

	public void setLicenseCost(double licenseCost) {
		this.licenseCost = licenseCost;
	}

	public double getOtherCost() {
		return otherCost;
	}

	public void setOtherCost(double otherCost) {
		this.otherCost = otherCost;
	}

	public double getKmPerLitre() {
		return kmPerLitre;
	}

	public void setKmPerLitre(double kmPerLitre) {
		this.kmPerLitre = kmPerLitre;
	}

	public double getStdKmPerLitre() {
		return stdKmPerLitre;
	}

	public void setStdKmPerLitre(double stdKmPerLitre) {
		this.stdKmPerLitre = stdKmPerLitre;
	}

	public double getTotalStdKmPerLitre() {
		return totalStdKmPerLitre;
	}

	public void setTotalStdKmPerLitre(double totalStdKmPerLitre) {
		this.totalStdKmPerLitre = totalStdKmPerLitre;
	}

	public long getWorking_time() {
		return working_time;
	}

	public void setWorking_time(long working_time) {
		this.working_time = working_time;
	}
	
}
