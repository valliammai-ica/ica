package com.rac.iam.okta.adp.csv.employee.importer.utilities;

import java.util.Date;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.csv.CSVRecord;
import com.okta.sdk.models.users.UserProfile;
import com.rac.iam.okta.adp.csv.employee.importer.model.OktaProfileConfigurationData;
import com.rac.iam.okta.adp.csv.employee.importer.model.ADPUser;

public class EmployeeUtility {

	public static final String IN_PROCESS_SUFFIX = "IN_PROCESS";
	public static final String PROCESSED_SUFFIX = "PROCESSED";
	public static final String EMPLOYEE_IMPORTER = "EMPLOYEE_IMPORTER";
	public static final String FILE_DOWNLOADER = "FILE_DOWNLOADER";
	public static final String ADP_STATUS_ACTIVE = "A";
	public static final String ADP_STATUS_INACTIVE = "I";
	public static final String OKTA_STATUS_ACTIVE = "ACTIVE";
	public static final String OKTA_STATUS_PROVISIONED = "PROVISIONED";
	public static final String OKTA_STATUS_DEPROVISIONED = "DEPROVISIONED";
	public static final String FSC_ORG_VALUE = "FSC";
	public static final String NPS_ORG_VALUE = "NPS";
	public static final String DIVISION_FIELD = "Field";
	public static final String DIVISION_HOME_OFFICE = "Home Office";
	public static final int FSC_LINE_OF_BUSINESS = 10;
	public static final int FRANCHISE_LINE_OF_BUSINESS = 21;
	public static final int MEX_FSC_LINE_OF_BUSINESS = 22;//future

	public static String getInProcessFilename(String fileName) {
		String[] fileNameParts = fileName.split("\\.");
		return fileNameParts[0] + "_" + EmployeeUtility.IN_PROCESS_SUFFIX + "." + fileNameParts[1];
	}

	public static String getProcessedFilename(String fileName) {
		String[] fileNameParts = fileName.split("\\.");
		return fileNameParts[0] + "_" + EmployeeUtility.PROCESSED_SUFFIX + "." + fileNameParts[1];
	}

	public static ADPUser constructEmployee(CSVRecord record) {
		ADPUser user = new ADPUser();
		user.setEmployeeStatus(record.get("status"));
		user.setJobTitle(record.get("title"));
		user.setManagerID(record.get("managerId"));
		user.setManager(record.get("managerId"));//duplicated in the original file feed from SF....same data different attribute
		user.setCompany(record.get("Company"));
		user.setEmployeeID(record.get("empID"));
		user.setFirstName(record.get("firstName"));
		user.setZipCode(record.get("employee_zipCode"));
		user.setState(record.get("employee_state"));
		user.setAddressLine1(record.get("employee_addressLine1"));//pipe '|' separated list
		user.setEmail(record.get("email"));
		user.setLastName(record.get("lastName"));
		user.setMiddleName(record.get("middleName"));
		if (record.get("nickName") == null || record.get("nickName").equals("")){
			user.setNickName(record.get("firstName"));
		} else {
			user.setNickName(record.get("nickName"));
		}
		user.setLocation(record.get("locationName"));
		user.setDepartment(record.get("locationCode"));
		user.setDepartmentID(record.get("locationCode"));
		user.setsAMAccountName(record.get("SamAccountName"));
		user.setLineOfBusiness(String.valueOf(Integer.parseInt(record.get("lineOfBusiness"),10)));//strip out leading 0's
		user.setLineOfBusinessDesc(record.get("lineOfBusinessDesc"));
		user.setCountryCode(record.get("CountryCode"));
		user.setBusinessUnit(record.get("CountryCode"));
		user.setJobFamily(record.get("jobFamily"));
		user.setOrgLevel1(record.get("orglevel1"));// pipe '|' separated list
		user.setOrgLevel1Name(record.get("orglevel1name"));// pipe '|' separated list
		user.setAdjHireDate(record.get("hireDate"));
		user.setPayGroup(record.get("payGroup"));
		user.setPayGroupDesc(record.get("payGroupDesc"));
		user.setPersonalEmail(record.get("personalEmail"));
		
		return user;
	}

