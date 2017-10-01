package db;


/**
 * Contains all of the table layouts that we use. 
 * Serves as a reference for the verifyTableExists() method
 * @author Duemmer
 *
 */
public class RCTables 
{

	/**
	 * The general settings / parameters are stored on this table
	 * as key - value pairs
	 */
	public static final DBTable paramTable= new DBTable(
			"general_settings", 
			new DBCol[] 
					{
							new DBCol("setting", DBType.VARCHAR, true, true, "", false, 100),
							new DBCol("num_val", DBType.REAL),
							new DBCol("str_val", DBType.VARCHAR, false, false, "", false, 500)
					}, 
					""
			);




	/**
	 * This is where the active queue of songs is stored
	 */
	public static final DBTable forwardQueueTable= new DBTable(
			"forward_queue", 
			new DBCol[] 
					{
							new DBCol("user_id", DBType.VARCHAR, true, false, "", false, 30),
							new DBCol("time_requested", DBType.DATETIME, true, false, "", false, -1),
							new DBCol("song_id", DBType.VARCHAR, true, false, "", false, 256),
							new DBCol("priority", DBType.INTEGER, true, false, "", false, -1),
					}, 
					""
			);




	/**
	 * This is where songs are sent and kept (semi) permanently after they are
	 * played out of the forward queue
	 */
	public static final DBTable queueHistoryTable= new DBTable(
			"queue_history", 
			new DBCol[] 
					{
							new DBCol("user_id", DBType.VARCHAR, true, false, "", false, 30),
							new DBCol("time_requested", DBType.DATETIME, true, false, "", false, -1),
							new DBCol("song_id", DBType.VARCHAR, true, false, "", false, 256),
							new DBCol("priority", DBType.INTEGER, true, false, "", false, -1),
							new DBCol("time_played", DBType.DATETIME, true, false, "", false, -1),
							new DBCol("play_id", DBType.BIGINT, true, true, "", true, -1)
					}, 
					"play_id"
			);




	/**
	 * This is where viewer information (like rupees, username, and userid) are stored
	 */
	public static final DBTable viewerTable= new DBTable(
			"viewers", 
			new DBCol[] 
					{
							new DBCol("username", DBType.VARCHAR, true, false, "", false, 30),
							new DBCol("user_id", DBType.VARCHAR, true, false, "", false, 30),
							new DBCol("rupees", DBType.INTEGER, true, false, "", false, 256),
							new DBCol("favorite_song", DBType.VARCHAR, false, false, "", false, 256),
							new DBCol("is_admin", DBType.BOOLEAN),
							new DBCol("is_blacklisted", DBType.BOOLEAN),
							new DBCol("rupee_discount", DBType.REAL),
							new DBCol("free_requests", DBType.INTEGER),
							new DBCol("login_bonus_count", DBType.INTEGER),
							new DBCol("watchtime_rank", DBType.VARCHAR, false, false, "", false, 50),
							new DBCol("static_rank", DBType.VARCHAR, false, false, "", false, 50),
							new DBCol("birthday", DBType.DATETIME),
							new DBCol("last_birthday_withdraw", DBType.DATETIME),
							new DBCol("song_on_hold", DBType.VARCHAR, false, false, "", false, 256)
					}, 
					""
			);






	/**
	 * Contains override costs / parameters for certain songs
	 */
	public static final DBTable songOverrideTable= new DBTable(
			"song_overrides", 
			new DBCol[] 
					{
							new DBCol("song_id", DBType.VARCHAR, true, false, "", false, 256),
							new DBCol("base_cost", DBType.REAL, true, false, "", false, -1),
							new DBCol("base_cooldown", DBType.REAL, true, false, "", false, -1)
					}, 
					""
			);




	/**
	 * Contains the entire playlist of songs
	 */
	public static final DBTable playlistTable= new DBTable(
			"playlist", 
			new DBCol[] 
					{
							new DBCol("song_name", DBType.VARCHAR, true, false, "", false, 300),
							new DBCol("ost_name", DBType.VARCHAR, true, false, "", false, 100),
							new DBCol("song_id", DBType.VARCHAR, true, true, "", false, 256),
							new DBCol("song_length", DBType.REAL, true, false, "-1", false, -1),
							new DBCol("song_franchise", DBType.VARCHAR, true, false, "", false, 100),
							new DBCol("song_alias", DBType.VARCHAR, true, false, "", false, 100)
					}, 
					""
			);





	/**
	 * Contains blacklisted users whom we will punish for being terrible >:-D
	 */
	public static final DBTable userBlacklistTable= new DBTable(
			"user_blacklist", 
			new DBCol[] 
					{
							new DBCol("user_id", DBType.VARCHAR, true, true, "", false, 30),
							new DBCol("time_banned", DBType.INTEGER),
							new DBCol("note", DBType.TEXT)
					}, 
					""
			);





	/**
	 * Contains viewers who are important enough to recieve discounts, lower cooldowns, etc.
	 * Probably best to not let this get public
	 */
	public static final DBTable vipUserTable= new DBTable(
			"vip_users", 
			new DBCol[] 
					{
							new DBCol("user_id", DBType.VARCHAR, true, true, "", false, 30),
							new DBCol("cost_scalar", DBType.REAL),
							new DBCol("note", DBType.TEXT)
					}, 
					""
			);
	
	
	
	/**
	 * Makes sure each and every table exists in the database. Only for testing
	 */
	public static void verifyAll(DatabaseIO db)
	{
		paramTable.verifyExists(db);
		forwardQueueTable.verifyExists(db);
		queueHistoryTable.verifyExists(db);
		viewerTable.verifyExists(db);
		songOverrideTable.verifyExists(db);
		playlistTable.verifyExists(db);
		userBlacklistTable.verifyExists(db);
		vipUserTable.verifyExists(db);
	}

}
