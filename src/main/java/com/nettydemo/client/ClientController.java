package com.nettydemo.client;

import com.nettydemo.common.Utils;
import io.netty.channel.ChannelFuture;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ClientController {

    private Map<String, String> messageQueue = new HashMap<>();
    private Map<String, Long> messageLenInQueue = new HashMap<>();

    public void doLogin(ClientSender clientSender, String login, String password) throws UnknownHostException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        // Message length, size 8
        sb.append(String.format("%08d", 118));
        // Sender IP, size 15
        sb.append(String.format("%" + 15 + "s", InetAddress.getLocalHost().getHostAddress()));
        // Service ID, size 20
        sb.append(String.format("%" + 20 + "s", "login"));
        // Message GUID, size 20
        sb.append(String.format("%" + 20 + "s", Utils.getInstance().getNextMessageId()));
        // Request time, size 15
        sb.append(String.format("%015d", Utils.getInstance().getCurrentDateTime()));
        // Login, size 20
        sb.append(String.format("%" + 20 + "s", login));
        // Password, size 20
        sb.append(String.format("%" + 20 + "s", password));
        sendToServer(clientSender, sb.toString());
    }

    public void sendInfo(ClientSender clientSender, String info) throws UnknownHostException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        // Message length, size 8
        sb.append(String.format("%08d", 278));
        // Sender IP, size 15
        sb.append(String.format("%" + 15 + "s", InetAddress.getLocalHost().getHostAddress()));
        // Service ID, size 20
        sb.append(String.format("%" + 20 + "s", "info"));
        // Message GUID, size 20
        sb.append(String.format("%" + 20 + "s", Utils.getInstance().getNextMessageId()));
        // Request time, size 15
        sb.append(String.format("%015d", Utils.getInstance().getCurrentDateTime()));
        // Info, size 200
        sb.append(String.format("%" + 200 + "s", info));
        sendToServer(clientSender, sb.toString());
    }

    public void sendExit(ClientSender clientSender) throws UnknownHostException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        // Message length, size 8
        sb.append(String.format("%08d", 78));
        // Sender IP, size 15
        sb.append(String.format("%" + 15 + "s", InetAddress.getLocalHost().getHostAddress()));
        // Service ID, size 20
        sb.append(String.format("%" + 20 + "s", "exit"));
        // Message GUID, size 20
        sb.append(String.format("%" + 20 + "s", Utils.getInstance().getNextMessageId()));
        // Request time, size 15
        sb.append(String.format("%015d", Utils.getInstance().getCurrentDateTime()));
        sendToServer(clientSender, sb.toString());
        clientSender.getChannel().closeFuture().sync();
    }

    private void sendToServer(ClientSender clientSender, String fixedLengthMessage) throws InterruptedException {
        ChannelFuture lastFuture = clientSender.getLastFuture();
        lastFuture = clientSender.getChannel().writeAndFlush(fixedLengthMessage + "\r\n");
        syncFuture(lastFuture);
    }
    
    private void syncFuture(ChannelFuture lastFuture) throws InterruptedException {
        if (lastFuture != null)
            lastFuture.sync();
    }

    public void processResponse(ClientReceiver client, String msg) throws ParseException {
        String serviceId = msg.substring(23, 43).toLowerCase().trim();
        switch (serviceId) {
            case "info":  parseInfo(client, msg);
                          break;
            case "error": parseError(client, msg);
                          break;
        }
    }

    private void parseInfo(ClientReceiver client, String msg) throws ParseException {
        /*        8             15                  20                  20             151                  200
         * MessLeng       SenderIP           ServiceID         MessageGuid        ResTimeResCode            Info message */
        long messageLen = Long.parseLong(msg.substring(0, 8).trim());
        String senderIP = msg.substring(8, 23).trim();
        String serviceId = msg.substring(23, 43).trim();
        String messageGuid = msg.substring(43, 63).trim();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmsss");
        Date responseTime = format.parse(msg.substring(63, 78).trim());
        char responseCode = msg.charAt(78);
        String infoMessage = msg.substring(79).trim();
        String key = senderIP + ":" + serviceId;
        messageQueue.put(key, messageQueue.getOrDefault(key,"") + infoMessage);
        messageLenInQueue.put(key, messageLenInQueue.getOrDefault(key, 0L) + 279);
        if (messageLen <= messageLenInQueue.get(key)) { // 79 is message header, we don't keep it
            client.messageReceived(messageQueue.get(key));
            messageQueue.remove(key);
            messageLenInQueue.remove(key);
        }
    }

    private void parseError(ClientReceiver client, String msg) throws ParseException {
        /*        8             15                  20                  20            15 1       8                  200
         * MessLeng       SenderIP           ServiceID         MessageGuid       ResTime ResCode ErrCode    ErrorDetail */
        long messageLen = Long.parseLong(msg.substring(0, 8).trim());
        String senderIP = msg.substring(8, 23).trim();
        String serviceId = msg.substring(23, 43).trim();
        String messageGuid = msg.substring(43, 63).trim();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmsss");
        Date responseTime = format.parse(msg.substring(63, 78).trim());
        char responseCode = msg.charAt(78);
        String errorCode = msg.substring(79, 87);
        String errorDetail = msg.substring(87);
        client.messageReceived(errorCode.trim() + ": " + errorDetail.trim());
    }

}