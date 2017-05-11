package org.tutske.rest.exceptions;

import org.tutske.rest.data.RestObject;

import javax.servlet.http.HttpServletResponse;


public class AuthenticationFailure extends ResponseException {

	{
		type = "/authentication-failure";
		title = "Authentication Failure";
		status = HttpServletResponse.SC_FORBIDDEN;
	}

	public AuthenticationFailure () {
		this ("Invalid credentials.");
	}

	public AuthenticationFailure (String message) {
		super (message);
	}

	public AuthenticationFailure (String message, Throwable cause) {
		super (message, cause);
	}

	public AuthenticationFailure (Throwable cause) {
		super (cause);
	}

	public AuthenticationFailure (RestObject data) {
		super (data);
	}

	public AuthenticationFailure (String message, RestObject data) {
		super (message, data);
	}

}
