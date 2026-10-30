package tn.transporttn.dto.reservation;

import tn.transporttn.model.Reservation;
import tn.transporttn.model.ReservationBus;
import tn.transporttn.model.ReservationCollectif;
import tn.transporttn.model.ReservationTaxi;
import tn.transporttn.model.enums.ModePaiement;
import tn.transporttn.model.enums.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResumeResponse(
        Long id,
        String reference,
        String type,                    // TAXI, COLLECTIF ou BUS
        StatutReservation statut,
        BigDecimal montant,
        ModePaiement modePaiement,
        LocalDateTime dateCreation,
        String description,
        LocalDateTime dateVoyage
) {
    public static ReservationResumeResponse depuis(Reservation r) {
        String type;
        String description;
        LocalDateTime dateVoyage;

        if (r instanceof ReservationTaxi t) {
            type = "TAXI";
            description = t.getAdresseDepart() + " → " + t.getAdresseArrivee();
            dateVoyage = t.getDateCreation();
        } else if (r instanceof ReservationCollectif c) {
            type = "COLLECTIF";
            description = c.getDepart().getLigne().getItineraire() + " · " + c.getNombrePlaces() + " place(s)";
            dateVoyage = c.getDepart().getDateHeure();
        } else if (r instanceof ReservationBus b) {
            type = "BUS";
            description = b.getDepart().getLigne().getItineraire() + " · " + b.getNombreTickets() + " ticket(s)";
            dateVoyage = b.getDepart().getDateHeure();
        } else {
            throw new IllegalStateException("Type de réservation inconnu");
        }

        return new ReservationResumeResponse(r.getId(), r.getReference(), type, r.getStatut(),
                r.getMontant(), r.getModePaiement(), r.getDateCreation(), description, dateVoyage);
    }
}