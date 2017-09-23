package db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.sqlite.SQLiteErrorCode;

import application.DysMain;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import util.ReqMode;

/**
 * This class performs all of the database IO. Any SQL to execute is 
 * sent here, and results returned
 * @author Duemmer
 *
 */
public abstract class DatabaseIO 
{

	// maximum number of retries when encountering sqlite_busy
	private int busyRetriesMax = 3;






	private Connection db; // reference to the database we are connected to
	private String url; // address of the database
	private boolean connected = false; // specifies if we currently have a valid connection to the database

	/**
	 * @param url the Location / format identifyer of the database
	 */
	public DatabaseIO(String url)
	{
		this.url =url;
	}




	/**
	 * Checks to make sure we are validly connected to the 
	 * database. If it isn't open already, this will do it
	 */
	public void verifyConnected() throws SQLException
	{
		if(db == null || !db.isValid(3)) // If we are null or the connection is invalid, reestablish the connection
		{
			if(db != null)
				db.close();

			System.out.println("Connecting to database...");
			db = connect(this.url);
			connected = db != null;

			if(connected)
				System.out.println("Connection to database: \"" +url+ "\" has been established");
			else
			{
				System.err.println("Failed to establish connection to database \"" +url+ "\"");
				throw new SQLException("Connection invalid");
			}
		}
	}
	
	
	
	
	/**
	 * Attempts to connect to the database specified by <code>url</code>
	 * @return The reference to the database we connected to, or null if the connection is invalid
	 */
	protected abstract Connection connect(String url) throws SQLException;





	/**
	 * Executes sql on the database. This will automatically verify that the connection to the
	 * database is valid. Will also commit the results.
	 * @param sql the sql statement to execute
	 * @return the @link ResultSet of the query, or null if the reference to the database is invalid
	 */
	public ResultSet execRaw(String sql) throws SQLException
	{
		synchronized(this)
		{
			ResultSet res = null;
			Statement st;
			boolean isBusy = false;
			int busyRetries = 0;

			// Attempt to connect. Print an error and return an empty set on failure
			try { verifyConnected(); } 
			catch (SQLException e) 
			{
				e.printStackTrace();
				return res;
			}

			// Prepare and execute the query, and continue and retry if necessary
			do {
				try  {
					st = db.createStatement();
					st.closeOnCompletion();
					if (st.execute(sql)) // execute the query, and if it gave us a result set, grab it, and set the statement to close when the resultset is done being used
					{
						res = st.getResultSet();
						st.closeOnCompletion();
					}
					else // the action wasn't a query, so it had no resultset and we can close the statement
						st.close();

					break; // If we get this far the statement executed correctly, so we can just break out of the loop
				} catch (SQLException e) 
				{
					isBusy = e.getErrorCode() == SQLiteErrorCode.SQLITE_BUSY.code;
					if(isBusy && busyRetries <= busyRetriesMax) // If it's busy, and hasn't retried too much, make a special note, wait a little bit for everything to clear, and try again
					{
						busyRetries++;
						System.err.println("WARNING: read query \"" +sql+ "\" encountered SQLITE_BUSY. Retryig in 250ms...");
						try { Thread.sleep(250); } catch (InterruptedException e1) { e1.printStackTrace(); }
					} else if (isBusy && busyRetries > busyRetriesMax) // we retried too many times. This is likely an indicator of a larger issue if this runs...
					{
						System.err.println("FATAL: read query \"" +sql+ "\" encountered SQLITE_BUSY too many times. Cancelling...");
						throw e; //propagate up the error in the event it gives a little more info
					}

					else // something other than a busy happened - let the caller handle it
						throw e;
				}
			} while(isBusy);
			
			return res;
		}
	}






