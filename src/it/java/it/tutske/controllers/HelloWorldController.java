package it.tutske.controllers;

import org.tutske.rest.data.RestArray;
import org.tutske.rest.data.RestObject;
import org.tutske.rest.HttpRequest;
import org.tutske.rest.exceptions.ResponseException;

import java.util.Random;


public class HelloWorldController {

	private final Random random = new Random ();

	public RestObject get (HttpRequest request) {
		return new RestObject ("response") {{
			v ("method", "GET");
			v ("greeting", "Hello World!");
		}};
	}

	public RestObject post (HttpRequest request) throws Exception {
		throw new ResponseException ("We suffered an error that can not be recoverd");
	}

	public RestObject readTempFile (HttpRequest request) {
		RestObject object = new RestObject ("response");

		String filename = request.pathParams ().get ("filename");
		String param = request.queryParams ().get ("test");

		object.put ("filename", filename == null ? "NULL" : filename);
		if ( param != null ) { object.put ("test", param); }

		return object;
	}

	public RestObject echo (HttpRequest request) {
		RestObject object  = new RestObject ("response") {{
			v ("body", list ());
		}};

		String body = request.getBody ();

		for ( String line : body.split ("\n") ) {
			object.getArray ("body").add (line);
		}

		return object;
	}

	public RestObject randoms (HttpRequest request) {
		Integer amount = request.queryParams ().get ("amount", Integer.class);
		if ( amount == null ) {
			throw new ResponseException ("We need an amount");
		}

		RestArray randoms = new RestArray ();

		for ( int i = 0; i < amount; i++ ) {
			randoms.add (random.nextFloat ());
		}

		return new RestObject ("response") {{
			v ("randoms", randoms);
		}};
	}

}
