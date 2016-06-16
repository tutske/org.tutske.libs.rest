package org.tutske.rest.internals;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.tutske.rest.data.RestObject;
import org.tutske.rest.data.RestStructure;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;


public class ContentSerializerTest {

	private static final Serializer json = new JsonSerializer ();
	private static final Serializer xml = new XmlDomSerializer ();

	@Test (expected = RuntimeException.class)
	public void it_should_complain_when_it_does_not_know_how_to_serialize_to_requested_type () {
		ContentSerializer serializer = new ContentSerializer (new HashMap<> ());
		serializer.contentType ("application/json");
	}

	@Test
	public void it_should_fall_back_to_the_default_type_only_unknown_types_are_requested () {
		ContentSerializer serializer = new ContentSerializer ("application/json", new HashMap<String, Serializer> () {{
			put ("application/json", json);
		}});
		String result = serializer.serialize ("application/json", new RestObject ());
		assertThat (result, is ("{}"));
	}

	@Test
	public void it_should_serialize_based_on_accept_header () {
		ContentSerializer serializer = new ContentSerializer (new HashMap<String, Serializer> () {{
			put ("application/json", json);
		}});
		String result = serializer.serialize ("application/json", new RestObject ());
		assertThat (result, is ("{}"));
	}

	@Test
	public void it_should_serialize_to_a_writer () {
		ContentSerializer serializer = new ContentSerializer (new HashMap<String, Serializer> () {{
			put ("application/json", json);
		}});

		StringWriter writer = new StringWriter ();
		serializer.serialize ("application/json", new RestObject (), writer);

		assertThat (writer.toString (), is ("{}"));
	}

	@Test
	public void it_should_tell_the_content_type_of_the_serialization () {
		ContentSerializer serializer = new ContentSerializer (new HashMap<String, Serializer> () {{
			put ("application/json", json);
		}});
		String contentType = serializer.contentType ("application/json");
		assertThat (contentType, is ("application/json"));
	}

	@Test (expected = RuntimeException.class)
	public void it_should_complain_when_no_accept_header_is_given_and_no_default_is_configured () {
		ContentSerializer serializer = new ContentSerializer ();
		serializer.contentType ("");
	}

	@Test (expected = RuntimeException.class)
	public void it_should_complain_when_null_accept_header_is_given_and_no_default_is_configured () {
		ContentSerializer serializer = new ContentSerializer ();
		serializer.contentType (null);
	}

	@Test
	public void it_should_pick_something_that_it_knows_how_to_handle () {
		ContentSerializer serializer = new ContentSerializer (new HashMap<String, Serializer> () {{
			put ("application/json", json);
		}});
		String contentType = serializer.contentType ("application/json; q=0.2, appliction/xml; q=0.3");
		assertThat (contentType, is ("application/json"));
	}

	@Test
	public void it_should_pick_something_that_it_knows_how_to_handle_2 () {
		ContentSerializer serializer = new ContentSerializer (new HashMap<String, Serializer> () {{
			put ("application/xml", xml);
		}});
		String contentType = serializer.contentType ("application/json; q=0.2, application/xml; q=0.3");
		assertThat (contentType, is ("application/xml"));
	}

	@Test
	public void it_should_pick_something_that_it_knows_how_to_handle_3 () {
		ContentSerializer serializer = new ContentSerializer (new HashMap<String, Serializer> () {{
			put ("application/xml", xml);
		}});
		String accept = "application/json; special=\";=,\", application/xml; q=0.3";
		String contentType = serializer.contentType (accept);
		assertThat (contentType, is ("application/xml"));
	}

	@Test
	public void it_should_pass_attributes_down_to_the_serializers () {
		ArgumentCaptor<Map<String, String>> attributes = (ArgumentCaptor) ArgumentCaptor.forClass (Map.class);
		Serializer jsonp = mock (Serializer.class);

		ContentSerializer serializer = new ContentSerializer (new HashMap<String, Serializer> () {{
			put ("application/javascript", jsonp);
		}});

		serializer.serialize ("application/javascript; callback=method", new RestObject ());

		verify (jsonp).serialize (any (), attributes.capture ());
		assertThat (attributes.getValue (), hasEntry ("callback", "method"));
	}

	@Test
	public void it_should_pass_empty_attributes_down_to_the_serializers_for_plain_content_type () {
		ArgumentCaptor<Map<String, String>> attributes = (ArgumentCaptor) ArgumentCaptor.forClass (Map.class);
		Serializer jsonp = mock (Serializer.class);

		ContentSerializer serializer = new ContentSerializer (new HashMap<String, Serializer> () {{
			put ("application/javascript", jsonp);
		}});

		serializer.serialize ("application/javascript", new RestObject ());

		verify (jsonp).serialize (any (), attributes.capture ());
		assertThat (attributes.getValue ().entrySet (), hasSize (0));
	}

	@Test
	public void it_should_pass_all_attributes_down_to_the_serializers () {
		ArgumentCaptor<Map<String, String>> attributes = (ArgumentCaptor) ArgumentCaptor.forClass (Map.class);
		Serializer jsonp = mock (Serializer.class);

		ContentSerializer serializer = new ContentSerializer (new HashMap<String, Serializer> () {{
			put ("application/javascript", jsonp);
		}});

		serializer.serialize (
			"application/javascript; callback=method; pretty=\"true\"",
			new RestObject ()
		);

		verify (jsonp).serialize (any (), attributes.capture ());
		assertThat (attributes.getValue (), hasEntry ("callback", "method"));
		assertThat (attributes.getValue (), hasEntry ("pretty", "true"));
	}

	@Test
	public void it_should_handle_quoted_attribute_values () {
		ArgumentCaptor<Map<String, String>> attributes = (ArgumentCaptor) ArgumentCaptor.forClass (Map.class);
		Serializer jsonp = mock (Serializer.class);

		ContentSerializer serializer = new ContentSerializer (new HashMap<String, Serializer> () {{
			put ("application/javascript", jsonp);
		}});

		serializer.serialize (
			"application/javascript; complex=\",;= are special\"; key=\"value\"",
			new RestObject ()
		);

		verify (jsonp).serialize (any (), attributes.capture ());
		assertThat (attributes.getValue (), hasEntry ("complex", ",;= are special"));
		assertThat (attributes.getValue (), hasEntry ("key", "value"));
	}

	@Test
	public void it_should_call_the_rigth_serializer_when_serializing_to_a_string () {
		Serializer jsonp = mock (Serializer.class);
		ContentSerializer serializer = new ContentSerializer (new HashMap<String, Serializer> () {{
			put ("application/json", json);
			put ("application/xml", xml);
			put ("application/javascript", jsonp);
		}});

		serializer.serialize ("application/javascript", new RestObject ());
		verify (jsonp).serialize (any (RestStructure.class), any (Map.class));
	}

	@Test
	public void it_should_call_the_rigth_serializer_when_serializing_to_a_writer () {
		Serializer jsonp = mock (Serializer.class);
		ContentSerializer serializer = new ContentSerializer (new HashMap<String, Serializer> () {{
			put ("application/json", json);
			put ("application/xml", xml);
			put ("application/javascript", jsonp);
		}});

		serializer.serialize ("application/javascript", new RestObject (), new StringWriter ());
		verify (jsonp).serialize (any (RestStructure.class), any (Map.class), any (Writer.class));
	}

}
