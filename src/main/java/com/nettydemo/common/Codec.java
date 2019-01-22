package com.nettydemo.common;

import com.nettydemo.common.entities.RequestMessage;
import com.nettydemo.common.entities.ResponseMessage;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Codec {

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

    public String[] pack(Object content, Class contentType,
                                Class messageType, String serviceId) {
        // Create header for all the messages
        if (messageType.equals(RequestMessage.class)) {
            RequestMessage message = new RequestMessage();
            try {
                message.setSenderIp(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            message.setServiceId(serviceId);
            message.setMessageGuid(Utils.getInstance().getNextMessageId());
            message.setRequestTime(Utils.getInstance().getCurrentDateTime());
            message.setMessageBodyType(contentType);
            message.setMessageBody(content);

            String encodedMessage = convertToString(message);
            String[] result = new String[(int) Math.ceil((double) Objects.requireNonNull(encodedMessage).length() / messageLen)];
            for (int i = 0; i < result.length; i++)
                result[i] = encodedMessage.substring(i * messageLen,
                        Math.min((i+1) * messageLen, encodedMessage.length()));
            return result;
        } else if (messageType.equals(ResponseMessage.class)) {
            ResponseMessage message = new ResponseMessage();
            try {
                message.setSenderIp(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            message.setServiceId(serviceId);
            message.setMessageGuid(Utils.getInstance().getNextMessageId());
            message.setResponseTime(Utils.getInstance().getCurrentDateTime());
            message.setResponseCode('S');
            message.setMessageBodyType(contentType);
            message.setMessageBody(content);
            return packWithoutWrapping(message);
        }
        return null;
    }

    public String[] packWithoutWrapping(Object message) {
        String encodedMessage = convertToString(message);
        String[] result = new String[(int) Math.ceil((double) Objects.requireNonNull(encodedMessage).length() / messageLen)];
        for (int i = 0; i < result.length; i++)
            result[i] = encodedMessage.substring(i * messageLen,
                    Math.min((i+1) * messageLen, encodedMessage.length()));
        return result;
    }

    public Object unpack(String[] messages) {
        StringBuilder sb = new StringBuilder();
        for (String item : messages)
            sb.append(item);
        return convertFromString(sb.toString());
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
                Objects.requireNonNull(byteStream).close();
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

    public static void main(String[] args) {
        Codec p = new Codec();
        List<Integer> content = new ArrayList<>();
        for (int i = 0; i < 1000; i++)
            content.add(i);
        String[] packed = p.pack(content, content.getClass(), RequestMessage.class, "echo");
        for (String item : packed)
            System.out.println(item);
        System.out.println("Total messages = " + packed.length);
        System.out.println("Message size = " + packed[0].length());
        RequestMessage received = (RequestMessage) p.unpack(packed);
        System.out.println("Received: " + received.toString());
        Object receivedContent = received.getMessageBody();
        System.out.println("Message body type: " + receivedContent.getClass().getName());
        System.out.println("Message body: " + receivedContent.toString());
    }
}
