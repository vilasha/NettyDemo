package com.nettydemo.client.sync;

import com.nettydemo.common.Codec;
import com.nettydemo.common.Utils;
import com.nettydemo.common.entities.LoginMessage;
import com.nettydemo.common.entities.RequestMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.oio.OioSocketChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
public class SyncClient {

    /**
     * Host variable an IP of server remote host.
     * Value of this property is taken from "config.properties" file
     */
    private static final String HOST = System.getProperty("host", Utils.getInstance().getHost());
    /**
     * Port where remote server awaits our connection
     */
    private static final int PORT = Utils.getInstance().getPort();

    /**
     * Main client application
     * @param args ignored
     * @throws Exception might be thrown by Netty TCP connection implementation
     */
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new OioEventLoopGroup();

        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group)
                    .channel(OioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new SyncClientInitializer());

            Channel ch = clientBootstrap.connect(HOST, PORT).sync().channel();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            ChannelFuture lastFuture = null;
            for (;;) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                if (line.contains("list")) {
                    Integer size = Integer.parseInt(line.substring(4));
                    List<Integer> content = new ArrayList<>();
                    for (int i = 0; i < size; i++)
                        content.add(i);
                    sendAsComposite(ch, lastFuture, content, "echo");
                } else if("login1".equals(line)) {
                    LoginMessage login = new LoginMessage();
                    login.setLogin("login");
                    login.setPassword("password");
                    sendAsComposite(ch, lastFuture, login, "login");
                } else if("login2".equals(line)) {
                    LoginMessage login = new LoginMessage();
                    login.setLogin("asdfa");
                    login.setPassword("dghjrt4");
                    sendAsComposite(ch, lastFuture, login, "login");
                } else if ("exit".equals(line)) {
                    ch.writeAndFlush(line + "\r\n");
                    ch.closeFuture().sync();
                    break;
                } else {
                    for (int i = 0; i < 5; i++) {
                        lastFuture = ch.writeAndFlush(line + "\r\n");
                        if (lastFuture != null)
                            lastFuture.sync();
                    }
                }
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * If we need to send a large object, we encode it with Codec class,
     * split on messages of fixed length (this length is defined at config.properties
     * file) and then theese fixed length strings are sent to server one by one
     * @param ch open channel of our TCP connection
     * @param lastFuture event listener, that waits confirmation from server, that
     *                   the message have been successfully received
     * @param content object, that needs to be sent to the server
     * @param serviceId command for server, what to do with this object "content". At
     *                  the moment only one command "echo" is supported. To introduce
     *                  new commands please check class MessageProcessor (for business
     *                  logic of the command) and main method of this class for generation
     *                  messages with the new command)
     * @throws InterruptedException is thrown if connection is lost during sending the
     *                  message or awaiting a confirmation from the server
     */
    private static void sendAsComposite(Channel ch, ChannelFuture lastFuture, Object content, String serviceId) throws InterruptedException {
        Codec p = new Codec();
        String[] packed = p.pack(content, content.getClass(), RequestMessage.class, serviceId);
        lastFuture = ch.writeAndFlush("compositeMessage=" + String.valueOf(packed.length) + "\r\n");
        if (lastFuture != null)
            lastFuture.sync();
        for (String item : packed) {
            lastFuture = ch.writeAndFlush(item + "\r\n");
            if (lastFuture != null)
                lastFuture.sync();
        }

    }
}