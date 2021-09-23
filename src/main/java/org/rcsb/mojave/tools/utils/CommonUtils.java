package org.rcsb.mojave.tools.utils;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import org.jsonschema2pojo.FileCodeWriterWithEncoding;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
