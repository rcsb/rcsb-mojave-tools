package org.rcsb.mojave.tools.jsonschema.traversal.constants;

/**
 * Created on 1/3/20.
 *
 * @author Yana Valasatava
 */
public enum TraversalOrder {

    //    Pre Order Traversal: Root, Left, Right.
    //    In our example: A B D H I E J C F G K
    //    In Order Traversal: Left, Root, Right.
    //    In our example: H D I B J E A F C K G
    //    Post Order Traversal: Left, Right, Root.
    //    In our example: H I D J E B F K G C A
    //    Level Order Traversal, also known as Breadth-first search.
    //    In our example: A B C D E F G H I J K

    PRE_ORDER,
    IN_ORDER,
    POST_ORDER,
    LEVEL_ORDER
}
