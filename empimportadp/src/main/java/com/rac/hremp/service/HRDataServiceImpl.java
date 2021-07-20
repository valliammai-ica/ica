package com.rac.hremp.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Vector;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rac.hremp.objects.Department;
import com.rac.hremp.objects.Departments;
import com.rac.hremp.objects.Employee;
import com.rac.hremp.objects.EmployeeFullData;
import com.rac.hremp.objects.EmployeeHierarchyList;
import com.rac.hremp.objects.Employees;
import com.rac.hremp.objects.EmployeesFullData;
import com.rac.hremp.objects.JobFamilies;

@Service("hrDataService")
@Configuration
@EnableScheduling
public class HRDataServiceImpl implements HRDataService {
	private static final Logger LOGGER = LoggerFactory.getLogger("hremp_service_log");
	
	@Autowired
	public HRDataServiceConfig dataServiceConfig;
	
	@Value("${hremp.getalldepartments.url}")
	public String getAllDepartmentsURL;		//replace {include} with boolean to include inactive depts...to get specific dept types send code ie..RTOSTR that will append to url.
	
	@Value("${hremp.getdepartment.url}")
	public String getDepartmentURL;			//replace {deptid} with department code
	
	@Value("${hremp.getemployee.url}")
	public String getEmployeeURL;				//append employee id to the end of this
	
	@Value("${hremp.getemployeefulldata.url}")
	public String getEmployeeFullDataURL;				//append employee id to the end of this
	
	@Value("${hremp.getallemployees.url}")
	public String getAllEmployeesURL;				//replace {include} with boolean to include inactive emps
	
	@Value("${hremp.getemployeehierarchy.url}")
	public String getEmployeeHierarchyURL;		//replace {empid} with employee id
												//default direction is UP
	@Value("${hremp.getemployeesfordepartment.url}")
	public String getEmployeesForDepartmentURL;		//replace {deptid} with department code
	
	@Value("${hremp.getdepartmenthierarchy.url}")
	public String getDepartmentHierarchyURL;		//replace {deptid} with department code
													//default direction is UP
	@Value("${hremp.getjobfamilies.url}")
	public String getAllJobFamiliesURL;
	
	@Value("${hremp.getjobfamily.url}")			//append job family code to end of url
	public String getJobFamilyURL;

	@Value("${hremp.getjobcode.url}")			//replace {jobcode} with job code
	public String getJobCodeURL;
	
	@Value("${hremp.getemployeesforjobcode.url}")			//replace {jobcode} with job code
	public String getEmployeesForJobCodeURL;
	
	@Value("${hremp.rd.department.type.codes}")
	public String rdDepartmentTypeCodes;
	
	@Value("${hremp.postToADP.url}")
	public String postToADPURL;
	
	@Value("${hremp.postToADP.content.type}")
	public String postToADPContentType;
	
	@Value("${hremp.postWorkerToADP.url}")
	public String postWorkerToADPURL;
	
	//@Value("${hremp.putSupervisorIdtoEmployee.url}")
	//public String putSupervisorIdtoEmployeeURL;
	
	private String basicAuth;
	
