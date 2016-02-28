package com.dexter.fms.dto;

import java.util.Vector;

import com.dexter.fms.model.app.Vehicle;

public class VehicleUtilizationSummary {
	private int totalCount, inuse, notinuse, totalAccidented, totalInWorkshop;
	private Vector<Vehicle> vehicles, inuseList, notinuseList, accidentedList, inworkshopList;
	
	public VehicleUtilizationSummary() {}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getInuse() {
		return inuse;
	}

	public void setInuse(int inuse) {
		this.inuse = inuse;
	}

	public int getNotinuse() {
		return notinuse;
	}

	public void setNotinuse(int notinuse) {
		this.notinuse = notinuse;
	}

	public int getTotalAccidented() {
		return totalAccidented;
	}

	public void setTotalAccidented(int totalAccidented) {
		this.totalAccidented = totalAccidented;
	}

	public int getTotalInWorkshop() {
		return totalInWorkshop;
	}

	public void setTotalInWorkshop(int totalInWorkshop) {
		this.totalInWorkshop = totalInWorkshop;
	}

	public Vector<Vehicle> getVehicles() {
		return vehicles;
	}

	public void setVehicles(Vector<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public Vector<Vehicle> getInuseList() {
		return inuseList;
	}

	public void setInuseList(Vector<Vehicle> inuseList) {
		this.inuseList = inuseList;
	}

	public Vector<Vehicle> getNotinuseList() {
		return notinuseList;
	}

	public void setNotinuseList(Vector<Vehicle> notinuseList) {
		this.notinuseList = notinuseList;
	}

	public Vector<Vehicle> getAccidentedList() {
		return accidentedList;
	}

	public void setAccidentedList(Vector<Vehicle> accidentedList) {
		this.accidentedList = accidentedList;
	}

	public Vector<Vehicle> getInworkshopList() {
		return inworkshopList;
	}

	public void setInworkshopList(Vector<Vehicle> inworkshopList) {
		this.inworkshopList = inworkshopList;
	}
	
}
