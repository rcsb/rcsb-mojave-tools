package org.rcsb.mojave.tools.jsonschema.traversal.visitors;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.rcsb.mojave.tools.jsonschema.traversal.model.TraversalContext;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.Visitable;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.VisitableNode;
import org.rcsb.mojave.tools.jsonschema.utils.JsonSchemaNodeUtils;
import org.rcsb.mojave.tools.utils.ConfigurableMapper;

/**
 * Created on 1/2/20.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class SchemaBuilderVisitor implements Visitor {

    private JsonNode removeRef(JsonNode node) {

        if (!node.isObject())
            return node;

        ObjectNode nodeCopy = ConfigurableMapper.getMapper().createObjectNode();
        if (!JsonSchemaNodeUtils.isRef(node))
            node.fields().forEachRemaining(f -> nodeCopy.set(f.getKey(), removeRef(f.getValue())));

        return nodeCopy;
    }

    @Override
    public void visit(Visitable visitableNode) {

        JsonNode node = ((VisitableNode) visitableNode).getNode();
        TraversalContext ctx = ((VisitableNode) visitableNode).getTraversalContext();

        JsonNode nodeCopy = removeRef(node);
        JsonPointer path = ctx.getJsonPointer();
        ctx.getBuilder().add(path, nodeCopy);
    }
}
