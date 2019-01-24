package com.nettydemo.client.sync;

import com.nettydemo.common.Codec;
import com.nettydemo.common.entities.ResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Handler is added to client's inbound pipeline, and processes all
 * incoming messages. This handler accepts only String messages. If server
 * has sent us an object, this object will be encoded with Codec class
 * and split on several messages of fixed length.
 *
 * This class detects if an object was sent (first string will be "compositeMessage"
 * with number of parts), then receives all te parts of the message one by one
 * When number of parts received reaches declared number of parts at
 * compositeMessage command, it retrieves encoded object using Codec class
 */
@ChannelHandler.Sharable
public class SyncClientHandler extends SimpleChannelInboundHandler<String> {

    /**
     * Logger, that will contain all received decoded messages
     */
    private static Logger log = Logger.getLogger(SyncClientHandler.class.getName());

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
     * Method is called when client has an inbound message
     * @param ctx channel context
     * @param msg inbound message
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        if(msg.toLowerCase().contains("compositemessage")) {
            int totalPackages = Integer.parseInt(msg.substring(msg.indexOf('=')+1));
            packageCounter = 0;
            readingMultiple = true;
            multiplePackage = new String[totalPackages];
//            System.out.println("composite message " + totalPackages);
        } else if (readingMultiple) {
            if (packageCounter < multiplePackage.length - 1) {
                multiplePackage[packageCounter] = msg;
                packageCounter++;
            } else {
                multiplePackage[packageCounter] = msg;
                readingMultiple = false;
                Codec p = new Codec();
                ResponseMessage message = (ResponseMessage) p.unpack(multiplePackage);
                log.info("Received a message: " + message.toString());
                System.out.println("Message header: " + message.toString());
                log.info("Content type: " + message.getMessageBodyType().getName()
                        + "; value: " + message.getMessageBody().toString());
            }
        } else
            System.out.println(msg);
    }

    /**
     * Method is called when an exception appeared during reading the channel
     * @param ctx context
     * @param cause exception object
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}