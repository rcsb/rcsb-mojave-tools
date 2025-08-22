package org.rcsb.mojave.tools.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.rcsb.mojave.tools.jsonschema.SchemaLoader;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaModifier;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;
import org.rcsb.mojave.tools.jsonschema.traversal.JsonSchemaWalker;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.BsonTypeAliasConverter;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.KeywordsSyntaxChecker;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.Visitor;
import org.rcsb.mojave.tools.jsonschema.utils.SchemaVersion;
import org.rcsb.mojave.tools.utils.CommandOptions;
import org.rcsb.mojave.tools.utils.CommonUtils;
import org.rcsb.mojave.tools.utils.ConfigurableMapper;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This tool updates all core JSON schemas to BSON types and produce new schemas
 * that can be used for validation in MongoDB.
 * <p>
 * Created on 9/20/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class GenerateValidationBsonSchema {

    public static void main(String[] args) throws Exception {

        CommandOptions cmd = new CommandOptions(args);
        if (!cmd.hasOption("-i"))
            throw new IllegalArgumentException("Input arguments are not specified.");
        if (!cmd.hasOption("-o"))
            throw new IllegalArgumentException("Output argument is not specified.");

        String inputSchemasLocation = cmd.valueOf("-i").get(0);
        String outputSchemasLocation = cmd.valueOf("-o").get(0);
        CommonUtils.ensurePathToFolderExist(new File(outputSchemasLocation));

        String fileNamePrefix = null;
        if (cmd.hasOption("-p"))
            fileNamePrefix = cmd.valueOf("-p").get(0);

        File folder = new File(inputSchemasLocation);
        Collection<File> files = CommonUtils.listSchemaFiles(folder);
        if (files.size() == 0)
            throw new IllegalStateException("There are no schemas to process in "+folder.getAbsolutePath());

        SchemaLoader loader = new SchemaLoader();
        List<Visitor> visitors = Arrays.asList(
                new KeywordsSyntaxChecker(SchemaVersion.DRAFTV4.getSchema()),
                new BsonTypeAliasConverter());
        for (File f : files) {
            JsonNode schema = loader.readSchema(f.toURI());
            JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                    .fromInstance(schema)
                    .acceptingVisitors(visitors)
                    .build();
            walker.walk();

            JsonNode properties = schema.get(MetaSchemaProperty.PROPERTIES);

            // if JSON schema requires validation of additional properties to be false,
            // an explicit '_id' field is needed in the schema
            ObjectNode idNode = ConfigurableMapper.getMapper().createObjectNode();
            idNode.put(MetaSchemaModifier.BSON_TYPE, "objectId");
            ((ObjectNode) properties).set("_id", idNode);

            // keywords '$schema' and '$comment' are not supported by MongoDB v3.6
            ((ObjectNode) schema).remove(MetaSchemaProperty.SCHEMA);
            ((ObjectNode) schema).remove(MetaSchemaProperty.COMMENT);

            String fileName = (fileNamePrefix != null && !fileNamePrefix.isEmpty())
                    ? fileNamePrefix + f.getName()
                    : f.getName();
            String validationSchemasLocation = Paths.get(outputSchemasLocation, fileName).toString();
            loader.writeSchema(validationSchemasLocation, schema);
        }
    }
}