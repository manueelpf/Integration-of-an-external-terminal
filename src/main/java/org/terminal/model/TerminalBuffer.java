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


}