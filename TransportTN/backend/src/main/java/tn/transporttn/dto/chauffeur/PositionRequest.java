package tn.transporttn.dto.chauffeur;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PositionRequest(

        @NotNull(message = "La latitude est obligatoire")
        @DecimalMin(value = "30.0", message = "Position hors de Tunisie")
        @DecimalMax(value = "37.6", message = "Position hors de Tunisie")
        Double latitude,

        @NotNull(message = "La longitude est obligatoire")
        @DecimalMin(value = "7.5", message = "Position hors de Tunisie")
        @DecimalMax(value = "11.7", message = "Position hors de Tunisie")
        Double longitude
) {
}