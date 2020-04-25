package org.rcsb.mojave.tools.jsonschema.traversal.visitables;

import com.fasterxml.jackson.databind.JsonNode;
import org.rcsb.mojave.tools.jsonschema.traversal.model.TraversalContext;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.Visitor;

/**
 * Concrete implementation of the {@link Visitable} interface that allows visit Json nodes.
 *
 * Created on 8/27/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class VisitableNode implements Visitable {

    private String id;
    private JsonNode node;
    private TraversalContext ctx;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JsonNode getNode() {
        return node;
    }

    public void setNode(JsonNode node) {
        this.node = node;
    }

    public TraversalContext getTraversalContext() {
        return ctx;
    }

    public void setTraversalContext(TraversalContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}