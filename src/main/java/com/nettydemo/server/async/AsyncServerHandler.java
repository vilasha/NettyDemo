package com.nettydemo.server.async;

import com.nettydemo.common.Codec;
import com.nettydemo.common.entities.RequestMessage;
import com.nettydemo.common.entities.ResponseMessage;
import com.nettydemo.server.MessageProcessor;
import io.netty.channel.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@ChannelHandler.Sharable
public class AsyncServerHandler extends SimpleChannelInboundHandler<String> {

    private static Logger log = Logger.getLogger(AsyncServerHandler.class.getName());

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

    private boolean readingMultiple = false;
    private int packageCounter;
    private String[] multiplePackage;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.write("Connected to " + InetAddress.getLocalHost().getHostName()
                + " at " + new Date()
                + ". Available commands: \"list\", \"login1\", \"login2\", \"bye\"\r\n");
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
        String response;
        ChannelFuture lastFuture;

        if (request.isEmpty()) {
            response = "Please type something.\r\n";
            ctx.write(response);
        } else if ("bye".equals(request.toLowerCase())) {
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

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}