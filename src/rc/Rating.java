package rc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import application.DysMain;
import db.RCTables;

public class Rating 
{
	private int num = 0;
	private double pct = 0;
	
	
	public Rating(int num, double pct)
	{
		this.num = num;
		this.pct = pct;
	}
	
	
	
	
	public Rating(String songID)
	{
		try {
			RCTables.playlistTable.verifyExists(DysMain.remoteDB);
			PreparedStatement ps = DysMain.remoteDB.getDb().prepareStatement("SELECT rating_pct, rating_num FROM " +RCTables.playlistTable.getName()+ " WHERE song_id=?;");
			ps.setString(1, songID);
			
			ResultSet rs = DysMain.remoteDB.execRaw(ps);
			
			if(rs.next())
			{
				this.pct = rs.getDouble(1);
				this.num = rs.getInt(2);
			} else {System.err.println("No rating info found for songID "+songID); }
			
			rs.close();
		} catch (SQLException e) 
		{
			System.err.println("Failed to extract rating information!");
			e.printStackTrace();
		}
	}
	
	
	@Override
	public String toString()
	{
		if(num > 1)
			return String.format("%.1f/5, %d votes", pct*5, num);
		return "No votes";
	}


	/**
	 * @return the pct
	 */
	public double getPct() {
		return pct;
	}


	/**
	 * @param pct the pct to set
	 */
	public void setPct(double pct) {
		this.pct = pct;
	}


	/**
	 * @return the num
	 */
	public int getNum() {
		return num;
	}


	/**
	 * @param num the num to set
	 */
	public void setNum(int num) {
		this.num = num;
	}

}
