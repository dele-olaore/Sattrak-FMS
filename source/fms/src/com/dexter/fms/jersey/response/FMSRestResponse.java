package com.dexter.fms.jersey.response;

public class FMSRestResponse {
	private boolean success;
	private String message;
	
	public FMSRestResponse() {}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
