package com.rac.hremp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HRDataServiceConfig {
	
	@Value("${hremp.username}")
	public String username;
	
	@Value("${hremp.password}")
	public String password;
	
	@Value("${hremp.server}")
	public String server;
	
	@Value("${hremp.client_id}")
	public String client_id;
	
	@Value("${hremp.correlation_id}")
	public String correlation_id;
	
	@Value("${hremp.user_id}")
	public String user_id;
	
	@Value("${hremp.rd.department.type.codes}")
	public String rdDepartmentTypeCodes;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getClient_id() {
		return client_id;
	}

	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}

	public String getCorrelation_id() {
		return correlation_id;
	}

	public void setCorrelation_id(String correlation_id) {
		this.correlation_id = correlation_id;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getRdDepartmentTypeCodes() {
		return rdDepartmentTypeCodes;
	}

	public void setRdDepartmentTypeCodes(String rdDepartmentTypeCodes) {
		this.rdDepartmentTypeCodes = rdDepartmentTypeCodes;
	}

}
