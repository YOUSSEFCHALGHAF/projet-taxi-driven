package tn.transporttn.dto.taxi;

import java.math.BigDecimal;

public record EstimationResponse(
        double distanceKm,
        BigDecimal prixEstime,
        int dureeEstimeeMinutes,
        int chauffeursDisponibles
) {
}