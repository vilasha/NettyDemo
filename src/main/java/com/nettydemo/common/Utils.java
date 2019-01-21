package com.nettydemo.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class Utils {
    private static final Utils SINGLE_INSTANCE = new Utils();
    private Utils() {}

    public static Utils getInstance() {
        return SINGLE_INSTANCE;
    }

    // Remote host and port of server
    private static String HOST;
    private static int PORT;

    // Counter of messages on this JVM. Used for generating messageId
    private static volatile int messageCounter;

    // Unique prefix for this JVM
    private static final String messageIdPrefix;

    static {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("config.properties"));
            HOST = properties.getProperty("host");
            PORT = Integer.parseInt(properties.getProperty("port"));
        } catch (IOException e) {
            e.printStackTrace();
            HOST = "localhost";
            PORT = 9090;
        }
        messageCounter = 0;
        messageIdPrefix = String.valueOf((char)('A' + (new Random()).nextInt(26)));
    }

    public int getPort() {
        return PORT;
    }

    public String getHost() {
        return HOST;
    }

    public String getNextMessageId() {
        messageCounter++;
        return messageIdPrefix + String.format("%011d", messageCounter);
    }

    public Long getCurrentDateTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmSSS");
        return Long.parseLong(format.format(new Date()));
    }
}
