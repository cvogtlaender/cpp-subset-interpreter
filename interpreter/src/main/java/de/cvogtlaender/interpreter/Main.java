package de.cvogtlaender.interpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import de.cvogtlaender.interpreter.diagnostic.Diagnostic;
import de.cvogtlaender.interpreter.repl.Repl;

public class Main {

  private static final String USAGE = """
      usage: minicpp <file.cpp>            run a program
             minicpp run <file.cpp>        run a program
             minicpp check <file.cpp>      report errors without running
             minicpp ast <file.cpp>        print the AST
             minicpp to-cpp <file.cpp>     translate to standard C++ (for comparing with g++)
             minicpp repl [file.cpp]       interactive session (optionally loading a file first)""";

  public static void main(String[] args) {
    PrintStream out = new PrintStream(System.out, false, StandardCharsets.UTF_8);
    System.exit(run(args, out, System.err));
  }

  static int run(String[] args, PrintStream out, PrintStream err) {
    if (args.length == 0) {
      return repl(null, out, err);
    }

    String command = args[0];
    try {
      switch (command) {
        case "run" -> {
          return args.length == 2 ? runFile(args[1], out, err) : usage(err);
        }
        case "check" -> {
          return args.length == 2 ? check(args[1], out, err) : usage(err);
        }
        case "ast" -> {
          return args.length == 2 ? ast(args[1], out, err) : usage(err);
        }
        case "repl" -> {
          return args.length <= 2 ? repl(args.length == 2 ? args[1] : null, out, err) : usage(err);
        }
        case "-h", "--help", "help" -> {
          out.println(USAGE);
          return 0;
        }
        default -> {
          return args.length == 1 ? runFile(command, out, err) : usage(err);
        }
      }
    } catch (IOException e) {
      err.println("error: cannot read file: " + e.getMessage());
      return 2;
    }
  }

  private static int usage(PrintStream err) {
    err.println(USAGE);
    return 2;
  }

  private static int runFile(String file, PrintStream out, PrintStream err) throws IOException {
    MiniCpp.RunResult result = MiniCpp.run(Files.readString(Path.of(file)), out);
    out.flush();
    report(file, result.diagnostics(), err);
    return result.exitCode();
  }

  private static int check(String file, PrintStream out, PrintStream err) throws IOException {
    MiniCpp.Compilation compilation = MiniCpp.compile(Files.readString(Path.of(file)));
    report(file, compilation.diagnostics(), err);
    if (!compilation.hasErrors()) {
      out.println(file + ": no errors");
    }
    return compilation.hasErrors() ? 1 : 0;
  }

  private static int ast(String file, PrintStream out, PrintStream err) throws IOException {
    MiniCpp.ParseResult<?> parsed = MiniCpp.parseProgram(Files.readString(Path.of(file)));
    report(file, parsed.diagnostics(), err);
    if (parsed.hasErrors()) {
      return 1;
    }
    out.println(((de.cvogtlaender.interpreter.ast.Program) parsed.tree()).toStringTree());
    return 0;
  }

  private static int repl(String file, PrintStream out, PrintStream err) {
    Repl repl = new Repl(out, err);
    try {
      if (file != null) {
        repl.load(Files.readString(Path.of(file)));
      }
      repl.run(new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)));
      return 0;
    } catch (IOException e) {
      err.println("error: " + e.getMessage());
      return 2;
    }
  }

  private static void report(String file, Iterable<Diagnostic> diagnostics, PrintStream err) {
    for (Diagnostic d : diagnostics) {
      err.println(file + ":" + d.format());
    }
  }
}
