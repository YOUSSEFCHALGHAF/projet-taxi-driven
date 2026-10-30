package tn.transporttn.dto.admin;

import jakarta.validation.constraints.*;

public record ChauffeurCreationRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 80, message = "Le nom ne doit pas dépasser 80 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 80, message = "Le prénom ne doit pas dépasser 80 caractères")
        String prenom,

        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        String telephone,

        @Email(message = "L'adresse e-mail est invalide")
        @Size(max = 120)
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
                 message = "Le mot de passe doit contenir 8 à 72 caractères, avec au moins une lettre et un chiffre")
        String motDePasse,

        @NotBlank(message = "Le numéro de permis est obligatoire")
        @Size(max = 30, message = "Le numéro de permis est trop long")
        String numeroPermis,

        @NotBlank(message = "Le matricule est obligatoire")
        @Pattern(regexp = "^\\d{1,3}\\s*(?i:TU)\\s*\\d{1,4}$",
                 message = "Matricule invalide (format tunisien, ex : 215 TU 4821)")
        String matricule,

        @NotBlank(message = "Le modèle du véhicule est obligatoire")
        @Size(max = 80, message = "Le modèle du véhicule est trop long")
        String modeleVehicule
) {
}