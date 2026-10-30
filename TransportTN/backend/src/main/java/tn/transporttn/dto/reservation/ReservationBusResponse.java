package tn.transporttn.dto.reservation;

import tn.transporttn.dto.catalogue.DepartResponse;
import tn.transporttn.model.ReservationBus;
import tn.transporttn.model.enums.ModePaiement;
import tn.transporttn.model.enums.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReservationBusResponse(
        Long id,
        String reference,
        StatutReservation statut,
        BigDecimal montant,
        ModePaiement modePaiement,
        LocalDateTime dateCreation,
        int nombreTickets,
        DepartResponse depart,
        List<TicketResponse> tickets
) {
    public static ReservationBusResponse depuis(ReservationBus r) {
        return new ReservationBusResponse(r.getId(), r.getReference(), r.getStatut(), r.getMontant(),
                r.getModePaiement(), r.getDateCreation(), r.getNombreTickets(),
                DepartResponse.depuis(r.getDepart()),
                r.getTickets().stream().map(TicketResponse::depuis).toList());
    }
}