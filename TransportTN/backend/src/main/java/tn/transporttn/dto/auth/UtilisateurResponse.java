package tn.transporttn.dto.auth;

import tn.transporttn.model.Utilisateur;
import tn.transporttn.model.enums.Role;

import java.time.LocalDateTime;

/** Vue publique d'un utilisateur — sans mot de passe ni jeton FCM. */
public record UtilisateurResponse(
        Long id,
        String nom,
        String prenom,
        String telephone,
        String email,
        Role role,
        LocalDateTime dateCreation
) {
    public static UtilisateurResponse depuis(Utilisateur u) {
        return new UtilisateurResponse(
                u.getId(), u.getNom(), u.getPrenom(), u.getTelephone(),
                u.getEmail(), u.getRole(), u.getDateCreation());
    }
}