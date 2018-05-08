package org.tutske.rest.internals;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.tutske.rest.data.RestObject;
import org.tutske.rest.exceptions.NotFoundException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class NotFoundHandler extends AbstractHandler {

	@Override
	public void handle (String s, Request base, HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		throw new NotFoundException ("Could not find resource", new RestObject () {{
			v ("requested", s);
			v ("method", request.getMethod ());
		}});
	}

}
