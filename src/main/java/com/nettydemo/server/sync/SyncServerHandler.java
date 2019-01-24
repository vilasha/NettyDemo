package com.nettydemo.server.sync;

import com.nettydemo.common.Codec;
import com.nettydemo.common.entities.RequestMessage;
import com.nettydemo.common.entities.ResponseMessage;
import com.nettydemo.server.MessageProcessor;
import io.netty.channel.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.logging.*;

/**
 * Handler is added to server's inbound pipeline, and processes all
 * incoming messages. This handler accepts only String messages. If a client
 * has sent us an object, this object will be encoded with Codec class
 * and split on several messages of fixed length.
 *
 * This class detects if an object was sent (first string will be "compositeMessage"
 * with number of parts), then receives all te parts of the message one by one
 * When number of parts received reaches declared number of parts at
 * compositeMessage command, it retrieves encoded object using Codec class
 *
 * If a command "exit" received, server closes the connection
 *
 * Log file is opened only when connection with a client is established
 * and when client disconnects, log file is closed
 */
@ChannelHandler.Sharable
public class SyncServerHandler extends SimpleChannelInboundHandler<String> {

    /**
     * Logger, that will contain all received decoded messages
     */
    private static Logger log;

    /**
     * Indicates if received strings are parts of a large encoded object
     */
    private boolean readingMultiple = false;
    /**
     * Indicates number of the part
     */
    private int packageCounter;
    /**
     * Contains all the parts received so far
     */
    private String[] multiplePackage;

    /**
     * Method is called when a client connects to the channel
     * It opens a log file and sends to the clients welcome message
     * (in our case list of available instructions)
     * @param ctx channel context
     * @throws Exception can be thrown by Netty or log file writing
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
        log = Logger.getLogger(SyncServerHandler.class.getName());
        FileHandler logHandler;
        try {
            logHandler = new FileHandler("server.log", true);
            logHandler.setFormatter(new SimpleFormatter());
            log.addHandler(logHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ctx.write("Connected to " + InetAddress.getLocalHost().getHostName()
                + " at " + new Date()
                + ". Available commands: \"list10\"(instead of 10 can be any other integer)," +
                " \"login1\", \"login2\", \"exit\"\r\n");
        ctx.flush();
    }

    /**
     * Method is called when there is an inbound message from the client
     * It defines what server should do with the message. If it is an empty
     * string it answers "Please type something", if an object was sent
     * (first string will be "compositeMessage" with number of parts), then
     * receives all te parts of the message one by one.  When number of parts
     * received reaches declared number of parts at compositeMessage command,
     * it retrieves encoded object using Codec class
     * If the command was "exit", server closes the connection with the client
     * If server receives an unknown command, it asks "Did you say " + message
     * @param ctx channel context
     * @param request message
     * @throws Exception can be thrown by Netty
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
        String response;
        ChannelFuture lastFuture;

        if (request.isEmpty()) {
            response = "Please type something.\r\n";
            ctx.write(response);
        } else if ("exit".equals(request.toLowerCase())) {
            response = "Closing connection. Please wait\r\n";
            lastFuture = ctx.write(response);
            lastFuture.addListener(ChannelFutureListener.CLOSE);
        } else if(request.toLowerCase().contains("compositemessage")) {
            int totalPackages = Integer.parseInt(request.substring(request.indexOf('=')+1));
            packageCounter = 0;
            readingMultiple = true;
            multiplePackage = new String[totalPackages];
//            System.out.println("composite message " + totalPackages);
        } else if (readingMultiple) {
            if (packageCounter < multiplePackage.length - 1) {
                multiplePackage[packageCounter] = request;
                packageCounter++;
            } else {
                multiplePackage[packageCounter] = request;
                readingMultiple = false;
                Codec p = new Codec();
                RequestMessage message = (RequestMessage) p.unpack(multiplePackage);

                log.info("Received a message: " + message.toString());
                log.info("Content type: " + message.getMessageBodyType().getName()
                        + "; value: " + message.getMessageBody().toString());

                ResponseMessage answer = MessageProcessor.process(message);
                String[] packed = p.packWithoutWrapping(answer);
                lastFuture = ctx.writeAndFlush("compositeMessage=" + String.valueOf(packed.length) + "\r\n");
                if (lastFuture != null)
                    lastFuture.sync();
                for (String item : packed) {
                    lastFuture = ctx.writeAndFlush(item + "\r\n");
                    if (lastFuture != null)
                        lastFuture.sync();
                }
            }
        } else {
            response = "Did you say '" + request + "'?\r\n";
            ctx.write(response);
        }
    }

    /**
     * Method is called when all the messages from the channel are read
     * @param ctx
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * Method is called when client disconnects. It closes log file
     * @param ctx channel context
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();
        for(Handler h : log.getHandlers())
            h.close();
    }

    /**
     * Method for exception handling. Just closes log file and writes
     * the exception stack trace to console
     * @param ctx channel context
     * @param cause exception object
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        for(Handler h : log.getHandlers())
            h.close();
        LogManager.getLogManager().reset();
        cause.printStackTrace();
        ctx.close();
    }
}