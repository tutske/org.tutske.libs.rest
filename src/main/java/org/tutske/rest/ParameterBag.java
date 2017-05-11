package org.tutske.rest;

import org.tutske.rest.data.RestObject;
import org.tutske.rest.exceptions.WrongValueException;
import org.tutske.rest.internals.PrimitivesParser;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;


public class ParameterBag<T> implements Map<String, T> {

	private final Map<String, List<T>> data = new LinkedHashMap<> ();

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
		for ( List<T> vals : data.values () ) {
			if ( vals.contains (value) ) {
				return true;
			}
		}
		return false;
	}

	public ParameterBag add (String key, T ... values) {
		return addAll (key, Arrays.asList (values));
	}

	public ParameterBag addAll (String key, Collection<? extends T> values) {
		List<T> retrieved = data.get (key);
		if ( retrieved == null ) {
			retrieved = new LinkedList<T> ();
			data.put (key, retrieved);
		}
		retrieved.addAll (values);
		return this;
	}

	public void addAll (Map<? extends String, ? extends T> m) {
		m.forEach (this::add);
	}

	public void addAll (ParameterBag<? extends T> bag) {
		bag.data.forEach (this::addAll);
	}

	@Override
	public boolean replace (String key, T oldValue, T newValue) {
		List<T> values = data.get (key);
		if ( values == null || values.isEmpty () ) {
			return false;
		}
		int index = values.indexOf (oldValue);
		if ( index < 0 ) { return false; }
		values.set (index, newValue);
		return true;
	}

	@Override
	public T get (Object key) {
		List<T> values = data.get (key);

		if ( values == null || values.isEmpty () ) {
			return null;
		}

		return values.get (0);
	}

	public <S> S getAs (Object key, Class<S> clazz) {
		return (S) get (key);
	}

	public <S> S converted (Object key, Class<S> clazz) {
		List<T> values = data.get (key);

		if ( values == null || values.isEmpty () ) {
			return null;
		}

		try {
			return PrimitivesParser.parse (values.get (0).toString (), clazz);
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

	public Set<T> getAll (Object key) {
		List<T> values = data.get (key);
		if ( values == null ) {
			return Collections.emptySet ();
		}
		return new HashSet<T> (values);
	}

	@Override
	public T replace (String key, T value) {
		T current = get (key);
		boolean success = replace (key, current, value);
		return success ? current : null;
	}

	@Override
	public T put (String key, T value) {
		if ( ! containsKey (key) ) {
			add (key, value);
			return null;
		} else {
			List<T> values = data.get (key);
			T current = values.isEmpty () ? null : values.get (0);
			values.add (0, value);
			return current;
		}
	}

	@Override
	public void putAll (Map<? extends String, ? extends T> m) {
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
	public T remove (Object key) {
		List<T> values = data.get (key);
		if ( values == null || values.isEmpty () ) {
			return null;
		}
		T removed = values.remove (0);
		if ( values.isEmpty () ) {
			data.remove (key);
		}
		return removed;
	}

	@Override
	public boolean remove (Object key, Object value) {
		List<T> values = data.get (key);
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
	public Collection<T> values () {
		Set<T> values = new HashSet<> ();
		data.forEach ((key, value) -> {
			if ( value == null || value.isEmpty () ) {
				return;
			}
			values.add (value.get (0));
		});
		return values;
	}

	@Override
	public Set<Entry<String, T>> entrySet () {
		Set<Entry<String, T>> entries = new HashSet<> ();
		data.forEach ((key, value) -> {
			if ( value == null || value.isEmpty () ) {
				return;
			}
			entries.add (new Entry<String, T> () {
				@Override public String getKey () {
					return key;
				}

				@Override public T getValue () {
					return value.get (0);
				}

				@Override public T setValue (T value) {
					T current = data.get (key).get (0);
					data.get (key).add (0, value);
					return current;
				}
			});
		});
		return entries;
	}

	@Override
	public T getOrDefault (Object key, T defaultValue) {
		if ( ! containsKey (key) ) {
			return defaultValue;
		}
		return get (key);
	}

	@Override
	public void forEach (BiConsumer<? super String, ? super T> action) {
		data.forEach ((key, values) -> {
			values.forEach ((value) -> {
				action.accept (key, value);
			});
		});
	}

	@Override
	public void replaceAll (BiFunction<? super String, ? super T, ? extends T> function) {
		data.forEach ((key, values) -> {
			values.replaceAll ((value) -> {
				return function.apply (key, value);
			});
		});
	}

	@Override
	public T putIfAbsent (String key, T value) {
		if ( containsKey (key) ) {
			return get (key);
		}
		put (key, value);
		return null;
	}

	@Override
	public T computeIfAbsent (String key, Function<? super String, ? extends T> mappingFunction) {
		return null;
	}

	@Override
	public T computeIfPresent (String key, BiFunction<? super String, ? super T, ? extends T> remappingFunction) {
		return null;
	}

	@Override
	public T compute (String key, BiFunction<? super String, ? super T, ? extends T> remappingFunction) {
		return null;
	}

	@Override
	public T merge (String key, T value, BiFunction<? super T, ? super T, ? extends T> remappingFunction) {
		return null;
	}

}
