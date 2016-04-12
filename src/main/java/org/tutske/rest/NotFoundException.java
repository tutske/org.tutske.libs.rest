package org.tutske.rest;

import javax.servlet.http.HttpServletResponse;


public class NotFoundException extends ResponseException {

	{
		type = "/not_found";
		title = "Not Found";
		status = HttpServletResponse.SC_NOT_FOUND;
	}

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
