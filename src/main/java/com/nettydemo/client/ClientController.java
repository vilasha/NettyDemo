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

/**
 * Main business-logic of client application: encodes client's
 * commands into a fixed-length offset based messages with a header
 * (meta-information like message id, request time, etc)
 * And also decodes response from server, if server's response was split
 * into several messages, accumulates them in messageQueue, and when all
 * the parts of the message were received, sends it to ClientReceiver
 * implementation (AsyncClientHandler or SyncClientHandler)
 */
public class ClientController {

    /**
     * Storage for partly-received multi-message server responses
     * Key is senderIP + ":" + serviceId, value - concatenated
     * messages
     */
    private Map<String, String> messageQueue = new HashMap<>();
    /**
     * For previous queue this map keeps length of messages
     * already received (including headers)
     */
    private Map<String, Long> messageLenInQueue = new HashMap<>();

    /**
     * Method creates a login message
     * @param clientSender ClientSender implementation (AsyncClient or
     *                     SyncClient), which called this method
     * @param login value of login field
     * @param password value of password field
     * @throws UnknownHostException is thrown if getLocalHost() doesn't know localhost address
     * @throws InterruptedException is thrown if sending to server via Netty was interrupted
     */
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

    /**
     * Method sends any info to server
     * @param clientSender ClientSender implementation (AsyncClient or
     *      *                     SyncClient), which called this method
     * @param info information to send to server
     * @throws UnknownHostException is thrown if getLocalHost() doesn't know localhost address
     * @throws InterruptedException is thrown if sending to server via Netty was interrupted
     */
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

    /**
     * Method sends "exit" command to server, which enables him to close current connection
     * @param clientSender ClientSender implementation (AsyncClient or
     *      *                     SyncClient), which called this method
     * @throws UnknownHostException is thrown if getLocalHost() doesn't know localhost address
     * @throws InterruptedException is thrown if sending to server via Netty was interrupted
     */
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

    /**
     * Method actually sends fixed length message (created in previous methods)
     * to server using Netty
     * @param clientSender ClientSender implementation (AsyncClient or
     *                     SyncClient), which called this method
     * @param fixedLengthMessage message to send to server
     * @throws InterruptedException is thrown if sending to server via Netty was interrupted
     */
    private void sendToServer(ClientSender clientSender, String fixedLengthMessage) throws InterruptedException {
        ChannelFuture lastFuture = clientSender.getLastFuture();
        lastFuture = clientSender.getChannel().writeAndFlush(fixedLengthMessage + "\r\n");
        syncFuture(lastFuture);
    }

    /**
     * Method forses client to wait for a confirmation, that message was succesfully
     * sent using TCP/IP
     * @param lastFuture last channel future (delayed response from server)
     * @throws InterruptedException is thrown if sending to server via Netty was interrupted
     */
    private void syncFuture(ChannelFuture lastFuture) throws InterruptedException {
        if (lastFuture != null)
            lastFuture.sync();
    }

    /**
     * Method redirect server's response depending on service id to parseInfo method
     * or to parseError
     * @param client ClientReceiver implementation (AsyncClientHandler or SyncClientHandler)
     * @param msg response from server
     * @throws ParseException inherited from parseInfo and parseError methods
     */
    public void processResponse(ClientReceiver client, String msg) throws ParseException {
        String serviceId = msg.substring(23, 43).toLowerCase().trim();
        switch (serviceId) {
            case "info":  parseInfo(client, msg);
                          break;
            case "error": parseError(client, msg);
                          break;
        }
    }

    /**
     * Method parses response from server if serviceId was "info"
     * If server's response was split into several messages, accumulates them in messageQueue,
     * and when all the parts of the message were received, sends the complete message
     * to ClientReceiver implementation (AsyncClientHandler or SyncClientHandler)
     * @param client ClientReceiver implementation (AsyncClientHandler or SyncClientHandler)
     * @param msg response from server
     * @throws ParseException can be thrown by SimpleDateFormat.parse
     */
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

    /**
     * Method parses response from server if serviceId was "error"
     * @param client ClientReceiver implementation (AsyncClientHandler or SyncClientHandler)
     * @param msg response from server
     * @throws ParseException can be thrown by SimpleDateFormat.parse
     */
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