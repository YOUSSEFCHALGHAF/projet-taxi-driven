package com.transporttn.backend.dto;

import com.transporttn.backend.entity.Reservation;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationStatusUpdateDto {
    @NotNull
    private Reservation.Status status;
}
