package com.nettydemo.client.async;

import com.nettydemo.common.Packer;
import com.nettydemo.common.entities.ResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@ChannelHandler.Sharable
public class AsyncClientHandler extends SimpleChannelInboundHandler<String> {

    private static Logger log = Logger.getLogger(AsyncClientHandler.class.getName());

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

    private boolean readingMultiple = false;
    private int packageCounter;
    private String[] multiplePackage;

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
                Packer p = new Packer();
                ResponseMessage message = (ResponseMessage) p.unpack(multiplePackage);
                log.info("Received a message: " + message.toString());
                log.info("Content type: " + message.getMessageBodyType().getName()
                        + "; value: " + message.getMessageBody().toString());
            }
        } else
            System.out.println(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}