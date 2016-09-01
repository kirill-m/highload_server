package ru.matveev.highload;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by kirill on 01.09.16
 */
public class Main {
    public static final Object LOCK = new Object();

    private static Options options = new Options();

    static {
        options.addOption(new Option("r", "root",true, "Root directory"));
        options.addOption(new Option("c",  "cpus", true, "CPU number"));
        options.addOption(new Option("p", "port", true, "Port"));
    }

    static int workers = 5;

    static List<Server> threads = new ArrayList<>();

    static final Queue<Socket> requests = new ArrayDeque<>();

    public static final int DEFAULT_PORT = 8080;

    static String directory = System.getProperty("user.dir");

    static void enqueue(Socket s) {
        synchronized (requests) {
            requests.add(s);
            synchronized (LOCK) {
                LOCK.notify();
            }
        }
    }

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options, args);
        directory = commandLine.getOptionValue("r", directory);
        int port = Integer.parseInt(commandLine.getOptionValue("p", String.valueOf(DEFAULT_PORT)));
        int cpus = Integer.parseInt(commandLine.getOptionValue("c", "2"));
        System.out.println(directory + " " + port);

        workers = cpus * 2 + 1;
        for (int i = 0; i < workers; ++i) {
            Server s = new Server(directory);
            s.start();
            threads.add(s);
        }

        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            Socket socket = serverSocket.accept();
            enqueue(socket);
        }
    }

}