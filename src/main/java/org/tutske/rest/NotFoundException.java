package org.tutske.rest;

import javax.servlet.http.HttpServletResponse;


public class NotFoundException extends ResponseException {

	protected final String type = "/not_found";
	protected final String title = "Not Found";
	protected final int status = HttpServletResponse.SC_NOT_FOUND;

	public NotFoundException () {
	}

	public NotFoundException (String message) {
		super (message);
	}

	public NotFoundException (String message, Throwable cause) {
		super (message, cause);
	}

	public NotFoundException (Throwable cause) {
		super (cause);
	}

	public NotFoundException (RestObject data) {
		super (data);
	}

	public NotFoundException (String message, RestObject data) {
		super (message, data);
	}

}
