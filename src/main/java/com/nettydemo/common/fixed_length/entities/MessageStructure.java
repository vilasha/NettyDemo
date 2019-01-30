package com.nettydemo.common.fixed_length.entities;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlRootElement
public class MessageStructure {
    private int id;
    private int messageNumber;
    private int totalMessages;
    private ArrayList<MessageField> fields;

    public int getId() { return id; }

    @XmlAttribute
    public void setId(int id) { this.id = id; }

    public int getMessageNumber() {
        return messageNumber;
    }

    @XmlElement(name="message-number")
    public void setMessageNumber(int messageNumber) {
        this.messageNumber = messageNumber;
    }

    public int getTotalMessages() {
        return totalMessages;
    }

    @XmlElement(name="total-messages")
    public void setTotalMessages(int totalMessages) {
        this.totalMessages = totalMessages;
    }

    public ArrayList<MessageField> getFields() {
        return fields;
    }

    @XmlElement(name="field")
    public void setFields(ArrayList<MessageField> fields) {
        this.fields = fields;
    }
}
