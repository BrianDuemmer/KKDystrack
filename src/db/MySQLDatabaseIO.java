package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDatabaseIO extends DatabaseIO 
{
	protected String hostname;
	protected String user;
	protected String pass;
	protected String dbName;
	protected int port;
	
	
	public MySQLDatabaseIO(String hostname, String user, String pass, String dbName, int port, String argString) 
	{
		super("jdbc:mysql://" +hostname+ ":" +port+ "/" +dbName+ argString);
		
		this.hostname = hostname;
		this.user = user;
		this.pass = pass;
		this.dbName = dbName;
		this.port = port;
	}

	@Override
	protected Connection connect(String url) throws SQLException {
		return DriverManager.getConnection(url, user, pass);
	}

}
