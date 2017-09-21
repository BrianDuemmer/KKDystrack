package util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Extends the @link PrintStream class by having it format a date and time before each message
 * @author Duemmer
 *
 */
public class SmartPrintStream extends PrintStream 
{
	private PrintWriter writer = null;
	
	public SmartPrintStream(OutputStream out, PrintWriter writer) 
	{ 
		super(out); 
		this.writer = writer;
	}

	@Override
	public void print(String s)
	{
		SimpleDateFormat sd = new SimpleDateFormat("MM/dd/YY - hh:mm:ss:SSS a");
		String head = "[" +sd.format(new Date())+"]	";
		super.print(head + s);
		
		if(writer != null)
		{
			writer.print(head + s + "\n");
			writer.flush();
		}
	}
}
