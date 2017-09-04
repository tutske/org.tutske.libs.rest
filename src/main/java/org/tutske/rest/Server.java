package org.tutske.rest;

import com.google.gson.Gson;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.tutske.rest.data.RestStructure;
import org.tutske.rest.exceptions.ResponseException;
import org.tutske.rest.internals.*;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class Server {

	private final org.eclipse.jetty.server.Server server;
	private final String baseurl;

	private Handler resources = null;
	private Handler sockets = null;

	private List<Handler> handlers = new LinkedList<> ();
	private UrlRouter<ControllerFunction> router = null;
	private FilterCollection<HttpRequest, RestStructure> filters = null;
	private Map<String, Serializer> serializers = null;
	private String defaultSerializer = null;
	private Gson gson = new Gson ();

	public Server (String baseurl, int port) {
		this.server = new org.eclipse.jetty.server.Server (port);
		this.baseurl = baseurl;
	}

	public Server configureSsl (int port, KeyStore keys, KeyStore trusted, String password) {
		SslContextFactory ssl = new SslContextFactory ();
		ssl.setKeyStore (keys);
		ssl.setTrustStore (trusted);
		ssl.setKeyStorePassword (password);

		ssl.setWantClientAuth (true);

		HttpConfiguration https = new HttpConfiguration ();
		https.addCustomizer (new SecureRequestCustomizer ());
		https.setSecureScheme ("https");

		ServerConnector sslConnector = new ServerConnector (
			server,
			new SslConnectionFactory (ssl, HttpVersion.HTTP_1_1.asString ()),
			new HttpConnectionFactory (https)
		);

		sslConnector.setPort (port);

		server.addConnector (sslConnector);

		return this;
	}

	public Server configureHandlers (List<Handler> handlers) {
		this.handlers = handlers;
		return this;
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

	public Server configureGson (Gson gson) {
		this.gson = gson;
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

		this.handlers.forEach (handlers::addHandler);
		handlers.addHandler (new NotFoundHandler (serializers));

		server.setHandler (handlers);
		server.start ();
	}

	public void stop () throws Exception {
		server.stop ();
	}

	private Map<String, Serializer> defaultSerializers () {
		Map<String, Serializer> serializers = new HashMap<> ();

		serializers.put ("application/json", new JsonSerializer (gson));
		serializers.put ("application/xml", new XmlSerializer ());
		serializers.put ("application/javascript", new JsonPSerializer ());
		serializers.put ("default", serializers.get ("application/json"));

		return serializers;
	}

}
