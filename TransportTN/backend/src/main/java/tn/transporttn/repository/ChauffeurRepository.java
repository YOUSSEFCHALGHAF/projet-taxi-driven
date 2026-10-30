package tn.transporttn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tn.transporttn.model.Chauffeur;

import java.util.List;
import java.util.Optional;

public interface ChauffeurRepository extends JpaRepository<Chauffeur, Long> {

        /**
     * Passe le chauffeur à "indisponible" SEULEMENT s'il est encore disponible.
     * Renvoie 1 si l'opération a réussi, 0 si un autre client l'a pris entre-temps.
     * C'est une seule requête SQL : MySQL garantit qu'un seul client gagne.
     */
    @Modifying
    @Query("UPDATE Chauffeur c SET c.disponible = false WHERE c.id = :id AND c.disponible = true")
    int reserverSiDisponible(@Param("id") Long id);

    @Query("SELECT c FROM Chauffeur c JOIN FETCH c.utilisateur u ORDER BY u.nom, u.prenom")
    List<Chauffeur> findAllAvecUtilisateur();

    boolean existsByMatricule(String matricule);

    boolean existsByNumeroPermis(String numeroPermis);

    long countByDisponibleTrue();
    
    List<Chauffeur> findByDisponibleTrue();

    Optional<Chauffeur> findByUtilisateurId(Long utilisateurId);

    /** Chauffeurs disponibles ayant une position connue (pour la recherche du plus proche). */
    @Query("""
           SELECT c FROM Chauffeur c
           JOIN FETCH c.utilisateur
           WHERE c.disponible = true
             AND c.latitude IS NOT NULL
             AND c.longitude IS NOT NULL
           """)
    List<Chauffeur> findDisponiblesAvecPosition();
}