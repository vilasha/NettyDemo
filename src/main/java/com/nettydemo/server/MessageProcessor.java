package com.nettydemo.server;

import com.nettydemo.common.entities.ErrorMessage;
import com.nettydemo.common.entities.LoginMessage;
import com.nettydemo.common.entities.RequestMessage;
import com.nettydemo.common.entities.ResponseMessage;
import com.nettydemo.common.Utils;

import java.net.InetAddress;

public abstract class MessageProcessor {
    public static ResponseMessage process(RequestMessage message) {
        ResponseMessage result;
        switch (message.getServiceId()) {
            case "echo": result = getEcho(message);
                         break;
            case "login": result = tryLogin(message);
                          break;
            default: result = getEmptyMessage();
        }
        return result;
    }

    private static ResponseMessage tryLogin(RequestMessage input) {
        ResponseMessage message = getEcho(input);
        if (input.getMessageBodyType().getSimpleName().equals(LoginMessage.class)) {
            ErrorMessage err = new ErrorMessage();
            err.setErrorReason("WRONG_FORMAT");
            err.setErrorCodeDetail("Incorrect format for RequestMessage body:" +
                    " expected \"LoginMessage\", received \"" + message.getMessageBodyType() + "\"");
            message.setMessageBody(err);
            message.setMessageBodyType(ErrorMessage.class);
            message.setResponseCode('F');
        } else {
            LoginMessage login = (LoginMessage) message.getMessageBody();
            if (login.getLogin().equals("login") && login.getPassword().equals("password")) {
                message.setResponseCode('S');
                message.setMessageBodyType(String.class);
                message.setMessageBody("Login successful");
            } else {
                ErrorMessage err = new ErrorMessage();
                err.setErrorReason("WRONG_LOGIN");
                err.setErrorCodeDetail("Incorrect login or password");
                message.setMessageBody(err);
                message.setMessageBodyType(ErrorMessage.class);
                message.setResponseCode('F');
            }
        }
        return message;
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
            message.setMessageBodyType(input.getMessageBodyType());
            message.setResponseCode('S');
        } catch (Exception ex) {
            message.setResponseCode('F');
        }
        return message;
    }
}
