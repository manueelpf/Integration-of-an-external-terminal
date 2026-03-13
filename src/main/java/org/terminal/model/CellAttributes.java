package org.terminal.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Stores visual attributes for a terminal cell.
 */
public final class CellAttributes {
    private final TerminalColor foreground;
    private final TerminalColor background;
    private final EnumSet<TextStyle> styles;

    /**
     * Creates attributes with the given foreground, background and styles.
     *
     * @param foreground the foreground color
     * @param background the background color
     * @param styles the style set
     */
    public CellAttributes(TerminalColor foreground, TerminalColor background, Set<TextStyle> styles) {
        this.foreground = Objects.requireNonNull(foreground, "foreground must not be null");
        this.background = Objects.requireNonNull(background, "background must not be null");
        this.styles = styles == null || styles.isEmpty()
                ? EnumSet.noneOf(TextStyle.class)
                : EnumSet.copyOf(styles);
    }

    /**
     * Returns default terminal attributes.
     *
     * @return default attributes
     */
    public static CellAttributes defaults() {
        return new CellAttributes(TerminalColor.DEFAULT, TerminalColor.DEFAULT, EnumSet.noneOf(TextStyle.class));
    }

    /**
     * Returns the foreground color.
     *
     * @return the foreground color
     */
    public TerminalColor getForeground() {
        return foreground;
    }

    /**
     * Returns the background color.
     *
     * @return the background color
     */
    public TerminalColor getBackground() {
        return background;
    }

    /**
     * Returns an immutable copy of the style set.
     *
     * @return the styles
     */
    public Set<TextStyle> getStyles() {
        return Collections.unmodifiableSet(EnumSet.copyOf(styles));
    }

    /**
     * Returns whether a given style is enabled.
     *
     * @param style the style to check
     * @return true if enabled
     */
    public boolean hasStyle(TextStyle style) {
        return styles.contains(style);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CellAttributes that)) {
            return false;
        }
        return foreground == that.foreground
                && background == that.background
                && Objects.equals(styles, that.styles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreground, background, styles);
    }

    @Override
    public String toString() {
        return "CellAttributes{" +
                "foreground=" + foreground +
                ", background=" + background +
                ", styles=" + styles +
                '}';
    }
}