package tn.transporttn.dto.reservation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tn.transporttn.model.enums.ModePaiement;

public record ReservationCollectifRequest(

        @NotNull(message = "Le départ est obligatoire")
        Long departId,

        @NotNull(message = "Le nombre de places est obligatoire")
        @Min(value = 1, message = "Réservez au moins 1 place")
        @Max(value = 4, message = "Vous pouvez réserver au maximum 4 places")
        Integer nombrePlaces,

        @NotNull(message = "Le mode de paiement est obligatoire")
        ModePaiement modePaiement
) {
}