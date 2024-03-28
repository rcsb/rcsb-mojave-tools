package org.rcsb.mojave.tools.jsonschema.resolver;

import com.fasterxml.jackson.databind.JsonNode;
import junit.framework.TestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rcsb.mojave.tools.jsonschema.SchemaLoader;
import org.rcsb.mojave.tools.jsonschema.SchemaRefResolver;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaType;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

    @Test
    public void shouldChaseReferences() throws IOException {
        URL source = TestSchemaRefResolver.class
                .getResource("/schema/resolving/json-schema-chase-refs.json");
        JsonNode schema = loader.readSchema(source);

        SchemaRefResolver resolver = new SchemaRefResolver(schema, loader);
        resolver.resolveInline();

        assertEquals(MetaSchemaType.STRING, schema.get(MetaSchemaProperty.PROPERTIES)
                .get("field1").get(MetaSchemaProperty.TYPE).asText());
        assertEquals(MetaSchemaType.STRING, schema.get(MetaSchemaProperty.PROPERTIES)
                .get("field2").get(MetaSchemaProperty.TYPE).asText());
    }

    private void setUpMockSchemainFileSystem(File tmpDir) throws IOException {
        InputStream isRootSchema = TestSchemaRefResolver.class.getResourceAsStream("/schema/resolving/json-schema-chase-refs.json");
        InputStream isDir1Schema = TestSchemaRefResolver.class.getResourceAsStream("/schema/resolving/dir1/json-schema-fragment-1.json");
        InputStream isDir2f2Schema = TestSchemaRefResolver.class.getResourceAsStream("/schema/resolving/dir2/json-schema-fragment-2.json");
        InputStream isDir2f3Schema = TestSchemaRefResolver.class.getResourceAsStream("/schema/resolving/dir2/json-schema-fragment-3.json");
        // creating dirs
        File dir1 = new File(tmpDir, "dir1"); dir1.deleteOnExit();
        dir1.mkdirs();
        File dir2 = new File(tmpDir, "dir2"); dir2.deleteOnExit();
        dir2.mkdirs();
        // creating files
        File f1 = new File(dir1, "json-schema-fragment-1.json"); f1.deleteOnExit();
        File f2 = new File(tmpDir, "json-schema-chase-refs.json"); f2.deleteOnExit();
        File f3 = new File(dir2, "json-schema-fragment-2.json"); f3.deleteOnExit();
        File f4 = new File(dir2, "json-schema-fragment-3.json"); f4.deleteOnExit();
        writeIsToFileModifyingId(isDir1Schema, dir1 + "/json-schema-fragment-1.json", f1);
        writeIsToFileModifyingId(isRootSchema, tmpDir + "/json-schema-chase-refs.json", f2);
        writeIsToFileModifyingId(isDir2f2Schema, null, f3);
        writeIsToFileModifyingId(isDir2f3Schema, null, f4);
    }

    private void writeIsToFileModifyingId(InputStream is, String id, File outFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is));
             PrintWriter out = new PrintWriter(outFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("  \"$id\":")) {
                    line = "  \"$id\": \"" + id + "\",";
                }
                out.println(line);
            }
        }
    }

    @Test
    public void shouldChaseReferencesWhenSchemaFromFileSystem() throws IOException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "rcsb-mojave-tools-test-tmp"); tmpDir.deleteOnExit();
        setUpMockSchemainFileSystem(tmpDir);

        JsonNode schema = loader.readSchema(new File(tmpDir, "json-schema-chase-refs.json"));

        SchemaRefResolver resolver = new SchemaRefResolver(schema, loader);
        resolver.resolveInline();

        assertEquals(MetaSchemaType.STRING, schema.get(MetaSchemaProperty.PROPERTIES)
                .get("field1").get(MetaSchemaProperty.TYPE).asText());
        assertEquals(MetaSchemaType.STRING, schema.get(MetaSchemaProperty.PROPERTIES)
                .get("field2").get(MetaSchemaProperty.TYPE).asText());
    }

}
