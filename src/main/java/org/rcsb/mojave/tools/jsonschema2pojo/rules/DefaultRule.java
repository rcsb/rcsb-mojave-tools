package org.rcsb.mojave.tools.jsonschema2pojo.rules;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.sun.codemodel.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.EnumRule;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.rules.RuleFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.ParseException;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Applies the "default" schema rule.
 *
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.20">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.20</a>
 */
public class DefaultRule implements Rule<JFieldVar, JFieldVar> {

    private final RuleFactory ruleFactory;

    public DefaultRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * Default values are implemented by assigning an expression to the given
     * field (so when instances of the generated POJO are created, its fields
     * will then contain their default values).
     * <p>
     * Collections (Lists and Sets) are initialized to an empty collection, even
     * when no default value is present in the schema (node is null).
     *
     * @param nodeName
     *            the name of the property which has (or may have) a default
     * @param node
     *            the default node (may be null if no default node was present
     *            for this property)
     * @param field
     *            the Java field that has added to a generated type to represent
     *            this property
     * @return field, which will have an init expression is appropriate
     */
    @Override
    public JFieldVar apply(String nodeName, JsonNode node, JsonNode parent, JFieldVar field, Schema currentSchema) {

        boolean defaultPresent = node != null && isNotEmpty(node.asText());

        String fieldType = field.type().fullName();

        if (defaultPresent && !field.type().isPrimitive() && node.isNull()) {
            field.init(JExpr._null());

        } else if (fieldType.startsWith(List.class.getName())) {
            field.init(getDefaultList(field.type(), node));

        } else if (fieldType.startsWith(Set.class.getName())) {
            field.init(getDefaultSet(field.type(), node));
        } else if (fieldType.startsWith(String.class.getName()) && node != null ) {
            field.init(getDefaultValue(field.type(), node));
        } else if (defaultPresent) {
            field.init(getDefaultValue(field.type(), node));

        }

        return field;
    }

    private static JExpression getDefaultValue(JType fieldType, JsonNode node) {
        return getDefaultValue(fieldType, node.asText());
    }

    static JExpression getDefaultValue(JType fieldType, String value) {

        fieldType = fieldType.unboxify();

        if (fieldType.fullName().equals(String.class.getName())) {
            return JExpr.lit(value);

        } else if (fieldType.fullName().equals(int.class.getName())) {
            return JExpr.lit(Integer.parseInt(value));

        } else if (fieldType.fullName().equals(BigInteger.class.getName())) {
            return JExpr._new(fieldType).arg(JExpr.lit(value));

        } else if (fieldType.fullName().equals(double.class.getName())) {
            return JExpr.lit(Double.parseDouble(value));

        } else if (fieldType.fullName().equals(BigDecimal.class.getName())) {
            return JExpr._new(fieldType).arg(JExpr.lit(value));

        } else if (fieldType.fullName().equals(boolean.class.getName())) {
            return JExpr.lit(Boolean.parseBoolean(value));

        } else if (fieldType.fullName().equals(DateTime.class.getName()) || fieldType.fullName().equals(Date.class.getName())) {
            long millisecs = parseDateToMillisecs(value);

            JInvocation newDateTime = JExpr._new(fieldType);
            newDateTime.arg(JExpr.lit(millisecs));

            return newDateTime;

        } else if (fieldType.fullName().equals(LocalDate.class.getName()) || fieldType.fullName().equals(LocalTime.class.getName())) {

            JInvocation stringParseableTypeInstance = JExpr._new(fieldType);
            stringParseableTypeInstance.arg(JExpr.lit(value));
            return stringParseableTypeInstance;

        } else if (fieldType.fullName().equals(long.class.getName())) {
            return JExpr.lit(Long.parseLong(value));

        } else if (fieldType.fullName().equals(float.class.getName())) {
            return JExpr.lit(Float.parseFloat(value));

        } else if (fieldType.fullName().equals(URI.class.getName())) {
            JInvocation invokeCreate = fieldType.owner().ref(URI.class).staticInvoke("create");
            return invokeCreate.arg(JExpr.lit(value));

        } else if (fieldType instanceof JDefinedClass && ((JDefinedClass) fieldType).getClassType().equals(ClassType.ENUM)) {

            return getDefaultEnum(fieldType, value);

        } else {
            return JExpr._null();

        }

    }

