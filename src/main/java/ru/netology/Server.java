package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int socket;
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private final ExecutorService TREAD_POOL;
    private final ConcurrentHashMap<String, Map<String, Handler>> handlers;


    public Server(int serverSocket, int poolSize) {
        socket = serverSocket;
        TREAD_POOL = Executors.newFixedThreadPool(poolSize);
        handlers = new ConcurrentHashMap<>();
    }

    void start() {
        try (final var serverSocket = new ServerSocket(socket)) {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                TREAD_POOL.execute(() -> proceedConnection(socket));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            TREAD_POOL.shutdown();
        }
    }

    private Request createRequest(String method, String path) {
        if (method != null && !method.isBlank()) {
            return new Request(method, path);
        } else {
            return null;
        }
    }

    private void proceedConnection(Socket socket) {
        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {
            final var requestLine = in.readLine();

            if (requestLine == null || requestLine.isEmpty()) {
                // just close socket
                return;
            }
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                socket.close();
                return;
            }

            String method = parts[0];
            final var path = parts[1];
            Request request = createRequest(method, path);

            if (request == null || !handlers.containsKey(request.getMethod())) {
                handle(out, "400", "Bad Request");
                return;
            }

            Map<String, Handler> handlerMap = handlers.get(request.getMethod());
            String requestPath = request.getPath();
            if (handlerMap.containsKey(requestPath)) {
                Handler handler = handlerMap.get(requestPath);
                handler.handle(request, out);
            } else {  // Defaults
                if (!validPaths.contains(request.getPath())) {
                    handle(out, "404", "Not Found");
                } else {
                    defaultHandler(out, path);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void defaultHandler(BufferedOutputStream out, String path) throws IOException {
        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);

        // special case for classic
        if (path.equals("/classic.html")) {
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();
            return;
        }

        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }


    public void addHandler(String method, String path, Handler handler) {
        if (!handlers.containsKey(method)) {
            handlers.put(method, new HashMap<>());
        }
        handlers.get(method).put(path, handler);

    }

    public void handle(BufferedOutputStream responseOut, String responseCode, String responseStatus) throws IOException {
        responseOut.write((
                "HTTP/1.1 " + responseCode + " " + responseStatus + "\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        responseOut.flush();
    }

}
