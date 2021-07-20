package com.rac.hremp.objects;

import java.io.Serializable;

public class EmployeeStatus implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String code;
	public String description;
	public String effectiveStartDate;	//YYYY-MM-DD
	public String effectiveEndDate;		//YYYY-MM-DD default is 7777=07-07
	
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
	public String getEffectiveStartDate() {
		return effectiveStartDate;
	}
	public void setEffectiveStartDate(String effectiveStartDate) {
		this.effectiveStartDate = effectiveStartDate;
	}
	public String getEffectiveEndDate() {
		return effectiveEndDate;
	}
	public void setEffectiveEndDate(String effectiveEndDate) {
		this.effectiveEndDate = effectiveEndDate;
	}
	
}
