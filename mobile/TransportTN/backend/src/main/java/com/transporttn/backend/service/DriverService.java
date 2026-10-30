package com.transporttn.backend.service;

import com.transporttn.backend.entity.Driver;
import com.transporttn.backend.entity.User;
import com.transporttn.backend.exception.ResourceNotFoundException;
import com.transporttn.backend.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    @Transactional
    public void updateLocation(User user, double latitude, double longitude) {
        Driver driver = driverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profil chauffeur introuvable"));

        driver.setLatitude(latitude);
        driver.setLongitude(longitude);
        driverRepository.save(driver);
        // Note: pour un suivi GPS "toutes les 3 secondes" en temps reel cote client,
        // privilegier une ecriture directe sur Firestore depuis l'app Android (cf. cahier des charges).
        // Ce endpoint REST sert de source de verite cote backend / MySQL (historisation, audit).
    }

    @Transactional
    public void updateAvailability(User user, boolean available) {
        Driver driver = driverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profil chauffeur introuvable"));

        driver.setAvailable(available);
        driverRepository.save(driver);
    }
}
