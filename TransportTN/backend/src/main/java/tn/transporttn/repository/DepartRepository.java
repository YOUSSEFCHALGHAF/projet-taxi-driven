package tn.transporttn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.transporttn.model.Depart;

import java.time.LocalDateTime;
import java.util.List;

public interface DepartRepository extends JpaRepository<Depart, Long> {

    /** Départs d'une ligne sur une période (ex : une journée choisie par le client). */
    @Query("""
           SELECT d FROM Depart d
           JOIN FETCH d.ligne
           WHERE d.ligne.id = :ligneId
             AND d.dateHeure BETWEEN :debut AND :fin
           ORDER BY d.dateHeure
           """)
    List<Depart> findByLigneEtPeriode(@Param("ligneId") Long ligneId,
                                      @Param("debut") LocalDateTime debut,
                                      @Param("fin") LocalDateTime fin);

    /** Prochains départs ayant encore au moins "minPlaces" places. */
    List<Depart> findByLigneIdAndDateHeureAfterAndPlacesRestantesGreaterThanEqualOrderByDateHeureAsc(
            Long ligneId, LocalDateTime apres, int minPlaces);
            boolean existsByLigneIdAndDateHeure(Long ligneId, LocalDateTime dateHeure);

            boolean existsByLigneId(Long ligneId);

            long countByDateHeureAfter(LocalDateTime date);
}