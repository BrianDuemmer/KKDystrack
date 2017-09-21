package util;

import java.io.File;

public class SongUtils 
{
	/** if a potential song file does not have these extensions, discount it automatically */
	public static final String[] legalExtensions =
		{
				"mp3",
				"flac",
				"m4a",
				"wma",
				"ogg",
				"wav"
		};
	
	
	
	/**
	 * checks if the extension on the song file is a legal sound
	 * file extension. These are listed in {@link SongUtils#legalExtensions}
	 */
	public static boolean isLegalExtension(File song)
	{
		String ext = song.getName().substring(song.getName().lastIndexOf(".")+1);
		
		for(String foo: legalExtensions)
			if(foo.equalsIgnoreCase(ext))
				return true;
		
		System.err.println("rejected file \"" +song.getName()+ "\"");
		return false;
	}
	
	
	
	/**
	 * Gets the extension of a file, returning it with the '.' excluded
	 */
	public static String getExt(File song)
	{
		return song.getName().substring(song.getName().lastIndexOf('.') + 1);
	}
	
	
	
	
	
	/**
	 * Jid3lib likes to give things like song title / album in a strange dialect of
	 * unicode. just convert back to ASCII and call it a day
	 */
	public static String clean(String in)
	{
		in = in.replace("<", "");
		in = in.replace(">", "");
		in = in.replace(":", "");
		in = in.replace("\"", "");
		in = in.replace("/", "");
		in = in.replace("\\", "");
		in = in.replace("|", "");
		in = in.replace("?", "");
		in = in.replace("*", "");
		in = in.replace(",", "");
		in = in.replace("[", "");
		in = in.replace("]", "");
		in = in.replace("{", "");
		in = in.replace("}", "");
		in = in.replace("ÿþ", ""); // this funky thing is there... sometimes. IDK why, but it screws with the path
		in = in.replace(Character.toString('\0'), ""); // Same with NULs. Sometimes there, sometimes not...


		in.trim();

		return in;
	}
}




