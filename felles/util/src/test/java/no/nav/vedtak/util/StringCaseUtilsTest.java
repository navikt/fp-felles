package no.nav.vedtak.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class StringCaseUtilsTest {

    @Test
    void testNullInput() {
        assertNull(StringCaseUtils.formaterMedStoreOgSmåBokstaver(null));
    }

    @Test
    void testEmptyString() {
        assertNull(StringCaseUtils.formaterMedStoreOgSmåBokstaver(""));
    }

    @Test
    void testNoPunctuation() {
        assertEquals("Text", StringCaseUtils.formaterMedStoreOgSmåBokstaver("text"));
    }

    @Test
    void testUpperNoPunctuation() {
        assertEquals("Text", StringCaseUtils.formaterMedStoreOgSmåBokstaver("TEXT"));
    }

    @Test
    void testLeadingPunctuation() {
        assertEquals(".Text", StringCaseUtils.formaterMedStoreOgSmåBokstaver(".text"));
    }

    @Test
    void testTrailingPunctuation() {
        assertEquals("Text.", StringCaseUtils.formaterMedStoreOgSmåBokstaver("text."));
    }

    @Test
    void testMixedCasing() {
        assertEquals("Text", StringCaseUtils.formaterMedStoreOgSmåBokstaver("TeXT"));
    }

    @Test
    void testMultipleSpaces() {
        assertEquals("Text  With Multiple  Spaces", StringCaseUtils.formaterMedStoreOgSmåBokstaver("Text  with multiple  spaces"));
    }

    @Test
    void navnMedDash() {
        assertEquals("Fornavn-Fornavn Etternavn C/O Annetnavn As", StringCaseUtils.formaterMedStoreOgSmåBokstaver("FORNAVN-FORNAVN ETTERNAVN C/O ANNETNAVN AS"));
    }

    @Test
    void testSpecialCharacters() {
        assertEquals("Text@123", StringCaseUtils.formaterMedStoreOgSmåBokstaver("TeXT@123"));
    }

    @Test
    void testOnlyPunctuation() {
        assertEquals("...!!!", StringCaseUtils.formaterMedStoreOgSmåBokstaver("...!!!"));
    }

    @Test
    void testMultiplePunctuationMarks() {
        assertEquals(".-.Text", StringCaseUtils.formaterMedStoreOgSmåBokstaver(".-.text"));
    }

}
