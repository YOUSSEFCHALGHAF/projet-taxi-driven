package tn.transporttn.dto.admin;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DepartRequest(
        @NotNull(message = "La date et l'heure sont obligatoires")
        @Future(message = "Le départ doit être dans le futur")
        LocalDateTime dateHeure
) {
}