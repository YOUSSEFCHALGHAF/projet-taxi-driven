package tn.transporttn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.transporttn.model.ReservationCollectif;

import java.util.List;

public interface ReservationCollectifRepository extends JpaRepository<ReservationCollectif, Long> {

    List<ReservationCollectif> findByDepartId(Long departId);

    List<ReservationCollectif> findByClientIdOrderByDateCreationDesc(Long clientId);
    boolean existsByDepartId(Long departId);
}