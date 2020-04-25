package org.rcsb.mojave.tools.jsonschema.traversal.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaModifier;
import org.rcsb.mojave.tools.jsonschema.constants.MetaSchemaProperty;
import org.rcsb.mojave.common.jsonschema.MetaSchemaType;
import org.rcsb.mojave.tools.jsonschema.constants.TraversalLabel;
import org.rcsb.mojave.tools.jsonschema.traversal.model.TraversalContext;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.Visitable;
import org.rcsb.mojave.tools.jsonschema.traversal.visitables.VisitableNode;
import org.rcsb.mojave.tools.jsonschema.utils.JsonSchemaNodeUtils;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Annotates JSON schema with 'javaType' keyword. Java names for types are constructed using the name for a given node
 * prefixed with its parent name. Snake case is converted to camel case to conform to java naming convention.
 *
 * Created on 8/27/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class JavaTypeAnnotator implements Visitor {

    private static final String ILLEGAL_CHARACTER_SET_REGEX = "[^0-9a-zA-Z$]";

    private static List<String> specialFieldNames = asList("type", "source");

    private String targetPackage;

    private String prefix;
    private String suffix;

    public JavaTypeAnnotator setTargetPackage(String string) {
        targetPackage = string;
        return this;
    }

    public JavaTypeAnnotator withPrefix(String string) {
        prefix = string;
        return this;
    }

    public JavaTypeAnnotator withSuffix(String string) {
        suffix = string;
        return this;
    }

    private  String toTitleCase(String givenString) {

        givenString = givenString.replaceAll(ILLEGAL_CHARACTER_SET_REGEX, " ");

        String[] arr = givenString.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String anArr : arr) {
            sb.append(Character.toUpperCase(anArr.charAt(0)))
                    .append(anArr.substring(1));
        }
        return sb.toString().trim();
    }

    /**
     * Implements the visit operation defined in the {@link Visitor} interface. This operation will extend
     * the node content with 'javaType' property set to a fully qualified name for a Java type. An extension property
     * 'javaType' that applies to schema and allows specifying a fully qualified name for Java types generated by
     * @see <a href="https://github.com/joelittlejohn/jsonschema2pojo">org.rcsb.mojave.tools.jsonschema2pojo</a> tool.
     */
    @Override
    public void visit(Visitable visitableNode) {

        if (!(visitableNode instanceof VisitableNode))
            throw new IllegalArgumentException("Node object MUST be an instance of VisitableNode.");

        JsonNode node = ((VisitableNode) visitableNode).getNode();
        TraversalContext ctx = ((VisitableNode) visitableNode).getTraversalContext();

        // coerce date format
        if (JsonSchemaNodeUtils.isDate(node))
            node = ((ObjectNode) node).put(MetaSchemaProperty.FORMAT, MetaSchemaType.DATE_TIME);

        if (ctx.isRef() || (ctx.getLineage().size() == 0) || node.has(MetaSchemaModifier.JAVA_TYPE))
            return;

        // If this is object or enum apply naming strategy where current name
        // and parent name are concatenated.
        if (ctx.getLabel().equals(TraversalLabel.OBJECT)
                || (!ctx.getLabel().equals(TraversalLabel.PROPERTIES) && JsonSchemaNodeUtils.isEnum(node))) {

            String name = ctx.getCurrentFieldName();
            String className  = toTitleCase(name);

            if (ctx.getParentFieldName() != null) {
                String parentName = ctx.getParentFieldName();
                className = toTitleCase(parentName)+className;
                if (ctx.getLineage().size() >= 3 && specialFieldNames.contains(name)) {
                    String additionalPrefix = ctx.getLineage().get(ctx.getLineage().size() - 3);
                    additionalPrefix = toTitleCase(additionalPrefix);
                    className = additionalPrefix+className;
                }
            }

            if (suffix != null)
                className = className+toTitleCase(suffix);
            if (prefix != null)
                className = toTitleCase(prefix)+className;

            String fqn = String.join(".", targetPackage, className);
            ((ObjectNode) node).put(MetaSchemaModifier.JAVA_TYPE, fqn);
        }
    }
}
