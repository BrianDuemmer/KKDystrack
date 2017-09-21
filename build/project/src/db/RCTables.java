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
							new DBCol("setting", DBType.TEXT, true, true, "", false),
							new DBCol("value1", DBType.INTEGER),
							new DBCol("value2", DBType.TEXT)
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
							new DBCol("username", DBType.TEXT, true, false, "someone", false),
							new DBCol("user_id", DBType.TEXT, true, false, "", false),
							new DBCol("time_requested", DBType.INTEGER, true, false, "", false),
							new DBCol("song_name", DBType.TEXT, true, false, "", false),
							new DBCol("ost_name", DBType.TEXT, true, false, "", false),
							new DBCol("franchise_name", DBType.TEXT),
							new DBCol("rating_pct", DBType.REAL),
							new DBCol("rating_num", DBType.INTEGER),
							new DBCol("length", DBType.REAL),
							new DBCol("song_id", DBType.TEXT, true, false, "", false),
							new DBCol("priority", DBType.INTEGER, true, false, "", false)
					}, 
					"time_requested"
			);




	/**
	 * This is where songs are sent and kept (semi) permanently after they are
	 * played out of the forward queue
	 */
	public static final DBTable queueHistoryTable= new DBTable(
			"queue_history", 
			new DBCol[] 
					{
						new DBCol("username", DBType.TEXT, true, false, "someone", false),
						new DBCol("user_id", DBType.TEXT, true, false, "", false),
						new DBCol("time_requested", DBType.INTEGER, true, false, "", false),
						new DBCol("song_name", DBType.TEXT, true, false, "", false),
						new DBCol("ost_name", DBType.TEXT, true, false, "", false),
						new DBCol("franchise_name", DBType.TEXT),
						new DBCol("rating_pct", DBType.REAL),
						new DBCol("rating_num", DBType.INTEGER),
						new DBCol("length", DBType.REAL),
						new DBCol("song_id", DBType.TEXT, true, false, "", false),
						new DBCol("priority", DBType.INTEGER, true, false, "", false),
						new DBCol("time_played", DBType.INTEGER, true, false, "", false)
					}, 
					"time_played"
			);




	/**
	 * This is where viewer information (like rupees, username, and userid) are stored
	 */
	public static final DBTable viewerTable= new DBTable(
			"viewers", 
			new DBCol[] 
					{
							new DBCol("username", DBType.TEXT),
							new DBCol("coins", DBType.INTEGER),
							new DBCol("joined_at", DBType.TEXT),
							new DBCol("minutes_watched", DBType.INTEGER),
							new DBCol("channelID", DBType.TEXT),
							new DBCol("last_checked", DBType.INTEGER),
							new DBCol("last_gambling", DBType.INTEGER),
							new DBCol("u_ts", DBType.TEXT),
							new DBCol("static_rank_name", DBType.TEXT)
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
							new DBCol("song_name", DBType.TEXT, true, false, "", false),
							new DBCol("ost_name", DBType.TEXT, true, false, "", false),
							new DBCol("base_cost", DBType.REAL, true, false, "", false),
							new DBCol("history_expire_mins", DBType.REAL, true, false, "", false),
							new DBCol("immediate_replay_scl", DBType.REAL, true, false, "", false)
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
							new DBCol("song_name", DBType.TEXT, true, false, "", false),
							new DBCol("ost_name", DBType.TEXT, true, false, "", false),
							new DBCol("song_length", DBType.REAL, true, false, "", false),
							new DBCol("rating_pct", DBType.REAL, true, false, "", false),
							new DBCol("rating_num", DBType.INTEGER, true, false, "", false),
							new DBCol("song_id", DBType.TEXT, true, true, "", false)
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
							new DBCol("username", DBType.TEXT),
							new DBCol("channelID", DBType.TEXT, true, true, "", false),
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
							new DBCol("username", DBType.TEXT),
							new DBCol("channelID", DBType.TEXT, true, true, "", false),
							new DBCol("cost_scalar", DBType.REAL),
							new DBCol("note", DBType.TEXT)
					}, 
					""
			);

}
