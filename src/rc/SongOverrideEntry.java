package rc;

public class SongOverrideEntry 
{
	private Song song;
	private double cooldown;
	private double baseCost;
	
	
	
	/**
	 * Generates a new song entry without explicit datatbase requests
	 * @param song
	 * @param cooldown
	 * @param baseCost
	 */
	public SongOverrideEntry(Song song, double cooldown, double baseCost) {
		this.song = song;
		this.cooldown = cooldown;
		this.baseCost = baseCost;
	}
	
	
	

}
