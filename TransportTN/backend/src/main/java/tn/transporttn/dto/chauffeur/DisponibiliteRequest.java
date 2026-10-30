package tn.transporttn.dto.chauffeur;

import jakarta.validation.constraints.NotNull;

public record DisponibiliteRequest(
        @NotNull(message = "Indiquez si vous êtes disponible")
        Boolean disponible
) {
}