package tn.transporttn.dto.reservation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tn.transporttn.model.enums.ModePaiement;

public record ReservationBusRequest(

        @NotNull(message = "Le départ est obligatoire")
        Long departId,

        @NotNull(message = "Le nombre de tickets est obligatoire")
        @Min(value = 1, message = "Achetez au moins 1 ticket")
        @Max(value = 10, message = "Vous pouvez acheter au maximum 10 tickets")
        Integer nombreTickets,

        @NotNull(message = "Le mode de paiement est obligatoire")
        ModePaiement modePaiement
) {
}