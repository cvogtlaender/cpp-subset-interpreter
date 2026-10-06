package de.cvogtlaender.mcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * A server for the Model Context Protocol (MCP) over stdio: newline-delimited
 * JSON-RPC 2.0 on stdin/stdout. It offers the MiniC++ compiler and interpreter
 * ({@link MiniCppTools}) as tools and the language summary as a resource, so
 * that MCP hosts (Claude Code, Claude Desktop, VS Code, ...) can check and run
 * the code their model writes.
 */
public class McpStdioServer {

  /** Protocol versions this server speaks, newest first. */
  static final List<String> PROTOCOL_VERSIONS = List.of("2025-11-25", "2025-06-18", "2025-03-26", "2024-11-05");

  static final String LANGUAGE_URI = "minicpp://language";

  // JSON-RPC error codes
  static final int PARSE_ERROR = -32700;
  static final int INVALID_REQUEST = -32600;
  static final int METHOD_NOT_FOUND = -32601;
  static final int INVALID_PARAMS = -32602;
  static final int INTERNAL_ERROR = -32603;

  private static final String INSTRUCTIONS = """
      Tools for MiniC++, a small subset of C++. Before suggesting MiniC++ code, validate it with 'check'
      and, for complete programs, execute it with 'run'; the compiler is the ground truth for what
      MiniC++ supports.
      """;

  private static final String CODE_SCHEMA = """
      {"type": "object", "properties": {"code": {"type": "string", "description": "MiniC++ source code"}},
       "required": ["code"]}""";

  private static final String READ_ONLY = """
      {"readOnlyHint": true, "destructiveHint": false, "idempotentHint": true, "openWorldHint": false}""";

  private static final JsonArray TOOLS = JsonParser.parseString(
      """
          [
            {"name": "check", "title": "Check MiniC++ code",
             "description": "Runs the MiniC++ compiler (parser, name resolution, type checker) and returns its diagnostics. Lines are 1-based, columns 0-based. 'int main()' is only required if the code defines a main function.",
             "inputSchema": %1$s, "annotations": %2$s},
            {"name": "run", "title": "Run a MiniC++ program",
             "description": "Compiles and runs a complete MiniC++ program (with 'int main()' or 'void main()') in the interpreter and returns its output, exit code and compile or runtime errors (e.g. division by zero, null pointer dereference). Programs cannot read input. Execution is stopped after the time limit.",
             "inputSchema": {"type": "object", "properties": {
                 "code": {"type": "string", "description": "MiniC++ program"},
                 "timeoutSeconds": {"type": "integer", "minimum": 1, "maximum": %3$d,
                                    "description": "time limit, default %4$d"}},
               "required": ["code"]},
             "annotations": %2$s},
            {"name": "ast", "title": "Show the MiniC++ syntax tree",
             "description": "Parses MiniC++ code and returns its abstract syntax tree, or the syntax errors.",
             "inputSchema": %1$s, "annotations": %2$s}
          ]"""
          .formatted(CODE_SCHEMA, READ_ONLY, MiniCppTools.MAX_TIMEOUT.toSeconds(),
              MiniCppTools.DEFAULT_TIMEOUT.toSeconds()))
      .getAsJsonArray();

  /** Error answered with a JSON-RPC error response. */
  static final class RpcError extends Exception {
    final int code;

    RpcError(int code, String message) {
      super(message);
      this.code = code;
    }
  }

  private final MiniCppTools tools;
  private final BufferedReader in;
  private final Writer out;
  // nulls are needed for "id": null in error responses
  private final Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
  private final ExecutorService workers = Executors.newCachedThreadPool(task -> {
    Thread thread = new Thread(task, "mcp-worker");
    thread.setDaemon(true);
    return thread;
  });
  // tool calls in progress, by request id, so that they can be cancelled
  private final Map<JsonElement, FutureTask<?>> running = new ConcurrentHashMap<>();

  public McpStdioServer(MiniCppTools tools, InputStream in, OutputStream out) {
    this.tools = tools;
    this.in = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    this.out = new OutputStreamWriter(out, StandardCharsets.UTF_8);
  }

