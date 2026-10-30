package tn.transporttn.dto.chauffeur;

import tn.transporttn.model.Chauffeur;
import tn.transporttn.model.Utilisateur;

import java.time.LocalDateTime;

public record ChauffeurResponse(
        Long id,
        Long utilisateurId,
        String nomComplet,
        String telephone,
        boolean actif,
        String numeroPermis,
        String matricule,
        String modeleVehicule,
        boolean disponible,
        Double latitude,
        Double longitude,
        LocalDateTime derniereMiseAJour
) {
    public static ChauffeurResponse depuis(Chauffeur c) {
        Utilisateur u = c.getUtilisateur();
        return new ChauffeurResponse(c.getId(), u.getId(), u.getNomComplet(), u.getTelephone(), u.isActif(),
                c.getNumeroPermis(), c.getMatricule(), c.getModeleVehicule(), c.isDisponible(),
                c.getLatitude(), c.getLongitude(), c.getDerniereMiseAJour());
    }
}