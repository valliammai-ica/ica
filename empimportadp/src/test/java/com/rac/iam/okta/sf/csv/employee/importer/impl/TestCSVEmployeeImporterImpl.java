package com.rac.iam.okta.sf.csv.employee.importer.impl;

import org.junit.Test;
import com.rac.iam.okta.adp.csv.employee.importer.exception.EmployeeImportException;
import com.rac.iam.okta.adp.csv.employee.importer.impl.CSVEmployeeImporterImpl;
import com.rac.iam.okta.adp.csv.employee.importer.impl.MailImportErrorNotifierImpl;

public class TestCSVEmployeeImporterImpl {

	@Test
	public void testImportFiles() throws EmployeeImportException {
		/*CSVEmployeeImporterImpl importer = setUpImporter();
		importer.afterCreation();
		String testDataDir = importer.getLocalSourceFolder().replace("EmployeeData", "testData");
		importer.setLocalSourceFolder(testDataDir);
		String processedTestDataDir = importer.getLocalProcessedFolder().replace("ProcessedEmployeeData", "processedTestData");
		importer.setLocalProcessedFolder(processedTestDataDir);
		importer.importEmployeeRecords();*/
	}

	private CSVEmployeeImporterImpl setUpImporter() {
		CSVEmployeeImporterImpl importer = new CSVEmployeeImporterImpl();
		MailImportErrorNotifierImpl notifier = new MailImportErrorNotifierImpl();
		notifier.setSmtpMailUserName("matt.ruehle@rentacenter.com");
		notifier.setSmtpServer("10.40.1.160");
		notifier.setTo("matt.ruehle@rentacenter.com");

		return importer;
	}

}
