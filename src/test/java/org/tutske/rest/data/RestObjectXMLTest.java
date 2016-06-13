package org.tutske.rest.data;

import static org.junit.Assert.assertThat;
import static org.tutske.rest.data.XmlUtils.*;

import org.junit.Ignore;
import org.junit.Test;


public class RestObjectXMLTest {

	@Test (expected = RuntimeException.class)
	@Ignore
	public void it_should_complain_when_the_root_object_has_no_tag () throws Exception {
		marshall (new RestObject ());
	}

	@Test
	@Ignore
	public void it_should_marshall_empty_objects () throws Exception {
		RestObject object = new RestObject () {{
			tag ("response");
		}};
		assertThat (marshall (object), matchesXml ("<response />"));
	}

	@Test
	@Ignore
	public void it_should_marshall_empty_objects_with_constructor_tag () throws Exception {
		RestObject object = new RestObject ("response") {{
		}};
		assertThat (marshall (object), matchesXml ("<response />"));
	}

	@Test
	@Ignore
	public void it_should_marshall_simple_objects () throws Exception {
		RestObject object = new RestObject () {{
			tag ("response");
			v ("key", "value");
		}};

		assertThat (marshall (object), matchesXml (""
			, "<response>"
			, "	<key>value</key>"
			, "</response>"
		));
	}

	@Test
	@Ignore
	public void it_should_marshall_objects_with_attributes () throws Exception {
		RestObject object = new RestObject () {{
			tag ("response");
			attribute ("original", "/path/to/resource");
		}};

		assertThat (marshall (object), matchesXml (""
			, "<response original=\"/path/to/resource\" />"
		));
	}

	@Test
	@Ignore
	public void it_should_marshall_un_tagged_nested_objects_with_attributes () throws Exception {
		RestObject object = new RestObject () {{
			tag ("response");
			v ("person", new RestObject () {{
				attribute ("name", "Jhon Doe");
			}});
		}};

		assertThat (marshall (object), matchesXml (""
			, "<response>"
			, "	<person name=\"Jhon Doe\" />"
			, "</response>"
		));
	}

	@Test
	@Ignore
	public void it_should_marshall_tagged_nested_objects_with_attributes () throws Exception {
		RestObject object = new RestObject () {{
			tag ("response");
			v ("user", new RestObject () {{
				tag ("person");
				attribute ("name", "Jhon Doe");
			}});
		}};

		assertThat (marshall (object), matchesXml (""
			, "<response>"
			, "	<user>"
			, "		<person name=\"Jhon Doe\" />"
			, "	</user>"
			, "</response>"
		));
	}
}
