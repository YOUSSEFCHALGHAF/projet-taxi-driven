package com.transporttn.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OtpVerifyDto {
    @NotBlank
    @Pattern(regexp = "^\\+216[0-9]{8}$")
    private String phone;

    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "Le code OTP doit contenir 6 chiffres")
    private String code;

    /** Requis uniquement lors de la toute premiere inscription */
    private String name;
}
