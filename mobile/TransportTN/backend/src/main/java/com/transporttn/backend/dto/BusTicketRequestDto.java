package com.transporttn.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BusTicketRequestDto {
    @NotNull
    private Long ligneId;

    @NotNull
    private LocalDate travelDate;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer quantity;
}
