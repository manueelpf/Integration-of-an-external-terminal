package org.terminal.model;

import java.util.Objects;

/**
 * Represents a single cell in the terminal grid.
 */
public final class Cell {
    public static final String EMPTY_TEXT = " ";

    private String text;
    private CellAttributes attributes;
    private boolean continuation;

    /**
     * Creates an empty cell with default attributes.
     */
    public Cell() {
        this(EMPTY_TEXT, CellAttributes.defaults(), false);
    }

    /**
     * Creates a cell with the given text, attributes and continuation state.
     *
     * @param text the cell text
     * @param attributes the cell attributes
     * @param continuation whether this cell continues a wide character
     */
    public Cell(String text, CellAttributes attributes, boolean continuation) {
        this.text = Objects.requireNonNull(text, "text must not be null");
        this.attributes = Objects.requireNonNull(attributes, "attributes must not be null");
        this.continuation = continuation;
    }

    /**
     * Returns the cell text.
     *
     * @return the cell text
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the first character of the cell text for compatibility with simple tests.
     *
     * @return the first character or a space if empty
     */
    public char getCharacter() {
        return text.isEmpty() ? ' ' : text.charAt(0);
    }

    /**
     * Sets the cell text.
     *
     * @param text the new text
     */
    public void setText(String text) {
        this.text = Objects.requireNonNull(text, "text must not be null");
    }

    /**
     * Returns the cell attributes.
     *
     * @return the attributes
     */
    public CellAttributes getAttributes() {
        return attributes;
    }

    /**
     * Sets the cell attributes.
     *
     * @param attributes the new attributes
     */
    public void setAttributes(CellAttributes attributes) {
        this.attributes = Objects.requireNonNull(attributes, "attributes must not be null");
    }

    /**
     * Returns whether this cell is a continuation of a wide character.
     *
     * @return true if this is a continuation cell
     */
    public boolean isContinuation() {
        return continuation;
    }

    /**
     * Sets whether this cell is a continuation of a wide character.
     *
     * @param continuation the continuation flag
     */
    public void setContinuation(boolean continuation) {
        this.continuation = continuation;
    }

    /**
     * Updates the full content of the cell.
     *
     * @param text the new text
     * @param attributes the new attributes
     * @param continuation the continuation flag
     */
    public void set(String text, CellAttributes attributes, boolean continuation) {
        this.text = Objects.requireNonNull(text, "text must not be null");
        this.attributes = Objects.requireNonNull(attributes, "attributes must not be null");
        this.continuation = continuation;
    }

    /**
     * Clears the cell to an empty character with default attributes.
     */
    public void clear() {
        this.text = EMPTY_TEXT;
        this.attributes = CellAttributes.defaults();
        this.continuation = false;
    }

    /**
     * Returns whether the cell is visually empty.
     *
     * @return true if the cell is an empty non-continuation cell
     */
    public boolean isEmpty() {
        return !continuation && EMPTY_TEXT.equals(text);
    }

    /**
     * Creates a copy of this cell.
     *
     * @return a copied cell
     */
    public Cell copy() {
        return new Cell(text, attributes, continuation);
    }
}