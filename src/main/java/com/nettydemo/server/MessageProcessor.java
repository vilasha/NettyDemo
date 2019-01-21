package com.nettydemo.server;

import com.nettydemo.common.RequestMessage;
import com.nettydemo.common.ResponseMessage;
import com.nettydemo.common.Utils;

import java.net.InetAddress;

public abstract class MessageProcessor {
    public static ResponseMessage process(RequestMessage message) {
        ResponseMessage result;
        switch (message.getServiceId()) {
            case "echo": result = getEcho(message);
                         break;
            default: result = getEmptyMessage();
        }
        return result;
    }

    private static ResponseMessage getEmptyMessage() {
        ResponseMessage message = new ResponseMessage();
        message.setResponseCode('S');
        return message;
    }

    private static ResponseMessage getEcho(RequestMessage input) {
        ResponseMessage message = new ResponseMessage();
        try {
            message.setSenderIp(InetAddress.getLocalHost().getHostAddress());
            message.setServiceId(input.getServiceId());
            message.setMessageGuid(Utils.getInstance().getNextMessageId());
            message.setResponseTime(Utils.getInstance().getCurrentDateTime());
            message.setMessageBody(input.getMessageBody());
            message.setMessageLength(message.toString().length());
            message.setResponseCode('S');
        } catch (Exception ex) {
            message.setResponseCode('F');
        }
        return message;
    }
}
