package com.rac.hremp.objects;

import java.io.Serializable;
import java.util.ArrayList;

public class JobFamily implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String code;
	public String description;
	public int approvalLimitMX;
	public int approvalLimitUS;
	public ArrayList<JobCode> jobCodes;
	
	public int getApprovalLimitMX() {
		return approvalLimitMX;
	}
	public void setApprovalLimitMX(int approvalLimitMX) {
		this.approvalLimitMX = approvalLimitMX;
	}
	public int getApprovalLimitUS() {
		return approvalLimitUS;
	}
	public void setApprovalLimitUS(int approvalLimitUS) {
		this.approvalLimitUS = approvalLimitUS;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public ArrayList<JobCode> getJobCodes() {
		return jobCodes;
	}
	public void setJobCodes(ArrayList<JobCode> jobCodes) {
		this.jobCodes = jobCodes;
	}
}
