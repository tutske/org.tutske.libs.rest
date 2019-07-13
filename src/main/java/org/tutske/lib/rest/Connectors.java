package org.tutske.lib.rest;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.nio.file.Files;
import java.nio.file.Path;


public class Connectors {

	public static ServerConnector httpConnector (Server server, int port) {
		for ( Connector connector : server.getConnectors () ) {
			if ( connector instanceof ServerConnector && connector.getProtocols ().contains ("ssl") ) {
				return httpConnector (server, port, (ServerConnector) connector);
			}
		}
		return httpConnector (server, port, null);
	}

	public static ServerConnector httpConnector (Server server, int port, ServerConnector https) {
		return httpConnector (server, port, https == null ? -1 : https.getPort ());
	}

	public static ServerConnector httpConnector (Server server, int port, int https) {
		HttpConfiguration configuration = new HttpConfiguration ();

		if ( https > 0) {
			configuration.setSecureScheme ("https");
			configuration.setSecurePort (https);
		}

		ServerConnector connector = new ServerConnector (server,
			new HttpConnectionFactory (configuration)
		);

		connector.setPort(port);

		return connector;
	}

	public static ServerConnector httpsConnector (Server server, int port, Path keystore, String password) {
		if ( Files.notExists (keystore) ) { throw new RuntimeException ("No such file " + keystore); }
		if ( Files.isDirectory (keystore) ) { throw new RuntimeException ("Expected a file but found a directory at " + keystore); }
		if ( ! Files.isReadable (keystore) ) { throw new RuntimeException ("Can not read keystore file " + keystore); }

		SslContextFactory context = new SslContextFactory.Server ();
		context.setKeyStorePath (keystore.toString ());
		context.setKeyStorePassword (password);

		HttpConfiguration configuration = new HttpConfiguration ();
		configuration.addCustomizer (new SecureRequestCustomizer ());

		ServerConnector connector = new ServerConnector (server,
			new SslConnectionFactory (context, HttpVersion.HTTP_1_1.asString ()),
			new HttpConnectionFactory (configuration)
		);

		connector.setPort (port);

		return connector;
	}

}
