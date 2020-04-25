package org.rcsb.mojave.tools.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections15.IteratorUtils;

import java.util.Iterator;
import java.util.List;

/**
 * Schema stitching is the process of creating a single schema from multiple underlying schemas. This class provides
 * capability to operate on two instances of JSON schema (targetSchema and updateSchema). During merging targetSchema is
 * being updated with the content of updateSchema. If conflicts happen the content of updateSchema takes precedence.
 *
 * Created on 9/13/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class SchemaStitching {

    private SchemaStitching() {}

    /**
     * Recursively merges two JSON trees and overwrites the input targetNode with a content of merge.
     *
     * @param targetNode input JSON object to be updated.
     * @param updateNode input JSON object to be merged.
     *
     * @return targetNode updated with content of updateNode.
     */
    private static ObjectNode mergeObjectNodes(ObjectNode targetNode, ObjectNode updateNode) {

        Iterator<String> fieldNames = updateNode.fieldNames();

        while (fieldNames.hasNext()) {

            String fieldName = fieldNames.next();

            JsonNode targetValue = targetNode.get(fieldName);
            JsonNode updateValue = updateNode.get(fieldName);

            if (targetValue == null) {
                targetNode.set(fieldName, updateValue);

            } else if (targetValue.isObject() && updateValue.isObject()) {
                targetNode.set(fieldName, mergeObjectNodes((ObjectNode) targetValue, (ObjectNode) updateValue));

            } else if (targetValue.isArray() && updateValue.isArray()) {

                List<JsonNode> existing = IteratorUtils.toList(targetValue.elements());
                ArrayNode updateArr = (ArrayNode) updateValue;
                for (int i = 0; i < updateArr.size(); i++) {
                    JsonNode node = updateArr.get(i);
                    if (!existing.contains(node))
                        ((ArrayNode) targetValue).add(node);
                }

            } else {
                targetNode.set(fieldName, updateValue);
            }
        }

        return targetNode;
    }

    /**
     * Merge operation updates the content of targetSchema with a content of updateSchema.
     * The content of {@param targetSchema} is modified inplace. It is recommended that a
     * copy of original schema is used if changes in original schema are not desired.
     *
     * @param targetSchema an instance of JSON schema to be updated.
     * @param updateSchema an instance of JSON schema with update content.
     *
     */
    public static void mergeSchemas(JsonNode targetSchema, JsonNode updateSchema) {

        mergeObjectNodes((ObjectNode) targetSchema, (ObjectNode) updateSchema);
    }

    public static JsonNode mergeSchemas(List<JsonNode> instances) {
        JsonNode finalSchema = null;
        for (JsonNode schema : instances) {
            if (finalSchema == null)
                finalSchema = schema.deepCopy();
            else
                mergeSchemas(finalSchema, schema);
        }
        return finalSchema;
    }
}
