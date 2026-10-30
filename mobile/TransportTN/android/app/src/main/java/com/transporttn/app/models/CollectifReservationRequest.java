package com.transporttn.app.models;

public class CollectifReservationRequest {
    private Long ligneId;
    private Integer seatsCount;
    private String travelDate;

    public CollectifReservationRequest(Long ligneId, Integer seatsCount, String travelDate) {
        this.ligneId = ligneId;
        this.seatsCount = seatsCount;
        this.travelDate = travelDate;
    }
}
