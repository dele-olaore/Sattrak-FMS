package com.dexter.fms.jersey.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.dexter.fms.jersey.response.FMSRestResponse;

@Path("/")
public class FMSRestService
{
	public FMSRestService()
	{}
	
	@GET
    @Produces("application/json")
	@Path("available")
	public FMSRestResponse available()
	{
		return new FMSRestResponse();
	}
}
