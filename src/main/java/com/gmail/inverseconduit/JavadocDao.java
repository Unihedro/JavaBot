package com.gmail.inverseconduit;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Retrieves the Javadoc information of a class.
 * @author Michael Angstadt
 */
public class JavadocDao {
	private final List<Path> javadocFiles;
	private final Map<String, Set<String>> simpleToFullClassNames;
	
	/**
	 * Creates a new Javadoc DAO.
	 * @param javadocDir the path to the directory where the Javadoc ZIP files are held.
	 * @throws IOException if there's a problem reading a ZIP file
	 */
	public JavadocDao(Path javadocDir) throws IOException{
		//build a list of all the javadoc ZIP files
		List<Path> javadocFiles = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(javadocDir)){
			for (Path path : stream){
				if (isZipFile(path)){
					javadocFiles.add(path);
				}
			}
		}
		this.javadocFiles = Collections.unmodifiableList(javadocFiles);
		
		//build an index of all the simple names
		Map<String, Set<String>> simpleToFullClassNames = new HashMap<>();
		for (Path path : javadocFiles){
			try (FileSystem fs = FileSystems.newFileSystem(path, null)){
				Path allClassesFile = fs.getPath("/allclasses-frame.html");
				Document document = Jsoup.parse(Files.newInputStream(allClassesFile), "UTF-8", "");
				for (Element element : document.select("ul li a")){
					String url = element.attr("href");
					int slashPos = url.lastIndexOf('/');
					int dotPos = url.lastIndexOf('.');
					String packageName = url.substring(0, slashPos).replace('/', '.');
					String simpleName = url.substring(slashPos+1, dotPos);
					
					Set<String> fullNames = simpleToFullClassNames.get(simpleName.toLowerCase());
					if (fullNames == null){
						fullNames = new HashSet<>();
						simpleToFullClassNames.put(simpleName.toLowerCase(), fullNames);
					}
					fullNames.add(packageName + "." + simpleName);
				}
			}
		}
		this.simpleToFullClassNames = Collections.unmodifiableMap(simpleToFullClassNames);
	}
	
	/**
	 * Gets the Javadoc info of a class.
	 * @param className the simple class name (e.g. "String", case-insensitive)
	 * or the fully-qualified class name (e.g. "java.lang.String")
	 * @return the class info or null if not found
	 * @throws MultipleClassesFoundException if a simple class name was passed
	 * into the method, and multiple classes were found with that name
	 * @throws IOException if there's a problem reading a ZIP file
	 */
	public ClassInfo getClassInfo(String className) throws IOException, MultipleClassesFoundException{
		if (!className.contains(".")){
			Set<String> names = simpleToFullClassNames.get(className.toLowerCase());
			if (names == null){
				return null;
			}
			if (names.size() > 1){
				throw new MultipleClassesFoundException(names);
			}
			
			className = names.iterator().next();
		}
		
		Path htmlFile = Paths.get("/" + className.replace('.', '/') + ".html");
		for (Path path : javadocFiles){
			try (FileSystem fs = FileSystems.newFileSystem(path, null)){
				Path p = fs.getPath(htmlFile.toString());
				if (Files.exists(p)){
					Document document = Jsoup.parse(Files.newInputStream(p), "UTF-8", "");
					String description = document.select(".block").first().text();
					
					//TODO better parsing!
					//TODO support retrieval of method docs
					
					return new ClassInfo(className, description);
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Thrown if multiple classes were found.
	 * @see JavadocDao#getClassInfo(String)
	 */
	public static class MultipleClassesFoundException extends RuntimeException{
		private static final long serialVersionUID = -6218458106841347985L;
		private final Collection<String> classes;
		
		public MultipleClassesFoundException(Collection<String> classes){
			this.classes = classes;
		}
		
		public Collection<String> getClasses(){
			return classes;
		}
		
		@Override
		public String toString(){
			return classes.toString();
		}
	}
	
	/**
	 * Holds the Javadoc info of a class.
	 */
	public static class ClassInfo{
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
	
	private static boolean isZipFile(Path path){
		if (Files.isDirectory(path)){
			return false;
		}
		
		return path.getFileName().toString().endsWith(".zip");
	}
}
