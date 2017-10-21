package rc.viewer;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
	// All info fields
	private String username = "";
	private String userID = "";
	private int rupees = 0;
	private String favSong;
	private boolean isAdmin;
	private boolean isBlacklisted;
	private double rupeeDiscount;
	private int freeRequests;
	private int loginBonusCount;
	private String watchtimeRank;
	private String staticRank;
	private Date birthday;
	private Date lastBdayWithdraw;
	private String songOnHold;
	
	
	/**
	 * Creates a new viewer using fields directly. Intentionally package private.
	 * @param username
	 * @param userID
	 * @param rupees
	 * @param favSong
	 * @param isAdmin
	 * @param isBlacklisted
	 * @param rupeeDiscount
	 * @param freeRequests
	 * @param loginBonusCount
	 * @param watchtimeRank
	 * @param staticRank
	 * @param birthday
	 * @param lastBdayWithdraw
	 * @param songOnHold
	 */
	Viewer(String username, String userID, int rupees, String favSong, boolean isAdmin, boolean isBlacklisted,
			double rupeeDiscount, int freeRequests, int loginBonusCount, String watchtimeRank, String staticRank,
			Date birthday, Date lastBdayWithdraw, String songOnHold) {
		this.username = username;
		this.userID = userID;
		this.rupees = rupees;
		this.favSong = favSong;
		this.isAdmin = isAdmin;
		this.isBlacklisted = isBlacklisted;
		this.rupeeDiscount = rupeeDiscount;
		this.freeRequests = freeRequests;
		this.loginBonusCount = loginBonusCount;
		this.watchtimeRank = watchtimeRank;
		this.staticRank = staticRank;
		this.birthday = birthday;
		this.lastBdayWithdraw = lastBdayWithdraw;
		this.songOnHold = songOnHold;
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



	public String getUserID() { return userID; }
	public String getUsername() { return username; }
	public int getRupees() { return rupees; }




	public String getFavSong() {
		return favSong;
	}




	public boolean isAdmin() {
		return isAdmin;
	}




	public boolean isBlacklisted() {
		return isBlacklisted;
	}




	public double getRupeeDiscount() {
		return rupeeDiscount;
	}




	public int getFreeRequests() {
		return freeRequests;
	}




	public int getLoginBonusCount() {
		return loginBonusCount;
	}




	public String getWatchtimeRank() {
		return watchtimeRank;
	}




	public String getStaticRank() {
		return staticRank;
	}




	public Date getBirthday() {
		return birthday;
	}




	public Date getLastBdayWithdraw() {
		return lastBdayWithdraw;
	}




	public String getSongOnHold() {
		return songOnHold;
	}

}
