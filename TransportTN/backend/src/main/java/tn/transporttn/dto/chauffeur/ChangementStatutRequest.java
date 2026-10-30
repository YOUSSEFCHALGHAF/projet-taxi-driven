package tn.transporttn.dto.chauffeur;

import jakarta.validation.constraints.NotNull;
import tn.transporttn.model.enums.StatutReservation;

public record ChangementStatutRequest(
        @NotNull(message = "Le nouveau statut est obligatoire")
        StatutReservation statut
) {
}