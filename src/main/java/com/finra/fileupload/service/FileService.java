package com.finra.fileupload.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service to save the file on server.
 * @author akadam
 * @since 1.0
 */
public interface FileService {

	/**
	 * Saves the file to the server and also saves the metadata
	 * associated with this file
	 * @param container fileContainer object representing all data
	 * @return true or false
	 */
	public boolean save(FileContainer container);
	
	/**
	 * Finds the metadata about the files uploaded by a given user.
	 * @param user user to whom file belongs to
	 * @param fileCreationDate the date on which file was created
	 * @return list of metadata.
	 */
	public List<Map<String, String>> findMetaData(String user, Date fileCreationDate);
	
}
