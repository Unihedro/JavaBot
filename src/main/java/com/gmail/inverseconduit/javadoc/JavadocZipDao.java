package com.gmail.inverseconduit.javadoc;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

/**
 * Retrieves the Javadoc information from ZIP files.
 * @author Michael Angstadt
 */
public class JavadocZipDao implements JavadocDao {
	private final List<Path> javadocFiles;
	private final Map<String, Set<String>> simpleToFullClassNames;

	/**
	 * Creates a new Javadoc ZIP DAO.
	 * @param javadocDir the path to the directory where the ZIP files are.
	 * @throws IOException if there's a problem reading a ZIP file
	 */
	public JavadocZipDao(Path javadocDir) throws IOException {
		//build a list of all the javadoc ZIP files
		List<Path> javadocFiles = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(javadocDir)) {
			for (Path path : stream) {
				if (isZipFile(path)) {
					javadocFiles.add(path);
				}
			}
		}
		this.javadocFiles = Collections.unmodifiableList(javadocFiles);

		//build an index of all the simple names
		Map<String, Set<String>> simpleToFullClassNames = new HashMap<>();
		for (Path path : javadocFiles) {
			try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
				Path allClassesFile = fs.getPath("/allclasses-frame.html");
				Document document = Jsoup.parse(Files.newInputStream(allClassesFile), "UTF-8", "");
				for (Element element : document.select("ul li a")) {
					String url = element.attr("href");
					int slashPos = url.lastIndexOf('/');
					int dotPos = url.lastIndexOf('.');
					String packageName = url.substring(0, slashPos).replace('/', '.');
					String simpleName = url.substring(slashPos + 1, dotPos);

					Set<String> fullNames = simpleToFullClassNames.get(simpleName.toLowerCase());
					if (fullNames == null) {
						fullNames = new HashSet<>();
						simpleToFullClassNames.put(simpleName.toLowerCase(), fullNames);
					}
					fullNames.add(packageName + "." + simpleName);
				}
			}
		}
		this.simpleToFullClassNames = Collections.unmodifiableMap(simpleToFullClassNames);
	}

	@Override
	public ClassInfo getClassInfo(String className) throws IOException, MultipleClassesFoundException {
		if (!className.contains(".")) {
			Set<String> names = simpleToFullClassNames.get(className.toLowerCase());
			if (names == null) {
				return null;
			}
			if (names.size() > 1) {
				throw new MultipleClassesFoundException(names);
			}

			className = names.iterator().next();
		}

		Path htmlFile = Paths.get("/" + className.replace('.', '/') + ".html");
		for (Path path : javadocFiles) {
			try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
				Path p = fs.getPath(htmlFile.toString());
				if (!Files.exists(p)) {
					continue;
				}

				Document document = Jsoup.parse(Files.newInputStream(p), "UTF-8", "");
				Element descriptionElement = document.select(".block").first();

				DescriptionNodeVisitor visitor = new DescriptionNodeVisitor();
				descriptionElement.traverse(visitor);

				String description = visitor.sb.toString();

				//TODO better parsing!
				//TODO support retrieval of method docs

				return new ClassInfo(className, description);
			}
		}

		return null;
	}

	private static boolean isZipFile(Path path) {
		if (Files.isDirectory(path)) {
			return false;
		}

		return path.getFileName().toString().endsWith(".zip");
	}

	private static class DescriptionNodeVisitor implements NodeVisitor {
		private final StringBuilder sb = new StringBuilder();
		private boolean inPre = false;

		private String linkUrl, linkTitle, linkText;

		@Override
		public void head(Node node, int depth) {
			//for (int i = 0; i < depth; i++) {
			//	System.out.print(' ');
			//}
			//System.out.println("head " + node.nodeName());

			switch (node.nodeName()) {
			case "a":
				Element element = (Element) node;
				String href = element.absUrl("href");
				if (!href.isEmpty()) {
					linkUrl = href;
					linkTitle = element.attr("title");
				}
				break;
			case "code":
				sb.append("`");
				break;
			case "i":
			case "em":
				sb.append("*");
				break;
			case "b":
			case "strong":
				sb.append("**");
				break;
			case "br":
			case "p":
				sb.append("\n");
				break;
			case "pre":
				inPre = true;
				sb.append("\n");
				break;
			case "#text":
				TextNode text = (TextNode) node;
				String content = inPre ? text.getWholeText() : text.text();
				content = content.replaceAll("[*_\\[\\]]", "\\$0"); //escape special chars
				if (linkUrl != null) {
					linkText = content;
				} else {
					sb.append(content);
				}
				break;
			}
		}

		@Override
		public void tail(Node node, int depth) {
			//for (int i = 0; i < depth; i++) {
			//	System.out.print(' ');
			//}
			//System.out.println("tail " + node.nodeName());

			switch (node.nodeName()) {
			case "a":
				if (linkUrl != null) {
					sb.append("[").append(linkText).append("](").append(linkUrl);
					if (!linkTitle.isEmpty()) {
						sb.append(" \"").append(linkTitle).append("\"");
					}
					sb.append(")");
					
					linkUrl = linkText = linkTitle = null;
				}
				break;
			case "code":
				sb.append("`");
				break;
			case "i":
			case "em":
				sb.append("*");
				break;
			case "b":
			case "strong":
				sb.append("**");
				break;
			case "p":
				sb.append("\n");
				break;
			case "pre":
				inPre = false;
				sb.append("\n");
				break;
			}
		}
	}
}
