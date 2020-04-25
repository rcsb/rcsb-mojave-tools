package org.rcsb.mojave.tools.core;

import org.rcsb.mojave.tools.generators.SchemaEnumsGenerator;
import org.rcsb.mojave.tools.utils.CommandOptions;

import java.io.File;

/**
 * Creates a Java {@link Enum} for each enum definition in core JSON schemas.
 *
 * Created on 09/20/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class GenerateSchemaEnums {

    public static void main(String[] args) throws Exception {

        CommandOptions cmd = new CommandOptions(args);
        if (!cmd.hasOption("-i"))
            throw new IllegalArgumentException("Source Directory (-i) argument is not specified.");
        if (!cmd.hasOption("-o"))
            throw new IllegalArgumentException("Output Directory (-o) argument is not specified.");
        if (!cmd.hasOption("-t"))
            throw new IllegalArgumentException("Target Package (-t) argument is not specified.");

        String sourceDirectory = cmd.valueOf("-i").get(0);
        String outputDirectory = cmd.valueOf("-o").get(0);
        String targetPackage = cmd.valueOf("-t").get(0);

        String classNameSuffix = null;
        if (cmd.hasOption("-s"))
            classNameSuffix = cmd.valueOf("-s").get(0);

        File schemaDir = new File(sourceDirectory);
        if (!schemaDir.exists())
            throw new IllegalStateException("Folder with input schemas does not exist.");

        File[] files = schemaDir.listFiles(File::isFile);

        if (files == null || files.length == 0)
            throw new IllegalStateException("There are no schemas to process in "+schemaDir.getAbsolutePath());

        File outputDir = new File(outputDirectory);

        SchemaEnumsGenerator generator = new SchemaEnumsGenerator();
        if ( classNameSuffix!= null) 
            generator.withClassNamePostfix(classNameSuffix);

        generator.run(schemaDir, outputDir, targetPackage);
    }
}
