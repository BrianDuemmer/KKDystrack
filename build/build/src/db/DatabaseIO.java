package db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.sqlite.SQLiteErrorCode;

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
public class DatabaseIO 
{

	// maximum number of retries when encountering sqlite_busy
	private int busyRetriesMax = 3;






	private Connection db; // reference to the database we are connected to
	private String url; // address of the database
	private boolean connected = false; // specifies if we currently have a valid connection to the database

	/**
	 * @param dbPath the path on the filesystem of the database file
	 */
	public DatabaseIO(String dbPath)
	{
		this.url = "jdbc:sqlite:" +dbPath;
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
			db = DriverManager.getConnection(url);
			connected = db.isValid(3);

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
	private String readParameter(String key)
	{	
		synchronized(this)
		{
			String val = ""; // Will return empty string on failure

			// Use a prepared Statement for added security
			RCTables.paramTable.verifyExists(db);
			String sql = "SELECT value2 FROM " +RCTables.paramTable.getName()+ " WHERE setting = ?";
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
					System.err.println("WARNING: Could not find parameter key " +key+ ". Creating key with value \"0\"...");
					setParameter(key, "0");
					//Thread.dumpStack();
				} 
				// If this is hit then there is at least 1 result
				else { val = rs.getString(1); }

				rs.close();
				ps.close();
			} catch (SQLException e) 
			{ 
				System.err.println("ERROR attempting to read parameter key " +key);
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
	public boolean readBoolParam(String key) { return new Boolean(readParameter(key)); }




	/**
	 * Reads a real (double) parameter from the database
	 * @param key the name of the parameter
	 * @return the value saved in the database, or 0 on an error
	 */
	public double readRealParam(String key) 
	{ 
		try { return new Double(readParameter(key)); }
		catch(Exception e) { 
			System.err.println("Failed to parse Real for key \"" +key+ "\"");
			try {
				setParameter(key, 0);
			} catch (SQLException e1) {
				System.err.println("Failed to write default integer parameter");
				e1.printStackTrace();
			}
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
		try { return new Integer(readParameter(key)); }
		catch(Exception e) 
		{ 
			System.err.println("Failed to parse Integer for key \"" +key+ "\""); 
			try {
				setParameter(key, 0);
			} catch (SQLException e1) {
				System.err.println("Failed to write default integer parameter");
				e1.printStackTrace();
			}
		}
		return 0;
	}





	/**
	 * Reads a String parameter from the database
	 * @param key the name of the parameter
	 * @return the value saved in the database, or 0 on an error
	 */
	public String readStringParam(String key) { return readParameter(key);}


	/** Writes a real parameter to the database 
	 * @throws SQLException */
	public boolean setParameter(String key, double val) throws SQLException { return setParameter(key, String.valueOf(val)); }

	/** Writes a boolean parameter to the database 
	 * @throws SQLException */
	public boolean setParameter(String key, boolean val) throws SQLException { return setParameter(key, String.valueOf(val)); }

	/** Writes an integer parameter to the database 
	 * @throws SQLException */
	public boolean setParameter(String key, int val) throws SQLException { return setParameter(key, String.valueOf(val)); }


	/**
	 * Writes the specified key value pair to the database
	 * @return true if the parameter was created, false otherwise
	 * @throws SQLException 
	 */
	public boolean setParameter(String key, String value) throws SQLException
	{
		synchronized(this)
		{
			RCTables.paramTable.verifyExists(db);
			String sql1 = "DELETE FROM " +RCTables.paramTable.getName()+ " WHERE setting=?";
			String sql2 = "INSERT OR REPLACE INTO " +RCTables.paramTable.getName()+ " VALUES( ?, ?, ?)";

			try
			{
				//verifyTableExists(paramTable, paramTableCols);
				verifyConnected();

				PreparedStatement ps1 = db.prepareStatement(sql1);
				ps1.setString(1, key);
				execRaw(ps1);

				PreparedStatement ps2 = db.prepareStatement(sql2);
				ps2.setString(1, key);
				ps2.setLong(2, 0);
				ps2.setString(3, value);
				execRaw(ps2);
				
				ps1.close();
				ps2.close();

				// if we reached this point, then the parameter was created and the values should be in the table
				return true;
			} 	
			catch (SQLException e) // if this exception is thrown, that means the key already exists, so update instead
			{ 
				// if there's a timeout exception, let it propagate further to prevent locking up the database routines
				if(e.getErrorCode() == SQLiteErrorCode.SQLITE_BUSY.code)
					throw e;

				else
				{
					System.err.println("Exception encountered in SetParameter()!");
					e.printStackTrace();
				}
			} 

			// If it didn't hit the above return statement, then the key wasn't newly created
			return false;
		}
	}





	/**
	 * Writes the ReqMode to the database. This will run in a background thread.
	 */
	public void writeRequestModeToDB(ReqMode mode) {
		Thread t = new Thread(() -> {
			try { setParameter("requestMode", mode.toString()); } 
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
		String mode = readStringParam("requestMode");
		ReqMode r = ReqMode.valueOf(mode);
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









