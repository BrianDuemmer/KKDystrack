package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabaseIO extends DatabaseIO {

	public SQLiteDatabaseIO(String path) {
		super("jdbc:sqlite:" +path);
	}

	@Override
	protected Connection connect(String url) throws SQLException {
		return DriverManager.getConnection(url);
	}

}
