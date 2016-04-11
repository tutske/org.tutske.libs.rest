package org.tutske.main;

import static org.tutske.rest.HttpRequest.Method.POST;
import static org.tutske.rest.UrlRoute.SimpleRoute;

import org.tutske.controllers.HelloWorldController;
import org.tutske.rest.UrlRouter;

import java.util.EnumSet;


public class Routes {

	public final UrlRouter router = new UrlRouter ().add (
		"Hello world routes",
		new SimpleRoute ("get hello", "/hello", HelloWorldController::get),
		new SimpleRoute ("post hello", "/hello", EnumSet.of (POST), HelloWorldController::post),
		new SimpleRoute ("fail", "/fail", HelloWorldController::fail)
	);

}