	public static UserProfile constructOktaUserProfile(ADPUser user, UserProfile userProfile,
			String defaultUserAccountSuffix, OktaProfileConfigurationData oktaProfileConfig) {
		formatUserNames(user);
		if (userProfile == null) {
			userProfile = new UserProfile();
			userProfile.setLogin(user.getEmployeeID() + "@" + defaultUserAccountSuffix);
		}
		String employeeNumber = determineEmployeeNumber(user, oktaProfileConfig);
		
		userProfile.setFirstName(user.getFirstName());
		userProfile.setLastName(user.getLastName());
		Map<String, Object> unmappedAttributes = userProfile.getUnmapped();
		if (user.getAddressLine1().indexOf("|") > 0) {
			unmappedAttributes.put("streetAddress", user.getAddressLine1().substring(0, user.getAddressLine1().indexOf("|")));
		} else {
			unmappedAttributes.put("streetAddress", user.getAddressLine1());
		}
		unmappedAttributes.put("company", user.getCompany());
		unmappedAttributes.put("department", user.getDepartment());
		unmappedAttributes.put("departmentLawson", user.getDepartmentID());
		unmappedAttributes.put("departmentID", user.getDepartmentID());
		unmappedAttributes.put("displayName", user.getDisplayName());
		unmappedAttributes.put("lineOfBusiness", user.getLineOfBusiness());
		unmappedAttributes.put("lobDescription", user.getLineOfBusinessDesc());
		unmappedAttributes.put("employeeID", user.getEmployeeID());
		unmappedAttributes.put("employeeNumber", employeeNumber);
		unmappedAttributes.put("title", user.getJobTitle());
		unmappedAttributes.put("location", user.getLocation());
		unmappedAttributes.put("city", user.getLocation());
		unmappedAttributes.put("countryCode", user.getCountryCode());
		unmappedAttributes.put("manager", user.getManagerID());
		unmappedAttributes.put("managerId", user.getManagerID());
		unmappedAttributes.put("middleName", user.getMiddleName());
		unmappedAttributes.put("nickName", user.getNickName());
		unmappedAttributes.put("organization", user.getOrgLevel());
		unmappedAttributes.put("state", user.getState());
		unmappedAttributes.put("zipCode", user.getZipCode());
		unmappedAttributes.put("businessUnit", user.getBusinessUnit());
		
		//wipe out old data that may not be overwritten with new data in ADP
		unmappedAttributes.put("orglevel1", "");
		unmappedAttributes.put("orglevel2", "");
		unmappedAttributes.put("orglevel3", "");
		unmappedAttributes.put("orglevel4", "");
		unmappedAttributes.put("orglevel5", "");
		unmappedAttributes.put("orglevel6", "");
		unmappedAttributes.put("orglevel7", "");
		unmappedAttributes.put("orglevel1name", "");
		unmappedAttributes.put("orglevel2name", "");
		unmappedAttributes.put("orglevel3name", "");
		unmappedAttributes.put("orglevel4name", "");
		unmappedAttributes.put("orglevel5name", "");
		unmappedAttributes.put("orglevel6name", "");
		unmappedAttributes.put("orglevel7name", "");
		
		//now populate the org levels with ADP data...if it exists
		if (user.getOrgLevel1().indexOf("|") > 0) {
			Stack<String> orgLevelStack = breakOutLevels(user.getOrgLevel1());
			int kounter = 1;
			while (!orgLevelStack.isEmpty()) {
				String orgLevel = orgLevelStack.pop();
				if (!orgLevel.equals("_")) {
					unmappedAttributes.put("orglevel"+kounter, orgLevel);
				}
				kounter++;
			}
		}
		
		if (user.getOrgLevel1Name().indexOf("|") > 0) {
			Stack<String> orgLevelStack = breakOutLevels(user.getOrgLevel1Name());
			int kounter = 1;
			while (!orgLevelStack.isEmpty()) {
				String orgLevelName = orgLevelStack.pop();
				if (!orgLevelName.equals("_")) {
					unmappedAttributes.put("orglevel"+kounter+"name", orgLevelName);
				}
				kounter++;
			}
		}

		if (oktaProfileConfig.getContractorPayGroups().contains(user.getPayGroup())) {
			unmappedAttributes.put("Contractor", true);
		} else {
			unmappedAttributes.put("Contractor", false);
		}
		unmappedAttributes.put("isStore", false);
		user.setDivision(DIVISION_FIELD);//default to field 
		if(Integer.parseInt(user.getLineOfBusiness(), 10) != FRANCHISE_LINE_OF_BUSINESS) {
    		if((Integer.parseInt(user.getLineOfBusiness(), 10) == FSC_LINE_OF_BUSINESS) ||
    				(Integer.parseInt(user.getLineOfBusiness(), 10) == MEX_FSC_LINE_OF_BUSINESS) ||
    				oktaProfileConfig.getDvpVector().contains(user.getJobFamily().trim()) ||
    				oktaProfileConfig.getHoJobFamilies().contains(user.getJobFamily().trim())) {
    			user.setDivision(DIVISION_HOME_OFFICE);
    			unmappedAttributes.put("glDescription", user.getGlDescription());
    		} else {
    			user.setDivision(DIVISION_FIELD);
    			unmappedAttributes.put("glDescription", user.getDepartment());
    		}
		} else {
			user.setDivision(DIVISION_FIELD);
			unmappedAttributes.put("glDescription", user.getDepartment());
		}
		unmappedAttributes.put("division", user.getDivision());
		unmappedAttributes.put("jobRole", determineJobRole(user, oktaProfileConfig));
		unmappedAttributes.put("jobFamily", user.getJobFamily());
		unmappedAttributes.put("lastupdated", new Date().getTime());
		unmappedAttributes.put("payGroup", user.getPayGroup());
		unmappedAttributes.put("payGroupDesc",user.getPayGroupDesc());
		unmappedAttributes.put("hireDate", user.getAdjHireDate());
		userProfile.setSecondEmail(user.getPersonalEmail());
		return userProfile;
	}
	
