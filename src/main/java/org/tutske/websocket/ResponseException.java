package org.tutske.websocket;

import org.tutske.main.Options;
import org.tutske.options.OptionStore;

import javax.servlet.http.HttpServletResponse;


public class ResponseException extends RuntimeException {

	protected final String baseUrl = OptionStore.get (Options.BASE_URL);
	protected final String type = "/interal_server_error";
	protected final String title = "Internal Server Error";
	protected final int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	protected final RestObject data = new RestObject ();

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

	public void addExtra (RestObject extra) {
		this.data.merge (extra);
	}

	public Object asJson () {
		this.data.merge (new RestObject () {{
			v ("type", baseUrl + type);
			v ("title", title);
			v ("status", status);
			v ("detail", getMessage ());
		}});

		return this.data.asJson ();
	}
}
