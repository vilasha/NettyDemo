package com.nettydemo.common;

import java.io.Serializable;

public class ResponseMessage implements Serializable, Cloneable {
    private int messageLength;
    private String senderIp;
    private String serviceId;
    private String messageGuid;
    private long responseTime;
    private char responseCode;
    private long messageNum;
    private long totalMessages;
    private String messageBody;
    private Class messageBodyType;

    public int getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public void setSenderIp(String senderIp) {
        this.senderIp = senderIp;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getMessageGuid() {
        return messageGuid;
    }

    public void setMessageGuid(String messageGuid) {
        this.messageGuid = messageGuid;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public char getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(char responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public long getMessageNum() {
        return messageNum;
    }

    public void setMessageNum(long messageNum) {
        this.messageNum = messageNum;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public Class getMessageBodyType() {
        return messageBodyType;
    }

    public void setMessageBodyType(Class messageBodyType) {
        this.messageBodyType = messageBodyType;
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "messageLength=" + messageLength +
                ", senderIp='" + senderIp + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", messageGuid='" + messageGuid + '\'' +
                ", responseTime=" + responseTime +
                ", responseCode=" + responseCode +
                ", messageNum=" + messageNum +
                ", totalMessages=" + totalMessages +
                ", messageBody='" + messageBody + '\'' +
                ", messageBodyType=" + messageBodyType +
                '}';
    }
}
