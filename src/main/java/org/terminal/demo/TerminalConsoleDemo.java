package org.terminal.demo;

import org.terminal.model.TerminalBuffer;
import org.terminal.model.TerminalColor;
import org.terminal.model.TextStyle;

import java.util.EnumSet;

/**
 * Provides a small console demo for the terminal buffer.
 */
public final class TerminalConsoleDemo {

    private TerminalConsoleDemo() {
    }

    /**
     * Runs a simple manual demo.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        TerminalBuffer buffer = new TerminalBuffer(8, 4, 10);

        buffer.writeText("Hello");
        buffer.setCursorPosition(0, 1);
        buffer.setCurrentAttributes(
                TerminalColor.GREEN,
                TerminalColor.DEFAULT,
                EnumSet.of(TextStyle.BOLD)
        );
        buffer.writeText("世界");
        buffer.setCursorPosition(0, 2);
        buffer.insertText("🙂");
        buffer.setCursorPosition(0, 3);
        buffer.writeText("resize");
        buffer.resize(6, 4);

        System.out.println(buffer.getAllContentAsString());
    }
}