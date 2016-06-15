package org.tutske.rest.data;

import static org.junit.Assert.assertThat;
import static org.tutske.rest.data.XmlUtils.*;

import com.google.gson.GsonBuilder;
import org.junit.Test;


public class ComplexXmlTest {

	@Test
	public void it_should_serialize_an_array_with_objects_that_only_have_attributes () throws Exception {
		RestStructure structure = new RestObject ("response") {{
			attribute ("processTime", "123ms");
			attribute ("cachable", true);

			v ("success", true);
			v ("meta", new RestObject () {{
				v ("_self", "/path/to/self");
				v ("type", "collection");
				v ("items", "#/response/users");
			}});
			v ("users", new RestArray (".", "user") {{
				v (new RestObject () {{
					attribute ("name", "Jhon Doe");
					attribute ("age", 24);
				}}, new RestObject () {{
					attribute ("name", "Jane Doe");
					attribute ("age", 24);
				}});
			}});
			v ("links", new RestArray (".", "link") {{
				v (new RestObject () {{
					v ("href", "/path/to/resources");
				}});
				v (new RestObject () {{
					v ("href", "/path/to/resources/xxx");
				}});
				v (new RestObject () {{
					v ("href", "/path/to/resources/yyy");
				}});
			}});
		}};

		System.out.println (new GsonBuilder ().setPrettyPrinting ().create ().toJson (((RestObject)structure).asJson ()));

		assertThat (marshall (structure), matchesXml (""
			, "<response processTime=\"123ms\" cachable=\"true\">"
			, "	<success>true</success>"
			, "	<meta>"
			, "		<_self>/path/to/self</_self>"
			, "		<type>collection</type>"
			, "		<items>#/response/users</items>"
			, "	</meta>"
			, "	<users>"
			, "		<user name=\"Jhon Doe\" age=\"24\" />"
			, "		<user name=\"Jane Doe\" age=\"24\" />"
			, "	</users>"
			, "	<links>"
			, "		<link>"
			, "			<href>/path/to/resources</href>"
			, "		</link>"
			, "		<link>"
			, "			<href>/path/to/resources/xxx</href>"
			, "		</link>"
			, "		<link>"
			, "			<href>/path/to/resources/yyy</href>"
			, "		</link>"
			, "	</links>"
			, "</response>"
		));
	}
}
