package com.nettydemo.not_used.entities;

import com.fasterxml.jackson.annotation.JsonSetter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;

public class MessageField {
    private String name;
    private int fieldLength;
    private String dataType;
    private String defaultValue;
    private boolean isMandatory;
    private ArrayList<String> values;

    public String getName() {
        return name;
    }

    @XmlElement(name="name")
    @JsonSetter("name")
    public void setName(String name) {
        this.name = name;
    }

    public int getFieldLength() {
        return fieldLength;
    }

    @XmlElement(name="field-length")
    @JsonSetter("field-length")
    public void setFieldLength(int fieldLength) {
        this.fieldLength = fieldLength;
    }

    public String getDataType() {
        return dataType;
    }

    @XmlElement(name="data-type")
    @JsonSetter("data-type")
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @XmlElement(name="default-value")
    @JsonSetter("default-value")
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    @XmlElement(name="is-mandatory")
    @JsonSetter("is-mandatory")
    public void setMandatory(boolean mandatory) {
        isMandatory = mandatory;
    }

    public ArrayList<String> getValues() { return values; }

    @XmlElementWrapper(name="values")
    @XmlElement(name="value")
    @JsonSetter("value")
    public void setValues(ArrayList<String> values) {
        this.values = values;
    }
}
