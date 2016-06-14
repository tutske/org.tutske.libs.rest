package org.tutske.rest.java;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.tutske.rest.data.RestObject;


public class SyntaxTest {

	private static abstract class Subject {
		private final String name;
		Subject (String name) {
			this.name = name;
		}
		public String getName () {
			return this.name;
		}
	}

	@Test
	public void it_should_construct_with_the_right_properties () {
		Subject subject = new Subject ("Jhon") {{
		}};

		assertThat (subject.getName (), is ("Jhon"));
	}

	@Test
	public void test_if_we_can_turn_json_into_rest_objects () {
		String json = "{\"name\": \"John\"}";
		Gson gson = new GsonBuilder ().create ();
		RestObject object = gson.fromJson (json, RestObject.class);

		assertThat (object.get ("name"), is ("John"));
	}

}
