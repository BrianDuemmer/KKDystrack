package rc.queue;

import db.DatabaseIO;

public abstract class SongFactory 
{
	
	/**
	 * TODO implement the corresponding PHP scripts<br/><br/>
	 * 
	 * Queries the playlist table for this song in the provided database
	 * @return a {@link Song} corresponding to the entry in the playlist table, or null if
	 * it wasn't found
	 */
	public static Song fromPlaylist(String sondID) {
		return null;
	}
}
