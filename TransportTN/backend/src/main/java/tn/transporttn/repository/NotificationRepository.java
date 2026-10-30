package tn.transporttn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.transporttn.model.Notification;
import java.util.Optional;
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByIdAndUtilisateurId(Long id, Long utilisateurId);
    Page<Notification> findByUtilisateurIdOrderByDateEnvoiDesc(Long utilisateurId, Pageable pageable);

    /** Nombre affiché sur la cloche de notifications. */
    long countByUtilisateurIdAndLueFalse(Long utilisateurId);

    @Modifying
    @Query("UPDATE Notification n SET n.lue = true WHERE n.utilisateur.id = :utilisateurId AND n.lue = false")
    int marquerToutesCommeLues(@Param("utilisateurId") Long utilisateurId);
}