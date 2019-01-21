package com.nettydemo.common;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Packer {

    private static int messageLen;

    static {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("config.properties"));
            messageLen = Integer.parseInt(properties.getProperty("messageLen"));
        } catch (IOException e) {
            e.printStackTrace();
            messageLen = 500;
        }
    }

    private String serviceId;

    public String[] pack(Object content, Class contentType,
                                Class messageType, String serviceId) {
        // Convert content to a String with Base64 encoding
        String encodedContent = convertToString(content);

        // Create header for all the messages
        if (messageType.equals(RequestMessage.class)) {
            RequestMessage empty = new RequestMessage();
            try {
                empty.setSenderIp(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            empty.setServiceId(serviceId);
            empty.setMessageGuid("");
            empty.setRequestTime(Utils.getInstance().getCurrentDateTime());
            empty.setMessageBodyType(contentType);
            empty.setMessageNum(0);
            empty.setTotalMessages(0);
            int headerLen = convertToString(empty).length();
            if (headerLen >= messageLen - 10)
                giveErrorResponse(headerLen);
            int bodyLen = messageLen - headerLen;
            String[] result = new String[(int) Math.ceil((double)encodedContent.length() / bodyLen)];
            for (int i = 0; i < result.length; i++) {
                RequestMessage item = new RequestMessage();
                item.setSenderIp(empty.getSenderIp());
                item.setServiceId(empty.getServiceId());
                item.setMessageGuid(Utils.getInstance().getNextMessageId());
                item.setRequestTime(empty.getRequestTime());
                item.setMessageNum(i);
                item.setTotalMessages(result.length);
                item.setMessageBodyType(empty.getMessageBodyType());
                item.setMessageBody(encodedContent.substring(i * bodyLen,
                        Math.min((i+1) * bodyLen, encodedContent.length())));
                item.setMessageLength(convertToString(item).length());
                result[i] = convertToString(item);
            }
            return result;
        } else if (messageType.equals(ResponseMessage.class)) {
            ResponseMessage empty = new ResponseMessage();
            try {
                empty.setSenderIp(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            empty.setServiceId(serviceId);
            empty.setMessageGuid("");
            empty.setResponseTime(Utils.getInstance().getCurrentDateTime());
            empty.setResponseCode('S');
            empty.setMessageBodyType(contentType);
            empty.setMessageNum(0);
            empty.setTotalMessages(0);
            int headerLen = convertToString(empty).length();
            if (headerLen >= messageLen - 10)
                giveErrorResponse(headerLen);
            int bodyLen = messageLen - headerLen;
            String[] result = new String[(int) Math.ceil((double)encodedContent.length() / bodyLen)];
            for (int i = 0; i < result.length; i++) {
                ResponseMessage item = new ResponseMessage();
                item.setSenderIp(empty.getSenderIp());
                item.setServiceId(empty.getServiceId());
                item.setMessageGuid(Utils.getInstance().getNextMessageId());
                item.setResponseTime(empty.getResponseTime());
                item.setResponseCode('S');
                item.setMessageNum(i);
                item.setTotalMessages(result.length);
                item.setMessageBodyType(empty.getMessageBodyType());
                item.setMessageBody(encodedContent.substring(i * bodyLen,
                        Math.min((i+1) * bodyLen, encodedContent.length())));
                item.setMessageLength(convertToString(item).length());
                result[i] = convertToString(item);
            }
            return result;
        }
        return null;
    }

    public Object unpack(String[] messages, Class messageType) {
        if (messageType.equals(RequestMessage.class)) {
            RequestMessage[] decodedMessages = new RequestMessage[messages.length];
            for (int i = 0; i < messages.length; i++)
                decodedMessages[i] = (RequestMessage) convertFromString(messages[i]);
            this.serviceId = decodedMessages[0].getServiceId();
            if (decodedMessages[0].getTotalMessages() == decodedMessages.length) {
                StringBuilder sb = new StringBuilder();
                for (RequestMessage item : decodedMessages)
                    sb.append(item.getMessageBody());
                return convertFromString(sb.toString());
            }
        } else if (messageType.equals(ResponseMessage.class)) {
            ResponseMessage[] decodedMessages = new ResponseMessage[messages.length];
            for (int i = 0; i < messages.length; i++)
                decodedMessages[i] = (ResponseMessage) convertFromString(messages[i]);
            this.serviceId = decodedMessages[0].getServiceId();
            if (decodedMessages[0].getTotalMessages() == decodedMessages.length) {
                StringBuilder sb = new StringBuilder();
                for (ResponseMessage item : decodedMessages)
                    sb.append(item.getMessageBody());
                return convertFromString(sb.toString());
            }
        }
        return null;
    }

    public String getServiceId() {
        return serviceId;
    }

    private String convertToString(Object content) {
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(byteStream);
            out.writeObject(content);
            out.flush();
            byte[] contentInBytes = byteStream.toByteArray();
            return Base64.getUrlEncoder().encodeToString(contentInBytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                byteStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Object convertFromString(String content) {
        Object result = null;
        byte[] decodedBytes = Base64.getUrlDecoder().decode(content);
        ByteArrayInputStream bis = new ByteArrayInputStream(decodedBytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            result = in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Objects.requireNonNull(in).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String[] giveErrorResponse(int headerLen) {
        System.out.println("headerLen = " + headerLen + "; messageLen = " + messageLen);
        System.out.println("Error: Message length defined in property file should be" +
                " big enough to include message header and at least 10 symbols of message body");
        ResponseMessage response = new ResponseMessage();
        response.setResponseCode('F');
        String[] result = new String[1];
        result[0] = convertToString(response);
        return result;
    }

    public static void main(String[] args) {
        Packer p = new Packer();
        List<Integer> content = new ArrayList<>();
        for (int i = 0; i < 1000; i++)
            content.add(i);
        String[] packed = p.pack(content, String.class, ResponseMessage.class, "echo");
        for (String item : packed)
            System.out.println(item);
        System.out.println("Total messages = " + packed.length);
        ArrayList<Integer> received = (ArrayList<Integer>) p.unpack(packed, ResponseMessage.class);
        System.out.println("Service id = " + p.getServiceId());
        System.out.println("Received: " + received.toString());
    }
}
