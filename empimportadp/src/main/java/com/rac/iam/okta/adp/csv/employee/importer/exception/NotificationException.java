package com.rac.iam.okta.adp.csv.employee.importer.exception;

public class NotificationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NotificationException(Throwable cause) {
		super(cause);
	}

	public NotificationException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotificationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
