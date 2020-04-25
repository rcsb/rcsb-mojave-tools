package org.rcsb.mojave.tools.derived;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.rcsb.mojave.tools.jsonschema.SchemaLoader;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;
import org.rcsb.mojave.tools.jsonschema.traversal.JsonSchemaWalker;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.NamespaceVisitor;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.PropertiesFilterVisitor;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.Visitor;
import org.rcsb.mojave.tools.utils.CommandOptions;
import org.rcsb.mojave.tools.utils.CommonUtils;

import java.io.File;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * This tool generates JSON schema for sequence clusters data that will be stitched to the core ENTITY schema.
 *
 * Created on 10/19/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class GenerateDerivedSchemaSeqClusters {

    private static List<String> retain = singletonList("cluster_membership");

    public static void main(String[] args) throws Exception {

        CommandOptions cmd = new CommandOptions(args);
        if (!cmd.hasOption("-i"))
            throw new IllegalArgumentException("Input arguments are not specified.");
        if (!cmd.hasOption("-o"))
            throw new IllegalArgumentException("Output argument is not specified.");

        String sourceSchemaLocation = cmd.valueOf("-i").get(0);
        String derivedSchemaLocation = cmd.valueOf("-o").get(0);

        SchemaLoader loader = new SchemaLoader();
        JsonNode schema = loader.readSchema(sourceSchemaLocation);

        Visitor v1 = new PropertiesFilterVisitor(retain);
        NamespaceVisitor v2 = new NamespaceVisitor()
                .atLevel(NamespaceVisitor.Level.ROOT)
                .withPrefix("rcsb").withDelimiter("_");
        List<Visitor> visitors = asList(v1, v2);

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .acceptingVisitors(visitors)
                .build();
        walker.walk();

        // required fields validation for sequence cluster data won't be applicable fro core entity.
        ObjectNode updatedSchema = (ObjectNode) schema;

        updatedSchema.remove(MetaSchemaProperty.REQUIRED);
        updatedSchema.remove(MetaSchemaProperty.SCHEMA_ID);
        updatedSchema.remove(MetaSchemaProperty.TITLE);
        updatedSchema.remove(MetaSchemaProperty.SCHEMA);

        File file = new File(derivedSchemaLocation);
        CommonUtils.ensurePathToFolderExist(file.getParentFile());
        loader.writeSchema(derivedSchemaLocation, updatedSchema);
    }
}
