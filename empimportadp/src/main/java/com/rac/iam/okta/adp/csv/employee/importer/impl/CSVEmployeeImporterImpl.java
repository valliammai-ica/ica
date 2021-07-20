package com.rac.iam.okta.adp.csv.employee.importer.impl;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.Vector;
import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.okta.sdk.clients.UserApiClient;
import com.okta.sdk.exceptions.ApiException;
import com.okta.sdk.framework.ApiClientConfiguration;
import com.okta.sdk.framework.FilterBuilder;
import com.okta.sdk.models.users.LoginCredentials;
import com.okta.sdk.models.users.Password;
import com.okta.sdk.models.users.User;
import com.okta.sdk.models.users.UserProfile;
import com.rac.hremp.objects.Department;
import com.rac.hremp.objects.Departments;
import com.rac.hremp.objects.EmployeeFullData;
import com.rac.hremp.objects.EmployeesFullData;
import com.rac.hremp.service.HRDataService;
import com.rac.iam.okta.adp.csv.employee.importer.EmployeeImporter;
import com.rac.iam.okta.adp.csv.employee.importer.ImportErrorNotifier;
import com.rac.iam.okta.adp.csv.employee.importer.PasswordGenerator;
import com.rac.iam.okta.adp.csv.employee.importer.exception.EmployeeImportException;
import com.rac.iam.okta.adp.csv.employee.importer.model.NotificationMessage;
import com.rac.iam.okta.adp.csv.employee.importer.model.OktaProfileConfigurationData;
import com.rac.iam.okta.adp.csv.employee.importer.model.ADPUser;
import com.rac.iam.okta.adp.csv.employee.importer.utilities.EmployeeUtility;
import com.rac.okta.employeead.service.EmployeeADService;

@Configuration
@EnableScheduling
@Controller
@RequestMapping(value = "/empImportController")
public class CSVEmployeeImporterImpl implements EmployeeImporter {

	private static final Logger LOGGER = LoggerFactory.getLogger("import_logfile");
	private static final Logger ERROR_LOGGER = LoggerFactory.getLogger("errors");
	private static final int LINE_OF_BUSINESS_NPS = 12;
	private static final int LINE_OF_BUSINESS_FSC = 10;
	private static final int LINE_OF_BUSINESS_ANOW = 5;
	private static final int LINE_OF_BUSINESS_RACFI = 21;
	private static final int LINE_OF_BUSINESS_FRANCHISING = 11;
	private static final int LINE_OF_BUSINESS_MEXICO_NPS = 13;
	private static final int LINE_OF_BUSINESS_PREFERRED_LEASE = 23;
	private static final int LINE_OF_BUSINESS_MEXICO_PREFERRED_LEASE = 24;
	private static final String JOB_ROLE_STORE_MANAGER = "STORE MANAGER";
	private static final String JOB_ROLE_VP = "VP";
	private static final String JOB_ROLE_DM = "DM";
	private static final String MP_DEPARTMENT_PREFIX = "MP";
	private static String OS = System.getProperty("os.name").toLowerCase();
	private static final Long ONE_MINUTE_DELAY = 60000L;
	private static final Long TWO_MINUTE_DELAY = 120000L;
	private static final Long THREE_MINUTE_DELAY = 180000L;
	private static final Long FIVE_MINUTE_DELAY = 300000L;
	private static final Long FIFTEEN_MINUTE_DELAY = 900000L;
	private static final Long SEVENTY_FIVE_MIN_DELAY = 4500000L;
	
	private ArrayList<Integer> lobList = new ArrayList<Integer>();
	
	@Autowired
	private OktaProfileConfigurationData oktaProfileConfig;
	
	@Autowired
	private HRDataService hrDataService;
	
	@Autowired
	EmployeeADService employeeADService;

	@Value("${importer.localdirectory}")
	private String localSourceFolder = null;
	
	@Value("${importer.localprocesseddirectory}")
	private String localProcessedFolder = null;

	@Value("${okta.tenant.url}")
	private String oktaTenantUrl = null;

	@Value("${okta.api.key}")
	private String oktaAPIKey = null;

	@Value("${importer.stopProcessingAllFilesOnError}")
	private boolean stopProcessingAllFilesOnError = false;

	@Value("${importer.stopProcessingFileOnError}")
	private boolean stopProcessingFileOnError = false;

	@Value("${ad.user.account.suffix}")
	private String adUserSuffix = null;

	@Value("${importer.skip.sftpfiles}")
	private boolean skipSFTPFilesForTest = false;
	
	@Value("${default.user.account.suffix}")
	private String defaultUserAccountSuffix = null;
	
	@Value("${fsc.new.user.emailid}")
	private String fscNewUserEmailID = null;
	
	@Value("${field.new.user.emailid}")
	private String fieldNewUserEmailID = null;
	
	@Value("${importer.default.manager.cn}")
	private String defaultManagerCN = null;
	
	@Value("${importer.rac.domain}")
	private String racDomain = null;
	
	@Value("${importer.nps.domain}")
	private String npsDomain = null;
	
	@Value("${importer.franchising.domain}")
	private String racFranchisingDomain = null;
	
	@Value("${importer.racfi.domain}")
	private String racfiDomain = null;
	
	@Value("${importer.anow.domain}")
	private String anowDomain = null;
	
	@Value("${importer.preferredlease.domain}")
	private String plDomain = null;
	
	@Value("${importer.use.firstname.lastname}")
	private String useFirstDotLast = null;
	
	@Value("${importer.pauseonstartup}")
	private boolean pauseImporting;
	
	@Value("${importer.treatasfullfile}")
	private int treatAsFullFile = 0;
	
	@Value("${importer.rac.domain.exceptions}")
	private String racDomainExceptions = null;
	
	@Value("${importer.preferredlease.domain.exceptions}")
	private String plDomainExceptions = null;

	@Autowired
	private ImportErrorNotifier notifier = null;
	
	@Autowired
	private PasswordGenerator passwordGenerator;
	
	private ApiClientConfiguration apiClientConfiguration = null;
	private UserApiClient userAPIClient = null;
	private boolean deleteFile = false;
	private boolean isImporting = false;
	private String computerName = null;
	private boolean largeFileLoad = false;
	
	private HashMap<String,Department> deptMap = new HashMap<String,Department>();

	@PostConstruct
	public void afterCreation() {
		apiClientConfiguration = new ApiClientConfiguration(oktaTenantUrl, oktaAPIKey);
		userAPIClient = new UserApiClient(apiClientConfiguration);
		try {
			setComputerName(InetAddress.getLocalHost().getHostName());
		} catch (Exception e){}
		LOGGER.info("Employee Importer started on "+OS+" server "+getComputerName());
		System.out.println("Employee Importer started on "+OS+" server "+getComputerName());

		lobList.add(LINE_OF_BUSINESS_NPS);
		lobList.add(LINE_OF_BUSINESS_FSC);
		lobList.add(LINE_OF_BUSINESS_FRANCHISING);
		lobList.add(LINE_OF_BUSINESS_MEXICO_NPS);

		buildDeptList();
		LOGGER.info("Startup configuration complete on server "+getComputerName());
	}
	
	@Scheduled(cron="${importer.department.map.cron}")
	private void buildDeptList() {
		try {
			LOGGER.info("Building Department Map");
			LOGGER.info("Saving current import pause status");
			boolean pauseStatus = isPauseImporting();
			LOGGER.info("Pausing imports during map rebuild.");
			setPauseStatus(true);
			HashMap<String,Department> newDeptMap = new HashMap<String,Department>();
			Departments depts = hrDataService.getAllDepartments(true,"");
			for (Department dept : depts.getDepartments()) {
				newDeptMap.put(dept.getDepartmentCode(), dept);
			}
			synchronized(this) {
				deptMap = newDeptMap;
			}
			LOGGER.info("Completed Department Map rebuild");
			LOGGER.info("Resetting pause import status to prior setting of "+pauseStatus);
			setPauseStatus(pauseStatus);
		} catch (Exception e) {
			ERROR_LOGGER.error("Error building department map for stores."+e.getMessage());
			e.printStackTrace();
		}
		LOGGER.info("deptMap contains "+deptMap.size()+" objects.");
		/*for (String dept : deptMap.keySet()) {
			Department dpt = deptMap.get(dept);
			LOGGER.info(dept+" "+dpt.getDepartmentName()+" "+dpt.getDepartmentTypeInfo().getCode());
		}*/
	}
	
