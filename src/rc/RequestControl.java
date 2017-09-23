package rc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import application.DysMain;
import db.RCTables;
import foobarIO.Foobar;
import javafx.scene.control.ProgressBar;
import util.ReqMode;

/**
 * Serves as the primary control class of the system. This is the 
 * part that calcuates costs, manages the GUI, web interface, and foobar
 * @author Duemmer
 *
 */
public class RequestControl 
{
	
	private boolean queueOpen = false;
	private double percentFull = 0;
	private double queueLength;
	private int queueSize = 0;
	
	
	
	
	/**
	 * First try to pick the next one from queue. If that doesn't work, 
	 * let RNGsus take care of it. If that also doesn't work, do nothing.
	 * Errors will have already been logged.
	 */
	private Runnable addSong = ()->{
		QueueEntry song = getNextQueueEntry();
		
		if(song == null) // if null, queue is empty so have a random :)
			song = QueueEntry.uniformRandomEntry();
		
		if(song != null) { // if it's still null then RNGsus fucked up
			DysMain.foobar.addToPlaybackQueue(song);
			
			// add to the history queue, accounting for the few seconds early it gets added
			song.addToHistory((System.currentTimeMillis() / 1000L) + (long)Foobar.addSongTime);
			song.deleteFromForwardQueue();
		}
	};
	
	
	
	
	
	
	/**
	 * For now just starts the var calc service
	 */
	public RequestControl()
	{
		//DysMain.varCalcService.scheduleAtFixedRate(() -> { calcVars(); }, 1000, DysMain.varCalcUpdateMillis, TimeUnit.MILLISECONDS);
	}
	
	
	
	
	
	
	
	/**
	 * Gets the next song that should play from the forward queue, or null
	 * if the queue is empty
	 */
	private QueueEntry getNextQueueEntry()
	{
		QueueEntry qe = null;
		Song song;
		Rating rating;
		Viewer vw;
		
		String sql = "SELECT song_id, user_id, time_requested FROM " +RCTables.forwardQueueTable.getName()+ " WHERE priority=(SELECT MAX(priority) FROM " +RCTables.forwardQueueTable.getName()+ ")";
		try {
			RCTables.forwardQueueTable.verifyExists(DysMain.remoteDB);
			ResultSet rs = DysMain.remoteDB.execRaw(sql);
			
			if(rs.next()) { // The queue wasn't empty, so pick the first one from there
				song = new Song(rs.getString(1));
				rating = new Rating(song.getSongID());
				vw = new Viewer(rs.getString(2));
				
				qe = new QueueEntry(vw, rs.getLong("time_requested"), rating, song);
				
				rs.close();
			}
		} catch (SQLException e) 
		{
			System.err.println("Error adding song to queue");
			e.printStackTrace();
		}
		
		return qe;
	}
	
	
	
	
	/**
	 * Computes the sum of the lengths of each song in the queue, or 0 if the queue is empty
	 */
	public double calcQueueLength()
	{
		double length = 0;
		
		try {
			RCTables.forwardQueueTable.verifyExists(DysMain.remoteDB);
			String sql = "SELECT length FROM " +RCTables.forwardQueueTable.getName()+ " WHERE 1=1;";
			ResultSet rs = DysMain.remoteDB.execRaw(sql);
			
			while(rs.next())
				length += rs.getDouble(1);
			
			rs.close();
		} catch (Exception e) { System.err.println("Error getting queue length"); }
		
		return length + DysMain.foobar.getTotalQueueLength(); // the foobar queue isn't accounted for in the database, so add it on;
	}
	
	
	
	
	
	/**
	 * Calculates the values of all the variables in the "variables" tab of the UI, which get used throughout code
	 */
	private void calcVars()
	{
		queueLength = calcQueueLength(); 
		percentFull = ProgressBar.INDETERMINATE_PROGRESS; // default for indeterminate
		queueSize = DysMain.remoteDB.getQueueSize();
		
		double currSongLength = 0;
		if(DysMain.foobar.getCurrSong() != null)
			currSongLength = 0;//DysMain.foobar.getCurrSong().getLength();
		
		// If request mode is in manual, use that to determine the queue open state. If not, let the system calculate it on its own
		ReqMode reqMode = DysMain.remoteDB.getRequestMode();
		if(reqMode == ReqMode.OPEN)
			queueOpen = true;
		else if (reqMode == ReqMode.CLOSED) 
			queueOpen = false;
		else // leave queueOpen up to the system, but we can calculate percentFull, as the ratio between length and max length
			percentFull = queueLength / DysMain.remoteDB.readRealParam("queueCloseMins");
		
//		 Write that stuff to the database
		try {
			DysMain.remoteDB.writeParam("queueLength", queueLength);
			DysMain.remoteDB.writeParam("queueOpen", queueOpen);
			DysMain.remoteDB.writeParam("percentFull", percentFull);
			DysMain.remoteDB.writeParam("queueSize", queueSize);
			DysMain.remoteDB.writeParam("currSongRemaining", DysMain.foobar.getCurrSongRemaining());
			DysMain.remoteDB.writeParam("currSongLength", currSongLength);
		} catch (Exception e) { e.printStackTrace(); }
		
	}

	
	


	public Runnable getAddSong() { return addSong; }
}






