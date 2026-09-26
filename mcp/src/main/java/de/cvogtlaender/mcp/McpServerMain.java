package de.cvogtlaender.mcp;

import java.io.IOException;
import java.time.Duration;

import de.cvogtlaender.mcp.provider.GenAiProvider;
import de.cvogtlaender.mcp.provider.MockProvider;
import de.cvogtlaender.mcp.provider.OllamaProvider;

/**
 * Starts the GenAI server. Options (each also settable via environment
 * variable):
 *
 * <pre>
 * --host HOST        MCP_HOST        interface to bind (default 127.0.0.1)
 * --port PORT        MCP_PORT        port (default 8080)
 * --provider NAME    MCP_PROVIDER    ollama | mock (default ollama)
 * --ollama-url URL   OLLAMA_URL      (default http://localhost:11434)
 * --model MODEL      OLLAMA_MODEL    (default codellama)
 * --timeout SECONDS  OLLAMA_TIMEOUT  generation timeout (default 120)
 * </pre>
 */
public class McpServerMain {

  public static void main(String[] args) throws IOException {
    String host = option(args, "--host", "MCP_HOST", "127.0.0.1");
    int port = Integer.parseInt(option(args, "--port", "MCP_PORT", "8080"));
    String providerName = option(args, "--provider", "MCP_PROVIDER", "ollama");

    GenAiProvider provider = switch (providerName) {
      case "ollama" -> new OllamaProvider(
          option(args, "--ollama-url", "OLLAMA_URL", "http://localhost:11434"),
          option(args, "--model", "OLLAMA_MODEL", "codellama"),
          Duration.ofSeconds(Long.parseLong(option(args, "--timeout", "OLLAMA_TIMEOUT", "120"))));
      case "mock" -> new MockProvider();
      default -> throw new IllegalArgumentException("unknown provider '" + providerName + "' (ollama | mock)");
    };

    McpServer server = new McpServer(new AssistantService(provider), host, port);
    server.start();
    System.out.println("MiniC++ GenAI server listening on http://" + host + ":" + server.port()
        + " (provider " + provider.name() + ", model " + provider.model() + ")");
    if (!provider.isAvailable()) {
      System.out.println("warning: provider is not available yet; is Ollama running and the model pulled?"
          + " (ollama pull " + provider.model() + ")");
    }
    Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
  }

  private static String option(String[] args, String flag, String env, String fallback) {
    for (int i = 0; i < args.length - 1; i++) {
      if (args[i].equals(flag)) {
        return args[i + 1];
      }
    }
    String value = System.getenv(env);
    return value != null && !value.isBlank() ? value : fallback;
  }
}
