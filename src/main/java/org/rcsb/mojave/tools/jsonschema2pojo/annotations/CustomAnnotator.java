package org.rcsb.mojave.tools.jsonschema2pojo.annotations;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.*;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jsonschema2pojo.AbstractAnnotator;
import org.jsonschema2pojo.GenerationConfig;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaModifier;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;

import java.util.*;

/**
 * Annotates generated Java types using the custom annotations. When used in Maven plugin a fully qualified class name,
 * referring to this instance, should be passed with <customAnnotator> attribute.
 * See <a href="http://joelittlejohn.github.io/jsonschema2pojo/site/0.5.1/generate-mojo.html#customAnnotator">org.rcsb.mojave.tools.jsonschema2pojo documentation</a>.
 * <p>
 * Custom annotations are:
 * <p>
 * 1. Jackson {@link JsonPropertyDescription} annotation defines a human-readable description
 *      for a logical property. When examples or enums metadata is available in JSON schema,
 *      the metadata is added to description as text.
 * <p>
 * 2. OpenAPI {@link Schema} annotation:
 *      - description as {@link Schema#description()}
 *      - examples as {@link Schema#example()}
 *      - enumerations as {@link Schema#allowableValues()}
 *
 *      See <a href="https://github.com/OAI/OpenAPI-Specification">OpenAPI specification</a>.
 * <p>
 * 3. Javadoc is enriched by description, examples and enums metadata.
 * <p>
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class CustomAnnotator extends AbstractAnnotator {

    private String className;

    private Map<String, String> examples;
    private Map<String, String> description;
    private Map<String, List<String>> enumValues;

    private static final String VALUE_PARAM = "value";
    private static final String DESCRIPTION_PARAM = "description";
    private static final String EXAMPLE_PARAM = "example";
    private static final String NAME_PARAM = "name";

    public CustomAnnotator(GenerationConfig generationConfig) {
        super(generationConfig);
    }

    private String updateDescriptionTextWithExamples(String propertyName) {
        String descriptionText = examples.get(propertyName);
        if (description.containsKey(propertyName))
            descriptionText = description.get(propertyName) + "\n\nExamples:\n" + descriptionText;
        return descriptionText;
    }

    private boolean updateJacksonAnnotationWithExamples(Collection<JAnnotationUse> annotations, String propertyName) {

        Optional<JAnnotationUse> annotation = annotations.stream()
                .filter(a -> a.getAnnotationClass().name().equals(JsonPropertyDescription.class.getSimpleName()))
                .findFirst();
        if (annotation.isPresent()) {
            annotation.get().param(VALUE_PARAM, updateDescriptionTextWithExamples(propertyName));
            return true;
        }
        return false;
    }

    private boolean updateSwaggerAnnotationWithExamples(Collection<JAnnotationUse> annotations, String propertyName) {
        return doUpdateAnnotation(annotations, propertyName, EXAMPLE_PARAM, examples);
    }

    private boolean doUpdateAnnotation(Collection<JAnnotationUse> annotations, String propertyName, String paramName, Map<String, String> params) {
        Optional<JAnnotationUse> annotation = annotations.stream()
                .filter(a -> a.getAnnotationClass().name().equals(Schema.class.getSimpleName()))
                .findFirst();
        if (annotation.isPresent()) {
            annotation.get().param(paramName, params.get(propertyName));
            return true;
        }
        return false;
    }

    private boolean updateJacksonAnnotationWithDescription(Collection<JAnnotationUse> annotations, String propertyName) {

        Optional<JAnnotationUse> annotation = annotations.stream()
                .filter(a -> a.getAnnotationClass().name().equals(JsonPropertyDescription.class.getSimpleName()))
                .findFirst();
        if (annotation.isPresent()) {
            annotation.get().param(VALUE_PARAM, description.get(propertyName));
            return true;
        }
        return false;
    }

    private String updateDescriptionTextWithAllowableValues(String propertyName) {

        List<String> allowableValues = enumValues.get(propertyName);

        StringJoiner joiner = new StringJoiner(", ");
        allowableValues.iterator().forEachRemaining(joiner::add);
        String descriptionText = joiner + "\n";

        if (description.containsKey(propertyName))
            descriptionText = description.get(propertyName) + "\n\nAllowable values:\n" + descriptionText;
        return descriptionText;
    }

    private boolean updateJacksonAnnotationWithAllowableValues(Collection<JAnnotationUse> annotations, String propertyName) {

        Optional<JAnnotationUse> annotation = annotations.stream()
                .filter(a -> a.getAnnotationClass().name().equals(JsonPropertyDescription.class.getSimpleName()))
                .findFirst();
        if (annotation.isPresent()) {
            annotation.get().param(VALUE_PARAM, updateDescriptionTextWithAllowableValues(propertyName));
            return true;
        }
        return false;
    }

    private boolean updateSwaggerAnnotationWithDescription(Collection<JAnnotationUse> annotations, String propertyName) {
        return doUpdateAnnotation(annotations, propertyName, DESCRIPTION_PARAM, description);
    }

    private void updateMethodWithExamples(JMethod method, String propertyName) {

        if (!examples.containsKey(propertyName))
            return;

        boolean jacksonUpdated = updateJacksonAnnotationWithExamples(method.annotations(), propertyName);
        if (!jacksonUpdated)
            method.annotate(JsonPropertyDescription.class)
                    .param(VALUE_PARAM, updateDescriptionTextWithExamples(propertyName));
    }

    private void updateMethodWithDescription(JMethod method, String propertyName) {

        if (!description.containsKey(propertyName))
            return;

        boolean jacksonUpdated = updateJacksonAnnotationWithDescription(method.annotations(), propertyName);
        if (!jacksonUpdated)
            method.annotate(JsonPropertyDescription.class).param(VALUE_PARAM, description.get(propertyName));
    }

    /**
     * Add annotations for allowable values to {@link Schema} annotation object attached to the method.
     *
     * @param method
     *            the method that will be used to get or set the value of the given property.
     * @param propertyName
     *            the name of the  property that this method gets.
     */
    private void updateMethodWithAllowableValues(JMethod method, String propertyName) {

        if (!enumValues.containsKey(propertyName))
            return;

        boolean jacksonUpdated = updateJacksonAnnotationWithAllowableValues(method.annotations(), propertyName);
        if (!jacksonUpdated) {

            StringJoiner joiner = new StringJoiner(", ");
            enumValues.get(propertyName).iterator().forEachRemaining(joiner::add);
            String descriptionText = "\n\nAllowable values:\n" + joiner + "\n";

            method.annotate(JsonPropertyDescription.class).param(VALUE_PARAM, descriptionText);
        }
    }

    /**
     * Add annotations for allowable values to {@link Schema} annotation object attached to the property field.
     *
     * @param field        the field that contains data that will be serialized.
     * @param propertyNode the schema node defining this property.
     */
    private void annotateFieldWithAllowableValues(JFieldVar field, String propertyName, JsonNode propertyNode) {

        // annotations for allowable values that are members of an array are attached to the definition
        // of an array item.
        if (propertyNode.has(MetaSchemaProperty.ITEMS))
            propertyNode = propertyNode.get(MetaSchemaProperty.ITEMS);

        if ( !propertyNode.has(MetaSchemaModifier.ALLOWABLE_VALUES) )
            return;

        JsonNode allowableValuesList = propertyNode.get(MetaSchemaModifier.ALLOWABLE_VALUES);

        List<String> list = new ArrayList<>();

        JAnnotationUse swaggerSchemaAnnotation = field.annotations().stream()
                .filter(a -> a.getAnnotationClass().name().equals(Schema.class.getSimpleName()))
                .findFirst()
                .orElseGet(() -> field.annotate(Schema.class));

        JAnnotationArrayMember allowableValues = swaggerSchemaAnnotation.paramArray(MetaSchemaModifier.ALLOWABLE_VALUES);
        allowableValuesList.iterator().forEachRemaining(e -> {
            allowableValues.param(e.asText());
            list.add(e.asText());
        });

        enumValues.put(propertyName, list);

        StringJoiner joiner = new StringJoiner(", ");
        list.iterator().forEachRemaining(joiner::add);
        String allowableValuesText = "Allowable values: " + joiner + ".";

        // add allowable values to javadoc as text
        field.javadoc().append("\n" + allowableValuesText);

        boolean jacksonUpdated = updateJacksonAnnotationWithAllowableValues(field.annotations(), propertyName);
        if (!jacksonUpdated)
            field.annotate(JsonPropertyDescription.class).param(VALUE_PARAM, allowableValuesText);
    }

    private void annotateFieldWithDescription(JFieldVar field, String propertyName, JsonNode propertyNode) {

        if (propertyNode.has(MetaSchemaProperty.ITEMS))
            propertyNode = propertyNode.get(MetaSchemaProperty.ITEMS);

        if (!propertyNode.has(MetaSchemaProperty.DESCRIPTION))
            return;

        description.put(propertyName, propertyNode.get(MetaSchemaProperty.DESCRIPTION).asText());

        boolean jacksonUpdated = updateJacksonAnnotationWithDescription(field.annotations(), propertyName);
        if (!jacksonUpdated)
            field.annotate(JsonPropertyDescription.class)
                    .param(VALUE_PARAM, description.get(propertyName));

        boolean swaggerUpdated = updateSwaggerAnnotationWithDescription(field.annotations(), propertyName);
        if (!swaggerUpdated)
            field.annotate(Schema.class).param(DESCRIPTION_PARAM, description.get(propertyName));
    }

    private void annotateFieldWithExamples(JFieldVar field, String propertyName, JsonNode propertyNode) {

        if (!propertyNode.has(MetaSchemaProperty.EXAMPLES))
            return;

        StringJoiner joiner = new StringJoiner(", ");
        propertyNode.get(MetaSchemaProperty.EXAMPLES).iterator()
                .forEachRemaining(e -> joiner.add(e.textValue()));
        String examplesText = joiner + "\n";

        // add examples to javadoc as text
        field.javadoc().append("\n\nExamples:\n" + examplesText);

        examples.put(propertyName, examplesText);

        boolean jacksonUpdated = updateJacksonAnnotationWithExamples(field.annotations(), propertyName);
        if (!jacksonUpdated)
            field.annotate(JsonPropertyDescription.class)
                    .param(VALUE_PARAM, updateDescriptionTextWithExamples(propertyName));

        boolean swaggerUpdated = updateSwaggerAnnotationWithExamples(field.annotations(), propertyName);
        if (!swaggerUpdated)
            field.annotate(Schema.class).param(EXAMPLE_PARAM, examples.get(propertyName));
    }

    private void annotateFieldWithPropertyName(JFieldVar field, String propertyName) {

        JAnnotationUse swaggerSchemaAnnotation = field.annotate(Schema.class);
        swaggerSchemaAnnotation.param(NAME_PARAM, propertyName);
    }

    /**
     * Adds necessary annotations to a Java field.
     *
     * @param field        the field that contains data that will be serialized.
     * @param clazz        the owner of the field (class to which the field belongs).
     * @param propertyName the name of the JSON property that this field represents.
     * @param propertyNode the schema node defining this property.
     */
    @Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {

        // clear the description map for every new class
        if (className == null || !className.equals(clazz.name())) {
            className = clazz.name();
            examples = new HashMap<>();
            description = new HashMap<>();
            enumValues = new HashMap<>();
        }

        annotateFieldWithPropertyName(field, propertyName);
        annotateFieldWithDescription(field, propertyName, propertyNode);
        annotateFieldWithExamples(field, propertyName, propertyNode);
        annotateFieldWithAllowableValues(field, propertyName, propertyNode);
    }

    @Override
    public void propertyGetter(JMethod getter, JDefinedClass clazz, String propertyName) {
        updateMethodWithDescription(getter, propertyName);
        updateMethodWithExamples(getter, propertyName);
        updateMethodWithAllowableValues(getter, propertyName);
    }
}
