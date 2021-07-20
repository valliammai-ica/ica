package com.rac.okta.employeead.controller;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.PostConstruct;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.okta.sdk.framework.FilterBuilder;
import com.okta.sdk.models.users.User;
import com.rac.iam.okta.adp.csv.employee.importer.utilities.EmployeeUtility;
import com.rac.okta.employeead.service.EmployeeADService;
import com.rac.okta.service.OktaDirectoryAccessService;

@Configuration
@EnableScheduling
@Controller
@RequestMapping(value = "/empADController")
public class EmployeeADController {
	private static final Logger LOGGER = LoggerFactory.getLogger("ad_service_log");
	private static final Logger ERROR_LOGGER = LoggerFactory.getLogger("ad_service_errors_log");
	
	@Autowired
	EmployeeADService employeeADService;
	
	@Autowired
	OktaDirectoryAccessService oktaDirectoryAccessService;
	
	@Value("${ad.pause.trigger.on.startup}")
	public boolean pauseTriggerOnStartup;
	
	@Value("${ad.max.disabled.age}")
	public long maxDisabledAge;
	
	public boolean pauseTrigger = false;
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	private String serverName = null;
	
	@PostConstruct
	public void afterInit() {
		try {
    		setServerName(InetAddress.getLocalHost().getHostName());
		} catch (Exception e){}
		LOGGER.info("Employee AD Service started on "+OS+" server "+getServerName());
		System.out.println("Employee AD Service started on "+OS+" server "+getServerName());
		//LOGGER.info("testing the AD connection)");
		//testADConnection();
		if (!isPauseTriggerOnStartup()) {
			LOGGER.info("Triggering Sweep for Disabled AD accounts");
			triggerSweepForDisabled();
		} else {
			setPauseTrigger(true);
		}
	}
	
	/**
	 * Find all de-activated profiles over {ad.max.disabled.age} days old.
	 * Delete the AD account in the disabled OU
	 * Delete the Okta profile
	 */
	@Scheduled(cron="${ad.trigger.disabled.sweep}")
	public void triggerSweepForDisabled() {
		if (!isPauseTrigger()) {
    		Timer tymer = new Timer();
    		TimerTask tt = new TimerTask() {
    			public void run() {
    				try {
    					List<String> oldDisabled = employeeADService.getDisabledAccounts(getMaxDisabledAge());
    					LOGGER.info("processing "+oldDisabled.size()+" disabled accounts");
    					for (String acct : oldDisabled) {
    						if (acct != null) {//at this point there shouldn't be any null entries in the list...but it is prudent to check
        						LOGGER.info(acct);
        						String[] acctInfo = acct.split("[|]");
        						LOGGER.info("deleting AD account for "+acctInfo[0]);
        						employeeADService.deleteFromDisabledOU(acctInfo[0]);
        						User user = oktaDirectoryAccessService.findUserByEmployeeID(acctInfo[1]);
        						//ArrayList<User> users = (ArrayList<User>) oktaDirectoryAccessService.findUsersByFilter("profile.employeeID", "eq", acctInfo[1]);
        						if (user != null) {
        							if (user.getStatus().equals(EmployeeUtility.OKTA_STATUS_DEPROVISIONED)) {
        								LOGGER.info("Deleting profile for "+user.getProfile().getLogin());
            							oktaDirectoryAccessService.deleteOktaUser(user);
        							} else {
        								LOGGER.info("An active/provisioned profile for "+user.getProfile().getLogin()+" was found. No action was taken");
        							}
        						}
    						}
    					}
    				} catch (Exception e) {
    					ERROR_LOGGER.error("Fatal error in triggerSweepForDisabled.");
    					e.printStackTrace();
    				}
    			}
    		};
    		tymer.schedule(tt, 30000);
    		LOGGER.info("triggerSweepForDisabled() has been triggered........");
		}
	}
	
	@RequestMapping(value = "/testNewSweep", method = RequestMethod.GET)
	public @ResponseBody String testNewSweep() {
		Timer tymer = new Timer();
		TimerTask tt = new TimerTask() {
			public void run() {
				try {
					//long searchAge = (new Date().getTime()) - getMaxDisabledAge();
					//for (String activeOU : employeeADService.getActiveOUs()) {
					//	List<String> disabledAccts = employeeADService.getDisabledAccounts(activeOU);
					//	employeeADService.moveAllDisabledAcctsToDisabledOU(disabledAccts, activeOU);
						//for (String acct : disabledAccts) {
						//	LOGGER.info(acct);
						//}
					//}
				} catch (Exception e) {
					ERROR_LOGGER.error("Fatal error in triggerSweepForDisabled.");
					e.printStackTrace();
				}
			}
		};
		tymer.schedule(tt, 5000);
		LOGGER.info("testNewSweep has been triggered........");
		return "testNewSweep has been triggered........";
	}
	
