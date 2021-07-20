package com.rac.hremp.objects;

import java.io.Serializable;
import java.util.Vector;

public class JobCodes implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public Vector<JobCode> jobCodes;

	public Vector<JobCode> getJobCodes() {
		return jobCodes;
	}

	public void setJobCodes(Vector<JobCode> jobCodes) {
		this.jobCodes = jobCodes;
	}
	
}
