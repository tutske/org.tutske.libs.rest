package org.tutske.rest;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.tutske.rest.data.RestStructure;
import org.tutske.rest.exceptions.ResponseException;
import org.tutske.rest.internals.*;

import java.util.HashMap;
import java.util.Map;


public class Server {

	private static int DEFAULT_PORT = 8080;

	private final org.eclipse.jetty.server.Server server;
	private final String baseurl;

	private Handler resources = null;
	private Handler sockets = null;

	private UrlRouter<ControllerFunction> router = null;
	private FilterCollection<HttpRequest, RestStructure>  filters = null;
	private Map<String, Serializer> serializers = null;
	private String defaultSerializer = null;

	public Server (String baseurl) {
		this (baseurl, DEFAULT_PORT);
	}

	public Server (String baseurl, int port) {
		this.server = new org.eclipse.jetty.server.Server (port);
		this.baseurl = baseurl;
	}

	public Server configureRoutes (UrlRouter<ControllerFunction> router) {
		this.router = router;
		return this;
	}

	public Server configureSocketRoutes (UrlRouter<SocketFunction> router) {
		sockets = new SocketHandler (router);
		return this;
	}

	public Server configureFilters (FilterCollection<HttpRequest, RestStructure> filters) {
		this.filters = filters;
		return this;
	}

	public Server configureSerializers (String defaultSerializer, Map<String, Serializer> serializers) {
		this.defaultSerializer = defaultSerializer;
		this.serializers = serializers;
		return this;
	}

	public Server configureStaticContent (String path) {
		String externalform = this.getClass ().getClassLoader ()
			.getResource (path).toExternalForm ();

		ResourceHandler resources = new ResourceHandler ();
		resources.setResourceBase (externalform);
		this.resources = resources;

		return this;
	}

	public void start () throws Exception {
		startAsync ();
		server.join ();
	}

	public void startAsync () throws Exception {
		ResponseException.configureBaseUrl (baseurl);

		if ( serializers == null ) {
			serializers = defaultSerializers ();
		}
		if ( defaultSerializer == null ) {
			defaultSerializer = "application/json";
		}
		ContentSerializer serializer = new ContentSerializer (defaultSerializer, serializers);

		HandlerList handlers = new HandlerList ();
		if ( resources != null ) {
			handlers.addHandler (resources);
		}
		if ( router != null && filters != null ) {
			handlers.addHandler (new RestHandler (router, filters, serializer));
		}
		if ( router != null && filters == null ) {
			handlers.addHandler (new RestHandler (router, serializer));
		}
		if ( sockets != null ) {
			handlers.addHandler (sockets);
		}
		handlers.addHandler (new NotFoundHandler (serializers));

		server.setHandler (handlers);
		server.start ();
	}

	public void stop () throws Exception {
		server.stop ();
	}

	private Map<String, Serializer> defaultSerializers () {
		Map<String, Serializer> serializers = new HashMap<> ();

		serializers.put ("application/json", new JsonSerializer ());
		serializers.put ("application/xml", new XmlSerializer ());
		serializers.put ("default", serializers.get ("application/json"));

		return serializers;
	}

}
