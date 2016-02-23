import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by kirill on 23.02.16.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        System.out.print("test");

        ServerSocket serverSocket = new ServerSocket(9000);

        while (true) {
            Socket s = serverSocket.accept();
            System.out.print("socket!");
        }
    }

}
