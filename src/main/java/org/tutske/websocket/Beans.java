package org.tutske.websocket;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.tutske.options.OptionStore;


public class Beans {

	private final int port = OptionStore.get (Options.PORT);

	public UrlRouter router () {
		return new Routes ().router;
	}

	public Server server () {
		Server server = new Server (port);
		server.setHandler (handlers ());
		return server;
	}

	public HandlerList handlers () {
		HandlerList handlers = new HandlerList ();
		handlers.addHandler (resourceHandler ());
		handlers.addHandler (routingHandler ());
		return handlers;
	}

	public Handler resourceHandler () {
		String path = Main.class.getClassLoader ()
			.getResource (OptionStore.get (Options.STATIC_PATH))
			.toExternalForm ();

		ResourceHandler resources = new ResourceHandler ();
		resources.setResourceBase (path);

		return resources;
	}

	public Handler routingHandler () {
		return new RoutingHandler (router ());
	}

}
