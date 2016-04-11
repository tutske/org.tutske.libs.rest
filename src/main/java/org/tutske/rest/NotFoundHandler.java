package org.tutske.rest;

import com.google.gson.Gson;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class NotFoundHandler extends AbstractHandler {

	private final Gson gson;

	public NotFoundHandler (Gson gson) {
		this.gson = gson;
	}

	@Override
	public void handle (String s, Request base, HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		ResponseException exception = new NotFoundException ("Could not find resource");
		exception.addExtra (new RestObject () {{
			v ("requested", s);
		}});

		gson.toJson (exception.asJson (), response.getWriter ());
		base.setHandled (true);
	}

}
