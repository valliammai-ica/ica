package com.rac.iam.okta.adp.csv.employee.importer;

import com.rac.iam.okta.adp.csv.employee.importer.exception.NotificationException;
import com.rac.iam.okta.adp.csv.employee.importer.model.NotificationMessage;

public interface ImportErrorNotifier {
	
	public void sendNotification(NotificationMessage message) throws NotificationException;
	
	public void sendReactivationNotification(NotificationMessage message) throws NotificationException;

}
