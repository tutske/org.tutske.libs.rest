package it.tutske;

import static org.tutske.rest.HttpRequest.Method.*;
import static org.tutske.rest.UrlRoute.*;

import it.tutske.controllers.ChatSocket;
import it.tutske.controllers.HelloWorldController;
import it.tutske.controllers.JavaSocket;
import org.tutske.rest.ControllerFunction;
import org.tutske.rest.SocketFunction;
import org.tutske.rest.UrlRouter;

import java.util.EnumSet;


public class Routes {

	private static final HelloWorldController hello = new HelloWorldController ();

	public static final UrlRouter<ControllerFunction> router = new UrlRouter<ControllerFunction> ().add (
		"Hello world routes",
		new ControllerRoute ("get hello", "/hello", hello::get),
		new ControllerRoute ("post hello", "/hello", EnumSet.of (POST), hello::post),
		new ControllerRoute ("file", "/file/:filename", hello::readTempFile),
		new ControllerRoute ("randoms", "/randoms", hello::randoms),
		new ControllerRoute ("echo", "/echo", EnumSet.of (POST, PUT), hello::echo)
	);

	public static final UrlRouter<SocketFunction> sockets = new UrlRouter<SocketFunction> ().add (
		"basic socket",
		new SocketRoute ("chat socket", "/chat/:room", ChatSocket::create),
		new SocketRoute ("java socket", "/java", JavaSocket::create)
	);

}
