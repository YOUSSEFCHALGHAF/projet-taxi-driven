package com.transporttn.backend.dto;

import com.transporttn.backend.entity.Reservation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaxiReservationRequestDto {
    @NotBlank
    private String pickupAddress;
    @NotNull
    private Double pickupLat;
    @NotNull
    private Double pickupLng;

    @NotBlank
    private String dropoffAddress;
    @NotNull
    private Double dropoffLat;
    @NotNull
    private Double dropoffLng;

    @NotNull
    private Reservation.PaymentMode paymentMode;
}
