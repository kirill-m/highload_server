package ru.matveev.highload.thread_pool;

import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by kirill on 01.09.16
 */
public class FixedThreadPool implements ThreadPool {
    public final static Object LOCK = new Object();
    private static final Queue<Socket> tasks = new ArrayDeque<>();
    private final int maxWorkers;

    public FixedThreadPool(int maxWorkers) {
        this.maxWorkers = maxWorkers;
    }

    @Override
    public void execute(Socket socket) {
        tasks.add(socket);
        synchronized (LOCK) {
            LOCK.notify();
        }
    }

    @Override
    public void start() {
        for (int i = 0; i < maxWorkers; i++) {
            new Thread(new Worker()).start();
        }
    }

    public static Socket getSocket() {
        return tasks.poll();
    }
}
