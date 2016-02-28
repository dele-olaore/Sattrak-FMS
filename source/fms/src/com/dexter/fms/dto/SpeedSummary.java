package com.dexter.fms.dto;

import java.util.Vector;

import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleSpeedData;

public class SpeedSummary {
	private int thirtyKmCount, nintyKmCount, aboveKmCount;
	private Vector<Vehicle> vehicles;
	private Vector<VehicleSpeedData> thirtyKmList, nintyKmList, aboveKmList, allKmList;
	
	public SpeedSummary(){}

	public Vector<Vehicle> getVehicles() {
		return vehicles;
	}

	public void setVehicles(Vector<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public int getThirtyKmCount() {
		return thirtyKmCount;
	}

	public void setThirtyKmCount(int thirtyKmCount) {
		this.thirtyKmCount = thirtyKmCount;
	}

	public int getNintyKmCount() {
		return nintyKmCount;
	}

	public void setNintyKmCount(int nintyKmCount) {
		this.nintyKmCount = nintyKmCount;
	}

	public int getAboveKmCount() {
		return aboveKmCount;
	}

	public void setAboveKmCount(int aboveKmCount) {
		this.aboveKmCount = aboveKmCount;
	}

	public Vector<VehicleSpeedData> getThirtyKmList() {
		return thirtyKmList;
	}

	public void setThirtyKmList(Vector<VehicleSpeedData> thirtyKmList) {
		this.thirtyKmList = thirtyKmList;
	}

	public Vector<VehicleSpeedData> getNintyKmList() {
		return nintyKmList;
	}

	public void setNintyKmList(Vector<VehicleSpeedData> nintyKmList) {
		this.nintyKmList = nintyKmList;
	}

	public Vector<VehicleSpeedData> getAboveKmList() {
		return aboveKmList;
	}

	public void setAboveKmList(Vector<VehicleSpeedData> aboveKmList) {
		this.aboveKmList = aboveKmList;
	}

	public Vector<VehicleSpeedData> getAllKmList() {
		return allKmList;
	}

	public void setAllKmList(Vector<VehicleSpeedData> allKmList) {
		this.allKmList = allKmList;
	}

}
