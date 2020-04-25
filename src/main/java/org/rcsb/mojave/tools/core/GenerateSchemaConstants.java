package org.rcsb.mojave.tools.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.CaseFormat;
import com.sun.codemodel.*;
import org.rcsb.mojave.tools.jsonschema.SchemaLoader;
import org.rcsb.mojave.tools.jsonschema.traversal.JsonSchemaWalker;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.FieldNamesCollector;
import org.rcsb.mojave.tools.utils.CommandOptions;
import org.rcsb.mojave.tools.utils.CommonUtils;
import org.rcsb.mojave.tools.utils.NameUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singletonList;

/**
 * Creates a Java {@link Class} with constants for each of the unique fields present
 * in the core schemas. The constant names are derived from the field names and modified
 * according to Java conventions for constants.
 *
 * Created on 9/20/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class GenerateSchemaConstants {

    private SchemaLoader loader;

    private GenerateSchemaConstants() {
        loader = new SchemaLoader();
    }

    /**
     * Uses CodeModel java source code generation library to generate code model.
     *
     * @param fqn fully qualified name of class to be created.
     * @param fieldNames set of class variables.
     * @return code model that provides a way to generate Java code.
     *
     * @throws JClassAlreadyExistsException when the specified class/interface was already created.
     */
    private JCodeModel createCodeModel(String fqn, Set<String> fieldNames) throws JClassAlreadyExistsException {

        JCodeModel cm = new JCodeModel();
        JDefinedClass clazz = cm._class(JMod.PUBLIC, fqn, ClassType.CLASS);

        Set<String> existingNames = new HashSet<>();

        int mods = JMod.PUBLIC + JMod.STATIC + JMod.FINAL;
        for (String n : fieldNames) {

            JExpression fieldVar = JExpr.lit(n);
            String field = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE, n);

            // Important Note: Java naming convention requires constants be all uppercase, however
            // mmCIF dictionary assumes case sensitive names. To deduplicate constant variables
            // that differ only in case - an additional postfix added to the field name.
            field = NameUtils.makeNameBeLegalJavaName(field);
            field = NameUtils.makeUnique(field, existingNames);
            existingNames.add(field);

            clazz.field(mods, String.class, field, fieldVar);
        }
        return cm;
    }

    /**
     * Generates a Java {@link Class} with constants for each of the unique fields present
     * in the JSON schemas.
     *
     * @param schemasDir the full path to the directory with input JSON schemas.
     * @param outputDir the full path to the directory where constants file will be stored.
     * @param fqp the full path to the constants classes.
     * @throws IOException is schema file cannot be read.
     * @throws JClassAlreadyExistsException is constant file already exists.
     */
    private void run(File schemasDir, File outputDir, String fqp) throws IOException, JClassAlreadyExistsException {

        File[] files = schemasDir.listFiles(File::isFile);
        if (files == null || files.length == 0)
            throw new IllegalStateException("There are no schemas to process in "+schemasDir.getAbsolutePath());

        FieldNamesCollector visitor = new FieldNamesCollector();

        for(File f : files) {
            JsonNode schema = loader.readSchema(f.toURI());
            JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                    .fromInstance(schema)
                    .acceptingVisitors(singletonList(visitor))
                    .build();
            walker.walk();
        }

        CommonUtils.ensurePathToFolderExist(outputDir);

        JCodeModel codeModel = createCodeModel(fqp, visitor.getNames());
        CommonUtils.writeClassToFile(outputDir, codeModel);
    }

    public static void main(String[] args) throws Exception {

        CommandOptions cmd = new CommandOptions(args);
        if (!cmd.hasOption("-i"))
            throw new IllegalArgumentException("Source Directory (-i) argument is not specified.");
        if (!cmd.hasOption("-o"))
            throw new IllegalArgumentException("Output Directory (-o) argument is not specified.");
        if (!cmd.hasOption("-t"))
            throw new IllegalArgumentException("Target Class Name (-t) argument is not specified.");

        String sourceDirectory = cmd.valueOf("-i").get(0);
        String outputDirectory = cmd.valueOf("-o").get(0);
        String targetClassName = cmd.valueOf("-t").get(0);

        File schemaDir = new File(sourceDirectory);
        if (!schemaDir.exists())
            throw new IllegalStateException("Folder with input schemas does not exist.");

        File outputDir = new File(outputDirectory);

        GenerateSchemaConstants me = new GenerateSchemaConstants();
        me.run(schemaDir, outputDir, targetClassName);
    }
}
