package rc.queue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

import db.DBTable;
import db.DatabaseIO;
import db.RCTables;

/**
 * @author Duemmer
 *
 */
public class SongQueue implements Queue<QueueEntry2>
{

	private String name;
	private DBTable tableModel = RCTables.forwardQueueTable; // represents the table structure in the database
	private DatabaseIO db;
	private ArrayList<QueueEntry2> entries = new ArrayList<>();
	


	/**
	 * Creates a new song queue that is bound to a database table. 
	 * @param name the name of this queue. The table this is linked to follows the format "queue_${name}"
	 * @param db the database to link to
	 * @param read if true, it will try to read the queue from the database
	 */
	public SongQueue(String name, DatabaseIO db, boolean read) {
		this.name = name;
		this.db = db;
		tableModel.setName("queue_" +name); 
		
		if(read)
			readAll(true);
	}
	
	
	
	
	/**
	 * Appends this element to the back of the queue
	 */
	@Override public boolean add(QueueEntry2 e) {		
		try {
		PreparedStatement ps = prepInsertSt();
		e.prepareIns(ps);
		ps.executeUpdate();
		ps.close();
		
		entries.add(e); // only add if things went well
		} catch(Exception err) {
			System.err.println("Failed to append queueEntry to the database");
			err.printStackTrace();
		}
		
		return true;
	}
	
	
	
	
	
	
	@Override public boolean offer(QueueEntry2 e) {
		// We don't have any specific length requirements, so just call add()
		add(e);
		return true;
	}



	@Override public QueueEntry2 remove() {
		return null;
	}



	@Override public QueueEntry2 poll() {
		return null;
	}



	@Override
	public QueueEntry2 element() {
		return null;
	}



	@Override
	public QueueEntry2 peek() {
		return null;
	}
	
	
	
	
	
	
	
	

	/**
	 * Writes the contents of the queue to a table in the database specified by
	 * <code>name</code>. This will flush the table first.
	 */
	public void writeAllToDB()
	{
		try {
			//verify that it is empty and exists
			clearDB(false);

			//add all the inserts to one batch insert
			PreparedStatement ps = prepInsertSt();
			for(QueueEntry2 entry : this) {
				entry.prepareIns(ps);
				ps.addBatch();
			}
			ps.executeBatch();
			ps.close();
			
		} catch (SQLException e) {
			System.err.println("Failed to push queue \"" +name+ "\" to database");
			e.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * Gets the entire contents of the queue
	 * @param fromDB if true, this will pull from the database, update itself, and then return. If 
	 * {@code false}, it just returns the in-memory list.
	 */
	public ArrayList<QueueEntry2> readAll(boolean fromDB)
	{
		if(fromDB) {
			String sql = "SELECT user_id, time_requested, song_id, priority FROM " +tableModel.getName();
			try {
				ResultSet rs = db.execRaw(sql);
				entries.clear(); // make a fresh list
				
				while(!rs.next())
					entries.add(new QueueEntry2(rs));
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return entries;
	}
	




	/**
	 * Clears the database portion of the song queue, optionally
	 * dropping the table. if <code>removeTable</code> is false, the
	 * table is guaranteed to exist and be empty after this runs.
	 */
	public void clearDB(boolean removeTable) throws SQLException
	{
			if(removeTable)
				tableModel.dropIfExist(db);
			else
				tableModel.verifyEmpty(db);
	}
	
	
	
	
	/**
	 * Convenience method to format a prepared insert statement for an entry in the queue
	 */
	private PreparedStatement prepInsertSt() throws SQLException {
		String sql = "INSERT INTO " +tableModel.getName()+ 
				" user_id, time_requested, song_id, priority VALUES(?, ?, ?, ?)";

		PreparedStatement ps = db.getDb().prepareStatement(sql);
		return ps;
	}




	@Override
	public int size() {
		return 0;
	}




	@Override
	public boolean isEmpty() {
		return false;
	}




	@Override
	public boolean contains(Object o) {
		return false;
	}




	@Override
	public Iterator<QueueEntry2> iterator() {
		return null;
	}




	@Override
	public Object[] toArray() {
		return null;
	}




	@Override
	public <T> T[] toArray(T[] a) {
		return null;
	}




	@Override
	public boolean remove(Object o) {
		return false;
	}




	@Override
	public boolean containsAll(Collection<?> c) {
		return false;
	}




	@Override
	public boolean addAll(Collection<? extends QueueEntry2> c) {
		return false;
	}




	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}




	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}




	@Override
	public void clear() {
	}

}
















