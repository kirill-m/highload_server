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
        String path = System.getProperty("user.dir") + "/src/static";
        String extension = contentName.substring(contentName.indexOf(".") + 1);
        byte[] content = null;
        byte[] header = null;
        if (contentName.length() == 1)
            path +="/index.html";
        else
            path += contentName;

        try {
            content = getContent(path, extension);
            if (content == null) {
                content = "<!DOCTYPE html><html><head></head><body><h1>Error 404. Not found</h1></body></html>".getBytes();
                header =  ("HTTP/1.1 404 Not Found\r\n" +
                        "Server: MyServer\r\n"+
                        "Content-Type: text/html\r\n" +
                        "Connection: close\r\n\r\n").getBytes();
            } else {
                header = getHeader(extension, content.length).getBytes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            outputStream.write(header);
            outputStream.write(content);
            outputStream.flush();
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

    private String getHeader(String extension, int length) {
        String contentType = null;
        switch (extension) {
            case "png":
                contentType = "image/png";
                break;
            case "jpg":
                contentType = "image/jpeg";
                break;
            case "jpeg":
                contentType = "image/jpeg";
                break;
            case "gif":
                contentType = "image/gif";
                break;
            case "/":
            case "html":
                contentType = "text/html";
                break;
            case "css":
                contentType = "text/css";
                break;
            case "js":
                contentType = "text/javascript";
                break;
            case "swf":
                contentType = "application/x-shockwave-flash";
        }
        String result = "HTTP/1.1 200 OK\r\n" +
                "Server: MyServer\r\n"+
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n\r\n";

        return result;

    }

    private byte[] getContent(String path, String extension) throws IOException {
        byte[] content = null;
        System.out.println("PATH!!! " + path);
        path = tryEncode(path);
        System.out.println("PATH!!! " + path);
        switch (extension) {
            case "html":
            case "css":
            case "js":
            case "/":
            case "": {
                FileInputStream fis = new FileInputStream(path);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
                String strLine;
                StringBuilder stringBuilder = new StringBuilder();

                while ((strLine = bufferedReader.readLine()) != null) {
                    stringBuilder.append(strLine);
                }
                content = stringBuilder.toString().getBytes("Cp1251");
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
                content = baos.toByteArray();
                break;
            }
            case "swf": {
                InputStream inputStream = null;
                BufferedInputStream bis = null;

                try{
                    inputStream = new FileInputStream(path);
                    bis = new BufferedInputStream(inputStream);

                    int numByte = bis.available();
                    content = new byte[numByte];

                    bis.read(content);

                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    if(inputStream!=null)
                        inputStream.close();
                    if(bis!=null)
                        bis.close();
                }
            }
        }
        return content;
    }

    public static String tryEncode(String str) throws UnsupportedEncodingException {
        int i = str.indexOf('%');

        while (i != -1){
            byte bs[] = new byte[1];
            bs[0] = (byte) Integer.parseInt(str.substring(i+1, i+3), 16);

            str = str.replaceFirst("%..", new String(bs,"UTF8"));
            i = str.indexOf('%');
        }
        return str;
    }

}
