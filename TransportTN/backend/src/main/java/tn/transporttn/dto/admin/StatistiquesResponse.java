package tn.transporttn.dto.admin;

import tn.transporttn.model.enums.StatutReservation;

import java.math.BigDecimal;
import java.util.Map;

public record StatistiquesResponse(
        long clients,
        long chauffeurs,
        long chauffeursDisponibles,
        long lignesActives,
        long departsAVenir,
        long reservationsTotal,
        long reservationsTaxi,
        long reservationsCollectif,
        long reservationsBus,
        long reservationsAujourdhui,
        Map<StatutReservation, Long> reservationsParStatut,
        long ticketsEmis,
        long ticketsUtilises,
        BigDecimal montantTotalHorsAnnulations
) {
}