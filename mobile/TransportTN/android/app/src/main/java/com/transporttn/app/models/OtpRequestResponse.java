package com.transporttn.app.models;

public class OtpRequestResponse {
    private String message;
    private String debugCode; // present uniquement en mode debug backend

    public String getMessage() { return message; }
    public String getDebugCode() { return debugCode; }
}
