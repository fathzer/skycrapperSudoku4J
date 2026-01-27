package com.fathzer.skycrapper;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class InputDataParserTest {
    private final InputDataParser parser = new InputDataParser();

    @Test
    void testParseValid4x4() {
        String input = "1 2 3 4 4 3 2 1 1 2 2 1 4 3 3 4";
        InputData data = parser.parse(input);
        
        assertArrayEquals(new int[]{1, 2, 3, 4}, data.up());
        assertArrayEquals(new int[]{4, 3, 2, 1}, data.down());
        assertArrayEquals(new int[]{1, 2, 2, 1}, data.left());
        assertArrayEquals(new int[]{4, 3, 3, 4}, data.right());
    }

    @Test
    void testParseValid1x1() {
        String input = "1 1 1 1";
        InputData data = parser.parse(input);
        assertArrayEquals(new int[]{1}, data.up());
        assertArrayEquals(new int[]{1}, data.down());
        assertArrayEquals(new int[]{1}, data.left());
        assertArrayEquals(new int[]{1}, data.right());
    }

    @Test
    void testParseInvalidCount() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("1 2 3"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("1 2 3 4 5"));
    }

    @Test
    void testParseOutOfRange() {
        // N=1, but value is 2
        assertThrows(IllegalArgumentException.class, () -> parser.parse("2 1 1 1"));
        // Value is negative
        assertThrows(IllegalArgumentException.class, () -> parser.parse("-1 1 1 1"));
    }

    @Test
    void testParseInvalidInteger() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("1 2 a 4"));
    }

    @Test
    void testParseNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null));
        assertThrows(IllegalArgumentException.class, () -> parser.parse(""));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("   "));
    }
}
