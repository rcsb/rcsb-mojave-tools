package org.rcsb.mojave.tools.jsonschema.traversal.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;
import org.rcsb.mojave.tools.jsonschema.constants.TraversalLabel;
import org.rcsb.mojave.tools.jsonschema.traversal.model.TraversalContext;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.Visitable;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.VisitableNode;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 8/17/19.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class PropertiesFilterVisitor implements Visitor {

    private List<String> propertiesFilter;

    public PropertiesFilterVisitor(List<String> f) {
        propertiesFilter = f;
    }

    @Override
    public void visit(Visitable visitableNode) {

        if (!(visitableNode instanceof VisitableNode))
            throw new IllegalArgumentException("Node object MUST be an instance of VisitableNode.");

        JsonNode node = ((VisitableNode) visitableNode).getNode();
        TraversalContext ctx = ((VisitableNode) visitableNode).getTraversalContext();

        if (ctx.isRef())
            return;

        int i = ctx.isTitleIncluded() ? 1 : 0;
        if (ctx.getLabel().equals(TraversalLabel.PROPERTIES) && ctx.getLineage().size() == i)
            ((ObjectNode) node).retain(propertiesFilter);

        AtomicBoolean hasRequired = new AtomicBoolean(false);
        if (node.has(MetaSchemaProperty.REQUIRED)) {
            ArrayNode requiredFields = JsonNodeFactory.instance.arrayNode();
            node.get(MetaSchemaProperty.REQUIRED)
                    .forEach(el -> {
                        if (propertiesFilter.contains(el.asText())) {
                            requiredFields.add(el.textValue());
                            hasRequired.set(true);
                        }
                    });

            ((ObjectNode) node).remove(MetaSchemaProperty.REQUIRED);
            if (hasRequired.get())
                ((ObjectNode) node).set(MetaSchemaProperty.REQUIRED, requiredFields);
        }
    }
}
