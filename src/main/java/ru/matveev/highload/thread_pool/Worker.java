package ru.matveev.highload.thread_pool;

import java.net.Socket;

import static ru.matveev.highload.thread_pool.FixedThreadPool.*;
import static ru.matveev.highload.utils.ResponseBuilder.*;

/**
 * Created by kirill on 01.09.16
 */
public class Worker implements Runnable {
    @Override
    public void run() {
        while (true) {
            Socket socket = getSocket();
            synchronized (LOCK) {
                while(socket == null) {
                    try {
                        LOCK.wait();
                        socket = getSocket();
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Worker has been interrupted during run", e);
                    }
                }
                respond(socket);
            }
        }
    }
}
