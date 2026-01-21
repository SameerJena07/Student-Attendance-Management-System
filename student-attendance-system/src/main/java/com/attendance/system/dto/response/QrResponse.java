package com.attendance.system.dto.response;

public class QrResponse {
    private String qrToken;

    public QrResponse(String qrToken) {
        this.qrToken = qrToken;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }
}