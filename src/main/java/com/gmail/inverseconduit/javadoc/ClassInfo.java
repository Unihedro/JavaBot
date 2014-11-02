package com.gmail.inverseconduit.javadoc;

/**
 * Holds the Javadoc info of a class.
 * @author Michael Angstadt
 */
public class ClassInfo{
	private final String fullName;
	private final String description;
	
	public ClassInfo(String fullName, String description) {
		this.fullName = fullName;
		this.description = description;
	}
	
	/**
	 * Gets the class's fully-qualified name.
	 * @return the fully-qualified name (e.g. "java.lang.String")
	 */
	public String getFullName() {
		return fullName;
	}
	
	/**
	 * Gets the class's description.
	 * @return the class description
	 */
	public String getDescription() {
		return description;
	}
}