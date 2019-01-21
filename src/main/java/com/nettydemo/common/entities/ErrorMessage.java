package com.nettydemo.common.entities;

import java.io.Serializable;

public class ErrorMessage implements Serializable {
    private String errorCodeDetail;
    private String errorReason;

    public String getErrorCodeDetail() {
        return errorCodeDetail;
    }

    public void setErrorCodeDetail(String errorCodeDetail) {
        this.errorCodeDetail = errorCodeDetail;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "errorCodeDetail='" + errorCodeDetail + '\'' +
                ", errorReason='" + errorReason + '\'' +
                '}';
    }
}
