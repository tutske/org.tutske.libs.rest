package org.tutske.rest.util;

import static org.mockito.Mockito.*;

import org.eclipse.jetty.server.Request;
import org.tutske.rest.internals.RestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;


public class RoundTrip {

	public final ByteArrayOutputStream _output = new ByteArrayOutputStream ();
	public final PrintWriter _writer = new PrintWriter (_output);

	public final Request base = mock (Request.class);
	public final HttpServletRequest request = mock (HttpServletRequest.class);
	public final HttpServletResponse response = mock (HttpServletResponse.class);

	public void setup () throws IOException {
		when (response.getWriter ()).thenReturn (_writer);
	}

	public void get (RestHandler handler, String url) throws Exception {
		when (request.getMethod ()).thenReturn ("GET");
		handler.handle (url, base, request, response);
	}

	public String output () {
		_writer.flush ();
		return new String (_output.toByteArray ());
	}

}
