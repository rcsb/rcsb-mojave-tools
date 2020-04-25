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

/**
 * Modifies field names and members of 'required' field by appending specified prefix or suffix.
 *
 * Created on 10/19/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class NamespaceVisitor implements Visitor {

    private Level level;

    private String prefix;
    private String suffix;
    private String delimiter;

    public NamespaceVisitor atLevel(Level l) {
        level = l;
        return this;
    }

    public NamespaceVisitor withPrefix(String string) {
        prefix = string;
        return this;
    }

    public NamespaceVisitor withSuffix(String string) {
        suffix = string;
        return this;
    }

    public NamespaceVisitor withDelimiter(String string) {
        delimiter = string;
        return this;
    }

    private String modifyName(String name) {
        if (prefix != null)
            name = prefix + delimiter + name;
        if (suffix != null)
            name = name + delimiter + suffix;
        return name;
    }

    private void modifyNode(JsonNode node) {

        if (node.has(MetaSchemaProperty.REQUIRED)) {
            ArrayNode requiredFields = JsonNodeFactory.instance.arrayNode();
            node.get(MetaSchemaProperty.REQUIRED)
                    .forEach(el -> requiredFields.add(modifyName(el.textValue())));
            ((ObjectNode) node).set(MetaSchemaProperty.REQUIRED, requiredFields);
        } else {
            node.deepCopy().fieldNames().forEachRemaining(name -> {
                ((ObjectNode) node).set(modifyName(name), node.get(name));
                ((ObjectNode) node).remove(name);
            });
        }
    }

    @Override
    public void visit(Visitable visitableNode) {

        if (!(visitableNode instanceof VisitableNode))
            throw new IllegalArgumentException("Node object MUST be an instance of VisitableNode.");

        JsonNode node = ((VisitableNode) visitableNode).getNode();
        TraversalContext ctx = ((VisitableNode) visitableNode).getTraversalContext();

        if (ctx.isRef())
            return;

        if ((ctx.getLabel().equals(TraversalLabel.OBJECT) && node.has(MetaSchemaProperty.REQUIRED))
                || ctx.getLabel().equals(TraversalLabel.PROPERTIES)) {

            switch (level) {
                case ROOT:
                    if (ctx.getLineage().size() == 0)
                        modifyNode(node);
                    break;
                case ALL:
                default:
                    modifyNode(node);
            }
        }
    }

    public enum Level {
        ROOT, ALL
    }
}
