import org.apache.commons.cli.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Created by kirill on 23.02.16.
 */
public class Main {

    private static Options options = new Options();

    static {
        options.addOption(new Option("r", "root",true, "Root directory"));
        options.addOption(new Option("c",  "cpus", true, "CPU number"));
        options.addOption(new Option("p", "port", true, "Port"));
    }

    static int workers = 5;

    static Vector threads = new Vector();

    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws IOException, ParseException {

        CommandLineParser parser = new DefaultParser();

        CommandLine commandLine = parser.parse(options, args);

        String directory = commandLine.getOptionValue("r", "static");
        String host = commandLine.getOptionValue("w", "127.0.0.1");
        int port = Integer.parseInt(commandLine.getOptionValue("p", "8080"));
        System.out.println(directory + " " + host + " " + port);

        for (int i = 0; i < workers; ++i) {
            Server s = new Server();
            (new Thread(s, "worker #"+i)).start();
            threads.addElement(s);
        }

        ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);

        while (true) {
            Socket socket = serverSocket.accept();
//            System.out.println("Main thread");
//            new Thread(new Server(socket)).start();
//
//            System.out.println("Main thread stopped");

            Server s = null;
            synchronized (threads) {
                if (threads.isEmpty()) {
                    Server ws = new Server();
                    ws.setSocket(socket);
                    (new Thread(ws, "additional worker")).start();
                } else {
                    s = (Server) threads.elementAt(0);
                    threads.removeElementAt(0);
                    s.setSocket(socket);
                }
            }


        }
    }

}
