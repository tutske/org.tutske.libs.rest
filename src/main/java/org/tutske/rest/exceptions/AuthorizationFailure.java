package org.tutske.rest.exceptions;

import org.tutske.rest.data.RestObject;
import org.tutske.rest.jwt.JsonWebToken;

import javax.servlet.http.HttpServletResponse;


public class AuthorizationFailure extends ResponseException {

	{
		type = "/authorization_failure";
		title = "You are not authorizated to access the url";
		status = HttpServletResponse.SC_FORBIDDEN;
	}

	public AuthorizationFailure () {
	}

	public AuthorizationFailure (String message) {
		super (message);
	}

	public AuthorizationFailure (String message, Throwable cause) {
		super (message, cause);
	}

	public AuthorizationFailure (Throwable cause) {
		super (cause);
	}

	public AuthorizationFailure (RestObject data) {
		super (data);
	}

	public AuthorizationFailure (String message, RestObject data) {
		super (message, data);
	}

}
