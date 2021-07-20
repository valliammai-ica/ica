package com.rac.hremp.objects;

import java.io.Serializable;
import java.util.List;

public class DepartmentHierarchyList implements Serializable {

	private static final long serialVersionUID = 1L;
	public List<DepartmentHierarchy> departmentHierarchyList;
	
	public List<DepartmentHierarchy> getDeptHierarchyList() {
		return departmentHierarchyList;
	}
	public void setDeptHierarchyList(List<DepartmentHierarchy> departmentHierarchyList) {
		this.departmentHierarchyList = departmentHierarchyList;
	}
	
}
