package com.rac.postToADP.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.naming.directory.Attributes;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.okta.sdk.clients.UserApiClient;
import com.okta.sdk.framework.ApiClientConfiguration;
import com.okta.sdk.models.users.User;
import com.okta.sdk.models.users.UserProfile;
import com.rac.hremp.objects.Employee;
import com.rac.hremp.objects.EmployeeFullData;
import com.rac.hremp.objects.Employees;
import com.rac.hremp.objects.EmployeesFullData;
import com.rac.hremp.service.HRDataService;
import com.rac.iam.okta.adp.csv.employee.importer.ImportErrorNotifier;
import com.rac.iam.okta.adp.csv.employee.importer.model.NotificationMessage;
import com.rac.iam.okta.adp.csv.employee.importer.utilities.EmployeeUtility;
import com.rac.postToADP.config.LdapTemplateConfig;

@Configuration
@EnableScheduling
@Controller
@RequestMapping(value = "/postToADP")
public class postToADP {
	private static final Logger LOGGER = LoggerFactory.getLogger("ldap_sync_log");
	private static final Logger ERROR_LOGGER = LoggerFactory.getLogger("ldap_sync_errors");
	
	@Autowired
	private HRDataService hrDataService;
	
	@Autowired
	private LdapTemplateConfig ldapTemplateConfig;
	
	@Autowired
	private ImportErrorNotifier notifier = null;
	
	@Value("${okta.tenant.url}")
	private String oktaTenantUrl = null;

	@Value("${okta.api.key}")
	private String oktaAPIKey = null;
	private ApiClientConfiguration apiClientConfiguration = null;
	private UserApiClient userAPIClient = null;
	public long oktaRequestLimit = 300;
	
	private LdapTemplate ldapTemplateLDAP;
	
	private HashMap<String,String> employeeEmailMap = new HashMap<String,String>();
	
	private boolean pauseProcessing = true;			//email sync from ldap to Okta/ADP is paused by default
	private boolean inProcess = false;
	
	private Vector<String> ignoreList = new Vector<String>();

	@PostConstruct
	public void afterCreation() {
		setLdapTemplateLDAP(ldapTemplateConfig.getLdapTemplateLDAP());
		apiClientConfiguration = new ApiClientConfiguration(oktaTenantUrl, oktaAPIKey);
		userAPIClient = new UserApiClient(apiClientConfiguration);
		ignoreList.add("2849");
	}
	
