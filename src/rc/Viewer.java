package rc;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import application.DysMain;
import db.DatabaseIO;
import db.RCTables;




/**
 * Represents a viewer, in terms of queueing. Has convenience methods for 
 * checking what songs they requested, framework supports
 * charging / giving rupees, but this is not supported yet as we are unable to
 * write to the fussbot database.
 * @author Duemmer
 *
 */
public class Viewer 
{
	/**
	 * Returns DysBot's credentials. Useful for dummy Viewers
	 */
	public static final Viewer dysbot = new Viewer("UCq4Zg02QNxH3jrLW-1tBYvw", "DysBot");
	
	/** RNGsus's credentials. 'Nuff said. */
	public static final Viewer RNGsus = new Viewer("UCOlo_l_Jmq6pUc1FIGYUimA", "RNGsus");
	
	
	private String username = "";
	private String userID = "";
	
	private int rupees = 0;
	
	
	public Viewer(String userID)
	{
		this.userID = userID;
		queryUser();
	}
	
	
	
	
	public Viewer(String userID, String username)
	{
		this.userID = userID;
		this.username  = username;
	}
	
	
	
	/**
	 * Queries the database for the user metrics given the userID
	 * @return
	 */
	public void queryUser()
	{
		RCTables.viewerTable.verifyExists(DysMain.remoteDB);
		ResultSet rs = null;
		
		
		try 
		{
			// create / execute SQL query
			PreparedStatement ps = DysMain.remoteDB.getDb().prepareStatement("SELECT username, coins FROM " +RCTables.viewerTable.getName()+ " WHERE channelID=?;");
			ps.setString(1, userID);
			rs = DysMain.remoteDB.execRaw(ps);
			
			if(rs == null) // bad statement
				System.err.println("bad statement in queryUsername()!");
			
			else if (!rs.next()) // check if there are any results AND prepare the result to be read
				System.err.println("ChannelID " +userID+ " did not find any matching entries");
			
			else // there is (at least) 1 matching username
			{
				username = rs.getString(1);
				rupees = rs.getInt(2);
			}
				
			rs.close();
		} catch (SQLException e) 
		{
			System.err.println("Exception encountered in queryUsername()");
			e.printStackTrace();
		}
	}



	public void setUserID(String userID) 
	{
		this.userID = userID;
		queryUser();
	}
	
	
	
	
	/**
	 * Gets all of the requests that this user has made
	 * @param maxMins maximum minutes to search backwards for, or -1 for everything
	 * @return
	 */
	public List<QueueEntry> getSongsPlayed(int maxMins)
	{
		// set cutoff to 0 if we don't want to set one
		long cutoff = 0;
		if(maxMins < 0)
			cutoff = System.currentTimeMillis() - ((long)maxMins) / 1000;
		
		List<QueueEntry> entries = new ArrayList<QueueEntry>();
		ResultSet res = null;
		
		try {
			RCTables.viewerTable.verifyExists(DysMain.remoteDB);
			PreparedStatement ps = DysMain.remoteDB.getDb().prepareStatement("SELECT " // get everything, but make sure we know the order
					+ "song_name, "
					+ "ost_name, "
					+ "length, "
					+ "franchise_name, "
					+ "time_played, "
					+ "rating_num, "
					+ "rating_pct "
					+ "WHERE user_id = ? and time_played > ?;");
			
			ps.setString(1, userID);
			ps.setLong(2, cutoff);
			res = DysMain.remoteDB.execRaw(ps);
			
			// iteratively extract the information, pulling parameters based on their order above
			while(res.next())
			{
				Song s = new Song(res.getString(1), res.getString(2), res.getInt(3), res.getString(4));
				Rating r = new Rating(res.getInt(6), res.getDouble(7));
				entries.add(new QueueEntry(this, res.getLong(5), r, s));
			}
			
			res.close();
		} catch (SQLException e) 
		{
			System.err.println("Exception encountered in getSongsPlayed()!");
			e.printStackTrace();
		}	
		
		return entries;
	}


	public String getUserID() { return userID; }
	public String getUsername() { return username; }
	public int getRupees() { return rupees; }

}
