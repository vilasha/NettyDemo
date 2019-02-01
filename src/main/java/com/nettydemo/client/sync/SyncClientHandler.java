package com.nettydemo.client.sync;

import com.nettydemo.client.ClientReceiver;
import com.nettydemo.client.ClientSender;
import com.nettydemo.client.ClientController;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.text.ParseException;
import java.util.logging.Logger;

/**
 * Handler is added to client's inbound pipeline, and processes all
 * incoming messages. This handler accepts only String messages. If server
 * has sent us an object, this object will be encoded with Codec class
 * and split on several messages of fixed length.
 *
 * This class detects if an object was sent (first string will be "compositeMessage"
 * with number of parts), then receives all te parts of the message one by one
 * When number of parts received reaches declared number of parts at
 * compositeMessage command, it retrieves encoded object using Codec class
 */
@ChannelHandler.Sharable
public class SyncClientHandler extends SimpleChannelInboundHandler<String>
                               implements ClientReceiver {

    /**
     * Logger, that will contain all received decoded messages
     */
    private static Logger log;

    static {
        log = Logger.getLogger(SyncClientHandler.class.getName());
        ClientSender.initLogger(log);
    }

    private ClientController controller = new ClientController();

    /**
     * Method is called when client has an inbound message
     * @param ctx channel context
     * @param msg inbound message
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws ParseException {
        log.info(msg);
        controller.processResponse(this, msg);
    }

    public void messageReceived(String msg) {
        // Fixed length output
        int MAX_WIDTH = 160;
        for (int i = 0; i < msg.length(); i += MAX_WIDTH)
            System.out.println(msg.substring(i, Math.min(i + MAX_WIDTH, msg.length())));
    }

    /**
     * Method is called when an exception appeared during reading the channel
     * @param ctx context
     * @param cause exception object
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}