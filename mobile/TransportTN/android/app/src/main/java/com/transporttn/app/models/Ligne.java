package com.transporttn.app.models;

public class Ligne {
    private Long id;
    private String name;
    private String departure;
    private String destination;
    private String departTime;
    private Double price;
    private Integer totalSeats;
    private Integer seatsAvailable;
    private String mode;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDeparture() { return departure; }
    public String getDestination() { return destination; }
    public String getDepartTime() { return departTime; }
    public Double getPrice() { return price; }
    public Integer getTotalSeats() { return totalSeats; }
    public Integer getSeatsAvailable() { return seatsAvailable; }
    public String getMode() { return mode; }
}
