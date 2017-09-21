package rc;

import java.util.ArrayList;
import java.util.List;

public class BlacklistedViewer extends Viewer 
{
	private String note;
	private long timeBanned;

	public BlacklistedViewer(String userID) { super(userID); }
	
	
	
	
//	public static BlacklistedViewer getAllBlacklistedUsers()
//	{
//		List<BlacklistedViewer> vws = new ArrayList<BlacklistedViewer>();
//	}
	
	
	
	

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public long getTimeBanned() {
		return timeBanned;
	}

	public void setTimeBanned(long timeBanned) {
		this.timeBanned = timeBanned;
	}
}
