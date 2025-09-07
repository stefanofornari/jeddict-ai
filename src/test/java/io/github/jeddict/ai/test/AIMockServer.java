package io.github.jeddict.ai.test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple mock server for OpenAI endpoints.
 * <p>
 * This server listens for POST requests on the root path ("/").
 * It inspects the request body for an instruction to "use mock <file>", where <file>
 * is the path to a mock file relative to "src/test/resources/mocks".
 * <p>
 * The server will respond with the content of the specified mock file.
 * <p>
 * The file name can be unquoted or enclosed in double quotes.
 * For example:
 * <ul>
 *     <li>use mock my_file.txt</li>
 *     <li>use mock "my file with spaces.json"</li>
 * </ul>
 * <p>
 * To start the server, run the {@link #main(String[])} method from your IDE or
 * from the command line. By default, the server listens on port 8080.
 * You can specify a different port by passing it as a command line argument.
 * <p>
 * To stop the server, interrupt the process (Ctrl+C).
 */
public class AIMockServer {

    private static final Pattern MOCK_INSTRUCTION_PATTERN =
            Pattern.compile("use mock\\s+(?:\"([^\"]+)\"|(\\\\S+))", Pattern.CASE_INSENSITIVE);

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[0]);
                System.err.println("Usage: java AIMockServer [port]");
                System.exit(1);
            }
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new MockHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port " + port);
    }

    static class MockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Matcher matcher = MOCK_INSTRUCTION_PATTERN.matcher(body);

            if (matcher.find()) {
                String mockFile = matcher.group(1); // Quoted file name
                if (mockFile == null) {
                    mockFile = matcher.group(2); // Unquoted file name
                }
                Path mockPath = Path.of("src/test/resources/mocks").resolve(mockFile).normalize();

                if (!mockPath.startsWith(Path.of("src/test/resources/mocks"))) {
                    sendResponse(exchange, 400, "Invalid mock file path: " + mockFile);
                    return;
                }

                if (!Files.exists(mockPath)) {
                    sendResponse(exchange, 404, "Mock file not found: " + mockFile);
                    return;
                }

                try {
                    String mockContent = Files.readString(mockPath, StandardCharsets.UTF_8);
                    sendResponse(exchange, 200, mockContent);
                } catch (IOException ex) {
                    sendResponse(exchange, 500, "Error reading mock file: " + ex.getMessage());
                }
            } else {
                sendResponse(exchange, 400, "No 'use mock <mock-file>' instruction found in POST body.");
            }
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
