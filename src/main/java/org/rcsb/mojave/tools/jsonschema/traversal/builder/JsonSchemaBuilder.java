package org.rcsb.mojave.tools.jsonschema.traversal.builder;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.Pair;
import org.rcsb.mojave.tools.jsonschema.SchemaStitching;
import org.rcsb.mojave.tools.jsonschema.constants.JsonPointerConstants;
import org.rcsb.mojave.tools.utils.ConfigurableMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created on 1/2/20.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class JsonSchemaBuilder implements TreeBuilder {

    private List<Pair<JsonPointer, JsonNode>> nodes = new ArrayList<>();

    @Override
    public void add(JsonPointer path, JsonNode node) {

        if (!nodes.contains(Pair.of(path, node)))
            nodes.add(Pair.of(path, node));
    }

    private int countLevels(String line) {
        return line.length() - line.replace("/", "").length();
    }

    private void addMissingNode(ObjectNode tree, JsonPointer missingPath) {

        String[] tokens = missingPath.toString().split(Pattern.quote("/"));

        for (String t : tokens) {
            if (t.isEmpty())
                continue;
            JsonPointer path = JsonPointer.compile(String.format("/%s", t));
            if (tree.at(path).isMissingNode()) {
                ObjectNode missingNode = ConfigurableMapper.getMapper().createObjectNode();
                tree.set(t, missingNode);
                tree = missingNode;
            } else
                tree = (ObjectNode) tree.at(path);
        }
    }

    @Override
    public JsonNode buildTree() throws IOException {

        // sort by the number of levels in the pointer (the shorter the pointer - the closer this node
        // is located to the root)
        nodes.sort(Comparator.comparingInt(o -> countLevels(o.getLeft().toString())));

        ObjectNode tree = ConfigurableMapper.getMapper().createObjectNode();

        for (Pair<JsonPointer, JsonNode> pair : nodes) {

            if (pair.getLeft().equals(JsonPointerConstants.ROOT))
                continue;

            JsonPointer p = pair.getLeft();
            JsonNode n = pair.getRight();

            if (tree.at(p).isMissingNode())
                addMissingNode(tree, p);

            JsonNode locatedNode = tree.at(p);
            try {
                SchemaStitching.mergeSchemas(locatedNode, n);
            } catch (Exception e) {
                String contextMessage = "Cannot build JsonNode tree.";
                throw new IOException(contextMessage, e);
            }
        }
        return tree;
    }
}
