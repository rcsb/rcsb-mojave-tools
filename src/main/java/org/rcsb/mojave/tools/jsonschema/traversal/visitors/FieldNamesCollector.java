package org.rcsb.mojave.tools.jsonschema.traversal.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import org.rcsb.mojave.tools.jsonschema.constants.TraversalLabel;
import org.rcsb.mojave.tools.jsonschema.traversal.model.TraversalContext;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.Visitable;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.VisitableNode;

import java.util.HashSet;
import java.util.Set;

/**
 * Visitor collects all field names of a given node.
 *
 * Created on 10/29/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class FieldNamesCollector implements Visitor {

    private Set<String> names = new HashSet<>();

    public Set<String> getNames() {
        return names;
    }

    @Override
    public void visit(Visitable visitableNode) {

        if ( !(visitableNode instanceof VisitableNode) )
            throw new IllegalArgumentException("Node object MUST be an instance of VisitableNode.");

        JsonNode node = ((VisitableNode) visitableNode).getNode();
        TraversalContext ctx = ((VisitableNode) visitableNode).getTraversalContext();

        if (!ctx.isRef() && ctx.getLabel().equals(TraversalLabel.PROPERTIES))
            node.fieldNames().forEachRemaining(name -> names.add(name));
    }
}
