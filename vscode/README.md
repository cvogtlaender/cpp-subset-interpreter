# MiniC++ for VS Code

Language support for MiniC++, the C++ subset of the
[cpp-subset-interpreter](https://github.com/cvogtlaender/cpp-subset-interpreter) project.

- Diagnostics of all phases (syntax, names, types) while typing
- Hover with declarations and expression types
- Completion of variables, members (also after `.` and `->`), functions, classes and keywords
- Go to definition, find references, highlights, rename
- Formatting and quick fixes
- Outline and syntax highlighting

MiniC++ files use the extension `.mcpp`. To treat other files as MiniC++, add for example
`"files.associations": { "*.cpp": "minicpp" }` to the workspace settings.

## Requirements

Java 21 or newer. The extension uses `minicpp.java.path`, then `JAVA_HOME`, then `java` on the `PATH`.

## Settings

| Setting | Meaning |
|---|---|
| `minicpp.java.path` | Java executable for the bundled server |
| `minicpp.server.path` | a different `minicpp-lsp` start script instead of the bundled server |
| `minicpp.trace.server` | log the JSON-RPC messages (`off`, `messages`, `verbose`) |
