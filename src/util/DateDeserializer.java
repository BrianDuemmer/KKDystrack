package util;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class DateDeserializer implements JsonDeserializer<Date> {
	@Override
	public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException 
	{
		String raw = json.getAsString();
		Date date = null;

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		try { date = fmt.parse(raw); } 
		catch (ParseException e) {
			System.err.println("Failed to parse date ");
			e.printStackTrace();
		}
		
		return date;
	}
}
