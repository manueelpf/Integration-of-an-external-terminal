package org.terminal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one fixed-width line in the terminal buffer.
 */
public final class TerminalLine {
    private final List<Cell> cells;

    /**
     * Creates an empty line with the given width.
     *
     * @param width the line width
     */
    public TerminalLine(int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than zero");
        }
        this.cells = new ArrayList<>(width);
        for (int i = 0; i < width; i++) {
            cells.add(new Cell());
        }
    }

    /**
     * Returns the width of the line.
     *
     * @return the width
     */
    public int width() {
        return cells.size();
    }

    /**
     * Returns the cell at the given column.
     *
     * @param column the column index
     * @return the cell
     */
    public Cell getCell(int column) {
        checkColumn(column);
        return cells.get(column);
    }

    /**
     * Sets the cell at the given column.
     *
     * @param column the column index
     * @param text the cell text
     * @param attributes the cell attributes
     * @param continuation whether the cell continues a wide character
     */
    public void setCell(int column, String text, CellAttributes attributes, boolean continuation) {
        checkColumn(column);
        cells.get(column).set(text, attributes, continuation);
    }

    /**
     * Clears the cell at the given column.
     *
     * @param column the column index
     */
    public void clearCell(int column) {
        checkColumn(column);
        cells.get(column).clear();
    }

    /**
     * Fills the line with the given character and attributes.
     *
     * @param character the fill character
     * @param attributes the fill attributes
     */
    public void fill(char character, CellAttributes attributes) {
        String text = String.valueOf(character);
        for (Cell cell : cells) {
            cell.set(text, attributes, false);
        }
    }

    /**
     * Clears the line to empty cells.
     */
    public void clear() {
        for (Cell cell : cells) {
            cell.clear();
        }
    }

    /**
     * Returns the line content as a raw fixed-width string.
     *
     * @return the raw line string
     */
    public String toRawString() {
        StringBuilder builder = new StringBuilder();
        for (Cell cell : cells) {
            if (!cell.isContinuation()) {
                builder.append(cell.getText());
            }
        }
        return builder.toString();
    }

    /**
     * Returns the line content with trailing spaces removed.
     *
     * @return the trimmed line string
     */
    public String toTrimmedString() {
        String raw = toRawString();
        int end = raw.length();
        while (end > 0 && raw.charAt(end - 1) == ' ') {
            end--;
        }
        return raw.substring(0, end);
    }

    /**
     * Creates a copy of the line.
     *
     * @return a copied line
     */
    public TerminalLine copy() {
        TerminalLine copy = new TerminalLine(width());
        for (int i = 0; i < width(); i++) {
            Cell source = this.getCell(i);
            copy.setCell(i, source.getText(), source.getAttributes(), source.isContinuation());
        }
        return copy;
    }

    private void checkColumn(int column) {
        if (column < 0 || column >= cells.size()) {
            throw new IndexOutOfBoundsException("column out of bounds: " + column);
        }
    }
}