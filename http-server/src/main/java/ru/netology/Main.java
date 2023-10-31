package ru.netology;

import java.io.IOException;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(9999);

        for (String path : Server.getValidPaths()) {
            server.addHandler("GET", path, server::defaultRequestHandler);
        }

        //Демонстрация работоспособности методов getQueryParams и getQueryParam
        server.addHandler("GET", "/submit", (request, responseStream) -> {
            String param1 = request.getQueryParam("param1");
            String param2 = request.getQueryParam("param2");
            Map<String, String> queryParams = request.getQueryParams();

            String responseText = "Received parameters:\n";
            responseText += "param1=" + param1 + "\n";
            responseText += "param2=" + param2 + "\n";
            responseText += "All parameters: " + queryParams.toString();

            try {
                responseStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: " + responseText.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        responseText
                ).getBytes());
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.openConnection();
    }
}

