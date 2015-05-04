package com.dexter.fms.model.ref;

public enum RentalDurationEnum
{
	HOUR("HOUR"), DAY("DAY"), WEEK("WEEK"), MONTH("MONTH"), YEAR("YEAR");
	private String durationType;
	
	private RentalDurationEnum(String durationType)
	{
		this.durationType = durationType;
	}
	
	public String getDurationType()
	{
		return durationType;
	}
}
