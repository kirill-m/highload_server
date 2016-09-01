package ru.matveev.highload;

import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by kirill on 01.09.16
 */
public class FixedThreadPool implements ThreadPool {
    private final Object lock = new Object();
    private final Queue<Socket> tasks = new ArrayDeque<>();

    @Override
    public void execute(Socket socket) {
        synchronized (lock) {
            lock.notify();
        }
        tasks.add(socket);
    }

    @Override
    public void start(int maxWorkers) {
        for (int i = 0; i < maxWorkers; i++) {
            new Server(Main.directory).start();
        }
    }
}
