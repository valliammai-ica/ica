package com.rac.okta.service;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.okta.sdk.clients.UserApiClient;
import com.okta.sdk.framework.ApiClientConfiguration;
import com.okta.sdk.framework.FilterBuilder;
import com.okta.sdk.framework.PagedResults;
import com.okta.sdk.models.users.User;

@Configuration
@Transactional
@Service("oktaAccessService")
public class OktaDirectoryAccessServiceImpl implements OktaDirectoryAccessService {
	private static final Logger LOGGER = LoggerFactory.getLogger("okta_service_logfile");
	private static final Logger ERROR_LOGGER = LoggerFactory.getLogger("okta_service_errors");
	
	@Value("${okta.tenant.url}")
	private String oktaTenantUrl = null;

	@Value("${okta.api.key}")
	private String oktaAPIKey = null;
	
	private ApiClientConfiguration apiClientConfiguration = null;
	private UserApiClient userAPIClient = null;
	
	@PostConstruct
	public void afterCreation() {
		this.apiClientConfiguration = new ApiClientConfiguration(oktaTenantUrl, oktaAPIKey);
		this.userAPIClient = new UserApiClient(apiClientConfiguration);
		LOGGER.info("Startup configuration complete");
	}
	
	//find okta profile by employee id
	public User findUserByEmployeeID(String employeeId) {
		User user = null;
		try {
    		FilterBuilder filterBuilder = new FilterBuilder("profile.employeeID eq \"" + employeeId + "\"");
    		List<User> users = userAPIClient.getUsersWithAdvancedSearch(filterBuilder);
    		if (users.size() > 1) {
    			ERROR_LOGGER.error("Error retrieving User for employeeId="+employeeId+". Multiple matches found, returning null.");
    		} else if (users.size() == 0) {
    			ERROR_LOGGER.error("Error retrieving User for employeeId="+employeeId+". No match found, returning null.");
    		} else {
    			user = users.get(0);
    		}
		} catch (Exception e) {
			ERROR_LOGGER.error("Error retrieving User for employeeId="+employeeId);
			e.printStackTrace();
		}
		return user;
	}
	
	//find okta profile by okta login
	public User findUserByLogin(String login) {
		User user = null;
		try {
    		FilterBuilder filterBuilder = new FilterBuilder("profile.login eq \"" + login + "\"");
    		List<User> users = userAPIClient.getUsersWithAdvancedSearch(filterBuilder);
    		if (users.size() > 1) {
    			ERROR_LOGGER.error("Error retrieving User for login="+login+". Multiple matches found, returning null.");
    		} else if (users.size() == 0) {
    			ERROR_LOGGER.error("Error retrieving User for login="+login+". No match found, returning null.");
    		} else {
    			user = users.get(0);
    		}
		} catch (Exception e) {
			ERROR_LOGGER.error("Error retrieving User for login="+login);
			e.printStackTrace();
		}
		return user;
	}
	
	/**
	 * logical possible values are eq/lt/gt
	 */
	public List<User> findUsersByFilter(String filter, String logical, String val) {
		ArrayList<User> users = new ArrayList<User>();
		try {
    		FilterBuilder filterBuilder = new FilterBuilder(filter+" "+logical+" \"" + val + "\"");
    		//users = (ArrayList<User>) userAPIClient.getUsersWithAdvancedSearch(filterBuilder);
    		PagedResults<User> pr = userAPIClient.getUsersPagedResultsWithAdvancedSearch(filterBuilder);
    		while (true) {
    			users.addAll(pr.getResult());
    			if (!pr.isLastPage()) {
        			String nextEarl = pr.getNextUrl();
        			pr = userAPIClient.getUsersPagedResultsByUrl(nextEarl);
    			} else {
    				break;
    			}
    		}
		} catch (Exception e) {
			ERROR_LOGGER.error("Error finding users for query = "+filter+" "+logical+" \"" + val + "\"");
		}
		return users;
	}
	
	public void deleteOktaUser(User user) {
		try {
			LOGGER.info("Deleting profile for "+user.getProfile().getLogin());
    		userAPIClient.deleteUser(user.getId());
    		LOGGER.info("Profile deleted for "+user.getProfile().getLogin());
		} catch (Exception e) {
			ERROR_LOGGER.error("Error deleting profile for "+user.getProfile().getLogin());
		}
	}
}
