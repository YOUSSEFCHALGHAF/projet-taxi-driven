package tn.transporttn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.transporttn.model.Utilisateur;
import tn.transporttn.model.enums.Role;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; 
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

      /**
     * Un jeton FCM identifie un navigateur : il ne doit appartenir qu'à un seul compte.
     * Utilisé aussi pour oublier un jeton expiré.
     */
@Modifying(flushAutomatically = true, clearAutomatically = true)
@Transactional
@Query("UPDATE Utilisateur u SET u.fcmToken = null WHERE u.fcmToken = :jeton")
int effacerJetonFcm(@Param("jeton") String jeton);
    Optional<Utilisateur> findByTelephone(String telephone);

    
    boolean existsByTelephone(String telephone);

    boolean existsByEmail(String email);

    List<Utilisateur> findByRole(Role role);

    long countByRole(Role role);
}