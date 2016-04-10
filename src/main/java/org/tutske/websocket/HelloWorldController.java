package org.tutske.websocket;

public class HelloWorldController {

	public static RestResponse get (HttpRequest request) {
		return new RestResponse ("Hello World!");
	}

	public static RestResponse post (HttpRequest request) {
		return new RestResponse ("posted: Hello World!");
	}

}
