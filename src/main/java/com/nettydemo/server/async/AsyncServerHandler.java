package com.nettydemo.server.async;

import com.nettydemo.server.ServerSenderReceiver;
import com.nettydemo.server.ServerService;
import com.nettydemo.server.sync.SyncServerHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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
public class AsyncServerHandler extends SimpleChannelInboundHandler<String>
                                implements ServerSenderReceiver {

    /**
     * Logger, that will contain all received decoded messages
     */
    private static Logger log;

    private ServerService service = new ServerService();
    private ChannelHandlerContext ctx;

    /**
     * Method is called when a client connects to the channel
     * It opens a log file and sends to the clients welcome message
     * (in our case list of available instructions)
     * @param context channel context
     * @throws Exception can be thrown by Netty or log file writing
     */
    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        this.ctx = context;
        ctx.fireChannelActive();
        log = Logger.getLogger(SyncServerHandler.class.getName());
        ServerSenderReceiver.initLogger(log);
        service.doWelcome(this);
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
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String request) throws ParseException, InterruptedException, UnknownHostException {
        log.info(replacePassword(request));
        service.processRequest(this, request);
    }

    private String replacePassword(String msg) {
        String serviceId = msg.substring(23, 43).toLowerCase().trim();
        if ("login".equals(serviceId))
            return msg.substring(0, 98) + "*****";
        else
            return msg;
    }

    /**
     * Method is called when all the messages from the channel are read
     * @param ctx channel context
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

    @Override
    public ChannelHandlerContext getContext() {
        return ctx;
    }
}