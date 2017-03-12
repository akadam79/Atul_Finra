package com.finra.fileupload;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.net.URL;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;

/**
 * Test case to test Restful services we build
 * @author akadam
 * @since 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileControllerTest {
	
	/**Logger*/
	private static final Logger logger = Logger.getLogger(FileControllerTest.class.getCanonicalName());
	@LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate template;

    @Test
    public void getHello() throws Exception {
    	logger.info("Testing welcome service.");
    	URL base = new URL("http://localhost:" + port + "/");
        ResponseEntity<String> response = template.getForEntity(base.toString(),
                String.class);
        assertThat(response.getBody(), equalTo("Welcome, Guest...!"));
    }
    
    @Test
    public void testUploadFile() throws Exception {
    	logger.info("Testing happy path scenario uploading file.");
    	
    	URL url = new URL("http://localhost:" + port + "/upload?user={user}&fileCreationDate={fileCreationDate}");
    	MultiValueMap<String, Object> fileParams = createFileParam();
    	String fileCreationDate = "2017-01-01";
    	String user = "Service-Account-User";
    	
        String response = template.postForObject(url.toString(), fileParams, String.class, user, fileCreationDate);
        assertThat(response, equalTo("File successfully uploaded"));
    }
    
    @Test
    public void testWrongPath() throws Exception {
    	logger.info("Testing invalid service.");
    	
    	URL url = new URL("http://localhost:" + port + "/wrongupload?user={user}&fileCreationDate={fileCreationDate}");
    	MultiValueMap<String, Object> fileParams = createFileParam();
    	String fileCreationDate = "2017-01-01";
    	String user = "Service-Account-User";
    	ResponseEntity<String> response = template.postForEntity(url.toString(), fileParams, String.class, user, fileCreationDate);
        Assert.assertEquals(response.getStatusCodeValue(), 404);
    }

    @Test
    public void testWrongParam() throws Exception {
    	logger.info("Testing incorrect params.");
    	URL url = new URL("http://localhost:" + port + "/upload?user={user}&fileCreationDate={fileCreationDate}");
    	MultiValueMap<String, Object> fileParams = createFileParam();
    	String fileCreationDate = "05-02-1995";
    	String user = "Service-Account-User";
    	ResponseEntity<String> response = template.postForEntity(url.toString(), fileParams, String.class, user, fileCreationDate);
        Assert.assertEquals(response.getStatusCodeValue(), 400);
    }

    @Test
    public void testMissingFile() throws Exception {
    	logger.info("Testing missing post data.");
    	URL url = new URL("http://localhost:" + port + "/upload?user={user}&fileCreationDate={fileCreationDate}");
    	MultiValueMap<String, Object> fileParams = null;
    	String fileCreationDate = "05-02-1995";
    	String user = "Service-Account-User";
    	ResponseEntity<String> response = template.postForEntity(url.toString(), fileParams, String.class, user, fileCreationDate);
        Assert.assertEquals(response.getStatusCodeValue(), 500);
    }

    @Test
    public void testFileSearchByUser()throws Exception {
    	String user = "Test-User-1";
    	String date = "2017-01-01";
    	
    	uploadFilesForTesting(user, date);
    	URL url = new URL("http://localhost:" + port + "/metadata?user={user}");
    	
    	List<Map<String,String>> list = template.getForObject(url.toString(), List.class, user);
    	Assert.assertNotNull(list);
    	Assert.assertTrue(list.size() > 0);
    	
    	String searchedUser = list.get(0).get("User_Name");
    	String searchedDate = list.get(0).get("Creation_Date");
    	
    	Assert.assertNotNull(searchedUser);
    	Assert.assertNotNull(searchedDate);
    	
    	assertThat(searchedUser, equalTo(user));
    	assertThat(searchedDate, equalTo(date));
    }

    @Test
    public void testFileSearchByUserAndDate()throws Exception {
    	String user = "Test-User-2";
    	String date = "2017-01-02";

    	uploadFilesForTesting(user,date);
    	
    	URL url = new URL("http://localhost:" + port + "/metadata?user={user}&fileCreationDate={fileCreationDate}");
    	
    	Class<List<Map<String, String>>> x;
    	
    	List<Map<String,String>> list = template.getForObject(url.toString(), List.class, user, date);
    	Assert.assertNotNull(list);
    	Assert.assertTrue(list.size() > 0);
    	
    	String searchedUser = list.get(0).get("User_Name");
    	String searchedDate = list.get(0).get("Creation_Date");
    	
    	Assert.assertNotNull(searchedUser);
    	Assert.assertNotNull(searchedDate);
    	
    	assertThat(searchedUser, equalTo(user));
    	assertThat(searchedDate, equalTo(date));
    }
    
    /**
     * Builds multipart file to be loaded on Request
     * @return
     */
    private MultiValueMap<String, Object> createFileParam() {
    	
    	//Yea Yea little shortcut here. We should have used @Value("classpath:test.txt") But then it gives us stream
    	//Which means you need to push that to disc (mostly under /tmp/) and then get File path. So instead of that let's just use
    	//File from resources directory.
    	String filePath = "src"+File.separator+"test"+File.separator+"resources"+File.separator+"test.txt";
    	
    	//Check file exists at this location.
    	File file = new File(filePath);
    	if(!file.exists()) {
    		throw new RuntimeException("File doesn't exists at ["+filePath+"]");
    	}
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<String, Object>();           
        param.add("file", new FileSystemResource(filePath));
        return param;
    }

    private void uploadFilesForTesting(String user, String date) {
    	MultiValueMap<String, Object> fileParams = createFileParam();
        template.postForObject("http://localhost:" + port + "/upload?user={user}&fileCreationDate={fileCreationDate}", fileParams, String.class, user, date);
    }
}
