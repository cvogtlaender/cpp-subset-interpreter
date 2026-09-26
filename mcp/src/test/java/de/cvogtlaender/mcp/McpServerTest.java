package de.cvogtlaender.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.cvogtlaender.mcp.provider.GenAiException;
import de.cvogtlaender.mcp.provider.GenAiProvider;
import de.cvogtlaender.mcp.provider.MockProvider;

class McpServerTest {

  private final HttpClient http = HttpClient.newHttpClient();
  private MockProvider provider;
  private McpServer server;

  @BeforeEach
  void start() throws IOException {
    provider = new MockProvider();
    server = new McpServer(new AssistantService(provider), "127.0.0.1", 0);
    server.start();
  }

  @AfterEach
  void stop() {
    server.stop();
  }

  private HttpResponse<String> post(String path, String body) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + path))
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .header("Content-Type", "application/json")
        .build();
    return http.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> get(String path) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + path)).GET().build();
    return http.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private static JsonObject json(HttpResponse<String> response) {
    return JsonParser.parseString(response.body()).getAsJsonObject();
  }

  @Test
  void health() throws Exception {
    HttpResponse<String> response = get("/health");
    assertEquals(200, response.statusCode());
    JsonObject body = json(response);
    assertEquals("ok", body.get("status").getAsString());
    assertEquals("mock", body.get("provider").getAsString());
    assertTrue(body.get("providerAvailable").getAsBoolean());
  }

  @Test
  void complete() throws Exception {
    HttpResponse<String> response = post("/complete", "{\"code\": \"int main() {\\n\\n}\", \"line\": 2, \"column\": 0}");
    assertEquals(200, response.statusCode(), response.body());
    assertEquals("return 0;", json(response).get("completion").getAsString());
  }

  @Test
  void explain() throws Exception {
    HttpResponse<String> response = post("/explain", "{\"code\": \"int main() { return 0; }\"}");
    assertEquals(200, response.statusCode(), response.body());
    assertTrue(json(response).get("explanation").getAsString().contains("MiniC++"));
  }

  @Test
  void refactor() throws Exception {
    HttpResponse<String> response = post("/refactor", "{\"code\": \"int main() { int x = 0; return x; }\"}");
    assertEquals(200, response.statusCode(), response.body());
    JsonObject body = json(response);
    assertTrue(body.get("compiles").getAsBoolean());
    assertEquals("int main() {\n  return 0;\n}\n", body.get("code").getAsString());
    assertEquals(0, body.getAsJsonArray("diagnostics").size());
  }

  @Test
  void detectBugsReportsCompilerDiagnosticsWithPositions() throws Exception {
    HttpResponse<String> response = post("/detect-bugs", "{\"code\": \"int main() {\\n  return y;\\n}\"}");
    assertEquals(200, response.statusCode(), response.body());
    JsonObject diagnostic = json(response).getAsJsonArray("compilerDiagnostics").get(0).getAsJsonObject();
    assertEquals("RESOLVE", diagnostic.get("phase").getAsString());
    assertEquals(2, diagnostic.get("line").getAsInt());
    assertEquals(9, diagnostic.get("column").getAsInt());
  }

  @Test
  void badRequests() throws Exception {
    assertEquals(400, post("/explain", "{}").statusCode());
    assertEquals(400, post("/explain", "not json").statusCode());
    assertEquals(400, post("/explain", "[1, 2]").statusCode());
    assertEquals(400, post("/complete", "{\"code\": \"x\", \"line\": \"1\", \"column\": 0}").statusCode());
    HttpResponse<String> outside = post("/complete", "{\"code\": \"x\", \"line\": 5, \"column\": 0}");
    assertEquals(400, outside.statusCode());
    assertTrue(json(outside).get("error").getAsString().contains("beyond the end"));
    assertEquals(413, post("/explain", "{\"code\": \"" + "a".repeat((1 << 20) + 10) + "\"}").statusCode());
  }

  @Test
  void wrongMethodOrPath() throws Exception {
    assertEquals(405, get("/explain").statusCode());
    assertEquals(405, post("/health", "{}").statusCode());
    assertEquals(404, get("/nope").statusCode());
    assertEquals(404, post("/explain/more", "{\"code\": \"\"}").statusCode());
  }

  @Test
  void providerFailureIsBadGateway() throws Exception {
    server.stop();
    GenAiProvider failing = new GenAiProvider() {
      @Override
      public String name() {
        return "failing";
      }

      @Override
      public String model() {
        return "none";
      }

      @Override
      public String generate(String system, String prompt) throws GenAiException {
        throw new GenAiException("backend down");
      }

      @Override
      public boolean isAvailable() {
        return false;
      }
    };
    server = new McpServer(new AssistantService(failing), "127.0.0.1", 0);
    server.start();

    HttpResponse<String> response = post("/explain", "{\"code\": \"int main() { }\"}");
    assertEquals(502, response.statusCode());
    assertEquals("backend down", json(response).get("error").getAsString());
    assertEquals("degraded", json(get("/health")).get("status").getAsString());
  }
}
