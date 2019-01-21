package com.nettydemo.server.async;

import com.nettydemo.common.Utils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * This is the startup code that configures the server.
 * At a minimum, it binds the server to the port
 * on which it will listen for connection requests
 */
public class AsyncServer {
    public void start() throws InterruptedException {
        final AsyncServerHandler serverHandler = new AsyncServerHandler();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            System.out.println("Waiting for clients");
            bootstrap.group(group)
                    // Specify the use of an NIO transport Channel
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(Utils.getInstance().getPort()))
                    // Add an AsyncServerHandler to the Channel’s pipeline
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) {
                            // AsyncServerHandler is @Sharable so we can always use the same one
                            socketChannel.pipeline().addLast(serverHandler);
                        }
                    });
            // Bind the server asynchronously; sync() waits for the bind to complete
            ChannelFuture future = bootstrap.bind().sync();
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
        new AsyncServer().start();
    }
}