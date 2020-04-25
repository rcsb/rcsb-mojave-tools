package org.rcsb.mojave.tools.jsonschema.resolver;

import com.fasterxml.jackson.databind.JsonNode;
import junit.framework.TestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rcsb.mojave.tools.jsonschema.SchemaLoader;
import org.rcsb.mojave.tools.jsonschema.SchemaRefResolver;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created on 1/12/20.
 *
 * @author Yana Valasatava
 * @since 4.0.0
 */
public class TestSchemaRefResolver {

    private static SchemaLoader loader;

    @BeforeClass
    public static void configure() {
        loader = new SchemaLoader();
    }

    @Test
    public void shouldNotChangeSchemaWithoutRefs() throws IOException {

        URL source = TestSchemaRefResolver.class
                .getResource("/schema/resolving/json-schema-no-refs.json");
        JsonNode actual = loader.readSchema(source);

        SchemaRefResolver resolver = new SchemaRefResolver(actual, loader);
        resolver.resolveInline();

        JsonNode expected = loader.readSchema(source);

        TestCase.assertEquals(expected, actual);
    }

    @Test
    public void shouldResolveLocalFragment() throws IOException {

        URL source = TestSchemaRefResolver.class
                .getResource("/schema/resolving/json-schema-with-local-fragment.json");
        JsonNode schema = loader.readSchema(source);

        SchemaRefResolver resolver = new SchemaRefResolver(schema, loader);
        resolver.resolveInline();

        assertTrue(schema.get(MetaSchemaProperty.PROPERTIES).get("field").has(MetaSchemaProperty.TYPE));
        assertEquals("string", schema.get(MetaSchemaProperty.PROPERTIES).get("field")
                .get(MetaSchemaProperty.TYPE).asText());
    }

    @Test
    public void shouldResolveExternalFragment() throws IOException {
        URL source = TestSchemaRefResolver.class
                .getResource("/schema/resolving/json-schema-with-external-fragment.json");
        JsonNode schema = loader.readSchema(source);

        SchemaRefResolver resolver = new SchemaRefResolver(schema, loader);
        resolver.resolveInline();

        assertTrue(schema.get(MetaSchemaProperty.PROPERTIES).get("field").has(MetaSchemaProperty.TYPE));
        assertEquals("string", schema.get(MetaSchemaProperty.PROPERTIES).get("field")
                .get(MetaSchemaProperty.TYPE).asText());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowExceptionOnLoop() throws IOException {

        URL source = TestSchemaRefResolver.class
                .getResource("/schema/resolving/json-schema-with-circular-ref.json");
        JsonNode schema = loader.readSchema(source);

        SchemaRefResolver resolver = new SchemaRefResolver(schema, loader);
        resolver.resolveInline();
    }

    @Test
    public void shouldResolveAllOfExtension() throws IOException {
        URL source = TestSchemaRefResolver.class
                .getResource("/schema/resolving/json-schema-with-allOf-extension.json");
        JsonNode schema = loader.readSchema(source);

        SchemaRefResolver resolver = new SchemaRefResolver(schema, loader);
        resolver.resolveInline();

        assertTrue(schema.get(MetaSchemaProperty.PROPERTIES).get("field")
                .get(MetaSchemaProperty.PROPERTIES).has("integer_field"));
        assertTrue(schema.get(MetaSchemaProperty.PROPERTIES)
                .get("field").get(MetaSchemaProperty.PROPERTIES).has("number_field"));
    }
}
