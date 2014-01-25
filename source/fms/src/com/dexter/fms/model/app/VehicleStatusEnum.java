package com.dexter.fms.model.app;

public enum VehicleStatusEnum
{
	ACTIVE("ACTIVE"), UNDER_MAINTENANCE("UNDER MAINTENANCE"), SOLD_DISPOSED("SOLD/DISPOSED"), GROUNDED("GROUNDED"), STOLEN("STOLEN"), STOLEN_REPLACED("STOLEN & REPLACED"), STOLEN_RECOVERED("STOLEN & RECOVERED"), ACCIDENTED("ACCIDENTED");
	private String status;
	
	private VehicleStatusEnum(String status)
	{
		this.status = status;
	}
	
	public String getStatus()
	{
		return status;
	}
}
