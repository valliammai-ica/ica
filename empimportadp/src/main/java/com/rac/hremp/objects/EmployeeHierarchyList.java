package com.rac.hremp.objects;

import java.io.Serializable;
import java.util.List;

public class EmployeeHierarchyList implements Serializable {

	private static final long serialVersionUID = 1L;
	public List<EmployeeHierarchy> employeeHierarchyList;
	
	public List<EmployeeHierarchy> getEmployeeHierarchyList() {
		return employeeHierarchyList;
	}
	public void setEmployeeHierarchyList(List<EmployeeHierarchy> employeeHierarchyList) {
		this.employeeHierarchyList = employeeHierarchyList;
	}
	
}
