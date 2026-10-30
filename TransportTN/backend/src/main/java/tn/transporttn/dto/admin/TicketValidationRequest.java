package tn.transporttn.dto.admin;

import jakarta.validation.constraints.NotBlank;

/** Contenu lu par le scanner : "TRANSPORTTN:<uuid>" ou directement l'UUID. */
public record TicketValidationRequest(
        @NotBlank(message = "Le code du ticket est obligatoire")
        String code
) {
}