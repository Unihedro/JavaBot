package com.gmail.inverseconduit;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.gmail.inverseconduit.AppContext;

/**
 * @author Michael Angstadt
 */
public class AppContextTest {
	@Test
	public void get_set() {
		AppContext context = AppContext.INSTANCE;

		ArrayList<Object> arrayList = new ArrayList<Object>();
		LinkedList<Object> linkedList = new LinkedList<Object>();
		context.add(arrayList);
		context.add(linkedList);

		assertTrue(context.get(ArrayList.class) == arrayList);
		assertTrue(context.get(LinkedList.class) == linkedList);
		assertTrue(context.get(List.class) == arrayList);
		assertNull(context.get(String.class));
	}
}
