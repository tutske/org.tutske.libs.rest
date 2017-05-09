package org.tutske.rest.exceptions;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.tutske.rest.data.RestObject;


public class ResponseExceptionTest {

	@Test
	public void it_should_be_able_to_convert_to_a_rest_structure () {
		RestObject result = (RestObject) new ResponseException ().asRestStructure ();
		assertThat ((String) result.get ("title"), containsString ("Internal Server Error"));
	}

	@Test
	public void it_should_retain_extra_values_for_the_exception () {
		ResponseException exception = new ResponseException ();
		exception.addExtra (new RestObject () {{
			v ("extra", "value");
		}});

		RestObject result = (RestObject) exception.asRestStructure ();
		assertThat (result.get ("extra"), is ("value"));
	}

}
