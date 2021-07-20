package com.rac.iam.okta.adp.csv.employee.importer.exception;

public class FileDownloadException extends Exception {

	private static final long serialVersionUID = 1L;

	public FileDownloadException(String message) {
		super(message);
	}

	public FileDownloadException(Throwable cause) {
		super(cause);
	}

	public FileDownloadException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileDownloadException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
