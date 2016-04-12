package org.tutske.main;

import static org.tutske.rest.HttpRequest.Method.*;
import static org.tutske.rest.UrlRoute.SimpleRoute;

import org.tutske.controllers.HelloWorldController;
import org.tutske.rest.UrlRouter;

import java.util.EnumSet;


public class Routes {

	private static final HelloWorldController hello = new HelloWorldController ();


	public final UrlRouter router = new UrlRouter ().add (
		"Hello world routes",
		new SimpleRoute ("get hello", "/hello", hello::get),
		new SimpleRoute ("post hello", "/hello", EnumSet.of (POST), hello::post),
		new SimpleRoute ("file", "/file/:filename", hello::readTempFile),
		new SimpleRoute ("randoms", "/randoms", hello::randoms),
		new SimpleRoute ("echo", "/echo", EnumSet.of (POST, PUT), hello::echo)
	);

}
