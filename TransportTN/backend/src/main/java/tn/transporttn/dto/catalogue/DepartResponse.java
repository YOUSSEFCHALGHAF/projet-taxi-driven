package tn.transporttn.dto.catalogue;

import tn.transporttn.model.Depart;
import tn.transporttn.model.Ligne;
import tn.transporttn.model.enums.TypeLigne;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DepartResponse(
        Long id,
        Long ligneId,
        TypeLigne type,
        String itineraire,
        String stationDepart,
        String stationArrivee,
        LocalDateTime dateHeure,
        LocalDateTime arriveeEstimee,
        int placesTotal,
        int placesRestantes,
        boolean complet,
        BigDecimal prix
) {
    public static DepartResponse depuis(Depart d) {
        Ligne l = d.getLigne();
        return new DepartResponse(d.getId(), l.getId(), l.getType(), l.getItineraire(),
                l.getStationDepart(), l.getStationArrivee(), d.getDateHeure(),
                d.getDateHeure().plusMinutes(l.getDureeMinutes()),
                d.getPlacesTotal(), d.getPlacesRestantes(), d.estComplet(), l.getPrix());
    }
}