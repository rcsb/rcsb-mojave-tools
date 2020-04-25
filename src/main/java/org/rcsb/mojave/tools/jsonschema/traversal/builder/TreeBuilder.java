package org.rcsb.mojave.tools.jsonschema.traversal.builder;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Created on 8/22/19.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public interface TreeBuilder {

    void add(JsonPointer path, JsonNode node);

    JsonNode buildTree() throws IOException;
}
