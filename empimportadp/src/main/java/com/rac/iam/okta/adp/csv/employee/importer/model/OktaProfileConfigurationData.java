package com.rac.iam.okta.adp.csv.employee.importer.model;

import java.util.Vector;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OktaProfileConfigurationData {
	private static final Logger LOGGER = LoggerFactory.getLogger("import_logfile");
	@Value("${okta.elevated.jobcodes}")
	private String elevatedAccessJobCodes = null;
	
	@Value("${okta.dvp.jobfamilies}")
	private String dvpJobFamilies = null;
	
	@Value("${okta.dm.jobfamilies}")
	private String dmJobFamilies = null;
	
	@Value("${okta.rd.jobfamilies}")
	private String rdJobFamilies = null;
	
	@Value("${okta.sm.jobfamilies}")
	private String smJobFamilies = null;
	
	@Value("${okta.ho.jobfamilies}")
	private String hoJobFamilies = null;
	
	@Value("${okta.contractor.paygroups}")
	private String contractorPayGroups = null;
	
	@Value("${okta.franchise.backoffice.jobfamilies}")
	private String franchiseBackOfficeFamilies = null;
	
	private Vector<String> elevatedAccessVector = new Vector<String>();
	private Vector<String> dvpVector = new Vector<String>();
	private Vector<String> rdVector = new Vector<String>();
	private Vector<String> dmVector = new Vector<String>();
	private Vector<String> smVector = new Vector<String>();
	private Vector<String> hoVector = new Vector<String>();
	private Vector<String> boVector = new Vector<String>();
	
	@PostConstruct
	public void afterCreation() {
		try {
			if(elevatedAccessJobCodes != null && elevatedAccessJobCodes != "") {
				String codes[] = elevatedAccessJobCodes.split(",");
				for(String jobCode : codes) {
					elevatedAccessVector.addElement(jobCode);
				}
			}
			if(dvpJobFamilies != null && dvpJobFamilies != "") {
				String codes[] = dvpJobFamilies.split(",");
				for(String jobCode : codes) {
					dvpVector.addElement(jobCode);
				}
			}
			if(rdJobFamilies != null && rdJobFamilies != "") {
				String codes[] = rdJobFamilies.split(",");
				for(String jobCode : codes) {
					rdVector.addElement(jobCode);
				}
			}
			if(dmJobFamilies != null && dmJobFamilies != "") {
				String codes[] = dmJobFamilies.split(",");
				for(String jobCode : codes) {
					dmVector.addElement(jobCode);
				}
			}
			if(smJobFamilies != null && smJobFamilies != "") {
				String codes[] = smJobFamilies.split(",");
				for(String jobCode : codes) {
					smVector.addElement(jobCode);
				}
			}
			if(hoJobFamilies != null && hoJobFamilies != "") {
				String codes[] = hoJobFamilies.split(",");
				for(String jobCode : codes) {
					hoVector.addElement(jobCode);
				}
			}
			if (franchiseBackOfficeFamilies != null && franchiseBackOfficeFamilies != "") {
				String codes[] = franchiseBackOfficeFamilies.split(",");
				for (String code : codes) {
					boVector.addElement(code);
				}
			}
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	public String getElevatedAccessJobCodes() {
		return elevatedAccessJobCodes;
	}
	public void setElevatedAccessJobCodes(String elevatedAccessJobCodes) {
		this.elevatedAccessJobCodes = elevatedAccessJobCodes;
	}
	public String getDvpJobCodes() {
		return dvpJobFamilies;
	}
	public void setDvpJobCodes(String dvpJobCodes) {
		this.dvpJobFamilies = dvpJobCodes;
	}
	public String getDmJobCodes() {
		return dmJobFamilies;
	}
	public void setDmJobCodes(String dmJobCodes) {
		this.dmJobFamilies = dmJobCodes;
	}
	public String getRdJobCodes() {
		return rdJobFamilies;
	}
	public void setRdJobCodes(String rdJobCodes) {
		this.rdJobFamilies = rdJobCodes;
	}

	public String getSmJobCodes() {
		return smJobFamilies;
	}

	public void setSmJobCodes(String smJobCodes) {
		this.smJobFamilies = smJobCodes;
	}

	public Vector<String> getElevatedAccessVector() {
		return elevatedAccessVector;
	}

	public void setElevatedAccessVector(Vector<String> elevatedAccessVector) {
		this.elevatedAccessVector = elevatedAccessVector;
	}

	public Vector<String> getDvpVector() {
		return dvpVector;
	}

	public void setDvpVector(Vector<String> dvpVector) {
		this.dvpVector = dvpVector;
	}

	public Vector<String> getRdVector() {
		return rdVector;
	}

	public void setRdVector(Vector<String> rdVector) {
		this.rdVector = rdVector;
	}

	public Vector<String> getDmVector() {
		return dmVector;
	}

	public void setDmVector(Vector<String> dmVector) {
		this.dmVector = dmVector;
	}

	public Vector<String> getSmVector() {
		return smVector;
	}

	public void setSmVector(Vector<String> smVector) {
		this.smVector = smVector;
	}

	public String getHoJobFamilies() {
		return hoJobFamilies;
	}

	public void setHoJobFamilies(String hoJobFamilies) {
		this.hoJobFamilies = hoJobFamilies;
	}

	public Vector<String> getHoVector() {
		return hoVector;
	}

	public void setHoVector(Vector<String> hoVector) {
		this.hoVector = hoVector;
	}

	public String getContractorPayGroups() {
		return contractorPayGroups;
	}

	public void setContractorPayGroups(String contractorPayGroups) {
		this.contractorPayGroups = contractorPayGroups;
	}

	public String getFranchiseBackOfficeFamilies() {
		return franchiseBackOfficeFamilies;
	}

	public void setFranchiseBackOfficeFamilies(String franchiseBackOfficeFamilies) {
		this.franchiseBackOfficeFamilies = franchiseBackOfficeFamilies;
	}

	public Vector<String> getBoVector() {
		return boVector;
	}

	public void setBoVector(Vector<String> boVector) {
		this.boVector = boVector;
	}
}
