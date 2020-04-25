package org.rcsb.mojave.tools.jsonschema.traversal.algorithm;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.rcsb.mojave.tools.jsonschema.traversal.TestVisitableSchemaTree;
import org.rcsb.mojave.tools.jsonschema.traversal.model.TraversalContext;
import org.rcsb.mojave.tools.utils.ConfigurableMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertEquals;

/**
 * Created on 1/14/20.
 *
 * @author Yana Valasatava
 * @since 4.0.0
 */
public class TestSchemaTreeTraversal {

    /**
     * Tests that visiting nodes of the following tree:
     *          1
     *         / \
     *        2   3
     *            \
     *            4
     *           / \
     *          5   6
     * will happen in the following order: 1 2 3 4 5 6 (pre-order traversal)
     *
     * @throws IOException if cannot read the schema file.
     */
    @Test
    public void testPreOrderTraversal() throws IOException {

        InputStream is = TestVisitableSchemaTree.class
                .getResourceAsStream("/schema/traversal/json-schema-with-node-id.json");
        JsonNode schemaTree = ConfigurableMapper.getMapper().readTree(is);

        JsonSchemaTraversal iterator = new JsonSchemaTraversal();
        iterator.setTraversalStrategy(JsonSchemaTraversal.Strategy.PRE_ORDER);
        iterator.traverse(schemaTree);

        List<Integer> expectedOrder = asList(1, 2, 3, 4, 5, 6);
        List<Integer> actualOrder = new ArrayList<>();
        iterator.forEachRemaining(i -> {
            if (i.getLeft().has("id")) actualOrder.add(i.getLeft().get("id").asInt());
        });
        assertEquals(expectedOrder, actualOrder);
    }

    /**
     * Tests that visiting nodes of the following tree:
     *          1
     *         / \
     *        2   3
     *            \
     *            4
     *           / \
     *          5   6
     * will happen in the following order: 6 5 4 3 2 1 (post-order right-to-left traversal)
     *
     * @throws IOException  if cannot read the schema file.
     */
    @Test
    public void testPostOrderTraversal() throws IOException {

        InputStream is = TestVisitableSchemaTree.class
                .getResourceAsStream("/schema/traversal/json-schema-with-node-id.json");
        JsonNode schemaTree = ConfigurableMapper.getMapper().readTree(is);

        JsonSchemaTraversal iterator = new JsonSchemaTraversal();
        iterator.setTraversalStrategy(JsonSchemaTraversal.Strategy.POST_ORDER);
        iterator.traverse(schemaTree);

        List<Integer> expectedOrder = asList(6, 5, 4, 3, 2, 1);
        List<Integer> actualOrder = new ArrayList<>();
        iterator.forEachRemaining(i -> {
            if (i.getLeft().has("id")) actualOrder.add(i.getLeft().get("id").asInt());
        });
        assertEquals(expectedOrder, actualOrder);
    }

    @Test
    public void testTraversalContextBuilding() throws IOException {

        InputStream is = TestVisitableSchemaTree.class
                .getResourceAsStream("/schema/traversal/json-schema-context-test.json");
        JsonNode schemaTree = ConfigurableMapper.getMapper().readTree(is);
        
        JsonSchemaTraversal iterator = new JsonSchemaTraversal();
        iterator.setTraversalStrategy(JsonSchemaTraversal.Strategy.PRE_ORDER);
        iterator.traverse(schemaTree);
        iterator.forEachRemaining(i -> {
            TraversalContext ctx = i.getRight();
        });
    }
}
