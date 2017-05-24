package org.tutske.rest.util;

import static org.mockito.Mockito.*;

import org.eclipse.jetty.server.Request;
import org.tutske.rest.internals.RestHandler;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;


public class RoundTrip {

	public final ByteArrayOutputStream _output = new ByteArrayOutputStream ();
	public final PrintWriter _writer = new PrintWriter (_output);
	public final ServletOutputStream _stream = new ServletOutputStream () {
		@Override public void write (int b) throws IOException { _output.write (b); }
		@Override public boolean isReady () { return true; }
		@Override public void setWriteListener (WriteListener writeListener) {}
	};

	public final Request base = mock (Request.class);
	public final HttpServletRequest request = mock (HttpServletRequest.class);
	public final HttpServletResponse response = mock (HttpServletResponse.class);

	public void setup () throws IOException {
		when (response.getWriter ()).thenReturn (_writer);
		when (response.getOutputStream ()).thenReturn (_stream);
	}

	public void get (RestHandler handler, String url) throws Exception {
		when (request.getMethod ()).thenReturn ("GET");
		handler.handle (url, base, request, response);
	}

	public String output () throws IOException {
		_writer.flush ();
		_stream.flush ();
		return new String (_output.toByteArray ());
	}

}
