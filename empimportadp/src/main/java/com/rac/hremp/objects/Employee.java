package com.rac.hremp.objects;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee implements Serializable {

	private static final long serialVersionUID = 1L;
	public String employeeNumber;
	public String supervisorEmployeeNumber;
	public String firstName;
	public String lastName;
	public String middleName;
	public String middleInitial;
	public String nickName;
	public String startDate;		//YYYY-MM-DD
	public String terminationDate;	//YYYY-MM-DD   default value 7777-07-07
	public String lastModifiedDate; //YYYY-MM-DD
	public String gender;
	public String emailAddress;
	public String homePhoneNumber;
	public String departmentCode;
	public String glDistributionCompany;
	public String glAccountingUnit;
	public JobCode jobCode;
	public String employeeStatus;
	public PayStatus payStatus;
	public Company company;
	public Address address;
	
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
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	public String getMiddleInitial() {
		return middleInitial;
	}
	public void setMiddleInitial(String middleInitial) {
		this.middleInitial = middleInitial;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getTerminationDate() {
		return terminationDate;
	}
	public void setTerminationDate(String terminationDate) {
		this.terminationDate = terminationDate;
	}
	public String getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getHomePhoneNumber() {
		return homePhoneNumber;
	}
	public void setHomePhoneNumber(String homePhoneNumber) {
		this.homePhoneNumber = homePhoneNumber;
	}
	public String getDepartmentCode() {
		return departmentCode;
	}
	public void setDepartmentCode(String departmentCode) {
		this.departmentCode = departmentCode;
	}
	public String getGlDistributionCompany() {
		return glDistributionCompany;
	}
	public void setGlDistributionCompany(String glDistributionCompany) {
		this.glDistributionCompany = glDistributionCompany;
	}
	public String getGlAccountingUnit() {
		return glAccountingUnit;
	}
	public void setGlAccountingUnit(String glAccountingUnit) {
		this.glAccountingUnit = glAccountingUnit;
	}
	public JobCode getJobCode() {
		return jobCode;
	}
	public void setJobCode(JobCode jobCode) {
		this.jobCode = jobCode;
	}
	public String getEmployeeStatus() {
		return employeeStatus;
	}
	public void setEmployeeStatus(String employeeStatus) {
		this.employeeStatus = employeeStatus;
	}
	public PayStatus getPayStatus() {
		return payStatus;
	}
	public void setPayStatus(PayStatus payStatus) {
		this.payStatus = payStatus;
	}
	public Company getCompany() {
		return company;
	}
	public void setCompany(Company company) {
		this.company = company;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	
}
