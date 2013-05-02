/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;

import hu.openig.utils.Exceptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Object containing other message objects.
 * @author akarnokd, 2013.04.21.
 */
public class MessageArray implements Iterable<Object>, MessageSerializable {
	/** The array's name. */
	public final String name;
	/** The list of message objects. */
	protected final List<Object> items = new ArrayList<Object>();
	/**
	 * Constructor, sets the optional object name.
	 * @param name the object name
	 */
	public MessageArray(String name) {
		if (name != null && !MessageObject.verifyName(name)) {
			throw new IllegalArgumentException("name");
		}
		this.name = name;
	}
	/**
	 * Constructs a message array with the given values.
	 * @param name the object name
	 * @param values the value array
	 */
	public MessageArray(String name, Object... values) {
		this(name);
		addAll(values);
	}
	/**
	 * Constructs a message array with the given values.
	 * @param name the object name
	 * @param values the value array
	 */
	public MessageArray(String name, boolean... values) {
		this(name);
		addAll(values);
	}
	/**
	 * Constructs a message array with the given values.
	 * @param name the object name
	 * @param values the value array
	 */
	public MessageArray(String name, byte... values) {
		this(name);
		addAll(values);
	}
	/**
	 * Constructs a message array with the given values.
	 * @param name the object name
	 * @param values the value array
	 */
	public MessageArray(String name, short... values) {
		this(name);
		addAll(values);
	}
	/**
	 * Constructs a message array with the given values.
	 * @param name the object name
	 * @param values the value array
	 */
	public MessageArray(String name, char... values) {
		this(name);
		addAll(values);
	}
	/**
	 * Constructs a message array with the given values.
	 * @param name the object name
	 * @param values the value array
	 */
	public MessageArray(String name, int... values) {
		this(name);
		addAll(values);
	}
	/**
	 * Constructs a message array with the given values.
	 * @param name the object name
	 * @param values the value array
	 */
	public MessageArray(String name, long... values) {
		this(name);
		addAll(values);
	}
	/**
	 * Constructs a message array with the given values.
	 * @param name the object name
	 * @param values the value array
	 */
	public MessageArray(String name, float... values) {
		this(name);
		addAll(values);
	}
	/**
	 * Constructs a message array with the given values.
	 * @param name the object name
	 * @param values the value array
	 */
	public MessageArray(String name, double... values) {
		this(name);
		addAll(values);
	}
	/**
	 * Constructs a message array with the given values.
	 * @param name the object name
	 * @param values the value sequence
	 */
	public MessageArray(String name, Iterable<?> values) {
		this(name);
		addAll(values);
	}
	@Override
	public void save(Appendable out) throws IOException {
		if (name != null) {
			out.append(name);
		}
		out.append('[');
		int i = 0;
		for (Object o : items) {
			if (i > 0) {
				out.append(',');
			}
			MessageObject.appendTo(out, o);
			i++;
		}
		out.append(']');
	}
	/**
	 * Returns the count of elements in the array.
	 * @return the count
	 */
	public int size() {
		return items.size();
	}
	/**
	 * Get an object at a specified index.
	 * @param index the index
	 * @return the object
	 */
	public Object get(int index) {
		return items.get(index);
	}
	/**
	 * Wrap a primitive or object array value into
	 * a MessageArray, or just return the value.
	 * @param value the value
	 * @return the wrapped value
	 */
	public static Object wrap(Object value) {
		if (value instanceof Object[]) {
			value = new MessageArray(null, (Object[])value);
		} else
		if (value instanceof Iterable<?>) {
			value = new MessageArray(null, (Iterable<?>)value);
		} else
		if (value instanceof boolean[]) {
			value = new MessageArray(null, (boolean[])value);
		} else
		if (value instanceof byte[]) {
			value = new MessageArray(null, (byte[])value);
		} else
		if (value instanceof short[]) {
			value = new MessageArray(null, (short[])value);
		} else
		if (value instanceof char[]) {
			value = new MessageArray(null, (char[])value);
		} else
		if (value instanceof int[]) {
			value = new MessageArray(null, (int[])value);
		} else
		if (value instanceof long[]) {
			value = new MessageArray(null, (long[])value);
		} else
		if (value instanceof float[]) {
			value = new MessageArray(null, (float[])value);
		} else
		if (value instanceof double[]) {
			value = new MessageArray(null, (double[])value);
		}
		return value;
	}
	/**
	 * Add a new object to the list.
	 * @param value the value to add
	 */
	public void add(Object value) {
		items.add(wrap(value));
	}
	/**
	 * Adds all items from the value array.
	 * @param values the value array
	 */
	public void addAll(Object... values) {
		for (Object o : values) {
			add(o);
		}
	}
	/**
	 * Adds all items from the value array.
	 * @param values the value array
	 */
	public void addAll(boolean... values) {
		for (Object o : values) {
			add(o);
		}
	}
	/**
	 * Adds all items from the value array.
	 * @param values the value array
	 */
	public void addAll(byte... values) {
		for (Object o : values) {
			add(o);
		}
	}
	/**
	 * Adds all items from the value array.
	 * @param values the value array
	 */
	public void addAll(short... values) {
		for (Object o : values) {
			add(o);
		}
	}
	/**
	 * Adds all items from the value array.
	 * @param values the value array
	 */
	public void addAll(char... values) {
		for (Object o : values) {
			add(o);
		}
	}
	/**
	 * Adds all items from the value array.
	 * @param values the value array
	 */
	public void addAll(int... values) {
		for (Object o : values) {
			add(o);
		}
	}
	/**
	 * Adds all items from the value array.
	 * @param values the value array
	 */
	public void addAll(long... values) {
		for (Object o : values) {
			add(o);
		}
	}
	/**
	 * Adds all items from the value array.
	 * @param values the value array
	 */
	public void addAll(float... values) {
		for (Object o : values) {
			add(o);
		}
	}
	/**
	 * Adds all items from the value array.
	 * @param values the value array
	 */
	public void addAll(double... values) {
		for (Object o : values) {
			add(o);
		}
	}
	/**
	 * Adds all items from the value sequence.
	 * @param values the value sequence
	 */
	public void addAll(Iterable<?> values) {
		for (Object o : values) {
			add(o);
		}
	}
	/**
	 * Adds a value at the specified index.
	 * @param index the index
	 * @param value the value
	 */
	public void add(int index, Object value) {
		items.add(index, wrap(value));
	}
	/**
	 * Remove a value at the specified index.
	 * @param index the index
	 */
	public void remove(int index) {
		items.remove(index);
	}
	/**
	 * Clear the array.
	 */
	public void clear() {
		items.clear();
	}
	/**
	 * Set a value at the specified index.
	 * @param index the index value
	 * @param value the value
	 */
	public void set(int index, Object value) {
		items.set(index, value);
	}
	@Override
	public Iterator<Object> iterator() {
		return items.iterator();
	}
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		try {
			save(b);
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
		return b.toString();
	}
}