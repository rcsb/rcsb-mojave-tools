package org.rcsb.mojave.tools.jsonschema.traversal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rcsb.mojave.tools.jsonschema.SchemaLoader;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.*;
import org.rcsb.mojave.tools.jsonschema.utils.SchemaVersion;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests for {@link JsonSchemaWalker} traversal and available visitors.
 *
 * Created on 8/30/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class TestVisitableSchemaTree {

    private static SchemaLoader loader;

    @BeforeClass
    public static void configure() {
        loader = new SchemaLoader();
    }

    @Test
    public void shouldRemoveUnqualifiedKeywords() throws IOException {

        URL source = TestVisitableSchemaTree.class.getResource("/schema/traversal/json-schema-syntax-error.json");
        JsonNode schema = loader.readSchema(source);

        KeywordsSyntaxChecker visitor = new KeywordsSyntaxChecker(SchemaVersion.DRAFTV4.getSchema());

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .acceptingVisitors(singletonList(visitor))
                .build();
        walker.walk();

        assertTrue(schema.findValue("properties").has("id"));
        assertFalse(schema.get("properties").get("bad_node").has("bad_key"));
    }

    @Test
    public void shouldGenerateValidationSchema() throws IOException {

        URL source = TestVisitableSchemaTree.class.getResource("/schema/traversal/json-schema-json-to-bson.json");
        JsonNode schema = loader.readSchema(source);

        List<Visitor> visitors = asList(
                new KeywordsSyntaxChecker(SchemaVersion.DRAFTV4.getSchema()),
                new BsonTypeAliasConverter());

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .acceptingVisitors(visitors)
                .build();
        walker.walk();

        assertTrue(schema.get("properties").get("field1").has("bsonType"));
        assertTrue(schema.get("properties").get("field1").has("description"));
        assertFalse(schema.get("properties").get("field1").has("unwanted_meta"));

        assertTrue(schema.get("properties").get("field2").get("properties").get("field3").has("bsonType"));
        assertFalse(schema.get("properties").get("field2").get("properties").get("field3").has("unwanted_meta"));

        assertTrue(schema.get("properties").get("field3").has("bsonType"));
        assertTrue(schema.get("properties").get("field3").get("items").has("bsonType"));
        assertFalse(schema.get("properties").get("field3").get("items").has("unwanted_meta"));

        assertTrue(schema.get("properties").get("field5").has("bsonType"));
        assertTrue(schema.get("properties").get("field5").get("items").has("anyOf"));
    }

    @Test
    public void shouldAnnotateWithJavaTypes() throws IOException {

        URL source = TestVisitableSchemaTree.class
                .getResource("/schema/traversal/json-schema-java-types.json");
        JsonNode schema = loader.readSchema(source);

        JavaTypeAnnotator visitor = new JavaTypeAnnotator().setTargetPackage("org.test");

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .acceptingVisitors(singletonList(visitor))
                .build();
        walker.walk();

        JsonNode node1 = schema.path("properties").path("object_node");
        assertEquals("org.test.ObjectNode", node1.get("javaType").textValue());

        JsonNode node2 = schema.path("properties").path("array_node").path("items")
                .path("properties").path("array_object");
        assertEquals("org.test.ArrayNodeArrayObject", node2.get("javaType").textValue());
    }

    @Test
    public void shouldChangeNamingStrategy() throws IOException {

        URL source = TestVisitableSchemaTree.class
                .getResource("/schema/traversal/json-schema-naming-visitor.json");
        JsonNode schema = loader.readSchema(source);

        Visitor visitor = new NamingStrategyVisitor(CaseFormat.UPPER_CAMEL, CaseFormat.LOWER_UNDERSCORE);
        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .acceptingVisitors(singletonList(visitor))
                .build();
        walker.walk();

        assertTrue(StreamSupport.stream(schema.get("required").spliterator(), false)
                .anyMatch(name -> "object_node_camel_case".equals(name.textValue())));
        assertTrue(schema.findValue("object_node_camel_case").get("properties").has("complex_name"));
    }

    @Test
    public void shouldAddNamespaceToAll() throws IOException {

        URL source = TestVisitableSchemaTree.class
                .getResource("/schema/traversal/json-schema-namespace-visitor.json");
        JsonNode schema = loader.readSchema(source);

        NamespaceVisitor visitor = new NamespaceVisitor()
                .atLevel(NamespaceVisitor.Level.ALL)
                .withPrefix("rcsb").withDelimiter("_");

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .acceptingVisitors(singletonList(visitor))
                .build();
        walker.walk();

        assertTrue(schema.get("properties").has("rcsb_field_1"));
        assertTrue(schema.findValue("rcsb_field_1").get("properties").has("rcsb_field_2"));
    }

    @Test
    public void shouldAddNamespaceToRootOnly() throws IOException {

        URL source = TestVisitableSchemaTree.class
                .getResource("/schema/traversal/json-schema-namespace-visitor.json");
        JsonNode schema = loader.readSchema(source);

        NamespaceVisitor visitor = new NamespaceVisitor()
                .atLevel(NamespaceVisitor.Level.ROOT)
                .withPrefix("rcsb").withDelimiter("_");

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .acceptingVisitors(singletonList(visitor))
                .build();
        walker.walk();

        assertTrue(schema.get("properties").has("rcsb_field_1"));
        assertTrue(schema.get("properties").get("rcsb_field_1").get("properties").has("field_2"));
    }

    @Test
    public void shouldChangeNamespaceInRequired() throws IOException {

        URL source = TestVisitableSchemaTree.class
                .getResource("/schema/traversal/json-schema-namespace-visitor.json");
        JsonNode schema = loader.readSchema(source);

        NamespaceVisitor visitor = new NamespaceVisitor()
                .atLevel(NamespaceVisitor.Level.ROOT)
                .withPrefix("rcsb").withDelimiter("_");

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .acceptingVisitors(singletonList(visitor))
                .build();
        walker.walk();

        assertTrue(StreamSupport.stream(schema.get("required").spliterator(), false)
                .anyMatch(name -> "rcsb_field_1".equals(name.textValue())));
    }

    @Test
    public void shouldCollectAllNames() throws Exception {

        URL source = TestVisitableSchemaTree.class
                .getResource("/schema/traversal/json-schema-collect-names.json");
        JsonNode schema = loader.readSchema(source);

        FieldNamesCollector visitor = new FieldNamesCollector();

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .acceptingVisitors(singletonList(visitor))
                .build();
        walker.walk();

        Set<String> names = visitor.getNames();

        assertEquals(5, names.size());
        assertTrue(names.contains("field1"));
        assertTrue(names.contains("field2"));
        assertTrue(names.contains("field3"));
        assertTrue(names.contains("field4"));
        assertTrue(names.contains("field5"));
    }

    @Test
    public void shouldCollectEnumsFromParentOnly() throws IOException {

        URL source = TestVisitableSchemaTree.class
                .getResource("/schema/traversal/json-schema-collect-enums.json");
        JsonNode schema = loader.readSchema(source);

        EnumCollector visitor = new EnumCollector();

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .acceptingVisitors(singletonList(visitor))
                .build();
        walker.walk();

        Set<JsonNode> enumNodes = visitor.getSelectedNodes();

        assertEquals(3, enumNodes.size());
    }

    @Test
    public void shouldCollectEnumsIgnoringDefinitions() throws IOException {

        URL source = TestVisitableSchemaTree.class
                .getResource("/schema/reference/json-schema-with-definitions.json");
        JsonNode schema = loader.readSchema(source);

        EnumCollector visitor = new EnumCollector();

        JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                .fromInstance(schema)
                .acceptingVisitors(singletonList(visitor))
                .build();
        walker.walk();

        Set<JsonNode> enumNodes = visitor.getSelectedNodes();

        assertEquals(0, enumNodes.size());
    }
}
