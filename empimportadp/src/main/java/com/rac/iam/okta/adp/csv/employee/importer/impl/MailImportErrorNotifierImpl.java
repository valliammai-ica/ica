package com.rac.iam.okta.adp.csv.employee.importer.impl;

import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.rac.iam.okta.adp.csv.employee.importer.ImportErrorNotifier;
import com.rac.iam.okta.adp.csv.employee.importer.exception.NotificationException;
import com.rac.iam.okta.adp.csv.employee.importer.model.NotificationMessage;


@Configuration
public class MailImportErrorNotifierImpl implements ImportErrorNotifier {
	
	private static final Logger LOGGER = LoggerFactory.getLogger("import_logfile");
	
	@Value("${importer.smtp.server}")
	private String smtpServer = null;
	
	@Value("${importer.smtp.mail.recepient}")
	private String to = null;
	
	@Value("${importer.smtp.mail.user}")
	private String smtpMailUserName = null;
	
	@Value("${importer.smtp.mail.user.password}")
	private String smtpMailUserPassword = null;
	
	@Value("${importer.smtp.mail.recepient.reactivation}")
	private String reactivationToList = null;
	
	@Override
	public void sendNotification(NotificationMessage message) throws NotificationException{
		LOGGER.info("Sending email notification to: " + to);
		Properties properties = System.getProperties();
	      properties.setProperty("mail.user", smtpMailUserName);
	      properties.setProperty("mail.password", smtpMailUserPassword);
	      properties.setProperty("mail.smtp.host", smtpServer);
	      Session session = Session.getDefaultInstance(properties);

	      try{
	         MimeMessage emailMessage = new MimeMessage(session);
	         emailMessage.setFrom(new InternetAddress(smtpMailUserName));
	         emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
	         emailMessage.setSubject(message.getSubjectLine());
	         emailMessage.setText(message.getMessageBody());
	         Transport.send(emailMessage);
	         LOGGER.info("Mail sent to: " + to);
	      }catch (MessagingException mex) {
	         LOGGER.error(mex.getMessage(), mex);
	         throw new NotificationException(mex.getMessage(), mex);
	      }
	}
	
	@Override
	public void sendReactivationNotification(NotificationMessage message) throws NotificationException{
		LOGGER.info("Sending reactivation notification to: " + reactivationToList);
		Properties properties = System.getProperties();
	      properties.setProperty("mail.user", smtpMailUserName);
	      properties.setProperty("mail.password", smtpMailUserPassword);
	      properties.setProperty("mail.smtp.host", smtpServer);
	      Session session = Session.getDefaultInstance(properties);

	      try{
	         MimeMessage emailMessage = new MimeMessage(session);
	         emailMessage.setFrom(new InternetAddress(smtpMailUserName));
	         emailMessage.setSubject(message.getSubjectLine());
	         emailMessage.setText(message.getMessageBody());
	         StringTokenizer st = new StringTokenizer(reactivationToList,",");
	         while (st.hasMoreTokens()) {
	        	 String addy = st.nextToken();
	        	 emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(addy));
	         }
	         
	         Transport.send(emailMessage);
	         LOGGER.info("Reactivation mail sent to: " + reactivationToList);
	      }catch (MessagingException mex) {
	         LOGGER.error(mex.getMessage(), mex);
	         throw new NotificationException(mex.getMessage(), mex);
	      }
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getSmtpMailUserName() {
		return smtpMailUserName;
	}

	public void setSmtpMailUserName(String smtpMailUserName) {
		this.smtpMailUserName = smtpMailUserName;
	}

	public String getSmtpMailUserPassword() {
		return smtpMailUserPassword;
	}

	public void setSmtpMailUserPassword(String smtpMailUserPassword) {
		this.smtpMailUserPassword = smtpMailUserPassword;
	}

}
