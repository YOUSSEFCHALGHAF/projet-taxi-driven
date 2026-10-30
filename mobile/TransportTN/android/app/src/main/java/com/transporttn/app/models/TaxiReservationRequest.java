package com.transporttn.app.models;

public class TaxiReservationRequest {
    private String pickupAddress;
    private Double pickupLat;
    private Double pickupLng;
    private String dropoffAddress;
    private Double dropoffLat;
    private Double dropoffLng;
    private String paymentMode;

    public TaxiReservationRequest(String pickupAddress, Double pickupLat, Double pickupLng,
                                   String dropoffAddress, Double dropoffLat, Double dropoffLng,
                                   String paymentMode) {
        this.pickupAddress = pickupAddress;
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.dropoffAddress = dropoffAddress;
        this.dropoffLat = dropoffLat;
        this.dropoffLng = dropoffLng;
        this.paymentMode = paymentMode;
    }
}