	private static Stack<String> breakOutLevels(String orgLevels) {
		Stack<String> orgLevelStack = new Stack<String>();
		StringTokenizer st = new StringTokenizer(orgLevels,"|");
		while (st.hasMoreTokens()) {
			String level = st.nextToken();
			orgLevelStack.push(level);
		}
		int stackPad = 7 - orgLevelStack.size();
		for (int k=0;k<stackPad;k++) {
			orgLevelStack.push("_");
		}
		return orgLevelStack;
	}
	
	private static void formatUserNames(ADPUser user) {
		if(user.getFirstName() != null && user.getFirstName().length()>0) {
			StringBuffer modFName = new StringBuffer();
			StringTokenizer st = new StringTokenizer(user.getFirstName(), " ");
			try {
				while(st.hasMoreTokens()) {
					String val = st.nextToken();
					if (val.contains("-")) {
						modFName.append(val.substring(0, 1).toUpperCase()+val.substring(1, val.indexOf("-")+1).toLowerCase()
								+val.substring(val.indexOf("-")+1, val.indexOf("-")+2).toUpperCase()+val.substring(val.indexOf("-")+2).toLowerCase());
					} else if (val.contains("'")) {
						modFName.append(val.substring(0, 1).toUpperCase()+val.substring(1, val.indexOf("'")+1).toLowerCase()
								+val.substring(val.indexOf("'")+1, val.indexOf("'")+2).toUpperCase()+val.substring(val.indexOf("'")+2).toLowerCase());
					} else {
						modFName.append(val.substring(0, 1).toUpperCase());
						modFName.append(val.substring(1).toLowerCase());
					}
					if(st.hasMoreTokens()) {
						modFName.append(" ");
					}
				}
			}
			catch (Exception e) {
				System.out.println("Error formatting employee First_Name. ");
				modFName = new StringBuffer().append(user.getFirstName());
			}
			user.setFirstName(modFName.toString());
		}
		if(user.getNickName() != null && user.getNickName().length()>0) {
			StringBuffer modNName = new StringBuffer();
			StringTokenizer st = new StringTokenizer(user.getNickName(), " ");
			try {
				while(st.hasMoreTokens()) {
					String val = st.nextToken();
					if (val.contains("-")) {
						modNName.append(val.substring(0, 1).toUpperCase()+val.substring(1, val.indexOf("-")+1).toLowerCase()
								+val.substring(val.indexOf("-")+1, val.indexOf("-")+2).toUpperCase()+val.substring(val.indexOf("-")+2).toLowerCase());
					} else if (val.contains("'")) {
						modNName.append(val.substring(0, 1).toUpperCase()+val.substring(1, val.indexOf("'")+1).toLowerCase()
								+val.substring(val.indexOf("'")+1, val.indexOf("'")+2).toUpperCase()+val.substring(val.indexOf("'")+2).toLowerCase());
					} else {
						modNName.append(val.substring(0, 1).toUpperCase());
						modNName.append(val.substring(1).toLowerCase());
					}
					if(st.hasMoreTokens()) {
						modNName.append(" ");
					}
				}
			}
			catch (Exception e) {
				System.out.println("Error formatting employee Nick_Name. ");
				modNName = new StringBuffer().append(user.getNickName());
			}
			user.setNickName(modNName.toString());
		}
		if(user.getLastName() != null && user.getLastName().length()>0) {
			StringBuffer modLName = new StringBuffer();
			StringTokenizer st = new StringTokenizer(user.getLastName(), " ");
			try {
				while(st.hasMoreTokens()) {
					String val = st.nextToken();
					if(val.equals("II") || val.equals("III") || val.equals("IV") || val.equals("V")) {
						modLName.append(val);
					} else if (val.contains("-")) {
						modLName.append(val.substring(0, 1).toUpperCase()+val.substring(1, val.indexOf("-")+1).toLowerCase()
								+val.substring(val.indexOf("-")+1, val.indexOf("-")+2).toUpperCase()+val.substring(val.indexOf("-")+2).toLowerCase());
					} else if (val.contains("'")) {
						modLName.append(val.substring(0, 1).toUpperCase()+val.substring(1, val.indexOf("'")+1).toLowerCase()
								+val.substring(val.indexOf("'")+1, val.indexOf("'")+2).toUpperCase()+val.substring(val.indexOf("'")+2).toLowerCase());
					} else {
						modLName.append(val.substring(0, 1).toUpperCase());
						modLName.append(val.substring(1).toLowerCase());
					}
					if(st.hasMoreTokens()) {
						modLName.append(" ");
					}
				}
			}
			catch (Exception e) {
				System.out.println("Error formatting employee Last_Name. ");
				modLName = new StringBuffer().append(user.getLastName());
			}
			user.setLastName(modLName.toString());
		}
		if(user.getMiddleName() != null && user.getMiddleName().length()>0) {
			StringBuffer modMName = new StringBuffer();
			StringTokenizer st = new StringTokenizer(user.getMiddleName(), " ");
			try {
				while(st.hasMoreTokens()) {
					String val = st.nextToken();
					if (val.contains("-")) {
						modMName.append(val.substring(0, 1).toUpperCase()+val.substring(1, val.indexOf("-")+1).toLowerCase()
								+val.substring(val.indexOf("-")+1, val.indexOf("-")+2).toUpperCase()+val.substring(val.indexOf("-")+2).toLowerCase());
					} else if (val.contains("'")) {
						modMName.append(val.substring(0, 1).toUpperCase()+val.substring(1, val.indexOf("'")+1).toLowerCase()
								+val.substring(val.indexOf("'")+1, val.indexOf("'")+2).toUpperCase()+val.substring(val.indexOf("'")+2).toLowerCase());
					} else {
						modMName.append(val.substring(0, 1).toUpperCase());
						modMName.append(val.substring(1).toLowerCase());
					}
					if(st.hasMoreTokens()) {
						modMName.append(" ");
					}
				}
			}
			catch (Exception e) {
				System.out.println("Error formatting employee Middle_Name. ");
				modMName = new StringBuffer().append(user.getMiddleName());
			}
			user.setMiddleName(modMName.toString());
		}
		
		//replace the display name from the lawson file with a properly formatted name
		if(user.getNickName()!=null && user.getNickName()!="") {
			user.setDisplayName(user.getNickName()+" "+user.getLastName());
		} else {
			user.setDisplayName(user.getFirstName()+" "+user.getLastName());
		}
	}
	
