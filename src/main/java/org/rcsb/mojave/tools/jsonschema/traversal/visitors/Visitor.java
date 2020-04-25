package org.rcsb.mojave.tools.jsonschema.traversal.visitors;

import org.rcsb.mojave.tools.jsonschema.traversal.visitables.Visitable;

/**
 * Allows for an operation to be applied to a set of objects at runtime,
 * decoupling the operations from the object structure.
 *
 * The implementation of the Visitor deals with the specifics of what to do when it pays a visit.
 *
 * Created on 8/27/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public interface Visitor {

    /**
     * Defines a visit operation for each type of {@link Visitable} in the object structure.
     *
     * @param visitableNode the visitable node
     */
    void visit(Visitable visitableNode);
}