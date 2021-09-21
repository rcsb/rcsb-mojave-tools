package org.rcsb.mojave.tools.jsonschema2pojo.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.*;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.exception.ClassAlreadyExistsException;
import org.jsonschema2pojo.exception.GenerationException;
import org.jsonschema2pojo.rules.ObjectRule;
import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.util.AnnotationHelper;
import org.jsonschema2pojo.util.ParcelableHelper;
import org.jsonschema2pojo.util.ReflectionHelper;
import org.jsonschema2pojo.util.SerializableHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created on 9/21/21.
 * TODO: fix @since tag
 *
 * @author Yana Rose
 * @since 1.4.0
 */
public class CustomObjectRule extends ObjectRule {

    private final RuleFactory ruleFactory;
    private final ReflectionHelper reflectionHelper;

    public CustomObjectRule(RuleFactory ruleFactory, ParcelableHelper parcelableHelper, ReflectionHelper reflectionHelper) {
        super(ruleFactory, parcelableHelper, reflectionHelper);
        this.ruleFactory = ruleFactory;
        this.reflectionHelper = reflectionHelper;
    }

    @Override
    public JType apply(String nodeName, JsonNode node, JsonNode parent, JPackage jPackage, Schema schema) {
        try {
            return super.apply(nodeName, node, parent, jPackage, schema);
        } catch (GenerationException e) {
            if (node.has("javaType") && node.path("javaType").asText().contains("<"))
                return generateGenericType(nodeName, node, parent, jPackage, schema);
            throw e;
        }
    }

    private JType generateGenericType(String nodeName, JsonNode node, JsonNode parent, JPackage _package, Schema schema) {
        JType superType = reflectionHelper.getSuperType(nodeName, node, _package, schema);
        if (superType.isPrimitive() || reflectionHelper.isFinal(superType)) {
            return superType;
        }

        JDefinedClass jclass;
        try {
            jclass = createClass(node, _package);
        } catch (ClassAlreadyExistsException e) {
            return e.getExistingClass();
        }

        jclass._extends((JClass) superType);

        schema.setJavaTypeIfEmpty(jclass);

        if (node.has("title")) {
            ruleFactory.getTitleRule().apply(nodeName, node.get("title"), node, jclass, schema);
        }
        if (node.has("description")) {
            ruleFactory.getDescriptionRule().apply(nodeName, node.get("description"), node, jclass, schema);
        }
        if (node.has("$comment")) {
            ruleFactory.getCommentRule().apply(nodeName, node.get("$comment"), node, jclass, schema);
        }
        // Creates the class definition for the builder
        if (ruleFactory.getGenerationConfig().isGenerateBuilders() && ruleFactory.getGenerationConfig().isUseInnerClassBuilders()){
            ruleFactory.getBuilderRule().apply(nodeName, node, parent, jclass, schema);
        }

        ruleFactory.getPropertiesRule().apply(nodeName, node.get("properties"), node, jclass, schema);

        if (node.has("javaInterfaces")) {
            invoke("addInterfaces", new Class<?>[]{JDefinedClass.class,JsonNode.class}, new Object[]{jclass,node.get("javaInterfaces")});
        }

        ruleFactory.getAdditionalPropertiesRule().apply(nodeName, node.get("additionalProperties"), node, jclass, schema);

        ruleFactory.getDynamicPropertiesRule().apply(nodeName, node.get("properties"), node, jclass, schema);

        if (node.has("required")) {
            ruleFactory.getRequiredArrayRule().apply(nodeName, node.get("required"), node, jclass, schema);
        }
        if (ruleFactory.getGenerationConfig().isIncludeGeneratedAnnotation()) {
            AnnotationHelper.addGeneratedAnnotation(jclass);
        }
        if (ruleFactory.getGenerationConfig().isIncludeToString()) {
            invoke("addToString", new Class<?>[]{JDefinedClass.class}, new Object[]{jclass});
        }
        if (ruleFactory.getGenerationConfig().isIncludeHashcodeAndEquals()) {
            invoke("addHashCode", new Class<?>[]{JDefinedClass.class,JsonNode.class}, new Object[]{jclass,node});
            invoke("addEquals", new Class<?>[]{JDefinedClass.class,JsonNode.class}, new Object[]{jclass,node});
        }
        if (ruleFactory.getGenerationConfig().isParcelable()) {
            invoke("addParcelSupport", new Class<?>[]{JDefinedClass.class}, new Object[]{jclass});
        }
        if (ruleFactory.getGenerationConfig().isIncludeConstructors()) {
            ruleFactory.getConstructorRule().apply(nodeName, node, parent, jclass, schema);
        }
        if (ruleFactory.getGenerationConfig().isSerializable()) {
            SerializableHelper.addSerializableSupport(jclass);
        }

        return jclass;
    }

    private void invoke(String methodName, Class<?>[] parameterTypes, Object[] args) {
        try {
            Method m = getClass().getSuperclass().getDeclaredMethod(methodName, parameterTypes);
            m.setAccessible(true);
            m.invoke(this, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new GenerationException(e);
        }
    }

    private JDefinedClass createClass(JsonNode node, JPackage _package) throws ClassAlreadyExistsException {

        Annotator annotator = ruleFactory.getAnnotator();

        String genericTypeName = "T";
        String fqn = node.path("javaType").asText().replace("<T>", "");
        try {
            JDefinedClass newType = _package.owner()._class(JMod.PUBLIC, fqn, ClassType.CLASS);
            newType.generify(genericTypeName);
            annotator.typeInfo(newType, node);
            annotator.propertyInclusion(newType, node);
            return newType;
        } catch (JClassAlreadyExistsException e) {
            throw new ClassAlreadyExistsException(e.getExistingClass());
        }
    }
}
