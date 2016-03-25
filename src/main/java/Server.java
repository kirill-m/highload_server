import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by kirill on 23.02.16.
 */

public class Server extends Thread {

    private InputStream inputStream;
    private Socket socket;
    private OutputStream outputStream;
    private String requestType;
    private LinkedList<Socket> requests = Main.requests;
    @Override
    public synchronized void run() {

        while(true) {

            synchronized (requests) {
                if (requests.isEmpty()) {
                    try {
                        requests.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (socket == null) {
                        Socket skt = requests.removeFirst();
                        this.setSocket(skt);

                        try {
                            this.inputStream = socket.getInputStream();
                            this.outputStream = socket.getOutputStream();
                            String input = readInput();
                            if (!input.equals("null")) {
                                requestType = getRequestType(input);
                                switch (requestType) {
                                    case "HEAD":
                                    case "GET": {
                                        String path = getPath(input);
                                        writeOutput(path);
                                        break;
                                    }
                                    case "POST":
                                        writeOutputPost();
                                }
                            }
                            this.setSocketNull();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }

        }

    }

    public Server(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    public Server() throws IOException {
        socket = null;
//        this.inputStream = socket.getInputStream();
//        this.outputStream = socket.getOutputStream();
    }

    synchronized void setSocket(Socket socket) {
        this.socket = socket;
        //notify();
    }

    synchronized void setSocketNull() {
        this.socket = null;
        //notify();
    }

    private String readInput() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String str = null;
        StringBuilder sb = new StringBuilder();
        while(true) {
            str = bufferedReader.readLine();
            sb.append(str);
            if(str == null || str.length() == 0) {
                break;
            }
        }
        return sb.toString();
    }

    private void writeOutput(String contentName) throws IOException {
        byte[] content = null;
        byte[] header = null;

        String path = Main.directory;

        File file = new File(path+contentName);
        if (file.isDirectory()) {
            if (contentName.charAt(contentName.length()-1) != '/') {
                contentName += "/index.html";
            }else{
                contentName += "index.html";
            }
            file = new File(path+contentName);
            if (!file.isDirectory() && !file.exists()) {
                content = "<!DOCTYPE html><html><head></head><body><h1>Error 403. Forbidden</h1></body></html>".getBytes();
                header =  ("HTTP/1.1 403 Forbidden\r\n" +
                        "Server: MyServer\r\n"+
                        "Content-Type: text/html\r\n" +
                        "Connection: close\r\n\r\n").getBytes();
                outputStream.write(header);
                if (requestType.equals("GET"))
                    outputStream.write(content);
                outputStream.flush();
                socket.close();
                return;
            }
        } else {
            if (contentName.charAt(contentName.length()-1) == '/') {
                contentName = contentName.substring(0, contentName.length() - 1);
            }
        }

        String extension = contentName.substring(contentName.lastIndexOf(".") + 1);

        path += contentName;
        System.out.println(path);
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
            if (requestType.equals("GET"))
                outputStream.write(content);
            outputStream.flush();
            socket.close();
        }

    }

    private void writeOutputPost() throws IOException {
        byte[] content = null;
        byte[] header = null;
        content = "<!DOCTYPE html><html><head></head><body><h1>Error 405. Method Not allowed</h1></body></html>".getBytes();
        header =  ("HTTP/1.1 405 Method Not Allowed\r\n" +
                "Server: MyServer\r\n"+
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n\r\n").getBytes();
        outputStream.write(header);
        outputStream.write(content);
        outputStream.flush();
        socket.close();
    }

    private String getPath(String s) throws UnsupportedEncodingException {
        String result = null;
        int i = 0;
        int pathIndex = 0;
        if (requestType.equals("GET"))
            pathIndex = "GET ".length();
        else
            pathIndex = "HEAD ".length();

        for (i = pathIndex; i < s.length(); i++) {
            if (s.charAt(i) == ' ') break;
        }

        result = s.substring(pathIndex, i);

        if (result.indexOf('?') != -1) {
            result = result.substring(0,result.indexOf('?'));
        }
        result = URLDecoder.decode(result, "utf-8");
        if (result.indexOf('.') == -1){
            if (result.charAt(result.length()-1) != '/')
                result += "/";
        }
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
                "Date:" + new Date() + "\r\n" +
                "Server: MyServer\r\n"+
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n\r\n";

        return result;

    }

    private byte[] getContent(String path, String extension) throws IOException {
        File f = new File(path);
        if(!f.exists()) {
            return null;
        }
        byte[] content = null;
        switch (extension) {
            case "html":
            case "css":
            case "js":
            case "/":
            case "txt":
            case "png":
            case "gif":
            case "jpg":
            case "jpeg":
            case "swf": {
                InputStream inputStream = null;
                BufferedInputStream bufferedInputStream = null;

                try{
                    inputStream = new FileInputStream(path);
                    bufferedInputStream = new BufferedInputStream(inputStream);

                    int numByte = bufferedInputStream.available();
                    content = new byte[numByte];

                    bufferedInputStream.read(content);

                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    if(inputStream!=null)
                        inputStream.close();
                    if(bufferedInputStream!=null)
                        bufferedInputStream.close();
                }
            }
        }
        return content;
    }

    private String  getRequestType(String request) {
        return request.substring(0, request.indexOf(' '));
    }

}
