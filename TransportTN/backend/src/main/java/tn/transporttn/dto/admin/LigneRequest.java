package tn.transporttn.dto.admin;

import jakarta.validation.constraints.*;
import tn.transporttn.model.enums.TypeLigne;

import java.math.BigDecimal;

public record LigneRequest(

        @NotNull(message = "Le type de ligne est obligatoire")
        TypeLigne type,

        @NotBlank(message = "La ville de départ est obligatoire")
        @Size(max = 60, message = "La ville de départ est trop longue")
        String villeDepart,

        @NotBlank(message = "La ville d'arrivée est obligatoire")
        @Size(max = 60, message = "La ville d'arrivée est trop longue")
        String villeArrivee,

        @NotBlank(message = "La station de départ est obligatoire")
        @Size(max = 120, message = "La station de départ est trop longue")
        String stationDepart,

        @NotBlank(message = "La station d'arrivée est obligatoire")
        @Size(max = 120, message = "La station d'arrivée est trop longue")
        String stationArrivee,

        @NotNull(message = "Le prix est obligatoire")
        @DecimalMin(value = "0.100", message = "Le prix minimum est de 0,100 DT")
        @Digits(integer = 7, fraction = 3, message = "Le prix doit avoir au maximum 3 décimales")
        BigDecimal prix,

        @NotNull(message = "La capacité est obligatoire")
        @Min(value = 1, message = "La capacité doit être d'au moins 1 place")
        @Max(value = 100, message = "La capacité ne peut pas dépasser 100 places")
        Integer capacite,

        @NotNull(message = "La durée est obligatoire")
        @Min(value = 5, message = "La durée minimale est de 5 minutes")
        @Max(value = 1440, message = "La durée maximale est de 24 heures")
        Integer dureeMinutes
) {
}