	/**
	 * Executes the preparedStatement on the database. This will automatically verify that the connection to the
	 * database is valid. Will also commit the results.<br><br>
	 * 
	 * <B> NOTE: </B> These methods are thread safe for writes. Regular statements ARE NOT!
	 * 
	 * @param sql the preparedStatement to execute
	 * @return the @link ResultSet of the query, or null if the reference to the database is invalid
	 */
	public ResultSet execRaw(PreparedStatement ps) throws SQLException
	{
		synchronized(this)
		{
			ResultSet res = null;
			boolean isBusy = false;
			int busyRetries = 0;

			// Attempt to connect. Print an error and return an empty set on failure
			try { verifyConnected(); } 
			catch (SQLException e) 
			{
				e.printStackTrace();
				return res;
			}

			// Prepare and execute the query, and continue and retry if necessary
			do {
				try  {
					if (ps.execute()) {// execute the query, and if it gave us a result set, grab it. Close when we're done with the reultset
						res = ps.getResultSet();
						ps.closeOnCompletion();
					}
					
					else // no resultset needed, so close it
						ps.close();

					break; // If we get this far the statement executed correctly, so we can just break out of the loop
				} catch (SQLException e) 
				{
					isBusy = e.getErrorCode() == SQLiteErrorCode.SQLITE_BUSY.code;
					if(isBusy && busyRetries <= busyRetriesMax) // If it's busy, and hasn't retried too much, make a special note, wait a little bit for everything to clear, and try again
					{
						busyRetries++;
						System.err.println("WARNING: PreparedStatement encountered SQLITE_BUSY. Retryig in 250ms...");
						try { Thread.sleep(250); } catch (InterruptedException e1) { e1.printStackTrace(); }
					} else if (isBusy && busyRetries > busyRetriesMax) // we retried too many times. This is likely an indicator of a larger issue if this runs...
					{
						System.err.println("FATAL: PreparedStatement encountered SQLITE_BUSY too many times. Cancelling...");
						throw e; //propagate up the error in the event it gives a little more info
					}

					else // something other than a busy happened - let the caller handle it
						throw e;
				}
			} while(isBusy);
			return res;
		}
	}








	/**
	 * Converts a @link ResultSet to a string
	 */
	public String resultsToString(ResultSet rs)
	{
		String ret = "";
		try 
		{
			ResultSetMetaData md = rs.getMetaData();
			int colNum = md.getColumnCount();
			while(rs.next())
			{
				for (int i = 1; i <= colNum; i++) {
					if (i > 1) 
						ret += ", ";
					String columnValue = rs.getString(i);
					ret += md.getColumnName(i) + " "+ columnValue + "\n";
				}
				ret += "\n";
			}
		} 

		catch (SQLException e)  { e.printStackTrace(); }
		return ret;
	}




	/**
	 * Reads a Request controller parameter from the database
	 * @param key
	 * @return the value of the key (cast to an appropriate type) or null
	 * if the key wasn't found
	 */
	private Object readParamBase(String key, boolean asString)
	{	
		synchronized(this)
		{
			String colName = "num_val";
			if(asString)
				colName = "str_val";
			
			
			// Will return the default value of either a string or a number (depending on asString parameter) on failure
			Object val = asString  ?  "" : new Double(0); 
			
			// Use a prepared Statement for added security
			RCTables.paramTable.verifyExists(db);
			String sql = "SELECT " +colName+ " FROM " +RCTables.paramTable.getName()+ " WHERE setting = ?";
			ResultSet rs;
			try
			{
				// Be sure we are connected and that the table exists before proceeding
				verifyConnected();
				RCTables.paramTable.verifyExists(db);

				PreparedStatement ps = db.prepareStatement(sql);
				ps.setString(1, key);
				rs = execRaw(ps);

				if(!rs.next()) // if empty, print a warning and return empty string
				{
					System.err.println("WARNING: Could not find parameter key " +key+ ". Creating key with default values...");
					writeRawParam(key, "", 0);
					//Thread.dumpStack(); // sometimes enabled for debugging
				} 
				// If this is hit then there is at least 1 result, parse the proper type
				else if(asString){ val = rs.getString(1); }
				else { val = rs.getDouble(1); }

				rs.close();
				ps.close();
			} catch (SQLException e) 
			{ 
				System.err.println("database error attempting to read parameter key " +key);
			} catch (Exception e) {
				System.err.println("general error attempting to read parameter key " +key);
				e.printStackTrace();
			}

			return val;
		}
	}



	/**
	 * Reads a boolean parameter from the database
	 * @param key the name of the parameter
	 * @return the value saved in the database
	 */
	public boolean readBoolParam(String key) { return new Boolean(readParamBase(key, true).toString()); }




	/**
	 * Reads a real (double) parameter from the database
	 * @param key the name of the parameter
	 * @return the value saved in the database, or 0 on an error
	 */
	public double readRealParam(String key) 
	{ 
		try { return (Double) readParamBase(key, false); }
		catch(Exception e) { 
			System.err.println("Failed to parse Real for key \"" +key+ "\"");
			writeParam(key, 0);
		}
		return 0;
	}





