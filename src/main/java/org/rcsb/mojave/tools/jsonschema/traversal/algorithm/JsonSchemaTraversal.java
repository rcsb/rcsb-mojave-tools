package org.rcsb.mojave.tools.jsonschema.traversal.algorithm;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.tuple.Pair;
import org.rcsb.mojave.tools.jsonschema.constants.JsonPointerConstants;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;
import org.rcsb.mojave.tools.jsonschema.constants.TraversalLabel;
import org.rcsb.mojave.tools.jsonschema.traversal.model.TraversalContext;
import org.rcsb.mojave.tools.jsonschema.utils.JsonSchemaNodeUtils;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * JSON schema tree traversal supports the following strategies:
 * <li>{@link Strategy#PRE_ORDER}</li>
 * <li>{@link Strategy#POST_ORDER}</li>
 *
 * (1) Visiting nodes of the following tree with {@link Strategy#PRE_ORDER}:
 *          1
 *         / \
 *        2   3
 *            \
 *            4
 *           / \
 *          5   6
 * will happen in the following order: 1 2 3 4 5 6 (pre-order traversal)
 *
 * (2) Visiting nodes of the following tree with {@link Strategy#POST_ORDER}:
 *          1
 *         / \
 *        2   3
 *            \
 *            4
 *           / \
 *          5   6
 *
 * will happen in the following order: 6 5 4 3 2 1 (post-order right-to-left traversal)
 *
 * Created on 1/12/20.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class JsonSchemaTraversal implements Iterator<Pair<JsonNode, TraversalContext>> {

    private Strategy strategy = Strategy.POST_ORDER; // default traversal strategy
    private LinkedList<Pair<JsonNode, TraversalContext>> queue;

    public JsonSchemaTraversal() {
        queue = new LinkedList<>();
    }

    public void setTraversalStrategy(Strategy strategy) {
        if (strategy != null)
            this.strategy = strategy;
    }

    public Strategy getTraversalStrategy() {
        return this.strategy;
    }

    private void setLabel(JsonNode node, TraversalContext ctx) {
        if (JsonSchemaNodeUtils.isObject(node))
            ctx.setLabel(TraversalLabel.OBJECT);
        else if (JsonSchemaNodeUtils.isArray(node))
            ctx.setLabel(TraversalLabel.ARRAY);
        else
            ctx.setLabel(TraversalLabel.ATTRIBUTE);
    }

    private TraversalContext buildPropertySchemaCxt(String name, JsonNode node, TraversalContext ctx) {

        TraversalContext ctxCopy = ctx.deepCopy();

        if (JsonSchemaNodeUtils.isRef(node)) {
            ctxCopy.setRef(true);
            ctxCopy.setLabel(null);
        } else {
            ctxCopy.setRef(false);
            ctxCopy.setJsonPointer(ctx.getJsonPointer()
                    .append(JsonPointer.compile(JsonPointer.SEPARATOR+name)));
            ctxCopy.getLineage().add(name);
        }

        return ctxCopy;
    }

    private void traverseProperty(String name, JsonNode node, TraversalContext ctx) {

        TraversalContext ctxAttr = buildPropertySchemaCxt(name, node, ctx);
        traverse(node, ctxAttr);
    }

    private TraversalContext buildPropertiesSchemaCxt(TraversalContext ctx) {

        TraversalContext ctxCopy = ctx.deepCopy();
        ctxCopy.setRef(false);
        ctxCopy.setLabel(TraversalLabel.PROPERTIES);
        ctxCopy.setJsonPointer(ctx.getJsonPointer()
                .append(JsonPointer.compile(JsonPointer.SEPARATOR+MetaSchemaProperty.PROPERTIES)));

        return ctxCopy;
    }

    private void traversePropertiesSchema(JsonNode node, TraversalContext ctx) {

        TraversalContext ctxCopy = buildPropertiesSchemaCxt(ctx);
        JsonNode properties = node.get(MetaSchemaProperty.PROPERTIES);
        queue.add(Pair.of(properties, ctxCopy));
        properties.fieldNames().forEachRemaining(name -> traverseProperty(name, properties.get(name), ctxCopy));
    }

    private TraversalContext buildArraySchemaCtx(TraversalContext ctx) {

        TraversalContext ctxCopy = ctx.deepCopy();
        ctxCopy.setRef(false);
        ctxCopy.setJsonPointer(ctx.getJsonPointer().append(JsonPointerConstants.ARRAY));

        return ctxCopy;
    }

    private void traverseArraySchema(JsonNode node, TraversalContext ctx) {

        JsonNode array = node.get(MetaSchemaProperty.ITEMS);
        TraversalContext ctxCopy = buildArraySchemaCtx(ctx);
        traverse(array, ctxCopy);
    }

    private TraversalContext buildCombinedSchemaCtx(String keyword, TraversalContext ctx) {

        TraversalContext ctxCopy = ctx.deepCopy();
        ctxCopy.setRef(false);
        ctxCopy.setLabel(TraversalLabel.COMBINED);
        ctxCopy.setJsonPointer(ctx.getJsonPointer()
                .append(JsonPointer.compile(JsonPointer.SEPARATOR+keyword)));
        return ctxCopy;
    }

    private void traverseCombinedSchema(JsonNode node, TraversalContext ctx) {

        String keyword;
        if (node.has(MetaSchemaProperty.ANY_OF))
            keyword = MetaSchemaProperty.ANY_OF;
        else if (node.has(MetaSchemaProperty.ONE_OF))
            keyword = MetaSchemaProperty.ONE_OF;
        else
            keyword = MetaSchemaProperty.ALL_OF;

        node = node.get(keyword);
        TraversalContext ctxCopy = buildCombinedSchemaCtx(keyword, ctx);

        queue.add(Pair.of(node, ctxCopy));
        traverse(node, ctxCopy);
    }

    public void traverse(JsonNode node) {

        TraversalContext initCtx = new TraversalContext();
        initCtx.setSchema(node);
        initCtx.setJsonPointer(JsonPointerConstants.ROOT);

        traverse(node, initCtx);
    }

    /**
     * Recursively traverses through JSON schema tree, visiting each and every
     * existing node, without revisiting a node that has already been traversed.
     *
     * Note: {@link JsonNode} is mutable, this method modifies schema in place.
     */
    public void traverse(JsonNode node, TraversalContext ctx) {

        setLabel(node, ctx);
        queue.add(Pair.of(node, ctx));

        if (node.isArray()) {
            for (JsonNode aNode : node)
                traverse(aNode, ctx);
        } else if (node.isObject()) {
            if (node.has(MetaSchemaProperty.PROPERTIES))
                traversePropertiesSchema(node, ctx);
            else if (JsonSchemaNodeUtils.isArray(node))
                traverseArraySchema(node, ctx);
            else if (JsonSchemaNodeUtils.isComposite(node))
                traverseCombinedSchema(node, ctx);
            else if (JsonSchemaNodeUtils.isMultiType(node)) {
                // TODO: handle multiple types declared through the "type" keyword
            }
        } else if (node.isBoolean()) {
            //TODO: handle boolean node
        }
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public Pair<JsonNode, TraversalContext> next() {

        switch (strategy) {
            case PRE_ORDER:
                return queue.pollFirst();
            case POST_ORDER:
                return queue.pollLast();
            default:
                throw new UnsupportedOperationException("Unsupported traversal order: "+strategy.name());
        }
    }

    public enum Strategy {
        PRE_ORDER,
        POST_ORDER
    }
}
