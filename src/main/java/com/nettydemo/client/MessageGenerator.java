package com.nettydemo.client;

import com.nettydemo.common.RequestMessage;
import com.nettydemo.common.Utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class MessageGenerator {
    public static RequestMessage generateMessage() {
        return generateMessage(100, "echo");
    }

    public static RequestMessage generateMessage(int wordCount) {
        return generateMessage(wordCount, "echo");
    }

    public static RequestMessage generateMessage(int wordCount, String serviceId) {
        RequestMessage message = new RequestMessage();
        try {
            message.setSenderIp(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        message.setServiceId(serviceId);
        message.setMessageGuid(Utils.getInstance().getNextMessageId());
        message.setRequestTime(Utils.getInstance().getCurrentDateTime());
        message.setMessageBody(generateRandomWords(wordCount));
        message.setMessageLength(message.toString().length());
        return message;
    }

    private static String generateRandomWords(int wordCount)
    {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            char[] word = new char[random.nextInt(8)+3];
            for(int j = 0; j < word.length; j++)
                word[j] = (char)('a' + random.nextInt(26));
            sb.append(word);
            sb.append(random.nextBoolean() ? " " : random.nextBoolean() ? ", " : ". ");
        }
        return sb.toString();
    }
}
