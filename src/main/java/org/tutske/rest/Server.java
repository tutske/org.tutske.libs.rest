package org.tutske.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
	private List<HandlerConfig> handlers = new LinkedList<> ();

	private Map<String, Serializer> serializers;
	private String preferred;
	private Gson gson;

	public Server (String baseurl, int port) {
		this.server = new org.eclipse.jetty.server.Server (port);
		this.baseurl = baseurl.endsWith ("/") ? baseurl.substring (0, baseurl.length () - 1) : baseurl;
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

	public Server configureHandlers (Handler ... handlers) {
		for ( Handler handler : handlers ) {
			this.handlers.add (new WrappedHandlerConfig (handler));
		}
		return this;
	}

	public Server configureRoutes (UrlRouter<ControllerFunction> router) {
		return configureRoutes (router, new FilterCollection<> ());
	}

	public Server configureRoutes (UrlRouter<ControllerFunction> router, FilterCollection<HttpRequest, RestStructure> filters) {
		return configureRoutes (router, filters, null);
	}

	public Server configureRoutes (
		UrlRouter<ControllerFunction> router,
		FilterCollection<HttpRequest, RestStructure> filters,
		ContentSerializer serializer
	) {
		this.handlers.add (new ApiHandlerConfig (router, filters, serializer));
		return this;
	}

	public Server configureSocketRoutes (UrlRouter<SocketFunction> router) {
		this.handlers.add (new SocketHandlerConfig (router));
		return this ;
	}

	public Server configureStaticContent (String path) {
		String externalform = this.getClass ().getClassLoader ().getResource (path).toExternalForm ();
		ResourceHandler resources = new ResourceHandler ();
		resources.setResourceBase (externalform);
		return configureHandlers (resources);
	}

	public Server configureGson (Gson gson) {
		this.gson = gson;
		return this;
	}

	public Server configureSerializers (Map<String, Serializer> serializers, String preferred) {
		this.serializers = serializers;
		this.preferred = preferred;
		return this;
	}

	public void start () throws Exception {
		startAsync ();
		server.join ();
	}

	public void startAsync () throws Exception {
		ResponseException.configureBaseUrl (baseurl);

		if ( gson == null ) { gson = new GsonBuilder ().create (); }
		if ( serializers == null ) { serializers = defaultSerializers (); }
		if ( preferred == null ) { preferred = "application/json"; }

		HandlerList handlers = new ErrorAwareHandlerList (new ContentSerializer (preferred, serializers));

		this.handlers.forEach (config -> {
			Handler handler = config.createHandler (gson, serializers, preferred);
			handlers.addHandler (handler);
		});
		handlers.addHandler (new NotFoundHandler ());

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

	private interface HandlerConfig {
		Handler createHandler (Gson gson, Map<String, Serializer> serializers, String prefered);
	}

	private static class ApiHandlerConfig implements HandlerConfig {
		private final UrlRouter<ControllerFunction> router;
		private final FilterCollection<HttpRequest, RestStructure> filters;
		private final ContentSerializer serializers;

		public ApiHandlerConfig (
			UrlRouter<ControllerFunction> router,
			FilterCollection<HttpRequest, RestStructure> filters,
			ContentSerializer serializers
		) {
			if ( router == null ) {
				throw new NullPointerException ("Routes for api can not bu null");
			}

			this.router = router;
			this.filters = filters;
			this.serializers = serializers;
		}

		@Override public Handler createHandler (Gson gson, Map<String, Serializer> serializers, String prefered) {
			ContentSerializer s = this.serializers == null ? new ContentSerializer (prefered, serializers) : this.serializers;
			return new RestHandler (router, filters, s);
		}
	}

	private static class SocketHandlerConfig implements HandlerConfig {
		private final UrlRouter<SocketFunction> router;

		public SocketHandlerConfig (UrlRouter<SocketFunction> router) {
			this.router = router;
		}

		@Override public Handler createHandler (Gson gson, Map<String, Serializer> serializers, String prefered) {
			return new SocketHandler (router);
		}
	}

	private static class WrappedHandlerConfig implements HandlerConfig {
		private final Handler handler;

		public WrappedHandlerConfig (Handler handler) {
			this.handler = handler;
		}

		@Override public Handler createHandler (Gson gson, Map<String, Serializer> serializers, String prefered) {
			return handler;
		}
	}

}
