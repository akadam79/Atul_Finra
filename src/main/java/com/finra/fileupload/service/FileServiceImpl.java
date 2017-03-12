package com.finra.fileupload.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation for saving file and it's metadata on server.
 * @author akadam
 * @since 1.0
 */
@Service
public class FileServiceImpl implements FileService {

	/**Logger*/
	private static final Logger logger = Logger.getLogger(FileServiceImpl.class.getCanonicalName());

	@Autowired
	FileManagerHelper fileManagerHelper;
	
	/**Path under which files to be stored*/
	private static final String PATH = "file_storage";
	

	/** (non-Javadoc)
	 * @see com.finra.fileupload.service.FileService#save(com.finra.fileupload.service.FileContainer)
	 */
	@Override
	public boolean save(FileContainer container) {
		logger.info("File persisting started");
		fileManagerHelper.storeFileToUniqueLocation(container, PATH);
		logger.info("File persisting completed");
		return true;
	}

	/** (non-Javadoc)
	 * @see com.finra.fileupload.service.FileService#findMetaData(java.lang.String, java.util.Date)
	 */
	@Override
	public List<Map<String, String>> findMetaData(String user, Date fileCreationDate) {
		logger.info("Started searching metadata for user["+user+"] and fileCreationDate["+fileCreationDate+"]");
		List<Map<String, String>> metadata = fileManagerHelper.searchFileMetaData(PATH, user, fileCreationDate);
		logger.info("Completed searching metadata for user["+user+"] and fileCreationDate["+fileCreationDate+"]");
		return metadata;
	}
	

}
