package org.tutske.websocket;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;


public class HttpRequest {

	public enum Method {
		HEAD, OPTIONS, GET, POST, PUT, DELETE, TRACE
	}

	public HttpServletRequest getServletRequest () {
		return null;
	}

	public HttpServletResponse getServletResponse () {
		return null;
	}

	public Method getMethod () {
		return null;
	}

	public String getHeader (String header) {
		return null;
	}

	public String getUri () {
		return null;
	}

	public <T> T getPathParameter (String name, Class<T> clazz) {
		return null;
	}

	public <T> T getQueryParameter (String name, Class<T> clazz) {
		return null;
	}

	public String getBody () {
		return null;
	}

	public InputStream getInputStream () {
		return null;
	}

}
