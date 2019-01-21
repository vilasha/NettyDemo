package com.nettydemo.common.entities;

import java.io.Serializable;

public class RequestMessage implements Serializable, Cloneable {
    private String senderIp;
    private String serviceId;
    private String messageGuid;
    private long requestTime;
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

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
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
        return "RequestMessage{" +
                "senderIp='" + senderIp + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", messageGuid='" + messageGuid + '\'' +
                ", requestTime=" + requestTime +
                ", messageBodyType=" + messageBodyType +
                '}';
    }
}
