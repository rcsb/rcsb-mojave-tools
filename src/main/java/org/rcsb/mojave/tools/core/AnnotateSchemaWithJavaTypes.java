package org.rcsb.mojave.tools.core;

import com.fasterxml.jackson.databind.JsonNode;
import org.rcsb.mojave.tools.jsonschema.SchemaLoader;
import org.rcsb.mojave.tools.jsonschema.traversal.JsonSchemaWalker;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.JavaTypeAnnotator;
import org.rcsb.mojave.tools.utils.CommandOptions;
import org.rcsb.mojave.tools.utils.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static java.util.Collections.singletonList;

/**
 * Creates a Java {@link Enum} for each enum definition in core JSON schemas.
 *
 * Created on 09/20/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class AnnotateSchemaWithJavaTypes {

    public static void main(String[] args) throws IOException {

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

        boolean useTitleAsClassname = false;
        if (cmd.hasOption("-n") && Boolean.parseBoolean(cmd.valueOf("-n").get(0)))
            useTitleAsClassname = true;

        File schemasDir = new File(sourceDirectory);
        if (!schemasDir.exists())
            throw new IllegalStateException("Folder with input schemas does not exist.");

        File[] files = schemasDir.listFiles(File::isFile);
        if (files == null || files.length == 0)
            throw new IllegalStateException("There are no schemas to process in "+schemasDir.getAbsolutePath());

        File outputDir = new File(outputDirectory);
        CommonUtils.ensurePathToFolderExist(outputDir);

        SchemaLoader loader = new SchemaLoader();

        JavaTypeAnnotator javaTypeNameVisitor = new JavaTypeAnnotator()
                .setTargetPackage(targetPackage);

        for (File f : files) {
            JsonNode schema = loader.readSchema(f.toURI());
            JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                    .fromInstance(schema)
                    .acceptingVisitors(singletonList(javaTypeNameVisitor))
                    .withSchemaTitleAsName(useTitleAsClassname)
                    .build();
            walker.walk();
            String finalSchemaLocation = Paths.get(outputDirectory, f.getName()).toFile().getAbsolutePath();
            loader.writeSchema(finalSchemaLocation, schema);
        }
    }
}
