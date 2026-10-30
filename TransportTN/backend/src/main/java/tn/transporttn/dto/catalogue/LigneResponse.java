package tn.transporttn.dto.catalogue;

import tn.transporttn.model.Ligne;
import tn.transporttn.model.enums.TypeLigne;

import java.math.BigDecimal;

public record LigneResponse(
        Long id,
        TypeLigne type,
        String villeDepart,
        String villeArrivee,
        String stationDepart,
        String stationArrivee,
        BigDecimal prix,
        int capacite,
        int dureeMinutes,
        boolean active
) {
    public static LigneResponse depuis(Ligne l) {
        return new LigneResponse(l.getId(), l.getType(), l.getVilleDepart(), l.getVilleArrivee(),
                l.getStationDepart(), l.getStationArrivee(), l.getPrix(), l.getCapacite(),
                l.getDureeMinutes(), l.isActive());
    }
}