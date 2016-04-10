package org.tutske.websocket;

import static org.tutske.options.Option.*;

import org.tutske.options.*;


public class Options {

	private static final String PREFIX = "TS_WEBSOCKET";
	private static final String FILENAME  = "application.properties";

	public static final Option<Integer> PORT = new IntegerOption ("port", 8080);
	public static final Option<String> STATIC_PATH = new StringOption ("static path", "static");

	public static void initialize (String [] args) {
		OptionStore.registerOptions (
			PORT, STATIC_PATH
		);
		OptionStore.registerPopulators (
			new PropertyFileOptionsPopulator ("classpath://" + FILENAME),
			new PropertyFileOptionsPopulator (FILENAME),
			new EnvironmentOptionsPopulator (PREFIX),
			new ArgumentOptionsPopulator (args)
		);
		OptionStore.loadOptions ();
	}

}
