package com.rac.hremp.objects;

import java.io.Serializable;

public class GLDistributionCompany implements Serializable {

	private static final long serialVersionUID = 1L;
	public String code;
	public String description;
	public String glCompanyCode;
	
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
	public String getGlCompanyCode() {
		return glCompanyCode;
	}
	public void setGlCompanyCode(String glCompanyCode) {
		this.glCompanyCode = glCompanyCode;
	}
	
}
