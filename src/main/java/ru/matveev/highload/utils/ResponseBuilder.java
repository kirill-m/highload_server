package ru.matveev.highload.utils;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;

/**
 * Created by kirill on 01.09.16
 */
public class ResponseBuilder {
    public static void respond(Socket socket) {
        String request = readInput(socket);
        if (!"null".equals(request) && !"".equals(request)) {
            writeOutput(getRequestType(request), socket, request);
        }
    }

    private static void writeOutput(RequestType requestType, Socket socket, String request) {
        System.out.println("request: " + request);
        System.out.println(getPath(request));
    }

    private static String getPath(String request) {
        String result;
        request = request.trim();
        int pathIndex = request.indexOf("/");
        int i;
        for (i = pathIndex; i < request.length(); i++) {
            if (request.charAt(i) == ' ') break;
        }
        result = request.substring(pathIndex, i);

        if (result.indexOf('?') != -1) {
            result = result.substring(0, result.indexOf('?'));
        }
        try {
            result = URLDecoder.decode(result, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Exception happened during url decoding to UTF-8", e);
        }
        if (result.indexOf('.') == -1) {
            if (result.charAt(result.length() - 1) != '/')
                result += "/";
        }

        return result;
    }

    private static String readInput(Socket socket) {

        try (InputStream inputStream = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String str;
            StringBuilder sb = new StringBuilder();

            while((str = reader.readLine()) != null) {
                sb.append(str);
            }

            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while reading input", e);
        }
    }

    private static RequestType getRequestType(String request) {
        String input = request.substring(0, request.indexOf(' '));
        RequestType type = null;
        switch (input) {
            case "GET":
                type = RequestType.GET;
                break;
            case "HEAD":
                type = RequestType.HEAD;
                break;
            case "POST":
                type = RequestType.POST;
                break;
            case "DELETE":
                type = RequestType.DELETE;
        }
        return type;
    }
}
