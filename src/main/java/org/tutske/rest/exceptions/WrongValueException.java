package org.tutske.rest.exceptions;

import javax.servlet.http.HttpServletResponse;


public class WrongValueException extends ResponseException {

	{
		type = "/wrong_value";
		title = "wrong value";
		status = HttpServletResponse.SC_BAD_REQUEST;
	}

	public WrongValueException () {
	}

	public WrongValueException (String message) {
		super (message);
	}

	public WrongValueException (String message, Throwable cause) {
		super (message, cause);
	}

	public WrongValueException (Throwable cause) {
		super (cause);
	}

	public WrongValueException (ExceptionData data) {
		super (data);
	}

	public WrongValueException (String message, ExceptionData data) {
		super (message, data);
	}

}
