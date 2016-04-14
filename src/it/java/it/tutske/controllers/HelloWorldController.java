package it.tutske.controllers;

import org.tutske.rest.RestArray;
import org.tutske.rest.RestObject;
import org.tutske.rest.HttpRequest;
import org.tutske.rest.ResponseException;

import java.util.Random;


public class HelloWorldController {

	private final Random random = new Random ();

	public RestObject get (HttpRequest request) {
		return new RestObject () {{
			v ("method", "GET");
			v ("greeting", "Hello World!");
		}};
	}

	public RestObject post (HttpRequest request) throws Exception {
		if ( random.nextFloat () < 0.5 ) {
			throw new ResponseException ("We suffered an error that can not be recoverd");
		}
		return new RestObject () {{
			v ("method", "POST");
			v ("greeting", "Hello World!");
		}};
	}

	public RestObject readTempFile (HttpRequest request) {
		RestObject object = new RestObject ();

		String filename = request.pathParams ().get ("filename");
		String param = request.queryParams ().get ("test");

		object.put ("filename", filename == null ? "NULL" : filename);
		if ( param != null ) { object.put ("test", param); }

		return object;
	}

	public RestObject echo (HttpRequest request) {
		RestObject object  = new RestObject () {{
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

		Random random = new Random ();
		RestArray randoms = new RestArray ();

		for ( int i = 0; i < amount; i++ ) {
			randoms.add (random.nextFloat ());
		}

		return new RestObject () {{
			v ("randoms", randoms);
		}};
	}

}
