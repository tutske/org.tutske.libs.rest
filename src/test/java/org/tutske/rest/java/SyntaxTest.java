package org.tutske.rest.java;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;


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
}
