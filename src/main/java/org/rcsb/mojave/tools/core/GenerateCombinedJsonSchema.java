package org.rcsb.mojave.tools.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.rcsb.mojave.tools.jsonschema.SchemaLoader;
import org.rcsb.mojave.tools.jsonschema.SchemaRefResolver;
import org.rcsb.mojave.tools.jsonschema.SchemaStitching;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;
import org.rcsb.mojave.tools.utils.CommandOptions;
import org.rcsb.mojave.tools.utils.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This tool generates JSON schema that describes core collections in Data Warehouse. It merges multiple JSON schemas
 * into one core schema - schema stitching.
 *
 * Created on 9/20/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class GenerateCombinedJsonSchema {

    public static void main(String[] args) throws IOException {

        CommandOptions cmd = new CommandOptions(args);
        if (!cmd.hasOption("-i"))
            throw new IllegalArgumentException("Input arguments are not specified.");
        if (!cmd.hasOption("-o"))
            throw new IllegalArgumentException("Output argument is not specified.");

        boolean resolve = false;
        if (cmd.hasOption("-r"))
            resolve = Boolean.parseBoolean(cmd.valueOf("-r").get(0));

        JsonNode finalSchema = null;
        SchemaLoader loader = new SchemaLoader();

        List<String> input = cmd.valueOf("-i");
        for (String path : input) {
            JsonNode schema = loader.readSchema(path);
            if (resolve) {
                SchemaRefResolver resolver = new SchemaRefResolver(schema, loader);
                resolver.resolveInline();
            }
            ((ObjectNode) schema).remove(MetaSchemaProperty.SCHEMA_ID);
            if (finalSchema == null)
                finalSchema = schema.deepCopy();
            else
                SchemaStitching.mergeSchemas(finalSchema, schema);
        }

        String coreSchemaLocation = cmd.valueOf("-o").get(0);
        File file = new File(coreSchemaLocation);
        CommonUtils.ensurePathToFolderExist(file.getParentFile());

        if (finalSchema == null)
            throw new IllegalStateException("Final schema was not produced.");

        loader.writeSchema(coreSchemaLocation, finalSchema);
    }
}
