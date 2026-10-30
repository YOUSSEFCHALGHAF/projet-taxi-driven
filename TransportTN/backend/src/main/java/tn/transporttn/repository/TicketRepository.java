package tn.transporttn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.transporttn.model.Ticket;
import tn.transporttn.model.enums.StatutTicket;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    long countByStatut(StatutTicket statut);
    /** Utilisé lors du scan du QR code. */
    Optional<Ticket> findByCodeUnique(String codeUnique);

    /** "Mes tickets" : tickets du client avec réservation, départ et ligne en une seule requête. */
    @Query("""
           SELECT t FROM Ticket t
           JOIN FETCH t.reservation r
           JOIN FETCH r.depart d
           JOIN FETCH d.ligne
           WHERE r.client.id = :clientId
           ORDER BY t.dateEmission DESC
           """)
    List<Ticket> findTicketsDuClient(@Param("clientId") Long clientId);
}