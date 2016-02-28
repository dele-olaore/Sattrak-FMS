package com.dexter.fms.dto;

import java.util.Vector;

import com.dexter.fms.model.app.Vehicle;

public class CorporateTripsSummary {
	private int no_of_trips, no_of_passengers, no_of_drivers;
	private double distance, fuel_consumed, km_per_liter, working_time;
	private Vector<Vehicle> vehicles;
	
	public CorporateTripsSummary() {}

	public int getNo_of_trips() {
		return no_of_trips;
	}

	public void setNo_of_trips(int no_of_trips) {
		this.no_of_trips = no_of_trips;
	}

	public int getNo_of_passengers() {
		return no_of_passengers;
	}

	public void setNo_of_passengers(int no_of_passengers) {
		this.no_of_passengers = no_of_passengers;
	}

	public int getNo_of_drivers() {
		return no_of_drivers;
	}

	public void setNo_of_drivers(int no_of_drivers) {
		this.no_of_drivers = no_of_drivers;
	}

	public double getWorking_time() {
		return working_time;
	}

	public void setWorking_time(double working_time) {
		this.working_time = working_time;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getFuel_consumed() {
		return fuel_consumed;
	}

	public void setFuel_consumed(double fuel_consumed) {
		this.fuel_consumed = fuel_consumed;
	}

	public double getKm_per_liter() {
		return km_per_liter;
	}

	public void setKm_per_liter(double km_per_liter) {
		this.km_per_liter = km_per_liter;
	}

	public Vector<Vehicle> getVehicles() {
		return vehicles;
	}

	public void setVehicles(Vector<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}
	
}
