import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
            String input = readInput();
            String path = getPath(input);
            writeOutput(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Server(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    private String readInput() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String str = null;
        StringBuilder sb = new StringBuilder();
        while(true) {
            str = bufferedReader.readLine();
            sb.append(str);
            System.out.println(str);
            if(str == null || str.length() == 0) {
                break;
            }
        }
        return sb.toString();
    }

    private void writeOutput(String contentName) throws IOException {
        String path = System.getProperty("user.dir") + "/src/static/";
        String extension = contentName.substring(contentName.indexOf(".") + 1);

        if (contentName.length() == 1)
            path +="index.html";
        else
            path += contentName;

        try {
            switch (extension) {
                case "html":
                case "txt": {
                    FileInputStream fis = new FileInputStream(path);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
                    String strLine;
                    StringBuilder sb = new StringBuilder();

                    while ((strLine = bufferedReader.readLine()) != null) {
                        sb.append(strLine);
                    }

                    outputStream.write(sb.toString().getBytes());
                    outputStream.write(getHeader().getBytes());
                    outputStream.flush();
                    break;
                }
                case "png":
                case "gif":
                case "jpg":
                case "jpeg": {
                    BufferedImage image = ImageIO.read(new File(path));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(image, extension, baos);
                    baos.flush();
                    byte[] result = baos.toByteArray();
                    outputStream.write(result);
                    outputStream.write(getHeader().getBytes());
                    outputStream.flush();
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }

    }

    private String getPath(String s) {
        String result = null;
        int i = 0;

        int pathIndex = "GET ".length();

        for (i = pathIndex; i < s.length(); i++) {
            if (s.charAt(i) == ' ') break;
        }

        result = s.substring(pathIndex, i);

        if (result.indexOf('.') == -1){
            if (result.charAt(result.length()-1) != '/')
                result += "/";
        }

        System.out.println("It asks this:\n" + s);
        System.out.println("\n" + "Work Directory:\n" + result + "\n");
        return result;
    }

    private String getHeader() {
        String result = "HTTP/1.1 200 OK\r\n" +
                "Server: JSS\r\n"+
                "Content-Type: " + "TODO: Type" + "\r\n" +
                "Content-Length: " + "TODO: Length" + "\r\n" +
                "Connection: close\r\n\r\n";

        return result;

    }

}
