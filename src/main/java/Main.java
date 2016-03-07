import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by kirill on 23.02.16.
 */
public class Main {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(9000);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Main thread");
            new Thread(new Server(socket)).start();
            System.out.println("Main thread stopped");
        }
    }

}
