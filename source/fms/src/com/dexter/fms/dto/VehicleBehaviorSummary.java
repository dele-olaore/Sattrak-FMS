package com.dexter.fms.dto;

import java.util.Vector;

import com.dexter.fms.model.app.VehicleBehaviour;

public class VehicleBehaviorSummary {
	private String warningName;
	private long count, score;
	
	private Vector<VehicleBehaviour> vehicleWarnings;
	
	public VehicleBehaviorSummary() {}

	public String getWarningName() {
		return warningName;
	}

	public void setWarningName(String warningName) {
		this.warningName = warningName;
	}

	public long getCount() {
		count = getVehicleWarnings().size();
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public long getScore() {
		score = 0;
		for(VehicleBehaviour e : getVehicleWarnings()) {
			score += e.getWarningValue();
		}
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public Vector<VehicleBehaviour> getVehicleWarnings() {
		if(vehicleWarnings == null)
			vehicleWarnings = new Vector<VehicleBehaviour>();
		return vehicleWarnings;
	}

	public void setVehicleWarnings(Vector<VehicleBehaviour> vehicleWarnings) {
		this.vehicleWarnings = vehicleWarnings;
	}
	
}
