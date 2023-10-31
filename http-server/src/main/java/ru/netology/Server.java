package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;


import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class Server {
    private static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html",
            "/events.html", "/events.js");
    private final ExecutorService threadPool = newFixedThreadPool(64);
    private final int port;

    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server(int port) {
        this.port = port;
    }

    public void openConnection() {
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                final Socket socket = serverSocket.accept();
                threadPool.submit(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            final String requestLine = in.readLine();
            final String[] parts = requestLine.split(" ");

            if (parts.length != 3) {
                return;
            }

            Request request = new Request(parts[0], parts[1]);
            Handler handler = handlers.getOrDefault(request.getMethod(),
                    new ConcurrentHashMap<>()).get(request.getPath());

            if (handler != null) {
                handler.handle(request, out);
            } else {
                defaultRequestHandler(request, out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).put(path, handler);
    }

    public void defaultRequestHandler(Request request, BufferedOutputStream responseStream) {
        try {
            if (validPaths.contains(request.getPath())) {
                final Path filePath = Path.of(".", "public", request.getPath());
                final String mimeType = Files.probeContentType(filePath);

                if (request.getPath().equals("/classic.html")) {
                    final String template = Files.readString(filePath);
                    final byte[] content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    responseStream.write((
                            "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
                    ).getBytes());
                    responseStream.write(content);
                    responseStream.flush();
                } else {
                    final long length = Files.size(filePath);
                    responseStream.write((
                            "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
                    ).getBytes());
                    Files.copy(filePath, responseStream);
                    responseStream.flush();
                }
            } else {
                responseStream.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
                ).getBytes());
                responseStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getValidPaths() {
        return unmodifiableList(validPaths);
    }
}
