package org.rcsb.mojave.tools.jsonschema.traversal.visitables;

import org.rcsb.mojave.tools.jsonschema.traversal.visitors.Visitor;

/**
 * This interface simply defines an accept method to allow the visitor to run some action
 * over an instance of {@link Visitor}
 *
 * Created on 8/27/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public interface Visitable {
    /**
     * Allows the visitor access to an instance of {@link Visitable}.
     *
     * @param visitor an instance of {@link Visitor}
     */
    void accept(Visitor visitor);
}
