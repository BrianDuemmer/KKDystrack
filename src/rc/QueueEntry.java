package rc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.DysMain;
import db.RCTables;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import rc.queue.Song;
import rc.viewer.Viewer;
import rc.viewer.ViewerFactory;

/**
 * Represents a single song in the forward queue
 * @author Duemmer
 *
 *@deprecated
 */
public class QueueEntry
{
	// User metrics
	private Viewer vw;
	private long time;
	private Song song;
	private int priority;

	private boolean pending = false; //indicates whether this request has played yet



	public QueueEntry(Viewer vw, long time, Song song) 
	{
		this.vw = vw;
		this.time  = time;
		this.song = song;
	}



	/**
	 * Reads the entire active queue from the database
	 */
	public static ObservableList<QueueEntry> getForwardQueue()
	{
		List<QueueEntry> entries = new ArrayList<QueueEntry>();

		try 
		{
			// downsync from the remote database
			DysMain.remoteDB.upSync(DysMain.localDB, RCTables.forwardQueueTable, true);
			
			// just dump the whole table
			ResultSet rs = DysMain.localDB.execRaw("SELECT * FROM " +RCTables.forwardQueueTable.getName()+ ";");

			while(rs.next()) // read each table entry seperately and dump it into the QueueEntry
			{
				String user_id = rs.getString("user_id");
				String song_id = rs.getString("song_id");
				long time_requested = rs.getLong("time_requested");
				int priority = rs.getInt("priority");

				// format into QE
//				QueueEntry e = new QueueEntry(
//						new Viewer(user_id, username), 
//						time_requested, 
//						new Rating(rating_num, rating_pct), 
//						new Song(song_name, ost_name, length, franchise_name)
//						);
//
//				e.setPriority(priority);
//
//				entries.add(e);
			}

			rs.close();
		} catch (Exception e) 
		{
			System.err.println("Error encountered trying to read queue from database!");
			e.printStackTrace();
		}

		return FXCollections.observableArrayList(entries);
	}



	/**
	 * Writes this queueEntry to the forward queue in the database
	 */
	public void writeToDB()
	{
		Task<Void> tsk= new Task<Void>() {

			@Override
			protected Void call() throws Exception 
			{
				// setup insert / field names
				String sql = "INSERT INTO " +RCTables.forwardQueueTable.getName()+ " ("
						+ "user_id, "
						+ "time_requested, "
						+ "priority, "
						+ "song_id) ";

				// Add values clause, build prepared statement
				sql += "VALUES (?, ?, ?, ?)";

				try 
				{
					RCTables.forwardQueueTable.verifyExists(DysMain.remoteDB);
					PreparedStatement ps = DysMain.remoteDB.getDb().prepareStatement(sql);

					// Add each value
					ps.setString(1, vw.getUserID());
					ps.setLong(2, time);
					ps.setInt(3, priority);
					ps.setString(4, song.getSongID());


					DysMain.remoteDB.execRaw(ps); // write the statement down
					ps.close();
					System.out.println("Wrote song \"" +song.getOstName()+ " - " +song.getSongName()+ "\" to the queue");
				} catch (SQLException e) 
				{
					System.err.println("Exception encountered trying to write QueueEntry to database");
					e.printStackTrace();
				}

				return null;
			}};

			Thread t = new Thread(tsk);
			t.setDaemon(true);
			t.setName("writeQueueEntryToDB");
			t.start();
	}




	/**
	 * Takes an SRS of 1 song as a random song
	 */
	public static QueueEntry uniformRandomEntry()
	{
		Song song = Song.getUniformRandom();

		QueueEntry q = new QueueEntry(ViewerFactory.RNGsus, System.currentTimeMillis() / 1000L, song);
		return q;
	}