    /**
     * Creates a default value for a list property by:
     * <ol>
     * <li>Creating a new {@link ArrayList} with the correct generic type
     * <li>Using {@link Arrays#asList(Object...)} to initialize the list with
     * the correct default values
     * </ol>
     *
     * @param fieldType
     *            the java type that applies for this field ({@link List} with
     *            some generic type argument)
     * @param node
     *            the node containing default values for this list
     * @return an expression that creates a default value that can be assigned
     *         to this field
     */
    private JExpression getDefaultList(JType fieldType, JsonNode node) {

        JClass listGenericType = ((JClass) fieldType).getTypeParameters().get(0);

        JClass listImplClass = fieldType.owner().ref(ArrayList.class);
        listImplClass = listImplClass.narrow(listGenericType);

        JInvocation newListImpl = JExpr._new(listImplClass);

        if (node instanceof ArrayNode && node.size() > 0) {
            JInvocation invokeAsList = fieldType.owner().ref(Arrays.class).staticInvoke("asList");
            for (JsonNode defaultValue : node) {
                invokeAsList.arg(getDefaultValue(listGenericType, defaultValue));
            }
            newListImpl.arg(invokeAsList);
        } else if (!ruleFactory.getGenerationConfig().isInitializeCollections()) {
            return JExpr._null();
        }

        return newListImpl;

    }

    /**
     * Creates a default value for a set property by:
     * <ol>
     * <li>Creating a new {@link LinkedHashSet} with the correct generic type
     * <li>Using {@link Arrays#asList(Object...)} to initialize the set with the
     * correct default values
     * </ol>
     *
     * @param fieldType
     *            the java type that applies for this field ({@link Set} with
     *            some generic type argument)
     * @param node
     *            the node containing default values for this set
     * @return an expression that creates a default value that can be assigned
     *         to this field
     */
    private JExpression getDefaultSet(JType fieldType, JsonNode node) {

        JClass setGenericType = ((JClass) fieldType).getTypeParameters().get(0);

        JClass setImplClass = fieldType.owner().ref(LinkedHashSet.class);
        setImplClass = setImplClass.narrow(setGenericType);

        JInvocation newSetImpl = JExpr._new(setImplClass);

        if (node instanceof ArrayNode && node.size() > 0) {
            JInvocation invokeAsList = fieldType.owner().ref(Arrays.class).staticInvoke("asList");
            for (JsonNode defaultValue : node) {
                invokeAsList.arg(getDefaultValue(setGenericType, defaultValue));
            }
            newSetImpl.arg(invokeAsList);
        } else if (!ruleFactory.getGenerationConfig().isInitializeCollections()) {
            return JExpr._null();
        }

        return newSetImpl;

    }

    /**
     * @see EnumRule
     */
    private static JExpression getDefaultEnum(JType fieldType, String value) {

        JDefinedClass enumClass = (JDefinedClass) fieldType;
        JType backingType = enumClass.fields().get("value").type();
        JInvocation invokeFromValue = enumClass.staticInvoke("fromValue");
        invokeFromValue.arg(getDefaultValue(backingType, value));

        return invokeFromValue;
    }

    private static long parseDateToMillisecs(String valueAsText) {

        try {
            return Long.parseLong(valueAsText);
        } catch (NumberFormatException nfe) {
            try {
                return new StdDateFormat().parse(valueAsText).getTime();
            } catch (ParseException pe) {
                throw new IllegalArgumentException("Unable to parse this string as a date: " + valueAsText);
            }
        }
    }
}
