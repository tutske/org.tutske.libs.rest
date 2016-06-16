package org.tutske.rest.exceptions;

import org.tutske.rest.data.RestObject;
import org.tutske.rest.data.RestStructure;

import javax.servlet.http.HttpServletResponse;


public class ResponseException extends RuntimeException {

	private static String BASE_URL = null;

	public static void configureBaseUrl (String baseUrl) {
		if ( BASE_URL == null ) {
			BASE_URL = baseUrl;
		}
	}

	protected final String baseUrl = BASE_URL;
	protected final RestObject data = new RestObject ();
	protected String type;
	protected String title;
	protected int status;

	{
		type = "/interal_server_error";
		title = "Internal Server Error";
		status = HttpServletResponse.SC_BAD_REQUEST;
	}

	public ResponseException () {}
	public ResponseException (String message) { super (message); }
	public ResponseException (String message, Throwable cause) { super (message, cause); }
	public ResponseException (Throwable cause) { super (cause); }

	public ResponseException (RestObject data) {
		this.data.merge (data);
	}
	public ResponseException (String message, RestObject data) {
		this (message);
		this.data.merge (data);
	}

	public int getStatusCode () {
		return this.status;
	}

	public void addExtra (RestObject extra) {
		this.data.merge (extra);
	}

	public RestStructure asRestStructure () {
		return new RestObject ("error") {{
			v ("type", baseUrl + type);
			v ("title", title);
			v ("status", status);
			v ("detail", getMessage ());
		}}.merge (this.data);
	}

}
