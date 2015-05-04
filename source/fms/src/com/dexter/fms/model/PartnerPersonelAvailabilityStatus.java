package com.dexter.fms.model;

public enum PartnerPersonelAvailabilityStatus
{
	AVAILABLE("AVAILABLE"), ON_LEAVE("ON LEAVE"), SUSPENDED("SUSPENDED"), NOT_AVAILABLE("NOT AVAILABLE"), SACKED("SACKED"), RESIGNED("RESIGNED");
	private String status;
	
	private PartnerPersonelAvailabilityStatus(String status)
	{
		this.status = status;
	}
	
	public String getStatus()
	{
		return status;
	}
}
