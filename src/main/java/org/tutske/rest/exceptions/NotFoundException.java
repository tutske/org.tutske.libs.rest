package org.tutske.rest.exceptions;

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

	public NotFoundException (ExceptionData data) {
		super (data);
	}

	public NotFoundException (String message, ExceptionData data) {
		super (message, data);
	}

}
