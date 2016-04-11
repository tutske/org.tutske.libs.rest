package org.tutske.main;

import org.tutske.controllers.HelloWorldController;
import org.tutske.websocket.UrlRouter;

import static org.tutske.websocket.HttpRequest.Method.POST;
import static org.tutske.websocket.UrlRoute.*;

import java.util.EnumSet;


public class Routes {

	public final UrlRouter router = new UrlRouter ().add (
		"Hello world routes",
		new SimpleRoute ("get hello", "/hello", HelloWorldController::get),
		new SimpleRoute ("post hello", "/hello", EnumSet.of (POST), HelloWorldController::post)
	);

}
