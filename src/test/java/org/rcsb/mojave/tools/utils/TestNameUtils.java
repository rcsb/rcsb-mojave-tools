package org.rcsb.mojave.tools.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created on 3/26/19.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class TestNameUtils {

    @Test
    public void shouldConvertNameWithIllegalCharactersToLegalJavaName() {

        // Java name can contain only latin characters (english uppercase or lowercase characters),
        // numbers, the dollar sign ( $ ), and the underscore character ( _ ).
        String illegalName = "%badName)";
        String legalName = NameUtils.makeNameBeLegalJavaName(illegalName);
        assertFalse(legalName.contains("%"));
        assertFalse(legalName.contains(")"));
    }

    @Test
    public void shouldConvertNameStartingWithIllegalCharacterToLegalJavaName() {

        // Java name is a valid identifier ONLY if beginning with a letter, the dollar sign "$",
        // or the underscore character "_".
        String illegalName = "1badName)";
        String legalName = NameUtils.makeNameBeLegalJavaName(illegalName);
        assertTrue(legalName.startsWith("_"));
    }

    @Test
    public void shouldConvertNameStartingWithLegalCharacterToLegalJavaName() {

        // Java name is a valid identifier ONLY if beginning with a letter, the dollar sign "$",
        // or the underscore character "_".
        String illegalName = "$goodName)";
        String legalName = NameUtils.makeNameBeLegalJavaName(illegalName);
        assertTrue(legalName.startsWith("$"));
    }
}
