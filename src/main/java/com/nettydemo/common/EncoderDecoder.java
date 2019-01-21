package com.nettydemo.common;

public class EncoderDecoder {

    private static final String separator = "\t";

    public static String encodeRequest(RequestMessage message) {
//        System.out.println("> encodeRequest: " + message.toString());
        StringBuilder sb = new StringBuilder();
        sb.append("messageLength").append(separator).append(message.getMessageLength());
        sb.append(separator).append("senderIp").append(separator).append(message.getSenderIp());
        sb.append(separator).append("serviceId").append(separator).append(message.getServiceId());
        sb.append(separator).append("messageGuid").append(separator).append(message.getMessageGuid());
        sb.append(separator).append("requestTime").append(separator).append(message.getRequestTime());
        sb.append(separator).append("messageBody").append(separator).append(message.getMessageBody());
        sb.append("\r\n");
//        System.out.println("> encodeRequest: " + sb.toString());
        return sb.toString();
    }

    public static RequestMessage decodeRequest(String encoded) {
//        System.out.println("> decodeRequest: " + encoded);
        String[] fields = encoded.split(separator);
        RequestMessage message = new RequestMessage();
        for (int i = 0; i < fields.length-1; i = i+2)
            switch (fields[i]) {
                case "messageLength": message.setMessageLength(Integer.parseInt(fields[i+1]));
                    break;
                case "senderIp": message.setSenderIp(fields[i+1]);
                    break;
                case "serviceId": message.setServiceId(fields[i+1]);
                    break;
                case "messageGuid": message.setMessageGuid(fields[i+1]);
                    break;
                case "requestTime": message.setRequestTime(Long.parseLong(fields[i+1]));
                    break;
                case "messageBody": message.setMessageBody(fields[i+1]);
                    break;
            }
//        System.out.println("> decodeRequest: " + message.toString());
        return  message;
    }

    public static String encodeResponse(ResponseMessage message) {
//        System.out.println("> encodeResponse: " + message.toString());
        StringBuilder sb = new StringBuilder();
        sb.append("messageLength").append(separator).append(message.getMessageLength());
        sb.append(separator).append("senderIp").append(separator).append(message.getSenderIp());
        sb.append(separator).append("serviceId").append(separator).append(message.getServiceId());
        sb.append(separator).append("messageGuid").append(separator).append(message.getMessageGuid());
        sb.append(separator).append("responseTime").append(separator).append(message.getResponseTime());
        sb.append(separator).append("responseCode").append(separator).append(message.getResponseCode());
        sb.append(separator).append("messageBody").append(separator).append(message.getMessageBody());
        sb.append("\r\n");
//        System.out.println("> encodeResponse: " + sb.toString());
        return sb.toString();
    }

    public static ResponseMessage decodeResponse(String encoded) {
//        System.out.println("> decodeResponse: " + encoded);
        String[] fields = encoded.split(separator);
        ResponseMessage message = new ResponseMessage();
        for (int i = 0; i < fields.length-1; i = i+2)
            switch (fields[i]) {
                case "messageLength": message.setMessageLength(Integer.parseInt(fields[i+1]));
                    break;
                case "senderIp": message.setSenderIp(fields[i+1]);
                    break;
                case "serviceId": message.setServiceId(fields[i+1]);
                    break;
                case "messageGuid": message.setMessageGuid(fields[i+1]);
                    break;
                case "responseTime": message.setResponseTime(Long.parseLong(fields[i+1]));
                    break;
                case "responseCode": message.setResponseCode(fields[i+1].charAt(0));
                    break;
                case "messageBody": message.setMessageBody(fields[i+1]);
                    break;
            }
//        System.out.println("> decodeResponse: " + message.toString());
        return  message;
    }
}
