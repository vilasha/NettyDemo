package com.nettydemo.common;

import java.io.Serializable;

public class RequestMessage implements Serializable, Cloneable {
    private int messageLength;
    private String senderIp;
    private String serviceId;
    private String messageGuid;
    private long requestTime;
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

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
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
        return "RequestMessage{" +
                "messageLength=" + messageLength +
                ", senderIp='" + senderIp + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", messageGuid='" + messageGuid + '\'' +
                ", requestTime=" + requestTime +
                ", messageNum=" + messageNum +
                ", totalMessages=" + totalMessages +
                ", messageBody='" + messageBody + '\'' +
                ", messageBodyType=" + messageBodyType +
                '}';
    }
}
