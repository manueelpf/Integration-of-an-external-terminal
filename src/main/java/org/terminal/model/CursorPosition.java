package org.terminal.model;

/**
 * Represents a cursor position on the visible screen.
 *
 * @param column the zero-based column
 * @param row the zero-based row
 */
public record CursorPosition(int column, int row) {
}