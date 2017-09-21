package util;


/**
 * Describes the request mode status - 
 * either auto, on, or off
 * @author Duemmer
 *
 */
public enum ReqMode 
{
	AUTO("AUTO"),
	OPEN("OPEN"),
	CLOSED("CLOSED");
	
	private String mode;
	
	private ReqMode(String mode) { this.mode = mode; }
	public String mode() { return mode; }
	
	@Override
	public String toString() { return mode(); }


}