	@RequestMapping(value = "/pauseSweep", method = RequestMethod.GET)
	public @ResponseBody String pauseSweep() {
		setPauseTrigger(true);
		LOGGER.info("Sweep of Disabled AD accounts has been paused.");
		return "Sweep of Disabled AD accounts has been paused.";
	}
	
	@RequestMapping(value = "/enableSweep", method = RequestMethod.GET)
	public @ResponseBody String enableSweep() {
		setPauseTrigger(false);
		LOGGER.info("Sweep of Disabled AD accounts has been enabled.");
		return "Sweep of Disabled AD accounts has been enabled.";
	}
	
	@RequestMapping(value = "/triggerSweep", method = RequestMethod.GET)
	public @ResponseBody String triggerSweep() {
		triggerSweepForDisabled();
		LOGGER.info("Sweep of Disabled AD accounts has been triggered.");
		return "Sweep of Disabled AD accounts has been triggered.";
	}
	
	@RequestMapping(value = "/triggerMoveToDisabledOU", method = RequestMethod.GET)
	public @ResponseBody String triggerMoveToDisabledOU(@RequestParam("employee") String employeeCN, @RequestParam("ou") String ou) {
		LOGGER.info("Setting timer task for one time move to disabled OU for "+employeeCN+" in ou="+ou);
		Timer tymer = new Timer();
		TimerTask tt = new TimerTask() {
			public void run() {
				try {
					ArrayList<String> aList = new ArrayList<String>();
					aList.add(employeeCN);
					employeeADService.moveAllDisabledAcctsToDisabledOU(aList,ou);
				} catch (Exception e) {
					ERROR_LOGGER.error("Failed to execute move to disabled OU for "+employeeCN+" in ou="+ou);
					e.printStackTrace();
				}
			}
		};
		tymer.schedule(tt, 10000);
		LOGGER.info("Timer task for one time move to disabled OU for "+employeeCN+" in ou="+ou+" has been set.");
		return "moveToDisabledOU for "+employeeCN+" in ou="+ou+" has been triggered........";
	}
	
	@RequestMapping(value = "/triggerMoveToEnabledOU", method = RequestMethod.GET)
	public @ResponseBody String triggerMoveToEnabledOU(@RequestParam("employee") String employeeCN, @RequestParam("targetOU")String targetOU) {
		LOGGER.info("Setting timer task for one time move to enabled OU for "+employeeCN);
		Timer tymer = new Timer();
		TimerTask tt = new TimerTask() {
			public void run() {
				try {
					employeeADService.moveToEnabledOU(employeeCN, targetOU);
				} catch (Exception e) {
					ERROR_LOGGER.error("Failed to execute move to enabled OU for "+employeeCN);
					e.printStackTrace();
				}
			}
		};
		tymer.schedule(tt, 10000);
		return "moveToEnabledOU for "+employeeCN+" has been triggered........";
	}
	
	
	private void testADConnection() {
		for (String ou : employeeADService.getActiveOUs()) {
			try {
				List<String> acctList = employeeADService.listAccountsInOU(ou);
				for (String acct : acctList) {
					LOGGER.info(acct);
				}
			} catch (Exception e) {
				ERROR_LOGGER.error("Failed to execute list accounts in OU for "+ou);
				e.printStackTrace();
			}
		}
	}
	@RequestMapping(value = "/triggerListAccountsInOU", method = RequestMethod.GET)
	public @ResponseBody String triggerListAccountsInOU(@RequestParam("ou") String ou) {
		LOGGER.info("Setting timer task to list accounts in the OU "+ou);
		Timer tymer = new Timer();
		TimerTask tt = new TimerTask() {
			public void run() {
				try {
					List<String> acctList = employeeADService.listAccountsInOU(ou);
					for (String acct : acctList) {
						LOGGER.info(acct);
					}
				} catch (Exception e) {
					ERROR_LOGGER.error("Failed to execute list accounts in OU for "+ou);
					e.printStackTrace();
				}
			}
		};
		tymer.schedule(tt, 10000);
		return "triggerListAccountsInOU for "+ou+" has been triggered........";
	}
	
	public boolean isPauseTriggerOnStartup() {
		return pauseTriggerOnStartup;
	}

	public void setPauseTriggerOnStartup(boolean pauseTriggerOnStartup) {
		this.pauseTriggerOnStartup = pauseTriggerOnStartup;
	}

	public boolean isPauseTrigger() {
		return pauseTrigger;
	}

	public void setPauseTrigger(boolean pauseTrigger) {
		this.pauseTrigger = pauseTrigger;
	}
	
	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public long getMaxDisabledAge() {
		return maxDisabledAge;
	}

	public void setMaxDisabledAge(long maxDisabledAge) {
		this.maxDisabledAge = maxDisabledAge;
	}
}
