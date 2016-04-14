package org.tutske.rest;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;


public class ParameterBag implements Map<String, String> {

	private final Map<String, List<String>> data = new LinkedHashMap<String, List<String>> ();

	@Override
	public int size () {
		return data.size ();
	}

	@Override
	public boolean isEmpty () {
		return data.isEmpty ();
	}

	@Override
	public boolean containsKey (Object key) {
		return data.containsKey (key);
	}

	@Override
	public boolean containsValue (Object value) {
		if ( value == null ) {
			return false;
		}
		for ( List<String> vals : data.values () ) {
			if ( vals.contains (value) ) {
				return true;
			}
		}
		return false;
	}

	public ParameterBag add (String key, String... values) {
		return addAll (key, Arrays.asList (values));
	}

	public ParameterBag addAll (String key, Collection<String> values) {
		List<String> retrieved = data.get (key);
		if ( retrieved == null ) {
			retrieved = new LinkedList<String> ();
			data.put (key, retrieved);
		}
		retrieved.addAll (values);
		return this;
	}

	public void addAll (Map<? extends String, ? extends String> m) {
		m.forEach (this::add);
	}

	public void addAll (ParameterBag bag) {
		bag.data.forEach (this::addAll);
	}

	@Override
	public boolean replace (String key, String oldValue, String newValue) {
		List<String> values = data.get (key);
		if ( values == null || values.isEmpty () ) {
			return false;
		}
		int index = values.indexOf (oldValue);
		if ( index < 0 ) { return false; }
		values.set (index, newValue);
		return true;
	}

	@Override
	public String get (Object key) {
		return get (key, String.class);
	}

	public <T> T get (Object key, Class<T> clazz) {
		List<String> values = data.get (key);

		if ( values == null || values.isEmpty () ) {
			return null;
		}

		try {
			return PrimitivesParser.parse (values.get (0), clazz);
		} catch ( NumberFormatException excetion ) {
			throw new WrongValueException (
				"The value is not of the right type.",
				new RestObject () {{
					v ("value", values.get (0));
					v ("type", clazz.getName ());
				}}
			);
		}
	}

	public Set<String> getAll (Object key) {
		List<String> values = data.get (key);
		if ( values == null ) {
			return Collections.emptySet ();
		}
		return new HashSet<String> (values);
	}

	@Override
	public String replace (String key, String value) {
		String current = get (key);
		boolean success = replace (key, current, value);
		return success ? current : null;
	}

	@Override
	public String put (String key, String value) {
		if ( ! containsKey (key) ) {
			add (key, value);
			return null;
		} else {
			List<String> values = data.get (key);
			String current = values.isEmpty () ? null : values.get (0);
			values.add (0, value);
			return current;
		}
	}

	@Override
	public void putAll (Map<? extends String, ? extends String> m) {
		m.forEach (this::put);
	}

	@Override
	public void clear () {
		data.clear ();
	}

	public void clear (String key) {
		data.remove (key);
	}

	@Override
	public String remove (Object key) {
		List<String> values = data.get (key);
		if ( values == null || values.isEmpty () ) {
			return null;
		}
		String removed = values.remove (0);
		if ( values.isEmpty () ) {
			data.remove (key);
		}
		return removed;
	}

	@Override
	public boolean remove (Object key, Object value) {
		List<String> values = data.get (key);
		if ( values == null || values.isEmpty () ) {
			return false;
		}
		boolean success = values.remove (value);
		if ( success && values.isEmpty () ) {
			data.remove (key);
		}
		return success;
	}

	@Override
	public Set<String> keySet () {
		return data.keySet ();
	}

	@Override
	public Collection<String> values () {
		Set<String> values = new HashSet<String> ();
		data.forEach ((key, value) -> {
			if ( value == null || value.isEmpty () ) {
				return;
			}
			values.add (value.get (0));
		});
		return values;
	}

	@Override
	public Set<Entry<String, String>> entrySet () {
		Set<Entry<String, String>> entries = new HashSet<Entry<String, String>> ();
		data.forEach ((key, value) -> {
			if ( value == null || value.isEmpty () ) {
				return;
			}
			entries.add (new Entry<String, String> () {
				@Override
				public String getKey () {
					return key;
				}

				@Override
				public String getValue () {
					return value.get (0);
				}

				@Override
				public String setValue (String value) {
					String current = data.get (key).get (0);
					data.get (key).add (0, value);
					return current;
				}
			});
		});
		return entries;
	}

	@Override
	public String getOrDefault (Object key, String defaultValue) {
		if ( ! containsKey (key) ) {
			return defaultValue;
		}
		return get (key);
	}

	@Override
	public void forEach (BiConsumer<? super String, ? super String> action) {
		data.forEach ((key, values) -> {
			values.forEach ((value) -> {
				action.accept (key, value);
			});
		});
	}

	@Override
	public void replaceAll (BiFunction<? super String, ? super String, ? extends String> function) {
		data.forEach ((key, values) -> {
			values.replaceAll ((value) -> {
				return function.apply (key, value);
			});
		});
	}

	@Override
	public String putIfAbsent (String key, String value) {
		if ( containsKey (key) ) {
			return get (key);
		}
		put (key, value);
		return null;
	}

	@Override
	public String computeIfAbsent (String key, Function<? super String, ? extends String> mappingFunction) {
		return null;
	}

	@Override
	public String computeIfPresent (String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
		return null;
	}

	@Override
	public String compute (String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
		return null;
	}

	@Override
	public String merge (String key, String value, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
		return null;
	}
}
