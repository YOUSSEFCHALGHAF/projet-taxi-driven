package com.transporttn.app.models;

public class Ticket {
    private Long id;
    private Ligne ligne;
    private String travelDate;
    private Integer quantity;
    private String qrCode;
    private Double price;
    private String status;
    private String createdAt;

    public Long getId() { return id; }
    public Ligne getLigne() { return ligne; }
    public String getTravelDate() { return travelDate; }
    public Integer getQuantity() { return quantity; }
    public String getQrCode() { return qrCode; }
    public Double getPrice() { return price; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}
