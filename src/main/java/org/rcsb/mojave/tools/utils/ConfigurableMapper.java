package org.rcsb.mojave.tools.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = JsonMapper.builder()
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .enable(JsonParser.Feature.ALLOW_COMMENTS)
                    .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
                    .serializationInclusion(JsonInclude.Include.NON_EMPTY)
                    .build();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            mapper.configOverride(List.class).setInclude(
                    JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, null));
            mapper.configOverride(Set.class).setInclude(
                    JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, null));
            mapper.configOverride(Map.class).setInclude(
                    JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, null));
        }
        return mapper;
    }
}
