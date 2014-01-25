package com.dexter.fms.model.ref;

public enum SubLicenseTypeEnum
{
	VEHICLE_LICENSE("VEHICLE LICENSE"), PREMIUM_INSURANCE("PREMIUM INSURANCE"), THIRD_PARTY_INSURANCE("THIRD-PARTY INSURANCE"), DRIVER_LICENSE("DRIVER LICENSE"), ROAD_WORTHINESS("ROAD WORTHINESS"), HACKNEY_PERMIT("HACKNEY PERMIT");
	private String subLicType;
	
	private SubLicenseTypeEnum(String subLicType)
	{
		this.subLicType = subLicType;
	}
	
	public String getSubLicType()
	{
		return subLicType;
	}
}
