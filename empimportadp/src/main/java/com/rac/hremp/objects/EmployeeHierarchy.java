package com.rac.hremp.objects;

import java.io.Serializable;
import java.util.List;

public class EmployeeHierarchy implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String employeeNumber;
	public String supervisorEmployeeNumber;
	public String level;
	public String branch;
	
	public String getEmployeeNumber() {
		return employeeNumber;
	}
	public void setEmployeeNumber(String employeeNumber) {
		this.employeeNumber = employeeNumber;
	}
	public String getSupervisorEmployeeNumber() {
		return supervisorEmployeeNumber;
	}
	public void setSupervisorEmployeeNumber(String supervisorEmployeeNumber) {
		this.supervisorEmployeeNumber = supervisorEmployeeNumber;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}

}
