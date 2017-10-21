package rc.queue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import rc.viewer.Viewer;
import rc.viewer.ViewerFactory;

public class QueueEntry2 
{
	private Song song;
	private Viewer vw;
	private int priority;
	private Date timeRequested;

	
	
	
	public QueueEntry2(Song song, Viewer vw, int priority, Date timeRequested) 
	{
		super();
		this.song = song;
		this.vw = vw;
		this.priority = priority;
		this.timeRequested = timeRequested;
	}
	
	
	
	/**
	 * Produces a queueEntry based on the results from a SELECT 
	 * query on a queue table. It then in turn perfoms 
	 * @param rs the results of a select statement on a database. NOTE: this will not
	 * advance the resultSet, and it assumes it points to a valid entry
	 */
	public QueueEntry2(ResultSet rs) throws SQLException
	{
		this.song = SongFactory.fromPlaylist(rs.getString("song_id"));
		this.vw = ViewerFactory.newViewer(rs.getString("user_id"));
		this.priority = rs.getInt("priority");
		this.timeRequested = new Date(rs.getTimestamp("time_requested").getTime());
	}
	
	
	



	/**
	 * <p>Adds all of the parameters to the prepared statement, but doesn't call 
	 * {@link PreparedStatement#addBatch() addBatch()} or 
	 * {@link PreparedStatement#execute() execute()} </p>
	 * 
	 * Adds parameters in the following order:
	 * <ol>
	 * <li>user_id</li>
	 * <li>time_requested</li>
	 * <li>song_id</li>
	 * <li>priority</li>
	 * </ol>
	 * 
	 * @return the same preparedStatement, but with the parameters added
	 * @throws SQLException 
	 */
	public PreparedStatement prepareIns(PreparedStatement ps) throws SQLException
	{
		ps.setString(1, vw.getUserID());
		ps.setTimestamp(2, new Timestamp(timeRequested.getTime()));
		ps.setString(3, song.getSongID());
		ps.setInt(4, priority);

		return ps;
	}
}









