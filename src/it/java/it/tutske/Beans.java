package it.tutske;

import org.tutske.options.OptionStore;
import org.tutske.rest.Server;


public class Beans {

	private final int port = OptionStore.get (Options.PORT);
	private final String baseurl = OptionStore.get (Options.BASE_URL);
	private final String staticFilesPath = OptionStore.get (Options.STATIC_PATH);

	public Server server () {
		return new Server (baseurl, port)
			.configureStaticContent (staticFilesPath)
			.configureRoutes (Routes.router)
			.configureSocketRoutes (Routes.sockets);
	}

}
