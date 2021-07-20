package com.rac.iam.okta.adp.csv.employee.importer;

import com.rac.iam.okta.adp.csv.employee.importer.exception.FileDownloadException;

public interface FileDownloader {
	
	public void copyFilesFromSource() throws FileDownloadException;

}
