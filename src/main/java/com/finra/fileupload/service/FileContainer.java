package com.finra.fileupload.service;

import java.io.Serializable;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Value object for storing file bytes and file metadata. 
 * @author akadam
 * @since 1.0
 */
public class FileContainer implements Serializable {

	/**Logger*/
	private static final Logger logger = Logger.getLogger(FileContainer.class.getCanonicalName());
	
	/**Serail version UID*/
	private static final long serialVersionUID = -1L;
	
	/**User who uploaded this file*/
    private String userName;
    
	/**file contents*/
	private byte[] fileContentBytes;
	
	/**Name of the file uploaded*/
	private String fileName;
    
	/**date on which file is uploaded*/
	protected Date fileDate;

	
	public FileContainer(String userName, byte[] fileContentBytes,
			String fileName, Date fileDate) {
		super();
		this.userName = userName;
		this.fileContentBytes = fileContentBytes;
		this.fileName = fileName;
		this.fileDate = fileDate;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public byte[] getFileContentBytes() {
		return fileContentBytes;
	}

	public void setFileContentBytes(byte[] fileContentBytes) {
		this.fileContentBytes = fileContentBytes;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Date getFileDate() {
		return fileDate;
	}

	public void setFileDate(Date fileDate) {
		this.fileDate = fileDate;
	}
}
