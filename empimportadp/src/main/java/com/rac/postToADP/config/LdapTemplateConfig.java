package com.rac.postToADP.config;

import org.springframework.ldap.core.LdapTemplate;

public class LdapTemplateConfig {
	private LdapTemplate ldapTemplateLDAP;
	private String usersOU;
	private String storesOU;
	private String storeGroupsOU;
	private String employeeGroupStoreManagerOU;
	private String employeeGroupHomeOfficeViewerOU;
	private String employeeGroupFieldManagerOU;
	private String employeeGroupStoreEmployeeOU;
	private String employeeGroupHomeOfficeEmployeeOU;
	private String employeeGroupNet_Str_DmOU;
	private String employeeGroupNet_Str_RdOU;
	private String storeGroupNet_Get_It_NowOU;
	private String storeGroupNet_Home_ChoiceOU;
	private String storeGroupNet_Home_Choice_IlOU;
	private String storeGroupNet_Rac_AcceptanceOU;
	private String storeGroupNet_Rac_CanadaOU;
	private String storeGroupNet_Rac_RtoOU;
	private String storeGroupNet_Rac_MexicoOU;
	private String storeGroupNet_Rto_PrOU;
	private String storeGroupNet_Str_NpsOU;
	private String storeGroupNet_Str_PrOU;
	private String baseDN;
	
	public LdapTemplate getLdapTemplateLDAP() {
		return ldapTemplateLDAP;
	}

	public void setLdapTemplateLDAP(LdapTemplate ldapTemplateLDAP) {
		this.ldapTemplateLDAP = ldapTemplateLDAP;
	}
	
	public String getUsersOU() {
		return usersOU;
	}

	public void setUsersOU(String usersOU) {
		this.usersOU = usersOU;
	}

	public String getStoresOU() {
		return storesOU;
	}

	public void setStoresOU(String storesOU) {
		this.storesOU = storesOU;
	}
	
	public String getStoreGroupsOU() {
		return storeGroupsOU;
	}

	public void setStoreGroupsOU(String storeGroupsOU) {
		this.storeGroupsOU = storeGroupsOU;
	}

	public String getEmployeeGroupStoreManagerOU() {
		return employeeGroupStoreManagerOU;
	}

	public void setEmployeeGroupStoreManagerOU(String employeeGroupStoreManagerOU) {
		this.employeeGroupStoreManagerOU = employeeGroupStoreManagerOU;
	}

	public String getEmployeeGroupHomeOfficeViewerOU() {
		return employeeGroupHomeOfficeViewerOU;
	}

	public void setEmployeeGroupHomeOfficeViewerOU(String employeeGroupHomeOfficeViewerOU) {
		this.employeeGroupHomeOfficeViewerOU = employeeGroupHomeOfficeViewerOU;
	}

	public String getEmployeeGroupFieldManagerOU() {
		return employeeGroupFieldManagerOU;
	}

	public void setEmployeeGroupFieldManagerOU(String employeeGroupFieldManagerOU) {
		this.employeeGroupFieldManagerOU = employeeGroupFieldManagerOU;
	}

	public String getEmployeeGroupStoreEmployeeOU() {
		return employeeGroupStoreEmployeeOU;
	}

	public void setEmployeeGroupStoreEmployeeOU(String employeeGroupStoreEmployeeOU) {
		this.employeeGroupStoreEmployeeOU = employeeGroupStoreEmployeeOU;
	}

	public String getEmployeeGroupHomeOfficeEmployeeOU() {
		return employeeGroupHomeOfficeEmployeeOU;
	}

	public void setEmployeeGroupHomeOfficeEmployeeOU(String employeeGroupHomeOfficeEmployeeOU) {
		this.employeeGroupHomeOfficeEmployeeOU = employeeGroupHomeOfficeEmployeeOU;
	}

	public String getEmployeeGroupNet_Str_DmOU() {
		return employeeGroupNet_Str_DmOU;
	}

	public void setEmployeeGroupNet_Str_DmOU(String employeeGroupNet_Str_DmOU) {
		this.employeeGroupNet_Str_DmOU = employeeGroupNet_Str_DmOU;
	}

