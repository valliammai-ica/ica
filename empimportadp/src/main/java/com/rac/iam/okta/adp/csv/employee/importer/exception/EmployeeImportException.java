package com.rac.iam.okta.adp.csv.employee.importer.exception;

public class EmployeeImportException extends Exception {

	private static final long serialVersionUID = 1L;

	public EmployeeImportException(Throwable cause) {
		super(cause);
	}

	public EmployeeImportException(String message, Throwable cause) {
		super(message, cause);
	}

	public EmployeeImportException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
