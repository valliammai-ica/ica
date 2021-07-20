package com.rac.hremp.service;

import java.io.IOException;
import java.net.MalformedURLException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.rac.hremp.objects.Department;
import com.rac.hremp.objects.Departments;
import com.rac.hremp.objects.Departments;
import com.rac.hremp.objects.EmployeeFullData;
import com.rac.hremp.objects.EmployeeHierarchyList;
import com.rac.hremp.objects.EmployeesFullData;
import com.rac.hremp.objects.JobFamilies;

public interface HRDataService {
	
	public static final String HIERARCHY_DOWN = "DOWN";
	public static final String HIERARCHY_UP = "UP";
	
	//get a single employee
	public EmployeesFullData getEmployee(String empNum);
	
	//get hierarchy for a single employee
	//direction = UP or DOWN
	public EmployeeHierarchyList getEmployeeHierarchy(String empNum, String direction) throws JsonParseException, JsonMappingException,
			MalformedURLException, IOException;
	
	//get list of all employees
	public EmployeesFullData getAllEmployees(boolean includeInactive) throws JsonParseException, JsonMappingException,
			MalformedURLException, IOException;
	
	//get all departments
	public Departments getAllDepartments(boolean includeInactive, String deptType) throws JsonParseException, JsonMappingException, MalformedURLException, IOException;
	
	//get list of all job families...includes jobcodes within each family
	public JobFamilies getAllJobFamilies() throws JsonParseException, JsonMappingException, MalformedURLException, IOException;
	
	//get employees for job code
	public EmployeesFullData getEmployeeForJobCode(String jobCode);
	
	public int postEmailAddressToADP(String jsonPayload);
	
	public int postWorkerToADP(String externalEmployeeID);
	
	//public int putSupervisorIdtoEmployee(int employeeId, int supervisorId);
	
	//public Department putSupervisorIdtoDepartment(String departmentId, int supervisorId);
}