	@RequestMapping(value = "/triggerEmailProcessing", method = RequestMethod.GET)
	public @ResponseBody String triggerEmailProcess() {
		Timer tymer = new Timer();
		TimerTask tt = new TimerTask() {
			public void run() {
				try {
					populateEmployeeEmailMap();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		tymer.schedule(tt, 30000);
		return "populateEmployeeEmailMap has been triggered........";
	}
	
	@RequestMapping(value = "/pauseProcessing", method = RequestMethod.GET)
	public @ResponseBody String pauseProcessing() {
		LOGGER.info("Received request to pause processing.");
		setPauseProcessing(true);
		return "Processing has been paused.";
	}
	
	@RequestMapping(value = "/resumeProcessing", method = RequestMethod.GET)
	public @ResponseBody String resumeProcessing() {
		LOGGER.info("Received request to resume processing.");
		setPauseProcessing(false);
		return "Processing has been resumed.";
	}

	//@Scheduled(initialDelayString="${importer.scheduled.initialDelay}",fixedDelayString="${importer.scheduled.fixedDelay}")
	//@Scheduled(initialDelay=15000,fixedDelay=3600000)
	@Scheduled(cron="0 0 11 ? * MON-FRI")
	public void populateEmployeeEmailMap() {
		this.employeeEmailMap = new HashMap<String,String>();
		if (isPauseProcessing()) {
			LOGGER.info("Email Processing paused.....skipping run............");
		} else if (isInProcess()) {
			LOGGER.info("Email Processing is currently underway.....skipping run.................");
		} else {
			setInProcess(true);
    		LOGGER.info("Beginning populateEmployeeEmailMap");
    		int kounter = 0;
    		try {
    			EmployeesFullData employees = hrDataService.getAllEmployees(false);
    			for (EmployeeFullData employee : employees.getEmployees()) {
    				if (ignoreList.contains(employee.getEmployeeNumber())) {
    					LOGGER.info("Employee "+employee.getEmployeeNumber()+" is on the ignore list, skipping.");
    				} else {
        				boolean addToMap = false;
        				if (employee.getDepartment().getLineOfBusinessInfo().getCode().equals("10")
        						|| (employee.getManagerRole() != null && employee.getManagerRole().equals("Y"))) {
        					addToMap = true;
        				}
        				if (addToMap) {
            				String workerDbEmail = employee.getWorkEmailAddress();
            				String ldapEmail = getEmailFromLDAP(employee.getEmployeeNumber());
            				if (ldapEmail != null) {
            					if (!ldapEmail.startsWith(employee.getEmployeeNumber())) {
                    				if (workerDbEmail == null && ldapEmail != null) {
                    					getEmployeeEmailMap().put(employee.getEmployeeNumber(), ldapEmail);
                						LOGGER.info("added "+employee.getEmployeeNumber()+" "+ldapEmail+" to the map");
                    				} else if (ldapEmail != null && workerDbEmail != null) {
                    					if (workerDbEmail.trim().toUpperCase().equals(ldapEmail.trim().toUpperCase())) {
                    						LOGGER.info(" email addresses match "+workerDbEmail+" = "+ldapEmail);
                    					} else {
                    						getEmployeeEmailMap().put(employee.getEmployeeNumber(), ldapEmail);
                    						LOGGER.info("added "+employee.getEmployeeNumber()+" "+ldapEmail+" to the map");
                    					}
                    				}
            					}
            				}
            				LOGGER.info(employee.getEmployeeNumber()+" workerdb email="+employee.getWorkEmailAddress()+"  LDAP email="+ldapEmail);
            				kounter++;
        				}
    				}
    			}
    		} catch (Exception e) {
    			ERROR_LOGGER.error(e.getLocalizedMessage());
    			e.printStackTrace();
    		}
    		LOGGER.info("Processed "+kounter+" records");
    		LOGGER.info("Setting timer to process map");
    		Timer tymer = new Timer();
    		TimerTask tt = new TimerTask() {
    			public void run() {
    				try {
    					processMap();
    					setInProcess(false);
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    			}
    		};
    		tymer.schedule(tt, 30000);
    		LOGGER.info("Exiting populateEmployeeEmailMap");
		}
	}
	
	public String getEmailFromLDAP(String empid) {
		try {
			String lookUpCn = buildDN(empid);
    		DirContextOperations ctx = ldapTemplateLDAP.lookupContext(lookUpCn);
    		Attributes attrs = ctx.getAttributes();
    		if (attrs.get("mail") != null) {
        		String email = ctx.getStringAttribute("mail");
        		return email;
    		}
		} catch (NameNotFoundException nnfe) {
			ERROR_LOGGER.error("LDAP object for "+empid+" is not found.");
			ERROR_LOGGER.error(nnfe.getLocalizedMessage());
		} catch (Exception e) {
			ERROR_LOGGER.error("ERROR getting email address from LDAP for "+empid);
			ERROR_LOGGER.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	public void processMap() {
		LOGGER.info("Beginning processMap");
		LOGGER.info("Map contains "+getEmployeeEmailMap().size()+" entries.");
		try {
			int requestKounter = 0;
			long now  = new Date().getTime();
			long futureNow = now + 60000;
			HashMap<String, String> emailMap = getEmployeeEmailMap();
			Set<String> keys = emailMap.keySet();
			for (String empid : keys) {
				String ldapEmail = emailMap.get(empid);
				LOGGER.info("retrieved "+ldapEmail+ "from email map");
				if (postUpdateToADP(empid,ldapEmail)) {
					LOGGER.info("email address "+ldapEmail+ " updated for empid="+empid);
					LOGGER.info("updating email address in Okta");
					List<User> users = userAPIClient.getUsersByUrl(oktaTenantUrl+"/api/v1/users?search=profile.employeeID+eq+%22"+empid+"%22");
					requestKounter++;
        			if (users.size() == 1) {
        				User user = users.get(0);
            			UserProfile userProfile = user.getProfile();
            			if (user.getStatus().equals(EmployeeUtility.OKTA_STATUS_ACTIVE)) {
                			try {
                				userProfile.setEmail(ldapEmail);
                				user.setProfile(userProfile);
                				userAPIClient.updateUser(user);
        						requestKounter++;
            				} catch (Exception e) {
            					LOGGER.error("Failed to update Okta profile for "+empid);
            					LOGGER.error(e.getLocalizedMessage());
                			}
            			}
    				} else if (users.size() > 1){
    					LOGGER.error("Multiple profiles found for employee id= "+empid);
    				} else if (users.size() == 0){
    					LOGGER.info("No profiles found for employee id= "+empid);
    				}
        			if (requestKounter >= this.oktaRequestLimit) {
        				now  = new Date().getTime();
            			if (now < futureNow) {
            				Thread.sleep((futureNow-now));
            				requestKounter = 0;
            				futureNow = (new Date().getTime()) + 60000;
            			}
        			}
				} else {
					LOGGER.info("failed to update email address for empid="+empid);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("Exiting processMap");
	}
	
	public boolean postUpdateToADP(String empid, String emailAddress) {
		try {
    		LOGGER.info("Beginning postUpdateToADP with empid="+empid+" emailAddress="+emailAddress);
    		String payload = "{\"employeeId\": "+empid+",\"emailAddress\": \""+emailAddress+"\"}";
    		LOGGER.info("calling PostEmailAddressToADP service with payload="+payload);;
    		int responseCode = hrDataService.postEmailAddressToADP(payload);
    		if (responseCode == 200 || responseCode == 201) {
    			return true;
    		}
		} catch (Exception e) {
			ERROR_LOGGER.error("error posting to ADP for empid="+empid);
			ERROR_LOGGER.error(e.getLocalizedMessage());
			NotificationMessage message = new NotificationMessage();
			StringBuffer buffy = new StringBuffer();
			buffy.append("Error while posting primary email address to ADP\n");
			buffy.append("for "+empid+" - "+emailAddress+"\n\n");
			buffy.append("The primary email address will have to be manually updated in ADP.\n\n");
			message.setMessageBody(buffy.toString());
			message.setSubjectLine("Error posting update to ADP for "+empid);
			notifier.sendNotification(message);
		}
		return false;
	}
	
	public String buildDN(String empid) {
		return "CN="+empid+","+ldapTemplateConfig.getBaseDN();
	}

	public LdapTemplate getLdapTemplateLDAP() {
		return ldapTemplateLDAP;
	}

	public void setLdapTemplateLDAP(LdapTemplate ldapTemplateLDAP) {
		this.ldapTemplateLDAP = ldapTemplateLDAP;
	}

	public HashMap<String, String> getEmployeeEmailMap() {
		return this.employeeEmailMap;
	}

	public void setEmployeeEmailMap(HashMap<String, String> employeeEmailMap) {
		this.employeeEmailMap = employeeEmailMap;
	}

	public boolean isPauseProcessing() {
		return pauseProcessing;
	}

	public void setPauseProcessing(boolean pauseProcessing) {
		this.pauseProcessing = pauseProcessing;
	}
	
	public boolean isInProcess() {
		return inProcess;
	}

	public void setInProcess(boolean inProcess) {
		this.inProcess = inProcess;
	}
}