	/**
	 * Reads a Integer parameter from the database
	 * @param key the name of the parameter
	 * @return the value saved in the database, or 0 on an error
	 */
	public int readIntegerParam(String key) 
	{ 
		try { return (Integer)readParamBase(key, false); }
		catch(Exception e) 
		{ 
			System.err.println("Failed to parse Integer for key \"" +key+ "\""); 
			writeParam(key, 0);
		}
		return 0;
	}





	/**
	 * Reads a String parameter from the database
	 * @param key the name of the parameter
	 * @return the value saved in the database, or 0 on an error
	 */
	public String readStringParam(String key) { return readParamBase(key, true).toString();}


	/**
	 * Writes the specified key value pair to the database
	 * @return true if the parameter was created, false otherwise 
	 */
	public void writeParam(String key, double val) { writeRawParam(key, "", val); }

	/**
	 * Writes the specified key value pair to the database
	 * @return true if the parameter was created, false otherwise 
	 */
	public void writeParam(String key, boolean val) { writeRawParam(key, String.valueOf(val), 0); }

	/**
	 * Writes the specified key value pair to the database
	 * @return true if the parameter was created, false otherwise 
	 */
	public void writeParam(String key, int val) { writeRawParam(key, "", val); }


	/**
	 * Writes the specified key value pair to the database
	 * @return true if the parameter was created, false otherwise 
	 */
	public void writeParam(String key, String val) { writeRawParam(key, val, 0); }
	
	
	
	/**
	 * Writes a raw parameter to the database
	 */
	private void writeRawParam(String key, String valS, double valN)
	{
		synchronized(this)
		{
			try {
				// Verify the database is ready for writing parameters
				verifyConnected();
				RCTables.paramTable.verifyExists(db);
				
				// format statement to make sure a fresh key value pair goes in the DB
				String sql = "REPLACE INTO " +RCTables.paramTable.getName()+ " VALUES (?, ?, ?);";
				
				PreparedStatement ps = db.prepareStatement(sql);
				
				// add parameters
				ps.setString(1, key);
				ps.setDouble(2, valN);
				ps.setString(3, valS);
				
				System.out.println(sql);
				
				execRaw(ps);
				ps.close();
			} catch (SQLException e) {
				System.err.println("SQL Error writing key!");
				System.err.println(e.getMessage());
//				e.printStackTrace();
			}
		}
	}





	/**
	 * Writes the ReqMode to the database. This will run in a background thread.
	 */
	public void writeRequestModeToDB(ReqMode mode) {
		Thread t = new Thread(() -> {
			try { writeParam("requestMode", mode.toString()); } 
			catch (Exception e) 
			{
				Platform.runLater(() -> { new Alert(AlertType.ERROR, "Failed to write parameter \"requestMode\" to database!").show(); });
				e.printStackTrace();
			}
		});
		t.setDaemon(true);
		t.setName("writeRequestMode");
		t.start();
	}



	/** Gets the current request mode */
	public ReqMode getRequestMode()
	{
		ReqMode r = ReqMode.CLOSED;
		String mode = readStringParam("requestMode");
		
		try {r = ReqMode.valueOf(mode); }
		catch(Exception e) {System.err.println("error reading request mode"); }
		
		return r;
	}





	/** Gets the size of the current queue */
	public int getQueueSize()
	{
		RCTables.forwardQueueTable.verifyExists(db);
		String sql = "SELECT COUNT(*) FROM " +RCTables.forwardQueueTable.getName();
		int size = 0;

		try {
			ResultSet rs = execRaw(sql);
			rs.next();
			size = rs.getInt(1);

			rs.close();
		} catch (SQLException e) {
			System.err.println("Error reading queue size");
			e.printStackTrace();
		}

		return size;
	}
	
	
	
	
	
	public void dumpTableToCSV(String path, DBTable table)
	{	
		try {
			// Setup files
			Files.deleteIfExists(new File(path).toPath());
			
			Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
			
			ArrayList<String> cols = new ArrayList<String>();
			ResultSet rs = execRaw("SELECT * FROM " +table.getName()+ " WHERE 1=1;");
			
			// Get column names
			int colCt = rs.getMetaData().getColumnCount();
			for(int i=0; i<colCt; i++)
			{
				cols.add(rs.getMetaData().getColumnLabel(i+1));
				w.write("\"" +cols.get(i)+"\"" +"\t");
			}
			w.write('\n');
			
			while(rs.next())
			{
				for(int j=0; j<colCt; j++)
					w.write("\"" +rs.getString(j+1)+ "\"\t");
				w.write('\n');
					
			}
			
			w.close();
			
			System.out.println("Finished Exporting table");
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}






	public Connection getDb() {
		return db;
	}





}









