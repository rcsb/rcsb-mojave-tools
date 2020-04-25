package org.rcsb.mojave.tools.jsonschema.traversal.model;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import org.rcsb.mojave.tools.jsonschema.constants.JsonPointerConstants;
import org.rcsb.mojave.tools.jsonschema.constants.TraversalLabel;
import org.rcsb.mojave.tools.jsonschema.traversal.builder.TreeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 8/16/19.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class TraversalContext {

    private TreeBuilder builder;

    private JsonNode schema;                // the schema passed to traversal algorithm
    private JsonPointer jsonPointer;        // from the root of the schema to the current schema node

    private boolean isRef;                  // is true when the current schema node is a resolved $ref pointer

    private TraversalLabel label;

    private boolean titleIncluded;
    private List<String> lineage;           // full field lineage for the current schema node

    public TraversalContext() {
        lineage = new ArrayList<>();
    }

    public boolean isProperty() {
        return jsonPointer.head().getMatchingProperty()
                .equals(JsonPointerConstants.PROPERTIES.getMatchingProperty());
    }

    public JsonNode getSchema() {
        return schema;
    }

    public void setSchema(JsonNode schema) {
        this.schema = schema;
    }

    public JsonPointer getJsonPointer() {
        return jsonPointer;
    }

    public void setJsonPointer(JsonPointer jsonPointer) {
        this.jsonPointer = jsonPointer;
    }

    /**
     * Get the path from the root of the schema to the parent schema node.
     *
     * @return pointer to the parent node.
     */
    public JsonPointer getParentJsonPointer() {
        return jsonPointer.head();
    }

    /**
     * The parent field name in the hierarchy of nested properties.
     *
     * @return property name
     */
    public String getParentFieldName() {
        int index = 1;
        if (isTitleIncluded())
            index = 2;
        return lineage.size() <= index ? null : lineage.get(lineage.size()-(index+1));
    }

    /**
     * The field name that corresponds to the current schema node.
     *
     * @return property name
     */
    public String getCurrentFieldName() {
        return lineage.isEmpty() ? null : lineage.get(lineage.size()-1);
    }

    public void setRef(boolean f) {
        isRef = f;
    }

    public boolean isRef() {
        return isRef;
    }

    public boolean isTitleIncluded() {
        return titleIncluded;
    }

    public void setTitleIncluded(boolean titleIncluded) {
        this.titleIncluded = titleIncluded;
    }

    public List<String> getLineage() {
        return lineage;
    }

    public String getFullyQualifiedName() {
        int index = 0;
        if (isTitleIncluded())
            index = 1;
        return String.join(".", getLineage().subList(index, getLineage().size()));
    }

    public void setLineage(List<String> lineage) {
        this.lineage = lineage;
    }

    public TreeBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(TreeBuilder builder) {
        this.builder = builder;
    }

    public TraversalLabel getLabel() {
        return label;
    }

    public void setLabel(TraversalLabel label) {
        this.label = label;
    }

    public TraversalContext deepCopy() {

        TraversalContext ctxClone = new TraversalContext();

        // we want to pass around pointer to the schema object
        ctxClone.setSchema(schema);
        // we want to pass around the builder object
        ctxClone.setBuilder(this.builder);

        ctxClone.setJsonPointer(this.getJsonPointer()!=null?
                JsonPointer.compile(this.getJsonPointer().toString()):null);

        ctxClone.setRef(this.isRef);

        ctxClone.setLabel(this.getLabel());
        ctxClone.setLineage(new ArrayList<>(this.getLineage()));

        return ctxClone;
    }
}
