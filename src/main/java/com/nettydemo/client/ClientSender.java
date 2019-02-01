package com.nettydemo.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public interface ClientSender {
    Channel getChannel();
    ChannelFuture getLastFuture();

    static void initLogger(Logger log) {
        FileHandler logHandler;
        try {
            logHandler = new FileHandler("client.log", true);
            logHandler.setFormatter(new SimpleFormatter());
            log.addHandler(logHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
