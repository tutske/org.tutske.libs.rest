package org.tutske.rest.exceptions;

import org.tutske.rest.data.RestObject;
import org.tutske.rest.exceptions.ResponseException;

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

	public WrongValueException (RestObject data) {
		super (data);
	}

	public WrongValueException (String message, RestObject data) {
		super (message, data);
	}
}
