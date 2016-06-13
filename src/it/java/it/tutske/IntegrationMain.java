package it.tutske;


public class IntegrationMain {

	public static void main (String [] args) throws Exception {
		Options.initialize (args);
		new Beans ().server ().start ();
	}

}
