package it.tutske;

import org.tutske.options.OptionStore;
import org.tutske.rest.Server;


public class IntegrationMain {

	public static void main (String [] args) throws Exception {
		Options.initialize (args);

		new Server (OptionStore.get (Options.BASE_URL), OptionStore.get (Options.PORT))
			.configureStaticContent (OptionStore.get (Options.STATIC_PATH))
			.configureRoutes (Routes.router)
			.configureSocketRoutes (Routes.sockets)
			.start ();
	}

}
