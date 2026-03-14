# Terminal Buffer

A Java implementation of a terminal text buffer designed to model the core behavior of a terminal emulator screen.

The project implements the fundamental data structures and operations required for storing, editing, and retrieving terminal content while supporting features such as scrollback history, styled characters, wide-character rendering, and screen resizing.

---

## Features

The terminal buffer supports the following functionality:

- Configurable **screen width and height**
- Configurable **scrollback history size**
- **Cursor management** with bounds checking
- **Overwrite and insert text operations**
- **Character attributes**
    - foreground color
    - background color
    - style flags (bold, italic, underline)
- **Screen and scrollback content access**
- **Wide character support** for double-width terminal glyphs (e.g., CJK characters and emoji-like symbols)
- **Screen resizing with content reflow**
- **Unit tests using JUnit 5**

---
## Project Structure

```text
src/main/java/org/terminal/
├─ demo/
│  └─ TerminalConsoleDemo.java
└─ model/
   ├─ Cell.java
   ├─ CellAttributes.java
   ├─ CursorPosition.java
   ├─ DisplayWidthUtils.java
   ├─ TerminalBuffer.java
   ├─ TerminalColor.java
   ├─ TerminalLine.java
   └─ TextStyle.java

src/test/java/org/example/
└─ TerminalBufferTest.java
```


### Core Components

**TerminalBuffer**

The main class responsible for:

- managing the visible screen
- managing scrollback history
- tracking the cursor position
- applying editing operations
- handling resizing and wrapping behavior

**TerminalLine**

Represents a fixed-width row in the terminal screen.

**Cell**

Represents a single terminal cell storing:

- text content
- attributes
- continuation state (for wide characters)

**CellAttributes**

Encapsulates character styling information.

**DisplayWidthUtils**

Utility class used to determine the display width of Unicode characters.

---

## Wide Character Handling

Some Unicode characters occupy **two terminal cells** instead of one.

Examples include:

- CJK ideographs
- certain emoji-like characters

This implementation supports wide characters by:

- storing the glyph in the first cell
- marking the second cell as a **continuation cell**

This allows cursor movement, insertion, and wrapping logic to behave correctly for double-width characters.

---

## Resize Behavior

The terminal buffer supports dynamic resizing.

When the screen is resized:

- existing content is **reflowed to the new width**
- the **bottom-most lines remain visible**
- older lines are preserved in **scrollback**

This strategy preserves as much content as possible while maintaining deterministic behavior.

## Behavioral Decisions

This implementation makes the following explicit behavioral choices:

### Deferred wrapping
Writing uses deferred wrapping semantics. When a character is written at the last available column, the cursor remains on that line and a wrap is performed only before the next printable character is written. This more closely matches common terminal behavior than wrapping immediately after the last cell is filled.

### Insert semantics
`insertText` inserts glyphs at the current cursor position by shifting the existing line content to the right. If the line overflows, the excess content is propagated to the following lines. If overflow reaches the bottom of the screen, the screen scrolls upward and the top line is moved into scrollback, subject to the configured scrollback limit.

### Wide-character representation
Wide characters are represented using two terminal cells. The first cell stores the glyph, and the second cell is marked as a continuation cell. Cursor normalization prevents the cursor from being placed on the continuation half of a wide glyph.

### Resize strategy
Resizing uses a reflow-based strategy. Existing visible and scrollback content is re-packed according to the new width, and the bottom-most lines remain on screen after resize. Older content is preserved in scrollback as long as the configured scrollback capacity allows it.

### Scrollback trimming policy
Scrollback is bounded by a fixed maximum size. When the number of scrollback lines exceeds this limit, the oldest lines are discarded first.

---

## Running the Tests

The project uses **Maven** as the build tool and **JUnit 5** for testing.

To compile the project and run all tests:

```bash
    mvn test
```
---

## Known Limitations

This project focuses on the terminal buffer data structure itself and does not attempt to implement a full terminal emulator.

Some important limitations are:

- **Unicode width heuristics are approximate.** Display width is estimated using Unicode ranges and scripts, which is sufficient for this task but not equivalent to a full terminal-width implementation.
- **Terminal control semantics are intentionally limited.** The project models buffer storage, editing, wrapping, scrollback, and resizing, but it does not implement escape-sequence parsing, alternate screen behavior, or full ANSI terminal semantics.