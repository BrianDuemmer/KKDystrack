package rc.viewer;

import java.util.Date;

import com.google.gson.Gson;

import http.ViewerGetPost;
import util.Util;

public class ViewerFactory {
	
	/** Returns DysBot's credentials. Useful for dummy Viewers */
	public static final Viewer dysbot = new Viewer(
			"Dysbot", 
			"UCq4Zg02QNxH3jrLW-1tBYvw", 
			0, 
			"", 
			true, 
			false, 
			0, 
			0, 
			0, 
			"Korok", 
			"Ancient Robot", 
			new Date(1502820956000L), 
			new Date(1502820956000L), 
			"");
	
	/** RNGsus's credentials. 'Nuff said. */
	public static final Viewer RNGsus = new Viewer(
			"RNGsus", 
			"UCOlo_l_Jmq6pUc1FIGYUimA", 
			0, 
			"", 
			true, 
			false, 
			0, 
			0, 
			0, 
			"Korok", 
			"Literal God", 
			new Date(1502820956000L), 
			new Date(1502820956000L), 
			"");
	
	
	public static Viewer newViewer(String userID) {
		ViewerGetPost req = new ViewerGetPost(userID);
		String raw = req.send();
		System.out.println(raw);
		
		// decode the raw data
		Gson dat = Util.gsonFromPHP();
		Viewer vw = dat.fromJson(raw, Viewer.class);
		
		if(vw == null || vw.getUserID() == null) { // user wasn't found. warn and return a safe default
			System.err.println("WARNING: Viewer with channel ID \"" +userID+ "\" was not found!");
			return dysbot;
		}
		
		return vw;
	}
}










