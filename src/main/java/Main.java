import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by kirill on 23.02.16.
 */
public class Main {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(9000);
        int i = 0;
        while (i != 5) {
            Socket socket = serverSocket.accept();
            System.out.println("Main thread");
            i++;
            new Thread(new Server(socket)).start();
            System.out.println("Main thread stopped");
        }
    }

}
