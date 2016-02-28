package com.dexter.fms.dto;

import java.util.Vector;

import com.dexter.fms.model.app.VehicleTrackerEventData;

public class TrackerEventSummary
{
	private String eventName;
	private long count;
	
	private Vector<VehicleTrackerEventData> trackerEventData;
	
	public TrackerEventSummary() {}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public long getCount() {
		count = getTrackerEventData().size();
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public Vector<VehicleTrackerEventData> getTrackerEventData() {
		if(trackerEventData == null)
			trackerEventData = new Vector<VehicleTrackerEventData>();
		return trackerEventData;
	}

	public void setTrackerEventData(Vector<VehicleTrackerEventData> trackerEventData) {
		this.trackerEventData = trackerEventData;
	}

}
