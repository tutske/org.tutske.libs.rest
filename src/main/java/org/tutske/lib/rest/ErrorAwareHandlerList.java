package org.tutske.lib.rest;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class ErrorAwareHandlerList extends HandlerList {

	private final ExceptionResponder responder;

	public ErrorAwareHandlerList (Handler... handlers) {
		this (null, handlers);
	}

	public ErrorAwareHandlerList (ExceptionResponder responder, Handler... handlers) {
		for ( Handler handler : handlers ) { addHandler (handler); }
		this.responder = responder == null ? new ExceptionResponder () : responder;
	}

	@Override
	public void handle (String s, Request base, HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		try { super.handle (s, base, request, response); }
		catch ( Exception ex) {
			responder.respond (s, base, request, response, ex);
			base.setHandled (true);
		}
	}

}
