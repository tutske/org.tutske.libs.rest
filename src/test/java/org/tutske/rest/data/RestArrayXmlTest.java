package org.tutske.rest.data;

import static org.junit.Assert.assertThat;
import static org.tutske.rest.data.XmlUtils.*;

import org.junit.Ignore;
import org.junit.Test;


public class RestArrayXmlTest {

	@Test (expected = RuntimeException.class)
	@Ignore
	public void it_should_complain_when_the_root_array_has_no_tag () throws Exception {
		marshall (new RestArray ());
	}

	@Test
	@Ignore
	public void it_should_marshall_empty_arrays () throws Exception {
		RestArray array = new RestArray () {{
			tag ("users");
		}};
		assertThat (marshall (array), matchesXml ("<users />"));
	}

	@Test
	@Ignore
	public void it_should_marshall_empty_arrays_with_constructor_tag () throws Exception {
		RestArray array = new RestArray ("users") {{
		}};
		assertThat (marshall (array), matchesXml ("<users />"));
	}

	@Test
	@Ignore
	public void it_should_serialize_shallow_arrays_without_attributes () throws Exception {
		RestArray array = new RestArray () {{
			tag ("names");
			childTag ("name");
			v ("Jhon", "jr", "Doe");
		}};

		assertThat (marshall (array), matchesXml (""
			, "<names>"
			, "	<name>Jhon</name>"
			, "	<name>jr</name>"
			, "	<name>Doe</name>"
			, "</names>"
		));
	}

	@Test
	public void it_should_serialize_shallow_arrays_with_constructor_child_tags () throws Exception {
		RestArray array = new RestArray ("names", "name") {{
			v ("Jhon", "jr", "Doe");
		}};

		assertThat (marshall (array), matchesXml (""
			, "<names>"
			, "	<name>Jhon</name>"
			, "	<name>jr</name>"
			, "	<name>Doe</name>"
			, "</names>"
		));
	}

	@Test
	@Ignore
	public void it_should_serialize_shallow_arrays_without_attributes_or_childTag () throws Exception {
		RestArray array = new RestArray () {{
			tag ("names");
			v ("Jhon", "jr", "Doe");
		}};

		assertThat (marshall (array), matchesXml (""
			, "<names>"
			, "	Jhon"
			, "	jr"
			, "	Doe"
			, "</names>"
		));
	}

	@Test
	@Ignore
	public void it_should_serialize_shallow_arrays_with_attributes () throws Exception {
		RestArray array = new RestArray () {{
			tag ("names");
			attribute ("complete", false);
		}};

		assertThat (marshall (array), matchesXml (""
			, "<names complete=\"false\" />"
		));
	}

}
