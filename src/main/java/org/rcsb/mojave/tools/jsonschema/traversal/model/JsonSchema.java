package org.rcsb.mojave.tools.jsonschema.traversal.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created on 1/3/20.
 *
 * @author Yana Valasatava
 */
public class JsonSchema {

    private String name;
    private String type;
    private JsonNode data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }
}
