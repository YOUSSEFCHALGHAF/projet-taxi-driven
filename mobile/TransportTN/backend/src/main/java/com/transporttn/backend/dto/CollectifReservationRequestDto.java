package com.transporttn.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CollectifReservationRequestDto {
    @NotNull
    private Long ligneId;

    @NotNull
    @Min(1)
    @Max(4)
    private Integer seatsCount;

    @NotNull
    private LocalDate travelDate;
}
