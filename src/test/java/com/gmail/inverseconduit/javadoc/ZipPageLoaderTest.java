package com.gmail.inverseconduit.javadoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * @author Michael Angstadt
 */
public class ZipPageLoaderTest {
	private final Path folder = Paths.get("src", "test", "resources", "com", "gmail", "inverseconduit", "javadoc");
	private final Path file = folder.resolve("javadoc-file.zip");
	private final PageLoader loader;

	{
		try {
			loader = new ZipPageLoader(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void getAllClassesFiles() throws Exception {
		String expected;
		try (FileSystem fs = FileSystems.newFileSystem(file, null)) {
			Path allClassesFile = fs.getPath("/allclasses-frame.html");
			expected = new String(Files.readAllBytes(allClassesFile));
		}

		String actual;
		try (InputStream in = loader.getAllClassesFile()) {
			actual = IOUtils.toString(in);
		}

		assertEquals(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getAllClassesFiles_no_allclasses_file() throws Exception {
		new ZipPageLoader(folder.resolve("javadoc-without-allclasses-frame.zip"));
	}

	@Test(expected = FileSystemNotFoundException.class)
	public void getAllClassesFiles_does_not_exist() throws Exception {
		new ZipPageLoader(Paths.get("foobar.zip"));
		
	}

	@Test
	public void getClassPage() throws Exception {
		String expected;
		try (FileSystem fs = FileSystems.newFileSystem(file, null)) {
			Path allClassesFile = fs.getPath("/java/lang/String.html");
			expected = new String(Files.readAllBytes(allClassesFile));
		}

		String actual;
		try (InputStream in = loader.getClassPage("java.lang.String")) {
			actual = IOUtils.toString(in);
		}

		assertEquals(expected, actual);
	}

	@Test
	public void getClassPage_does_not_exist() throws Exception {
		InputStream in = loader.getClassPage("java.util.FooBar");
		assertNull(in);
	}
}
