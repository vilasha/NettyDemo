package com.nettydemo.common.fixed_length.entities;

public class ErrorMessage extends Message {
    private String errorCode;
    private String errorDescription;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "errorCode='" + errorCode + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                "} " + super.toString();
    }
}
