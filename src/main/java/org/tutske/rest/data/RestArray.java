package org.tutske.rest.data;

import java.util.*;
import java.util.Map.Entry;


public class RestArray extends LinkedList<Object> implements RestStructure {

	private String tag;
	private String childTag;
	private final Map<String, Object> attributes = new LinkedHashMap<String, Object> ();

	public RestArray () {
	}

	public RestArray (String tag) {
		this (tag, null);
	}

	public RestArray (String tag, String childTag) {
		this.tag = tag;
		this.childTag = childTag;
	}

	protected void tag (String tagname) {
		this.tag = tagname;
	}

	protected void childTag (String tagname) {
		this.childTag = tagname;
	}

	protected void attribute (String name, Object value) {
		RestUtil.assurePrimitive (value);
		attributes.put (name, value);
	}

	public String getTag () {
		return tag;
	}

	public String getChildTag () {
		return childTag;
	}

	public Map<String, Object> getAttributes () {
		return attributes;
	}

	protected RestArray v (Object ... objects) {
		for ( Object object : objects ) {
			add (object);
		}
		return this;
	}

	@Override
	public RestStructure asRestStructure () {
		if ( attributes.isEmpty () ) {
			return new RestArray ().merge (this);
		}

		RestObject object = new RestObject ();
		for ( Entry<String, Object> attribute : attributes.entrySet ()) {
			object.attribute (attribute.getKey (), attribute.getValue ());
		}
		object.put ("items", new RestArray ().merge (this));

		return object;
	}

	@Override
	public boolean add (Object o) {
		RestUtil.assureValid (o);
		return super.add (o);
	}

	@Override
	public void add (int index, Object element) {
		RestUtil.assureValid (element);
		super.add (index, element);
	}

	@Override
	public Object set (int index, Object element) {
		RestUtil.assureValid (element);
		return super.set (index, element);
	}

	@Override
	public boolean addAll (Collection<?> c) {
		c.forEach (RestUtil::assureValid);
		return super.addAll (c);
	}

	@Override
	public boolean addAll (int index, Collection<?> c) {
		c.forEach (RestUtil::assureValid);
		return super.addAll (c);
	}

	@Override
	public boolean retainAll (Collection<?> c) {
		c.forEach (RestUtil::assureValid);
		return super.retainAll (c);
	}

	@Override
	public Object get (int index) {
		return super.get (index);
	}

	public RestObject getObject (int index) {
		return (RestObject) get (index);
	}

	public RestArray getArray (int index) {
		return (RestArray) get (index);
	}

	public Number getNumber (int index) {
		return (Number) get (index);
	}

	public String getString (int index) {
		return (String) get (index);
	}

	public Boolean getBoolean (int index) {
		return (Boolean) get (index);
	}

	public RestArray merge (RestArray source) {
		if ( source.getTag () != null ) {
			this.tag (source.getTag ());
		}
		if ( source.getChildTag () != null && ! source.getChildTag ().equals (".") ) {
			this.childTag (source.getChildTag ());
		}

		for ( Entry<String, Object> attribute : source.getAttributes ().entrySet () ) {
			this.attribute (attribute.getKey (), attribute.getValue ());
		}

		for ( Object object : source ) {
			if ( object instanceof RestObject ) {
				this.add (new RestObject ().merge ((RestObject) object));
			} else if ( object instanceof RestArray ) {
				this.add (new RestArray ().merge ((RestArray) object));
			} else {
				add (object);
			}
		}
		return this;
	}

}
