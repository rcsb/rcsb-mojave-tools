package org.rcsb.mojave.tools.jsonschema2pojo.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.*;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.rules.RuleFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Applies the custom "required" schema rule that attaches annotations to the fields as well as getters/setters.
 *
 * @see <a
 *  href="https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.7">https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.7</a>
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class CustomRequiredRule implements Rule<JDocCommentable, JDocCommentable> {

    /**
     * Text added to JavaDoc to indicate that a field is not required
     */
    private static final String REQUIRED_COMMENT_TEXT = "\nThis property is not nullable";

    private final RuleFactory ruleFactory;

    CustomRequiredRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    private void annotateType(JDocCommentable type, JClass annotation) {
        if (type instanceof JFieldVar)
            ((JFieldVar) type).annotate(annotation);
        else if (type instanceof JMethod)
            ((JMethod) type).annotate(annotation);
    }

    @Override
    public JDocCommentable apply(String s, JsonNode node, JDocCommentable genType, Schema schema) {

        JCodeModel cm = new JCodeModel();

        if (node.asBoolean()) {
            genType.javadoc().append(REQUIRED_COMMENT_TEXT);
            if (ruleFactory.getGenerationConfig().isIncludeJsr303Annotations())
                annotateType(genType, cm.directClass(NotNull.class.getSimpleName()));
        }

        if (ruleFactory.getGenerationConfig().isIncludeJsr305Annotations())
            annotateType(genType, cm.directClass(Nullable.class.getSimpleName()));

        return genType;
    }
}
