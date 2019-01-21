package com.nettydemo.client.async;

import com.nettydemo.common.Utils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * This is the startup code that configures the client
 */
public class AsyncClient {
    public void start() throws InterruptedException {
        System.out.println("Starting client");
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    // Specify the use of an NIO transport Channel
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(Utils.getInstance().getHost(), Utils.getInstance().getPort()))
                    // Add an AsyncClientHandler to the Channel’s pipeline
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(new AsyncClientHandler());
                        }
                    });
            // Bind the server asynchronously; sync() waits for the bind to complete
            ChannelFuture future = bootstrap.connect().sync();
            // Get the CloseFuture of the Channel and block the current thread until it’s complete
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Shut down the EventLoopGroup, releasing all resources
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new AsyncClient().start();
    }
}