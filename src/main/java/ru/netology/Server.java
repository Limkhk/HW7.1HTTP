package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final List<String> validPaths;
    private final ExecutorService threadPool;
    public static int port;

    public Server(List<String> validPaths, int threadCount, int port) {
        this.validPaths = validPaths;
        this.threadPool = Executors.newFixedThreadPool(threadCount);
        this.port = port;
    }

    public void start() {
        try (final var serverSocket = new ServerSocket(port)) {
            System.out.println("Запускаем сервер на порту " + port);
            System.out.println("Открой в браузере http://localhost:" + port + "/");
            while (true) {
                final var socket = serverSocket.accept();
                threadPool.execute(new RequestHandler(socket, validPaths));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