	public String getEmployeeGroupNet_Str_RdOU() {
		return employeeGroupNet_Str_RdOU;
	}

	public void setEmployeeGroupNet_Str_RdOU(String employeeGroupNet_Str_RdOU) {
		this.employeeGroupNet_Str_RdOU = employeeGroupNet_Str_RdOU;
	}

	public String getStoreGroupNet_Get_It_NowOU() {
		return storeGroupNet_Get_It_NowOU;
	}

	public void setStoreGroupNet_Get_It_NowOU(String storeGroupNet_Get_It_NowOU) {
		this.storeGroupNet_Get_It_NowOU = storeGroupNet_Get_It_NowOU;
	}

	public String getStoreGroupNet_Home_ChoiceOU() {
		return storeGroupNet_Home_ChoiceOU;
	}

	public void setStoreGroupNet_Home_ChoiceOU(String storeGroupNet_Home_ChoiceOU) {
		this.storeGroupNet_Home_ChoiceOU = storeGroupNet_Home_ChoiceOU;
	}

	public String getStoreGroupNet_Home_Choice_IlOU() {
		return storeGroupNet_Home_Choice_IlOU;
	}

	public void setStoreGroupNet_Home_Choice_IlOU(String storeGroupNet_Home_Choice_IlOU) {
		this.storeGroupNet_Home_Choice_IlOU = storeGroupNet_Home_Choice_IlOU;
	}

	public String getStoreGroupNet_Rac_AcceptanceOU() {
		return storeGroupNet_Rac_AcceptanceOU;
	}

	public void setStoreGroupNet_Rac_AcceptanceOU(String storeGroupNet_Rac_AcceptanceOU) {
		this.storeGroupNet_Rac_AcceptanceOU = storeGroupNet_Rac_AcceptanceOU;
	}

	public String getStoreGroupNet_Rac_CanadaOU() {
		return storeGroupNet_Rac_CanadaOU;
	}

	public void setStoreGroupNet_Rac_CanadaOU(String storeGroupNet_Rac_CanadaOU) {
		this.storeGroupNet_Rac_CanadaOU = storeGroupNet_Rac_CanadaOU;
	}

	public String getStoreGroupNet_Rac_RtoOU() {
		return storeGroupNet_Rac_RtoOU;
	}

	public void setStoreGroupNet_Rac_RtoOU(String storeGroupNet_Rac_RtoOU) {
		this.storeGroupNet_Rac_RtoOU = storeGroupNet_Rac_RtoOU;
	}

	public String getStoreGroupNet_Rac_MexicoOU() {
		return storeGroupNet_Rac_MexicoOU;
	}

	public void setStoreGroupNet_Rac_MexicoOU(String storeGroupNet_Rac_MexicoOU) {
		this.storeGroupNet_Rac_MexicoOU = storeGroupNet_Rac_MexicoOU;
	}

	public String getStoreGroupNet_Rto_PrOU() {
		return storeGroupNet_Rto_PrOU;
	}

	public void setStoreGroupNet_Rto_PrOU(String storeGroupNet_Rto_PrOU) {
		this.storeGroupNet_Rto_PrOU = storeGroupNet_Rto_PrOU;
	}

	public String getStoreGroupNet_Str_NpsOU() {
		return storeGroupNet_Str_NpsOU;
	}

	public void setStoreGroupNet_Str_NpsOU(String storeGroupNet_Str_NpsOU) {
		this.storeGroupNet_Str_NpsOU = storeGroupNet_Str_NpsOU;
	}

	public String getStoreGroupNet_Str_PrOU() {
		return storeGroupNet_Str_PrOU;
	}

	public void setStoreGroupNet_Str_PrOU(String storeGroupNet_Str_PrOU) {
		this.storeGroupNet_Str_PrOU = storeGroupNet_Str_PrOU;
	}

	public String getBaseDN() {
		return baseDN;
	}

	public void setBaseDN(String baseDN) {
		this.baseDN = baseDN;
	}
}
