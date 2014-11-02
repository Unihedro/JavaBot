package com.gmail.inverseconduit.javadoc;

import java.io.IOException;

/**
 * Retrieves the Javadoc information of a class.
 * @author Michael Angstadt
 */
public interface JavadocDao {
	/**
	 * Gets the Javadoc info of a class.
	 * @param className can either be the simple class name (e.g. "String",
	 * case-insensitive) or the fully-qualified class name (e.g.
	 * "java.lang.String")
	 * @return the class info or null if not found
	 * @throws MultipleClassesFoundException if a simple class name was passed
	 * into the method and multiple classes were found with that name
	 * @throws IOException if there's a problem reading a ZIP file
	 */
	ClassInfo getClassInfo(String className) throws IOException, MultipleClassesFoundException;
}
