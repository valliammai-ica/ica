package com.rac.hremp.objects;

import java.io.Serializable;
import java.util.Vector;

public class JobFamilies implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public Vector<JobFamily> jobFamilies;

	public Vector<JobFamily> getJobFamilies() {
		return jobFamilies;
	}

	public void setJobFamilies(Vector<JobFamily> jobFamilies) {
		this.jobFamilies = jobFamilies;
	}
	
}
