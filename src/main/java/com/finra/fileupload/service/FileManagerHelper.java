package com.finra.fileupload.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Utility file handler for all file operations
 * @author akadam
 * @since 1.0
 */
public class FileManagerHelper {

	/**Logger*/
	private static final Logger logger = Logger.getLogger(FileManagerHelper.class.getCanonicalName());

	/**For generating unique identifier*/
	@Autowired
	SecureRandom secureRandom;
	
	/**Simple Date format*/
	@Autowired
	DateFormat dateFormat;
	
	/**File name constant*/
	private static final String FILE_NAME = "File_Name";
	
	/**User name constant*/
	private static final String USER_NAME = "User_Name";
	
	/**Uploaded date constant*/
	private static final String UPLOADED_DATE = "Uploaded_Date";
	
	/**Creation date constant*/
	private static final String CREATION_DATE = "Creation_Date";

	/**
	 * Saves File and its metadata to unique location on disk
	 * @param container object representing file with its metadata
	 * @param parentDirectoryPath parent/root path where files to be saved.
	 */
	public void storeFileToUniqueLocation(FileContainer container, String parentDirectoryPath) {
		String directoryPath = getUniquePath(parentDirectoryPath);
		createDirectory(directoryPath);
		persistFile(container, directoryPath);
	}

	/**
	 * Finds file metadata saved on disk.
	 * @param parentDirectoryPath parent directory to searched into, for file metadata
	 * @param user user to which this metadata belongs to
	 * @param fileCreationDate optional file creation date
	 * @return List of all file metadata
	 */
	public List<Map<String, String>> searchFileMetaData(String parentDirectoryPath, String user, Date fileCreationDate) {
		List<Map<String, String>> metadata = null;
		File file = new File(parentDirectoryPath);
		String[] directories = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		
		if(directories != null && directories.length > 0) {
			metadata =  getMetaData(parentDirectoryPath, directories, user, fileCreationDate);
		}else {
			metadata = new ArrayList<Map<String,String>>();
		}
		return metadata;
	}
	/**
	 * Creates unique path on server where we will save file
	 * @return
	 */
	private String getUniquePath(String parentDir) {
		return new StringBuilder(parentDir).append(File.separator).append(new BigInteger(130, secureRandom).toString(32)).toString();
	}
	
	/**
	 * Creates the directory if it doesn not exists on server
	 * @param path
	 */
	private void createDirectory(String path) {
		File file = new File(path);
		file.mkdirs();
	}
	
	
	/**
	 * SAves file on server
	 * @param container
	 * @param directoryPath
	 */
	private void persistFile(FileContainer container, String directoryPath){
		File dir = new File(directoryPath);
		File fileToStore = new File(dir, container.getFileName());
		
		FileOutputStream fStream = null;
		FileOutputStream fos = null;
		BufferedOutputStream bStream = null;
		try {
			fStream = new FileOutputStream(fileToStore);
			bStream = new BufferedOutputStream(fStream);
			bStream.write(container.getFileContentBytes());
			
			Properties prop= new Properties();
			prop.put(FILE_NAME, container.getFileName());
			prop.put(USER_NAME, container.getUserName());
			prop.put(CREATION_DATE, dateFormat.format(container.getFileDate()));
			prop.put(UPLOADED_DATE, dateFormat.format(new Date()));
			
			String propertiesFileLocation = directoryPath+File.separator+container.getFileName()+".properties"; 
			fos = new FileOutputStream(propertiesFileLocation);
			prop.store(fos, "=============================File Metadata=============================");
		}catch(IOException e) {
			logger.log(Level.SEVERE, "Failed while saving file on disk", e);
			throw new RuntimeException(e);
		}finally {
			if(bStream != null) {
				try {
					bStream.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Failed while closing stream [bStream] ", e);
					throw new RuntimeException(e);
				}
			}
			if(fStream != null) {
				try {
					fStream.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Failed while closing stream [fStream]", e);
					throw new RuntimeException(e);
				}
			}
			if(fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Failed while closing stream [fos]", e);
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * Finds the file metadata from the list of directories under parent directory
	 * @param parentDirectoryPath parent directory to searched into, for file metadata
	 * @param directories list of directoties from which metadata needs to be searched
	 * @param user user to which this metadata belongs to
	 * @param fileCreationDate optional file creation date
	 * @return List of all file metadata
	 */
	public List<Map<String, String>> getMetaData(String parentDirectoryPath, String[] directories, String user, Date fileCreationDate) {
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		
		logger.fine("Total directories to scan ["+directories.length+"]");
		for(String dir : directories) {
			String path = parentDirectoryPath+File.separator+dir;
			logger.fine("Checking properties files under ["+path+"]");
			
			String[] filesNames = new File(path).list(new FilenameFilter() {
				  @Override
				  public boolean accept(File current, String name) {
					  logger.fine("File Name ["+name+"]");
					  return name.endsWith(".properties");
				  }
				});
			
			if(filesNames == null || filesNames.length < 1) {
				return new ArrayList<Map<String, String>>();
			}
			
			for(String propFileName : filesNames) {
				String propFilePath = path + File.separator + propFileName;
				Map<String, String> map = getMetaDataFromPropertiesFiles(propFilePath, user, fileCreationDate);
				if(map != null && !map.isEmpty()) {
					data.add(map);
				}
			}
		}
		
		return data;
	}
	
	/**
	 * Loads metadata from the given properties file
	 * @param propFileName relative path of property file and name from which data needs to be loaded 
	 * @param user user to which this metadata belongs to
	 * @param fileCreationDate optional file creation date
	 * @return key value pair from the file
	 */
	public Map<String, String> getMetaDataFromPropertiesFiles(String propFileName, String user, Date fileCreationDate) {
		Map<String, String> map = new HashMap<String, String>();
		Properties prop = new Properties();
		InputStream input = null;     
		try {
			input = new FileInputStream(new File(propFileName));
			prop.load(input);
			 
			StringWriter writer = new StringWriter();
			prop.list(new PrintWriter(writer));
			logger.fine("Listing all properties ["+writer.getBuffer().toString()+"]");
			
			if(prop.getProperty(USER_NAME).equalsIgnoreCase(user)) {
				if(fileCreationDate != null) {
					String searchDate = dateFormat.format(fileCreationDate);
					if(prop.getProperty(CREATION_DATE).equalsIgnoreCase(searchDate)) {
						populate(prop, map);
					}
				}else {
					populate(prop, map);
				}
			}
			return map;
		}catch(IOException e) {
			logger.log(Level.SEVERE, "Failed while opening stream to properties file", e);
			throw new RuntimeException(e);
		}finally {
			if(input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Failed while closing stream [input]", e);
				}
			}
		}
	}
	
	/**
	 * Populates the data from properties object to Map object
	 * @param prop properties object which has all properties
	 * @param map to be populated in this map
	 */
	private void populate(Properties prop, Map<String, String> map) {
		for(String key : prop.stringPropertyNames()) {
			map.put(key, prop.getProperty(key));
		}
	}
}
