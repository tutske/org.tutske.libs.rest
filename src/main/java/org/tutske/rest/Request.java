package org.tutske.rest;

import com.fasterxml.jackson.databind.JsonNode;
import org.tutske.utils.Bag;
import org.tutske.utils.StreamCopier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public interface Request {

	public enum Method {
		CONNECT, DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT, TRACE, UNKNOWN;
		public static Method of (String value) {
			try { return Method.valueOf (value.toUpperCase ()); }
			catch ( IllegalArgumentException e ) { return UNKNOWN; }
		}
	}

	public Method getMethod ();
	public String getUri ();

	public String getHeader (String header);

	public Bag<String, String> pathParams ();
	public Bag<String, String> queryParams ();
	public Bag<String, Object> context ();

	public void setHeader (String header, String value);
	public void setStatus (int status);

	default public String getBody () {
		ByteArrayOutputStream out = new ByteArrayOutputStream ();

		try { new StreamCopier (getInputStream (), out).copy (); }
		catch ( IOException e ) { throw new RuntimeException (e); }

		return new String (out.toByteArray ());
	}

	public <T> T json (Class<T> clazz);
	default public JsonNode json () {
		return json (JsonNode.class);
	}

	default public InputStream getInputStream () throws IOException {
		return new ByteArrayInputStream (getBody ().getBytes ());
	}

	public OutputStream getOutputStream () throws IOException;

	public <T> T extractWrapped (Class<T> clazz);

}
