package org.tutske.rest.data;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.tutske.rest.internals.Serializer;
import org.tutske.rest.internals.XmlSerializer;


public class XmlUtils {

	private final static Serializer serializer = new XmlSerializer ();

	public static String marshall (RestStructure structure) throws Exception {
		return serializer.serialize (structure);
	}

	public static Matcher<String> matchesXml (final String ... parts) {
		return new BaseMatcher<String> () {
			@Override public boolean matches (Object object) {
				if ( ! (object instanceof String) ) {
					return false;
				}

				String xml = (String) object;

				for ( String part : parts ) {
					if ( ! xml.contains (part.trim ())) {
						return false;
					}
				}
				return true;
			}
			@Override public void describeTo (Description description) {
				String xml = "";
				for ( String part : parts) { xml += part + "\n"; }

				description.appendText ("xml matching");
				description.appendText (xml.substring (0, xml.length () - 1));
			}
		};
	}

	@Test
	public void it_should_match_dense_xml () {
		String xml = "<?xml version=\"1.0\"><response><key>value</key></response>";
		assertThat (xml, matchesXml (""
			, "<response>"
			, "	<key>value</key>"
			, "</response>"
		));
	}

	@Test
	public void it_should_match_pretty_printed_xml () {
		String xml = "" +
			"<?xml version=\"1.0\">\n" +
			"<response>\n" +
			"	<key>value</key>\n" +
			"</response>\n";

		assertThat (xml, matchesXml (""
			, "<response>"
			, "	<key>value</key>"
			, "</response>"
		));
	}

	@Test
	public void it_should_not_match_wrong_xml_input () {
		String xml = "<?xml version=\"1.0\"><cats />";
		assertThat (xml, not (matchesXml ("<dogs />")));
	}

}
