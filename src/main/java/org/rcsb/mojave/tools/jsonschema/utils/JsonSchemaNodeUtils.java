package org.rcsb.mojave.tools.jsonschema.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.rcsb.mojave.tools.jsonschema.SchemaLoader;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;
import org.rcsb.mojave.common.jsonschema.MetaSchemaType;
import org.rcsb.mojave.tools.jsonschema.traversal.model.JsonReference;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created on 8/16/19.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class JsonSchemaNodeUtils {

    private JsonSchemaNodeUtils() {}

    private static boolean hasType(JsonNode node) {
        return node.isObject() && node.has(MetaSchemaProperty.TYPE)
                && node.get(MetaSchemaProperty.TYPE).isTextual();
    }

    public static boolean isDate(JsonNode node) {
        return hasType(node) && node.get(MetaSchemaProperty.TYPE).asText().equals(MetaSchemaType.STRING)
                && node.has(MetaSchemaProperty.FORMAT)
                && (node.get(MetaSchemaProperty.FORMAT).asText().equals(MetaSchemaType.DATE_TIME)
                    || node.get(MetaSchemaProperty.FORMAT).asText().equals(MetaSchemaType.DATE)
                    || node.get(MetaSchemaProperty.FORMAT).asText().equals(MetaSchemaType.TIME));
    }

    public static boolean isObject(JsonNode node) {
        return hasType(node) && node.get(MetaSchemaProperty.TYPE).asText().equals(MetaSchemaType.OBJECT);
    }

    public static boolean isArray(JsonNode node) {
        return hasType(node) && node.get(MetaSchemaProperty.TYPE).asText().equals(MetaSchemaType.ARRAY);
    }

    public static boolean isNumeric(JsonNode node) {
        return hasType(node) && (node.get(MetaSchemaProperty.TYPE).asText().equals(MetaSchemaType.INTEGER)
                    || node.get(MetaSchemaProperty.TYPE).asText().equals(MetaSchemaType.NUMBER));
    }

    public static boolean isNumber(JsonNode node) {
        return hasType(node) && node.get(MetaSchemaProperty.TYPE).asText().equals(MetaSchemaType.NUMBER);
    }

    public static boolean isInteger(JsonNode node) {
        return hasType(node) && node.get(MetaSchemaProperty.TYPE).asText().equals(MetaSchemaType.INTEGER);
    }

    public static boolean isString(JsonNode node) {
        return hasType(node) && node.get(MetaSchemaProperty.TYPE).asText().equals(MetaSchemaType.STRING);
    }

    public static boolean isBoolean(JsonNode node) {
        return hasType(node) && node.get(MetaSchemaProperty.TYPE).asText().equals(MetaSchemaType.BOOLEAN);
    }

    public static boolean isMultiType(JsonNode node) {
        return node.isObject() && node.has(MetaSchemaProperty.TYPE)
                && node.get(MetaSchemaProperty.TYPE).isArray();
    }

    public static boolean isEnum(JsonNode node) {
        return node.isObject() && node.has(MetaSchemaProperty.ENUM);
    }

    public static boolean isComposite(JsonNode node) {
        return node.has(MetaSchemaProperty.ANY_OF)
                || node.has(MetaSchemaProperty.ONE_OF)
                || node.has(MetaSchemaProperty.ALL_OF);
    }

    public static boolean isRef(JsonNode node) {
        return node.has(MetaSchemaProperty.SCHEMA_REF) && node.get(MetaSchemaProperty.SCHEMA_REF).isTextual();
    }

    public static JsonReference getRef(String baseURI, JsonNode node) throws IOException {

        String refPath;
        String refValue = node.get(MetaSchemaProperty.SCHEMA_REF).asText();
        if (refValue == null)
            return null;

        if (baseURI == null || refValue.startsWith("#") || SchemaLoader.hasScheme(refValue))
            refPath = refValue;
        else
            refPath = Paths.get(baseURI, refValue).toFile().getCanonicalPath();
        return new JsonReference(refPath);
    }

    public static String getBaseURI(JsonNode schema) {
        String baseURI = null;
        // $id declares a base URI against which $ref URIs are resolved
        if (schema.has(MetaSchemaProperty.SCHEMA_ID)) {
            String idValue = schema.get(MetaSchemaProperty.SCHEMA_ID).asText();
            int index = idValue.lastIndexOf('/');
            baseURI = idValue.substring(0, index);
        }
        return baseURI;
    }
}
