package org.tutske.rest.data;

import static org.junit.Assert.assertThat;
import static org.tutske.rest.data.XmlUtils.*;

import org.junit.Ignore;
import org.junit.Test;


public class ComplexArrayObjectXmlTest {

	@Test
	public void it_should_serialize_an_array_of_un_tagged_objects () throws Exception {
		RestArray array = new RestArray ("users", "user") {{
			v (new RestObject () {{
				v ("name", "Jhon Doe");
				v ("age", 24);
			}}, new RestObject () {{
				v ("name", "Jane Doe");
				v ("age", 24);
			}});
		}};

		assertThat (marshall (array), matchesXml (""
			, "<users>"
			, "	<user>"
			, "		<name>Jhon Doe</name>"
			, "		<age>24</age>"
			, "	</user>"
			, "	<user>"
			, "		<name>Jane Doe</name>"
			, "		<age>24</age>"
			, "	</user>"
			, "</users>"
		));
	}

	@Test
	public void it_should_serialize_an_array_of_tagged_objects_with_childtag_configured () throws Exception {
		RestArray array = new RestArray () {{
			tag ("users");
			childTag ("user");
			v (new RestObject () {{
				tag ("person");
				v ("name", "Jhon Doe");
				v ("age", 24);
			}}, new RestObject () {{
				tag ("person");
				v ("name", "Jane Doe");
				v ("age", 24);
			}});
		}};

		assertThat (marshall (array), matchesXml (""
			, "<users>"
			, "	<user>"
			, "		<person>"
			, "			<name>Jhon Doe</name>"
			, "			<age>24</age>"
			, "		</person>"
			, "	</user>"
			, "	<user>"
			, "		<person>"
			, "			<name>Jane Doe</name>"
			, "			<age>24</age>"
			, "		</person>"
			, "	</user>"
			, "</users>"
		));
	}

	@Test
	public void it_should_serialize_an_array_of_un_tagged_objects_when_no_child_tag_given () throws Exception {
		RestArray array = new RestArray () {{
			tag ("users");
			v (new RestObject () {{
				v ("name", "Jhon Doe");
				v ("age", 24);
			}}, new RestObject () {{
				v ("name", "Jane Doe");
				v ("age", 24);
			}});
		}};

		assertThat (marshall (array), matchesXml (""
			, "<users>"
			, "	<name>Jhon Doe</name>"
			, "	<age>24</age>"
			, "	<name>Jane Doe</name>"
			, "	<age>24</age>"
			, "</users>"
		));
	}

	@Test (expected = RuntimeException.class)
	public void it_should_complain_when_an_object_has_attributes_but_no_name () throws Exception {
		RestArray array = new RestArray ("users") {{
			v (new RestObject () {{
				attribute ("name", "John Doe");
			}});
		}};
		marshall (array);
	}

}
