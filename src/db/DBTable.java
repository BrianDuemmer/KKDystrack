package db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents a table in an SQLite database
 * @author Duemmer
 *
 */
public class DBTable 
{
	private String primaryKey = "";
	private DBCol[] cols;
	private String name;

	public DBTable(String name, DBCol[] cols, String primaryKey) 
	{
		this.name  =name;
		this.cols = cols;
		this.primaryKey = primaryKey;
	}
	
	
	/**
	 * Checks to make sure the table exists in the supplied database. If it does,
	 * this does nothing. If not, the table is created
	 * @param remoteDB the database. If the connection is invalid, print a warning and break
	 */
	public void verifyExists(DatabaseIO db)
	{
		String sql = "";
		
		// We don't want to create any columns without tables, so make sure to break if there aren't any
		if(cols.length <1)
		{
			System.err.println("Cannot create table " +name+ " because it has no columns!");
			return;
		}
		
		try 
		{
			db.verifyConnected();
			if(db == null || !db.getDb().isValid(3)) // break if invalid or null
			{
				System.err.println("Invalid database at verifyTable");
				return;
			}
			
			sql = "CREATE TABLE IF NOT EXISTS " +name+ " (";
			sql += cols[0]; // add the first one seperately without the comma
			for(int i=1; i<this.cols.length; i++)
				sql += ", " +cols[i];
			
			if(!primaryKey.trim().isEmpty()) //if the primary key isn't null, empty, or whitespace, add the primary key flag
				sql += ", PRIMARY KEY('" +primaryKey+ "')";
			
			sql +=");"; // cap off the statement, and execute
			db.execRaw(sql);
			
		} catch (SQLException e) 
		{
			System.err.println("Failed to verify table existance with SQL " +sql);
			e.printStackTrace();
		}
	}


	public String getPrimaryKey() { return primaryKey; }


	public void setPrimaryKey(String primaryKey) 
	{
		this.primaryKey = primaryKey;
		if(primaryKey.trim().isEmpty())
			System.err.println("Invalid primary key set in DBTable!");
	}
	
	
	
	
	public void dropIfExist(DatabaseIO db)
	{
		String sql = "DROP TABLE ?;";
		try {
			PreparedStatement ps = db.getDb().prepareStatement(sql);
			ps.setString(1, getName());
			db.execRaw(ps);
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public DBCol[] getCols() { return cols; }
	public void setCols(DBCol[] cols) { this.cols = cols; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

}
