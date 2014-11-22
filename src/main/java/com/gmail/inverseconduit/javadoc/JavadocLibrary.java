package com.gmail.inverseconduit.javadoc;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Represents a Javadoc library.
 * @author Michael Angstadt
 */
public class JavadocLibrary {
	private final PageLoader loader;
	private final PageParser parser;

	public JavadocLibrary(PageLoader loader, PageParser parser) {
		this.loader = loader;
		this.parser = parser;
	}

	public List<String> getAllClassNames() throws IOException {
		Document document;
		try (InputStream in = loader.getAllClassesFile()) {
			document = Jsoup.parse(in, "UTF-8", parser.getBaseUrl());
		}
		return parser.parseClassNames(document);
	}

	public ClassInfo getClassInfo(String className) throws IOException {
		Document document;
		try (InputStream in = loader.getClassPage(className)) {
			if (in == null) {
				return null;
			}
			document = Jsoup.parse(in, "UTF-8", parser.getBaseUrl());
		}
		return parser.parseClassPage(document, className);
	}
}