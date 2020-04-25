package org.rcsb.mojave.tools.jsonschema.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.rcsb.mojave.tools.jsonschema.SchemaLoader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Loads a JSON representation of JSON Schema meta-schemas (or drafts).
 *
 * Created on 8/23/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public enum SchemaVersion {

    /**
     * Draft v7
     */
    //DRAFTV7("https://raw.githubusercontent.com/json-schema-org/json-schema-spec/draft-07/schema.json"),
    DRAFTV7("/json-schema-draft/json-schema-spec-draft-07.json"),
    /**
     * Draft v4 (default version)
     */
    //DRAFTV4("https://raw.githubusercontent.com/json-schema-org/json-schema-spec/draft-04/schema.json"),
    DRAFTV4("/json-schema-draft/json-schema-spec-draft-04.json"),
    /**
     * Draft v3
     */
    //DRAFTV3("https://raw.githubusercontent.com/json-schema-org/json-schema-spec/draft-03/schema.json"),
    DRAFTV3("/json-schema-draft/json-schema-spec-draft-03.json");

    private final URI location;
    private final JsonNode schema;

    SchemaVersion(final String uri) {
        try {
            location = new URI(uri);
            SchemaLoader loader = new SchemaLoader();
            schema = loader.readSchema(location);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Return the value of {@code $schema} as a {@link URL}
     *
     * @return the JSON Reference for that schema version
     */
    public URI getLocation() {
        return location;
    }

    /**
     * Return the meta schema as JSON
     *
     * Note: since {@link JsonNode} is mutable, this method returns a copy.
     *
     * @return the meta schema
     * @see JsonNode#deepCopy()
     */
    public JsonNode getSchema() {
        return schema.deepCopy();
    }
}
