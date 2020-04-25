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
import org.rcsb.mojave.tools.utils.CommonUtils;
import org.rcsb.mojave.tools.utils.ConfigurableMapper;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * This tool updates all core JSON schemas to BSON types and produce new schemas
 * that can be used for validation in MongoDB.
 *
 * Created on 9/20/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class GenerateValidationBsonSchema {

    public static void main(String[] args) throws Exception {

        if (args.length < 2)
            throw new IllegalArgumentException("Method has been passed less arguments than required.");

        String coreSchemasLocation = args[0];
        String validationSchemasLocation = args[1];

        File file = new File(validationSchemasLocation);
        CommonUtils.ensurePathToFolderExist(file.getParentFile());

        SchemaLoader loader = new SchemaLoader();
        JsonNode schema = loader.readSchema(new URL("file://" + coreSchemasLocation));

        List<Visitor> visitors = Arrays.asList(
                new KeywordsSyntaxChecker(SchemaVersion.DRAFTV4.getSchema()),
                new BsonTypeAliasConverter());

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

        loader.writeSchema(validationSchemasLocation, schema);
    }
}