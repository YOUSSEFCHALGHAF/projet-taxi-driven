package com.transporttn.backend.repository;

import com.transporttn.backend.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByUserId(Long userId);

    /**
     * Recupere les chauffeurs disponibles pour l'algorithme de matching.
     * Le calcul de distance (Haversine) est fait en memoire dans MatchingService.
     */
    List<Driver> findByAvailableTrue();
}
