package tn.transporttn.dto.admin;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Crée un départ pour chaque jour de la période et chaque heure de la liste. */
public record GenerationDepartsRequest(

        @NotNull(message = "La date de début est obligatoire")
        @FutureOrPresent(message = "La date de début ne peut pas être dans le passé")
        LocalDate dateDebut,

        @NotNull(message = "La date de fin est obligatoire")
        LocalDate dateFin,

        @NotEmpty(message = "Indiquez au moins une heure de départ")
        List<@NotNull(message = "Heure invalide") LocalTime> heures
) {
}