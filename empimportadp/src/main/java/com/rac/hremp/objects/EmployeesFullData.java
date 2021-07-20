package com.rac.hremp.objects;

import java.io.Serializable;
import java.util.Vector;

public class EmployeesFullData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public Vector<EmployeeFullData> employees = new Vector<EmployeeFullData>();

	public Vector<EmployeeFullData> getEmployees() {
		return employees;
	}

	public void setEmployees(Vector<EmployeeFullData> employees) {
		this.employees = employees;
	}
	
}
