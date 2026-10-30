package tn.transporttn.dto.reservation;

import tn.transporttn.model.Depart;
import tn.transporttn.model.Ligne;
import tn.transporttn.model.ReservationBus;
import tn.transporttn.model.Ticket;
import tn.transporttn.model.enums.StatutTicket;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String codeUnique,
        String qrCodeBase64,           // à afficher avec src="data:image/png;base64,..."
        StatutTicket statut,
        LocalDateTime dateEmission,
        String reference,
        String itineraire,
        String stationDepart,
        String stationArrivee,
        LocalDateTime dateHeureDepart
) {
    public static TicketResponse depuis(Ticket t) {
        ReservationBus r = t.getReservation();
        Depart d = r.getDepart();
        Ligne l = d.getLigne();
        return new TicketResponse(t.getId(), t.getCodeUnique(), t.getQrCodeBase64(), t.getStatut(),
                t.getDateEmission(), r.getReference(), l.getItineraire(),
                l.getStationDepart(), l.getStationArrivee(), d.getDateHeure());
    }
}