package util;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import application.DysMain;
import db.RCTables;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

/**
 * Contains some general purpose utility methods that don't fit anywhere else
 * @author Duemmer
 *
 */
public class Util 
{

	/**
	 * Converts an integer number of seconds to a time string in the form of mins:secs
	 */
	public static String secsToTimeString(int seconds)
	{
		return String.format("%d:%02d", Math.floorDiv(seconds, 60), seconds%60);
	}
	
	
	
	/**
	 * Fits a string to lie within a certain length
	 * @param maxLen the max length for the string. Must be > 4
	 * @param str the string to trim
	 */
	public static String ellipseTrim(int maxLen, String str)
	{
		if(str.length() > maxLen)
			str = str.substring(0, maxLen-4) + "..."; // subtract 4 instead of 1 to account for the ellipse
		
		return str;
	}
	
	
	
	
	/**
	 * Verifies a textField for data valididty (As a double), alerts the user if something's wrong, or
	 * writes to the database if it's good 
	 * @param name the settings key name to write to
	 * @param field
	 */
	public static void writeDoubleTextFieldToDB(String name, TextField field) {
		try {
			double val = Double.parseDouble(field.getText()); // Attempt to parse out the field

			Thread t = new Thread(() -> {  // If we get here the field is valid, so we can push it to the database
				DysMain.localDB.writeParam(name, val);
				DysMain.localDB.upSync(DysMain.remoteDB, RCTables.paramTable, false);
			});

			t.setDaemon(true);
			t.setName("WriteDoubleTextFieldToDB");
			t.start();
		} catch (Exception e) // format / display the error message, then update the field to be a safe value
		{
			Alert a = new Alert(AlertType.ERROR);
			a.setContentText("The value \"" +field.getText()+ "\" is not a valid number!");
			a.setTitle("Bad input!");
			a.setHeaderText("Bad input!");
			a.showAndWait();

			field.setText("0");
		}
	}
	
	
	/**
	 * Writes the value of the given checkbox to the database
	 */
	public static void writeCheckboxToDB(CheckBox box, String name) {
		Thread t = new Thread(() -> {
				DysMain.localDB.writeParam(name, box.isSelected());
				DysMain.localDB.upSync(DysMain.remoteDB, RCTables.paramTable, false);
		});

		t.setDaemon(true);
		t.setName("write " +name+ " to DB");
		t.start();
	}
	
	
	
	
	/**
	 * Writes the ReqMode to the database. This will run in a background thread.
	 */
	public static void writeRequestModeToDB(ReqMode mode) {
		Thread t = new Thread(() -> {
			DysMain.localDB.writeParam("requestMode", mode.toString());
			DysMain.localDB.upSync(DysMain.remoteDB, RCTables.paramTable, false);
		});
		t.setDaemon(true);
		t.setName("writeRequestMode");
		t.start();
	}


	
	
	
	/**
	 * Copies a file from inside of a jar file to some location in the filesystem
	 * @param srcFile path of the file relative to the "src" directory
	 * @param dstFile the path on the filesystem to save this to
	 */
	public static void jarCopy(String srcFile, String dstFile) throws Exception
	{
		InputStream in = Util.class.getClassLoader().getResourceAsStream(srcFile);
		
		FileOutputStream out = new FileOutputStream(dstFile);
		
		byte[] buf = new byte[4096];
		int r;
		
		while((r = in.read(buf)) != -1)
			out.write(buf, 0, r);
		
		out.close();
		in.close();
	}
	
	
	
	
	/**
	 * returns a new Gson object that will accept json produced by
	 * the php end of things
	 */
	public static Gson gsonFromPHP() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Boolean.class, new BooleanAsIntAdapter());
		builder.registerTypeAdapter(boolean.class, new BooleanAsIntAdapter());
		builder.registerTypeAdapter(Date.class, new DateDeserializer());
		return builder.create();
	}
}







