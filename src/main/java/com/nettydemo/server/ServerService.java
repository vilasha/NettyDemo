package com.nettydemo.server;

import com.nettydemo.common.Utils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Business-logic of server side of application
 * It receives fixed-length offset based messages from client,
 * decodes them and depending on the message content gives an
 * answer. For "login" command it compares credentials to
 * "login" + "password", if login-password are correct, sends
 * a big quote about Shakespeare (quote is too big to go into
 * one message, so it will be split on several messages),
 * otherwise server produces an error message
 * For "info" command it parses the message and answers "ok"
 * For "exit" command it parses the message and closes the
 * connectio with the client
 */
public class ServerService {

    /**
     * Method distributes messages from client between methods
     * parseLogin, parseInfo or closes the connection with the client
     * @param server who called this method (AsyncServerHandler or SyncServerHandler)
     * @param msg message from the client
     * @throws ParseException inherited from parseLogin and parseInfo methods
     * @throws InterruptedException inherited from parseLogin and parseInfo methods
     * @throws UnknownHostException inherited from parseLogin and parseInfo methods
     */
    public void processRequest(ServerSenderReceiver server, String msg) throws ParseException, InterruptedException, UnknownHostException {
        String serviceId = msg.substring(23, 43).toLowerCase().trim();
        switch (serviceId) {
            case "login":  parseLogin(server, msg);
                break;
            case "exit": sendInfo(server, "Wait for connection to close")
                    .addListener(ChannelFutureListener.CLOSE);
                break;
            case "info": parseInfo(server, msg);
                break;
        }
    }

    /**
     * Sends a welcome message to client once a connection was established
     * @param server who called this method (AsyncServerHandler or SyncServerHandler)
     * @throws UnknownHostException inherited from sendInfo method
     * @throws InterruptedException inherited from sendInfo method
     */
    public void doWelcome(ServerSenderReceiver server) throws UnknownHostException, InterruptedException {
        sendInfo(server, "Connected to " + InetAddress.getLocalHost().getHostName()
                + " at " + new Date() + ". "
                + "Available commands: \"login1\", \"login2\", \"exit\"");
    }

    /**
     * Method packs information for a client into one or several messages
     * and sends them one by one
     * @param server who called this method (AsyncServerHandler or SyncServerHandler)
     * @param message info to send
     * @return ChannelFuture to wait till message sending was succesfully finished
     * @throws UnknownHostException can be thrown by InetAddress.getLocalHost()
     * @throws InterruptedException can be thrown by ChannelFuture.sync()
     */
    private ChannelFuture sendInfo(ServerSenderReceiver server, String message) throws InterruptedException, UnknownHostException {
        ChannelHandlerContext ctx = server.getContext();
        int messageCount = (int)Math.ceil((double)message.length() / 200);
        int messageLen = messageCount * 279;
        ChannelFuture future = null;
        for (int i = 0; i < messageCount; i++) {
            String msg = message.substring(0, Math.min(200, message.length()));
            message = message.substring(Math.min(200, message.length()));
            StringBuilder sb = new StringBuilder();
            // Message length, size 8
            sb.append(String.format("%08d", messageLen));
            // Sender IP, size 15
            sb.append(String.format("%" + 15 + "s", InetAddress.getLocalHost().getHostAddress()));
            // Service ID, size 20
            sb.append(String.format("%" + 20 + "s", "info"));
            // Message GUID, size 20
            sb.append(String.format("%" + 20 + "s", Utils.getInstance().getNextMessageId()));
            // Request time, size 15
            sb.append(String.format("%015d", Utils.getInstance().getCurrentDateTime()));
            // Response code, size 1
            sb.append("S");
            // Info, size 200
            sb.append(String.format("%" + 200 + "s", msg));
            future = ctx.writeAndFlush(sb.toString() + "\r\n");
            future.sync();
        }
        return future;
    }

