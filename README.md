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

---

## Running the Tests

The project uses **Maven** as the build tool and **JUnit 5** for testing.

To compile the project and run all tests:

```bash
    mvn run test