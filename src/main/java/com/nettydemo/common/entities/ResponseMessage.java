package com.nettydemo.common.entities;

import java.io.Serializable;

public class ResponseMessage implements Serializable, Cloneable {
    private String senderIp;
    private String serviceId;
    private String messageGuid;
    private long responseTime;
    private char responseCode;
    private Object messageBody;
    private Class messageBodyType;

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

    public Object getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(Object messageBody) {
        this.messageBody = messageBody;
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
                "senderIp='" + senderIp + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", messageGuid='" + messageGuid + '\'' +
                ", responseTime=" + responseTime +
                ", responseCode=" + responseCode +
                ", messageBodyType=" + messageBodyType +
                '}';
    }
}
