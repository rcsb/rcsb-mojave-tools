package org.rcsb.mojave.tools.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests to ensure an expected behaviour of JSON schemas stitching process.
 *
 * Created on 9/27/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class TestSchemaStitching {

    private static JsonNode targetSchema;
    private static final SchemaLoader loader = new SchemaLoader();

    @BeforeClass
    public static void configure() throws IOException {

        URL targetSchemaURL = TestSchemaStitching.class.getResource("/schema/stitching/target_json_schema.json");
        targetSchema = loader.readSchema(targetSchemaURL);

        URL updateSchemaURL = TestSchemaStitching.class.getResource("/schema/stitching/update_json_schema.json");
        JsonNode updateSchema = loader.readSchema(updateSchemaURL);

        SchemaStitching.mergeSchemas(targetSchema, updateSchema);
    }

    @Test
    public void shouldUpdateObjectsWithNewFields() {

        assertTrue(targetSchema.get("properties").has("target_object_node"));
        assertTrue(targetSchema.get("properties").has("update_object_node"));
    }

    @Test
    public void shouldUpdateArraysWithNewFields() {

        assertTrue(targetSchema
                .get("properties").get("common_array_node")
                .get("items").get("properties").has("target_element"));

        assertTrue(targetSchema
                .get("properties").get("common_array_node")
                .get("items").get("properties").has("update_element"));
    }

    @Test
    public void shouldMergeValuesSimpleArrays() {

        // should merge simple arrays, e.g. 'required' field
        List<String> requiredFields = new ArrayList<>();
        targetSchema.get("required").iterator().forEachRemaining(fName -> requiredFields.add(fName.textValue()));
        assertTrue(requiredFields.contains("target_object_node"));
        assertTrue(requiredFields.contains("update_object_node"));
    }

    @Test
    public void shouldPreferUpdateIfConflict() {
        assertEquals("integer", targetSchema.get("properties").get("conflict_node").get("type").textValue());
    }

    @Test
    public void shouldMergeEnums() {
        List<String> enumValues = new ArrayList<>();
        targetSchema.get("properties").get("enum_node_common").get("enum")
                .iterator().forEachRemaining(fName -> enumValues.add(fName.textValue()));
        assertTrue(enumValues.contains("A"));
        assertTrue(enumValues.contains("B"));
        assertTrue(enumValues.contains("C"));
    }
}
