package org.rcsb.mojave.tools.jsonschema.constants;

/**
 * JSON Schema vocabulary.
 *
 * Created on 2/26/19.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class MetaSchemaProperty {

    private MetaSchemaProperty() {}

    // the "$id" keyword defines a URI for the schema, and the base URI
    // that other URI references within the schema are resolved against.
    public static final String SCHEMA_ID = "$id";

    // the "$schema" keyword is both used as a JSON Schema version
    // identifier.
    public static final String SCHEMA = "$schema";

    public static final String COMMENT = "$comment";

    public static final String SCHEMA_REF = "$ref";

    public static final String MIN_LENGTH = "minLength";
    public static final String PATTERN = "pattern";
    public static final String DESCRIPTION = "description";
    public static final String TITLE = "title";
    public static final String TYPE = "type";
    public static final String REQUIRED = "required";
    public static final String EXCLUSIVE_MAXIMUM = "exclusiveMaximum";
    public static final String PATTERN_PROPERTIES = "patternProperties";
    public static final String ALL_OF = "allOf";
    public static final String DEFAULT = "default";
    public static final String ONE_OF = "oneOf";
    public static final String NOT = "not";
    public static final String ADDITIONAL_ITEMS = "additionalItems";
    public static final String ID = "id";
    public static final String MAX_PROPERTIES = "maxProperties";
    public static final String EXCLUSIVE_MINIMUM = "exclusiveMinimum";
    public static final String DEFINITIONS = "definitions";
    public static final String MULTIPLE_OF = "multipleOf";
    public static final String MAX_ITEMS = "maxItems";
    public static final String FORMAT = "format";
    public static final String ANY_OF = "anyOf";
    public static final String ENUM = "enum";
    public static final String MIN_PROPERTIES = "minProperties";
    public static final String DEPENDENCIES = "dependencies";
    public static final String MIN_ITEMS = "minItems";
    public static final String UNIQUE_ITEMS = "uniqueItems";
    public static final String MAXIMUM = "maximum";
    public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
    public static final String MINIMUM = "minimum";
    public static final String ITEMS = "items";
    public static final String MAX_LENGTH = "maxLength";
    public static final String PROPERTIES = "properties";
    public static final String EXAMPLES = "examples";
}
