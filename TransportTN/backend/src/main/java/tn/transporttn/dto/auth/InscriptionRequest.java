package tn.transporttn.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InscriptionRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 80, message = "Le nom ne doit pas dépasser 80 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 80, message = "Le prénom ne doit pas dépasser 80 caractères")
        String prenom,

        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Size(max = 20, message = "Le numéro de téléphone est trop long")
        String telephone,

        @Email(message = "L'adresse e-mail est invalide")
        @Size(max = 120, message = "L'adresse e-mail ne doit pas dépasser 120 caractères")
        String email,                       // facultatif

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
                 message = "Le mot de passe doit contenir 8 à 72 caractères, avec au moins une lettre et un chiffre")
        String motDePasse
) {
}