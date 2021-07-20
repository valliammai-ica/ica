package com.rac.iam.okta.adp.csv.employee.importer;

import com.rac.iam.okta.adp.csv.employee.importer.exception.EmployeeImportException;

public interface EmployeeImporter {
	
	public void importEmployeeRecords() throws EmployeeImportException;

}
