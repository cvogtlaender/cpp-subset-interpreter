package de.cvogtlaender.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import de.cvogtlaender.mcp.provider.GenAiException;
import de.cvogtlaender.mcp.provider.OllamaProvider;

/** Runs the Ollama client against a fake Ollama server. */
class OllamaProviderTest {

  private HttpServer fakeOllama;
  private final AtomicReference<String> lastRequest = new AtomicReference<>();
  private volatile int generateStatus = 200;
  private volatile String generateBody = "{\"model\":\"codellama\",\"response\":\"generated text\",\"done\":true}";

  @BeforeEach
  void start() throws IOException {
    fakeOllama = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    fakeOllama.createContext("/api/generate", exchange -> {
      lastRequest.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
      respond(exchange, generateStatus, generateBody);
    });
    fakeOllama.createContext("/api/tags",
        exchange -> respond(exchange, 200, "{\"models\":[{\"name\":\"codellama:latest\"}]}"));
    fakeOllama.start();
  }

  @AfterEach
  void stop() {
    fakeOllama.stop(0);
  }

  private static void respond(HttpExchange exchange, int status, String body) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(status, bytes.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(bytes);
    }
  }

  private OllamaProvider provider(String model) {
    return new OllamaProvider("http://127.0.0.1:" + fakeOllama.getAddress().getPort(), model, Duration.ofSeconds(5));
  }

  @Test
  void generatesWithoutStreaming() throws GenAiException {
    assertEquals("generated text", provider("codellama").generate("sys", "prompt"));
    JsonObject request = JsonParser.parseString(lastRequest.get()).getAsJsonObject();
    assertEquals("codellama", request.get("model").getAsString());
    assertEquals("sys", request.get("system").getAsString());
    assertEquals("prompt", request.get("prompt").getAsString());
    assertFalse(request.get("stream").getAsBoolean());
  }

  @Test
  void availabilityChecksTheModel() {
    assertTrue(provider("codellama").isAvailable());
    assertFalse(provider("deepseek-coder").isAvailable());
  }

  @Test
  void httpErrorsBecomeExceptions() {
    generateStatus = 404;
    generateBody = "{\"error\":\"model 'x' not found\"}";
    GenAiException e = assertThrows(GenAiException.class, () -> provider("x").generate("s", "p"));
    assertTrue(e.getMessage().contains("404"));
  }

  @Test
  void unreachableServer() {
    OllamaProvider unreachable = new OllamaProvider("http://127.0.0.1:1", "codellama", Duration.ofSeconds(2));
    assertThrows(GenAiException.class, () -> unreachable.generate("s", "p"));
    assertFalse(unreachable.isAvailable());
  }
}
