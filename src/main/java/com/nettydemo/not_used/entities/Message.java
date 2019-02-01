package com.nettydemo.not_used.entities;

public abstract class Message {
    private String messageGuid;
    private String senderIp;
    private Long requestTime;

    public String getMessageGuid() {
        return messageGuid;
    }

    public void setMessageGuid(String messageGuid) {
        this.messageGuid = messageGuid;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public void setSenderIp(String senderIp) {
        this.senderIp = senderIp;
    }

    public Long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Long requestTime) {
        this.requestTime = requestTime;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageGuid='" + messageGuid + '\'' +
                ", senderIp='" + senderIp + '\'' +
                ", requestTime=" + requestTime +
                '}';
    }
}
