package com.rac.hremp.objects;

import java.io.Serializable;
import java.util.Vector;

public class Departments implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public Vector<Department> departments = new Vector<Department>();

	public Vector<Department> getDepartments() {
		return departments;
	}

	public void setDepartments(Vector<Department> departments) {
		this.departments = departments;
	}
	
}