	@RequestMapping(value = "/triggerBuildDeptList", method = RequestMethod.GET)
	public @ResponseBody String triggerBuildDeptList() {
		Timer tymer = new Timer();
		TimerTask tt = new TimerTask() {
			public void run() {
				try {
					buildDeptList();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		tymer.schedule(tt, 30000);
		return "buildDeptList has been triggered........";
	}

	@Scheduled(initialDelayString="${importer.scheduled.initialDelay}",fixedDelayString="${importer.scheduled.fixedDelay}")
	public void importEmployeeRecords() throws EmployeeImportException {
		if (isImporting()) {//this should never happen....but just in case.
			LOGGER.info("Previous import is still in process. Skipping run.");
			return;
		}
		if (isPauseImporting()) {
			LOGGER.info("Importing is manually paused. This run will be skipped until the manual restart is issued. Files will continue to queue.");
			return;
		}
		setImporting(true);
		LOGGER.debug("Importing Employee Records");
		Set<String> listOfFiles = getListOfFiles();
		for (String csvFile : listOfFiles) {
			//reset file deletion flag
			deleteFile = false;
			try {
				LOGGER.debug("Marking file as in process");
				String inProcessFileName = markFileAsInProcess(csvFile);
				LOGGER.debug("Importing records from file");
				importRecordsFromFile(inProcessFileName);
				if(deleteFile) {
					LOGGER.debug("Deleting empty file");
					File fileToDelete = new File(inProcessFileName);
					fileToDelete.delete();
				} else {
					LOGGER.debug("Marking file as processed");
					String processedFile = markFileAsProcessed(csvFile, constructInProcessFileName(csvFile));
					LOGGER.debug("Moving file to processed directory");
					moveFileToProcessed(processedFile);
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				NotificationMessage message = new NotificationMessage();
				StringBuffer buffy = new StringBuffer();
				buffy.append("[Error] while processing csv file "+csvFile+"\n\n");
				String stackTrace = ExceptionUtils.getStackTrace(e);
				buffy.append(e.getMessage()+"\n"+stackTrace);
				message.setMessageBody(buffy.toString());
				message.setSubjectLine("[Error] on server "+getComputerName());
				notifier.sendNotification(message);
				if (stopProcessingAllFilesOnError) {
					throw new EmployeeImportException(e.getMessage(), e);
				}
			}
		}
		setImporting(false);
	}
	
	@RequestMapping(value = "/pauseImports", method = RequestMethod.GET)
	public @ResponseBody String pauseImporting() {
		LOGGER.info("Received request to pause imports.");
		setPauseStatus(true);

		return "Imports have been paused, files will continue to queue.";
	}
	
	public void setPauseStatus(boolean status) {
		setPauseImporting(status);
	}
	
	@RequestMapping(value = "/resumeImports", method = RequestMethod.GET)
	public @ResponseBody String resumeImporting() {
		LOGGER.info("Received request to resume imports.");
		setPauseStatus(false);

		return "Imports have been resumed, queued files will resume processing.";
	}

	public String getLocalSourceFolder() {
		return localSourceFolder;
	}

	public void setLocalSourceFolder(String localSourceFolder) {
		this.localSourceFolder = localSourceFolder;
	}

	public String getLocalProcessedFolder() {
		return localProcessedFolder;
	}

	public void setLocalProcessedFolder(String localProcessedFolder) {
		this.localProcessedFolder = localProcessedFolder;
	}

	public String getOktaTenantUrl() {
		return oktaTenantUrl;
	}

	public void setOktaTenantUrl(String oktaTenantUrl) {
		this.oktaTenantUrl = oktaTenantUrl;
	}

	public String getOktaAPIKey() {
		return oktaAPIKey;
	}

	public void setOktaAPIKey(String oktaAPIKey) {
		this.oktaAPIKey = oktaAPIKey;
	}

	private void importRecordsFromFile(String fileName) throws EmployeeImportException {
		LOGGER.debug("Importing records from file: " + fileName);
		FileReader fileReader = null;
		CSVParser csvFileParser = null;
		try {
			CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader().withIgnoreSurroundingSpaces(true);
			File csvFileObj = new File(fileName);
			fileReader = new FileReader(csvFileObj);
			csvFileParser = new CSVParser(fileReader, csvFileFormat);
			Hashtable<String,ADPUser> adpUsersFromFile = new Hashtable<String,ADPUser>();
			Vector<String> sortedManagerIds = new Vector<String>();
			for (CSVRecord record : csvFileParser) {
				ADPUser adpUser = EmployeeUtility.constructEmployee(record);
				adpUsersFromFile.put(adpUser.getEmployeeID(),adpUser);
				if(sortedManagerIds.contains(adpUser.getManagerID())) {
					int mgrIdIndx = sortedManagerIds.indexOf(adpUser.getManagerID());
					sortedManagerIds.add(mgrIdIndx+1,adpUser.getEmployeeID());
				} else {
					sortedManagerIds.add(0,adpUser.getEmployeeID());
				}
			}
			LOGGER.debug("Successfully read all the employee records from the file: " + fileName);
			LOGGER.info("Total records in the file " + fileName + " :" + adpUsersFromFile.size());
			if (adpUsersFromFile.size()>getTreatAsFullFile()) {
				largeFileLoad = true;
				LOGGER.info("Total records in the file exceeds 250, treating as a full file load. Emails will be bypassed");
			}
			if(adpUsersFromFile.size() > 0) {
				LOGGER.info("Sorting the records by manager reporting relationship.... ");
				List<ADPUser> sortedADPUsersFromFile = new ArrayList<ADPUser>();
				for (int i=0;i<sortedManagerIds.size();i++) {
					String mgrId = (String) sortedManagerIds.elementAt(i);
					ADPUser adpU = adpUsersFromFile.get(mgrId);
					sortedADPUsersFromFile.add(adpU);
				}
				LOGGER.info("Importing "+sortedADPUsersFromFile.size()+" user accounts.");
				for (int i=0;i<sortedManagerIds.size();i++) {
					String empid = (String) sortedManagerIds.get(i);
					ADPUser adpU = adpUsersFromFile.get(empid);
					System.out.println("   empid="+empid+"    mgrid="+adpU.getManagerID());
				}
				importEmployeeAccountsToOkta(sortedADPUsersFromFile);
			} else {
				deleteFile = true;
			}
		} catch (IOException ex) {
			LOGGER.error(ex.getMessage(), ex);
			ERROR_LOGGER.error(ex.getMessage());
			throw new EmployeeImportException(ex.getMessage(), ex);
		} finally {
			closeQuietly(fileReader, csvFileParser);
		}
	}
	
	private void importEmployeeAccountsToOkta(List<ADPUser> adpUsers) throws EmployeeImportException {
		LOGGER.debug("Importing employee accounts to Okta");
		for (ADPUser adpUser : adpUsers) {
			try {
    			User userFromOkta = checkIfUserRecordExistsInOkta(adpUser);
    			if (userFromOkta == null && adpUser.getEmployeeStatus().equals(EmployeeUtility.ADP_STATUS_INACTIVE)) {
    				LOGGER.info("User "+adpUser.getEmployeeID()+" in import file is inactive. No matching profile exists.....nothing to do.");
    			} else if (userFromOkta != null && userFromOkta.getStatus().equals(EmployeeUtility.OKTA_STATUS_DEPROVISIONED) &&
    							adpUser.getEmployeeStatus().equals(EmployeeUtility.ADP_STATUS_INACTIVE)){
    				if (userFromOkta.getProfile().getUnmapped().get("division").equals(EmployeeUtility.DIVISION_FIELD)) {
    					LOGGER.info("Field user "+adpUser.getEmployeeID()+" in import is inactive. Matching profile is decativated. Deleting profile.");
    					deleteUserRecordInUD(userFromOkta);
    				}
    				LOGGER.info("User "+adpUser.getEmployeeID()+" in import is inactive. Matching profile is decativated...nothing to do.");
    			} else if (userFromOkta != null && userFromOkta.getStatus().equals(EmployeeUtility.OKTA_STATUS_DEPROVISIONED) &&
    						adpUser.getEmployeeStatus().equals(EmployeeUtility.ADP_STATUS_ACTIVE)){
    				LOGGER.info("Re-activating user with employee-id: " + adpUser.getEmployeeID());
					updateUserRecordInUD(adpUser, userFromOkta);
    			} else {
    				if (userFromOkta == null) {
    					LOGGER.info("No user found with employee-id: " + adpUser.getEmployeeID());
    					createUserRecordInUD(adpUser);
    				} else {
    					LOGGER.info("Updating user with employee-id: " + adpUser.getEmployeeID());
    					updateUserRecordInUD(adpUser, userFromOkta);
    				}
    			}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
				ERROR_LOGGER.error("Error processing data for "+adpUser.getEmployeeID()+"   "+e.getMessage());
			}
		}
	}
	
	private void deleteUserRecordInUD(User userFromOkta) throws EmployeeImportException {
		try {
			LOGGER.info("Deleteing profile for user "+ userFromOkta.getProfile().getLogin());
			userAPIClient.deleteUser(userFromOkta.getId());
			LOGGER.info("Profile deleted");
		}
		catch (IOException ioEx) {
			LOGGER.error(ioEx.getMessage(), ioEx);
			if (!largeFileLoad) {
    			NotificationMessage message = new NotificationMessage();
    			StringBuffer buffy = new StringBuffer();
    			buffy.append("Unable to delete employee profile in UD for: " + userFromOkta.getProfile().getLogin()+"\n\n");
    			String stackTrace = ExceptionUtils.getStackTrace(ioEx);
    			buffy.append(ioEx.getMessage()+"\n"+stackTrace);
    			message.setMessageBody(buffy.toString());
    			message.setSubjectLine("[Error] on server "+getComputerName());
    			notifier.sendNotification(message);
			}
			if (stopProcessingFileOnError) {
				LOGGER.info("Stop processing file on error is set. Ignoring other records.");
				throw new EmployeeImportException(ioEx.getMessage(), ioEx);
			}
		}
	}
	
	private void scheduleDelayedProfileDelete(User userFromOkta) {
		LOGGER.info("Received request to schedule a delayed profile deletion for field user "+userFromOkta.getProfile().getLogin());
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					LOGGER.info("Executing delayed profile deletion for field user "+userFromOkta.getProfile().getLogin());
					deleteUserRecordInUD(userFromOkta);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					NotificationMessage message = new NotificationMessage();
					StringBuffer buffy = new StringBuffer();
					buffy.append("Unable to execute delayed profile deletion for field user " + userFromOkta.getProfile().getLogin()+"\n\n");
					String stackTrace = ExceptionUtils.getStackTrace(e);
					buffy.append(e.getMessage()+"\n"+stackTrace);
					message.setMessageBody(buffy.toString());
					message.setSubjectLine("[Error] on server "+getComputerName());
					notifier.sendNotification(message);
				}
				LOGGER.info("Completed delayed profile deletion for field user "+userFromOkta.getProfile().getLogin());
			}
		};
		
		Timer timer = new Timer();
		timer.schedule(task, ONE_MINUTE_DELAY);
	}

	private void updateUserRecordInUD(ADPUser adpUser, User userFromOkta) throws EmployeeImportException {
		LOGGER.debug("Updating user record: " + adpUser.getEmployeeID());
		try {
			UserProfile userProfileFromOkta = userFromOkta.getProfile();
			boolean contractorStatus = (boolean) userProfileFromOkta.getUnmapped().get("Contractor");
			if(adpUser.getEmployeeStatus().equals(EmployeeUtility.ADP_STATUS_ACTIVE) && 
					(adpUser.getManagerID() == null || adpUser.getManagerID().equals(""))) {
				ERROR_LOGGER.error("Record for "+userProfileFromOkta.getUnmapped().get("employeeID")+ " does not have a manager id");
				LOGGER.error("Record for "+userProfileFromOkta.getUnmapped().get("employeeID")+ " does not have a manager id");
			} else {
    			String accountSuffix = getAccountSuffix(adpUser);
    			String currentEmail = userProfileFromOkta.getEmail();
    			String emailName = currentEmail.substring(0,currentEmail.indexOf("@"));
    			String currentEmailAddy = userProfileFromOkta.getEmail().toLowerCase();
    			EmployeeUtility.constructOktaUserProfile(adpUser, userProfileFromOkta, accountSuffix, oktaProfileConfig);
    			String calculatedEmail = constructEmailAddress(userProfileFromOkta,accountSuffix).toLowerCase();
    			boolean contractorStatusPostConstruct = (boolean) userProfileFromOkta.getUnmapped().get("Contractor");
    			
    			if (isCurrentEmailUser(emailName)) {
    				if (!currentEmailAddy.equals(calculatedEmail)) {
    					if (!largeFileLoad) {
            				NotificationMessage message = new NotificationMessage();
            				StringBuffer buffy = new StringBuffer();
            				buffy.append("An update record for employee (" + adpUser.getEmployeeID()+") "+adpUser.getFirstName()+" "+adpUser.getLastName()+" has been processed. \n");
            				buffy.append("The calculated username and email address("+calculatedEmail+") for this employee does not match the current username and email address("+currentEmailAddy+").\n");
            				buffy.append("Manual intervention may be required to resolve this.");
            				message.setMessageBody(buffy.toString());
            				message.setSubjectLine("Update to user profile for employee "+adpUser.getEmployeeID());
            				notifier.sendReactivationNotification(message);
    					}
    				}
    			} else if(oktaProfileConfig.getBoVector().contains(adpUser.getJobFamily())) {
    				userProfileFromOkta.setLogin(adpUser.getEmployeeID()+"@"+accountSuffix);
    				userProfileFromOkta.getUnmapped().put("samAccountName",adpUser.getEmployeeID());
					String emailAddy = constructEmailAddress(userProfileFromOkta,accountSuffix);
					userProfileFromOkta.setEmail(emailAddy);
    			} else if (Integer.parseInt(adpUser.getLineOfBusiness(),10) == LINE_OF_BUSINESS_ANOW ||
						Integer.parseInt(adpUser.getLineOfBusiness(),10) == LINE_OF_BUSINESS_PREFERRED_LEASE) {
    				accountSuffix = getAccountSuffix(adpUser);
    				if (userProfileFromOkta.getUnmapped().get("jobRole").equals(JOB_ROLE_DM)) {
    					String email = constructEmailAddress(userProfileFromOkta,accountSuffix);
    					userProfileFromOkta.setEmail(email);
    					userProfileFromOkta.getUnmapped().put("samAccountName", adpUser.getEmployeeID());
    				} else {
    					userProfileFromOkta.setEmail(adpUser.getEmployeeID()+"@"+accountSuffix);
    				}
    				userProfileFromOkta.setLogin(adpUser.getEmployeeID()+"@"+accountSuffix);
    				
				} else if (Integer.parseInt(adpUser.getLineOfBusiness(),10) == LINE_OF_BUSINESS_MEXICO_PREFERRED_LEASE &&
    						userProfileFromOkta.getUnmapped().get("jobRole").equals(JOB_ROLE_DM)) {
					accountSuffix = getAccountSuffix(adpUser);
					String email = constructEmailAddress(userProfileFromOkta,accountSuffix);
					userProfileFromOkta.setEmail(email);
					userProfileFromOkta.setLogin(adpUser.getEmployeeID()+"@"+accountSuffix);
				} else if (Integer.parseInt(adpUser.getLineOfBusiness(),10) == LINE_OF_BUSINESS_MEXICO_PREFERRED_LEASE) {
					if(oktaProfileConfig.getHoVector().contains(adpUser.getJobFamily())) {
						userProfileFromOkta.getUnmapped().put("division", EmployeeUtility.DIVISION_HOME_OFFICE);
						accountSuffix = getAccountSuffix(adpUser);
    					String email = constructEmailAddress(userProfileFromOkta,accountSuffix);
    					userProfileFromOkta.setEmail(email);
    					userProfileFromOkta.setLogin(email);
					}
				} else {
    				userProfileFromOkta.setSecondEmail(getStoreEmailAddress(adpUser.getDepartmentID()));
    			}
    			
    			if (deptMap.containsKey(adpUser.getDepartment())) {
    				Department dept = deptMap.get(adpUser.getDepartment());
    				if (dept.getDepartmentTypeInfo().getCode().equals("HYBSTR")) {
    					userProfileFromOkta.getUnmapped().put("samAccountName", adpUser.getEmployeeID());
    				}
    				userProfileFromOkta.getUnmapped().put("departmentType", dept.getDepartmentTypeInfo().getCode());
    			}
    			
    			constructManagerCN(userProfileFromOkta, adpUser);
    			userFromOkta.setProfile(userProfileFromOkta);
    			if (adpUser.getEmployeeStatus().equals(EmployeeUtility.ADP_STATUS_ACTIVE)) {
    				LOGGER.info("User account is active in ADP Updating all the attributes for the user account: "+ adpUser.getEmployeeID());
    				if (userFromOkta.getStatus().equals(EmployeeUtility.OKTA_STATUS_PROVISIONED)) {
    					userFromOkta.setStatus(EmployeeUtility.OKTA_STATUS_ACTIVE);
    					scheduleDelayedUpdate(userFromOkta);
    				} else {
    					if (userFromOkta.getStatus().equals(EmployeeUtility.OKTA_STATUS_DEPROVISIONED)) {
    						scheduleReactivationProfilePrep(userFromOkta);
    					} else {
    						userAPIClient.updateUser(userFromOkta);
    						if (contractorStatus != contractorStatusPostConstruct){
    							 notifyContractorFlip(userProfileFromOkta);
    						}
    					}
    				}
    			} else {
    				LOGGER.info("User account is inactive in ADP. Deactivating user account: " + adpUser.getEmployeeID());
    				
    				if (userFromOkta.getProfile().getUnmapped().get("division").equals(EmployeeUtility.DIVISION_FIELD)) {
    					userAPIClient.deactivateUser(userFromOkta.getId());
    					if (userFromOkta.getProfile().getUnmapped().get("lineOfBusiness").equals(LINE_OF_BUSINESS_PREFERRED_LEASE) &&
    							userFromOkta.getProfile().getUnmapped().get("samAccountName").equals(userFromOkta.getProfile().getUnmapped().get("employeeID"))) {
    						//must be a field user with an AD account
    						//do nothing
    					} else {
    						scheduleDelayedProfileDelete(userFromOkta);
    					}
    				} else {
    					String racAdDN = (String) userFromOkta.getProfile().getUnmapped().get("racADDN");
    					String cn = racAdDN.substring(3, racAdDN.indexOf("OU")-1);
    					String currentOU = racAdDN.substring(racAdDN.indexOf("OU"),racAdDN.indexOf("DC")-1);
    					/*for (String ou : employeeADService.getActiveOUs()) {
    						if (racAdDN.contains(ou)) {
    							currentOU = ou;
    							break;
    						}
    					}*/
    					scheduleDelayedMoveToDisabledOU(cn, currentOU);
    					userFromOkta.getProfile().getUnmapped().put("lastupdated", (new Date().getTime()));
    					userAPIClient.deactivateUser(userFromOkta.getId());
    				}
    			}
    
    			LOGGER.info("Successfully completed update tasks in Okta for: " + userProfileFromOkta.getLogin());
			}
		} catch (IOException ioEx) {
			ERROR_LOGGER.error("Update profile failed for employee "+adpUser.getEmployeeID()+". "+ioEx.getMessage());
			LOGGER.error(ioEx.getMessage(), ioEx);
			if (!largeFileLoad) {
    			NotificationMessage message = new NotificationMessage();
    			StringBuffer buffy = new StringBuffer();
    			buffy.append("Unable to update employee in UD for: " + adpUser.getEmployeeID()+"\n\n");
    			String stackTrace = ExceptionUtils.getStackTrace(ioEx);
    			buffy.append(ioEx.getMessage()+"\n"+stackTrace);
    			message.setMessageBody(buffy.toString());
    			message.setSubjectLine("[Error] on server "+getComputerName());
    			notifier.sendNotification(message);
			}
			if (stopProcessingFileOnError) {
				LOGGER.info("Stop processing file on error is set. Ignoring other records.");
				throw new EmployeeImportException(ioEx.getMessage(), ioEx);
			}
		} catch (Exception e) {
			ERROR_LOGGER.error("Update profile failed for employee "+adpUser.getEmployeeID()+". "+e.getMessage());
			LOGGER.error(e.getMessage(), e);
			if (!largeFileLoad) {
    			NotificationMessage message = new NotificationMessage();
    			StringBuffer buffy = new StringBuffer();
    			buffy.append("Unable to update employee in UD for: " + adpUser.getEmployeeID()+"\n\n");
    			String stackTrace = ExceptionUtils.getStackTrace(e);
    			buffy.append(e.getMessage()+"\n"+stackTrace);
    			message.setMessageBody(buffy.toString());
    			message.setSubjectLine("[Error] on server "+getComputerName());
    			notifier.sendNotification(message);
			}
			if (stopProcessingFileOnError) {
				LOGGER.info("Stoped processing file on error is set. Ignoring other records.");
				throw new EmployeeImportException(e.getMessage(), e);
			}
		}
	}
	
	private void notifyContractorFlip(UserProfile userProfileFromOkta) {
		NotificationMessage message = new NotificationMessage();
		StringBuffer buffy = new StringBuffer();
		buffy.append("An update record for employee (" + userProfileFromOkta.getUnmapped().get("employeeID")+") "+userProfileFromOkta.getFirstName()+" "+userProfileFromOkta.getLastName()+" has been processed. \n");
		buffy.append("Manual intervention may be required to resolve this.");
		message.setMessageBody(buffy.toString());
		message.setSubjectLine("EmployeeId " + userProfileFromOkta.getUnmapped().get("employeeID") + " has been flipped from contractor to permanent");
		notifier.sendNotification(message);		
	}

	private boolean isEmailUser(int lob, String jobRole) {
		boolean isEmailUser = false;
		if (jobRole.equals(JOB_ROLE_VP)) {
			isEmailUser = true;
		} else if (lobList.contains(lob)) {
			isEmailUser = true;
		}
		return isEmailUser;
	}
	
	private boolean isCurrentEmailUser(String userName) {
		boolean isCurrentEmailUser = false;
		try {
			int empIdFromEmailName = Integer.parseInt(userName,10);//quick and dirty way to determine if the left side of the email addy is text or not
		} catch (NumberFormatException nfe) {
			isCurrentEmailUser = true;
		}
		return isCurrentEmailUser;
	}

	private void createUserRecordInUD(ADPUser adpUser) throws EmployeeImportException {
		LOGGER.debug("Creating user for employee with id: "+adpUser.getEmployeeID());
		String accountSuffix = defaultUserAccountSuffix;
		LOGGER.debug("Using "+accountSuffix+" as the default.");
		try {
			UserProfile userProfile = EmployeeUtility.constructOktaUserProfile(adpUser, null, accountSuffix, oktaProfileConfig);
			if(adpUser.getEmployeeStatus().equals(EmployeeUtility.ADP_STATUS_ACTIVE) &&
					(userProfile.getUnmapped().get("manager") == null ||
					userProfile.getUnmapped().get("manager").equals(""))) {
				ERROR_LOGGER.error("Record for "+userProfile.getUnmapped().get("employeeID")+ " does not have a manager id");
				LOGGER.error("Record for "+userProfile.getUnmapped().get("employeeID")+ " does not have a manager id");
			} else {
    			if (isEmailUser(Integer.parseInt((String) userProfile.getUnmapped().get("lineOfBusiness"),10),
    							(String) userProfile.getUnmapped().get("jobRole"))) {
    				accountSuffix = getAccountSuffix(adpUser);
    				String email = constructEmailAddress(userProfile,accountSuffix);
    				userProfile.setEmail(email);
    				if (Integer.parseInt(adpUser.getLineOfBusiness(),10) == LINE_OF_BUSINESS_FSC ||
    						Integer.parseInt(adpUser.getLineOfBusiness(),10) == LINE_OF_BUSINESS_NPS ||
    						Integer.parseInt(adpUser.getLineOfBusiness(),10) == LINE_OF_BUSINESS_MEXICO_NPS ||
    						Integer.parseInt(adpUser.getLineOfBusiness(),10) == LINE_OF_BUSINESS_FRANCHISING) {
    					createSamAccountName(userProfile, adpUser);
    				} else if (((String) userProfile.getUnmapped().get("jobRole")).equals(JOB_ROLE_VP)) {
    					createSamAccountName(userProfile, adpUser);
    				}
    				if (Integer.parseInt(adpUser.getLineOfBusiness(),10) == LINE_OF_BUSINESS_ANOW) {
    					userProfile.setLogin(adpUser.getEmployeeID()+"@"+accountSuffix);
    				} else {
    					userProfile.setLogin(email);
    				}
    			} else {
    				userProfile.setEmail(adpUser.getEmployeeID()+"@"+accountSuffix);
    				userProfile.setLogin(adpUser.getEmployeeID()+"@"+accountSuffix);
    				LOGGER.debug("Creating user for employee with default login: "+userProfile.getLogin());
    				LOGGER.debug("Creating user for employee with default primary email: "+userProfile.getEmail());
    				LOGGER.debug("Checking for special cases to login/email defaults.");
    				if (oktaProfileConfig.getBoVector().contains(userProfile.getUnmapped().get("jobFamily"))) {
    					LOGGER.debug("Provisioning Franchise Back Office with default domain.");
    					userProfile.setLogin(adpUser.getEmployeeID()+"@"+accountSuffix);
    					userProfile.getUnmapped().put("samAccountName",adpUser.getEmployeeID());
    					String email = constructEmailAddress(userProfile,accountSuffix);
    					userProfile.setEmail(email);
    				} else if (userProfile.getUnmapped().get("jobRole").equals(JOB_ROLE_STORE_MANAGER)) {
    					userProfile.setEmail(adpUser.getEmployeeID()+"@"+accountSuffix);
    					userProfile.setLogin(adpUser.getEmployeeID()+"@"+accountSuffix);
    				} else if (Integer.parseInt(adpUser.getLineOfBusiness(),10) == LINE_OF_BUSINESS_RACFI) {
    					if (Integer.parseInt((String) userProfile.getUnmapped().get("employeeNumber"),10) == 7) {
    						String email = constructEmailAddress(userProfile,accountSuffix);
    						userProfile.setEmail(email);
    						userProfile.setLogin(email);
    						createSamAccountName(userProfile, adpUser);
    					} else {
    						userProfile.setEmail(adpUser.getEmployeeID()+"@"+accountSuffix);
    						userProfile.setLogin(adpUser.getEmployeeID()+"@"+accountSuffix);
    					}
    				} else if ((Integer.parseInt(adpUser.getLineOfBusiness(),10) == LINE_OF_BUSINESS_ANOW ||
    						Integer.parseInt(adpUser.getLineOfBusiness(),10) == LINE_OF_BUSINESS_PREFERRED_LEASE) &&
    						userProfile.getUnmapped().get("jobRole").equals(JOB_ROLE_DM)) {
    					accountSuffix = getAccountSuffix(adpUser);
    					String email = constructEmailAddress(userProfile,accountSuffix);
    					userProfile.setEmail(email);
    					userProfile.setLogin(adpUser.getEmployeeID()+"@"+accountSuffix);
    					userProfile.getUnmapped().put("samAccountName", adpUser.getEmployeeID());
    				} else if (Integer.parseInt(adpUser.getLineOfBusiness(),10) == LINE_OF_BUSINESS_MEXICO_PREFERRED_LEASE &&
    							userProfile.getUnmapped().get("jobRole").equals(JOB_ROLE_DM)) {
    					accountSuffix = getAccountSuffix(adpUser);
    					String email = constructEmailAddress(userProfile,accountSuffix);
    					userProfile.setEmail(email);
    					userProfile.setLogin(adpUser.getEmployeeID()+"@"+accountSuffix);
    				} else if (Integer.parseInt(adpUser.getLineOfBusiness(),10) == LINE_OF_BUSINESS_MEXICO_PREFERRED_LEASE) {
    					if(oktaProfileConfig.getHoVector().contains(adpUser.getJobFamily())) {
    						userProfile.getUnmapped().put("division", EmployeeUtility.DIVISION_HOME_OFFICE);
    						accountSuffix = getAccountSuffix(adpUser);
    						createSamAccountName(userProfile, adpUser);
        					String email = constructEmailAddress(userProfile,accountSuffix);
        					userProfile.setEmail(email);
        					userProfile.setLogin(email);
    					}
    				} 
    				LOGGER.debug("Creating user for employee with login: "+userProfile.getLogin());
    				LOGGER.debug("Creating user for employee with primary email: "+userProfile.getEmail());
    				if (Integer.parseInt((String) userProfile.getUnmapped().get("employeeNumber"),10) == 6) {
    					userProfile.setSecondEmail(getStoreEmailAddress(adpUser.getDepartmentID()));
    				}
    			}
    			
    			if (deptMap.containsKey(adpUser.getDepartment())) {
    				Department dept = deptMap.get(adpUser.getDepartment());
    				if (dept.getDepartmentTypeInfo().getCode().equals("HYBSTR")) {
    					userProfile.getUnmapped().put("samAccountName", adpUser.getEmployeeID());
    				}
    				userProfile.getUnmapped().put("departmentType", dept.getDepartmentTypeInfo().getCode());
    			}
    			
    			constructManagerCN(userProfile, adpUser);
    			LoginCredentials loginCredentials = new LoginCredentials();
    			Password password = new Password();
    			password.setValue(passwordGenerator.generatePassword());
    			loginCredentials.setPassword(password);
    			User user = new User();
    			user.setProfile(userProfile);
    			user.setCredentials(loginCredentials);
    			String pwd = adpUser.getFirstName().substring(0, 1).toUpperCase()+
    					adpUser.getLastName().substring(0, 1).toLowerCase()+adpUser.getAdjHireDate();
    			User newUser = userAPIClient.createUser(user, true);
    			newUser.setProfile(userProfile);
    			scheduleDelayedPasswordSet(newUser,pwd);
    			LOGGER.info("Successfully created employee account in Okta for: " + userProfile.getLogin());
			}
		} catch (IOException ioEx) {
			ERROR_LOGGER.error("Create profile failed for employee "+adpUser.getEmployeeID()+". "+ioEx.getMessage());
			LOGGER.error(ioEx.getMessage(), ioEx);
			if (!largeFileLoad) {
    			NotificationMessage message = new NotificationMessage();
    			StringBuffer buffy = new StringBuffer();
    			buffy.append("Unable to create employee in UD for: " + adpUser.getEmployeeID()+"\n\n");
    			String stackTrace = ExceptionUtils.getStackTrace(ioEx);
    			buffy.append(ioEx.getMessage()+"\n"+stackTrace);
    			message.setMessageBody(buffy.toString());
    			message.setSubjectLine("[Error] on server "+getComputerName());
    			notifier.sendNotification(message);
			}
			if (stopProcessingFileOnError) {
				LOGGER.info("Stop file processing on error set. Ignoring subsequent records from the file.");
				throw new EmployeeImportException(ioEx.getMessage(), ioEx);
			}
		} catch (Exception e) {
			ERROR_LOGGER.error("Create profile failed for employee "+adpUser.getEmployeeID()+". "+e.getMessage());
			LOGGER.error(e.getMessage(), e);
			if (!largeFileLoad) {
    			NotificationMessage message = new NotificationMessage();
    			StringBuffer buffy = new StringBuffer();
    			buffy.append("Unable to create employee in UD for: " + adpUser.getEmployeeID()+"\n\n");
    			String stackTrace = ExceptionUtils.getStackTrace(e);
    			buffy.append(e.getMessage()+"\n"+stackTrace);
    			message.setMessageBody(buffy.toString());
    			message.setSubjectLine("[Error] on server "+getComputerName());
    			notifier.sendNotification(message);
			}
			if (stopProcessingFileOnError) {
				LOGGER.info("Stoped processing file on error is set. Ignoring other records.");
				throw new EmployeeImportException(e.getMessage(), e);
			}
		}
	}
	
	private String getAccountSuffix(ADPUser adpUser) {
		String accountSuffix = null;
		//   the ugly debug messages are here for testing purposes.
		//   we only have 1 email domain in test so we can't provision the actual email address
		//   this is to be sure the proper email domain will be used in prod.
		if (getPlDomainExceptions().contains(adpUser.getDepartment())) {
			accountSuffix = getPlDomain();
			LOGGER.debug("**********Provisioning exception email domain of Preferred Lease for dept="+adpUser.getDepartment()+" **********");
		} else if (getRacDomainExceptions().contains(adpUser.getDepartment())) {
			accountSuffix = getRacDomain();
			LOGGER.debug("**********Provisioning exception email domain of RAC for dept="+adpUser.getDepartment()+" **********");
		} else if(oktaProfileConfig.getBoVector().contains(adpUser.getJobFamily())) {
			accountSuffix = getRacDomain();
			LOGGER.debug("**********Provisioning Franchise Back Office email domain of rentacenter.com.**********");
		} else if (oktaProfileConfig.getDvpVector().contains(adpUser.getJobFamily())){//failing the 3 tests above, all execs are provisioned as FSC without regard to LOB
			accountSuffix = getRacDomain();
			LOGGER.debug("**********Provisioning email domain for RAC.**********");
		} else if (adpUser.getDepartment().startsWith(MP_DEPARTMENT_PREFIX)) {//this is an asinine way to determine domain assignment but it's all i have
			accountSuffix = getPlDomain();
			LOGGER.debug("**********Provisioning email domain for Preferred Lease based on dept="+adpUser.getDepartment()+" **********");
		} else {
			int userLOB = Integer.parseInt(adpUser.getLineOfBusiness(),10);
			
			switch(userLOB) {
			
    			case LINE_OF_BUSINESS_NPS:
    				accountSuffix = getNpsDomain();
    				LOGGER.debug("**********Provisioning email domain for NPS.**********");
    				break;
    				
    			case LINE_OF_BUSINESS_FSC:
    				accountSuffix = getRacDomain();
    				LOGGER.debug("**********Provisioning email domain for RAC.**********");
    				break;
    				
    			case LINE_OF_BUSINESS_ANOW:
    				accountSuffix = getPlDomain();
    				LOGGER.debug("**********Provisioning Preferred Lease email domain for ANOW.**********");
    				break;
    				
    			case LINE_OF_BUSINESS_RACFI:
    				accountSuffix = getRacDomain();
    				LOGGER.debug("**********Provisioning email domain for RAC.**********");
    				break;
    				
    			case LINE_OF_BUSINESS_FRANCHISING:
    				accountSuffix = getRacFranchisingDomain();
    				LOGGER.debug("**********Provisioning email domain for FRANCHISING.**********");
    				break;
    				
    			case LINE_OF_BUSINESS_MEXICO_NPS:
    				accountSuffix = getNpsDomain();
    				LOGGER.debug("**********Provisioning email domain for Mexico NPS.**********");
    				break;
    				
    			case LINE_OF_BUSINESS_PREFERRED_LEASE:
    				accountSuffix = getPlDomain();
    				LOGGER.debug("**********Provisioning email domain for Preferred Lease.**********");
    				break;
    				
    			case LINE_OF_BUSINESS_MEXICO_PREFERRED_LEASE:
    				accountSuffix = getPlDomain();
    				LOGGER.debug("**********Provisioning email domain for Mexico Preferred Lease.**********");
    				break;
    				
    			default:
    				accountSuffix = defaultUserAccountSuffix;
    				LOGGER.debug("**********Provisioning email domain for default field user.**********");
			}
		}
		LOGGER.debug("Provisioning email domain "+accountSuffix);
		return accountSuffix;
	}
	
	private String constructEmailAddress(UserProfile userProfile, String accountSuffix) throws IOException {
		String firstDotLast = ((String) userProfile.getUnmapped().get("nickName")).replace(" ","")+"."+userProfile.getLastName().replace(" ", "");
		String email = firstDotLast.toLowerCase()+"@"+accountSuffix; //sathishKumar@rac.com
		if (userProfile.getEmail() != null) {
			if (userProfile.getEmail().toLowerCase().equals(email)) {
				return email;
			} else {
				if (userProfile.getEmail().startsWith(firstDotLast.toLowerCase())) {
					return email;//sathishKumar@rac.com
				} 
			}
		} 
		email = findUniqueAddress(firstDotLast)+"@"+accountSuffix;
		return email;
	} 
	
	private String findUniqueAddress(String firstDotLast) throws IOException {
		String email = firstDotLast;
		//check for similar login id's
		FilterBuilder filterBuilder = new FilterBuilder("profile.email sw \"" + firstDotLast + "\"");
		List<User> usersEmailAddress = userAPIClient.getUsersWithAdvancedSearch(filterBuilder);
		if (usersEmailAddress.size() != 0) {
			LOGGER.info("User accounts with similar email found!");
			int index = usersEmailAddress.size();
			email = email + index;
		}
		return email; 
	}
	
	private String getStoreEmailAddress(String location) throws IOException {
		String storeEmail = "";
		FilterBuilder filterBuilder = new FilterBuilder("profile.login sw \"Store." +location+ "\"");
		List<User> storeUserList = userAPIClient.getUsersWithAdvancedSearch(filterBuilder);
		if (storeUserList.size() != 0) {
			LOGGER.info("Store account found for Store."+location);
			User storeUser = storeUserList.get(0);
			storeEmail = storeUser.getProfile().getEmail();
		}
		return storeEmail;
	}
	
	private void scheduleDelayedMoveToEnabledOU(String cn, String targetOU) {
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					LOGGER.info("Executing delayed move to enabled OU for user "+cn);
					employeeADService.moveToEnabledOU(cn, targetOU);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					NotificationMessage message = new NotificationMessage();
					StringBuffer buffy = new StringBuffer();
					buffy.append("Unable to execute move to enabled OU for: " + cn+"\n\n");
					String stackTrace = ExceptionUtils.getStackTrace(e);
					buffy.append(e.getMessage()+"\n"+stackTrace);
					message.setMessageBody(buffy.toString());
					message.setSubjectLine("[Error] on server "+getComputerName());
					notifier.sendNotification(message);
				}
				LOGGER.info("Completed delayed move to enabled OU for user "+cn);
			}
		};
		
		Timer timer = new Timer();
		timer.schedule(task, FIVE_MINUTE_DELAY);
	}
	
	private void scheduleDelayedMoveToDisabledOU(String cn, String currentOU) {
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					LOGGER.info("Executing delayed move to disabled OU for user "+cn);
					employeeADService.moveToDisabledOU(cn, currentOU);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					NotificationMessage message = new NotificationMessage();
					StringBuffer buffy = new StringBuffer();
					buffy.append("Unable to execute move to disabled OU for: " + cn+"\n\n");
					String stackTrace = ExceptionUtils.getStackTrace(e);
					buffy.append(e.getMessage()+"\n"+stackTrace);
					message.setMessageBody(buffy.toString());
					message.setSubjectLine("[Error] on server "+getComputerName());
					notifier.sendNotification(message);
				}
				LOGGER.info("Completed delayed move to disabled OU for user "+cn);
			}
		};
		
		Timer timer = new Timer();
		timer.schedule(task, THREE_MINUTE_DELAY);
		LOGGER.info("Delayed move to disabled OU scheduled for user "+cn);
	}
	
	private void scheduleReactivationProfilePrep(User userFromOkta) throws EmployeeImportException {
		LOGGER.info("Received request to prep profile for re-activation update for user "+userFromOkta.getProfile().getLogin());
		String racAdDn = (String) userFromOkta.getProfile().getUnmapped().get("racADDN");
		if (racAdDn != null) {
			try {
				scheduleDelayedProfileDelete(userFromOkta);
			} catch (Exception e) {
				ERROR_LOGGER.error("Error deleting old AD mastered profile for "+userFromOkta.getProfile().getLogin());
				return;
			}
			
			String cn = racAdDn.substring(3, racAdDn.indexOf("OU")-1);
			String currentOU = racAdDn.substring(racAdDn.indexOf("OU"),racAdDn.indexOf("DC")-1);
			scheduleDelayedMoveToEnabledOU(cn,currentOU);
			scheduleReactivationDelayedUpdate(userFromOkta);
		}
	}

	private void scheduleReactivationDelayedUpdate(User userFromOkta) throws EmployeeImportException {
		LOGGER.info("Received request to schedule delayed update for user "+userFromOkta.getProfile().getLogin()+" "+userFromOkta.getProfile().getUnmapped().get("employeeID"));
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					LOGGER.info("Executing delayed update of profile for user "+userFromOkta.getProfile().getLogin()+" "+userFromOkta.getProfile().getUnmapped().get("employeeID"));
					LOGGER.info("Retrieving new Okta uid for "+userFromOkta.getProfile().getLogin()+" "+userFromOkta.getProfile().getUnmapped().get("employeeID"));
					String empid = (String) userFromOkta.getProfile().getUnmapped().get("employeeID");
					FilterBuilder filterBuilder = new FilterBuilder("profile.employeeID eq \"" + empid + "\"");
					List<User> users = userAPIClient.getUsersWithAdvancedSearch(filterBuilder);
					if (users.isEmpty()) {
						LOGGER.info("Unable to find profile in the UD with employee id: "+empid);
						triggerNewFile(empid);
						//throw new EmployeeImportException(new Throwable("Unable to find profile in the UD with employee id: "+empid));
					} else {
						LOGGER.info("User profile found in UD with employee id: "+empid);
						userFromOkta.setId(users.get(0).getId());
						userAPIClient.updateUser(userFromOkta);
						String defaultPwd = userFromOkta.getProfile().getFirstName().substring(0, 1).toUpperCase()+
								userFromOkta.getProfile().getLastName().substring(0, 1).toLowerCase()+
								userFromOkta.getProfile().getUnmapped().get("hireDate");
						scheduleDelayedPasswordSet(userFromOkta,defaultPwd);
					}
				} catch (ApiException apie) {
					String errorCode = apie.getMessage();
					if (errorCode.contains("E0000112")) {
						scheduleDelayedUpdate(userFromOkta);
					} else {
						String defaultPwd = userFromOkta.getProfile().getFirstName().substring(0, 1).toUpperCase()+
											userFromOkta.getProfile().getLastName().substring(0, 1).toLowerCase()+
											userFromOkta.getProfile().getUnmapped().get("hireDate");
						scheduleDelayedPasswordSet(userFromOkta,defaultPwd);
					}
					LOGGER.info("Delayed update for user "+userFromOkta.getProfile().getLogin()+" completed.");
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					NotificationMessage message = new NotificationMessage();
					StringBuffer buffy = new StringBuffer();
					buffy.append("Unable to execute delayed update of employee in UD for: " + userFromOkta.getProfile().getLogin()+"\n\n");
					String stackTrace = ExceptionUtils.getStackTrace(e);
					buffy.append(e.getMessage()+"\n"+stackTrace);
					message.setMessageBody(buffy.toString());
					message.setSubjectLine("[Error] on server "+getComputerName());
					notifier.sendNotification(message);
				}
			}
		};
		
		Timer timer = new Timer();
		timer.schedule(task, SEVENTY_FIVE_MIN_DELAY);
	}
	
	private void triggerNewFile(String empid) {
		LOGGER.info("Received request to trigger a new file update for employee id "+empid);
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					EmployeesFullData emps = hrDataService.getEmployee(empid);
					if (emps.getEmployees().isEmpty()) {
						LOGGER.error("Unable to find employee "+empid+" in the WorkerDB");
						NotificationMessage message = new NotificationMessage();
						StringBuffer buffy = new StringBuffer();
						buffy.append("Unable to trigger a new file for employee: " + empid +".  Employee not found in the WorkerDB\n\n");
						message.setMessageBody(buffy.toString());
						message.setSubjectLine("[Error] on server "+getComputerName());
						notifier.sendNotification(message);
					} else {
						EmployeeFullData emp = emps.getEmployees().get(0);
						int response = hrDataService.postWorkerToADP(emp.getExternalEmployeeId());
						if (response == 200) {
							LOGGER.info("New file triggered for employee "+empid);
						} else {
							throw new EmployeeImportException(new Throwable("Unable to trigger new file for employee id: "+empid+". PostWorker event failed."));
						}
					}
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					NotificationMessage message = new NotificationMessage();
					StringBuffer buffy = new StringBuffer();
					buffy.append("Unable to trigger a new file for employee: " + empid +"\n\n");
					String stackTrace = ExceptionUtils.getStackTrace(e);
					buffy.append(e.getMessage()+"\n"+stackTrace);
					message.setMessageBody(buffy.toString());
					message.setSubjectLine("[Error] on server "+getComputerName());
					notifier.sendNotification(message);
				}
			}
		};
		Timer timer = new Timer();
		timer.schedule(task, THREE_MINUTE_DELAY);
	}
	
	private void scheduleDelayedUpdate(User userFromOkta) {
		LOGGER.info("Received request to schedule delayed update for user "+userFromOkta.getProfile().getLogin());
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					LOGGER.info("Executing delayed update of profile for user "+userFromOkta.getProfile().getLogin());
					userAPIClient.updateUser(userFromOkta);
					String defaultPwd = userFromOkta.getProfile().getFirstName().substring(0, 1).toUpperCase()+
							userFromOkta.getProfile().getLastName().substring(0, 1).toLowerCase()+
							userFromOkta.getProfile().getUnmapped().get("hireDate");
					scheduleDelayedPasswordSet(userFromOkta,defaultPwd);
				} catch (ApiException apie) {
					String errorCode = apie.getMessage();
					if (errorCode.contains("E0000112")) {
						scheduleDelayedUpdate(userFromOkta);
					} else {
						String defaultPwd = userFromOkta.getProfile().getFirstName().substring(0, 1).toUpperCase()+
											userFromOkta.getProfile().getLastName().substring(0, 1).toLowerCase()+
											userFromOkta.getProfile().getUnmapped().get("hireDate");
						scheduleDelayedPasswordSet(userFromOkta,defaultPwd);
					}
					LOGGER.info("Delayed update for user "+userFromOkta.getProfile().getLogin()+" completed.");
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					NotificationMessage message = new NotificationMessage();
					StringBuffer buffy = new StringBuffer();
					buffy.append("Unable to execute delayed update of employee in UD for: " + userFromOkta.getProfile().getLogin()+"\n\n");
					String stackTrace = ExceptionUtils.getStackTrace(e);
					buffy.append(e.getMessage()+"\n"+stackTrace);
					message.setMessageBody(buffy.toString());
					message.setSubjectLine("[Error] on server "+getComputerName());
					notifier.sendNotification(message);
				}
			}
		};
		
		Timer timer = new Timer();
		timer.schedule(task, THREE_MINUTE_DELAY);
	}
	
	private void scheduleDelayedPasswordSet(User oktaUser, String pwd) {
		LOGGER.info("Received request to schedule a delayed password set for user "+oktaUser.getProfile().getLogin());
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					LOGGER.info("Executing delayed password set for user "+oktaUser.getProfile().getLogin());
					User user2 = userAPIClient.setPassword(oktaUser.getId(), pwd);
				} catch (ApiException apie) {
					LOGGER.error("Error attempting to set password for user "+oktaUser.getProfile().getLogin());
					apie.printStackTrace();
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					NotificationMessage message = new NotificationMessage();
					StringBuffer buffy = new StringBuffer();
					buffy.append("Unable to execute delayed pwd set of employee in UD for: " + oktaUser.getProfile().getLogin()+"\n\n");
					String stackTrace = ExceptionUtils.getStackTrace(e);
					buffy.append(e.getMessage()+"\n"+stackTrace);
					message.setMessageBody(buffy.toString());
					message.setSubjectLine("[Error] on server "+getComputerName());
					notifier.sendNotification(message);
				}
				LOGGER.info("Completed delayed password set for user "+oktaUser.getProfile().getLogin());
			}
		};
		
		Timer timer = new Timer();
		timer.schedule(task, TWO_MINUTE_DELAY);
	}
	
	//this method constructs the manager attribute which maps to the manager account in LDAP
	//and it constructs the managerAD attribute which maps to the manager account in AD
	private void constructManagerCN(UserProfile userProfile, ADPUser adpUser) throws IOException{
		String managerEmployeeID = adpUser.getManagerID();
		FilterBuilder filterBuilder = new FilterBuilder("profile.employeeID eq \"" + managerEmployeeID + "\"");
		List<User> users = userAPIClient.getUsersWithAdvancedSearch(filterBuilder);
		if (users.isEmpty()) {
			LOGGER.info("Manager user not found in UD with employee-id: "+managerEmployeeID);
		} else {
			LOGGER.info("Manager user found in UD with employee-id: "+managerEmployeeID);
			UserProfile manager = users.get(0).getProfile();
			userProfile.getUnmapped().put("managerAD", manager.getLogin());
		}
		String cn = "";
		if(managerEmployeeID != null && !managerEmployeeID.equals("")) {
			cn = "CN="+managerEmployeeID+","+getDefaultManagerCN();
		}
		userProfile.getUnmapped().put("manager",cn);
	}

	private void createSamAccountName(UserProfile userProfile, ADPUser adpUser)
			throws IOException {
		LOGGER.info("Creating SAMAccountName for FSC Employee with id: "+adpUser.getEmployeeID());
		String samAccountName = adpUser.getsAMAccountName();
		if(samAccountName == null || samAccountName.equals("")){
			samAccountName = (String)userProfile.getUnmapped().get("samAccountName");
		}
		if (samAccountName == null || samAccountName.equals("")) {
			samAccountName = constructSamAccountName(adpUser);
			FilterBuilder filterBuilder = new FilterBuilder("profile.samAccountName sw \"" + samAccountName + "\"");
			List<User> usersWithSamAccountName = userAPIClient.getUsersWithAdvancedSearch(filterBuilder);
			if (usersWithSamAccountName.size() != 0) {
				LOGGER.info("User accounts with similar sam account name found!");
				int kounter = 1;
				while (true) {
					filterBuilder = new FilterBuilder("profile.samAccountName sw \"" + samAccountName+kounter + "\"");
					usersWithSamAccountName = userAPIClient.getUsersWithAdvancedSearch(filterBuilder);
					if (usersWithSamAccountName.size() == 0) {
						samAccountName = samAccountName+kounter;
						break;
					}
    				kounter++;
				}
			}
		}
		samAccountName = samAccountName.toLowerCase();
		userProfile.getUnmapped().put("samAccountName", samAccountName);
		
		LOGGER.info("SAMAccount name set for the user with employee-id: "+adpUser.getEmployeeID());
	}

	private String constructSamAccountName(ADPUser adpUser) {
		String samAccountName = null;
		String firstName = adpUser.getFirstName();
		String lastName = adpUser.getLastName().replace(" ", "");
		lastName = lastName.replace("'", "");
		lastName = lastName.replace("-", "");
		if(lastName.length()>3) {
			samAccountName = lastName.toLowerCase().substring(0, 3);
		} else {
			samAccountName = adpUser.getLastName().toLowerCase();
		}
		if (firstName.length()>3) {
			samAccountName += adpUser.getFirstName().toLowerCase().substring(0, 3);
		} else {
			samAccountName += adpUser.getFirstName().toLowerCase();
		}
		
		return samAccountName;
	}

	private User checkIfUserRecordExistsInOkta(ADPUser adpUser) throws EmployeeImportException {
		LOGGER.debug("Checking if the user record exists in Okta");
		try {
			String employeeID = adpUser.getEmployeeID();
			FilterBuilder filterBuilder = new FilterBuilder("profile.employeeID eq \"" + employeeID + "\"");
			List<User> users = userAPIClient.getUsersWithAdvancedSearch(filterBuilder);
			if (users.isEmpty()) {
				// Checking to see if user account with samAccountName is
				// present
				LOGGER.info("User not found in UD with employee-id: "+employeeID);
				String samAccountName = adpUser.getsAMAccountName();
				if (samAccountName != null && !samAccountName.equals("")) {
					LOGGER.info("Checking to see if the user with samaccountname exists: "+samAccountName);
					FilterBuilder filterBuilderSamAccountName = new FilterBuilder(
							"profile.samAccountName eq \"" + samAccountName + "\"");
					List<User> usersWithSamAccountName = userAPIClient.getUsersWithAdvancedSearch(filterBuilderSamAccountName);
					if (!usersWithSamAccountName.isEmpty()) {
						LOGGER.info("User found in UD with samAccountName: "+samAccountName);
						return usersWithSamAccountName.get(0);
					}
				}
			} else {
				if (users.size() == 1) {
					LOGGER.info("User found in UD with employee-id: "+employeeID);
					return users.get(0);
				}
				LOGGER.info("Multiple Okta users found with employee id = "+employeeID);
				return users.get(0);
			}
		} catch (IOException ioEx) {
			LOGGER.error(ioEx.getMessage(), ioEx);
			NotificationMessage message = new NotificationMessage();
			StringBuffer buffy = new StringBuffer();
			buffy.append("Unable to check if the employee exists in the Universal Directory: " + adpUser.getEmployeeID()+"\n\n");
			String stackTrace = ExceptionUtils.getStackTrace(ioEx);
			buffy.append(ioEx.getMessage()+"\n"+stackTrace);
			message.setMessageBody(buffy.toString());
			message.setSubjectLine("[Error] on server "+getComputerName());
			notifier.sendNotification(message);
			if (stopProcessingFileOnError) {
				throw new EmployeeImportException(ioEx.getMessage(), ioEx);
			}
		}
		return null;
	}

	private Set<String> getListOfFiles() {
		LOGGER.debug("Getting the list of files to be imported");
		Set<String> listOfFilesToImport = new TreeSet<String>();
		File csvFileLocation = new File(localSourceFolder);
		if (csvFileLocation.exists()) {
			List<String> allFiles = Arrays.asList(csvFileLocation.list());
			for (String fileName : allFiles) {
				if (skipSFTPFilesForTest && fileName.startsWith("OktaEmployeeRecords")) {
					LOGGER.debug("Skip SFTP files set. Ignoring file: "+fileName);
					continue;
				}
				String[] fileNameParts = fileName.split("\\.");
				if (fileNameParts[0].endsWith(EmployeeUtility.IN_PROCESS_SUFFIX)) {
					LOGGER.debug("File ending with IN_PROCESS. Adding the file for processing: "+fileName);
					listOfFilesToImport.add(fileName);
				} else if (!fileNameParts[0].endsWith(EmployeeUtility.PROCESSED_SUFFIX)) {
					LOGGER.debug("Adding the file for processing: "+fileName);
					listOfFilesToImport.add(fileName);
				}
			}
		}
		LOGGER.info("List of files to be imported: " + listOfFilesToImport.toString());
		return listOfFilesToImport;
	}

	private static void closeQuietly(Closeable... closeables) {
		for (Closeable closeable : closeables) {
			if (closeable != null) {
				try {
					closeable.close();
				} catch (Exception ex) {
					LOGGER.warn("Error while closing the resource: ", ex);
				}
			}
		}
	}

	public boolean isStopProcessingAllFilesOnError() {
		return stopProcessingAllFilesOnError;
	}

	public void setStopProcessingAllFilesOnError(boolean stopProcessingAllFilesOnError) {
		this.stopProcessingAllFilesOnError = stopProcessingAllFilesOnError;
	}

	public boolean isStopProcessingFileOnError() {
		return stopProcessingFileOnError;
	}

	public void setStopProcessingFileOnError(boolean stopProcessingFileOnError) {
		this.stopProcessingFileOnError = stopProcessingFileOnError;
	}

	public ImportErrorNotifier getNotifier() {
		return notifier;
	}

	private String markFileAsInProcess(String fileName) throws IOException {
		String inProcessFileName = constructInProcessFileName(fileName);
		return renameFile(fileName, inProcessFileName);
	}

	private String constructInProcessFileName(String csvFileName) {
		String[] fileNameParts = csvFileName.split("\\.");
		if (fileNameParts[0].endsWith(EmployeeUtility.IN_PROCESS_SUFFIX)) {
			return csvFileName;
		}
		String inProcessFileName = fileNameParts[0] + "_" + EmployeeUtility.IN_PROCESS_SUFFIX + "." + fileNameParts[1];
		return inProcessFileName;
	}

	private String markFileAsProcessed(String actualFileName, String inProcessFileName) throws IOException {
		String[] fileNameParts = actualFileName.split("\\.");
		if (fileNameParts[0].endsWith(EmployeeUtility.IN_PROCESS_SUFFIX)) {
			fileNameParts[0] = fileNameParts[0].replaceAll(EmployeeUtility.IN_PROCESS_SUFFIX, "");
		}
		String processedFileName = fileNameParts[0] + "_" + EmployeeUtility.PROCESSED_SUFFIX + "." + fileNameParts[1];
		return renameFile(inProcessFileName, processedFileName);
	}

	private String renameFile(String fromFileName, String toFileName) throws IOException {
		String toCompleteFilePath = localSourceFolder + File.separator + toFileName;
		Path fromFilePath = Paths.get(localSourceFolder + File.separator + fromFileName);
		Path toFilePath = Paths.get(toCompleteFilePath);
		Files.move(fromFilePath, toFilePath, StandardCopyOption.ATOMIC_MOVE);
		return toCompleteFilePath;
	}
	
	private String moveFileToProcessed(String processedFileName) throws IOException {
		String sourceFile = processedFileName.substring(processedFileName.lastIndexOf("/"));
		String processedFile = localProcessedFolder + File.separator + sourceFile;
		Path sourceFilePath = Paths.get(processedFileName);
		Path processedFilePath = Paths.get(processedFile);
		Files.move(sourceFilePath, processedFilePath, StandardCopyOption.ATOMIC_MOVE);
		return processedFile;
	}

	public void setNotifier(ImportErrorNotifier notifier) {
		this.notifier = notifier;
	}

	public String getAdUserSuffix() {
		return adUserSuffix;
	}

	public void setAdUserSuffix(String adUserSuffix) {
		this.adUserSuffix = adUserSuffix;
	}

	public boolean isSkipSFTPFilesForTest() {
		return skipSFTPFilesForTest;
	}

	public void setSkipSFTPFilesForTest(boolean skipSFTPFilesForTest) {
		this.skipSFTPFilesForTest = skipSFTPFilesForTest;
	}

	public String getFscNewUserEmailID() {
		return fscNewUserEmailID;
	}

	public void setFscNewUserEmailID(String fscNewUserEmailID) {
		this.fscNewUserEmailID = fscNewUserEmailID;
	}

	public String getFieldNewUserEmailID() {
		return fieldNewUserEmailID;
	}

	public void setFieldNewUserEmailID(String fieldNewUserEmailID) {
		this.fieldNewUserEmailID = fieldNewUserEmailID;
	}

	public String getDefaultManagerCN() {
		return defaultManagerCN;
	}

	public void setDefaultManagerCN(String defaultManagerCN) {
		this.defaultManagerCN = defaultManagerCN;
	}

	public String getRacDomain() {
		return racDomain;
	}

	public void setRacDomain(String racDomain) {
		this.racDomain = racDomain;
	}

	public String getNpsDomain() {
		return npsDomain;
	}

	public void setNpsDomain(String npsDomain) {
		this.npsDomain = npsDomain;
	}

	public String getRacFranchisingDomain() {
		return racFranchisingDomain;
	}

	public void setRacFranchisingDomain(String racFranchisingDomain) {
		this.racFranchisingDomain = racFranchisingDomain;
	}

	public String getRacfiDomain() {
		return racfiDomain;
	}

	public void setRacfiDomain(String racfiDomain) {
		this.racfiDomain = racfiDomain;
	}

	public String getAnowDomain() {
		return anowDomain;
	}

	public void setAnowDomain(String anowDomain) {
		this.anowDomain = anowDomain;
	}

	public String getPlDomain() {
		return plDomain;
	}

	public void setPlDomain(String plDomain) {
		this.plDomain = plDomain;
	}

	public String getComputerName() {
		return computerName;
	}

	public void setComputerName(String computerName) {
		this.computerName = computerName;
	}

	public boolean isImporting() {
		return isImporting;
	}

	public void setImporting(boolean isImporting) {
		this.isImporting = isImporting;
	}

	public boolean isPauseImporting() {
		return pauseImporting;
	}

	public void setPauseImporting(boolean pauseImporting) {
		this.pauseImporting = pauseImporting;
	}

	public String getUseFirstDotLast() {
		return useFirstDotLast;
	}

	public void setUseFirstDotLast(String useFirstDotLast) {
		this.useFirstDotLast = useFirstDotLast;
	}

	public int getTreatAsFullFile() {
		return treatAsFullFile;
	}

	public Vector<String> getRacDomainExceptions() {
		Vector<String> exceptions = new Vector<String>();
		for (String dept : racDomainExceptions.split("\\|")) {
			exceptions.add(dept);
		}
		return exceptions;
	}

	public void setRacDomainExceptions(String racDomainExceptions) {
		this.racDomainExceptions = racDomainExceptions;
	}

	public Vector<String> getPlDomainExceptions() {
		Vector<String> exceptions = new Vector<String>();
		for (String dept : plDomainExceptions.split("\\|")) {
			exceptions.add(dept);
		}
		return exceptions;
	}

	public void setPlDomainExceptions(String plDomainExceptions) {
		this.plDomainExceptions = plDomainExceptions;
	}
}
