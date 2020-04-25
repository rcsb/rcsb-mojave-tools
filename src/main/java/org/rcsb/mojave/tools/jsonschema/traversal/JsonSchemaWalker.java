package org.rcsb.mojave.tools.jsonschema.traversal;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.tuple.Pair;
import org.rcsb.mojave.tools.jsonschema.SchemaRefResolver;
import org.rcsb.mojave.tools.jsonschema.constants.JsonPointerConstants;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;
import org.rcsb.mojave.tools.jsonschema.traversal.algorithm.JsonSchemaTraversal;
import org.rcsb.mojave.tools.jsonschema.traversal.builder.TreeBuilder;
import org.rcsb.mojave.tools.jsonschema.traversal.model.JsonReference;
import org.rcsb.mojave.tools.jsonschema.traversal.model.TraversalContext;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.VisitableNode;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.Visitor;
import org.rcsb.mojave.tools.jsonschema.utils.JsonSchemaNodeUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON schema tree traversal with visitors that can add/modify/remove
 * attributes of the tree nodes.
 *
 * Note: the combination of visitors is not necessarily commutative,
 * the visitor dependency should be explicitly resolved.
 *
 * Created on 8/16/19.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class JsonSchemaWalker {

    private boolean resolveRef;
    private SchemaRefResolver resolver;

    // $refs we see during resolution, necessary to detect $ref loops.
    private final Map<JsonReference, JsonNode> refs = new HashMap<>();

    private TraversalContext initCtx;
    private List<Visitor> visitors;
    private JsonSchemaTraversal.Strategy strategy;

    public static class Builder {

        private JsonNode schema;
        private TreeBuilder builder;
        private List<Visitor> visitors;
        private JsonSchemaTraversal.Strategy strategy;

        private boolean useTitle;
        private boolean deReference;

        public Builder() {}

        public Builder fromInstance(JsonNode schemaInstance) {
            schema = schemaInstance;
            return this;
        }

        public Builder acceptingVisitors(List<Visitor> visitors) {
            this.visitors = visitors;
            return this;
        }

        public Builder withSchemaTraversalStrategy(JsonSchemaTraversal.Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder withSchemaTreeBuilder(TreeBuilder builder) {
            this.builder = builder;
            return this;
        }

        public Builder withSchemaTitleAsName(boolean flag) {
            useTitle = flag;
            return this;
        }

        public Builder withDynamicRefResolution(boolean flag) {
            this.deReference = flag;
            return this;
        }

        public JsonSchemaWalker build() {

            if (schema == null)
                throw new IllegalStateException("Schema instance MUST be provided to the walker.");

            JsonSchemaWalker walker = new JsonSchemaWalker();

            // ==== Traversal Strategy ====
            if (strategy != null)
                walker.strategy = strategy;

            // ==== Traversal Context ====
            TraversalContext initCtx = new TraversalContext();
            initCtx.setSchema(schema);
            initCtx.setJsonPointer(JsonPointerConstants.ROOT);

            if (useTitle) {
                String title = schema.get(MetaSchemaProperty.TITLE).asText();
                initCtx.setTitleIncluded(true);
                initCtx.getLineage().add(title);
            }

            if (builder != null)
                initCtx.setBuilder(builder);

            if (deReference) {
                if (!schema.has(MetaSchemaProperty.SCHEMA_ID))
                    throw new IllegalStateException("The $id MUST be added at the top level of the referrer schema " +
                            "in order to ensure correct resolution of JSON references.");
                walker.resolveRef = true;
                walker.resolver = new SchemaRefResolver(schema);
            }

            walker.initCtx = initCtx;
            walker.visitors = visitors;

            return walker;
        }
    }

    public TreeBuilder getTreeBuilder() {
        return this.initCtx.getBuilder();
    }

    public void setVisitors(List<Visitor> visitors) {
        this.visitors = visitors;
    }

    private void acceptVisitors(JsonNode node, TraversalContext ctx) {

        if (visitors == null || visitors.isEmpty())
            return;

        VisitableNode vNode = new VisitableNode();
        vNode.setNode(node);
        vNode.setTraversalContext(ctx);

        for (Visitor visitor : visitors)
            vNode.accept(visitor);
    }

    /**
     * Resolves and replaces JSON schema $refs with it's resolved schema definition.
     * Recursively walks the resolved schema, converting every instance of $ref in the
     * json-schema node. This method mutates the JSON schema object.
     *
     * @param node JSON schema node with $ref
     * @param ctx traversal context
     */
    private JsonNode dereference(JsonNode node, TraversalContext ctx) throws IOException {

        String baseURI = JsonSchemaNodeUtils.getBaseURI(ctx.getSchema());
        JsonReference ref = JsonSchemaNodeUtils.getRef(baseURI, node);

        if (ref == null || refs.containsKey(ref))
            return null;

        JsonNode resolvedNode = resolver.resolve(ref);
        refs.put(ref, resolvedNode);

        return resolvedNode;
    }

    private void walk(JsonSchemaTraversal traversal) throws IOException {
        while (traversal.hasNext()) {
            Pair<JsonNode, TraversalContext> item = traversal.next();
            acceptVisitors(item.getLeft(), item.getRight());
            if (JsonSchemaNodeUtils.isRef(item.getLeft()) && resolveRef) {
                JsonNode refSchema = dereference(item.getLeft(), item.getRight());
                if (refSchema != null) {
                    JsonSchemaTraversal refTraversal = new JsonSchemaTraversal();
                    refTraversal.setTraversalStrategy(traversal.getTraversalStrategy());
                    refTraversal.traverse(refSchema, item.getRight());
                    walk(refTraversal);
                }
            }
        }
    }

    public void walk() throws IOException {
        JsonSchemaTraversal traversal = new JsonSchemaTraversal();
        traversal.setTraversalStrategy(strategy);
        traversal.traverse(initCtx.getSchema(), initCtx);
        walk(traversal);
    }
}
