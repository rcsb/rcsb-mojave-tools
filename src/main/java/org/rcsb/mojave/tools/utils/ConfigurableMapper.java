package org.rcsb.mojave.tools.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

/**
 * Wrapper around Jackson ObjectMapper which is a JSON serialization/deserialization library for Java.
 * This wrapper allows a custom configuration to alter the object mapper behaviour.
 *
 * Created on 4/4/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class ConfigurableMapper {

    private ConfigurableMapper() {}

    private static ObjectMapper mapper;

    private static void configure() {

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    public static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            configure();
        }
        return mapper;
    }
}
