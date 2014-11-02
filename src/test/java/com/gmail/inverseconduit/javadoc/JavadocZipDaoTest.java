package com.gmail.inverseconduit.javadoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Paths;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Michael Angstadt
 */
public class JavadocZipDaoTest {
	private static JavadocZipDao dao;
	
	@BeforeClass
	public static void beforeClass() throws Exception{
		dao = new JavadocZipDao(Paths.get("src", "test", "resources"));
	}
	
	@Test
	public void simpleName_single_match() throws Exception {
		ClassInfo info = dao.getClassInfo("String");
		assertEquals("java.lang.String", info.getFullName());
		assertNotNull(info.getDescription());
	}
	
	@Test
	public void simpleName_no_match() throws Exception {
		ClassInfo info = dao.getClassInfo("FooBar");
		assertNull(info);
		
		info = dao.getClassInfo("java.lang.FooBar");
		assertNull(info);
	}
	
	@Test
	public void simpleName_case_insensitive() throws Exception {
		ClassInfo info = dao.getClassInfo("string");
		assertEquals("java.lang.String", info.getFullName());
		assertNotNull(info.getDescription());
	}
	
	@Test
	public void simpleName_multiple_matches() throws Exception {
		try {
			ClassInfo info = dao.getClassInfo("List");
			fail(info.getFullName());
		} catch (MultipleClassesFoundException e){
			Collection<String> names = e.getClasses();
			assertEquals(2, names.size());
			assertTrue(names.contains("java.util.List"));
			assertTrue(names.contains("java.awt.List"));
		}
	}
	
	@Test
	public void fullName() throws Exception {
		ClassInfo info = dao.getClassInfo("java.util.List");
		assertEquals("java.util.List", info.getFullName());
		assertNotNull(info.getDescription());
	}
}
