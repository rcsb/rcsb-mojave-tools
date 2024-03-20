package org.rcsb.mojave.tools.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 *
 * Created on 1/30/19.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public final class NameUtils {

    private static final Logger logger = LoggerFactory.getLogger(NameUtils.class);

    private NameUtils() {}

    // Java name can contain only latin characters (english uppercase or lowercase characters),
    // numbers, the dollar sign ( $ ), and the underscore character ( _ ).

    // Java name is a valid identifier ONLY if beginning with a letter, the dollar sign "$",
    // or the underscore character "_".
    private static final Pattern ILLEGAL_FIRST_CHAR_REGEX = Pattern.compile("^[^a-zA-Z_$].*$");

    // Java name can contain only latin characters (english uppercase or lowercase characters),
    // numbers, the dollar sign ( $ ), and the underscore character ( _ ).
    private static final Pattern ILLEGAL_CHAR_REGEX = Pattern.compile("[^0-9a-zA-Z_$]");

    /**
     * Utilities to ensure that generated Java field names are consistent with
     * the specification for Java language. For more information refer to
     * <a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/variables.html">Java
     * rules for naming variables</a>.
     *
     * @param name the name to be converted to legal Java name.
     *
     * @return name where characters, that are not allowed by Java language, are replaced with '_'.
     */
    public static String makeNameBeLegalJavaName(String name) {
        String modified = ILLEGAL_CHAR_REGEX.matcher(name).replaceAll("_");
        if (ILLEGAL_FIRST_CHAR_REGEX.matcher(name).matches()) {
            modified = "_" + modified;
        }
        if (!modified.equals(name.replace(' ', '_'))) {
            logger.info("Renamed field '{}' → '{}'.", name, modified);
        }
        return modified;
    }

    /**
     * Helps to create a mapping between case sensitive names and their case insensitive
     * representation.
     *
     * @param name a name to be registered.
     * @param existingNames a collection of names already registered.
     *
     * @return case insensitive representation for a name
     */
    public static String makeUnique(String name, Collection<String> existingNames) {
        for (String existingName : existingNames) {
            if (name.equalsIgnoreCase(existingName)) {
                return name;
            }
        }
        return makeUnique(name + "_", existingNames);
    }
}
