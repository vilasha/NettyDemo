package com.nettydemo.client.async;

import com.nettydemo.common.Packer;
import com.nettydemo.common.entities.LoginMessage;
import com.nettydemo.common.entities.RequestMessage;
import com.nettydemo.common.Utils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AsyncClient {

    private static final String HOST = System.getProperty("host", Utils.getInstance().getHost());
    private static final int PORT = Utils.getInstance().getPort();

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new AsyncClientInitializer());

            Channel ch = clientBootstrap.connect(HOST, PORT).sync().channel();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            ChannelFuture lastFuture = null;
            for (;;) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                if ("list".equals(line)) {
                    List<Integer> content = new ArrayList<>();
                    for (int i = 0; i < 1000; i++)
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
                } else if ("bye".equals(line)) {
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

    private static void sendAsComposite(Channel ch, ChannelFuture lastFuture, Object content, String serviceId) throws InterruptedException {
        Packer p = new Packer();
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