    /**
     * Method packs (adds header, makes fixed length) an error
     * @param server who called this method (AsyncServerHandler or SyncServerHandler)
     * @param errorCode 8 symbol code of the error
     * @param errorDescription long description of the error
     * @throws UnknownHostException can be thrown by InetAddress.getLocalHost()
     * @throws InterruptedException can be thrown by ChannelFuture.sync()
     */
    private void sendError(ServerSenderReceiver server, String errorCode, String errorDescription) throws UnknownHostException, InterruptedException {
        ChannelHandlerContext ctx = server.getContext();
        StringBuilder sb = new StringBuilder();
        // Message length, size 8
        sb.append(String.format("%08d", 287));
        // Sender IP, size 15
        sb.append(String.format("%" + 15 + "s", InetAddress.getLocalHost().getHostAddress()));
        // Service ID, size 20
        sb.append(String.format("%" + 20 + "s", "error"));
        // Message GUID, size 20
        sb.append(String.format("%" + 20 + "s", Utils.getInstance().getNextMessageId()));
        // Request time, size 15
        sb.append(String.format("%015d", Utils.getInstance().getCurrentDateTime()));
        // Response code, size 1
        sb.append("F");
        // Error code, size 8
        sb.append(String.format("%" + 8 + "s", errorCode));
        // Error description, size 200
        sb.append(String.format("%" + 200 + "s", errorDescription));
        ChannelFuture future = ctx.writeAndFlush(sb.toString() + "\r\n");
        future.sync();
    }

    /**
     * Method parses inbound message from client with "login" command.
     * Then it compares credentials to "login" + "password", if login-password are correct,
     * sends a big quote about Shakespeare (quote is too big to go into one message,
     * so it will be split on several messages), otherwise server produces an error message
     * @param server who called this method (AsyncServerHandler or SyncServerHandler)
     * @param msg credential from client
     * @throws ParseException can be thrown by SimpleDateFormat.parse
     * @throws UnknownHostException can be thrown by InetAddress.getLocalHost()
     * @throws InterruptedException inherited from sendInfo and sendError methods
     */
    private void parseLogin(ServerSenderReceiver server, String msg) throws ParseException, UnknownHostException, InterruptedException {
        long messageLen = Long.parseLong(msg.substring(0, 8).trim());
        String senderIP = msg.substring(8, 23).trim();
        String serviceId = msg.substring(23, 43).trim();
        String messageGuid = msg.substring(43, 63).trim();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmsss");
        Date responseTime = format.parse(msg.substring(63, 78).trim());
        String login = msg.substring(78, 98).trim();
        String password = msg.substring(98).trim();
        if ("login".equals(login) && "password".equals(password))
            sendInfo(server,
                    "About 150 years after his death, questions arose about the authorship of William " +
                            "Shakespeare's plays. Scholars and literary critics began to float names like " +
                            "Christopher Marlowe, Edward de Vere and Francis Bacon — men of more known " +
                            "backgrounds, literary accreditation, or inspiration — as the true authors of " +
                            "the plays. Much of this stemmed from the sketchy details of Shakespeare's life " +
                            "and the dearth of contemporary primary sources. Official records from the Holy " +
                            "Trinity Church and the Stratford government record the existence of a William " +
                            "Shakespeare, but none of these attest to him being an actor or playwright."
                    );
        else
            sendError(server, "Err001", "Incorrect login or password");
    }

    /**
     * Parses an info message from a client and answers "ok"
     * @param server who called this method (AsyncServerHandler or SyncServerHandler)
     * @param msg info from the client
     * @throws ParseException can be thrown by SimpleDateFormat.parse
     * @throws UnknownHostException can be thrown by InetAddress.getLocalHost()
     * @throws InterruptedException inherited from sendInfo method
     */
    private void parseInfo(ServerSenderReceiver server, String msg) throws ParseException, UnknownHostException, InterruptedException {
        long messageLen = Long.parseLong(msg.substring(0, 8).trim());
        String senderIP = msg.substring(8, 23).trim();
        String serviceId = msg.substring(23, 43).trim();
        String messageGuid = msg.substring(43, 63).trim();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmsss");
        Date responseTime = format.parse(msg.substring(63, 78).trim());
        String info = msg.substring(78).trim();
        sendInfo(server, "ok");
    }

}