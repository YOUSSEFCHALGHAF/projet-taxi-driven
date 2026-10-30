package com.transporttn.backend.service;

import com.transporttn.backend.entity.Driver;
import com.transporttn.backend.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Algorithme de matching chauffeur pour les reservations Taxi.
 * Calcule la distance (formule de Haversine) entre le point de depart du client
 * et chaque chauffeur disponible, puis retourne le plus proche.
 *
 * Note: pour un plus grand nombre de chauffeurs, ceci devrait etre remplace par une
 * requete spatiale (ex: MySQL avec index SPATIAL, ou Firestore GeoQueries), mais
 * une recherche lineaire en memoire est suffisante pour le perimetre de ce projet.
 */
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final DriverRepository driverRepository;

    private static final double EARTH_RADIUS_KM = 6371.0;

    public Optional<Driver> findNearestAvailableDriver(double pickupLat, double pickupLng) {
        List<Driver> availableDrivers = driverRepository.findByAvailableTrue();

        return availableDrivers.stream()
                .filter(d -> d.getLatitude() != null && d.getLongitude() != null)
                .min((d1, d2) -> Double.compare(
                        haversineDistanceKm(pickupLat, pickupLng, d1.getLatitude(), d1.getLongitude()),
                        haversineDistanceKm(pickupLat, pickupLng, d2.getLatitude(), d2.getLongitude())
                ));
    }

    public double haversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
