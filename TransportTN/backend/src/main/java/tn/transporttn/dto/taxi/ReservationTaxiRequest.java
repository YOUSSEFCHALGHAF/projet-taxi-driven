package tn.transporttn.dto.taxi;

import jakarta.validation.constraints.*;
import tn.transporttn.model.enums.ModePaiement;

public record ReservationTaxiRequest(

        @NotBlank(message = "L'adresse de départ est obligatoire")
        @Size(max = 255, message = "L'adresse de départ est trop longue")
        String adresseDepart,

        @NotNull(message = "La latitude de départ est obligatoire")
        @DecimalMin(value = "30.0", message = "Le départ doit être situé en Tunisie")
        @DecimalMax(value = "37.6", message = "Le départ doit être situé en Tunisie")
        Double latDepart,

        @NotNull(message = "La longitude de départ est obligatoire")
        @DecimalMin(value = "7.5", message = "Le départ doit être situé en Tunisie")
        @DecimalMax(value = "11.7", message = "Le départ doit être situé en Tunisie")
        Double lngDepart,

        @NotBlank(message = "L'adresse d'arrivée est obligatoire")
        @Size(max = 255, message = "L'adresse d'arrivée est trop longue")
        String adresseArrivee,

        @NotNull(message = "La latitude d'arrivée est obligatoire")
        @DecimalMin(value = "30.0", message = "L'arrivée doit être située en Tunisie")
        @DecimalMax(value = "37.6", message = "L'arrivée doit être située en Tunisie")
        Double latArrivee,

        @NotNull(message = "La longitude d'arrivée est obligatoire")
        @DecimalMin(value = "7.5", message = "L'arrivée doit être située en Tunisie")
        @DecimalMax(value = "11.7", message = "L'arrivée doit être située en Tunisie")
        Double lngArrivee,

        @NotNull(message = "Le mode de paiement est obligatoire")
        ModePaiement modePaiement
) {
}