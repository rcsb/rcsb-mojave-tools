package org.rcsb.mojave.tools.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 *
 * Created on 1/30/19.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class NameUtils {

    private static final Logger logger = LoggerFactory.getLogger(NameUtils.class);

    private NameUtils() {}

    // Java name can contain only latin characters (english uppercase or lowercase characters),
    // numbers, the dollar sign ( $ ), and the underscore character ( _ ).
    private static final String JAVA_ILLEGAL_NAME_REGEX = ".*[^0-9a-zA-Z_$].*";

    // Java name is a valid identifier ONLY if beginning with a letter, the dollar sign "$",
    // or the underscore character "_".
    private static final String JAVA_NAME_ILLEGAL_FIRST_CHARACTER_REGEX = "^[^a-zA-Z_$].*$";

    // Java name can contain only latin characters (english uppercase or lowercase characters),
    // numbers, the dollar sign ( $ ), and the underscore character ( _ ).
    private static final String JAVA_NAME_ILLEGAL_CHARACTER_SET_REGEX = "[^0-9a-zA-Z_$]";

    /**
     * Utilities to ensure that generated Java field names are consistent with
     * the specification for Java language. For more information refer to
     * <a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/variables.html">Java
     * rules for naming variables</>.
     *
     * @param name the name to be converted to legal Java name.
     *
     * @return name where characters, that are not allowed by Java language, are replaced with '_'.
     */
    public static String makeNameBeLegalJavaName(String name) {

        if (name.matches(JAVA_ILLEGAL_NAME_REGEX) || name.matches(JAVA_NAME_ILLEGAL_FIRST_CHARACTER_REGEX)) {
            String modifiedName = name.replaceAll(JAVA_NAME_ILLEGAL_CHARACTER_SET_REGEX, "_");
            if(modifiedName.matches(JAVA_NAME_ILLEGAL_FIRST_CHARACTER_REGEX))
                modifiedName ="_"+modifiedName;
            logger.warn("Field name \"{}\" violates naming rules for Java language. This name will be changed to \"{}\".",
                    name, modifiedName);
            return modifiedName;
        }

        return name;
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
        boolean found = false;
        for (String existingName : existingNames) {
            if (name.equalsIgnoreCase(existingName)) {
                found = true;
                break;
            }
        }
        if (found) {
            name = makeUnique(name + "_", existingNames);
        }
        return name;
    }
}
