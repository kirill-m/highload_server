import java.io.*;
import java.net.Socket;

/**
 * Created by kirill on 23.02.16.
 */
public class Server implements Runnable {

    private InputStream inputStream;
    private Socket socket;
    private OutputStream outputStream;

    @Override
    public void run() {
        try {
            System.out.println("from new thread");
            readInput();
            writeOutput();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Server(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    private void readInput() throws IOException {
        System.out.println("Someone connected to me!");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String str = null;
        while(true) {
            str = bufferedReader.readLine();
            if(str == null || str.length() == 0) {
                break;
            }
        }
        System.out.println("Input:\n" + str);
    }

    private void writeOutput() throws IOException {
        try {
            String response = "<html><head><body><h1>Server answer</h1></body></head></html>";

            outputStream.write(response.getBytes());
            outputStream.flush();

            //socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }

    }

}
