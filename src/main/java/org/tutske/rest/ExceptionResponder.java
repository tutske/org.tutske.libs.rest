package org.tutske.rest;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tutske.rest.exceptions.InternalException;
import org.tutske.rest.exceptions.ResponseException;
import org.tutske.lib.utils.Bag;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class ExceptionResponder {

	private final static Logger logger = LoggerFactory.getLogger (ErrorAwareHandlerList.class);

	private final ObjectMapper mapper;

	public ExceptionResponder () {
		this (null);
	}

	public ExceptionResponder (ObjectMapper mapper) {
		this.mapper = mapper != null ? mapper.copy () : new ObjectMapper ();
		this.mapper.registerModule (errorModule ());
	}

	public void respond (String s, Request base, HttpServletRequest request, HttpServletResponse response, Throwable throwable)
	throws IOException, ServletException {
		String msg = throwable.getMessage ();
		ResponseException ex = throwable instanceof ResponseException ?
			(ResponseException) throwable :
			new InternalException (msg == null ? "" : msg, throwable);

		if ( throwable == ex ) {
			logger.info ("Responded with an exception [from: {}, {}] [req: {} {}] {} {}",
				request.getRemoteAddr (), retrievePrincipal (request), request.getMethod (), s,
				ex.getStatusCode (), ex.getClass ().getSimpleName ()
			);
		} else {
			logger.error ("An exception occurred while processing a request [from: {}, {}] {} {}",
				request.getRemoteAddr (), retrievePrincipal (request), request.getMethod (), s, throwable
			);
		}

		response.setStatus (ex.getStatusCode ());
		mapper.writeValue (response.getOutputStream (), ex);
		response.getOutputStream ().flush ();

		base.setHandled (true);
	}

	private String retrievePrincipal (HttpServletRequest request) {
		Bag<String, String> context = (Bag) request.getAttribute ("context");

		if ( context == null ) { return null; }
		if ( ! context.containsKey ("principal") ) { return null; }

		return context.getAs ("principal", String.class);
	}

	private static Module errorModule () {
		SimpleModule module = new SimpleModule ();
		module.addSerializer (new ResponseException.ResponseExceptionSerializer ());
		return module;
	}

}
