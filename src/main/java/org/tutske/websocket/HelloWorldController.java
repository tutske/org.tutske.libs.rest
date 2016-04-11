package org.tutske.websocket;


public class HelloWorldController {

	public static RestObject get (HttpRequest request) {
		return new RestObject () {{
			v ("method", "GET");
			v ("greeting", "Hello World!");
		}};
	}

	public static RestObject post (HttpRequest request) {
		return new RestObject () {{
			v ("method", "POST");
			v ("greeting", "Hello World!");
		}};
	}

}
