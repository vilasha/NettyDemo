package com.nettydemo.server;

import com.nettydemo.common.entities.ErrorMessage;
import com.nettydemo.common.entities.LoginMessage;
import com.nettydemo.common.entities.RequestMessage;
import com.nettydemo.common.entities.ResponseMessage;
import com.nettydemo.common.Utils;

import java.net.InetAddress;

/**
 * This class contains all server business logic: what server should
 * do with the received objects.
 * At the moment it has only two commands: echo will copy the inbound
 * object into a ResponseMessage wrapper and send it back to client.
 * "login" retrieves LoginMessage object sent by client and compares
 * credentials with the hardcoded ones. Doesn't actually authenticates
 * the client
 * For wrapping/unwrapping messages this classes uses a class Codec
 */
public abstract class MessageProcessor {

    /**
     * Method detects what command was sent to the server and redirects
     * the message to appropriate private method
     * @param message object with a header
     * @return answer object
     */
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

    /**
     * Method retrieves LoginMessage object sent by client and compares
     * credentials with the hardcoded ones. Doesn't actually authenticates
     * the client
     * @param input
     * @return
     */
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

    /**
     * Method creates an empty ResponseMessage instance
     * @return
     */
    private static ResponseMessage getEmptyMessage() {
        ResponseMessage message = new ResponseMessage();
        message.setResponseCode('S');
        return message;
    }

    /**
     * Copies all the content and most of the header form
     * incoming RequestMessage into outcoming ResponseMessage
     * @param input inbound message
     * @return answer
     */
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
