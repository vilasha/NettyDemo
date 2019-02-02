package com.nettydemo.client.async;

import com.nettydemo.client.ClientSender;
import com.nettydemo.client.ClientController;
import com.nettydemo.common.Utils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Client application entry point. It connects to an open TCP channel
 * on a host and port defined in "config.properties" file
 * (if there is no open channel on this host:port, app shuts down with an exception)
 *
 * And then waits commands from user on standard input (keyboard)
 *
 * "login1" and "login2" will create a LoginMessage object, login1 will pass
 * on the server, login2 will fail
 *
 * "exit" will close the channel and stop the application
 *
 * any other string won't be wrapped with header, but simply will be sent to
 * the server as a string
 *
 * To change reaction of the server on these commands or introduce other commands
 * please check class MessageProcessor
 */
public class AsyncClient implements ClientSender {

    private ClientController controller = new ClientController();
    private Channel channel;
    private ChannelFuture lastFuture;

    /**
     * Main client application
     * @param args ignored
     * @throws Exception might be thrown by Netty TCP connection implementation
     */
    public static void main(String[] args) throws Exception {
        AsyncClient client = new AsyncClient();
        client.start();
    }

    /**
     * Non-static version of main application
     * @throws IOException inherited from ClientController methods
     * @throws InterruptedException inherited from ClientController methods
     */
    private void start() throws IOException, InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new AsyncClientInitializer());

            channel = clientBootstrap.connect(
                    System.getProperty("host", Utils.getInstance().getHost()),
                    Utils.getInstance().getPort())
                    .sync().channel();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            lastFuture = null;
            for (;;) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                if("login1".equals(line))
                    controller.doLogin(this,"login", "password");
                else if("login2".equals(line))
                    controller.doLogin(this,"asdf", "qwerty");
                else if ("exit".equals(line)) {
                    controller.sendExit(this);
                    break;
                } else
                    controller.sendInfo(this, line);
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * Returns open channel. It's mainly for ClientController could write in it
     * @return channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Returns last future (delayed response from server), so ClientController
     * could sync() it, i.e. wait till previous message was succesfully sent
     * @return channel future
     */
    public ChannelFuture getLastFuture() {
        return lastFuture;
    }
}