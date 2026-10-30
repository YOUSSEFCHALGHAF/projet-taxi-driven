package tn.transporttn.dto.reservation;

import tn.transporttn.dto.catalogue.DepartResponse;
import tn.transporttn.model.ReservationCollectif;
import tn.transporttn.model.enums.ModePaiement;
import tn.transporttn.model.enums.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationCollectifResponse(
        Long id,
        String reference,
        StatutReservation statut,
        BigDecimal montant,
        ModePaiement modePaiement,
        LocalDateTime dateCreation,
        int nombrePlaces,
        DepartResponse depart
) {
    public static ReservationCollectifResponse depuis(ReservationCollectif r) {
        return new ReservationCollectifResponse(r.getId(), r.getReference(), r.getStatut(), r.getMontant(),
                r.getModePaiement(), r.getDateCreation(), r.getNombrePlaces(),
                DepartResponse.depuis(r.getDepart()));
    }
}