package com.epimorphics.dclib.values;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueBaseTest {
    private String toCamelCase(String value) {
        return new ValueString(value).toCamelCaseSegment().getValue().toString();

    }

    @Test
    public void toCamelCaseSegment_withoutSeparatorChars_returnsValue() {
        assertEquals("test", toCamelCase("test"));
    }

    @Test
    public void toCamelCaseSegment_withSeparators_returnsSegments() {
        assertEquals("testWithSeparators", toCamelCase("test_with-separators"));
    }

    @Test
    public void toCamelCaseSegment_withEmptySegments_returnsNonEmptySegments() {
        assertEquals("testWithEmpty", toCamelCase("/test_with--empty/"));
    }

    @Test
    public void toCamelCaseSegment_withApostrophes_removesApostrophes() {
        assertEquals("testWithApostrophes", toCamelCase("'test-with_ap'ostr'ophes'"));
    }

    @Test
    public void toCamelCaseSegment_withAllowedChars_returnsSegments() {
        assertEquals("test$@with.~allowedChars", toCamelCase("'test$-@with.~allowed-chars"));
    }
}