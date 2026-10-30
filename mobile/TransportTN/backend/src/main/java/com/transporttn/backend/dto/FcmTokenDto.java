package com.transporttn.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FcmTokenDto {
    @NotBlank
    private String fcmToken;
}
