package org.tutske.rest.exceptions;

import javax.servlet.http.HttpServletResponse;


public class InternalException extends ResponseException {

	{
		type = "/internal-error";
		title = "The server suffered an internal error.";
		status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	}

	public InternalException () {
	}

	public InternalException (String message) {
		super (message);
	}

	public InternalException (String message, Throwable cause) {
		super (message, cause);
	}

	public InternalException (Throwable cause) {
		super (cause);
	}

	public InternalException (ExceptionData data) {
		super (data);
	}

	public InternalException (String message, ExceptionData data) {
		super (message, data);
	}

}
