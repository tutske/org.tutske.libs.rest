package org.tutske.lib.rest.exceptions;

import javax.servlet.http.HttpServletResponse;


public class InputException extends ResponseException {

	{
		type = "/wrong-input";
		title = "Invalid input provided";
		status = HttpServletResponse.SC_BAD_REQUEST;
	}

	public InputException () {
		this ("Invalid input.");
	}

	public InputException (String message) {
		super (message);
	}

	public InputException (String message, Throwable cause) {
		super (message, cause);
	}

	public InputException (Throwable cause) {
		super (cause);
	}

	public InputException (ExceptionData data) {
		super (data);
	}

	public InputException (String message, ExceptionData data) {
		super (message, data);
	}

}
