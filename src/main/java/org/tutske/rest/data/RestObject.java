package org.tutske.rest.data;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;


public class RestObject extends LinkedHashMap<String, Object> {

	public RestObject () {
	}

	public RestObject (String name) {
	}

	protected void tag (String tag) {
	}

	protected void attribute (String attribute, Object value) {
	}

	protected void v (String key, Object value) {
		put (key, value);
	}

	protected RestArray list (Object ... objects) {
		RestArray arr = new RestArray ();
		Collections.addAll (arr, objects);
		return arr;
	}

	public Object asJson () {
		return new RestObject ().merge (this);
	}

	@Override
	public Object put (String key, Object value) {
		RestUtil.assureValid (value);
		return super.put (key, value);
	}

	@Override
	public void putAll (Map<? extends String, ?> m) {
		m.forEach ((key, value) -> { RestUtil.assureValid (value); });
		super.putAll (m);
	}

	@Override
	public Object putIfAbsent (String key, Object value) {
		RestUtil.assureValid (value);
		return super.putIfAbsent (key, value);
	}

	@Override
	public boolean replace (String key, Object oldValue, Object newValue) {
		RestUtil.assureValid (newValue);
		return super.replace (key, oldValue, newValue);
	}

	@Override
	public Object replace (String key, Object value) {
		RestUtil.assureValid (value);
		return super.replace (key, value);
	}

	@Override
	public Object getOrDefault (Object key, Object defaultValue) {
		return super.getOrDefault (key, defaultValue);
	}

	@Override
	public Object get (Object key) {
		return super.get (key);
	}

	public Object get (String ... path) {
		return lookup (this, path);
	}

	public RestObject getObject (String ... path) {
		return (RestObject) lookup (this, path);
	}

	public RestArray getArray (String ... path) {
		return (RestArray) lookup (this, path);
	}

	public Number getNumber (String ... path) {
		return (Number) lookup (this, path);
	}

	public String getString (String ... path) {
		return (String) lookup (this, path);
	}

	public Boolean getBoolean (String ... path) {
		return (Boolean) lookup (this, path);
	}

	public RestObject merge (RestObject source) {
		for ( String key : source.keySet () ) {
			Object sourceValue = source.get (key);
			Object targetValue = super.get (key);

			if ( (sourceValue instanceof RestObject) && targetValue == null ) {
				targetValue = new RestObject ();
			}
			if ( (sourceValue instanceof RestArray) && targetValue == null ) {
				targetValue = new RestArray ();
			}

			if (
				( (sourceValue instanceof RestObject) && ! (targetValue instanceof RestObject) ) ||
				( (sourceValue instanceof RestArray) && ! (targetValue instanceof RestArray) )
			) {
				throw new RuntimeException ("Could not merge objects");
			}

			if ( targetValue == null ) {
				targetValue = sourceValue;
			} else if ( sourceValue instanceof RestObject ) {
				((RestObject) targetValue).merge ((RestObject) sourceValue);
			} else if ( sourceValue instanceof RestArray ) {
				((RestArray) targetValue).merge ((RestArray) sourceValue);
			}

			super.put (key, targetValue);
		}
		return this;
	}

	/** disable thesee **/

	@Override
	public Object merge (String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
		throw new RuntimeException ("Operation not supported");
	}

	@Override
	public void replaceAll (BiFunction<? super String, ? super Object, ?> function) {
		throw new RuntimeException ("Operation not supported");
	}

	@Override
	public Object computeIfAbsent (String key, Function<? super String, ?> mappingFunction) {
		throw new RuntimeException ("Operation not supported");
	}

	@Override
	public Object computeIfPresent (String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
		throw new RuntimeException ("Operation not supported");
	}

	@Override
	public Object compute (String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
		throw new RuntimeException ("Operation not supported");
	}

	/** utility **/

	private Object lookup (RestObject data, String ... path) {
		RestObject current = data;
		for ( int i = 0; i < path.length - 1; i++ ) {
			current = current.getObject (path[i]);
		}
		return current.get (path[path.length - 1]);
	}

}
