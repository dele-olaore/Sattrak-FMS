package com.dexter.fms.jersey.response;

import java.util.List;

public class FMSRestListResponse extends FMSRestResponse {
	private List<Entity> list;
	
	public FMSRestListResponse() {}
	
	public class Entity {
		private String entity, type, description, date, requestBy;
		public Entity(){}
		public String getEntity() {
			return entity;
		}
		public void setEntity(String entity) {
			this.entity = entity;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getDate() {
			return date;
		}
		public void setDate(String date) {
			this.date = date;
		}
		public String getRequestBy() {
			return requestBy;
		}
		public void setRequestBy(String requestBy) {
			this.requestBy = requestBy;
		}
	}

	public List<Entity> getList() {
		return list;
	}

	public void setList(List<Entity> list) {
		this.list = list;
	}
	
}
