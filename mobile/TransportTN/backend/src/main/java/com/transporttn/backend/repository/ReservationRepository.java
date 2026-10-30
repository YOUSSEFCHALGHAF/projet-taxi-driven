package com.transporttn.backend.repository;

import com.transporttn.backend.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Page<Reservation> findByClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);
    List<Reservation> findByDriverIdAndStatus(Long driverId, Reservation.Status status);
}
