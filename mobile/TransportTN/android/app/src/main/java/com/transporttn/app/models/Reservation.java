package com.transporttn.app.models;

public class Reservation {
    private Long id;
    private String type;
    private String status;
    private String pickupAddress;
    private Double pickupLat;
    private Double pickupLng;
    private String dropoffAddress;
    private Double dropoffLat;
    private Double dropoffLng;
    private String paymentMode;
    private Double price;
    private Double distanceKm;
    private String createdAt;
    private Driver driver;

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getPickupAddress() { return pickupAddress; }
    public String getDropoffAddress() { return dropoffAddress; }
    public String getPaymentMode() { return paymentMode; }
    public Double getPrice() { return price; }
    public Double getDistanceKm() { return distanceKm; }
    public String getCreatedAt() { return createdAt; }
    public Driver getDriver() { return driver; }
}
