package de.cvogtlaender.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Runs every {@code programs/*.cpp} and compares its output with the
 * {@code .expected} file next to it. A line {@code // expect-exit: N} sets the
 * expected exit code (default 0).
 */
class ProgramTest {

  private static final Pattern EXPECT_EXIT = Pattern.compile("//\\s*expect-exit:\\s*(-?\\d+)");

  static Stream<Path> programs() throws IOException, URISyntaxException {
    Path dir = Path.of(ProgramTest.class.getResource("/programs").toURI());
    return Files.list(dir).filter(p -> p.toString().endsWith(".cpp")).sorted();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("programs")
  void producesExpectedOutput(Path program) throws IOException {
    String source = Files.readString(program);
    String expected = Files.readString(Path.of(program.toString().replaceAll("\\.cpp$", ".expected")));

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
    MiniCpp.RunResult result = MiniCpp.run(source, out);

    assertTrue(result.diagnostics().isEmpty(), () -> "unexpected errors: " + result.diagnostics());
    assertEquals(normalize(expected), normalize(buffer.toString(StandardCharsets.UTF_8)));

    Matcher m = EXPECT_EXIT.matcher(source);
    int expectedExit = m.find() ? Integer.parseInt(m.group(1)) : 0;
    assertEquals(expectedExit, result.exitCode());
  }

  static String normalize(String s) {
    return s.replace("\r\n", "\n");
  }
}