	/**
	 * Adds this {@link QueueEntry} to the history queue
	 * @param time the time this song was played, for recording purposes
	 */
	public void addToHistory(long time)
	{
		try {

			/** 
			 * This block of absolutely useless code reads this object's exact parameters
			 * form the database instead of using, y'know, IT'S OWN VALUES?!
			 * 
			 * This is why you don't program at 3 in the morning
			 */
			//			String sqlSel = "SELECT * FROM " +RCTables.forwardQueueTable.getName()+ " WHERE song_id=? AND time_requested=? AND user_ID =?;";
			//			
			//			PreparedStatement psSel = DysMain.remoteDB.getDb().prepareStatement(sqlSel);
			//			psSel.setString(1, song.getSongID());
			//			psSel.setLong(2, this.time);
			//			psSel.setString(3, vw.getUserID());
			//			
			//			ResultSet rsSel = DysMain.remoteDB.execRaw(psSel);
			//			
			//			// prepare the resultset
			//			rsSel.next();
			//			
			//			String username = rsSel.getString("username");
			//			String user_id = rsSel.getString("user_id");
			//			long time_requested = rsSel.getLong("time_requested");
			//			String song_name = rsSel.getString("song_name");
			//			String ost_name = rsSel.getString("ost_name");
			//			String franchise_name = rsSel.getString("franchise_name");
			//			double rating_pct = rsSel.getDouble("rating_pct");
			//			int rating_num = rsSel.getInt("rating_num");
			//			double length = rsSel.getDouble("length");
			//			String song_id = rsSel.getString("song_id");
			//			int priority = rsSel.getInt("priority");


			// Now format the add
			RCTables.queueHistoryTable.verifyExists(DysMain.remoteDB);
			String sqlIns = "INSERT INTO " +RCTables.queueHistoryTable.getName()+ " ("
					+ "username, "
					+ "user_id, "
					+ "time_requested, "
					+ "song_name, "
					+ "ost_name, "
					+ "franchise_name, "
					+ "rating_pct, "
					+ "rating_num, "
					+ "length, "
					+ "song_id, "
					+ "priority, "
					+ "time_played)"
					+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 

			// Add all the parameters, and run
			PreparedStatement psIns = DysMain.remoteDB.getDb().prepareStatement(sqlIns);

			psIns.setString(1, vw.getUsername());
			psIns.setString(2, vw.getUserID());
			psIns.setLong(3, this.time);
			psIns.setString(4, song.getSongName());
			psIns.setString(5, song.getOstName());
			psIns.setString(6, song.getFranchiseName());
//			psIns.setDouble(7, rating.getPct());
//			psIns.setInt(8, rating.getNum());
			psIns.setDouble(9, song.getLength());
			psIns.setString(10, song.getSongID());
			psIns.setInt(11, this.priority);
			psIns.setLong(12, time);

			DysMain.remoteDB.execRaw(psIns);

		} catch (SQLException e) {
			System.err.println("Error adding song to history queue");
			e.printStackTrace();
		}
	}




	/**
	 * Deletes the forward queue entry corresponding to this {@link QueueEntry}. If the entry doesn't exist
	 * (tis is a random song or something) it does nothing.
	 */
	public void deleteFromForwardQueue()
	{
		try {
			RCTables.forwardQueueTable.verifyExists(DysMain.remoteDB);
			String sql = "DELETE FROM " +RCTables.forwardQueueTable.getName()+ " WHERE song_id=? AND time_requested=? AND user_ID =?;";

			PreparedStatement ps = DysMain.remoteDB.getDb().prepareStatement(sql);
			ps.setString(1, song.getSongID());
			ps.setLong(2, this.time);
			ps.setString(3, vw.getUserID());

			DysMain.remoteDB.execRaw(ps);
		} catch(SQLException e) {
			System.err.println("Failed to delete queueEntry from queue");
			e.printStackTrace();
		}
	}





	public Viewer getVw() {
		return vw;
	}



	public void setVw(Viewer vw) {
		this.vw = vw;
	}



	public long getTime() {
		return time;
	}



	public void setTime(long time) {
		this.time = time;
	}



	public Rating getRating() {
		return /*rating;*/null;
	}



	public void setRating(Rating rating) {
		//this.rating = rating;
	}



	public Song getSong() {
		return song;
	}



	public void setSong(Song song) {
		this.song = song;
	}



	public boolean isPending() {
		return pending;
	}



	public void setPending(boolean pending) {
		this.pending = pending;
	}



	public Object getDate() 
	{
		return null;
	}



	public int getPriority() {
		return priority;
	}



	public void setPriority(int priority) {
		this.priority = priority;
	}

}
