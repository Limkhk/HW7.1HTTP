package ru.netology;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

        // Создаем экземпляр Server с 64 потоками
        final var server = new Server(validPaths, 64,9999);

        // Запуск сервера
        server.start();
    }
}