package com.dexter.fms.model.ref;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class TransactionType implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
	private Long id;
	
	private String name;
	
	public TransactionType()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
