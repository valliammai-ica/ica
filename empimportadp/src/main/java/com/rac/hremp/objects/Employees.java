package com.rac.hremp.objects;

import java.io.Serializable;
import java.util.Vector;

public class Employees implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public Vector<Employee> employees;

	public Vector<Employee> getEmployees() {
		return employees;
	}

	public void setEmployees(Vector<Employee> employees) {
		this.employees = employees;
	}
	
}
