package org.tutske.rest.exceptions;

import org.tutske.rest.data.RestObject;
import org.tutske.rest.data.RestStructure;

import javax.servlet.http.HttpServletResponse;


public class ResponseException extends RuntimeException {

	private static String BASE_HOST = null;
	private static String BASE_URL = null;

	public static void configureBaseUrl (String baseHost, String baseUrl) {
		BASE_HOST = BASE_HOST == null && baseHost != null ? baseHost : BASE_HOST;
		BASE_URL = BASE_URL == null && baseUrl != null ? baseUrl : BASE_URL;

		if ( BASE_HOST != null && BASE_HOST.endsWith ("/") ) {
			BASE_HOST = BASE_HOST.substring (0, BASE_HOST.length () - 1);
		}

		if ( BASE_URL != null && BASE_URL.equals ("/") ) {
			BASE_URL = null;
		}
	}

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
		return asRestStructure (BASE_HOST, BASE_URL);
	}

	public RestStructure asRestStructure (String baseHost) {
		return asRestStructure (baseHost, BASE_URL);
	}

	public RestStructure asRestStructure (String baseHost, String baseUrl) {
		String msg = getMessage ();
		return new RestObject ("error") {{
			v ("type", toTypeUrl (baseHost, baseUrl));
			v ("title", title);
			v ("status", status);
			v ("detail", msg == null ? "" : msg);
		}}.merge (this.data);
	}

	public String toTypeUrl (String baseHost, String baseUrl) {
		String h = BASE_HOST != null ? BASE_HOST : baseHost != null && ! baseHost.isEmpty () ? baseHost : "";
		String b = BASE_URL != null ? BASE_URL : baseUrl != null && ! baseUrl.isEmpty () ? baseUrl : "";
		return h + b + (type.startsWith ("/") ? type : "/" + type);
	}

}
