package com.nettydemo.server.sync;

import com.nettydemo.common.Utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SyncServer {

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(Utils.getInstance().getPort());
        boolean stop = false;
        while (!stop) {
            System.out.println("Waiting for clients");
            Socket socket = server.accept();
//            System.out.println("Client " + socket.getChannel().getRemoteAddress()+" connected");
            ClientThread clientThread = new ClientThread(socket);
            clientThread.start();
        }
        server.close();
    }
}
