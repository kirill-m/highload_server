package ru.matveev.highload;

import java.net.Socket;

/**
 * Created by kirill on 01.09.16
 */
public interface ThreadPool {
    void execute(Socket socket);

    void start(int maxWorkers);
}
