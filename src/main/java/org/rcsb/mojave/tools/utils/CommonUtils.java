package org.rcsb.mojave.tools.utils;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.jsonschema2pojo.FileCodeWriterWithEncoding;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Properties;

/**
 * Collects utility methods for tasks that are common between modules.
 *
 * Created on 9/20/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class CommonUtils {

    private CommonUtils() {}

    /**
     * Provides a list of properties stored in {@link Properties} object. Properties are defined in the
     * project's .pom file. Properties are extracted and stored in a text file during build process.
     *
     * @param resourceName absolute path to the resource.
     *
     * @return object with project properties in which the key is a string and the value is also a string.
     * @throws IOException when unable to load file with properties.
     */
    public static Properties getProjectProperties(String resourceName) throws IOException {

        InputStream is = AppUtils.class.getResourceAsStream(resourceName);
        Properties p = new Properties();
        p.load(is);

        return p;
    }

    /**
     * Creates folders (if needed) to ensure the path exists.
     *
     * @param folder that may not exist.
     */
    public static void ensurePathToFolderExist(File folder) throws IOException {
        if (!folder.exists() && !folder.mkdirs())
            throw new IOException("Couldn't create a directory: "+folder.getAbsolutePath());
    }

    /**
     * Finds JSON schema files within a given directory (and optionally its subdirectories).
     * All files found are filtered by .json extension.
     *
     * @param directory the directory to search in
     * @return a collection of {@link java.io.File} with the matching files
     */
    public static Collection<File> listSchemaFiles(File directory) {
        return FileUtils.listFiles(
                directory,
                new RegexFileFilter("^(.*?).json"),
                DirectoryFileFilter.DIRECTORY
        );
    }

    public static String getRelativePath(URI base, URI absolute) {
        return base.relativize(absolute).getPath();
    }

    /**
     * Writes Java class to a specified location.
     *
     * @param outDir location where the class should be placed.
     * @param codeModel with Java code to be generated.
     * @throws IOException when the application is unable to write resulting file.
     */
    public static void writeClassToFile(File outDir, JCodeModel codeModel) throws IOException {

        CodeWriter resourcesWriter = new FileCodeWriterWithEncoding(outDir, "UTF-8");
        codeModel.build(resourcesWriter);
    }
}
