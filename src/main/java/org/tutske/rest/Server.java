package org.tutske.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.tutske.rest.data.RestObject;
import org.tutske.rest.exceptions.ResponseException;
import org.tutske.rest.internals.FilterCollection;
import org.tutske.rest.internals.NotFoundHandler;
import org.tutske.rest.internals.RestHandler;
import org.tutske.rest.internals.SocketHandler;


public class Server {

	private static int DEFAULT_PORT = 8080;

	private final org.eclipse.jetty.server.Server server;
	private final String baseurl;
	private final Gson gson = new GsonBuilder ().create ();

	private Handler resources = null;
	private Handler sockets = null;

	private UrlRouter<ControllerFunction> router = null;
	private FilterCollection<HttpRequest, RestObject>  filters = null;

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

	public Server configureFilters (FilterCollection<HttpRequest, RestObject> filters) {
		this.filters = filters;
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

		HandlerList handlers = new HandlerList ();
		if ( resources != null ) {
			handlers.addHandler (resources);
		}
		if ( router != null && filters != null ) {
			handlers.addHandler (new RestHandler (router, filters, gson));
		}
		if ( router != null && filters == null ) {
			handlers.addHandler (new RestHandler (router, gson));
		}
		if ( sockets != null ) {
			handlers.addHandler (sockets);
		}
		handlers.addHandler (new NotFoundHandler (gson));

		server.setHandler (handlers);
		server.start ();
	}

	public void stop () throws Exception {
		server.stop ();
	}

}
