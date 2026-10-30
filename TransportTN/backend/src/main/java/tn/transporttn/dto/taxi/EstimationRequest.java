package tn.transporttn.dto.taxi;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record EstimationRequest(

        @NotNull(message = "La latitude de départ est obligatoire")
        @DecimalMin(value = "30.0", message = "Le départ doit être situé en Tunisie")
        @DecimalMax(value = "37.6", message = "Le départ doit être situé en Tunisie")
        Double latDepart,

        @NotNull(message = "La longitude de départ est obligatoire")
        @DecimalMin(value = "7.5", message = "Le départ doit être situé en Tunisie")
        @DecimalMax(value = "11.7", message = "Le départ doit être situé en Tunisie")
        Double lngDepart,

        @NotNull(message = "La latitude d'arrivée est obligatoire")
        @DecimalMin(value = "30.0", message = "L'arrivée doit être située en Tunisie")
        @DecimalMax(value = "37.6", message = "L'arrivée doit être située en Tunisie")
        Double latArrivee,

        @NotNull(message = "La longitude d'arrivée est obligatoire")
        @DecimalMin(value = "7.5", message = "L'arrivée doit être située en Tunisie")
        @DecimalMax(value = "11.7", message = "L'arrivée doit être située en Tunisie")
        Double lngArrivee
) {
}