package com.rac.okta.service;

import java.util.List;
import com.okta.sdk.models.users.User;

public interface OktaDirectoryAccessService {
	public User findUserByEmployeeID(String employeeId);
	
	public User findUserByLogin(String login);
	
	public List<User> findUsersByFilter(String filter, String logical, String val);
	
	public void deleteOktaUser(User user);
}
