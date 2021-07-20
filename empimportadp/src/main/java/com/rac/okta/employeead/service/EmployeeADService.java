package com.rac.okta.employeead.service;

import java.util.List;

public interface EmployeeADService {
	
	public List<String> getDisabledAccounts(long olderThan);
	public List<String> listAccountsInOU(String ou);
	public void moveAllDisabledAcctsToDisabledOU(List<String> acctList, String targetOU);
	public void moveToDisabledOU(String employeeCN, String targetOU);
	public void moveToEnabledOU(String employeeCN, String targetOU);
	public void deleteFromDisabledOU(String employeeCN);
	public void deleteFromSpecifiedOU(String employeeCN, String ou);
	public String escapeCN(String cn);
	public void setActiveOUs(String[] activeOUs);
	public String[] getActiveOUs();
}
