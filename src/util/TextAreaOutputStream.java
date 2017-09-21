package util;

import java.io.IOException;
import java.io.OutputStream;

import javafx.application.Platform;
import javafx.scene.control.TextArea;


/**
 * Class to output log data to a textarea object. 
 * Create a new Printstream(this) to use it
 * @author Duemmer
 *
 */
public class TextAreaOutputStream extends OutputStream
{
	private TextArea ta;
	
	public TextAreaOutputStream(TextArea ta)
	{
		this.ta = ta;
	}

	@Override
	public void write(int b) throws IOException 
	{
		Platform.runLater(() -> ta.appendText(String.valueOf((char) b)));
	}
}
