package com.finra.fileupload;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.finra.fileupload.service.FileManagerHelper;

/**
 * Main class OR entry point for spring boot application.
 * @author akadam
 * @since 1.0
 */
@SpringBootApplication
public class Application {
	
	/**
	 * Spring boot application entry point.
	 * @param args command line arguments
	 */
    public static void main(String[] args ){
    	SpringApplication.run(Application.class, args);
    }
    
    /**
     * FileManagerHelper bean
     * @return
     */
    @Bean
    public FileManagerHelper getFileManagerHelper() {
    	return new FileManagerHelper();
    }
    
    /**
     * SecureRandom bean
     * @return
     */
    @Bean
    public SecureRandom getSecureRandom() {
    	return new SecureRandom();
    }
    
    /**
     * SimpleDataFormat bean
     * @return
     */
    @Bean
    public DateFormat getSimpleDateFormat() {
    	return new SimpleDateFormat("yyyy-MM-dd");
    }
}
