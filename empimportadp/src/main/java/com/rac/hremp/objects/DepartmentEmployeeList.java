package com.rac.hremp.objects;

import java.io.Serializable;
import java.util.List;

public class DepartmentEmployeeList implements Serializable {

	private static final long serialVersionUID = 1L;
	public List<Employee> employeeList;
	
	public List<Employee> getDepartmentEmployeeList() {
		return employeeList;
	}
	public void setDepartmentEmployeeList(List<Employee> employeeList) {
		this.employeeList = employeeList;
	}
	
}
