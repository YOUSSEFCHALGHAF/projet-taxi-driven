package tn.transporttn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tn.transporttn.model.Reservation;
import tn.transporttn.model.enums.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** Historique paginé d'un client (taxi + collectif + bus). */
    Page<Reservation> findByClientIdOrderByDateCreationDesc(Long clientId, Pageable pageable);

    Optional<Reservation> findByReference(String reference);

    long countByStatut(StatutReservation statut);
    long countByDateCreationAfter(LocalDateTime date);

    /** Somme des montants, hors réservations du statut exclu (null s'il n'y a aucune réservation). */
    @Query("SELECT SUM(r.montant) FROM Reservation r WHERE r.statut <> :exclu")
    BigDecimal sommeMontantsHorsStatut(@Param("exclu") StatutReservation exclu);
}