package com.transporttn.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DriverLocationDto {
    @NotNull
    private Double latitude;
    @NotNull
    private Double longitude;
}
