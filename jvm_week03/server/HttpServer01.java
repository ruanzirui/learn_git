package com.example.datastructor.jvm_week02_nio;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer01 {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8801);
        while(true){
            Socket socket = serverSocket.accept();
            if(socket.getInputStream().available()>0){
                service(socket);
            }

        }

    }

    private static void service(Socket socket){
        try {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
            printWriter.println("HTTP/1.1 200 OK");
            printWriter.println("Content-Type:text/html;charset=utf-8");
            String body = "hello,nio1";
            printWriter.println("Content-Length:" + body.getBytes().length);
            printWriter.println();
            printWriter.write(body);
            printWriter.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
