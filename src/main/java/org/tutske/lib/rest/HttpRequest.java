package org.tutske.lib.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.tutske.lib.api.Request;
import org.tutske.lib.utils.Bag;
import org.tutske.lib.utils.Exceptions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class HttpRequest implements Request {

	protected final Bag<String, Object> context = new Bag<> ();
	protected final HttpServletRequest request;
	protected final HttpServletResponse response;
	protected final ObjectMapper mapper;
	protected final Bag<String, String> path;
	protected final Bag<String, String> queryParams;

	public HttpRequest (HttpServletRequest request, HttpServletResponse response, Bag<String, String> path, ObjectMapper mapper) {
		this.request = request;
		this.response = response;
		this.mapper = mapper;
		this.path = path;
		this.queryParams = new Bag<> ();
	}

	@Override
	public Method getMethod () {
		return Method.of (request.getMethod ());
	}

	@Override
	public String getHeader (String header) {
		return request.getHeader (header);
	}

	@Override
	public String getUri () {
		return request.getRequestURI ();
	}

	@Override
	public void setHeader (String header, String value) {
		response.setHeader (header, value);
	}

	@Override
	public void setStatus (int status) {
		response.setStatus (status);
	}

	@Override
	public Bag<String, String> pathParams () {
		return path;
	}

	@Override
	public Bag<String, String> queryParams () {
		if ( queryParams.isEmpty () ) {
			Request.decodeInto (queryParams, request.getQueryString ());
		}
		return queryParams;
	}

	@Override
	public Bag<String, Object> context () {
		return this.context;
	}

	@Override
	public <T> T json (Class<T> clazz) {
		try { return mapper.readValue (getInputStream (), clazz); }
		catch ( Exception e ) { throw Exceptions.wrap (e); }
	}

	@Override
	public InputStream getInputStream () throws IOException {
		return request.getInputStream ();
	}

	@Override
	public OutputStream getOutputStream () throws IOException {
		return response.getOutputStream ();
	}

	@Override
	public <T> T extractWrapped (Class<T> clazz) {
		if ( HttpServletRequest.class.equals (clazz) ) { return (T) request; }
		if ( HttpServletResponse.class.equals (clazz) ) { return (T) response; }
		if ( ObjectMapper.class.equals (clazz) ) { return (T) mapper; }
		return null;
	}

}
