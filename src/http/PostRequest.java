package http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Serves as a backbone class for sending POST requests
 * @author Duemmer
 *
 */
public class PostRequest {

	private Map<String, Object> params;
	private URL url;

	public PostRequest() {  
		
	}


	/**
	 * Sends the request, and returns the response as a String. Set the URL and 
	 * parameters first!
	 */
	protected String send()
	{
		// nullcheck the parameters
		if(params == null || url == null) {
			System.err.println("POST request was not initialized!");
			return "";
		}
		
		String respString = "";
		
		// format the post data
		StringBuilder postData = new StringBuilder();
		try {
			for(Map.Entry<String, Object> param : this.params.entrySet()) {
				if(postData.length() > 0)
					postData.append('&');
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
			byte[] postValBytes = postData.toString().getBytes();

			// Setup the connection
			HttpsURLConnection conn = (HttpsURLConnection) this.url.openConnection();

			// Add the metadata
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", String.valueOf(postValBytes.length));
			conn.setDoOutput(true);
			conn.getOutputStream().write(postValBytes);

			// read the input
			BufferedReader respReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

			String tmpRead;
			StringBuffer response = new StringBuffer();

			while((tmpRead = respReader.readLine()) != null)
				response.append(tmpRead);
			
			respString = response.toString();

		} catch (Exception e) {
			System.err.println("Error sending request to address " +url.toString());
			e.printStackTrace();
		}
		
		return respString;
	}


	
	protected void setParams(Map<String, Object> params) {
		this.params = params;
	}


	public void setUrl(URL url) {
		this.url = url;
	}

}
