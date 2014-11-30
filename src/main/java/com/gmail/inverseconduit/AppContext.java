package com.gmail.inverseconduit;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds objects that are used throughout the application.
 * @author Michael Angstadt
 */
public enum AppContext {
	INSTANCE;

	private final Set<Object> objects = new HashSet<>();

	/**
	 * Retrieves an object from the application context.
	 * @param <T> the object class
	 * @param clazz the object class
	 * @return the object or null if not found
	 */
	public <T> T get(Class<T> clazz) {
		for (Object object : objects) {
			if (clazz.isInstance(object)) {
				return clazz.cast(object);
			}
		}
		return null;
	}

	/**
	 * Adds an object to the application context. This should only be called
	 * during application startup.
	 * @param object the object to add
	 */
	public void add(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("Object cannot be null.");
		}
		objects.add(object);
	}
}