  /** Serves until stdin is closed, then waits briefly for running tool calls. */
  public void serve() throws IOException {
    String line;
    while ((line = in.readLine()) != null) {
      if (!line.isBlank()) {
        receive(line);
      }
    }
    workers.shutdown();
    try {
      if (!workers.awaitTermination(5, TimeUnit.SECONDS)) {
        workers.shutdownNow();
      }
    } catch (InterruptedException e) {
      workers.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  private void receive(String line) {
    JsonObject message;
    try {
      JsonElement json = JsonParser.parseString(line);
      if (!json.isJsonObject()) {
        sendError(JsonNull.INSTANCE, INVALID_REQUEST, "expected a JSON-RPC message object");
        return;
      }
      message = json.getAsJsonObject();
    } catch (JsonParseException e) {
      sendError(JsonNull.INSTANCE, PARSE_ERROR, "invalid JSON: " + e.getMessage());
      return;
    }

    if (!message.has("method")) {
      return; // a response; this server sends no requests
    }
    JsonElement methodElement = message.get("method");
    JsonElement id = message.get("id");
    if (!methodElement.isJsonPrimitive() || !methodElement.getAsJsonPrimitive().isString()
        || id != null && !(id.isJsonPrimitive() && !id.getAsJsonPrimitive().isBoolean())) {
      sendError(id != null && id.isJsonPrimitive() ? id : JsonNull.INSTANCE, INVALID_REQUEST,
          "'method' must be a string and 'id' a string or number");
      return;
    }
    String method = methodElement.getAsString();
    JsonObject params = message.has("params") && message.get("params").isJsonObject()
        ? message.getAsJsonObject("params")
        : new JsonObject();

    if (id == null) {
      if (method.equals("notifications/cancelled") && params.has("requestId")) {
        FutureTask<?> call = running.remove(params.get("requestId"));
        if (call != null) {
          call.cancel(true);
        }
      }
      return; // other notifications, e.g. notifications/initialized, need no action
    }

    if (method.equals("tools/call")) {
      // tool calls may take a while; run them in the background so that pings and
      // cancellations get through
      FutureTask<Void> call = new FutureTask<>(() -> {
        JsonObject response = respond(id, method, params);
        if (running.remove(id) != null) {
          send(response);
        }
        return null;
      });
      running.put(id, call);
      workers.execute(call);
    } else {
      send(respond(id, method, params));
    }
  }

  private JsonObject respond(JsonElement id, String method, JsonObject params) {
    try {
      return result(id, dispatch(method, params));
    } catch (RpcError e) {
      return error(id, e.code, e.getMessage());
    } catch (CancellationException e) {
      return error(id, INTERNAL_ERROR, "request was cancelled");
    } catch (RuntimeException e) {
      e.printStackTrace();
      return error(id, INTERNAL_ERROR, "internal error: " + e);
    }
  }

  JsonElement dispatch(String method, JsonObject params) throws RpcError {
    return switch (method) {
      case "initialize" -> initialize(params);
      case "ping" -> new JsonObject();
      case "tools/list" -> {
        JsonObject result = new JsonObject();
        result.add("tools", TOOLS.deepCopy());
        yield result;
      }
      case "tools/call" -> callTool(params);
      case "resources/list" -> {
        JsonObject result = new JsonObject();
        JsonArray resources = new JsonArray();
        resources.add(languageResource());
        result.add("resources", resources);
        yield result;
      }
      case "resources/templates/list" -> {
        JsonObject result = new JsonObject();
        result.add("resourceTemplates", new JsonArray());
        yield result;
      }
      case "resources/read" -> readResource(params);
      default -> throw new RpcError(METHOD_NOT_FOUND, "method not found: " + method);
    };
  }

  private static JsonObject initialize(JsonObject params) {
    String requested = params.has("protocolVersion") ? params.get("protocolVersion").getAsString() : "";
    JsonObject result = new JsonObject();
    result.addProperty("protocolVersion",
        PROTOCOL_VERSIONS.contains(requested) ? requested : PROTOCOL_VERSIONS.get(0));

    JsonObject capabilities = new JsonObject();
    JsonObject toolCapability = new JsonObject();
    toolCapability.addProperty("listChanged", false);
    capabilities.add("tools", toolCapability);
    JsonObject resourceCapability = new JsonObject();
    resourceCapability.addProperty("listChanged", false);
    capabilities.add("resources", resourceCapability);
    result.add("capabilities", capabilities);

    JsonObject serverInfo = new JsonObject();
    serverInfo.addProperty("name", "minicpp");
    serverInfo.addProperty("title", "MiniC++ compiler and interpreter");
    serverInfo.addProperty("version", version());
    result.add("serverInfo", serverInfo);
    result.addProperty("instructions", INSTRUCTIONS);
    return result;
  }

  private JsonObject callTool(JsonObject params) throws RpcError {
    if (!params.has("name")) {
      throw new RpcError(INVALID_PARAMS, "missing tool name");
    }
    String name = params.get("name").getAsString();
    JsonObject args = params.has("arguments") && params.get("arguments").isJsonObject()
        ? params.getAsJsonObject("arguments")
        : new JsonObject();
    MiniCppTools.Result result;
    try {
      result = switch (name) {
        case "check" -> tools.check(requireString(args, "code"));
        case "run" -> tools.run(requireString(args, "code"), timeout(args));
        case "ast" -> tools.ast(requireString(args, "code"));
        default -> throw new RpcError(INVALID_PARAMS, "unknown tool: " + name);
      };
    } catch (IllegalArgumentException e) {
      // invalid arguments are reported to the model, which can correct them
      return toolResult(e.getMessage(), null, true);
    }
    return toolResult(result.text(), gson.toJsonTree(result).getAsJsonObject(), false);
  }

  private static JsonObject toolResult(String text, JsonObject structured, boolean isError) {
    JsonObject content = new JsonObject();
    content.addProperty("type", "text");
    content.addProperty("text", text);
    JsonArray contents = new JsonArray();
    contents.add(content);
    JsonObject result = new JsonObject();
    result.add("content", contents);
    if (structured != null) {
      result.add("structuredContent", structured);
    }
    result.addProperty("isError", isError);
    return result;
  }

  private static JsonObject languageResource() {
    JsonObject resource = new JsonObject();
    resource.addProperty("uri", LANGUAGE_URI);
    resource.addProperty("name", "language");
    resource.addProperty("title", "MiniC++ language summary");
    resource.addProperty("description", "The C++ features MiniC++ supports, and those it does not");
    resource.addProperty("mimeType", "text/plain");
    return resource;
  }

  private static JsonObject readResource(JsonObject params) throws RpcError {
    String uri = params.has("uri") ? params.get("uri").getAsString() : "";
    if (!uri.equals(LANGUAGE_URI)) {
      throw new RpcError(-32002, "resource not found: " + uri);
    }
    JsonObject content = new JsonObject();
    content.addProperty("uri", LANGUAGE_URI);
    content.addProperty("mimeType", "text/plain");
    content.addProperty("text", MiniCppTools.LANGUAGE);
    JsonArray contents = new JsonArray();
    contents.add(content);
    JsonObject result = new JsonObject();
    result.add("contents", contents);
    return result;
  }

  private static String requireString(JsonObject args, String field) {
    JsonElement e = args.get(field);
    if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) {
      throw new IllegalArgumentException("missing string argument '" + field + "'");
    }
    return e.getAsString();
  }

  private static Duration timeout(JsonObject args) {
    JsonElement e = args.get("timeoutSeconds");
    if (e == null || e.isJsonNull()) {
      return MiniCppTools.DEFAULT_TIMEOUT;
    }
    if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber()) {
      throw new IllegalArgumentException("'timeoutSeconds' must be an integer");
    }
    long seconds = e.getAsLong();
    if (seconds < 1 || seconds > MiniCppTools.MAX_TIMEOUT.toSeconds()) {
      throw new IllegalArgumentException(
          "'timeoutSeconds' must be between 1 and " + MiniCppTools.MAX_TIMEOUT.toSeconds());
    }
    return Duration.ofSeconds(seconds);
  }

  private static String version() {
    String version = McpStdioServer.class.getPackage().getImplementationVersion();
    return version != null ? version : "dev";
  }

  private static JsonObject result(JsonElement id, JsonElement result) {
    JsonObject response = envelope(id);
    response.add("result", result);
    return response;
  }

  private static JsonObject error(JsonElement id, int code, String message) {
    JsonObject error = new JsonObject();
    error.addProperty("code", code);
    error.addProperty("message", message);
    JsonObject response = envelope(id);
    response.add("error", error);
    return response;
  }

  private static JsonObject envelope(JsonElement id) {
    JsonObject message = new JsonObject();
    message.addProperty("jsonrpc", "2.0");
    message.add("id", id);
    return message;
  }

  private void sendError(JsonElement id, int code, String message) {
    send(error(id, code, message));
  }

  // one message per line; Gson escapes newlines inside strings
  private synchronized void send(JsonObject message) {
    try {
      out.write(gson.toJson(message));
      out.write('\n');
      out.flush();
    } catch (IOException e) {
      System.err.println("mcp: cannot write response: " + e.getMessage());
    }
  }
}
