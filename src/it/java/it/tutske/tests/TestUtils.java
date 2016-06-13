package it.tutske.tests;

import it.tutske.Beans;
import it.tutske.Options;
import it.tutske.util.CommandRunner;
import it.tutske.util.Environment;
import org.tutske.rest.Server;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;


public class TestUtils {

	public static final int PORT = 18080;
	private static final boolean reInitialize = false;
	private static Application application;

	public static URL getUrl (String path) throws MalformedURLException {
		return new URL ("http", "localhost", PORT, path);
	}

	public static URI getSocketUri (String path) throws Exception {
		return new URI ("ws", "", "localhost", PORT, path, "username=jhon", "");
	}

	public static Application getApplication () {
		if ( application == null || reInitialize ) {
			application = new InternallyStartApplication ();
			// application = new ExternallyStartApplication ();
			// application = new ExternalRunningApplication ();
		}
		return application;
	}

	public static interface Application {
		void start () throws Exception;
		void stop () throws Exception;
	}

	public static class ExternallyStartApplication implements Application {
		private Process server;
		@Override public void start () throws Exception {
			Environment env = new Environment ().with ("TS_WEBSOCKET_PORT", "" + TestUtils.PORT);
			server = CommandRunner.runJar (env);
			Thread.sleep (600);
		}
		@Override public void stop () throws Exception {
			if ( server != null ) {
				server.destroy ();
			}
		}
	}

	public static class InternallyStartApplication implements Application {
		private Server server;
		@Override public void start () throws Exception {
			Options.initialize (new String [] {"--port=" + PORT});
			server = new Beans ().server ();
			server.startAsync ();
		}

		@Override public void stop () throws Exception {
			server.stop ();
		}
	}

	public static class ExternalRunningApplication implements Application {
		@Override public void start () throws Exception {
		}
		@Override public void stop () throws Exception {
		}
	}
}
