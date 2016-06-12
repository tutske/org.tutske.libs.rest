package it.tutske;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.tutske.options.OptionStore;
import org.tutske.rest.internals.NotFoundHandler;
import org.tutske.rest.internals.RestHandler;
import org.tutske.rest.internals.SocketHandler;


public class Beans {

	private final int port = OptionStore.get (Options.PORT);
	private final String staticFilesPath = OptionStore.get (Options.STATIC_PATH);

	public Server server () {
		Server server = new Server (port);
		server.setHandler (handlers ());
		return server;
	}

	public HandlerList handlers () {
		HandlerList handlers = new HandlerList ();
		handlers.addHandler (resourceHandler ());
		handlers.addHandler (routingHandler ());
		handlers.addHandler (socketHandler ());
		handlers.addHandler (notFoundHandler ());
		return handlers;
	}

	public Handler resourceHandler () {
		String path = IntegrationMain.class.getClassLoader ()
			.getResource (staticFilesPath)
			.toExternalForm ();

		ResourceHandler resources = new ResourceHandler ();
		resources.setResourceBase (path);

		return resources;
	}

	public Handler notFoundHandler () {
		return new NotFoundHandler (gson ());
	}

	public Handler routingHandler () {
		return new RestHandler (Routes.router, gson ());
	}

	public Gson gson () {
		return new GsonBuilder ().create ();
	}

	public Handler socketHandler () {
		return new SocketHandler (Routes.sockets);
	}

}