	public static String setElevatedAccess(String jobCode, Vector<String> elevatedAccessVec) {
		String elevatedAccess = "N";
		if(elevatedAccessVec.contains(jobCode)) {
			elevatedAccess = "Y";
		}
		return elevatedAccess;
	}
	
	public static String determineEmployeeNumber(ADPUser user,
			OktaProfileConfigurationData oktaProfileConfig) {
		String employeeNumber = "";

		if(oktaProfileConfig.getRdVector().contains(user.getJobFamily())){
			employeeNumber = "4";
		} else if(oktaProfileConfig.getDmVector().contains(user.getJobFamily())){
			employeeNumber = "5";
		} else if(Integer.parseInt(user.getLineOfBusiness(), 10) == FSC_LINE_OF_BUSINESS ||
				oktaProfileConfig.getDvpVector().contains(user.getJobFamily()) || 
				oktaProfileConfig.getHoVector().contains(user.getJobFamily())){
			employeeNumber = "7";
		} else {
			employeeNumber = "6";
		}
		return employeeNumber;
	}
	
	public static String determineJobRole(ADPUser user, 
			OktaProfileConfigurationData oktaProfileConfig) {
		String jobRole = "";
		if(user.getDivision().equals(DIVISION_FIELD)) {
			if(oktaProfileConfig.getSmVector().contains(user.getJobFamily())) {
				jobRole = "STORE MANAGER";
			}
			if(oktaProfileConfig.getDmVector().contains(user.getJobFamily())) {
				jobRole = "DM";
			}
			if(oktaProfileConfig.getRdVector().contains(user.getJobFamily())) {
				jobRole = "RD";
			}
		} else if (oktaProfileConfig.getDvpVector().contains(user.getJobFamily())){
			jobRole = "VP";
		} else {
			jobRole = "";
		}
		
		return jobRole;
	}

	public static String constructLoginId(String loginStringPrefix) {
		return loginStringPrefix.replace(' ', '_') + "@rentacenter.com";
	}

	public static String constructEmailAddress(String firstName, String lastName) {
		return firstName + "." + lastName + "@rentacenter.com";
	}

}
