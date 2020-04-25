package org.rcsb.mojave.tools.jsonschema.traversal.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import org.rcsb.mojave.tools.jsonschema.constants.TraversalLabel;
import org.rcsb.mojave.tools.jsonschema.traversal.JsonSchemaWalker;
import org.rcsb.mojave.tools.jsonschema.traversal.model.TraversalContext;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.Visitable;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.VisitableNode;
import org.rcsb.mojave.tools.jsonschema.utils.JsonSchemaNodeUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements the visit operation defined in the {@link Visitor} interface that can be applied in
 * {@link JsonSchemaWalker#walk()}.
 *
 * Created on 01/30/19.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class EnumCollector implements Visitor {

    private Set<JsonNode> selected = new HashSet<>();

    /**
     * This operation picks enum descriptions, if present in a given node of JSON schema.
     *
     * @param visitableNode the visitable node (wrapper around concrete JSON schema node implementation).
     */
    @Override
    public void visit(Visitable visitableNode) {

        if ( !(visitableNode instanceof VisitableNode) )
            throw new IllegalArgumentException("Node object MUST be an instance of VisitableNode.");

        JsonNode node = ((VisitableNode) visitableNode).getNode();
        TraversalContext ctx = ((VisitableNode) visitableNode).getTraversalContext();

        if (!ctx.isRef()
                && (ctx.getLabel().equals(TraversalLabel.ATTRIBUTE)
                    || ctx.getLabel().equals(TraversalLabel.ITEMS))
                && JsonSchemaNodeUtils.isEnum(node))
            selected.add(node);
    }

    public Set<JsonNode> getSelectedNodes() {
        return selected;
    }
}
