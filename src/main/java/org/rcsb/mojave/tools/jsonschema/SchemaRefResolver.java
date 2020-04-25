package org.rcsb.mojave.tools.jsonschema;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.Pair;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;
import org.rcsb.mojave.tools.jsonschema.traversal.algorithm.JsonSchemaTraversal;
import org.rcsb.mojave.tools.jsonschema.traversal.model.JsonReference;
import org.rcsb.mojave.tools.jsonschema.traversal.model.TraversalContext;
import org.rcsb.mojave.tools.jsonschema.utils.JsonSchemaNodeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *   Aims at resolving all JSON References until a final document is reached.
 *   It will throw an exception if a JSON Reference loop is detected, or if a
 *   JSON Reference does not resolve.
 *</p>
 *
 * <p>
 *   It relies on a {@link SchemaLoader} to load JSON References which are
 *   not resolvable within the current schema itself.
 * </p>
 *
 * <p>
 *   The only supported mode is "inline" resolution. This means input schema
 *   will be modified inplace and resolved "$ref" fragments will incorporated
 *   into the original schema.
 * </p>
 *
 * Created on 1/12/20.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class SchemaRefResolver {

    private JsonNode currentTree;
    private SchemaLoader loader;

    /*
     * The set of refs we see during ref resolution, necessary to detect ref
     * loops. We make it linked since we want the ref path reported in the
     * order where refs have been encountered.
     */
    private final Set<JsonReference> refs = new HashSet<>();

    public SchemaRefResolver(JsonNode jSchema) {
        currentTree = jSchema;
        loader = new SchemaLoader();
    }

    public SchemaRefResolver(JsonNode jSchema, SchemaLoader schemaLoader) {
        currentTree = jSchema;
        loader = schemaLoader;
    }

    private void resolve(JsonNode node) throws IOException {

        String baseURI = JsonSchemaNodeUtils.getBaseURI(currentTree);
        JsonReference ref = JsonSchemaNodeUtils.getRef(baseURI, node);
        if (ref == null)
            return;

        // Check for cyclic dependencies to prevent such structures from resulting in infinite
        // recursion or iteration. If we have seen this ref already, this is a ref loop.
        if (!refs.add(ref))
            throw new UnsupportedOperationException("This $ref [ "+ref.getFullPath()
                    +"] has been seen already.");

        // Resolution of a JSON Reference object yields the referenced JSON value.
        JsonNode resolved = resolve(ref);

        // Any members other than "$ref" in a JSON Reference object SHALL be ignored.
        ((ObjectNode) node).removeAll();

        // Implementations chooses to replace the reference with he referenced value.
        ((ObjectNode) node).setAll((ObjectNode) resolved);
    }

    private void combineAllOf(JsonNode node) {

        // because we use post order strategy, at this point fragments were resolved
        // and it's possible to create an explicit combination
        List<JsonNode> nodeList = new ArrayList<>();
        node.get(MetaSchemaProperty.ALL_OF).iterator().forEachRemaining(nodeList::add);
        JsonNode combined = SchemaStitching.mergeSchemas(nodeList);

        // Any members other than "$ref" in a JSON Reference object SHALL be ignored.
        ((ObjectNode) node).removeAll();

        // Implementations chooses to replace the reference with he referenced value.
        ((ObjectNode) node).setAll((ObjectNode) combined);
    }

    /**
     * Resolves a JSON Reference through locating and fetching corresponding schema fragment.
     * Resolution of the following references is supported:
     * {
     *   "person": {
     *     // references an external file
     *     "$ref": "schemas/people/Bruce-Wayne.json"
     *   },
     *   "place": {
     *     // references a sub-schema in an external file
     *     "$ref": "schemas/places.yaml#/definitions/Gotham-City"
     *   },
     *   "thing": {
     *     // references a URL
     *     "$ref": "http://wayne-enterprises.com/things/batmobile"
     *   }
     * }
     * @param ref JSON Reference.
     * @return schema fragment as a result of resolution of $ref.
     */
    public JsonNode resolve(JsonReference ref) {

        // Check whether $ref must be resolved within the current tree
        if ( ref.getLocator() == null )
            return resolveFragment(ref, currentTree);

        // If not, fetch a new tree
        else {
            try {
                JsonNode schema = loader.readSchema(ref.getURI());
                return resolveFragment(ref, schema);
            } catch (IOException ioe) {
                throw new IllegalArgumentException("Failed to resolve "+ref.getFragment()+" at "+
                        ref.getLocator()+". Error: " + ioe.getMessage());
            }
        }
    }

    private JsonNode resolveFragment(JsonReference ref, JsonNode schema) {

        if (ref.getFragment() == null)
            return schema;

        JsonPointer pointer = JsonPointer.compile(ref.getFragment());
        try {
            JsonNode resolved = schema.at(pointer);
            if (resolved.isMissingNode())
                throw new IllegalArgumentException("Pointer [ "+ref.getFragment()+" ] references is a dangling reference.");
            return resolved;
        } catch (Exception e) {
            throw new IllegalArgumentException("Pointer [ "+ref.getFragment()+" ] cannot be compiled.");
        }
    }

    public void resolveInline() throws IOException {
        JsonSchemaTraversal traversal = new JsonSchemaTraversal();
        traversal.setTraversalStrategy(JsonSchemaTraversal.Strategy.POST_ORDER);

        traversal.traverse(currentTree);
        while (traversal.hasNext()) {
            Pair<JsonNode, TraversalContext> item = traversal.next();
            JsonNode n = item.getLeft();
            if (JsonSchemaNodeUtils.isRef(n)) {
                resolve(n);
                traversal.traverse(n, item.getRight());
            } else if (n.has(MetaSchemaProperty.ALL_OF)) {
                combineAllOf(n);
            } else {
                //TODO Check if this could lead to cycles. It allows objects to be referenced twice while checking reference id in recurrent $refs
                refs.clear();
            }
        }
    }
}
