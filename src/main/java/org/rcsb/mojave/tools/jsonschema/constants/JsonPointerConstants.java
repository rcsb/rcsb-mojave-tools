package org.rcsb.mojave.tools.jsonschema.constants;

import com.fasterxml.jackson.core.JsonPointer;

/**
 * Created on 8/16/19.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class JsonPointerConstants {

    private JsonPointerConstants() {}

    public static final String ROOT_SCHEMA_NAME = "#";

    public static final JsonPointer ROOT = JsonPointer.compile("");
    public static final JsonPointer ARRAY = JsonPointer.compile(JsonPointer.SEPARATOR+MetaSchemaProperty.ITEMS);
    public static final JsonPointer PROPERTIES = JsonPointer.compile(JsonPointer.SEPARATOR+MetaSchemaProperty.PROPERTIES);

}
