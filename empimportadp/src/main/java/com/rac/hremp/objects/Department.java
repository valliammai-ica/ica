package com.rac.hremp.objects;

import java.io.Serializable;

public class Department implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public String departmentCode;
	public String parentDepartmentCode;
	public String departmentName;
	public DepartmentTypeInfo departmentType;
	public DepartmentGroupInfo departmentGroup;
	public LineOfBusinessInfo lineOfBusiness;
	public String emailAddress;
	public String phoneNumber;
	public String fax;
	public DepartmentManager departmentManager;
	public String createdBy;
	public String createdDate;							//YYYY-MM-DD
	public String lastModifiedBy;
	public String lastModifiedDate;					//YYYY-MM-DD
	public DepartmentAddressInfo departmentAddress;
	public Company company;
	
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
	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	public DepartmentTypeInfo getDepartmentTypeInfo() {
		return departmentType;
	}
	public void setDepartmentTypeInfo(DepartmentTypeInfo departmentTypeInfo) {
		this.departmentType = departmentTypeInfo;
	}
	public DepartmentGroupInfo getDepartmentGroupInfo() {
		return departmentGroup;
	}
	public void setDepartmentGroupInfo(DepartmentGroupInfo departmentGroupInfo) {
		this.departmentGroup = departmentGroupInfo;
	}
	public LineOfBusinessInfo getLineOfBusinessInfo() {
		return lineOfBusiness;
	}
	public void setLineOfBusinessInfo(LineOfBusinessInfo lineOfBusinessInfo) {
		this.lineOfBusiness = lineOfBusinessInfo;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		this.fax = fax;
	}
	public DepartmentManager getDepartmentManagerInfo() {
		return departmentManager;
	}
	public void setDepartmentManagerInfo(DepartmentManager departmentManagerInfo) {
		this.departmentManager = departmentManagerInfo;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	public String getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public DepartmentAddressInfo getDepartmentAddressInfo() {
		return departmentAddress;
	}
	public void setDepartmentAddressInfo(DepartmentAddressInfo departmentAddressInfo) {
		this.departmentAddress = departmentAddressInfo;
	}
	public Company getCompany() {
		return company;
	}
	public void setCompany(Company company) {
		this.company = company;
	}
	
}
