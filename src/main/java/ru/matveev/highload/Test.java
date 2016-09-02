package ru.matveev.highload;

import ru.matveev.highload.thread_pool.FixedThreadPool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by kirill on 01.09.16
 */
public class Test {
    public static void main(String[] args) {
        FixedThreadPool pool = new FixedThreadPool(5);
        pool.start();

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(9090);

            while (true) {
                Socket accept = serverSocket.accept();
                pool.execute(accept);
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while socket accepting", e);
        }

    }
}
