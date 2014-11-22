package com.gmail.inverseconduit.javadoc;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Parses the Java 8 Javadocs.
 * @author Michael Angstadt
 */
public class Java8PageParser implements PageParser {
	@Override
	public List<String> parseClassNames(Document document) {
		List<String> classNames = new ArrayList<>();
		for (Element element : document.select("ul li a")) {
			String url = element.attr("href");
			int dotPos = url.lastIndexOf('.');
			if (dotPos < 0) {
				continue;
			}

			url = url.substring(0, dotPos);
			url = url.replace('/', '.');
			classNames.add(url);
		}
		return classNames;
	}

	@Override
	public ClassInfo parseClassPage(Document document, String className) {
		String description;
		{
			Element descriptionElement = document.select(".block").first();
			DescriptionNodeVisitor visitor = new DescriptionNodeVisitor();
			descriptionElement.traverse(visitor);
			description = visitor.getStringBuilder().toString();
		}

		String url = getBaseUrl() + "?" + className.replace('.', '/') + ".html";
		return new ClassInfo(className, description, url);
	}

	@Override
	public String getBaseUrl() {
		return "https://docs.oracle.com/javase/8/docs/api/";
	}
}
