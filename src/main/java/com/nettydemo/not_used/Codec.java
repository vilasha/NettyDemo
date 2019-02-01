package com.nettydemo.not_used;

import com.nettydemo.common.Utils;
import com.nettydemo.not_used.entities.RequestMessage;
import com.nettydemo.not_used.entities.ResponseMessage;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Class encodes or decodes objects
 * First it wraps it with a header (RequestMessage or ResponseMessage)
 * then encodes with Base64 Java built in encoder, and after that
 * split encoded string into parts of fixed length. Length of the parts
 * is defined in config.properties file
 *
 * For decoding it does these steps backwards: concatenates parts,
 * decodes with Base64, returns RequestMessage or ResponseMessage
 */
public class Codec {

    /**
     * Length of message parts defined in config.properties file
     */
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

    /**
     * Wraps object with header, encodes and splits into parts
     * @param content object to send
     * @param contentType class of the object above. This field is optional, as we can
     *                    detect content's class by .getClass or instanceOf methods
     * @param messageType what header to wrap with: RequestMessage or ResponseMessage
     * @param serviceId command what to do with the object on server
     * @return encoded object split into parts
     */
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

    /**
     * Just encodes an object with Base64 and splits it into parts
     * of fixed length
     * @param message object to encode
     * @return encoded object split into parts
     */
    public String[] packWithoutWrapping(Object message) {
        String encodedMessage = convertToString(message);
        String[] result = new String[(int) Math.ceil((double) Objects.requireNonNull(encodedMessage).length() / messageLen)];
        for (int i = 0; i < result.length; i++)
            result[i] = encodedMessage.substring(i * messageLen,
                    Math.min((i+1) * messageLen, encodedMessage.length()));
        return result;
    }

    /**
     * Decodes message from parts: concatenates parts,
     * decodes with Base64, returns RequestMessage or ResponseMessage
     * @param messages parts of the message received
     * @return decoded object
     */
    public Object unpack(String[] messages) {
        StringBuilder sb = new StringBuilder();
        for (String item : messages)
            sb.append(item);
        return convertFromString(sb.toString());
    }

    /**
     * Encodes an object with Base64 encoding
     * @param content object to encode
     * @return encoded string
     */
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

    /**
     * Decodes object with Base64
     * @param content encoded string
     * @return decoded object
     */
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

    /**
     * Method for testing class's functionality: creates an array list,
     * encodes and decodes it
     * @param args ignored
     */
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
