package com.nettydemo.client.async;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * This is a handler, that is added to inbound pipeline only at the
 * beginning to initialize the channel with certain parameters
 * (in our case with fixed frame length). After channel is open
 * and initialized, this handler is removed from pipeline by Netty
 * This class is not mandatory, as we can define unnamed initializer
 * at main client application, on bootstrap lambda (line 65)
 */
public class AsyncClientInitializer extends ChannelInitializer<SocketChannel> {
    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();

    private static final AsyncClientHandler CLIENT_HANDLER = new AsyncClientHandler();
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline
                .addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()))
                .addLast(DECODER)
                .addLast(ENCODER)
                .addLast(CLIENT_HANDLER);
    }
}