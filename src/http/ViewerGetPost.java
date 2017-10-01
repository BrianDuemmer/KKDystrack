package http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Communicates to the web server database via
 * @author Duemmer
 *
 */
public class ViewerGetPost extends PostRequest 
{
	Map<String, Object> params = new LinkedHashMap<>();
	
	public ViewerGetPost(String userID) {
		params.put("target_op", "get_by_uid"); // set function to get by user ID
		params.put("target_id", "viewer"); // set the target to be for the viewer handler
		params.put("data", "{ \"userID\":\"" +userID+ "\"}");
		
	}
	
	
	

	
	public String send()
	{	
		// Setup the request and send
		setParams(params);
		
		try { setUrl(new URL("https://www.dystify.com/dev/viewer.php")); } 
		catch (MalformedURLException e) { e.printStackTrace(); }
		
		String rawResp = super.send();
		return rawResp;
	}

}
