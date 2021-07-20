package com.rac.okta.employeead.service;

import static org.springframework.ldap.query.LdapQueryBuilder.query;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableScheduling
@Transactional
@Service("employeeADService")
public class EmployeeADServiceImpl implements EmployeeADService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger("ad_service_log");
	private static final Logger ERROR_LOGGER = LoggerFactory.getLogger("ad_service_errors_log");
	
	private static final char COMMA_CHR = ',';
	private static final char DOUBLE_QUOTE_CHR = '"';
	private static final char ESCAPE_CHR = '\\';
	
	private static final long OFFSET_TO_ZULU = 21600000;
	
	@Autowired
	public LdapTemplate ldapTemplateAD;
	
	//private DirContext dirCtx;
	
	@Value("${ad.basedn}")
	public String baseDN;
	
	@Value("${ad.enabled.ou}")
	public String enabledOU;	// : delimited list of active OU's
	
	@Value("${ad.disabled.ou}")
	public String disabledOU;
	
	String[] activeOUs;
	
	private static final String AD_ACCOUNT_NORMAL = "512";
	private static final String AD_ACCOUNT_DISABLED = "514";
	
	@PostConstruct
	public void afterInit() {
		setActiveOUs(getEnabledOU().split(":"));
	}
	
	//retrieve accounts fro the specified ou older than whenChanged
	public List<String> getDisabledAccounts(long olderThan) {
		String adDateForSearch = getADDateForSearch(olderThan);
		LOGGER.info("Retrieving the list of disabled accounts in the OU "+disabledOU+" older than "+adDateForSearch);
		List<String> acctList = new ArrayList<String>();
		try {
			List<String> disabledList = ldapTemplateAD.search(query().base(disabledOU).searchScope(SearchScope.ONELEVEL)
													.where("objectclass").is("person")
													.and("userAccountControl").is(AD_ACCOUNT_DISABLED)
													.and("whenChanged").lte(adDateForSearch),
													new AttributesMapper<String>() {
                                        	            public String mapFromAttributes(Attributes attrs) throws NamingException {
                                        	            	int userAcctCtrl = Integer.parseInt(attrs.get("userAccountControl").get().toString(),10);
                                        	            	if (userAcctCtrl == 514) {//this is overkill, but it doesn't hurt to be sure
                                        	            		if (attrs.get("employeeID") != null) {
                                        	            			return attrs.get("cn").get().toString()+"|"+attrs.get("employeeID").get().toString();
                                        	            		}
                                        	            	}
                                        	            	return null;
                                        	            }
	         });
			for (String acct : disabledList) {
				if (acct != null) {
					acctList.add(acct);
				}
			}
		} catch (Exception e) {
			ERROR_LOGGER.error("Failed to retrieve list of disabled accounts from the OU "+disabledOU);
			e.printStackTrace();
		}
		return acctList;
	}
	
	private String getADDateForSearch(long olderThan) {
		DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String dateForSearch = null;
		Date now = new Date();
		Calendar disabledPriorToDate = new Calendar.Builder().setInstant((now.getTime()+OFFSET_TO_ZULU)-olderThan).build();
		dateForSearch = sdf.format(disabledPriorToDate.getTime()).toString()+".0Z";
		return dateForSearch;
	}
	
	public List<String> listAccountsInOU(String ou) {
		LOGGER.info("Retrieving the list of accounts in the OU "+ou);
		List<String> acctList = new ArrayList<String>();
		try {
			DirContext dirCtx = ldapTemplateAD.getContextSource().getReadWriteContext();
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration<SearchResult> results = dirCtx.search(ou, "objectClass=person", searchControls);
			while (results.hasMoreElements()) {
				SearchResult sr = results.next();
				acctList.add(sr.getName());
			}
			dirCtx.close();//always do this.....always
		} catch (Exception e) {
			ERROR_LOGGER.error("Failed to retrieve list of accounts from the OU "+ou);
			e.printStackTrace();
		}
		return acctList;
	}
	
	public void moveAllDisabledAcctsToDisabledOU(List<String> acctList, String targetOU) {
		for (String acct : acctList) {
			try {
				DirContext dirCtx = ldapTemplateAD.getContextSource().getReadWriteContext();
    			moveToDisabledOU(acct,targetOU);
    			LOGGER.info("Moved "+acct+" in OU="+targetOU+" to the disabled OU ");
    			dirCtx.close();
			} catch (Exception e) {
				ERROR_LOGGER.error("Error moving list of accounts to disabled OU");
			}
		}
	}
	
	public void moveToDisabledOU(String employeeCN, String targetOU) {
		employeeCN = escapeCN(employeeCN);
		try {
			DirContext dirCtx = ldapTemplateAD.getContextSource().getReadWriteContext();
			LOGGER.info("Moving "+employeeCN+" in OU="+targetOU+" to disabled OU.");
			final Name oldDn = LdapNameBuilder.newInstance("CN="+employeeCN+","+targetOU).build();
			final Name newDn = LdapNameBuilder.newInstance("CN="+employeeCN+","+getDisabledOU()).build();
			dirCtx.rename(oldDn, newDn);
			LOGGER.info("Moved "+employeeCN+" to disabled OU.");
			try {
				dirCtx.modifyAttributes(newDn, DirContext.ADD_ATTRIBUTE, new BasicAttributes("physicalDeliveryOfficeName",new Date().toString()));
			} catch (AttributeInUseException ex){
				dirCtx.modifyAttributes(newDn, DirContext.REPLACE_ATTRIBUTE, new BasicAttributes("physicalDeliveryOfficeName",new Date().toString()));
			}
			try {
				dirCtx.modifyAttributes(newDn, DirContext.ADD_ATTRIBUTE, new BasicAttributes("extensionAttribute11",targetOU));
			} catch (AttributeInUseException ex) {
				dirCtx.modifyAttributes(newDn, DirContext.REPLACE_ATTRIBUTE, new BasicAttributes("extensionAttribute11",targetOU));
			}
			LOGGER.info("Set physicalDeliveryOfficeName to "+(new Date().toString()));
			dirCtx.close();
		} catch (NameAlreadyBoundException nabe) {
			LOGGER.error("AD Account for "+employeeCN+" already exists in the disabled OU. Manual intervention is required.");
		} catch (Exception e) {
			ERROR_LOGGER.error("Error moving to disabled OU for "+employeeCN+" in OU="+targetOU);
			e.printStackTrace();
		}
	}
	
	public void moveToEnabledOU(String employeeCN, String targetOU) {
		try {
			employeeCN = escapeCN(employeeCN);
			DirContext dirCtx = ldapTemplateAD.getContextSource().getReadWriteContext();
			LOGGER.info("Moving "+employeeCN+" to enabled OU.");
			final Name oldDn = LdapNameBuilder.newInstance("CN="+employeeCN+","+getDisabledOU()).build();
			Attributes attrs = dirCtx.getAttributes(oldDn);
			final Name newDn = LdapNameBuilder.newInstance("CN="+employeeCN+","+targetOU).build();
			dirCtx.rename(oldDn, newDn);
			LOGGER.info("Moved "+employeeCN+" to enabled OU "+targetOU+".");
			try {
				dirCtx.modifyAttributes(newDn, DirContext.REPLACE_ATTRIBUTE, new BasicAttributes("physicalDeliveryOfficeName"," "));
			} catch (AttributeInUseException ex){
				dirCtx.modifyAttributes(newDn, DirContext.ADD_ATTRIBUTE, new BasicAttributes("physicalDeliveryOfficeName"," "));
			}
			try {
				dirCtx.modifyAttributes(newDn, DirContext.REPLACE_ATTRIBUTE, new BasicAttributes("extensionAttribute11"," "));
			} catch (AttributeInUseException ex){
				dirCtx.modifyAttributes(newDn, DirContext.ADD_ATTRIBUTE, new BasicAttributes("extensionAttribute11"," "));
			}
			dirCtx.modifyAttributes(newDn, DirContext.REPLACE_ATTRIBUTE, new BasicAttributes("userAccountControl",AD_ACCOUNT_NORMAL));
			dirCtx.close();
		} catch (Exception e) {
			ERROR_LOGGER.error("Error moving to enabled OU for "+employeeCN);
			e.printStackTrace();
		}
	}
	
	public void deleteFromDisabledOU(String employeeCN) {
		try {
			LOGGER.info("Deleting "+employeeCN+" from disabled OU "+getDisabledOU()+".");
			final Name targetDn = LdapNameBuilder.newInstance("CN="+escapeCN(employeeCN)+","+getDisabledOU()).build();
			ldapTemplateAD.unbind(targetDn, true);
			LOGGER.info("Deleted "+employeeCN+" from disabled OU "+getDisabledOU()+".");
		} catch (Exception e) {
			ERROR_LOGGER.error("Error deleting "+employeeCN+" from disabled OU "+getDisabledOU()+".");
			e.printStackTrace();
		}
	}
	
	public void deleteFromSpecifiedOU(String employeeCN, String ou) {
		try {
			LOGGER.info("Deleting "+employeeCN+" from OU "+ou);
			final Name targetDn = LdapNameBuilder.newInstance("CN="+escapeCN(employeeCN)+","+ou).build();
			ldapTemplateAD.unbind(targetDn, true);
			LOGGER.info("Deleted "+employeeCN+" from OU "+ou);
		} catch (Exception e) {
			ERROR_LOGGER.error("Error deleting "+employeeCN+" from OU "+ou);
			e.printStackTrace();
		}
	}
	
	public String escapeCN(String cn) {
		StringBuilder escapedCN = new StringBuilder();
		for (int i=0;i<cn.length();i++) {
			char currentChar = cn.charAt(i);
			if (currentChar == COMMA_CHR) {
				escapedCN.append(ESCAPE_CHR);
			}
			if (currentChar == DOUBLE_QUOTE_CHR) {
				escapedCN.append(ESCAPE_CHR);
			}
			escapedCN.append(currentChar);
		}
		return escapedCN.toString();
	}

	public LdapTemplate getLdapTemplateAD() {
		return ldapTemplateAD;
	}

	public void setLdapTemplateAD(LdapTemplate ldapTemplateAD) {
		this.ldapTemplateAD = ldapTemplateAD;
	}

	public String getEnabledOU() {
		return enabledOU;
	}

	public void setEnabledOU(String enabledOU) {
		this.enabledOU = enabledOU;
	}

	public String getDisabledOU() {
		return disabledOU;
	}

	public void setDisabledOU(String disabledOU) {
		this.disabledOU = disabledOU;
	}

	public String getBaseDN() {
		return baseDN;
	}

	public void setBaseDN(String baseDN) {
		this.baseDN = baseDN;
	}

	public String[] getActiveOUs() {
		return activeOUs;
	}

	public void setActiveOUs(String[] activeOUs) {
		this.activeOUs = activeOUs;
	}
}
