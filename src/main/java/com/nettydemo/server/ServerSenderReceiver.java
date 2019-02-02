package com.nettydemo.server;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Interface for AsyncServerHandler and SyncServerHandler,
 * so ServerService could process messages from either of
 * them, not even knowing which implementation of ServerSenderReceiver
 * is active at the moment
 */
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
