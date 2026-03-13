# Terminal Buffer

A Java implementation of a terminal text buffer with:

- configurable screen width and height
- configurable scrollback size
- cursor movement with bounds checking
- overwrite and insert text operations
- character attributes (foreground, background, styles)
- screen and scrollback content access
- wide character support for double-width terminal glyphs
- screen resize with content reflow
- unit tests with JUnit 5

## Project structure

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