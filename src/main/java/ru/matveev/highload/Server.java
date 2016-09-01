package ru.matveev.highload;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Queue;

/**
 * Created by kirill on 23.02.16
 */

public class Server extends Thread {

    private final String directory;
    private OutputStream outputStream;
    private String requestType;
    private final Queue<Socket> requests = Main.requests;

    public Server(String directory) {
        this.directory = directory;
    }

    @Override
    public synchronized void run() {
        while(true) {
            synchronized (Main.LOCK) {
                Socket socket = requests.poll();
                while(socket == null) {
                    try {
                        Main.LOCK.wait();
                        socket = requests.poll();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    this.outputStream = socket.getOutputStream();
                    String input = readInput(socket.getInputStream());
                    System.out.println("INPUT: " + input);
                    if (!input.equals("null")) {
                        System.out.println("INPUT2: " + input);
                        requestType = getRequestType(input);
                        switch (requestType) {
                            case "HEAD":
                            case "GET": {
                                writeOutput(input, socket);
                                break;
                            }
                            case "POST":
                                writeOutputPost(socket);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

    }


    private String readInput(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String str;
        StringBuilder sb = new StringBuilder();
        while((str = reader.readLine()) != null) {
            sb.append(str);
        }
        return sb.toString();
    }

    private void writeOutput(String input, Socket socket) throws IOException {
        byte[] content = null;
        byte[] header = null;

        String contentName = getPath(input);

        String path = directory;

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
                        "ru.matveev.highload.Server: MyServer\r\n"+
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

        try {
            content = getContent(path, extension);
            if (content == null) {
                content = "<!DOCTYPE html><html><head></head><body><h1>Error 404. Not found</h1></body></html>".getBytes();
                header =  ("HTTP/1.1 404 Not Found\r\n" +
                        "ru.matveev.highload.Server: MyServer\r\n"+
                        "Content-Type: text/html\r\n" +
                        "Connection: close\r\n\r\n").getBytes();
            } else {
                header = getHeader(extension, content.length).getBytes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (header != null) {
                outputStream.write(header);
            }
            if (requestType.equals("GET") && content != null)
                outputStream.write(content);
            outputStream.flush();
            socket.close();
        }

    }

    private void writeOutputPost(Socket socket) throws IOException {
        byte[] content;
        byte[] header;
        content = "<!DOCTYPE html><html><head></head><body><h1>Error 405. Method Not allowed</h1></body></html>".getBytes();
        header =  ("HTTP/1.1 405 Method Not Allowed\r\n" +
                "ru.matveev.highload.Server: MyServer\r\n"+
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n\r\n").getBytes();
        outputStream.write(header);
        outputStream.write(content);
        outputStream.flush();
        socket.close();
    }

    private String getPath(String s) throws UnsupportedEncodingException {
        String result;
        int i;
        int pathIndex;
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

        return "HTTP/1.1 200 OK\r\n" +
                "Date:" + new Date() + "\r\n" +
                "ru.matveev.highload.Server: MyServer\r\n"+
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n\r\n";

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

    private String getRequestType(String request) {
        return request.substring(0, request.indexOf(' '));
    }

}
