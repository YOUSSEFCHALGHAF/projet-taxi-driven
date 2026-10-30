package tn.transporttn.repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.transporttn.model.Ligne;
import tn.transporttn.model.enums.TypeLigne;

import java.util.List;

public interface LigneRepository extends JpaRepository<Ligne, Long> {

    List<Ligne> findByTypeAndActiveTrueOrderByVilleDepartAscVilleArriveeAsc(TypeLigne type);

    List<Ligne> findByTypeAndVilleDepartIgnoreCaseAndVilleArriveeIgnoreCaseAndActiveTrue(
            TypeLigne type, String villeDepart, String villeArrivee);

    @Query("SELECT DISTINCT l.villeDepart FROM Ligne l WHERE l.active = true ORDER BY l.villeDepart")
    List<String> findVillesDepart();

    @Query("SELECT DISTINCT l.villeArrivee FROM Ligne l WHERE l.active = true ORDER BY l.villeArrivee")
    List<String> findVillesArrivee();
    /** Recherche avec filtres facultatifs (null = pas de filtre). */
    @Query("""
           SELECT l FROM Ligne l
           WHERE l.active = true
             AND (:type IS NULL OR l.type = :type)
             AND (:villeDepart IS NULL OR LOWER(l.villeDepart) = LOWER(:villeDepart))
             AND (:villeArrivee IS NULL OR LOWER(l.villeArrivee) = LOWER(:villeArrivee))
           ORDER BY l.villeDepart, l.villeArrivee, l.type
           """)
    List<Ligne> rechercher(@Param("type") TypeLigne type,
                           @Param("villeDepart") String villeDepart,
                           @Param("villeArrivee") String villeArrivee);

    /** Pour l'administration : toutes les lignes, actives ou non. */
    List<Ligne> findAllByOrderByTypeAscVilleDepartAscVilleArriveeAsc();

    long countByActiveTrue();
}