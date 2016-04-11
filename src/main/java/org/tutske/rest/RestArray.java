package org.tutske.rest;

import java.util.*;


public class RestArray extends LinkedList<Object> {

	protected RestArray v (Object ... objects ) {
		for ( Object object : objects ) {
			add (object);
		}
		return this;
	}

	public Object asJson () {
		return new RestArray ().merge (this);
	}

	@Override
	public boolean add (Object o) {
		assureValid (o);
		return super.add (o);
	}

	@Override
	public void add (int index, Object element) {
		assureValid (element);
		super.add (index, element);
	}

	@Override
	public Object set (int index, Object element) {
		assureValid (element);
		return super.set (index, element);
	}

	@Override
	public boolean addAll (Collection<?> c) {
		c.forEach (this::assureValid);
		return super.addAll (c);
	}

	@Override
	public boolean addAll (int index, Collection<?> c) {
		c.forEach (this::assureValid);
		return super.addAll (c);
	}

	@Override
	public boolean retainAll (Collection<?> c) {
		c.forEach (this::assureValid);
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
		for ( Object object : source ) {
			if ( object instanceof RestObject ) {
				RestObject created = new RestObject ().merge ((RestObject) object);
			} else if ( object instanceof RestArray ) {
				RestArray created = new RestArray ().merge ((RestArray) object);
			} else {
				add (object);
			}
		}
		return this;
	}

	/** utility **/

	private void assureValid (Object object) {
		if (
			(object instanceof RestArray) || (object instanceof RestObject) ||
			(object instanceof Boolean) || (object instanceof Number)
		) {
			return;
		}

		throw new RuntimeException ("Not a valid object: " + object);
	}

}
