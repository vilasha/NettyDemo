package com.nettydemo.server.sync;

import com.nettydemo.common.Utils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Server application entry point. It opens a TCP channel on
 * a port (this port is defined in config.properties file) and
 * awaits for clients to connect
 */
@SuppressWarnings("deprecation")
public class SyncServer {

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new OioEventLoopGroup(1);
        EventLoopGroup workGroup = new OioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(OioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new SyncServerInitializer());
            Channel ch = serverBootstrap.bind(Utils.getInstance().getPort())
                    .sync().channel();
            System.out.println("Waiting for clients");
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}