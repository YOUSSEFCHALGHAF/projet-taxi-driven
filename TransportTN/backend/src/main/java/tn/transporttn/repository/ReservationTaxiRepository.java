package tn.transporttn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.transporttn.model.ReservationTaxi;
import tn.transporttn.model.enums.StatutReservation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationTaxiRepository extends JpaRepository<ReservationTaxi, Long> {
            /** Course suivie par le client (pour lui envoyer la position du chauffeur). */
    Optional<ReservationTaxi> findFirstByChauffeurIdAndStatutInOrderByDateCreationDesc(
            Long chauffeurId, Collection<StatutReservation> statuts);
    /** Courses d'un chauffeur dans certains statuts (ex : EN_ATTENTE, ACCEPTEE, EN_COURS). */
    List<ReservationTaxi> findByChauffeurIdAndStatutInOrderByDateCreationDesc(
            Long chauffeurId, Collection<StatutReservation> statuts);

    boolean existsByChauffeurIdAndStatutIn(Long chauffeurId, Collection<StatutReservation> statuts);

    /** Course active d'un client (pour l'empêcher d'en réserver deux en même temps). */
    Optional<ReservationTaxi> findFirstByClientIdAndStatutInOrderByDateCreationDesc(
            Long clientId, Collection<StatutReservation> statuts);
}