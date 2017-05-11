package org.tutske.rest.jwt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;


public class JsonWebToken {

	public static enum Keys {
		Headers, EncryptedKey, Iv, Payload, Authentication
	}

	public static final String DEFAULT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
	private static final JsonWebToken EMPTY_WEB_TOKEN = new JsonWebToken (createEmptyData ());
	private static final Charset utf8 = Charset.forName ("utf-8");

	private static final Base64.Decoder decoder = Base64.getUrlDecoder ();
	private static final Base64.Encoder encoder = Base64.getUrlEncoder ();
	private static Gson gson = new GsonBuilder ()
		.registerTypeAdapter (Date.class, (JsonDeserializer<Date>) (json, type, context) -> {
			return new Date (json.getAsJsonPrimitive ().getAsLong ());
		})
		.registerTypeAdapter (Date.class, (JsonSerializer<Date>) (date, type, context) -> {
			return new JsonPrimitive (date.getTime ());
		})
		.create ();

	public static void setGson (Gson gson) {
		JsonWebToken.gson = gson;
	}

	public static JsonWebToken fromString (String token) {
		if ( token == null ) { return EMPTY_WEB_TOKEN; }

		String [] parts = token.split ("\\.");
		byte [][] data = createEmptyData ();

		if ( parts.length == 2 ) {
			data[Keys.Headers.ordinal ()] = decoder.decode (parts[0]);
			data[Keys.Payload.ordinal ()] = decoder.decode (parts[1]);
		} else if ( parts.length == 3 ) {
			data[Keys.Headers.ordinal ()] = decoder.decode (parts[0]);
			data[Keys.Payload.ordinal ()] = decoder.decode (parts[1]);
			data[Keys.Authentication.ordinal ()] = decoder.decode (parts[2]);
		} else if ( parts.length == Keys.values ().length ) {
			for ( Keys key : Keys.values () ) {
				data[key.ordinal ()] = decoder.decode (parts[key.ordinal ()]);
			}
		} else {
			throw new IllegalArgumentException ("Token `" + token + "` has wrong number of parts.");
		}

		return new JsonWebToken (data);
	}

	public static JsonWebToken fromData (Object data) {
		return fromData (gson.toJson (data));
	}

	public static JsonWebToken fromData (String json) {
		return fromData (DEFAULT_HEADER, json);
	}

	public static JsonWebToken fromData (String header, String json) {
		return new JsonWebToken (new byte [][] {
			header.getBytes (utf8), {}, {}, json.getBytes (utf8), {}
		});
	}

	private static byte [][] createEmptyData () {
		byte [][] data = new byte [Keys.values ().length][];
		for ( Keys key : Keys.values () ) {
			data[key.ordinal ()] = new byte [] {};
		}
		return data;
	}

	private final byte [][] data;

	public JsonWebToken (byte [][] data) {
		if ( data.length != Keys.values ().length ) {
			throw new IllegalArgumentException ("data does not have required fields");
		}
		this.data = data;
	}

	public byte [] get (Keys key) {
		return data[key.ordinal ()];
	}

	public <T> T get (Keys key, Class<T> clazz) {
		return gson.fromJson (new String (get (key)), clazz);
	}

	public boolean has (Keys key) {
		byte [] part = data[key.ordinal ()];
		return part != null && part.length != 0;
	}

	public JsonWebToken with (Keys key, byte [] field) {
		byte [][] data = new byte [this.data.length][];
		for ( Keys k : Keys.values () ) {
			if ( k == key) { continue; }
			byte [] retrieved = get (k);
			data[k.ordinal ()] = Arrays.copyOf (retrieved, retrieved.length);
		}
		data[key.ordinal ()] = field;
		return new JsonWebToken (data);
	}

	public String getPayload () {
		return new String (get (Keys.Payload), Charset.forName ("UTF-8"));
	}

	public <T> T getPayload (Function<String, T> fn) {
		return fn.apply (getPayload ());
	}

	public <T> T getPayload (Class<T> clazz) {
		return gson.fromJson (getPayload (), clazz);
	}

	@Override
	public String toString () {
		StringBuilder builder = new StringBuilder ();

		for ( byte [] part : data ) {
			if ( part == null || part.length == 0 ) { continue; }
			builder.append (encoder.encodeToString (part)).append (".");
		}

		return builder.delete (builder.length () - 1, builder.length ()).toString ();
	}

}
