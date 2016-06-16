package org.tutske.rest.internals;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.tutske.rest.data.RestObject;
import org.tutske.rest.exceptions.NotFoundException;
import org.tutske.rest.exceptions.ResponseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


public class NotFoundHandler extends AbstractHandler {

	private final Map<String, Serializer> serializers;

	public NotFoundHandler (Map<String, Serializer> serializers) {
		this.serializers = serializers;
	}

	@Override
	public void handle (String s, Request base, HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		ResponseException exception = new NotFoundException ("Could not find resource");
		exception.addExtra (new RestObject () {{
			v ("requested", s);
			v ("method", request.getMethod ());
		}});

		String accept = request.getHeader ("Accept");
		String type = serializers.containsKey (accept) ? accept : "application/json";
		Serializer serializer = serializers.get (serializers.containsKey (accept) ? accept : "default");

		response.setStatus (exception.getStatusCode ());
		response.setContentType (type);
		serializer.serialize (exception.asRestStructure (), response.getWriter ());

		base.setHandled (true);
	}

}
