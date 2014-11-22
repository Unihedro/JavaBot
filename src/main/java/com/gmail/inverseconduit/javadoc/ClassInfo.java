package com.gmail.inverseconduit.javadoc;

/**
 * Holds the Javadoc info of a class.
 * @author Michael Angstadt
 */
public class ClassInfo {
	private final String fullName;
	private final String description;
	private final String url;

	public ClassInfo(String fullName, String description, String url) {
		this.fullName = fullName;
		this.description = description;
		this.url = url;
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
	 * @return the class description, formatted in SO Chat's markdown language
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the URL where this class's Javadocs can be viewed online.
	 * @return the URL or null if unknown
	 */
	public String getUrl() {
		return url;
	}
}