package com.transporttn.app.models;

public class AuthResponse {
    private String token;
    private Long userId;
    private String name;
    private String phone;
    private String role;

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
}
