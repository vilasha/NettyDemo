package com.nettydemo.client.sync;

import com.nettydemo.client.MessageGenerator;
import com.nettydemo.common.RequestMessage;
import com.nettydemo.common.ResponseMessage;
import com.nettydemo.common.Utils;

import java.io.*;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SyncClient {

    private static Logger log = Logger.getLogger(SyncClient.class.getName());

    static {
        FileHandler logHandler;
        try {
            logHandler = new FileHandler("client.log", true);
            logHandler.setFormatter(new SimpleFormatter());
            log.addHandler(logHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Starting client");
        Socket socket = new Socket(Utils.getInstance().getHost(), Utils.getInstance().getPort());

        RequestMessage message = MessageGenerator.generateMessage();

        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        out.writeObject(message);
        out.flush();
        log.info("Sent: " + message.toString());
        System.out.println("Message has been sent");

        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        ResponseMessage answer = (ResponseMessage) in.readObject();
        log.info("Received: " + answer.toString() + "\n");
    }
}
