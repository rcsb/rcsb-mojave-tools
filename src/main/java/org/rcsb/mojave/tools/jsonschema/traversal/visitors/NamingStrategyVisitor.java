package org.rcsb.mojave.tools.jsonschema.traversal.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.CaseFormat;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;
import org.rcsb.mojave.tools.jsonschema.constants.TraversalLabel;
import org.rcsb.mojave.tools.jsonschema.traversal.model.TraversalContext;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.Visitable;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.VisitableNode;

/**
 * Modifies field names and members of 'required' field based on the naming strategies
 * specified as {@link com.google.common.base.CaseFormat} objects.
 *
 * Created on 10/19/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class NamingStrategyVisitor implements Visitor {

    private CaseFormat fromNamingFormat;
    private CaseFormat toNamingFormat;

    public NamingStrategyVisitor(CaseFormat from, CaseFormat to) {
        fromNamingFormat = from;
        toNamingFormat = to;
    }

    @Override
    public void visit(Visitable visitableNode) {

        if ( !(visitableNode instanceof VisitableNode) )
            throw new IllegalArgumentException("Node object MUST be an instance of VisitableNode.");

        JsonNode node = ((VisitableNode) visitableNode).getNode();
        TraversalContext ctx = ((VisitableNode) visitableNode).getTraversalContext();

        if (ctx.isRef())
            return;

        if (ctx.getLabel().equals(TraversalLabel.PROPERTIES)) {

            node.deepCopy().fieldNames().forEachRemaining(name -> {
                String nodeName = fromNamingFormat.to(toNamingFormat, name);
                // do only if name has changed
                if (!nodeName.equals(name)) {
                    ((ObjectNode) node).set(nodeName, node.get(name));
                    ((ObjectNode) node).remove(name);
                }
            });
        }

        if (node.has(MetaSchemaProperty.REQUIRED)) {
            ArrayNode requiredFields = JsonNodeFactory.instance.arrayNode();
            node.get(MetaSchemaProperty.REQUIRED)
                    .forEach(el -> requiredFields.add(fromNamingFormat.to(toNamingFormat, el.textValue())));
            ((ObjectNode) node).set(MetaSchemaProperty.REQUIRED, requiredFields);
        }
    }
}
