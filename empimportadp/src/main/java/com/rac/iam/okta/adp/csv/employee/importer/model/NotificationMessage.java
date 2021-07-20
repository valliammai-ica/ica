package com.rac.iam.okta.adp.csv.employee.importer.model;

public class NotificationMessage {
	
	private String subjectLine = null;
	
	private String messageBody = null;

	public String getSubjectLine() {
		return subjectLine;
	}

	public void setSubjectLine(String subjectLine) {
		this.subjectLine = subjectLine;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

}
