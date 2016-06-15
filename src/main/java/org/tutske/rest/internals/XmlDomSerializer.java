package org.tutske.rest.internals;

import org.tutske.rest.data.RestArray;
import org.tutske.rest.data.RestObject;
import org.tutske.rest.data.RestStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map.Entry;


public class XmlDomSerializer implements Serializer {

	private final DocumentBuilder builder;
	private final Transformer transformer;

	public XmlDomSerializer () {
		try {
			builder = DocumentBuilderFactory.newInstance ().newDocumentBuilder ();
			TransformerFactory factory = TransformerFactory.newInstance ();
			factory.setAttribute ("indent-number", 4);
			transformer = factory.newTransformer ();
			transformer.setOutputProperty (OutputKeys.INDENT, "yes");
		} catch (Exception exception) {
			throw new RuntimeException (exception);
		}
	}

	@Override
	public String serialize (RestStructure structure) {
		StringWriter writer = new StringWriter ();
		serialize (structure, writer);
		return writer.toString ().replaceAll ("/>", " />").replace ("?>", "?>\n");
	}

	@Override
	public void serialize (RestStructure structure, Writer writer) {
		serialize (builder.newDocument (), structure, writer);
	}

	private void serialize (Document document, RestStructure structure, Writer writer) {
		if ( structure.getTag () == null || structure.getTag ().equals (".") ) {
			throw new RuntimeException ("root element has no tag name");
		}

		Element xml = serialize (document, structure, structure.getTag ());

		try { transformer.transform (new DOMSource (xml), new StreamResult (writer)); }
		catch ( Exception exception) { throw new RuntimeException (exception); }
	}

	private Element toElement (Document document, RestStructure structure, String tag) {
		Element element = document.createElement (tag);
		for ( Entry<String, Object> entry : structure.getAttributes ().entrySet () ) {
			element.setAttribute (entry.getKey (), entry.getValue ().toString ());
		}
		return element;
	}

	private Element serialize (Document document, RestStructure structure, String tag) {
		Element root = null;
		Element element;

		if ( hasTag (structure) && ! structure.getTag ().equals (tag) ) {
			root = document.createElement (tag);
			tag = structure.getTag ();
		}

		element = toElement (document, structure, tag);
		serializeInto (element, document, structure);

		if ( root != null ) {
			root.appendChild (element);
		} else {
			root = element;
		}

		return root;
	}

	private Text serializePrimitive (Document document, Object value) {
		return document.createTextNode (value.toString ());
	}

	private void serializeInto (Element element, Document document, RestStructure structure) {
		if ( structure instanceof RestArray ) {
			serializeInto (element, document, (RestArray) structure);
		} else if ( structure instanceof RestObject ) {
			serializeInto (element, document, (RestObject) structure);
		} else {
			throw new RuntimeException ("structure is not an object or an array?");
		}
	}

	private void serializeInto (Element element, Document document, RestObject object) {
		for ( Entry<String, Object> entry : object.entrySet () ) {
			if ( "$attributes".equals (entry.getKey ()) ) {
				continue;
			}

			Object value = entry.getValue ();
			if ( value instanceof RestStructure ) {
				element.appendChild (serialize (document, (RestStructure) value, entry.getKey ()));
			} else {
				Element child = document.createElement (entry.getKey ());
				child.appendChild (serializePrimitive (document, value));
				element.appendChild (child);
			}
		}
	}

	private void serializeInto (Element element, Document document, RestArray array) {
		for ( Object value : array ) {
			if ( value instanceof RestStructure ) {
				RestStructure r = (RestStructure) value;
				String childtag = hasChildTag (array) ? array.getChildTag () : r.getTag ();

				if ( childtag == null && r.getAttributes ().size () > 0 ) {
					throw new RuntimeException ("No tag for nested structure");
				} else if ( childtag == null ) {
					serializeInto (element, document, r);
				} else {
					element.appendChild (serialize (document, (RestStructure) value, childtag));
				}
			} else {
				if ( ! hasChildTag (array) ) {
					element.appendChild (serializePrimitive (document, value));
				} else {
					Element child = document.createElement (array.getChildTag ());
					child.appendChild (serializePrimitive (document, value));
					element.appendChild (child);
				}
			}
		}
	}

	private boolean hasTag (RestStructure structure) {
		return structure.getTag () != null && ! structure.getTag ().equals (".");
	}

	private boolean hasChildTag (RestArray array) {
		return array.getChildTag () != null && ! array.getChildTag ().equals (".");
	}

}
