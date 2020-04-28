package org.rcsb.mojave.tools.jsonschema.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The JSON Schema definition depends on the dynamic characteristics of json
 * to create recursive definitions using union types. It's not at all amenable
 * to data-binding to a static type system like Java's. Therefore, auto generation
 * of Java class for general JSON Schema meta schema is not supported by
 * @see <a href="https://github.com/joelittlejohn/jsonschema2pojo">jsonschema2pojo</a>
 * tool.
 *
 * http://json-schema.org/draft-07/schema#
 *
 * Created on 3/1/19.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class JsonSchemaInstance {

    private String id;
    private String schemaVersion;
    private String title;
    private String description;
    private Double multipleOf;
    private Double maximum;
    private Boolean exclusiveMaximum;
    private Double minimum;
    private Boolean exclusiveMinimum;
    private Integer maxLength;
    private Integer minLength;
    private Pattern pattern;
    private Boolean additionalItems;
    private JsonSchemaInstance items;
    private Integer maxItems;
    private Integer minItems;
    private Boolean uniqueItems;
    private Integer maxProperties;
    private Integer minProperties;
    private Set<String> required;
    private Boolean additionalProperties;
    private Map<String, JsonSchemaInstance> properties;
    private Set<Object> enumProperty;
    private String type;
    private String format;
    private List<JsonSchemaInstance> allOf;
    private List<JsonSchemaInstance> anyOf;
    private List<JsonSchemaInstance> oneOf;
    private List<Object> examples;

    /**
     * Core schemaVersion meta-schemaVersion
     *
     */
    private JsonSchemaInstance not;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String version) {
        this.schemaVersion = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMultipleOf() {
        return multipleOf;
    }

    public void setMultipleOf(Double multipleOf) {
        this.multipleOf = multipleOf;
    }

    public Double getMaximum() {
        return maximum;
    }

    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    public Boolean getExclusiveMaximum() {
        return exclusiveMaximum;
    }

    public void setExclusiveMaximum(Boolean exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
    }

    public Double getMinimum() {
        return minimum;
    }

    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }

    public Boolean getExclusiveMinimum() {
        return exclusiveMinimum;
    }

    public void setExclusiveMinimum(Boolean exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Boolean getAdditionalItems() {
        return additionalItems;
    }

    public void setAdditionalItems(Boolean additionalItems) {
        this.additionalItems = additionalItems;
    }

    public JsonSchemaInstance getItems() {
        return items;
    }

    public void setItems(JsonSchemaInstance items) {
        this.items = items;
    }

    public Integer getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }

    public Integer getMinItems() {
        return minItems;
    }

    public void setMinItems(Integer minItems) {
        this.minItems = minItems;
    }

    public Boolean getUniqueItems() {
        return uniqueItems;
    }

    public void setUniqueItems(Boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }

    public Integer getMaxProperties() {
        return maxProperties;
    }

    public void setMaxProperties(Integer maxProperties) {
        this.maxProperties = maxProperties;
    }

    public Integer getMinProperties() {
        return minProperties;
    }

    public void setMinProperties(Integer minProperties) {
        this.minProperties = minProperties;
    }

    public Set<String> getRequired() {
        return required;
    }

    public void setRequired(Set<String> required) {
        this.required = required;
    }

    public Boolean getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Boolean additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public Map<String, JsonSchemaInstance> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, JsonSchemaInstance> properties) {
        this.properties = properties;
    }

    public Set<Object> getEnum() {
        return enumProperty;
    }

    public void setEnum(Set<Object> e) {
        this.enumProperty = e;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<JsonSchemaInstance> getAllOf() {
        return allOf;
    }

    public void setAllOf(List<JsonSchemaInstance> allOf) {
        this.allOf = allOf;
    }

    public List<JsonSchemaInstance> getAnyOf() {
        return anyOf;
    }

    public void setAnyOf(List<JsonSchemaInstance> anyOf) {
        this.anyOf = anyOf;
    }

    public List<JsonSchemaInstance> getOneOf() {
        return oneOf;
    }

    public void setOneOf(List<JsonSchemaInstance> oneOf) {
        this.oneOf = oneOf;
    }

    public List<Object> getExamples() {
        return examples;
    }

    public void setExamples(List<Object> examples) {
        this.examples = examples;
    }

    public JsonSchemaInstance getNot() {
        return not;
    }

    public void setNot(JsonSchemaInstance not) {
        this.not = not;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(minLength).append(pattern).append(description).append(enumProperty).append(title).append(type).append(required).append(exclusiveMaximum).append(allOf).append(oneOf).append(not).append(additionalItems).append(id).append(maxProperties).append(exclusiveMinimum).append(multipleOf).append(maxItems).append(format).append(anyOf).append(minProperties).append(minItems).append(uniqueItems).append(maximum).append(additionalProperties).append(minimum).append(items).append(maxLength).append(properties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof JsonSchemaInstance)) {
            return false;
        }
        JsonSchemaInstance rhs = ((JsonSchemaInstance) other);
        return new EqualsBuilder().append(enumProperty, rhs.enumProperty).append(type, rhs.type).append(required, rhs.required).append(allOf, rhs.allOf).append(oneOf, rhs.oneOf).append(not, rhs.not).append(additionalItems, rhs.additionalItems).append(maxItems, rhs.maxItems).append(format, rhs.format).append(anyOf, rhs.anyOf).append(minProperties, rhs.minProperties).append(minItems, rhs.minItems).append(uniqueItems, rhs.uniqueItems).append(maximum, rhs.maximum).append(additionalProperties, rhs.additionalProperties).append(items, rhs.items).append(properties, rhs.properties).isEquals();
    }
}
