package org.terminal.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 * Stores and manipulates terminal screen and scrollback content.
 */
public final class TerminalBuffer {
    private int width;
    private int height;
    private final int scrollbackMaxSize;

    private final List<TerminalLine> screen;
    private final List<TerminalLine> scrollback;

    private int cursorColumn;
    private int cursorRow;
    private boolean pendingWrap;
    private CellAttributes currentAttributes;

    /**
     * Creates a terminal buffer with the given dimensions and scrollback capacity.
     *
     * @param width the visible screen width
     * @param height the visible screen height
     * @param scrollbackMaxSize the maximum number of scrollback lines
     */
    public TerminalBuffer(int width, int height, int scrollbackMaxSize) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than zero");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be greater than zero");
        }
        if (scrollbackMaxSize < 0) {
            throw new IllegalArgumentException("scrollbackMaxSize must not be negative");
        }

        this.width = width;
        this.height = height;
        this.scrollbackMaxSize = scrollbackMaxSize;
        this.screen = new ArrayList<>(height);
        this.scrollback = new ArrayList<>();
        this.currentAttributes = CellAttributes.defaults();
        this.cursorColumn = 0;
        this.cursorRow = 0;
        this.pendingWrap = false;

        for (int i = 0; i < height; i++) {
            screen.add(blankLine());
        }
    }

    /**
     * Returns the screen width.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the screen height.
     *
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the maximum scrollback size.
     *
     * @return the maximum scrollback size
     */
    public int getScrollbackMaxSize() {
        return scrollbackMaxSize;
    }

    /**
     * Returns the current cursor position on the screen.
     *
     * @return the cursor position
     */
    public CursorPosition getCursorPosition() {
        return new CursorPosition(cursorColumn, cursorRow);
    }

    /**
     * Sets the cursor position, clamped to screen bounds and normalized away from continuation cells.
     *
     * @param column the target column
     * @param row the target row
     */
    public void setCursorPosition(int column, int row) {
        this.cursorColumn = clamp(column, 0, width - 1);
        this.cursorRow = clamp(row, 0, height - 1);
        normalizeCursor();
        this.pendingWrap = false;
    }

    /**
     * Moves the cursor up by the given number of cells.
     *
     * @param amount the number of rows to move
     */
    public void moveCursorUp(int amount) {
        moveCursorBy(0, -Math.max(amount, 0));
    }

    /**
     * Moves the cursor down by the given number of cells.
     *
     * @param amount the number of rows to move
     */
    public void moveCursorDown(int amount) {
        moveCursorBy(0, Math.max(amount, 0));
    }

    /**
     * Moves the cursor left by the given number of cells.
     *
     * @param amount the number of columns to move
     */
    public void moveCursorLeft(int amount) {
        moveCursorBy(-Math.max(amount, 0), 0);
    }

    /**
     * Moves the cursor right by the given number of cells.
     *
     * @param amount the number of columns to move
     */
    public void moveCursorRight(int amount) {
        moveCursorBy(Math.max(amount, 0), 0);
    }

    private void moveCursorBy(int columnDelta, int rowDelta) {
        cursorColumn = clamp(cursorColumn + columnDelta, 0, width - 1);
        cursorRow = clamp(cursorRow + rowDelta, 0, height - 1);
        normalizeCursor();
        pendingWrap = false;
    }

    private void normalizeCursor() {
        TerminalLine line = screen.get(cursorRow);
        while (cursorColumn > 0 && line.getCell(cursorColumn).isContinuation()) {
            cursorColumn--;
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private TerminalLine blankLine() {
        return new TerminalLine(width);
    }

    /**
     * Sets the current attributes used for subsequent edits.
     *
     * @param foreground the foreground color
     * @param background the background color
     * @param styles the style set
     */
    public void setCurrentAttributes(TerminalColor foreground, TerminalColor background, EnumSet<TextStyle> styles) {
        this.currentAttributes = new CellAttributes(
                Objects.requireNonNull(foreground, "foreground must not be null"),
                Objects.requireNonNull(background, "background must not be null"),
                styles == null ? EnumSet.noneOf(TextStyle.class) : EnumSet.copyOf(styles)
        );
    }

    /**
     * Returns the current edit attributes.
     *
     * @return the current attributes
     */
    public CellAttributes getCurrentAttributes() {
        return currentAttributes;
    }

    /**
     * Writes text starting at the cursor, overwriting existing cells.
     * Newline characters move the cursor to the next line.
     *
     * @param text the text to write
     */
    public void writeText(String text) {
        Objects.requireNonNull(text, "text must not be null");

        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);

            if (codePoint == '\n') {
                newLine();
                continue;
            }

            ensureWrapBeforePrintable();

            int glyphWidth = DisplayWidthUtils.widthOf(codePoint);
            if (glyphWidth > width) {
                continue;
            }
            if (glyphWidth == 2 && cursorColumn == width - 1) {
                newLine();
            }

            //writeGlyphOverwrite(cursorRow, cursorColumn, codePoint, currentAttributes);
            advanceAfterWrite(glyphWidth);
        }
    }

    /**
     * Inserts text at the cursor, shifting existing content to the right and wrapping overflow.
     * Newline characters move the cursor to the next line.
     *
     * @param text the text to insert
     */
    public void insertText(String text) {
        Objects.requireNonNull(text, "text must not be null");

        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);

            if (codePoint == '\n') {
                newLine();
                continue;
            }

            ensureWrapBeforePrintable();

            int glyphWidth = DisplayWidthUtils.widthOf(codePoint);
            if (glyphWidth > width) {
                continue;
            }
            if (glyphWidth == 2 && cursorColumn == width - 1) {
                newLine();
            }

            //insertGlyph(cursorRow, cursorColumn, new Glyph(DisplayWidthUtils.stringOf(codePoint), glyphWidth, currentAttributes));
            advanceAfterWrite(glyphWidth);
        }
    }

    /**
     * Fills the current screen line using the current attributes.
     * A null character means clearing the line to empty cells.
     *
     * @param character the fill character or null for empty
     */
    public void fillCurrentLine(Character character) {
        TerminalLine line = screen.get(cursorRow);
        if (character == null) {
            line.clear();
        } else {
            line.fill(character, currentAttributes);
        }
        pendingWrap = false;
    }

    /**
     * Inserts an empty line at the bottom of the screen, pushing the top line into scrollback.
     */
    public void insertEmptyLineAtBottom() {
        pushTopLineToScrollback();
        screen.remove(0);
        screen.add(blankLine());
        cursorRow = Math.min(cursorRow, height - 1);
        normalizeCursor();
        pendingWrap = false;
    }

    private void advanceAfterWrite(int glyphWidth) {
        if (cursorColumn + glyphWidth >= width) {
            cursorColumn = width - 1;
            pendingWrap = true;
        } else {
            cursorColumn += glyphWidth;
        }
        normalizeCursor();
    }

    private void newLine() {
        pendingWrap = false;
        cursorColumn = 0;
        lineFeed();
        normalizeCursor();
    }
    private void lineFeed() {
        if (cursorRow < height - 1) {
            cursorRow++;
        } else {
            scrollUp();
        }
    }

    private void scrollUp() {
        pushTopLineToScrollback();
        screen.remove(0);
        screen.add(blankLine());
        cursorRow = height - 1;
    }

    private void pushTopLineToScrollback() {
        if (scrollbackMaxSize == 0) {
            return;
        }
        scrollback.add(screen.get(0).copy());
        trimScrollback();
    }

    /**
     * Returns the first character at the given global position.
     *
     * @param column the zero-based column
     * @param globalRow the zero-based global row
     * @return the stored character
     */
    public char getCharacterAt(int column, int globalRow) {
        return getLineByGlobalRow(globalRow).getCell(column).getCharacter();
    }

    /**
     * Returns the text stored at the given global position.
     *
     * @param column the zero-based column
     * @param globalRow the zero-based global row
     * @return the stored text
     */
    public String getTextAt(int column, int globalRow) {
        return getLineByGlobalRow(globalRow).getCell(column).getText();
    }

    /**
     * Returns the attributes at the given global position.
     *
     * @param column the zero-based column
     * @param globalRow the zero-based global row
     * @return the cell attributes
     */
    public CellAttributes getAttributesAt(int column, int globalRow) {
        return getLineByGlobalRow(globalRow).getCell(column).getAttributes();
    }

    /**
     * Returns the line content at the given global row as a string with trailing spaces removed.
     *
     * @param globalRow the zero-based global row
     * @return the line string
     */
    public String getLineAsString(int globalRow) {
        return getLineByGlobalRow(globalRow).toTrimmedString();
    }

    /**
     * Returns the visible screen content as a multi-line string.
     *
     * @return the visible screen content
     */
    public String getScreenContentAsString() {
        return joinLines(screen);
    }

    /**
     * Returns the full content including scrollback as a multi-line string.
     *
     * @return the complete content
     */
    public String getAllContentAsString() {
        List<TerminalLine> allLines = new ArrayList<>(scrollback.size() + screen.size());
        allLines.addAll(scrollback);
        allLines.addAll(screen);
        return joinLines(allLines);
    }

    private void trimScrollback() {
        while (scrollback.size() > scrollbackMaxSize) {
            scrollback.remove(0);
        }
    }

    private TerminalLine getLineByGlobalRow(int globalRow) {
        if (globalRow < 0 || globalRow >= getTotalLineCount()) {
            throw new IndexOutOfBoundsException("global row out of bounds: " + globalRow);
        }
        if (globalRow < scrollback.size()) {
            return scrollback.get(globalRow);
        }
        return screen.get(globalRow - scrollback.size());
    }

    private String joinLines(List<TerminalLine> lines) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(lines.get(i).toTrimmedString());
        }
        return builder.toString();
    }


    /**
     * Returns the number of scrollback lines currently stored.
     *
     * @return the scrollback line count
     */
    public int getScrollbackLineCount() {
        return scrollback.size();
    }

    /**
     * Returns the total number of addressable lines including scrollback and screen.
     *
     * @return the total line count
     */
    public int getTotalLineCount() {
        return scrollback.size() + screen.size();
    }

    private void ensureWrapBeforePrintable() {
        if (!pendingWrap) {
            return;
        }
        cursorColumn = 0;
        lineFeed();
        pendingWrap = false;
    }
    /**
     * Clears the visible screen and resets the cursor.
     * Scrollback is preserved.
     */
    public void clearScreen() {
        screen.clear();
        for (int i = 0; i < height; i++) {
            screen.add(blankLine());
        }
        cursorColumn = 0;
        cursorRow = 0;
        pendingWrap = false;
    }

    /**
     * Clears the visible screen, scrollback and resets the cursor.
     */
    public void clearScreenAndScrollback() {
        scrollback.clear();
        clearScreen();
    }
}