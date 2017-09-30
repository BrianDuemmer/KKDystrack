package util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;


/**
 * Reads an integer 0/1 parameter as a boolean. Useful as MySQL / PHP side stores them
 * this way.
 * @author Duemmer
 *
 */
public class BooleanAsIntAdapter implements JsonDeserializer<Boolean> {

	@Override
	public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		int code = json.getAsInt();
		boolean bVal = code != 0;
		return bVal;
	}

}
