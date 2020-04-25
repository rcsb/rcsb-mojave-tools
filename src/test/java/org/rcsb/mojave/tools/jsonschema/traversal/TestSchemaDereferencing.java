package org.rcsb.mojave.tools.jsonschema.traversal;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rcsb.mojave.tools.jsonschema.SchemaLoader;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.FieldNamesCollector;

import java.io.IOException;
import java.net.URL;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertTrue;

/**
 * Created on 8/2/19.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class TestSchemaDereferencing {

    private static SchemaLoader loader;

    @BeforeClass
    public static void configure() {
        loader = new SchemaLoader();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionNoDefinition() throws IOException {

        URL source = TestVisitableSchemaTree.class
                .getResource("/schema/reference/json-schema-no-definitions-bad.json");
        JsonNode schema = loader.readSchema(source);

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .withDynamicRefResolution(true)
                .build();
        walker.walk();
    }


    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionBadDefinition() throws IOException {

        URL source = TestVisitableSchemaTree.class
                .getResource("/schema/reference/json-schema-with-definitions-bad.json");
        JsonNode schema = loader.readSchema(source);

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .withDynamicRefResolution(true)
                .build();
        walker.walk();
    }

    @Test
    public void shouldResolveReferences() throws IOException {

        URL source = TestVisitableSchemaTree.class
                .getResource("/schema/reference/json-schema-with-circular-references.json");
        JsonNode schema = loader.readSchema(source);

        FieldNamesCollector visitor = new FieldNamesCollector();

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .acceptingVisitors(singletonList(visitor))
                .withDynamicRefResolution(true)
                .build();
        walker.walk();

        assertTrue(visitor.getNames().contains("complex_field"));
    }

    @Test
    public void shouldResolveReferencesFromFile() throws IOException {

        URL source = TestVisitableSchemaTree.class
                .getResource("/schema/reference/json-schema-parent.json");
        JsonNode schema = loader.readSchema(source);

        FieldNamesCollector visitor = new FieldNamesCollector();

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .acceptingVisitors(singletonList(visitor))
                .withDynamicRefResolution(true)
                .build();
        walker.walk();

        assertTrue(visitor.getNames().contains("parent_name"));
        assertTrue(visitor.getNames().contains("children"));
    }

    @Test
    public void shouldResolveFragmentFromFile() throws IOException {

        URL source = TestVisitableSchemaTree.class
                .getResource("/schema/reference/json-schema-with-external-ref.json");
        JsonNode schema = loader.readSchema(source);

        FieldNamesCollector visitor = new FieldNamesCollector();

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .acceptingVisitors(singletonList(visitor))
                .withDynamicRefResolution(true)
                .build();
        walker.walk();

        assertTrue(visitor.getNames().contains("simple_field"));
        assertTrue(visitor.getNames().contains("ref_field"));
        assertTrue(visitor.getNames().contains("non_ref_field"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldResolveFragmentFromFileNoId() throws IOException {

        URL source = TestVisitableSchemaTree.class
                .getResource("/schema/reference/json-schema-with-external-ref-no-id.json");
        JsonNode schema = loader.readSchema(source);

        FieldNamesCollector visitor = new FieldNamesCollector();

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .acceptingVisitors(singletonList(visitor))
                .withDynamicRefResolution(true)
                .build();
        walker.walk();
    }
}
