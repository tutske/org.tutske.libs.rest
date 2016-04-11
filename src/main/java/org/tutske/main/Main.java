package org.tutske.main;

import org.eclipse.jetty.server.Server;


public class Main {

	public static void main (String [] args) throws Exception {
		Options.initialize (args);

		Server server = new Beans ().server ();

		server.start ();
		server.dump (System.out);
		server.join ();
	}

}
