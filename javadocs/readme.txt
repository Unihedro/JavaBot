Put a ZIP file containing Java's Javadocs here (or where ever "BotConfig.JAVADOCS_DIR" points to)

The ZIP file should contain the files that are in the "api" directory of the "Documentation" download, found here:
http://www.oracle.com/technetwork/java/javase/documentation/jdk8-doc-downloads-2133158.html

Make sure that a file called "allclasses-frames.html" is at the root of the ZIP.

I don't want to commit the real Javadoc file to version control, due to its large file size (~50MB), so I've committed a smaller, sample file instead.
The sample ZIP file contains just a single class (java.lang.String).

-Michael