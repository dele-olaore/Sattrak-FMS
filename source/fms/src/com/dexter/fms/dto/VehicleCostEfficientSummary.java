package com.dexter.fms.dto;

import java.util.Vector;

import com.dexter.fms.model.app.Vehicle;

public class VehicleCostEfficientSummary {
	private double distance_covered, maint_cost, fuel_cost, other_cost;
	private Vector<Vehicle> vehicles;
	
	public VehicleCostEfficientSummary() {}

	public double getDistance_covered() {
		return distance_covered;
	}

	public void setDistance_covered(double distance_covered) {
		this.distance_covered = distance_covered;
	}

	public double getMaint_cost() {
		return maint_cost;
	}

	public void setMaint_cost(double maint_cost) {
		this.maint_cost = maint_cost;
	}

	public double getFuel_cost() {
		return fuel_cost;
	}

	public void setFuel_cost(double fuel_cost) {
		this.fuel_cost = fuel_cost;
	}

	public double getOther_cost() {
		return other_cost;
	}

	public void setOther_cost(double other_cost) {
		this.other_cost = other_cost;
	}

	public Vector<Vehicle> getVehicles() {
		return vehicles;
	}

	public void setVehicles(Vector<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}
	
}
