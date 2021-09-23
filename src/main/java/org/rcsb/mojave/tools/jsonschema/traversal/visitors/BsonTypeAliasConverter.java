package org.rcsb.mojave.tools.jsonschema.traversal.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaModifier;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaType;
import org.rcsb.mojave.tools.jsonschema.constants.TraversalLabel;
import org.rcsb.mojave.tools.jsonschema.traversal.model.TraversalContext;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.Visitable;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.VisitableNode;
import org.rcsb.mojave.tools.jsonschema.utils.JsonSchemaNodeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Annotates JSON schema with "bsonType" keyword instead of "type". Used in used in a MongoDB document validator,
 * which enforces that inserted or updated documents are valid against the JSON schema.
 * <p>
 * Created on 8/29/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class BsonTypeAliasConverter implements Visitor {

    /**
     * JSON Schema types mapped to string bson aliases for the BSON types accepted by MongoDB.
     */
    private static final Map<String, String> bsonAliases = new HashMap<>();

    static {
        bsonAliases.put(MetaSchemaType.NUMBER,  "double");
        bsonAliases.put(MetaSchemaType.INTEGER, "int");
        bsonAliases.put(MetaSchemaType.STRING,  "string");
        bsonAliases.put(MetaSchemaType.OBJECT,  "object");
        bsonAliases.put(MetaSchemaType.ARRAY,   "array");
        bsonAliases.put(MetaSchemaType.BOOLEAN, "bool");
        bsonAliases.put(MetaSchemaType.NULL,    "null");
    }

    @Override
    public void visit(Visitable visitableNode) {

        // An extension property 'bsonType' that applies to schema and allows specifying a type
        // for document validation in MongoDB.

        if (!(visitableNode instanceof VisitableNode))
            throw new IllegalArgumentException("Node object MUST be an instance of VisitableNode.");

        TraversalContext ctx = ((VisitableNode) visitableNode).getTraversalContext();
        JsonNode node = ((VisitableNode) visitableNode).getNode();
        if (!ctx.isRef() && !ctx.getLabel().equals(TraversalLabel.PROPERTIES)
                && node.has(MetaSchemaProperty.TYPE)) {

            String jsonType = node.get(MetaSchemaProperty.TYPE).textValue();
            if (!bsonAliases.containsKey(jsonType))
                throw new IllegalArgumentException("Provided JSON schema contains types that are not supported by specification.");

            ObjectNode updatedNode;
            if (JsonSchemaNodeUtils.isDate(node)) {
                updatedNode = ((ObjectNode) node).put(MetaSchemaModifier.BSON_TYPE, MetaSchemaType.DATE);
                updatedNode.remove(MetaSchemaProperty.FORMAT);
            } else {
                updatedNode = ((ObjectNode) node).put(MetaSchemaModifier.BSON_TYPE, bsonAliases.get(jsonType));
            }

            // MongoDB '$jsonSchema' support one of the keywords a time (either 'type' or 'bsonType')
            // therefore removing 'type' node.
            updatedNode.remove(MetaSchemaProperty.TYPE);
        }
    }
}
