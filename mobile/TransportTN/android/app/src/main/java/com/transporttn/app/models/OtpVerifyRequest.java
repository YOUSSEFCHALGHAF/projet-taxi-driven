package com.transporttn.app.models;

public class OtpVerifyRequest {
    private String phone;
    private String code;
    private String name;

    public OtpVerifyRequest(String phone, String code, String name) {
        this.phone = phone;
        this.code = code;
        this.name = name;
    }
}
