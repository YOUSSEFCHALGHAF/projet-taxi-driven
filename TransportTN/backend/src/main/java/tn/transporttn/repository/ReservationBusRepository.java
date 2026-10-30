package tn.transporttn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.transporttn.model.ReservationBus;

import java.util.List;

public interface ReservationBusRepository extends JpaRepository<ReservationBus, Long> {

    List<ReservationBus> findByDepartId(Long departId);

    List<ReservationBus> findByClientIdOrderByDateCreationDesc(Long clientId);
    boolean existsByDepartId(Long departId);
}