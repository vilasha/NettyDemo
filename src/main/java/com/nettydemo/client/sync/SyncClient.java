package com.nettydemo.client.sync;

import com.nettydemo.client.ClientSender;
import com.nettydemo.client.ClientController;
import com.nettydemo.common.Utils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.oio.OioSocketChannel;

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
 * "list10" will create an array list with 10 elements and send it to server
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
 *
 * Class uses deprecated syncronized connection Oio (please check results of
 * Performance test to see why it is deprecated: the sending of objects is much slower)
 */
@SuppressWarnings("deprecation")
public class SyncClient implements ClientSender {

    private ClientController controller = new ClientController();
    private Channel channel;
    private ChannelFuture lastFuture;

    /**
     * Main client application
     * @param args ignored
     * @throws Exception might be thrown by Netty TCP connection implementation
     */
    public static void main(String[] args) throws Exception {
        SyncClient client = new SyncClient();
        client.start();
    }

    private void start() throws IOException, InterruptedException {
        EventLoopGroup group = new OioEventLoopGroup();

        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group)
                    .channel(OioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new SyncClientInitializer());

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
                    controller.doLogin(this,"fghfghj", "2456rgjh");
                else if ("exit".equals(line)) {
                    controller.sendExit(this);
                    channel.closeFuture().sync();
                    break;
                } else {
                    for (int i = 0; i < 5; i++)
                        controller.sendInfo(this, line);
                }
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public ChannelFuture getLastFuture() {
        return lastFuture;
    }
}