package org.terminal.model;

/**
 * Provides helper methods to estimate terminal display width of Unicode code points.
 */
public final class DisplayWidthUtils {

    private DisplayWidthUtils() {
    }

    /**
     * Returns the display width of a code point in terminal cells.
     *
     * @param codePoint the Unicode code point
     * @return 1 for narrow characters and 2 for wide characters
     */
    public static int widthOf(int codePoint) {
        return isWide(codePoint) ? 2 : 1;
    }

    /**
     * Returns whether a code point should be treated as a wide terminal character.
     *
     * @param codePoint the Unicode code point
     * @return true if the code point is considered wide
     */
    public static boolean isWide(int codePoint) {
        Character.UnicodeScript script = Character.UnicodeScript.of(codePoint);

        if (script == Character.UnicodeScript.HAN
                || script == Character.UnicodeScript.HIRAGANA
                || script == Character.UnicodeScript.KATAKANA
                || script == Character.UnicodeScript.HANGUL) {
            return true;
        }

        return isInRange(codePoint, 0x1100, 0x115F)
                || isInRange(codePoint, 0x2329, 0x232A)
                || isInRange(codePoint, 0x2E80, 0xA4CF)
                || isInRange(codePoint, 0xAC00, 0xD7A3)
                || isInRange(codePoint, 0xF900, 0xFAFF)
                || isInRange(codePoint, 0xFE10, 0xFE19)
                || isInRange(codePoint, 0xFE30, 0xFE6F)
                || isInRange(codePoint, 0xFF00, 0xFF60)
                || isInRange(codePoint, 0xFFE0, 0xFFE6)
                || isInRange(codePoint, 0x1F300, 0x1FAFF);
    }

    /**
     * Returns the string representation of a single code point.
     *
     * @param codePoint the Unicode code point
     * @return the string form of the code point
     */
    public static String stringOf(int codePoint) {
        return new String(Character.toChars(codePoint));
    }

    private static boolean isInRange(int value, int start, int end) {
        return value >= start && value <= end;
    }
}