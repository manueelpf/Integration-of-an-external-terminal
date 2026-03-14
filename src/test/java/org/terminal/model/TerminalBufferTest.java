package org.terminal.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests terminal buffer behavior and edge cases.
 */
class TerminalBufferTest {

    @Test
    void shouldInitializeEmptyScreen() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 10);

        assertEquals(5, buffer.getWidth());
        assertEquals(3, buffer.getHeight());
        assertEquals(0, buffer.getScrollbackLineCount());
        assertEquals(new CursorPosition(0, 0), buffer.getCursorPosition());
        assertEquals("\n\n", buffer.getScreenContentAsString());
    }

    @Test
    void shouldRejectInvalidConstructorArguments() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(0, 3, 10));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(5, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(5, 3, -1));
    }

    @Test
    void shouldClampCursorToScreenBounds() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 10);

        buffer.setCursorPosition(100, 100);
        assertEquals(new CursorPosition(4, 2), buffer.getCursorPosition());

        buffer.setCursorPosition(-5, -3);
        assertEquals(new CursorPosition(0, 0), buffer.getCursorPosition());
    }

    @Test
    void shouldMoveCursorWithinBounds() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 10);

        buffer.moveCursorRight(2);
        buffer.moveCursorDown(1);
        assertEquals(new CursorPosition(2, 1), buffer.getCursorPosition());

        buffer.moveCursorLeft(10);
        buffer.moveCursorUp(10);
        assertEquals(new CursorPosition(0, 0), buffer.getCursorPosition());
    }

    @Test
    void shouldWriteTextAndOverwriteCells() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 10);

        buffer.writeText("abc");
        assertEquals("abc\n\n", buffer.getScreenContentAsString());
        assertEquals(new CursorPosition(3, 0), buffer.getCursorPosition());

        buffer.setCursorPosition(1, 0);
        buffer.writeText("Z");
        assertEquals("aZc\n\n", buffer.getScreenContentAsString());
    }

    @Test
    void shouldWrapTextWhenWriting() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);

        buffer.writeText("abcdef");
        assertEquals("abcd\nef", buffer.getScreenContentAsString());
        assertEquals(new CursorPosition(2, 1), buffer.getCursorPosition());
    }

    @Test
    void shouldHandleNewlinesWhenWriting() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 10);

        buffer.writeText("ab\ncd");
        assertEquals("ab\ncd\n", buffer.getScreenContentAsString());
        assertEquals(new CursorPosition(2, 1), buffer.getCursorPosition());
    }

    @Test
    void shouldInsertTextAndShiftLineContent() {
        TerminalBuffer buffer = new TerminalBuffer(6, 3, 10);

        buffer.writeText("abef");
        buffer.setCursorPosition(2, 0);
        buffer.insertText("cd");

        assertEquals("abcdef\n\n", buffer.getScreenContentAsString());
    }

    @Test
    void shouldWrapOverflowWhenInserting() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);

        buffer.writeText("abcd");
        buffer.setCursorPosition(2, 0);
        buffer.insertText("XY");

        assertEquals("abXY\ncd", buffer.getScreenContentAsString());
    }

    @Test
    void shouldInsertWideCharacterNearBoundary() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);

        buffer.writeText("abc");
        buffer.setCursorPosition(3, 0);
        buffer.insertText("界");

        assertEquals("abc\n界", buffer.getScreenContentAsString());
    }

    @Test
    void shouldFillCurrentLineUsingCurrentAttributes() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 10);

        buffer.setCursorPosition(0, 1);
        buffer.setCurrentAttributes(
                TerminalColor.RED,
                TerminalColor.BLACK,
                EnumSet.of(TextStyle.BOLD, TextStyle.UNDERLINE)
        );
        buffer.fillCurrentLine('*');

        assertEquals("\n*****\n", buffer.getScreenContentAsString());
        for (int col = 0; col < 5; col++) {
            assertEquals('*', buffer.getCharacterAt(col, 1));
            CellAttributes attributes = buffer.getAttributesAt(col, 1);
            assertEquals(TerminalColor.RED, attributes.getForeground());
            assertEquals(TerminalColor.BLACK, attributes.getBackground());
            assertTrue(attributes.hasStyle(TextStyle.BOLD));
            assertTrue(attributes.hasStyle(TextStyle.UNDERLINE));
        }
    }

    @Test
    void shouldClearCurrentLineWhenFillCharacterIsNull() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);

        buffer.writeText("hello");
        buffer.setCursorPosition(0, 0);
        buffer.fillCurrentLine(null);

        assertEquals("\n", buffer.getScreenContentAsString());
    }

    @Test
    void shouldInsertEmptyLineAtBottomAndPushTopToScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 5);

        buffer.writeText("abcd");
        buffer.setCursorPosition(0, 1);
        buffer.writeText("ef");

        buffer.insertEmptyLineAtBottom();

        assertEquals(1, buffer.getScrollbackLineCount());
        assertEquals("abcd", buffer.getLineAsString(0));
        assertEquals("ef\n", buffer.getScreenContentAsString());
    }

    @Test
    void shouldScrollWhenWritingPastBottom() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 5);

        buffer.writeText("line1\nline2\nline3");

        assertEquals(4, buffer.getScrollbackLineCount());
        assertEquals("line", buffer.getLineAsString(0));
        assertEquals("1", buffer.getLineAsString(1));
        assertEquals("line", buffer.getLineAsString(2));
        assertEquals("2", buffer.getLineAsString(3));
    }

    @Test
    void shouldRespectScrollbackMaxSize() {
        TerminalBuffer buffer = new TerminalBuffer(3, 1, 2);

        buffer.writeText("aaa\nbbb\nccc\nddd");

        assertEquals(2, buffer.getScrollbackLineCount());
        assertEquals("bbb", buffer.getLineAsString(0));
        assertEquals("ccc", buffer.getLineAsString(1));
        assertEquals("ddd", buffer.getLineAsString(2));
    }

    @Test
    void shouldTrimOldestScrollbackLinesUnderRepeatedOperations() {
        TerminalBuffer buffer = new TerminalBuffer(3, 1, 2);

        buffer.writeText("111\n222\n333\n444\n555");

        assertEquals(2, buffer.getScrollbackLineCount());
        assertEquals("333", buffer.getLineAsString(0));
        assertEquals("444", buffer.getLineAsString(1));
        assertEquals("555", buffer.getLineAsString(2));
    }

    @Test
    void shouldClearScreenButKeepScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 5);

        buffer.writeText("aaa\nbbb\nccc");
        int scrollbackBefore = buffer.getScrollbackLineCount();

        buffer.clearScreen();

        assertEquals(scrollbackBefore, buffer.getScrollbackLineCount());
        assertEquals("\n", buffer.getScreenContentAsString());
        assertEquals(new CursorPosition(0, 0), buffer.getCursorPosition());
    }

    @Test
    void shouldClearScreenAndScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 5);

        buffer.writeText("aaa\nbbb\nccc");
        buffer.clearScreenAndScrollback();

        assertEquals(0, buffer.getScrollbackLineCount());
        assertEquals(2, buffer.getTotalLineCount());
        assertEquals("\n", buffer.getScreenContentAsString());
        assertEquals(new CursorPosition(0, 0), buffer.getCursorPosition());
    }

    @Test
    void shouldAccessCharactersAndAttributesFromGlobalCoordinates() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 5);

        buffer.setCurrentAttributes(
                TerminalColor.BLUE,
                TerminalColor.DEFAULT,
                EnumSet.of(TextStyle.ITALIC)
        );
        buffer.writeText("hello");

        assertEquals('h', buffer.getCharacterAt(0, 0));
        assertEquals('o', buffer.getCharacterAt(4, 0));
        assertEquals(TerminalColor.BLUE, buffer.getAttributesAt(0, 0).getForeground());
        assertTrue(buffer.getAttributesAt(0, 0).hasStyle(TextStyle.ITALIC));
    }

    @Test
    void shouldThrowForInvalidGlobalRowAccess() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 5);

        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getLineAsString(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getLineAsString(99));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getCharacterAt(0, 99));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getAttributesAt(0, 99));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getTextAt(0, 99));
    }

    @Test
    void shouldThrowForInvalidColumnAccess() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 5);

        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getCharacterAt(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getCharacterAt(99, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getAttributesAt(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getTextAt(99, 0));
    }

    @Test
    void shouldWriteWideCharacters() {
        TerminalBuffer buffer = new TerminalBuffer(6, 2, 10);

        buffer.writeText("A界B");

        assertEquals("A界B", buffer.getLineAsString(0));
    }

    @Test
    void shouldNormalizeCursorAroundWideCharacters() {
        TerminalBuffer buffer = new TerminalBuffer(6, 2, 10);

        buffer.writeText("界");
        buffer.setCursorPosition(1, 0);

        assertEquals(new CursorPosition(0, 0), buffer.getCursorPosition());
    }

    @Test
    void shouldWrapWideCharacterWhenAtLastColumn() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);

        buffer.writeText("abc界");

        assertEquals("abc", buffer.getLineAsString(0));
        assertEquals("界", buffer.getLineAsString(1));
    }

    @Test
    void shouldOverwriteWideCharacterWhenWritingInsideItsOccupiedCells() {
        TerminalBuffer buffer = new TerminalBuffer(6, 2, 10);

        buffer.writeText("A界B");
        buffer.setCursorPosition(1, 0);
        buffer.writeText("Z");

        assertEquals("AZ B", buffer.getLineAsString(0));
    }

    @Test
    void shouldKeepAttributesForWideCharacters() {
        TerminalBuffer buffer = new TerminalBuffer(6, 2, 10);
        buffer.setCurrentAttributes(
                TerminalColor.RED,
                TerminalColor.BLACK,
                EnumSet.of(TextStyle.BOLD)
        );

        buffer.writeText("界");

        assertEquals(TerminalColor.RED, buffer.getAttributesAt(0, 0).getForeground());
        assertEquals(TerminalColor.BLACK, buffer.getAttributesAt(1, 0).getBackground());
        assertTrue(buffer.getAttributesAt(0, 0).hasStyle(TextStyle.BOLD));
    }

    @Test
    void shouldResizeAndReflowContentToNarrowerWidth() {
        TerminalBuffer buffer = new TerminalBuffer(6, 3, 10);

        buffer.writeText("abcdefgh");
        buffer.resize(4, 3);

        assertEquals("abcd\nef\ngh\n", buffer.getAllContentAsString());
    }

    @Test
    void shouldResizeToWiderWidthAndPreserveContent() {
        TerminalBuffer buffer = new TerminalBuffer(4, 3, 10);

        buffer.writeText("abcd");
        buffer.writeText("efgh");

        buffer.resize(8, 3);

        assertEquals("abcd\nefgh\n", buffer.getScreenContentAsString());
    }

    @Test
    void shouldPreserveBottomMostLinesWhenReducingHeight() {
        TerminalBuffer buffer = new TerminalBuffer(4, 4, 10);

        buffer.writeText("1111\n2222\n3333\n4444");
        buffer.resize(4, 2);

        assertEquals(2, buffer.getHeight());
        assertTrue(buffer.getScreenContentAsString().contains("3333"));
        assertTrue(buffer.getScreenContentAsString().contains("4444"));
    }

    @Test
    void shouldAddBlankLinesWhenIncreasingHeight() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);

        buffer.writeText("abcd");
        buffer.resize(4, 4);

        assertEquals(4, buffer.getHeight());
        assertEquals(4, buffer.getTotalLineCount());
    }

    @Test
    void shouldTrimScrollbackAfterResizeIfNeeded() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 2);

        buffer.writeText("1111\n2222\n3333\n4444\n5555");
        buffer.resize(2, 2);

        assertTrue(buffer.getScrollbackLineCount() <= 2);
    }
}