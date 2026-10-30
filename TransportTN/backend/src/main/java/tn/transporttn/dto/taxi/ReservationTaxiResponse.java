package tn.transporttn.dto.taxi;

import tn.transporttn.model.Chauffeur;
import tn.transporttn.model.ReservationTaxi;
import tn.transporttn.model.Utilisateur;
import tn.transporttn.model.enums.ModePaiement;
import tn.transporttn.model.enums.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationTaxiResponse(
        Long id,
        String reference,
        StatutReservation statut,
        BigDecimal montant,
        ModePaiement modePaiement,
        LocalDateTime dateCreation,
        String adresseDepart,
        Double latDepart,
        Double lngDepart,
        String adresseArrivee,
        Double latArrivee,
        Double lngArrivee,
        Double distanceKm,
        String clientNomComplet,
        String clientTelephone,
        ChauffeurInfo chauffeur          // null tant qu'aucun chauffeur n'est attribué
) {

    public record ChauffeurInfo(Long id, String nomComplet, String telephone, String matricule,
                                String modeleVehicule, Double latitude, Double longitude) {
    }

    public static ReservationTaxiResponse depuis(ReservationTaxi r) {
        Utilisateur client = r.getClient();
        Chauffeur c = r.getChauffeur();
        ChauffeurInfo info = c == null ? null : new ChauffeurInfo(
                c.getId(), c.getUtilisateur().getNomComplet(), c.getUtilisateur().getTelephone(),
                c.getMatricule(), c.getModeleVehicule(), c.getLatitude(), c.getLongitude());

        return new ReservationTaxiResponse(r.getId(), r.getReference(), r.getStatut(), r.getMontant(),
                r.getModePaiement(), r.getDateCreation(), r.getAdresseDepart(), r.getLatDepart(),
                r.getLngDepart(), r.getAdresseArrivee(), r.getLatArrivee(), r.getLngArrivee(),
                r.getDistanceKm(), client.getNomComplet(), client.getTelephone(), info);
    }
}