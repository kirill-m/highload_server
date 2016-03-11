import org.apache.commons.cli.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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

    static String directory = "/Users/kirill/IdeaProjects/highload/src/static";

    public static void main(String[] args) throws IOException, ParseException {

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options, args);

        directory = commandLine.getOptionValue("r", directory);
        int port = Integer.parseInt(commandLine.getOptionValue("p", "8080"));
        int cpus = Integer.parseInt(commandLine.getOptionValue("c", "2"));
        System.out.println(directory + " " + port);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2 * cpus + 1);

        for (int i = 0; i < workers; ++i) {
            Server s = new Server();
            (new Thread(s, "worker #"+i)).start();
            threads.addElement(s);
        }

        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            Socket socket = serverSocket.accept();

            Server ws = new Server();
            ws.setSocket(socket);
            //executor.execute(ws);

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
