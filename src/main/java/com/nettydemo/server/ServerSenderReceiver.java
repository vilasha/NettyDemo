package com.nettydemo.server;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public interface ServerSenderReceiver {

    ChannelHandlerContext getContext();

    static void initLogger(Logger log) {
        FileHandler logHandler;
        try {
            logHandler = new FileHandler("server.log", true);
            logHandler.setFormatter(new SimpleFormatter());
            log.addHandler(logHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
