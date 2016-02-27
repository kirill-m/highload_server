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
        String str;
        while(true) {
            str = bufferedReader.readLine();
            System.out.println(str);
            if(str == null || str.length() == 0) {
                break;
            }
        }
    }

    private void writeOutput() throws IOException {
        FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + "/src/static/index.html");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
        String strLine;
        try {
            StringBuilder sb = new StringBuilder();

            while((strLine = bufferedReader.readLine())!= null)
            {
                sb.append(strLine);
            }

            String response = "<html><head><body><h1>Server answer</h1></body></head></html>";

            outputStream.write(sb.toString().getBytes());
            outputStream.flush();

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }

    }

}
