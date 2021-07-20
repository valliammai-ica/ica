package com.rac.hremp.objects;

import java.io.Serializable;

public class Company implements Serializable {

	private static final long serialVersionUID = 1L;
	public String code;
	public String description;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