	@PostConstruct
	public void afterCreate() {
		try {
			String userpass = dataServiceConfig.getUsername() + ":" + dataServiceConfig.getPassword();
			basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//get a single department
	public Department getDepartment(String deptId) {
		Departments depts = new Departments();
		Department dept = new Department();
		try {
    		ObjectMapper mapper = new ObjectMapper();
    		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    		String deptUrl = getDepartmentURL;
    		deptUrl = deptUrl.replace("{deptid}", deptId);
    		URLConnection urlCon = new URL(dataServiceConfig.getServer()+deptUrl).openConnection();
    		urlCon.setRequestProperty("client_id", dataServiceConfig.getClient_id());
    		urlCon.setRequestProperty("correlation_id", dataServiceConfig.getCorrelation_id());
    		urlCon.setRequestProperty("user_id", dataServiceConfig.getUser_id());
    		urlCon.setRequestProperty("Authorization", basicAuth);
    		depts = mapper.readValue(urlCon.getInputStream(), Departments.class);
    		if (!depts.getDepartments().isEmpty()) {
    			dept = depts.getDepartments().get(0);
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dept;
	}
	
	//get all departments
	public Departments getAllDepartments(boolean includeInactive, String deptType) {
		Departments dept = new Departments();
		try {
    		ObjectMapper mapper = new ObjectMapper();
    		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    		String requestUrl = getAllDepartmentsURL;
    		if (!deptType.equals("")) {
    			requestUrl = getAllDepartmentsURL+deptType;
    		}
    		requestUrl = requestUrl.replace("{include}", String.valueOf(includeInactive));
    		
    		URLConnection urlCon = new URL(dataServiceConfig.getServer()+requestUrl).openConnection();
    		urlCon.setRequestProperty("client_id", dataServiceConfig.getClient_id());
    		urlCon.setRequestProperty("correlation_id", dataServiceConfig.getCorrelation_id());
    		urlCon.setRequestProperty("user_id", dataServiceConfig.getUser_id());
    		urlCon.setRequestProperty("Authorization", basicAuth);
    		long contentLength = urlCon.getContentLengthLong();
    		if (contentLength > 0) {
    			dept = mapper.readValue(urlCon.getInputStream(), Departments.class);
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dept;
	}
	
	private Departments removeFranchiseDept(Departments departments) {
		Vector<Department> modifiedDepts = new Vector<Department>();
		for (Department dept : departments.getDepartments()) {
			if (dept.getLineOfBusinessInfo() != null) {
				if (dept.getLineOfBusinessInfo().getCode() != null) {
        			if (!dept.getLineOfBusinessInfo().getCode().equals("21")) {
        				modifiedDepts.add(dept);
        			}
				}
			}
		}
		departments.setDepartments(modifiedDepts);
		return departments;
	}
	
	//get all data for a single employee
	public EmployeesFullData getEmployee(String empNum) {
		EmployeesFullData emp = new EmployeesFullData();
		try {
    		ObjectMapper mapper = new ObjectMapper();
    		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    		URLConnection urlCon = new URL(dataServiceConfig.getServer()+getEmployeeFullDataURL+empNum).openConnection();
    		urlCon.setRequestProperty("client_id", dataServiceConfig.getClient_id());
    		urlCon.setRequestProperty("correlation_id", dataServiceConfig.getCorrelation_id());
    		urlCon.setRequestProperty("user_id", dataServiceConfig.getUser_id());
    		urlCon.setRequestProperty("Authorization", basicAuth);
    		emp = mapper.readValue(urlCon.getInputStream(), EmployeesFullData.class);
		} catch (IOException ioe) {
			LOGGER.error("Exception retrieving single employee "+empNum+" from repository.");
			ioe.printStackTrace();
		}
		return emp;
	}
	
	//get hierarchy for a single employee
	//direction = UP or DOWN
	public EmployeeHierarchyList getEmployeeHierarchy(String empNum, String direction) throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		String directionParam = "?direction=";
		String employeeHierarchyUrl = getEmployeeHierarchyURL;
		employeeHierarchyUrl = employeeHierarchyUrl.replace("{empid}", empNum);
		if (direction != null) {
			employeeHierarchyUrl = employeeHierarchyUrl+directionParam+direction;
		}
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		URLConnection urlCon = new URL(dataServiceConfig.getServer()+employeeHierarchyUrl).openConnection();
		urlCon.setRequestProperty("client_id", dataServiceConfig.getClient_id());
		urlCon.setRequestProperty("correlation_id", dataServiceConfig.getCorrelation_id());
		urlCon.setRequestProperty("user_id", dataServiceConfig.getUser_id());
		urlCon.setRequestProperty("Authorization", basicAuth);
		EmployeeHierarchyList employeeHierarchyList = mapper.readValue(urlCon.getInputStream(), EmployeeHierarchyList.class);
		return employeeHierarchyList;
	}
	
	//get list of employees for department
	public Employees getDepartmentEmployeeList(String deptId) throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		String employeeForDepartmentUrl = getEmployeesForDepartmentURL;
		employeeForDepartmentUrl = employeeForDepartmentUrl.replace("{deptid}", deptId);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		URLConnection urlCon = new URL(dataServiceConfig.getServer()+employeeForDepartmentUrl).openConnection();
		urlCon.setRequestProperty("client_id", dataServiceConfig.getClient_id());
		urlCon.setRequestProperty("correlation_id", dataServiceConfig.getCorrelation_id());
		urlCon.setRequestProperty("user_id", dataServiceConfig.getUser_id());
		urlCon.setRequestProperty("Authorization", basicAuth);
		Employees departmentEmployeeList = mapper.readValue(urlCon.getInputStream(), Employees.class);
		return departmentEmployeeList;
	}
	
	//get all employees
	public EmployeesFullData getAllEmployees(boolean includeInactive) throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		URLConnection urlCon = new URL(dataServiceConfig.getServer()+getAllEmployeesURL.replace("{include}", String.valueOf(includeInactive))).openConnection();
		urlCon.setRequestProperty("client_id", dataServiceConfig.getClient_id());
		urlCon.setRequestProperty("correlation_id", dataServiceConfig.getCorrelation_id());
		urlCon.setRequestProperty("user_id", dataServiceConfig.getUser_id());
		urlCon.setRequestProperty("Authorization", basicAuth);
		EmployeesFullData emp = mapper.readValue(urlCon.getInputStream(), EmployeesFullData.class);
		return emp;
	}
	
	//get all job families
	public JobFamilies getAllJobFamilies() throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String requestUrl = getAllJobFamiliesURL;
		URLConnection urlCon = new URL(dataServiceConfig.getServer()+requestUrl).openConnection();
		urlCon.setRequestProperty("client_id", dataServiceConfig.getClient_id());
		urlCon.setRequestProperty("correlation_id", dataServiceConfig.getCorrelation_id());
		urlCon.setRequestProperty("user_id", dataServiceConfig.getUser_id());
		urlCon.setRequestProperty("Authorization", basicAuth);
		JobFamilies jobFamilies = mapper.readValue(urlCon.getInputStream(), JobFamilies.class);
		return jobFamilies;
	}
	
	//get job family
	public JobFamilies getJobFamily(String jobFamily) throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String requestUrl = getJobFamilyURL+jobFamily;
		URLConnection urlCon = new URL(dataServiceConfig.getServer()+requestUrl).openConnection();
		urlCon.setRequestProperty("client_id", dataServiceConfig.getClient_id());
		urlCon.setRequestProperty("correlation_id", dataServiceConfig.getCorrelation_id());
		urlCon.setRequestProperty("user_id", dataServiceConfig.getUser_id());
		urlCon.setRequestProperty("Authorization", basicAuth);
		JobFamilies jobFamilies = mapper.readValue(urlCon.getInputStream(), JobFamilies.class);
		return jobFamilies;
	}
	
	public EmployeesFullData getEmployeeForJobCode(String jobCode) {
		EmployeesFullData empFullData = new EmployeesFullData();
		try {
    		ObjectMapper mapper = new ObjectMapper();
    		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    		String empForJCUrl = getEmployeesForJobCodeURL;
    		empForJCUrl = empForJCUrl.replace("{jobcode}", jobCode);
    		URLConnection urlCon = new URL(dataServiceConfig.getServer()+empForJCUrl).openConnection();
    		urlCon.setRequestProperty("client_id", dataServiceConfig.getClient_id());
    		urlCon.setRequestProperty("correlation_id", dataServiceConfig.getCorrelation_id());
    		urlCon.setRequestProperty("user_id", dataServiceConfig.getUser_id());
    		urlCon.setRequestProperty("Authorization", basicAuth);
    		try {
        		Employees employees = mapper.readValue(urlCon.getInputStream(), Employees.class);
        		//this is such an ugly hack. the services are a bit of a mess
        		Vector<EmployeeFullData> empFull = new Vector<EmployeeFullData>();
        		for (Employee employee : employees.getEmployees()) {
        			EmployeesFullData empFullData2 = getEmployee(employee.getEmployeeNumber());
        			empFull.add(empFullData2.getEmployees().elementAt(0));
        		}
        		empFullData.setEmployees(empFull);
    		} catch (JsonMappingException jsm) {
    			LOGGER.info("error retreiving employees for job code "+jobCode);
    		}
		} catch (IOException ioe) {
			LOGGER.error("Exception retrieving employees for job code "+jobCode+" from repository.");
			ioe.printStackTrace();
		}
		return empFullData;
	}
	
	public int postEmailAddressToADP(String jsonPayload) {
		int postResponse = -1;
		try{
			HttpURLConnection postUrl = (HttpURLConnection)(new URL(dataServiceConfig.getServer()+getPostToADPURL()).openConnection());
			postUrl.setRequestMethod("POST");
			postUrl.setRequestProperty("client_id", dataServiceConfig.getClient_id());
			postUrl.setRequestProperty("correlation_id", dataServiceConfig.getCorrelation_id());
			postUrl.setRequestProperty("user_id", dataServiceConfig.getUser_id());
			postUrl.setRequestProperty("Authorization", basicAuth);
			postUrl.setRequestProperty("Content-Type", getPostToADPContentType());
			postUrl.setDoOutput(true);
			OutputStream os = postUrl.getOutputStream();
			os.write(jsonPayload.getBytes());
			os.flush();
			os.close();
			postResponse = postUrl.getResponseCode();
			os.close();
			postUrl.disconnect();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return postResponse;
	}
	
	public int postWorkerToADP(String externalEmployeeID) {
		int postResponse = -1;
		try{
			String postWorkerUrl = postWorkerToADPURL+externalEmployeeID;
			HttpURLConnection postUrl = (HttpURLConnection)(new URL(dataServiceConfig.getServer()+postWorkerUrl).openConnection());
			postUrl.setRequestMethod("POST");
			postUrl.setRequestProperty("client_id", dataServiceConfig.getClient_id());
			postUrl.setRequestProperty("correlation_id", dataServiceConfig.getCorrelation_id());
			postUrl.setRequestProperty("user_id", dataServiceConfig.getUser_id());
			postUrl.setRequestProperty("Authorization", basicAuth);
			postUrl.setRequestProperty("Content-Type","text/plain");
			postUrl.setDoOutput(true);
			OutputStream os = postUrl.getOutputStream();
			os.flush();
			os.close();
			postResponse = postUrl.getResponseCode();
			os.close();
			postUrl.disconnect();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return postResponse;
	}
	
	/*public int putSupervisorIdtoEmployee(int employeeId, int supervisorId) {
		String payload = "{\"employeeId\": "+employeeId+",\"supervisorId\": \""+supervisorId+"\"}";
		int putResponse = 0;
		try {
			HttpURLConnection postUrl = (HttpURLConnection)(new URL(dataServiceConfig.getServer()+putSupervisorIdtoEmployeeURL).openConnection());
			postUrl.setRequestMethod("PUT");
			postUrl.setRequestProperty("client_id", dataServiceConfig.getClient_id());
			postUrl.setRequestProperty("correlation_id", dataServiceConfig.getCorrelation_id());
			postUrl.setRequestProperty("user_id", dataServiceConfig.getUser_id());
			postUrl.setRequestProperty("Authorization", basicAuth);
			postUrl.setRequestProperty("Content-Type", getPostToADPContentType());
			postUrl.setDoOutput(true);
			OutputStream os = postUrl.getOutputStream();
			os.write(payload.getBytes());
			os.flush();
			os.close();
			putResponse = postUrl.getResponseCode();
			os.close();
			postUrl.disconnect();
		} catch (IOException ioe) {
			LOGGER.error("Exception updating supervisorId "+supervisorId+" for employeeId "+employeeId+".");
			ioe.printStackTrace();
		}
		return putResponse;
	}*/

	/*public Department putSupervisorIdtoDepartment(String departmentId, int supervisorId) {
		// TODO Auto-generated method stub
		return null;
	}*/

	public String getPostToADPURL() {
		return postToADPURL;
	}

	public void setPostToADPURL(String postToADPURL) {
		this.postToADPURL = postToADPURL;
	}

	public String getPostToADPContentType() {
		return postToADPContentType;
	}

	public void setPostToADPContentType(String postToADPContentType) {
		this.postToADPContentType = postToADPContentType;
	}
	
	public String getRDDepartmentTypeCodes() {
		return dataServiceConfig.getRdDepartmentTypeCodes();
	}

}
