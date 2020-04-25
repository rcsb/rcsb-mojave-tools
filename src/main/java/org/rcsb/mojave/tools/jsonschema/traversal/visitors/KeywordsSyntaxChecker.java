package org.rcsb.mojave.tools.jsonschema.traversal.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;
import org.rcsb.mojave.tools.jsonschema.constants.TraversalLabel;
import org.rcsb.mojave.tools.jsonschema.traversal.model.TraversalContext;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.Visitable;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.VisitableNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Ensures that JSON Schema's node uses ONLY keywords described in a given specification. Each schema node is
 * independently evaluated against the vocabulary described in the specification and foreign keywords
 * are filtered out from the node content.
 *
 * Created on 8/29/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class KeywordsSyntaxChecker implements Visitor {

    private List<String> allowed = new ArrayList<>();

    /**
     * Expect a valid meta-schema in {@link JsonNode} representation.
     *
     * @param spec meta-schema that describes JSON Schema.
     * @throws IllegalArgumentException if specification is not a valid JSON schema.
     */
    public KeywordsSyntaxChecker(JsonNode spec) {

        if (!spec.has(MetaSchemaProperty.PROPERTIES))
            throw new IllegalArgumentException("Syntax issue with schema draft: should describe allowed keywords");

        spec.get(MetaSchemaProperty.PROPERTIES).fieldNames().forEachRemaining(allowed::add);
    }

    /**
     * Implements the visit operation that removes all properties which names are not in a org.rcsb.mojave.tools.core vocabulary
     * of a given specification.
     *
     * @param visitableNode the visitable node.
     */
    @Override
    public void visit(Visitable visitableNode) {

        if ( !(visitableNode instanceof VisitableNode) )
            throw new IllegalArgumentException("Node object MUST be an instance of VisitableNode.");

        JsonNode node = ((VisitableNode) visitableNode).getNode();
        TraversalContext ctx = ((VisitableNode) visitableNode).getTraversalContext();

        if (!ctx.isRef() && !ctx.getLabel().equals(TraversalLabel.PROPERTIES)) {

            List<String> nodeKeywords = new ArrayList<>();
            node.fieldNames().forEachRemaining(nodeKeywords::add);

            for (String keyword : nodeKeywords) {
                if (allowed.contains(keyword))
                    continue;
                ObjectNode newNode = (ObjectNode) node;
                newNode.remove(keyword);
            }
        }
    }
}
