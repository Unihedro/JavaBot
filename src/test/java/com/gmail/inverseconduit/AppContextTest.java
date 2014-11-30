package com.gmail.inverseconduit;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

/**
 * @author Michael Angstadt
 */
public class AppContextTest {
	private final AppContext context = AppContext.INSTANCE;

	@Test
	public void get_set() {
		List<Object> list = new ArrayList<Object>();
		context.add(list);

		assertTrue(context.get(ArrayList.class) == list);
		assertTrue(context.get(List.class) == list);
		assertTrue(context.get(Collection.class) == list);
		assertNull(context.get(String.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void add_null() {
		context.add(null);
	}
}
