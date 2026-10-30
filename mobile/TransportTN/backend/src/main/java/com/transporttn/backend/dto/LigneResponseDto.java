package com.transporttn.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneResponseDto {
    private Long id;
    private String name;
    private String departure;
    private String destination;
    private LocalTime departTime;
    private Double price;
    private Integer totalSeats;
    private Integer seatsAvailable;
    private String mode;
}
