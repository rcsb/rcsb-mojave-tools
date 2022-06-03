package org.rcsb.mojave.tools.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.*;
import org.rcsb.mojave.tools.jsonschema.SchemaLoader;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaModifier;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaType;
import org.rcsb.mojave.tools.jsonschema.traversal.JsonSchemaWalker;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.EnumCollector;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.JavaTypeAnnotator;
import org.rcsb.mojave.tools.jsonschema.traversal.visitors.Visitor;
import org.rcsb.mojave.tools.utils.CommandOptions;
import org.rcsb.mojave.tools.utils.CommonUtils;
import org.rcsb.mojave.tools.utils.NameUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.sun.codemodel.JExpr.lit;
import static java.util.Arrays.asList;
import static org.openjdk.nashorn.internal.runtime.JSType.isString;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Creates a Java {@link Enum} for each enum definition in core JSON schemas.
 *
 * Created on 09/20/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class GenerateSchemaEnums {

    private boolean refResolution;
    private String classNameSuffix;

    private static final String DEFAULT_TYPE_NAME = "any";
    private static final String VALUE_FIELD_NAME = "value";

    private Collection<String> existingConstantNames;

    private void addValueMethod(JDefinedClass enumClass, JFieldVar valueField) {

        JMethod fromValue = enumClass.method(JMod.PUBLIC, valueField.type(), VALUE_FIELD_NAME);

        JBlock body = fromValue.body();
        body._return(JExpr._this().ref(valueField));
    }

    private JFieldVar addValueField(JDefinedClass enumClass, JType type) {

        JFieldVar valueField = enumClass.field(JMod.PRIVATE | JMod.FINAL, type, VALUE_FIELD_NAME);

        JMethod constructor = enumClass.constructor(JMod.PRIVATE);
        JVar valueParam = constructor.param(type, VALUE_FIELD_NAME);
        JBlock body = constructor.body();
        body.assign(JExpr._this().ref(valueField), valueParam);

        return valueField;
    }

    private String getConstantName(String constantName) {

        constantName = NameUtils.makeNameBeLegalJavaName(constantName);

        List<String> enumNameGroups = new ArrayList<>(asList(splitByCharacterTypeCamelCase(constantName)));
        enumNameGroups.removeIf(s -> (containsOnly(s, "_") || s.trim().isEmpty()));
        String enumName = upperCase(join(enumNameGroups, "_"));

        if (isEmpty(enumName))
            enumName = "__EMPTY__";

        enumName = NameUtils.makeNameBeLegalJavaName(enumName);

        return NameUtils.makeUnique(enumName, existingConstantNames);
    }

    private void setConstantValue(JEnumConstant constant, JsonNode enumValue) {

        if (enumValue.isTextual())
            constant.arg(lit(enumValue.asText()));
        else if (enumValue.isInt())
            constant.arg(lit(enumValue.asInt()));
        else if (enumValue.isDouble() || enumValue.isFloat())
            constant.arg(lit(enumValue.asDouble()));
        else if (enumValue.isBoolean())
            constant.arg(lit(enumValue.asBoolean()));
        else
            constant.arg(lit(enumValue.asText()));
    }

    private void addEnumConstants(JsonNode node, JDefinedClass enumClass) {

        if (!node.has(MetaSchemaProperty.ENUM))
            throw new IllegalArgumentException("Schema wasn't annotated correctly: 'enum' metadata is not available for "
                    + node.toString());

        for (JsonNode n : node.get(MetaSchemaProperty.ENUM)) {

            String constantName = getConstantName(n.asText());
            existingConstantNames.add(constantName);

            JEnumConstant constant = enumClass.enumConstant(constantName);
            setConstantValue(constant, n);
        }
    }

    private JFieldVar addQuickLookupMap(JDefinedClass enumClass, JType backingType) {

        JClass lookupType = enumClass.owner().ref(Map.class).narrow(backingType.boxify(), enumClass);
        JFieldVar lookupMap = enumClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, lookupType, "CONSTANTS");

        JClass lookupImplType = enumClass.owner().ref(HashMap.class).narrow(backingType.boxify(), enumClass);
        lookupMap.init(JExpr._new(lookupImplType));

        JForEach forEach = enumClass.init().forEach(enumClass, "c", JExpr.invoke("values"));
        JInvocation put = forEach.body().invoke(lookupMap, "put");
        put.arg(forEach.var().ref(VALUE_FIELD_NAME));
        put.arg(forEach.var());

        return lookupMap;
    }

    private void addFactoryMethod(JDefinedClass enumClass, JType backingType) {

        JFieldVar quickLookupMap = addQuickLookupMap(enumClass, backingType);

        JMethod fromValue = enumClass.method(JMod.PUBLIC | JMod.STATIC, enumClass, "fromValue");
        JVar valueParam = fromValue.param(backingType, VALUE_FIELD_NAME);

        JBlock body = fromValue.body();
        JVar constant = body.decl(enumClass, "constant");
        constant.init(quickLookupMap.invoke("get").arg(valueParam));

        JConditional ifConditional = body._if(constant.eq(JExpr._null()));

        JInvocation illegalArgumentException = JExpr._new(enumClass.owner().ref(IllegalArgumentException.class));
        JExpression expr = valueParam;

        // if string no need to add ""
        if (!isString(backingType)) {
            expr = expr.plus(JExpr.lit(""));
        }

        illegalArgumentException.arg(expr);
        ifConditional._then()._throw(illegalArgumentException);
        ifConditional._else()._return(constant);
    }

    private String getTypeName(JsonNode node) {

        if (node.has(MetaSchemaProperty.ITEMS)
                && node.get(MetaSchemaProperty.ITEMS).isArray()
                && node.get(MetaSchemaProperty.ITEMS).size() > 0) {
            for (JsonNode jsonNode : node.get(MetaSchemaProperty.ITEMS)) {
                String typeName = jsonNode.asText();
                if (!typeName.equals(MetaSchemaType.NULL)) {
                    return typeName;
                }
            }
        }

        if (node.has(MetaSchemaProperty.TYPE)
                && node.get(MetaSchemaProperty.TYPE).isTextual()) {
            return node.get(MetaSchemaProperty.TYPE).asText();
        }

        return DEFAULT_TYPE_NAME;
    }

    /**
     * Applies this type rule to take the required code generation steps. When applied,
     * this rule reads the details of the given node to determine the appropriate Java
     * type to return.
     *
     * @param cm   the code model into which any newly generated type may be placed.
     * @param node the node for which this "type" rule applies.
     * @return the Java type which, after reading the details of the given
     * schema node, most appropriately matches the "type" specified.
     */
    private JType applyTypeRule(JCodeModel cm, JsonNode node) {

        String propertyTypeName = getTypeName(node);

        JType type;
        switch (propertyTypeName) {
            case MetaSchemaType.STRING:
                type = cm._ref(String.class);
                break;

            case MetaSchemaType.NUMBER:
                type = cm._ref(Double.class);
                break;

            case MetaSchemaType.INTEGER:
                type = cm._ref(Integer.class);
                break;

            case MetaSchemaType.BOOLEAN:
                type = cm._ref(Boolean.class);
                break;

            case MetaSchemaType.ARRAY:
                type = cm._ref(List.class);
                break;

            default:
                type = cm._ref(Object.class);
                break;
        }

        return type;
    }

    /**
     * Uses CodeModel java source code generation library to generate code model.
     *
     * @param node the note of JSON schema holding set of enum values.
     * @return code model that provides a way to generate Java code.
     * @throws JClassAlreadyExistsException when the specified class/interface was already created.
     */
    private JCodeModel createCodeModel(JsonNode node) throws JClassAlreadyExistsException {

        JCodeModel cm = new JCodeModel();
        existingConstantNames = new ArrayList<>();

        if (node.has(MetaSchemaProperty.ITEMS))
            node = node.get(MetaSchemaProperty.ITEMS);

        if (!node.has(MetaSchemaModifier.JAVA_TYPE))
            throw new IllegalArgumentException("Schema wasn't annotated correctly: 'javaType' metadata is not available for "
                    + node.toString());

        String fqn = node.get(MetaSchemaModifier.JAVA_TYPE).asText();
        JDefinedClass enumClass = cm._class(JMod.PUBLIC, fqn, ClassType.ENUM);

        // If type is specified on the enum, get a type rule for it.  Otherwise, we're a string.
        JType backingType = node.has(MetaSchemaProperty.TYPE) ? applyTypeRule(cm, node) : cm._ref(String.class);

        JFieldVar valueField = addValueField(enumClass, backingType);

        addValueMethod(enumClass, valueField);

        addEnumConstants(node, enumClass);

        addFactoryMethod(enumClass, backingType);

        return cm;
    }

    private void withClassNamePostfix(String postfix) {
        classNameSuffix = postfix;
    }

    private void withDynamicRefResolution(boolean flag) {
        refResolution = flag;
    }

    private void run(File schemasDir, File outputDir, String fqp) throws IOException, JClassAlreadyExistsException {

        Collection<File> files = CommonUtils.listSchemaFiles(schemasDir);
        if (files.size() == 0)
            throw new IllegalStateException("There are no schemas to process in " + schemasDir.getAbsolutePath());

        JavaTypeAnnotator javaTypeNameVisitor = new JavaTypeAnnotator()
                .setTargetPackage(fqp)
                .withSuffix(classNameSuffix);

        EnumCollector javaEnumVisitor = new EnumCollector();
        List<Visitor> visitors = asList(javaTypeNameVisitor, javaEnumVisitor);

        SchemaLoader loader = new SchemaLoader();

        for (File f : files) {
            JsonNode schema = loader.readSchema(f.toURI());
            JsonSchemaWalker walker = new JsonSchemaWalker.Builder()
                    .fromInstance(schema)
                    .withDynamicRefResolution(refResolution)
                    .acceptingVisitors(visitors)
                    .build();
            walker.walk();
        }

        CommonUtils.ensurePathToFolderExist(outputDir);
        for (JsonNode e : javaEnumVisitor.getSelectedNodes()) {
            JCodeModel codeModel = createCodeModel(e);
            CommonUtils.writeClassToFile(outputDir, codeModel);
        }
    }

    public static void main(String[] args) throws Exception {

        CommandOptions cmd = new CommandOptions(args);
        if (!cmd.hasOption("-i"))
            throw new IllegalArgumentException("Source Directory (-i) argument is not specified.");
        if (!cmd.hasOption("-o"))
            throw new IllegalArgumentException("Output Directory (-o) argument is not specified.");
        if (!cmd.hasOption("-t"))
            throw new IllegalArgumentException("Target Package (-t) argument is not specified.");

        String sourceDirectory = cmd.valueOf("-i").get(0);
        String outputDirectory = cmd.valueOf("-o").get(0);
        String targetPackage = cmd.valueOf("-t").get(0);

        String classNameSuffix = null;
        if (cmd.hasOption("-s"))
            classNameSuffix = cmd.valueOf("-s").get(0);

        boolean withDynamicRefResolution = false;
        if (cmd.hasOption("-d"))
            withDynamicRefResolution = Boolean.parseBoolean(cmd.valueOf("-d").get(0));

        File schemaDir = new File(sourceDirectory);
        if (!schemaDir.exists())
            throw new IllegalStateException("Folder with input schemas does not exist.");

        File[] files = schemaDir.listFiles(File::isFile);

        if (files == null || files.length == 0)
            throw new IllegalStateException("There are no schemas to process in "+schemaDir.getAbsolutePath());

        File outputDir = new File(outputDirectory);

        GenerateSchemaEnums generator = new GenerateSchemaEnums();
        generator.withClassNamePostfix(classNameSuffix);
        generator.withDynamicRefResolution(withDynamicRefResolution);
        generator.run(schemaDir, outputDir, targetPackage);
    }
}
