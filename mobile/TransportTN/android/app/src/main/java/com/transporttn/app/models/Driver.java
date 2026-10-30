package com.transporttn.app.models;

public class Driver {
    private Long id;
    private User user;
    private String vehicleType;
    private String vehiclePlate;
    private Double latitude;
    private Double longitude;
    private Boolean available;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getVehicleType() { return vehicleType; }
    public String getVehiclePlate() { return vehiclePlate; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Boolean getAvailable() { return available; }
}
