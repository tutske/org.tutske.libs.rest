package it.tutske;

import org.eclipse.jetty.server.Server;
import org.tutske.options.OptionStore;
import org.tutske.rest.exceptions.ResponseException;


public class IntegrationMain {

	public static void main (String [] args) throws Exception {
		Options.initialize (args);
		ResponseException.configureBaseUrl (OptionStore.get (Options.BASE_URL));

		Server server = new Beans ().server ();

		server.start ();
		server.join ();
	}

}
