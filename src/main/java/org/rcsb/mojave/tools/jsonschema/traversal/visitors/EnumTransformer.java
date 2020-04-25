package org.rcsb.mojave.tools.jsonschema.traversal.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaModifier;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;
import org.rcsb.mojave.tools.jsonschema.constants.TraversalLabel;
import org.rcsb.mojave.tools.jsonschema.traversal.JsonSchemaWalker;
import org.rcsb.mojave.tools.jsonschema.traversal.model.TraversalContext;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.Visitable;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.VisitableNode;
import org.rcsb.mojave.tools.jsonschema.utils.JsonSchemaNodeUtils;
import org.rcsb.mojave.tools.jsonschema2pojo.annotations.CustomAnnotator;

/**
 * Transforms "enum" keyword to "allowableValues" preserving control vocabulary.
 *
 * Implements the visit operation defined in the {@link Visitor} interface that can be applied in
 * {@link JsonSchemaWalker#walk()}.
 *
 * Created on 01/25/2019.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class EnumTransformer implements Visitor {

    /**
     * This operation renames enum description, if present in a given node of JSON schema, to 'allowableValues'
     * so it can be picked up by {@link CustomAnnotator} but not by jsonschema2pojo tool during POJO generation.
     *
     * @param visitableNode the visitable node is a wrapper around concrete JSON schema node implementation.
     */
    @Override
    public void visit(Visitable visitableNode) {

        if ( !(visitableNode instanceof VisitableNode) )
            throw new IllegalArgumentException("Node object MUST be an instance of VisitableNode.");

        JsonNode node = ((VisitableNode) visitableNode).getNode();
        TraversalContext ctx = ((VisitableNode) visitableNode).getTraversalContext();

        // There is a discrepancy between the value returned by GraphQL API and the value
        // stored in the database when a given field is described as Enum. This happens as
        // for Enums in GraphQL the public names are taken from enum constant names (derived
        // from Java enum constant name in our case). All characters that cannot legally form
        // part of the Java enum constant name and/or GraphQL name are transformed. To provide
        // the actual enum value (that is held in a 'value' property inside the enum constants)
        // we remove 'enum' from the field definitions in JSON schemas used to generate POJO's.
        // Such fields will be described with proper scalar types. YV 01/28/2019.
        if (!ctx.isRef() && ctx.getLabel().equals(TraversalLabel.ATTRIBUTE)
                && JsonSchemaNodeUtils.isEnum(node)) {
            ((ObjectNode) node).set(MetaSchemaModifier.ALLOWABLE_VALUES, node.get(MetaSchemaProperty.ENUM));
            ((ObjectNode) node).remove(MetaSchemaProperty.ENUM);
        }
    }
}
