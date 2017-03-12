package com.finra.fileupload.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import com.finra.fileupload.service.FileContainer;
import com.finra.fileupload.service.FileService;

/**
 * Filehandling controller hosts the functionality of
 * all REST services related to file upload, managing its
 * metadata and so on. Curently supported services are as below
 * 
 * 
 * Service :- 1
 * /upload?file={file}&user={user}&fileCreationDate={fileCreationDate}
 * file :- File to be uploaded,
 * user :- user who is uploading this file
 * fileCreationDate :- file creation date, Format has to be yyyy-MM-dd
 * This service will upload the file and will also save meatadata associated with this file.
 * 
 * Service :- 2
 * /metadata?user={user}&fileCreationDate={fileCreationDate}
 * user :- metata that belongs to this user, mandatory param
 * fileCreationDate :- file creation date, Format has to be yyyy-MM-dd, optional param
 * This service will search all the metadata for a given user and optinal file creation date
 * 
 * 
 * @author akadam
 * @since 1.0
 */
@RestController
public class FileHandlingController {
	
	/**Logger*/
	private static final Logger logger = 
			Logger.getLogger(FileHandlingController.class.getCanonicalName());
	
	@Autowired
	FileService fileService;
	
	/**
	 * Simple welcome service.
	 * @return
	 */
	@RequestMapping("/")
    public String index() {
        return "Welcome, Guest...!";
    }
	
	/**
	 * Uploads the file to server.
	 * @param file File to be uploaded
	 * @param fileCreationDate file creation date
	 * @param user user who is trying to upload a file
	 * @return returns message if file is uploaded successfully
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public String uploadFile(@RequestParam(value="file", required=true) MultipartFile file ,
			@RequestParam(value="fileCreationDate", required=true) @DateTimeFormat(pattern="yyyy-MM-dd") Date fileCreationDate,
			@RequestParam(value="user", required=true) String user) {
	
		try {
			FileContainer continer = new FileContainer(user, file.getBytes(), file.getName(), fileCreationDate);
			fileService.save(continer);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed while file uploading", e);
			throw new RuntimeException(e);
		}
		
		return "File successfully uploaded";
	}
	
	/***
	 * Finds the metadata for given request
	 * @param user user who owned the document
	 * @param fileCreationDate file creation date to filter out the results
	 * @return List of metadata
	 */
	@RequestMapping(value = "/metadata", method=RequestMethod.GET)
	public HttpEntity<List<Map<String, String>>> getMetaData(@RequestParam(value="user", required=true) String user,
			@RequestParam(value="fileCreationDate", required=false) @DateTimeFormat(pattern="yyyy-MM-dd") Date fileCreationDate) {
		
		List<Map<String, String>> data = fileService.findMetaData(user, fileCreationDate);
		return new ResponseEntity<List<Map<String, String>>>(data, HttpStatus.OK);
	}
}
