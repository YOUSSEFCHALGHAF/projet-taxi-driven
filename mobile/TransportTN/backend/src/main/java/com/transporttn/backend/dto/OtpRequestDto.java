package com.transporttn.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OtpRequestDto {
    @NotBlank
    @Pattern(regexp = "^\\+216[0-9]{8}$", message = "Le numero doit etre au format +216XXXXXXXX")
    private String phone;
}
