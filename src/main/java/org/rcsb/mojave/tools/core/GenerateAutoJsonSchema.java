package org.rcsb.mojave.tools.core;

import com.fasterxml.jackson.databind.JsonNode;
import org.rcsb.mojave.tools.jsonschema.SchemaLoader;
import org.rcsb.mojave.tools.jsonschema.traversal.JsonSchemaWalker;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.EnumTransformer;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.JavaTypeAnnotator;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.Visitor;
import org.rcsb.mojave.tools.utils.CommandOptions;
import org.rcsb.mojave.tools.utils.CommonUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * This tool generates JSON schemas used to automatically produce corresponding Java classes. It annotates
 * all schemas in a given "core schemas" folder with Java type names.
 *
 * Created on 9/17/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class GenerateAutoJsonSchema {

    public static void main(String[] args) throws Exception {

        CommandOptions cmd = new CommandOptions(args);
        if (!cmd.hasOption("-i"))
            throw new IllegalArgumentException("Input arguments are not specified.");
        if (!cmd.hasOption("-o"))
            throw new IllegalArgumentException("Output argument is not specified.");
        if (!cmd.hasOption("-t"))
            throw new IllegalArgumentException("Target Package (-t) argument is not specified.");

        String coreSchemasLocation = cmd.valueOf("-i").get(0);
        String autoSchemasLocation = cmd.valueOf("-o").get(0);
        String targetPackage = cmd.valueOf("-t").get(0);
        CommonUtils.ensurePathToFolderExist(new File(autoSchemasLocation));

        EnumTransformer javaEnumVisitor = new EnumTransformer();
        JavaTypeAnnotator javaTypeNameVisitor = new JavaTypeAnnotator();
        javaTypeNameVisitor.setTargetPackage(targetPackage);
        List<Visitor> visitors = asList(javaEnumVisitor, javaTypeNameVisitor);

        SchemaLoader loader = new SchemaLoader();

        File folder = new File(coreSchemasLocation);
        File[] files = folder.listFiles(File::isFile);
        if (files == null || files.length == 0)
            throw new IllegalStateException("There are no schemas to process in "+folder.getAbsolutePath());

        for(File f : files) {
            JsonNode schema = loader.readSchema(f.toURI());
            JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                    .fromInstance(schema)
                    .acceptingVisitors(visitors)
                    .build();
            walker.walk();
            loader.writeSchema(Paths.get(autoSchemasLocation, f.getName()).toString(), schema);
        }
    }
}
