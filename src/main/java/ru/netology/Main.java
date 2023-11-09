package ru.netology;

import java.io.IOException;

public class Main {
    final static int PORT = 9999;
    final static int TREAD_POOL = 64;

    public static void main(String[] args) {
        Server server = new Server(PORT, TREAD_POOL);
        server.addHandler("GET", "/messages", (request, responseStream) -> {
            try {
                server.handle(responseStream, "404", "Not Found");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler("POST", "/messages", (request, responseStream) -> server.handle(responseStream, "503", "Service Unavailable"));

        server.addHandler("GET", "/", ((request, outputStream) -> server.defaultHandler(outputStream, "index.html")));
        System.out.println("Запускаем сервер на порту " + PORT);
        System.out.println("Открой в браузере http://localhost:" + PORT + "/");
        server.start();

    }
}