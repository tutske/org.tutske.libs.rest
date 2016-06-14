package org.tutske.rest.data;

import static org.junit.Assert.assertThat;
import static org.tutske.rest.data.XmlUtils.*;

import org.junit.Ignore;
import org.junit.Test;


public class ComplexObjectArrayXmlTest {

	@Test
	public void it_should_honor_child_tag_on_nested_array () throws Exception {
		RestObject object = new RestObject ("response") {{
			v ("names", new RestArray (".", "name") {{
				v ("John", "jr", "Doe");
			}});
		}};

		assertThat (marshall (object), matchesXml (""
			, "<response>"
			, "	<names>"
			, "		<name>John</name>"
			, "		<name>jr</name>"
			, "		<name>Doe</name>"
			, "	</names>"
			, "</response>"
		));
	}
}
