package com.transporttn.app.models;

public class BusTicketRequest {
    private Long ligneId;
    private String travelDate;
    private Integer quantity;

    public BusTicketRequest(Long ligneId, String travelDate, Integer quantity) {
        this.ligneId = ligneId;
        this.travelDate = travelDate;
        this.quantity = quantity;
    }
}
