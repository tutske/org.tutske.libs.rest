package org.tutske.controllers;

import org.tutske.rest.objects.RestArray;
import org.tutske.rest.objects.RestObject;
import org.tutske.websocket.HttpRequest;
import org.tutske.websocket.ResponseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Random;
import java.util.Scanner;


public class HelloWorldController {

	public static RestObject get (HttpRequest request) {
		return new RestObject () {{
			v ("method", "GET");
			v ("greeting", "Hello World!");
		}};
	}

	public static RestObject post (HttpRequest request) throws Exception {
		if ( new Random ().nextFloat () < 0.5 ) {
			throw new ResponseException ("We suffered an error that can not be recoverd");
		}
		return new RestObject () {{
			v ("method", "POST");
			v ("greeting", "Hello World!");
		}};
	}

	public static RestObject fail (HttpRequest request) throws FileNotFoundException {
		InputStream stream = new FileInputStream (new File ("/tmp/example.txtt"));
		Scanner scanner = new Scanner (stream);

		RestObject result = new RestObject () {{
			v ("lines", list ());
		}};

		while (scanner.hasNextLine ()) {
			result.merge (new RestObject () {{
				v ("lines", array (scanner.nextLine ()));
			}});
		}

		return result;
	}

}
