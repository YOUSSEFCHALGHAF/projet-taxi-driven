package com.transporttn.backend.repository;

import com.transporttn.backend.entity.Ligne;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneRepository extends JpaRepository<Ligne, Long> {
    List<Ligne> findByModeAndActiveTrue(Ligne.Mode mode);
}
