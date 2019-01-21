package com.nettydemo.server.sync;

import com.nettydemo.common.RequestMessage;
import com.nettydemo.common.ResponseMessage;
import com.nettydemo.server.MessageProcessor;

import java.io.*;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ClientThread extends Thread {

    private Socket socket;
    private static Logger log = Logger.getLogger(SyncServer.class.getName());

    static {
        FileHandler logHandler;
        try {
            logHandler = new FileHandler("server.log", true);
            logHandler.setFormatter(new SimpleFormatter());
            log.addHandler(logHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            RequestMessage message = (RequestMessage) in.readObject();
            log.info("Received: " + message.toString());

            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            ResponseMessage answer = MessageProcessor.process(message);
            log.info("Sent: " + answer.toString() + "\n");
            out.writeObject(answer);
            out.flush();

            in.close();
            out.close();
//            System.out.println("Client " + socket.getChannel().getRemoteAddress()+" disconnected");
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
