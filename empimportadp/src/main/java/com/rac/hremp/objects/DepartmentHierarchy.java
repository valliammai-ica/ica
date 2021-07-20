package com.rac.hremp.objects;

import java.io.Serializable;
import java.util.List;

public class DepartmentHierarchy implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String departmentCode;
	public String parentDepartmentCode;
	public String level;
	public String branch;
	
	public String getDepartmentCode() {
		return departmentCode;
	}
	public void setDepartmentCode(String departmentCode) {
		this.departmentCode = departmentCode;
	}
	public String getParentDepartmentCode() {
		return parentDepartmentCode;
	}
	public void setParentDepartmentCode(String parentDepartmentCode) {
		this.parentDepartmentCode = parentDepartmentCode;
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
