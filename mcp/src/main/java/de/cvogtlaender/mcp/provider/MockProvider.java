package de.cvogtlaender.mcp.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Deterministic provider for tests and offline demos. By default it answers
 * with a fixed text per task; tests can install their own responder. All
 * requests are recorded.
 */
public class MockProvider implements GenAiProvider {

  public record Request(String system, String prompt) {
  }

  private final List<Request> requests = new ArrayList<>();
  private BiFunction<String, String, String> responder = MockProvider::defaultResponse;

  @Override
  public String name() {
    return "mock";
  }

  @Override
  public String model() {
    return "mock-model";
  }

  @Override
  public synchronized String generate(String system, String prompt) {
    requests.add(new Request(system, prompt));
    return responder.apply(system, prompt);
  }

  @Override
  public boolean isAvailable() {
    return true;
  }

  public synchronized void respondWith(BiFunction<String, String, String> responder) {
    this.responder = responder;
  }

  public synchronized List<Request> requests() {
    return List.copyOf(requests);
  }

  private static String defaultResponse(String system, String prompt) {
    if (system.contains("code completion")) {
      return "return 0;";
    }
    if (system.contains("Refactor")) {
      return "```cpp\nint main() {\n  return 0;\n}\n```\nRationale: simplified the program.";
    }
    if (system.contains("bugs")) {
      return "NONE";
    }
    return "This code defines a MiniC++ program.";
  }
}
