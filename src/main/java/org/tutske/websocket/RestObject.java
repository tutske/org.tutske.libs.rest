package org.tutske.websocket;

import java.util.*;


public class RestObject {
	private final Map<String, Object> data = new LinkedHashMap<String, Object> ();

	public void v (String key, String value) {
		data.put (key, value);
	}

	public void v (String key, Number value) {
		data.put (key, value);
	}

	public void v (String key, Boolean value) {
		data.put (key, value);
	}

	public void v (String key, List<?> value) {
		data.put (key, value);
	}

	public void v (String key, RestObject value) {
		data.put (key, value.asJson ());
	}

	public RestObject merge (RestObject source) {
		merge (data, source.data);
		return this;
	}

	public <T> List<T> list (T ... elements) {
		return array (elements);
	}

	public <T> List<T> array (T ... elements) {
		List<T> result = new LinkedList<T> ();
		Collections.addAll (result, elements);
		return result;
	}

	public Object asJson () {
		return data;
	}

	private void merge (Map<String, Object> target, Map<String, Object> source) {
		for ( String key : source.keySet () ) {
			Object sourceValue = source.get (key);
			Object targetValue = target.get (key);

			if ( targetValue == null ) {
				if ( sourceValue instanceof Map ) {
					targetValue = new LinkedHashMap<String, Object> ();
				} else if ( sourceValue instanceof List ) {
					targetValue = list ();
				}
				target.put (key, targetValue);
			}

			if ( targetValue instanceof Map ) {
				if ( ! (sourceValue instanceof Map) ) {
					throw new RuntimeException ();
				}
				merge ((Map<String, Object>) targetValue, (Map<String, Object>) sourceValue);
			} else if ( targetValue instanceof List ) {
				if ( ! (sourceValue instanceof List) ) {
					throw new RuntimeException ();
				}
				merge ((List<Object>) targetValue, (List<Object>) sourceValue);
			} else {
				target.put (key, sourceValue);
			}
		}
	}

	private void merge (List<Object> target, List<Object> source) {
		for ( Object sourceValue : source ) {
			if ( sourceValue instanceof Map ) {
				Map<String, Object> targetValue = new LinkedHashMap<String, Object> ();
				merge (targetValue, (Map<String, Object>) sourceValue);
				target.add (targetValue);
			} else if ( sourceValue instanceof List) {
				List<Object> targetValue = list ();
				merge (targetValue, (List<Object>) sourceValue);
				target.add (targetValue);
			} else {
				target.add( sourceValue);
			}
		}
	}
}
